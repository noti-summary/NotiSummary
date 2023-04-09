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
import org.muilab.noti.summary.database.room.CurrentDrawerDatabase
import org.muilab.noti.summary.util.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

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
                val intent = Intent("edu.mui.noti.summary.RETURN_ALLNOTIS")
                intent.putParcelableArrayListExtra("activeNotis", getNotiUnits())
                sendBroadcast(intent)
            }
        }
    }

    fun isConnected(): Boolean {
        return connected
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        val allNotiFilter = IntentFilter("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(this).registerReceiver(allNotiRequestReceiver, allNotiFilter)
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

    private fun adHocRemove(notiItem: NotiItem): Boolean {
        val title = notiItem.getTitle()
        val content = notiItem.getContent()
        val flags = notiItem.getFlags()
        val packageName = notiItem.getPackageName()
        val notiId = notiItem.getSbnId()

        if (title == "null" && content == "null")
            return true
        if (packageName == "jp.naver.line.android" && notiId == 16880000)
            return true
        if (packageName == "com.google.android.gm" && flags?.and(512) != 0)
            return true
        if (packageName == "com.Slack" && flags != 16)
            return true
        return false
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        Log.d(TAG, "onNotificationPosted")
        try {
//            Log.d(TAG, "TAG: ${sbn?.tag}") // charging_state, ...

            if (sbn?.tag == null)
                return

            if (sbn.isOngoing) {
                Log.d(TAG, "posted ongoing noti")
                return
            }

            val sharedPref = applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userId = sharedPref.getString("user_id", "000").toString()
            val notiItem = NotiItem(this, sbn, userId)

            if (adHocRemove(notiItem))
                return

            notiItem.logProperty()
            val currentDrawerDao = CurrentDrawerDatabase.getInstance(applicationContext).currentDrawerDao()
            val drawerNoti = notiItem.makeDrawerNoti()
            GlobalScope.launch {
                if (drawerNoti.sortKey != "null")
                    currentDrawerDao.deleteByPackageSortKey(drawerNoti.packageName, drawerNoti.groupKey, drawerNoti.sortKey)
                currentDrawerDao.insert(drawerNoti)
                Log.d(TAG, "finish inserting the noti")
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onNotificationRemoved(
        sbn: StatusBarNotification?,
        rankingMap: RankingMap?,
        reason: Int
    ) {
        val sharedPref = applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()
        val notiItem = NotiItem(this, sbn, userId)
    }

    fun getNotiUnits(): ArrayList<NotiUnit> {
        val notiUnits = ArrayList<NotiUnit>()

        fun replaceChars(str: String): String {
            return str.replace("\n", " ")
                .replace(",", " ")
        }

        val appFilterPrefs = applicationContext.getSharedPreferences("app_filter", Context.MODE_PRIVATE)
        val userPref = applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)

        activeNotifications.forEach {
            val userId = userPref.getString("user_id", "000").toString()
            val notiItem = NotiItem(this, it, userId)
            val appName = replaceChars(notiItem.getAppName())
            val time = replaceChars(notiItem.getTimeStr())
            val title = replaceChars(notiItem.getTitle())
            val content = replaceChars(notiItem.getContent())

            if (appName == "null" || title == "null" || content == "null")
                return@forEach

            if (!appFilterPrefs.getBoolean(notiItem.getPackageName(), true))
                return@forEach
                
            notiUnits.add(NotiUnit(appName, time, title, content))
        }
        return notiUnits
    }
}