package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.Prompt


@Dao
interface PromptDao {
    @Query("SELECT promptText FROM prompt_history")
    fun getAllPrompt(): Flow<List<String>>

    @Query("SELECT * FROM prompt_history WHERE promptText = :promptText")
    fun getPromptByPromptText(promptText: String): Prompt?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertPrompt(prompt: Prompt)

    @Transaction
    fun insertPromptIfNotExists(prompt: Prompt) {
        val existingPrompt = getPromptByPromptText(promptText = prompt.promptText)
        if (existingPrompt == null) {
            insertPrompt(prompt)
        }
    }

    @Query("UPDATE prompt_history SET promptText = :newPrompt WHERE promptText = :oldPrompt")
    fun updatePromptText(oldPrompt: String, newPrompt: String)

    @Query("DELETE FROM prompt_history WHERE promptText = :prompt")
    fun deleteByPromptText(prompt: String)

    @Query("DELETE FROM prompt_history")
    fun deleteAllPrompt()
}