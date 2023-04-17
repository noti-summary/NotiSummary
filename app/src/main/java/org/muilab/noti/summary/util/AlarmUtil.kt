package org.muilab.noti.summary.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_CANCEL_CURRENT
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.util.Log
import org.muilab.noti.summary.model.Schedule
import org.muilab.noti.summary.receiver.AlarmReceiver
import java.util.*

fun addAlarm(context: Context, schedule: Schedule) {
    Log.d("addAlarm", "time=${schedule.getTime()}")
    Log.d("primaryKey", schedule.primaryKey.toString())

    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, schedule.hour)
        set(Calendar.MINUTE, schedule.minute)
        set(Calendar.SECOND, 0)
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("hour", schedule.hour)
    intent.putExtra("minute", schedule.minute)

    val pendingIntent = PendingIntent.getBroadcast(
        context, schedule.primaryKey, intent, FLAG_IMMUTABLE or FLAG_CANCEL_CURRENT
    )

    alarmManager.setExactAndAllowWhileIdle(
        /* type = */ AlarmManager.RTC_WAKEUP,
        /* triggerAtMillis = */ calendar.timeInMillis,
        /* operation = */ pendingIntent
    )
    LogAlarm(context, "add", String.format("%02d:%02d", schedule.hour, schedule.minute))
}

fun deleteAlarm(context: Context, schedule: Schedule) {
    Log.d("deleteAlarm", "time=${schedule.getTime()}")
    Log.d("primaryKey", schedule.primaryKey.toString())

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("hour", schedule.hour)
    intent.putExtra("minute", schedule.minute)
    val pendingIntent = PendingIntent.getBroadcast(
        context, schedule.primaryKey, intent, FLAG_IMMUTABLE or FLAG_CANCEL_CURRENT
    )

    alarmManager.cancel(pendingIntent)
    LogAlarm(context, "delete", String.format("%02d:%02d", schedule.hour, schedule.minute))
}

fun LogAlarm(context: Context, action: String, scheduleTime: String) {
    val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("user_id", "000").toString()
    val timestamp = System.currentTimeMillis()
    val scheduler = Scheduler(userId, timestamp, action, scheduleTime)
    uploadData("scheduler", scheduler)
}