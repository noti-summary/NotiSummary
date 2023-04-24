package org.muilab.noti.summary.receiver

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import kotlinx.coroutines.*
import org.muilab.noti.summary.MainActivity
import org.muilab.noti.summary.R
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.model.Schedule
import org.muilab.noti.summary.util.TAG
import java.time.LocalTime
import java.util.*

class AlarmReceiver : BroadcastReceiver() {

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.

        Log.d(TAG, "onReceive")

        val refreshSummaryIntent = Intent("edu.mui.noti.summary.REQUEST_ALLNOTIS")
        LocalBroadcastManager.getInstance(context).sendBroadcast(refreshSummaryIntent)

        val hour = intent.getIntExtra("hour", -1)
        val minute = intent.getIntExtra("minute", -1)
        if (hour != -1 && minute != -1)
            scheduleNextDayAlarm(context, hour, minute)

        val sharedPref = context.getSharedPreferences("noti-send", Context.MODE_PRIVATE)
        val sendNotiOrNot = sharedPref.getBoolean("send_or_not", true)
        if (!sendNotiOrNot)
            return
        // If user don't want to send the notification, return directly

        val notiContentIntent = Intent(context, MainActivity::class.java)
        val pendingIntent =
            PendingIntent.getActivity(context, 0, notiContentIntent, PendingIntent.FLAG_IMMUTABLE)

        val builder = NotificationCompat.Builder(context, "Alarm")
            .setSmallIcon(R.drawable.quotation)
            .setContentTitle(context.getString(R.string.app_name))
            .setContentText(context.getString(R.string.noti_content))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("Alarm", "Remind", importance)

        val notificationManager = context.getSystemService(NotificationManager::class.java)
        notificationManager.createNotificationChannel(channel)

        val notificationManagerCompat = NotificationManagerCompat.from(context)
        notificationManagerCompat.notify(0, builder.build())
    }

    private fun scheduleNextDayAlarm(context: Context, hour: Int, minute: Int) {
        scope.launch {
            val schedule = getAlarmTimeFromDatabase(context, hour, minute) ?: return@launch

            val calendar = Calendar.getInstance().apply {
                timeZone = TimeZone.getDefault()
                set(Calendar.HOUR_OF_DAY, schedule.hour)
                set(Calendar.MINUTE, schedule.minute)
                set(Calendar.SECOND, 0)
                add(Calendar.DAY_OF_YEAR, 1)
            }

            if (!schedule.isEveryDay()) {
                val weekRepeating = schedule.calendarWeek()

                while (!weekRepeating.contains(calendar.get(Calendar.DAY_OF_WEEK))) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
            }

            Log.d("scheduleNextDayAlarm", "${calendar.time}")

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

            val intent = Intent(context, AlarmReceiver::class.java)
            intent.putExtra("hour", schedule.hour)
            intent.putExtra("minute", schedule.minute)
            val pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent,
                FLAG_IMMUTABLE or FLAG_UPDATE_CURRENT
            )

            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        }
    }

    private suspend fun getAlarmTimeFromDatabase(context: Context, hour: Int, minute: Int): Schedule? {
        val scheduleDatabase by lazy { ScheduleDatabase.getInstance(context) }
        val scheduleDao = scheduleDatabase.scheduleDao()

        return withContext(Dispatchers.IO) {
            scheduleDao.getScheduleByTime(hour, minute)
        }
    }
}