package org.muilab.noti.summary.database.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.muilab.noti.summary.model.CurrentDrawer

@Database(entities = [CurrentDrawer::class], version = 1)
abstract class CurrentDrawerDatabase: RoomDatabase() {

    abstract fun currentDrawerDao(): CurrentDrawerDao

    companion object {
        @Volatile
        private var INSTANCE: CurrentDrawerDatabase? = null

        fun getInstance(context: Context): CurrentDrawerDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
            }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext, CurrentDrawerDatabase::class.java, "current_drawer_database")
            .build()
    }

}