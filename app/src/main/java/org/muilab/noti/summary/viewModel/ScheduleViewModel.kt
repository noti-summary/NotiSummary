package org.muilab.noti.summary.viewModel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.model.Schedule
import org.muilab.noti.summary.util.LogAlarm

class ScheduleViewModel(application: Application, scheduleDatabase: ScheduleDatabase) :
    AndroidViewModel(application) {

    private val scheduleDao = scheduleDatabase.scheduleDao()
    val allSchedule: LiveData<List<Schedule>> = scheduleDao.getSortedSchedules().asLiveData()

    private val scope = viewModelScope + Dispatchers.IO

    suspend fun addNewSchedule(hour: Int, minute: Int): Schedule? {
        return withContext(Dispatchers.IO) {
            scheduleDao.insertScheduleIfNotExists(Schedule(0, hour, minute))
            scheduleDao.getScheduleByTime(hour, minute)
        }
    }

    fun updateWeekSchedule(schedule: Schedule, newWeekState: Int) {
        scope.launch {
            scheduleDao.updateWeekSchedule(
                schedule.hour, schedule.minute, schedule.week, newWeekState
            )
        }
    }

    fun deleteSchedule(schedule: Schedule) {
        scope.launch {
            scheduleDao.deleteByTime(schedule.hour, schedule.minute)
        }
    }
}
