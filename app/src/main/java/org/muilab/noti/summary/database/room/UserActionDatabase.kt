package org.muilab.noti.summary.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.muilab.noti.summary.model.UserAction

@Database(entities = [UserAction::class], version = 1)
abstract class UserActionDatabase : RoomDatabase() {

    abstract fun userActionDao(): UserActionDao

    companion object {
        @Volatile
        private var INSTANCE: UserActionDatabase? = null

        fun getInstance(context: Context): UserActionDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                UserActionDatabase::class.java,
                "user_actions"
            ).build()
    }
}
