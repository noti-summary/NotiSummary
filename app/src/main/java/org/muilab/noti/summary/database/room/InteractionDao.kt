package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.Interaction
import org.muilab.noti.summary.model.Schedule

@Dao
interface InteractionDao {
    @Query("SELECT * FROM app_interactions ORDER BY time ASC")
    fun getAllInteractions(): Flow<List<Interaction>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertInteraction(interaction: Interaction)

    @Query("DELETE FROM app_interactions")
    fun deleteAll()
}