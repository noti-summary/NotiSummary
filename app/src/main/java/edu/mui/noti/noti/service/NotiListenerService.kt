package edu.mui.noti.noti.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import edu.mui.noti.noti.database.room.CurrentDrawerDatabase
import edu.mui.noti.noti.util.TAG
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class NotiListenerService: NotificationListenerService() {
    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onDestroy() {
        val restartServiceIntent = Intent(applicationContext, NotiListenerService::class.java).also {
            it.setPackage(packageName)
        }
        val restartServicePendingIntent: PendingIntent = PendingIntent.getService(this, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        applicationContext.getSystemService(Context.ALARM_SERVICE)
        val alarmService: AlarmManager = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(AlarmManager.ELAPSED_REALTIME, System.currentTimeMillis() + 10000, restartServicePendingIntent)
        Log.d(TAG, "onDestroy")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return super.onBind(intent)
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

            val sharedPref = applicationContext.getSharedPreferences("user_id", Context.MODE_PRIVATE)
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
                Log.d(TAG, "insert drawerNoti")
                notiItem.logProperty()
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
        val sharedPref = applicationContext.getSharedPreferences("user_id", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("user_id", "000").toString()
        val notiItem = NotiItem(this, sbn, userId)
    }
}