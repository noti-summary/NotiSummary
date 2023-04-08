package org.muilab.noti.summary.viewModel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.*
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.model.Schedule

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

    fun deleteSchedule(schedule: Schedule) {
        scope.launch {
            scheduleDao.deleteByTime(schedule.hour, schedule.minute)
        }
    }
}
