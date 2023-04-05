package org.muilab.noti.summary

import android.Manifest
import android.app.AppOpsManager
import android.content.*
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Process
import android.provider.Settings
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.ktx.Firebase
import org.muilab.noti.summary.database.room.APIKeyDatabase
import org.muilab.noti.summary.database.room.PromptDatabase
import org.muilab.noti.summary.model.UserCredit
import org.muilab.noti.summary.service.NotiListenerService
import org.muilab.noti.summary.service.NotiUnit
import org.muilab.noti.summary.ui.theme.NotiappTheme
import org.muilab.noti.summary.view.MainScreenView
import org.muilab.noti.summary.viewModel.APIKeyViewModel
import org.muilab.noti.summary.viewModel.PromptViewModel
import org.muilab.noti.summary.viewModel.SummaryViewModel
import java.util.*


const val maxCredit: Int = 50

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notiListenerIntent = Intent(this@MainActivity, NotiListenerService::class.java)
        startService(notiListenerIntent)

        val allNotiFilter = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS")
        registerReceiver(allNotiReturnReceiver, allNotiFilter)

        setUserId()
        val primaryLocale: Locale = applicationContext.resources.configuration.locales[0]
        val locale: String = primaryLocale.displayName

        Log.d("MainActivity", locale)
        
        setContent {
            NotiappTheme {
                MainScreenView(this, this, sumViewModel, promptViewModel, apiViewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (!isNotiListenerEnabled())
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
    }

    override fun onDestroy() {
        unregisterReceiver(allNotiReturnReceiver)
        super.onDestroy()
    }

    private val sumViewModel by viewModels<SummaryViewModel>()

    private val promptDatabase by lazy { PromptDatabase.getInstance(this) }
    private val promptViewModel by lazy { PromptViewModel(application, promptDatabase) }

    private val apiKeyDatabase  by lazy { APIKeyDatabase.getInstance(this) }
    private val apiViewModel by lazy { APIKeyViewModel(application, apiKeyDatabase ) }

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
                    sumViewModel.updateSummaryText(activeNotifications)
                }
            }
        }
    }

    private fun setUserId() {
        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userId: String = task.result
                Log.v("userId", userId)

                val sharedPref = this.getSharedPreferences("user_id", Context.MODE_PRIVATE)
                with(sharedPref.edit()) {
                    putString("user_id", userId)
                    apply()
                }

                val db = Firebase.firestore
                val docRef = db.collection("user-free-credit").document(userId)

                docRef.get()
                    .addOnSuccessListener { document ->
                        if (document != null) {
                            if(!document.exists()){
                                val data = UserCredit(userId, maxCredit)
                                docRef.set(data).addOnSuccessListener { Log.d("Installations", data.toString()) }
                            }
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.d("Installations", "get failed with ", exception)
                    }
            } else {
                Log.e("Installations", "Unable to get Installation ID")
            }
        }
    }
}
