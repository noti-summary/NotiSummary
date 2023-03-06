package edu.mui.noti.noti.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_drawer_table")
data class CurrentDrawer(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var notificationId: String,
    var packageName: String,
    var groupKey: String,
    var sortKey: String,
    var appName: String,
    var title: String,
    var content: String,
    var time: String,
)
