package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "time_events")
data class Schedule(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var time: String,
    var hour: Int,
    var minute: Int
)
