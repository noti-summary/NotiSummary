package org.muilab.noti.summary

import android.app.ActivityManager
import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.os.IBinder
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import org.muilab.noti.summary.database.room.APIKeyDatabase
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.service.NotiListenerService
import org.muilab.noti.summary.service.SummaryService
import org.muilab.noti.summary.ui.theme.NotiappTheme
import org.muilab.noti.summary.util.getDateTime
import org.muilab.noti.summary.util.logUserAction
import org.muilab.noti.summary.view.MainScreenView
import org.muilab.noti.summary.view.userInit.*
import org.muilab.noti.summary.viewModel.APIKeyViewModel
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.ScheduleViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel


const val maxCredit: Int = 50

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val settings = FirebaseFirestoreSettings.Builder()
            .setPersistenceEnabled(true)
            .build()
        FirebaseFirestore.getInstance().firestoreSettings = settings

        val notiListenerIntent = Intent(this@MainActivity, NotiListenerService::class.java)

        val summaryServiceIntent = Intent(this, SummaryService::class.java)
        if (!isServiceRunning(SummaryService::class.java)) {
            Log.d("SummaryService", "Start service")
            startService(summaryServiceIntent)
        }
        bindService(summaryServiceIntent, summaryServiceConnection, Context.BIND_AUTO_CREATE)

        val sharedPref = this.getSharedPreferences("user", Context.MODE_PRIVATE)
        setContent {

            var initStatus by remember {
                mutableStateOf(sharedPref.getString("initStatus", "NOT_STARTED").toString())
            }

            NotiappTheme {
                when (initStatus) {
                    "NOT_STARTED" -> {
                        if (!NotificationManagerCompat.from(applicationContext).areNotificationsEnabled()) {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APP_NOTIFICATION_SETTINGS
                                putExtra(Settings.EXTRA_APP_PACKAGE, packageName)
                            }
                            startActivity(intent)
                        }
                        if (isNetworkConnected())
                            PrivacyPolicyDialog(onAgree = {
                                with(sharedPref.edit()) {
                                    putBoolean("agreeTerms", true)
                                    putString("initStatus", "OPEN_PERMISSION")
                                    apply()
                                }
                                initStatus = "OPEN_PERMISSION"
                            })
                        else
                            NetworkCheckDialog(applicationContext)
                    }
                    "OPEN_PERMISSION" -> {
                        AskPermissionDialog (
                            onAgree = {
                                if (isNotiListenerEnabled()) {
                                    with(sharedPref.edit()) {
                                        putString("initStatus", "AGREED")
                                        apply()
                                    }
                                    initStatus = "AGREED"
                                } else {
                                    Toast.makeText(
                                        this,
                                        getString(R.string.permission_not_yet_enabled),
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            },
                            OpenPermission = {
                                startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                            }
                        )
                    }
                    "AGREED" -> {
                        PersonalInformationScreen(
                            onContinue = { birthYear, gender, country, source ->
                                with(sharedPref.edit()) {
                                    putInt("birthYear", birthYear)
                                    putString("gender", gender)
                                    putString("country", country)
                                    putString("source", source)
                                    putString("initStatus", "USER_INFO_FILLED")
                                    apply()
                                }
                                initStatus = "USER_INFO_FILLED"
                            }
                        )
                    }
                    "USER_INFO_FILLED" -> {
                        if (setUserId())
                            initStatus = "SHOW_FILTER_NOTICE"
                    }
                    "SHOW_FILTER_NOTICE" -> {
                        if (!isNotiListenerEnabled())
                            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                        FilterNotify(onAgree = {
                            with(sharedPref.edit()) {
                                putString("initStatus", "USER_READY")
                                apply()
                            }
                            initStatus = "USER_READY"
                        })
                    }
                    "USER_READY" -> {
                        startService(notiListenerIntent)
                        MainScreenView(this, this, sumViewModel, promptViewModel, apiViewModel, scheduleViewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val sharedPref = this.getSharedPreferences("user", Context.MODE_PRIVATE)
        val stage = sharedPref.getString("initStatus", "NOT_STARTED")
        if (stage.equals("USER_READY"))
            logUserAction("lifeCycle", "appResume", applicationContext)
        if (stage.equals("USER_READY") && !isNotiListenerEnabled())
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))

        val allNotiFilter = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS")
        registerReceiver(allNotiReturnReceiver, allNotiFilter)
        val newStatusFilter = IntentFilter("edu.mui.noti.summary.UPDATE_STATUS")
        registerReceiver(newStatusReceiver, newStatusFilter)
        sumViewModel.updateStatusText()
    }

    override fun onPause() {
        val sharedPref = this.getSharedPreferences("user", Context.MODE_PRIVATE)
        if (sharedPref.getString("initStatus", "NOT_STARTED").equals("USER_READY"))
            logUserAction("lifeCycle", "appPause", applicationContext)
        unregisterReceiver(allNotiReturnReceiver)
        unregisterReceiver(newStatusReceiver)
        super.onPause()
    }

    override fun onDestroy() {
        unbindService(summaryServiceConnection)
        super.onDestroy()
    }

    private lateinit var summaryService: SummaryService

    private val sumViewModel by viewModels<SummaryViewModel>()

    private val promptDatabase by lazy { PromptDatabase.getInstance(this) }
    private val promptViewModel by lazy { PromptViewModel(application, promptDatabase) }

    private val apiKeyDatabase by lazy { APIKeyDatabase.getInstance(this) }
    private val apiViewModel by lazy { APIKeyViewModel(application, apiKeyDatabase) }

    private val scheduleDatabase by lazy { ScheduleDatabase.getInstance(this) }
    private val scheduleViewModel by lazy { ScheduleViewModel(application, scheduleDatabase) }

    private fun isNotiListenerEnabled(): Boolean {
        val cn = ComponentName(this, NotiListenerService::class.java)
        val flat: String? =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return (flat != null) && (cn.flattenToString() in flat)
    }

    private val summaryServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, service: IBinder) {
            Log.d("SummaryService", "Bind service")
            val binder = service as SummaryService.SummaryBinder
            summaryService = binder.getService()
            sumViewModel.setService(summaryService)
            Log.d("SummaryService", "Bind service")
        }

        override fun onServiceDisconnected(className: ComponentName) {

        }
    }

    private val allNotiReturnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.RETURN_ALLNOTIS") {
                val activeKeys = intent.getSerializableExtra("activeKeys") as? ArrayList<Pair<String, String>>
                if (activeKeys != null) {
                    sumViewModel.updateSummaryText(activeKeys, false)
                }
            }
        }
    }

    private val newStatusReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.UPDATE_STATUS") {
                sumViewModel.updateStatusText()
            }
        }
    }

    @Composable
    private fun setUserId(): Boolean {

        var initSuccess by remember { mutableStateOf(0) }

        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId: String = task.result
                Log.v("userId", userId)

                val sharedPref = this.getSharedPreferences("user", Context.MODE_PRIVATE)
                val birthYear = sharedPref.getInt("birthYear", 0)
                val gender = sharedPref.getString("gender", "Unknown").toString()
                val country = sharedPref.getString("country", "Unknown").toString()
                val source = sharedPref.getString("source", "Unknown").toString()
                val initTime = System.currentTimeMillis()

                val db = Firebase.firestore
                val docRef = db.collection("user").document(userId)

                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            if (!document.exists()) {
                                val userInfo = hashMapOf<String, Any>(
                                    "userId" to userId,
                                    "credit" to maxCredit,
                                    "birthYear" to birthYear,
                                    "gender" to gender,
                                    "country" to country,
                                    "source" to source,
                                    "initTime" to initTime,
                                    "dateTime" to getDateTime(initTime)
                                )
                                docRef.set(userInfo).addOnSuccessListener {
                                    Log.d(
                                        "Installations",
                                        userInfo.toString()
                                    )
                                    with(sharedPref.edit()) {
                                        putString("user_id", userId)
                                        putString("initStatus", "SHOW_FILTER_NOTICE")
                                        apply()
                                    }
                                    initSuccess = 1
                                }
                            }
                        } else
                            initSuccess = -1
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Installations", "get failed with ", exception)
                        initSuccess = -1
                    }
            } else {
                Log.e("Installations", "Unable to get Installation ID")
                initSuccess = -1
            }
        }
        if (initSuccess == -1)
            NetworkCheckDialog(applicationContext)
        return initSuccess == 1
    }

    private fun isNetworkConnected(): Boolean {
        val connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkCapabilities = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false

        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val manager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in manager.getRunningServices(Int.MAX_VALUE))
            if (serviceClass.name == service.service.className)
                return true
        return false
    }
}
