package org.muilab.noti.summary.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.muilab.noti.summary.model.Interaction

@Database(entities = [Interaction::class], version = 1)
abstract class InteractionDatabase : RoomDatabase() {

    abstract fun interactionDao(): InteractionDao

    companion object {
        @Volatile
        private var INSTANCE: InteractionDatabase? = null

        fun getInstance(context: Context): InteractionDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                InteractionDatabase::class.java,
                "time_events"
            ).build()
    }
}
