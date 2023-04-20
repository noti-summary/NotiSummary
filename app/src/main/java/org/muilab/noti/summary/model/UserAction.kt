package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_actions")
data class UserAction(
    var userId: String,
    var time: Long,
    var type: String,
    var actionName: String,
    var metaData: String
) {
    @PrimaryKey(autoGenerate = false)
    var primaryKey: String = "${userId}_${time}"
}
