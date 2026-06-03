package com.tenebris.health_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.tenebris.health_tracker.data.model.CachedProduct
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.model.WeightEntry

@Database(entities = [FoodEntry::class, WeightEntry::class, CachedProduct::class, ProfileEntry::class], version = 12, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao

    abstract fun weightDao(): WeightDao

    abstract fun profileDao(): ProfileDao

    abstract fun cachedProductDao(): CachedProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                val instance =
                    Room
                        .databaseBuilder(
                            context.applicationContext,
                            AppDatabase::class.java,
                            "health_tracker_v3_release",
                        ).fallbackToDestructiveMigration()
                        .build()
                INSTANCE = instance
                instance
            }
    }
}
