package org.muilab.noti.summary.util

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.content.Context
import android.content.Intent
import android.util.Log
import org.muilab.noti.summary.receiver.AlarmReceiver
import java.util.*

fun addAlarm(context: Context, hour: Int, minute: Int) {
    Log.d("addAlarm", "hour:minute=$hour:$minute")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("time", "$hour:$minute")
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE)

    alarmManager.setExactAndAllowWhileIdle(
        /* type = */ AlarmManager.RTC_WAKEUP,
        /* triggerAtMillis = */ calendar.timeInMillis,
        /* operation = */ pendingIntent
    )
}

fun deleteAlarm(context: Context, hour: Int, minute: Int) {
    Log.d("deleteAlarm", "hour:minute=$hour:$minute")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("time", "$hour:$minute")
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE)

    alarmManager.cancel(pendingIntent)
}