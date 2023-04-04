package org.muilab.noti.summary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.asLiveData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.util.addAlarm

class BootUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d("BootUpReceiver", "onReceive")

        if (intent.action.equals("android.intent.action.BOOT_COMPLETED")) {

            // TODO: This method cannot work. Need to Rewrite in another way
            CoroutineScope(Dispatchers.IO).launch {
                val scheduleDatabase = ScheduleDatabase.getInstance(context)
                val scheduleDao = scheduleDatabase.scheduleDao()

                val allSchedule = scheduleDao.getAllSchedule().asLiveData().value

                if (allSchedule != null) {
                    Log.d("BootUpReceiver", allSchedule.joinToString())
                    for (schedule in allSchedule) {
                        val hour = schedule.split(":")[0].toInt()
                        val minute = schedule.split(":")[1].toInt()
                        addAlarm(context, hour, minute)
                    }
                }
            }
        }
    }
}