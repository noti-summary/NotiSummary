package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.Prompt


@Dao
interface PromptDao {
    @Query("SELECT promptText FROM prompt_history")
    fun getAllPrompt(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertPrompt(prompt: Prompt)

    @Query("DELETE FROM prompt_history")
    fun deleteAllPrompt()
}