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
    val allScheduleStr: LiveData<List<String>> = Transformations.map(allSchedule) { schedules ->
        schedules.map { schedule ->
            schedule.time
        }
    }

    private val scope = viewModelScope + Dispatchers.IO

    suspend fun addNewSchedule(newSchedule: String, hour: Int, minute: Int): Schedule? {
        val updateSchedule = newSchedule.trim()
        return withContext(Dispatchers.IO) {
            scheduleDao.insertScheduleIfNotExists(Schedule(0, updateSchedule, hour, minute))
            scheduleDao.getScheduleByTime(updateSchedule)
        }
    }

    fun deleteSchedule(time: String) {
        scope.launch {
            scheduleDao.deleteByTime(time)
        }
    }
}
