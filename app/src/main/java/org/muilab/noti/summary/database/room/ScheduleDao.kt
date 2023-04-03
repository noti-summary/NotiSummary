package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.Schedule

@Dao
interface ScheduleDao {
    @Query("SELECT time FROM time_events")
    fun getAllSchedule(): Flow<List<String>>

    @Query("SELECT * FROM time_events WHERE time = :time")
    fun getScheduleByTime(time: String): Schedule?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSchedule(schedule: Schedule)

    @Transaction
    fun insertScheduleIfNotExists(schedule: Schedule) {
        val existingTime = getScheduleByTime(time = schedule.time)
        if (existingTime == null) {
            insertSchedule(schedule)
        }
    }

    @Query("UPDATE time_events SET time = :newTime WHERE time = :oldTime")
    fun updateTime(oldTime: String, newTime: String)

    @Query("DELETE FROM time_events WHERE time = :time")
    fun deleteByTime(time: String)

    @Query("DELETE FROM time_events")
    fun deleteAllSchedule()
}