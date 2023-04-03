package org.muilab.noti.summary.viewModel

import android.app.Application
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus
import org.muilab.noti.summary.database.room.ScheduleDatabase
import org.muilab.noti.summary.model.Schedule

class ScheduleViewModel(application: Application, scheduleDatabase: ScheduleDatabase) :
    AndroidViewModel(application) {

    private val scheduleDao = scheduleDatabase.scheduleDao()
    val allSchedule: LiveData<List<String>> = scheduleDao.getAllSchedule().asLiveData()
    private val scope = viewModelScope + Dispatchers.IO

    fun addNewSchedule(newSchedule: String) {
        val updateSchedule = newSchedule.trim()
        scope.launch {
            scheduleDao.insertScheduleIfNotExists(Schedule(0, updateSchedule))
        }
    }

    fun updateSchedule(oldTime: String, newTime: String) {
        scope.launch{
            scheduleDao.updateTime(oldTime, newTime)
        }
    }

    fun deleteSchedule(time: String) {
        scope.launch {
            scheduleDao.deleteByTime(time)
        }
    }
}
