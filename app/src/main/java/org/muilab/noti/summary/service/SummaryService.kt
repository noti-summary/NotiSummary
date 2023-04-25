package org.muilab.noti.summary.service

import android.app.Service
import android.content.*
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.gson.Gson
import com.zqc.opencc.android.lib.ChineseConverter
import com.zqc.opencc.android.lib.ConversionType
import io.github.cdimascio.dotenv.dotenv
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.muilab.noti.summary.R
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
    private var statusText = ""
    private var notiInProcess = arrayListOf<NotiUnit>()

    private fun getPostContent(activeNotifications: ArrayList<NotiUnit>): String {

        val sb = StringBuilder()
        activeNotifications.shuffle()
        activeNotifications.forEach { noti ->

            val filterMap = mutableMapOf<String, String>()
            filterMap[applicationContext.getString(R.string.application_name)] =
                "App: ${noti.appName}"
            filterMap[applicationContext.getString(R.string.time)] = "Time: ${noti.time}"
            filterMap[applicationContext.getString(R.string.title)] = "Title: ${noti.title}"
            filterMap[applicationContext.getString(R.string.content)] = "Content: ${noti.content}"

            val notiFilterPrefs =
                applicationContext.getSharedPreferences("noti_filter", Context.MODE_PRIVATE)
            val input =
                filterMap.filter { attribute -> notiFilterPrefs.getBoolean(attribute.key, true) }
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

            notiInProcess = activeNotifications
            updateStatusText(applicationContext.getString(SummaryResponse.GENERATING.message))

            var responseStr = ""

            val client = OkHttpClient.Builder()
                .connectTimeout(180, TimeUnit.SECONDS)
                .writeTimeout(180, TimeUnit.SECONDS)
                .readTimeout(180, TimeUnit.SECONDS)
                .callTimeout(180, TimeUnit.SECONDS)
                .build()

            data class GPTRequest(val prompt: String, val content: String)
            data class GPTRequestWithKey(val prompt: String, val content: String, val key: String)

            val userAPIKey =
                apiPref.getString("userAPIKey", applicationContext.getString(R.string.system_key))!!

            val requestURL = if (userAPIKey == applicationContext.getString(R.string.system_key)) {
                serverURL
            } else {
                "$serverURL/key"
            }

            val postContent = getPostContent(activeNotifications)
            val prompt = promptPref.getString(
                "curPrompt",
                applicationContext.getString(R.string.default_summary_prompt)
            ) as String
            Log.d("sendToServer@SummaryViewModel", "current prompt: $prompt")

            @Suppress("IMPLICIT_CAST_TO_ANY")
            val gptRequest = if (userAPIKey == applicationContext.getString(R.string.system_key)) {
                GPTRequest(prompt, postContent)
            } else {
                Log.d("sendToServer", "userAPIKey: $userAPIKey")
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

                        summaryPref.edit().putString("resultValue", summary).apply()
                        if (userAPIKey == applicationContext.getString(R.string.system_key))
                            subtractCredit()
                        logUserAction("genSummary", "Success", applicationContext)

                        with(summaryPref.edit()) {
                            putLong("submitTime", submitTime)
                            putBoolean("isScheduled", isScheduled)
                            putString("prompt", prompt)

                            val notiDrawerJson = Gson().toJson(activeNotifications)
                            putString("notiDrawer", notiDrawerJson)

                            val notiData = activeNotifications.map {
                                // TODO: Get length from server
                                SummaryNoti(it)
                            }
                            val notiDataJson = Gson().toJson(notiData)
                            putString("notiData", notiDataJson)

                            val notiFilterPrefs = applicationContext.getSharedPreferences(
                                "noti_filter",Context.MODE_PRIVATE
                            )

                            val notiScope = mutableMapOf<String, Boolean>()
                            notiScope["appName"] = notiFilterPrefs.getBoolean(
                                applicationContext.getString(R.string.application_name),
                                true
                            )
                            notiScope["time"] = notiFilterPrefs.getBoolean(
                                applicationContext.getString(R.string.time),
                                true
                            )
                            notiScope["title"] = notiFilterPrefs.getBoolean(
                                applicationContext.getString(R.string.title),
                                true
                            )
                            notiScope["content"] = notiFilterPrefs.getBoolean(
                                applicationContext.getString(R.string.content),
                                true
                            )
                            val notiScopeJson = Gson().toJson(notiScope)
                            putString("notiScope", notiScopeJson)

                            // TODO: Get length from server
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
                        responseStr = applicationContext.getString(SummaryResponse.SERVER_ERROR.message)
                    }
                } else {
                    response.body?.let {
                        val responseBody = it.string()
                        Log.i("ServerResponse", responseBody)
                        responseStr = if (responseBody.contains("You didn't provide an API key") ||
                            responseBody.contains("Incorrect API key provided")
                        ) {
                            logUserAction("genSummary", "KeyError", applicationContext)
                            applicationContext.getString(SummaryResponse.APIKEY_ERROR.message)
                        } else if (responseBody.contains("exceeded your current quota")) {
                            logUserAction("genSummary", "NoQuota", applicationContext)
                            applicationContext.getString(SummaryResponse.QUOTA_ERROR.message)
                        } else {
                            logUserAction("genSummary", "ServerError", applicationContext)
                            applicationContext.getString(SummaryResponse.SERVER_ERROR.message)
                        }
                    } ?: let {
                        logUserAction("genSummary", "ServerError", applicationContext)
                        responseStr = applicationContext.getString(SummaryResponse.SERVER_ERROR.message)
                    }
                }
                response.close()
            } catch (e: InterruptedIOException) {
                Log.i("InterruptedIOException", e.toString())
                logUserAction("genSummary", "ServerTimeout", applicationContext)
                responseStr = applicationContext.getString(SummaryResponse.TIMEOUT_ERROR.message)
            } catch (e: IOException) {
                Log.i("IOException", e.toString())
                logUserAction("genSummary", "NetworkError", applicationContext)
                responseStr = applicationContext.getString(SummaryResponse.NETWORK_ERROR.message)
            } catch (e: Exception) {
                Log.i("Exception in sendToServer", e.toString())
                logUserAction("genSummary", "ServerError", applicationContext)
                responseStr = applicationContext.getString(SummaryResponse.SERVER_ERROR.message)
            }

            updateStatusText(responseStr)
            statusText = ""
            notiInProcess = arrayListOf()
            responseStr
        }
    }

    private fun subtractCredit() {
        val sharedPref = applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)
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

        summaryPref = applicationContext.getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
        promptPref = applicationContext.getSharedPreferences("PromptPref", Context.MODE_PRIVATE)
        apiPref = applicationContext.getSharedPreferences("ApiPref", Context.MODE_PRIVATE)

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
        super.onDestroy()
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

    private val allNotiReturnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.RETURN_ALLNOTIS_SCHEDULED") {
                val activeNotifications = intent.getParcelableArrayListExtra<NotiUnit>("activeNotis")
                if (activeNotifications != null) {
                    lifecycleScope.launch { sendToServer(activeNotifications, true) }
                }
            }
        }
    }
}