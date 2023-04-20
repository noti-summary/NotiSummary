package org.muilab.noti.summary

import android.content.*
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreSettings
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import org.muilab.noti.summary.database.room.APIKeyDatabase
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.service.NotiListenerService
import org.muilab.noti.summary.service.NotiUnit
import org.muilab.noti.summary.ui.theme.NotiappTheme
import org.muilab.noti.summary.util.insertUserAction
import org.muilab.noti.summary.view.MainScreenView
import org.muilab.noti.summary.view.userInit.FilterNotify
import org.muilab.noti.summary.view.userInit.NetworkCheckDialog
import org.muilab.noti.summary.view.userInit.PersonalInformationScreen
import org.muilab.noti.summary.view.userInit.PrivacyPolicyDialog
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
        startService(notiListenerIntent)

        val allNotiFilter = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS")
        val allNotiFilterScheduled = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS_SCHEDULED")
        registerReceiver(allNotiReturnReceiver, allNotiFilter)
        registerReceiver(allNotiReturnReceiver, allNotiFilterScheduled)

        val notiFilterPrefs = this.getSharedPreferences("noti_filter", Context.MODE_PRIVATE)
        if (!notiFilterPrefs.getBoolean(this.getString(R.string.content), false)) {
            with(notiFilterPrefs.edit()) {
                putBoolean(getString(R.string.content), false)
                apply()
            }
        }

        val sharedPref = this.getSharedPreferences("user", Context.MODE_PRIVATE)
        setContent {

            var initStatus by remember {
                mutableStateOf(sharedPref.getString("initStatus", "NOT_STARTED").toString())
            }

            NotiappTheme {
                when (initStatus) {
                    "NOT_STARTED" -> {
                        if (isNetworkConnected())
                            PrivacyPolicyDialog(onAgree = {
                                with(sharedPref.edit()) {
                                    putBoolean("agreeTerms", true)
                                    putString("initStatus", "AGREED")
                                    apply()
                                }
                                initStatus = "AGREED"
                            })
                        else
                            NetworkCheckDialog(applicationContext)
                    }
                    "AGREED" -> {
                        PersonalInformationScreen(
                            onContinue = { age, gender, country ->
                                with(sharedPref.edit()) {
                                    putInt("age", age)
                                    putString("gender", gender)
                                    putString("country", country)
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
                    "SHOW_FILTER_NOTICE" -> FilterNotify(onAgree = {
                        with(sharedPref.edit()) {
                            putString("initStatus", "USER_READY")
                            apply()
                        }
                        initStatus = "USER_READY"
                    })
                    "USER_READY" -> {
                        MainScreenView(this, this, sumViewModel, promptViewModel, apiViewModel, scheduleViewModel)
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        insertUserAction("lifeCycle", "appResume", "", applicationContext)
        if (!isNotiListenerEnabled())
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun onPause() {
        insertUserAction("lifeCycle", "appPause", "", applicationContext)
        super.onPause()
    }

    override fun onDestroy() {
        unregisterReceiver(allNotiReturnReceiver)
        super.onDestroy()
    }

    private val sumViewModel by viewModels<SummaryViewModel>()

    private val promptDatabase by lazy { PromptDatabase.getInstance(this) }
    private val promptViewModel by lazy { PromptViewModel(application, promptDatabase) }

    private val apiKeyDatabase by lazy { APIKeyDatabase.getInstance(this) }
    private val apiViewModel by lazy { APIKeyViewModel(application, apiKeyDatabase) }

    private val scheduleDatabase by lazy { ScheduleDatabase.getInstance(this) }
    private val scheduleViewModel by lazy { ScheduleViewModel(application, scheduleDatabase) }

    private fun isNotiListenerEnabled(): Boolean {
        val cn = ComponentName(this, NotiListenerService::class.java)
        val flat: String =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return cn.flattenToString() in flat
    }

    private val allNotiReturnReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.RETURN_ALLNOTIS") {
                val activeNotifications = intent.getParcelableArrayListExtra<NotiUnit>("activeNotis")
                if (activeNotifications != null) {
                    val curPrompt = promptViewModel.getCurPrompt()
                    sumViewModel.updateSummaryText(curPrompt, activeNotifications, false)
                }
            } else if (intent?.action == "edu.mui.noti.summary.RETURN_ALLNOTIS_SCHEDULED") {
                val activeNotifications = intent.getParcelableArrayListExtra<NotiUnit>("activeNotis")
                if (activeNotifications != null) {
                    val curPrompt = promptViewModel.getCurPrompt()
                    sumViewModel.updateSummaryText(curPrompt, activeNotifications, true)
                }
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
                val age = sharedPref.getInt("age", 0)
                val gender = sharedPref.getString("gender", "Unknown").toString()
                val country = sharedPref.getString("country", "Unknown").toString()

                val db = Firebase.firestore
                val docRef = db.collection("user").document(userId)

                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            if (!document.exists()) {
                                val userInfo = hashMapOf<String, Any>(
                                    "userId" to userId,
                                    "credit" to maxCredit,
                                    "age" to age,
                                    "gender" to gender,
                                    "country" to country
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
}
