package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.Schedule

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM time_events ORDER BY hour ASC, minute ASC")
    fun getSortedSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM time_events WHERE minute = :minute AND hour = :hour")
    fun getScheduleByTime(hour: Int, minute: Int): Schedule?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertSchedule(schedule: Schedule)

    @Transaction
    fun insertScheduleIfNotExists(schedule: Schedule) {
        val existingTime = getScheduleByTime(schedule.hour, schedule.minute)
        if (existingTime == null) {
            insertSchedule(schedule)
        }
    }

    @Query("DELETE FROM time_events WHERE hour = :hour AND minute = :minute")
    fun deleteByTime(hour: Int, minute: Int)
}