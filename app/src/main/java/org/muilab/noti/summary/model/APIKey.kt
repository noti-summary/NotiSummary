package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_key_pool")
data class APIKeyEntity(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var APIKey: String,
)
