package org.muilab.noti.summary.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompt_history")
data class Prompt(
    @PrimaryKey(autoGenerate = true)
    var primaryKey: Int = 0,
    var promptText: String,
)
