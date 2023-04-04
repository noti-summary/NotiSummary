package org.muilab.noti.summary.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.util.addAlarm

class ReScheduleService : Service(), LifecycleOwner {

    private lateinit var lifecycleRegistry: LifecycleRegistry
    private val scheduleDatabase by lazy { ScheduleDatabase.getInstance(applicationContext) }

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("ReScheduleService", "onStartCommand")
        val scheduleDao = scheduleDatabase.scheduleDao()

        lifecycleScope.launch {
            scheduleDao.getAllSchedule().collect { allSchedule ->
                for (schedule in allSchedule) {
                    val hour = schedule.split(":")[0].toInt()
                    val minute = schedule.split(":")[1].toInt()
                    addAlarm(applicationContext, hour, minute)
                }
                stopSelf()
            }
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }
}