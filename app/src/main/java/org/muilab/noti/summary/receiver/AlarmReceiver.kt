package org.muilab.noti.summary.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.asLiveData
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.model.Schedule
import java.time.LocalTime
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        Log.d("AlarmReceiver", "onReceive")

        val refreshSummaryIntent = Intent("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshSummaryIntent)

        scheduleNextDayAlarm(context)

        val builder = NotificationCompat.Builder(context, "Alarm")
            .setSmallIcon(R.mipmap.ic_launcher_foreground)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.noti_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("Alarm", "Remind", importance)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(0, builder.build())
    }

    private fun scheduleNextDayAlarm(context: Context) {
        val alarmSchedules = getAlarmTimeFromDatabase(context) ?: return

        val now = LocalTime.now()
        var nextAlarmTime: LocalTime? = null
        for (alarmSchedule in alarmSchedules) {
            val alarmTime = LocalTime.of(alarmSchedule.hour, alarmSchedule.minute)
            if (alarmTime > now) break
            nextAlarmTime = alarmTime
        }

        // If there are no future alarm times, schedule the first alarm for tomorrow
        if (nextAlarmTime == null) {
            nextAlarmTime = LocalTime.of(alarmSchedules.first().hour, alarmSchedules.first().minute)
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val calendar = Calendar.getInstance().apply {
            timeInMillis = System.currentTimeMillis()
            set(Calendar.HOUR_OF_DAY, nextAlarmTime!!.hour)
            set(Calendar.MINUTE, nextAlarmTime.minute)
            add(Calendar.DAY_OF_YEAR, 1)
        }

        val intent = Intent(context, AlarmReceiver::class.java)
        intent.putExtra(
            "time",
            String.format("%02d:%02d", nextAlarmTime!!.hour, nextAlarmTime.minute)
        )
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            pendingIntent
        )
    }

    private fun getAlarmTimeFromDatabase(context: Context): List<Schedule>? {
        val scheduleDatabase by lazy { ScheduleDatabase.getInstance(context) }
        val scheduleDao = scheduleDatabase.scheduleDao()

        return scheduleDao.getSortedSchedules().asLiveData().value
    }
}