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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.tooling.preview.Preview
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


const val maxCredit: Int = 50

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!isNotiListenerEnabled()) {
            startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
        }

        /*
        if (!isUsageEnabled()) {
            startActivity(Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS))
        }
        */

        val notiListenerIntent = Intent(this@MainActivity, NotiListenerService::class.java)
        startService(notiListenerIntent)

        val allNotiFilter = IntentFilter("edu.mui.noti.summary.RETURN_ALLNOTIS")
        registerReceiver(allNotiReturnReceiver, allNotiFilter)

        setUserId()
        
        setContent {
            NotiappTheme {
                MainScreenView(this, this, sumViewModel, promptViewModel, apiViewModel)
            }
        }
    }

    override fun onDestroy() {
        unregisterReceiver(allNotiReturnReceiver)
        super.onDestroy()
    }

    private val sumViewModel by viewModels<SummaryViewModel>()

    private val promptDatabase by lazy { PromptDatabase.getInstance(this) }
    private val promptViewModel by lazy { PromptViewModel(application, promptDatabase = promptDatabase) }

    private val apiKeyDatabase  by lazy { APIKeyDatabase.getInstance(this) }
    private val apiViewModel by lazy { APIKeyViewModel(application, apiKeyDatabase = apiKeyDatabase ) }

    private fun isNotiListenerEnabled(): Boolean {
        val cn = ComponentName(this, NotiListenerService::class.java)
        val flat: String =
            Settings.Secure.getString(this.contentResolver, "enabled_notification_listeners")
        return cn.flattenToString() in flat
    }

    private fun chkPermissionOps(permission: String): Boolean {
        val appOps = this.getSystemService(APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), this.packageName)
        return if (mode == AppOpsManager.MODE_DEFAULT) {
            checkCallingOrSelfPermission(permission) == PackageManager.PERMISSION_GRANTED
        } else {
            mode == AppOpsManager.MODE_ALLOWED
        }
    }

    private fun isUsageEnabled(): Boolean {
        return chkPermissionOps(Manifest.permission.PACKAGE_USAGE_STATS)
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

@Composable
fun Greeting(name: String) {
    Text(text = "Hello $name!")
}

@Preview(showBackground = true)
@Composable
fun DefaultPreview() {
    NotiappTheme {
        Greeting("Android")
    }
}
