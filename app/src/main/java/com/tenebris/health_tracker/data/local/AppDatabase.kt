package com.tenebris.health_tracker.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.tenebris.health_tracker.data.model.CachedProduct
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.model.WeightEntry

@Database(entities = [FoodEntry::class, WeightEntry::class, CachedProduct::class, ProfileEntry::class], version = 5, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodDao(): FoodDao
    abstract fun weightDao(): WeightDao
    abstract fun profileDao(): ProfileDao
    abstract fun cachedProductDao(): CachedProductDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // No schema changes between v4 and v5 — version bump to test migration path
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_tracker_db"
                )
                .addMigrations(MIGRATION_4_5)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
