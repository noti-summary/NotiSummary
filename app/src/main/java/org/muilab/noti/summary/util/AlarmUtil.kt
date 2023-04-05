package org.muilab.noti.summary.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.util.Log
import org.muilab.noti.summary.model.Schedule
import org.muilab.noti.summary.receiver.AlarmReceiver
import java.util.*

fun addAlarm(context: Context, schedule: Schedule) {
    Log.d("addAlarm", "time=${schedule.time}")
    Log.d("primaryKey", schedule.primaryKey.toString())

    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, schedule.hour)
        set(Calendar.MINUTE, schedule.minute)
    }

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("time", schedule.time)

    val pendingIntent = PendingIntent.getBroadcast(context, schedule.primaryKey, intent, FLAG_IMMUTABLE)

    alarmManager.setExactAndAllowWhileIdle(
        /* type = */ AlarmManager.RTC_WAKEUP,
        /* triggerAtMillis = */ calendar.timeInMillis,
        /* operation = */ pendingIntent
    )
}

fun deleteAlarm(context: Context, schedule: Schedule) {
    Log.d("deleteAlarm", "time=${schedule.time}")
    Log.d("primaryKey", schedule.primaryKey.toString())

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("time", schedule.time)
    val pendingIntent = PendingIntent.getBroadcast(context, schedule.primaryKey, intent, FLAG_IMMUTABLE)

    alarmManager.cancel(pendingIntent)
}