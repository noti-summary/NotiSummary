package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_actions")
data class UserAction(
    val userId: String,
    val time: Long,
    val type: String,
    val action: String,
    val metaData: String
) {
    @PrimaryKey(autoGenerate = false)
    val primaryKey: String = "${userId}_${time}"
}
