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
    val time = String.format("%02d:%02d", hour, minute)
    Log.d("addAlarm", "time=$time")

    val calendar = Calendar.getInstance().apply {
        timeZone = TimeZone.getDefault()
        set(Calendar.HOUR_OF_DAY, hour)
        set(Calendar.MINUTE, minute)
    }

    val alarmRequestCodePref = context.getSharedPreferences("AlarmRCPref", Context.MODE_PRIVATE)
    var requestCode = alarmRequestCodePref.getInt("alarm_request_code", 0)

    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("time", time)

    ++requestCode
    val pendingIntent = PendingIntent.getBroadcast(context, requestCode, intent, FLAG_IMMUTABLE)

    alarmManager.setExactAndAllowWhileIdle(
        /* type = */ AlarmManager.RTC_WAKEUP,
        /* triggerAtMillis = */ calendar.timeInMillis,
        /* operation = */ pendingIntent
    )

    alarmRequestCodePref.edit().putInt("alarm_request_code", requestCode).apply()
}

fun deleteAlarm(context: Context, hour: Int, minute: Int) {
    val time = String.format("%02d:%02d", hour, minute)
    Log.d("deleteAlarm", "time=$time")
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val intent = Intent(context, AlarmReceiver::class.java)
    intent.putExtra("time", time)
    val pendingIntent = PendingIntent.getBroadcast(context, 0, intent, FLAG_IMMUTABLE)

    alarmManager.cancel(pendingIntent)
}