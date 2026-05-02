package com.tenebris.health_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.WeightEntry

@Database(entities = [FoodEntry::class, WeightEntry::class], version = 2, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun weightDao(): WeightDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_tracker_db"
                )
                .fallbackToDestructiveMigration() // Simplified for development
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
