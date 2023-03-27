package org.muilab.noti.summary.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.muilab.noti.summary.model.APIKeyEntity
import org.muilab.noti.summary.model.Prompt

@Database(entities = [APIKeyEntity::class], version = 1)
abstract class APIKeyDatabase : RoomDatabase() {

    abstract fun apiKeyDao(): APIKeyDao

    companion object {
        @Volatile
        private var INSTANCE: APIKeyDatabase? = null

        fun getInstance(context: Context): APIKeyDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) =
            Room.databaseBuilder(
                context.applicationContext,
                APIKeyDatabase::class.java,
                "api_key_pool"
            )
                .build()
    }
}
