package org.muilab.noti.summary.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.*
import android.content.pm.PackageManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.compose.runtime.mutableStateMapOf
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.muilab.noti.summary.MainActivity
import org.muilab.noti.summary.R
import org.muilab.noti.summary.model.NotiUnit
import org.muilab.noti.summary.model.UserCredit
import org.muilab.noti.summary.util.*
import org.muilab.noti.summary.view.home.SummaryResponse
import java.io.IOException
import java.io.InterruptedIOException
import java.util.concurrent.TimeUnit

class SummaryService : Service(), LifecycleOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry

    private val dotenv = dotenv {
        directory = "./assets"
        filename = "env"
    }

    private val serverURL = dotenv["SUMMARY_URL"]
    private val mediaType = "application/json; charset=utf-8".toMediaType()

    // Binder given to clients
    private val binder = SummaryBinder()

    private lateinit var summaryPref: SharedPreferences
    private lateinit var promptPref: SharedPreferences
    private lateinit var apiPref: SharedPreferences
    private lateinit var appFilterPref: SharedPreferences
    private var statusText = ""
    private var notiInProcess = arrayListOf<NotiUnit>()

    private fun getPostContent(activeNotifications: ArrayList<NotiUnit>): String {

        val sb = StringBuilder()
        activeNotifications.shuffle()
        activeNotifications.forEach { noti ->

            val filterMap = mutableMapOf<String, String>()
            filterMap[getString(R.string.application_name)] = "App: ${noti.appName}"
            filterMap[getString(R.string.time)] = "Time: ${noti.time}"
            filterMap[getString(R.string.title)] = "Title: ${noti.title}"
            filterMap[getString(R.string.content)] = "Content: ${noti.content}"

            val notiFilterPrefs = getSharedPreferences("noti_filter", Context.MODE_PRIVATE)
            val input = filterMap.filter { attribute -> notiFilterPrefs.getBoolean(attribute.key, true) }
                    .values
                    .joinToString(separator = ", ")

            sb.append("$input\n")
        }
        return sb.toString()
    }

    suspend fun sendToServer(
        activeNotifications: ArrayList<NotiUnit>,
        isScheduled: Boolean
    ): String {
        return withContext(Dispatchers.IO) {

            val appFilterMap = getAppFilter()
            val summarizedNotifications = activeNotifications.filter {
                noti -> appFilterMap[noti.pkgName] == true
            }.sortedBy { it.index }.toCollection(ArrayList())

            notiInProcess = summarizedNotifications
            updateStatusText(getString(SummaryResponse.GENERATING.message))

            var responseStr = ""

            if (notiInProcess.isNotEmpty()) {

                val client = OkHttpClient.Builder()
                    .connectTimeout(180, TimeUnit.SECONDS)
                    .writeTimeout(180, TimeUnit.SECONDS)
                    .readTimeout(180, TimeUnit.SECONDS)
                    .callTimeout(180, TimeUnit.SECONDS)
                    .build()

                data class GPTRequest(val prompt: String, val content: String)
                data class GPTRequestWithKey(
                    val prompt: String,
                    val content: String,
                    val key: String
                )

                val userAPIKey = apiPref.getString("userAPIKey", getString(R.string.system_key))!!

                val requestURL = if (userAPIKey == getString(R.string.system_key)) {
                    serverURL
                } else {
                    "$serverURL/key"
                }

                val postContent = getPostContent(summarizedNotifications)
                val prompt = promptPref.getString(
                    "curPrompt",
                    getString(R.string.default_summary_prompt)
                ) as String
                Log.d("sendToServer", "current prompt: $prompt")

                @Suppress("IMPLICIT_CAST_TO_ANY")
                val gptRequest = if (userAPIKey == getString(R.string.system_key)) {
                    GPTRequest(prompt, postContent)
                } else {
                    GPTRequestWithKey(prompt, postContent, userAPIKey)
                }

                val postBody = Gson().toJson(gptRequest)

                val request = Request.Builder()
                    .url(requestURL)
                    .post(postBody.toRequestBody(mediaType))
                    .build()

                try {
                    val submitTime = System.currentTimeMillis()
                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        val responseText =
                            response.body?.string()?.replace("\\n", "\r\n")?.replace("\\", "")
                                ?.removeSurrounding("\"")
                        val summary = ChineseConverter.convert(
                            responseText,
                            ConversionType.S2TWP,
                            applicationContext
                        )
                        if (summary != null) {

                            if (userAPIKey == getString(R.string.system_key))
                                subtractCredit()
                            logUserAction("genSummary", "Success", applicationContext)

                            with(summaryPref.edit()) {
                                putString("resultValue", summary)
                                putLong("submitTime", submitTime)
                                putBoolean("isScheduled", isScheduled)
                                putString("prompt", prompt)

                                val notiDrawerJson = Gson().toJson(summarizedNotifications)
                                putString("notiDrawer", notiDrawerJson)

                                val notiData = summarizedNotifications.map {
                                    SummaryNoti(it)
                                }
                                val notiDataJson = Gson().toJson(notiData)
                                putString("notiData", notiDataJson)

                                val notiFilterPrefs =
                                    getSharedPreferences("noti_filter", Context.MODE_PRIVATE)

                                val notiScope = mutableMapOf<String, Boolean>()
                                notiScope["appName"] = notiFilterPrefs.getBoolean(
                                    getString(R.string.application_name),
                                    true
                                )
                                notiScope["time"] = notiFilterPrefs.getBoolean(
                                    getString(R.string.time),
                                    true
                                )
                                notiScope["title"] = notiFilterPrefs.getBoolean(
                                    getString(R.string.title),
                                    true
                                )
                                notiScope["content"] = notiFilterPrefs.getBoolean(
                                    getString(R.string.content),
                                    true
                                )
                                val notiScopeJson = Gson().toJson(notiScope)
                                putString("notiScope", notiScopeJson)

                                val summaryLength = getWordCount(summary)
                                val summaryLengthJson = Gson().toJson(summaryLength)
                                putString("summaryLength", summaryLengthJson)

                                putString("removedNotis", "{}")
                                putInt("rating", 0)

                                apply()
                            }
                            logSummary(applicationContext)
                            responseStr = summary
                        } else {
                            logUserAction("genSummary", "ServerError", applicationContext)
                            responseStr = getString(SummaryResponse.SERVER_ERROR.message)
                        }
                    } else {
                        response.body?.let {
                            val responseBody = it.string()
                            Log.i("ServerResponse", responseBody)
                            responseStr =
                                if (responseBody.contains("You didn't provide an API key") ||
                                    responseBody.contains("Incorrect API key provided")
                                ) {
                                    logUserAction("genSummary", "KeyError", applicationContext)
                                    getString(SummaryResponse.APIKEY_ERROR.message)
                                } else if (responseBody.contains("exceeded your current quota")) {
                                    logUserAction("genSummary", "NoQuota", applicationContext)
                                    getString(SummaryResponse.QUOTA_ERROR.message)
                                } else {
                                    logUserAction("genSummary", "ServerError", applicationContext)
                                    getString(SummaryResponse.SERVER_ERROR.message)
                                }
                        } ?: let {
                            logUserAction("genSummary", "ServerError", applicationContext)
                            responseStr = getString(SummaryResponse.SERVER_ERROR.message)
                        }
                    }
                    response.close()
                } catch (e: InterruptedIOException) {
                    Log.i("InterruptedIOException", e.toString())
                    logUserAction("genSummary", "ServerTimeout", applicationContext)
                    responseStr = getString(SummaryResponse.TIMEOUT_ERROR.message)
                } catch (e: IOException) {
                    Log.i("IOException", e.toString())
                    logUserAction("genSummary", "NetworkError", applicationContext)
                    responseStr = getString(SummaryResponse.NETWORK_ERROR.message)
                } catch (e: Exception) {
                    Log.i("Exception in sendToServer", e.toString())
                    logUserAction("genSummary", "ServerError", applicationContext)
                    responseStr = getString(SummaryResponse.SERVER_ERROR.message)
                }
            } else {
                responseStr = getString(SummaryResponse.NO_NOTIFICATION.message)
            }
            updateStatusText(responseStr)
            statusText = ""
            notiInProcess = arrayListOf()
            if (isScheduled)
                notifySummary(responseStr)
            responseStr // return value
        }
    }

    private fun subtractCredit() {
        val sharedPref = getSharedPreferences("user", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()

        val db = Firebase.firestore
        val docRef = db.collection("user").document(userId)
        docRef.get().addOnSuccessListener { document ->
            if (document != null) {
                val res = document.toObject<UserCredit>()!!
                docRef.update("credit", res.credit - 1)
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        val allNotiFilterScheduled = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS_SCHEDULED")
        registerReceiver(allNotiReturnReceiver, allNotiFilterScheduled)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        summaryPref = getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
        promptPref = getSharedPreferences("PromptPref", Context.MODE_PRIVATE)
        apiPref = getSharedPreferences("ApiPref", Context.MODE_PRIVATE)
        appFilterPref = getSharedPreferences("app_filter", Context.MODE_PRIVATE)

        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    inner class SummaryBinder : Binder() {
        fun getService(): SummaryService {
            return this@SummaryService
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        unregisterReceiver(allNotiReturnReceiver)
        super.onDestroy()
    }

    private fun getAppFilter(): Map<String, Boolean> {

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER)

        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(PackageManager.ApplicationInfoFlags.of(0))
        } else {
            packageManager.getInstalledApplications(0)
        }

        val launcherActivities = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0))
        } else {
            packageManager.queryIntentActivities(mainIntent, 0)
        }

        val packagesWithLauncher = mutableListOf<String>()
        for (activity in launcherActivities) {
            packagesWithLauncher.add(activity.activityInfo.packageName)
        }
        packagesWithLauncher.remove("org.muilab.noti.summary")
        packagesWithLauncher.add("android")

        val appFilterMap = mutableStateMapOf<String, Boolean>()
        // Initialize all package names with true as the default state
        packages.forEach { packageInfo ->
            if (packageInfo.packageName in packagesWithLauncher) {
                appFilterMap[packageInfo.packageName] = true
            }
        }

        appFilterPref.all.forEach { (packageName, state) ->
            if (state is Boolean) {
                appFilterMap[packageName] = state
            }
        }

        return appFilterMap
    }

    fun getStatusText(): String {
        return statusText
    }

    fun getNotiInProcess(): ArrayList<NotiUnit> {
        return notiInProcess
    }

    private fun updateStatusText(newStatus: String) {
        statusText = newStatus
        val updateIntent = Intent("edu.mui.noti.summary.UPDATE_STATUS")
        updateIntent.putExtra("newStatus", newStatus)
        updateIntent.putParcelableArrayListExtra("activeNotis", notiInProcess)
        sendBroadcast(updateIntent)
    }

    private fun notifySummary(responseStr: String) {
        val sharedPref = getSharedPreferences("noti-send", Context.MODE_PRIVATE)
        val sendNotiOrNot = sharedPref.getBoolean("send_or_not", true)
        if (!sendNotiOrNot)
            return
        // If user don't want to send the notification, return directly

        val notiContentIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(applicationContext, 0, notiContentIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(applicationContext, "Notify")
            .setSmallIcon(R.drawable.quotation)
            .setContentTitle(getString(R.string.noti_content))
            .setContentText(responseStr)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("Notify", "Notify", importance)

        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(0, builder.build())
    }

    private val allNotiReturnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.RETURN_ALLNOTIS_SCHEDULED") {
                val activeNotifications = intent.getParcelableArrayListExtra<NotiUnit>("activeNotis")
                if (activeNotifications != null) {
                    CoroutineScope(Dispatchers.IO).launch {
                        sendToServer(activeNotifications, true)
                    }
                }
            }
        }
    }
}