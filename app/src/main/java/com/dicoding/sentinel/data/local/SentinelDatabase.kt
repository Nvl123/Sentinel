package com.dicoding.sentinel.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.dicoding.sentinel.domain.model.RelapseLog
import com.dicoding.sentinel.domain.model.UrgeLog

@Database(entities = [RelapseLog::class, UrgeLog::class], version = 2, exportSchema = false)
abstract class SentinelDatabase : RoomDatabase() {
    abstract fun relapseDao(): RelapseDao

    companion object {
        @Volatile
        private var INSTANCE: SentinelDatabase? = null

        fun getDatabase(context: Context): SentinelDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SentinelDatabase::class.java,
                    "sentinel_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
