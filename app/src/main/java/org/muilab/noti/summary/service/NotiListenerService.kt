package org.muilab.noti.summary.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.muilab.noti.summary.util.TAG

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

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val allNotiFilter = IntentFilter("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        val allNotiFilterScheduled = IntentFilter("edu.mui.noti.summary.REQUEST_ALLNOTIS_SCHEDULED")
        LocalBroadcastManager.getInstance(this).registerReceiver(allNotiRequestReceiver, allNotiFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(allNotiRequestReceiver, allNotiFilterScheduled)
    }

    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, NotiListenerService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
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

    }

    fun getNotiUnits(): ArrayList<NotiUnit> {

        val notiUnits = activeNotifications.mapIndexed { idx, sbn ->
            NotiUnit(applicationContext, sbn, idx)
        }.filter{ it.title != "null" && it.content != "null" }.toCollection(ArrayList())
        return notiUnits
    }
}