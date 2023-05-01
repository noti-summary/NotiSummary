package org.muilab.noti.summary.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.muilab.noti.summary.util.TAG
import org.muilab.noti.summary.util.logSummary

class NotiListenerService: NotificationListenerService() {

    private var connected: Boolean = false

    override fun onListenerConnected() {
        super.onListenerConnected()
        Log.i(TAG, "Connected!")
        connected = true
    }

    override fun onListenerDisconnected() {
        connected = false
        super.onListenerDisconnected()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    private val allNotiRequestReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == "edu.mui.noti.summary.REQUEST_ALLNOTIS") {
                val broadcastIntent = Intent("edu.mui.noti.summary.RETURN_ALLNOTIS")
                broadcastIntent.putParcelableArrayListExtra("activeNotis", getNotiUnits())
                sendBroadcast(broadcastIntent)
            } else if (intent?.action == "edu.mui.noti.summary.REQUEST_ALLNOTIS_SCHEDULED") {
                val broadcastIntent = Intent("edu.mui.noti.summary.RETURN_ALLNOTIS_SCHEDULED")
                broadcastIntent.putParcelableArrayListExtra("activeNotis", getNotiUnits())
                sendBroadcast(broadcastIntent)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val allNotiFilter = IntentFilter("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        val allNotiFilterScheduled = IntentFilter("edu.mui.noti.summary.REQUEST_ALLNOTIS_SCHEDULED")
        LocalBroadcastManager.getInstance(this).registerReceiver(allNotiRequestReceiver, allNotiFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(allNotiRequestReceiver, allNotiFilterScheduled)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, NotiListenerService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis() + 10000, restartServicePendingIntent)
        unregisterReceiver(allNotiRequestReceiver)
        Log.d(TAG, "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {

    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        fun getNotificationReasonString(reason: Int): String {
            return when(reason) {
                REASON_APP_CANCEL -> "REASON_APP_CANCEL"
                REASON_APP_CANCEL_ALL -> "REASON_APP_CANCEL_ALL"
                REASON_ASSISTANT_CANCEL -> "REASON_ASSISTANT_CANCEL"
                REASON_CANCEL -> "REASON_CANCEL"
                REASON_CANCEL_ALL -> "REASON_CANCEL_ALL"
                REASON_CHANNEL_BANNED -> "REASON_CHANNEL_BANNED"
                REASON_CHANNEL_REMOVED -> "REASON_CHANNEL_REMOVED"
                REASON_CLEAR_DATA -> "REASON_CLEAR_DATA"
                REASON_CLICK -> "REASON_CLICK"
                REASON_ERROR -> "REASON_ERROR"
                REASON_GROUP_OPTIMIZATION -> "REASON_GROUP_OPTIMIZATION"
                REASON_GROUP_SUMMARY_CANCELED -> "REASON_GROUP_SUMMARY_CANCELED"
                REASON_LISTENER_CANCEL -> "REASON_LISTENER_CANCEL"
                REASON_LISTENER_CANCEL_ALL -> "REASON_LISTENER_CANCEL_ALL"
                REASON_PACKAGE_BANNED -> "REASON_PACKAGE_BANNED"
                REASON_PACKAGE_CHANGED -> "REASON_PACKAGE_CHANGED"
                REASON_PACKAGE_SUSPENDED -> "REASON_PACKAGE_SUSPENDED"
                REASON_PROFILE_TURNED_OFF -> "REASON_PROFILE_TURNED_OFF"
                REASON_SNOOZED -> "REASON_SNOOZED"
                REASON_TIMEOUT -> "REASON_TIMEOUT"
                REASON_UNAUTOBUNDLED -> "REASON_UNAUTOBUNDLED"
                REASON_USER_STOPPED -> "REASON_USER_STOPPED"
                else -> "UNKNOWN_REASON"
            }
        }

        val notiKey = sbn?.key as String
        val reasonStr = getNotificationReasonString(reason)

        val summarySharedPref = getSharedPreferences("SummaryPref", Context.MODE_PRIVATE)
        val prevRemovedNotisJson = summarySharedPref.getString("removedNotis", "{}")
        val removedNotisType = object : TypeToken<MutableMap<String, String>>() {}.type
        val removedNotis = Gson().fromJson<MutableMap<String, String>>(prevRemovedNotisJson, removedNotisType)
        removedNotis[notiKey] = reasonStr
        val newRemovedNotisJson = Gson().toJson(removedNotis)
        summarySharedPref.edit().putString("removedNotis", newRemovedNotisJson).apply()

        if (summarySharedPref.getLong("submitTime", 0) != 0L)
            logSummary(applicationContext)
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun getNotiUnits(): ArrayList<NotiUnit> {
        val notiUnits = activeNotifications.mapIndexed { idx, sbn ->
            NotiUnit(applicationContext, sbn, idx)
        }.filter{ it.title != "null" && it.content != "null" }.toCollection(ArrayList())
        return notiUnits
    }
}