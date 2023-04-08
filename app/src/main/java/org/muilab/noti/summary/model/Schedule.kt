package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_events")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var hour: Int,
    var minute: Int
) {
    fun getTime(): String {
        return String.format("%02d:%02d", hour, minute)
    }
}
