package org.muilab.noti.summary.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.muilab.noti.summary.model.CurrentDrawer
import org.muilab.noti.summary.model.Prompt

@Database(entities = [Prompt::class], version = 1)
abstract class PromptDatabase : RoomDatabase() {

    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: PromptDatabase? = null

        fun getInstance(context: Context): PromptDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                PromptDatabase::class.java,
                "prompt_history"
            )
                .build()
    }
}
