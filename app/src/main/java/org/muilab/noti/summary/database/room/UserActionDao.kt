package org.muilab.noti.summary.database.room

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import org.muilab.noti.summary.model.UserAction

@Dao
interface UserActionDao {
    @Query("SELECT * FROM user_actions ORDER BY time ASC")
    fun getAllActions(): List<UserAction>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAction(userAction: UserAction)

    @Query("DELETE FROM user_actions")
    fun deleteAll()

    @Query("SELECT COUNT(*) FROM user_actions")
    fun getActionsCount(): Int
}