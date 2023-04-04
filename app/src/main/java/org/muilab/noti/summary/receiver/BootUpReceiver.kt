package org.muilab.noti.summary.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import org.muilab.noti.summary.service.ReScheduleService

class BootUpReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        // This method is called when the BroadcastReceiver is receiving an Intent broadcast.
        Log.d("BootUpReceiver", "onReceive")

        if (intent.action.equals("android.intent.action.BOOT_COMPLETED")) {
            Log.d("BootUpReceiver", "onReceive: intent.action=BOOT_COMPLETED")
            val serviceIntent = Intent(context, ReScheduleService::class.java)
            context.startService(serviceIntent)
        }
    }
}