package com.tenebris.health_tracker.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.tenebris.health_tracker.data.model.DailyCalorieSum
import com.tenebris.health_tracker.data.model.FoodEntry
import kotlinx.coroutines.flow.Flow

@Dao
interface FoodDao {
    @Query("SELECT * FROM food_entries WHERE date = :date")
    fun getEntriesByDate(date: String): Flow<List<FoodEntry>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: FoodEntry)

    @Delete
    suspend fun deleteEntry(entry: FoodEntry)

    @Query("SELECT SUM(calories) FROM food_entries WHERE date = :date")
    fun getTotalCaloriesByDate(date: String): Flow<Int?>

    @Query("SELECT SUM(protein) FROM food_entries WHERE date = :date")
    fun getTotalProteinByDate(date: String): Flow<Int?>

    @Query("SELECT * FROM food_entries GROUP BY name, calories, protein ORDER BY id DESC LIMIT 50")
    fun getUniqueRecentEntries(): Flow<List<FoodEntry>>

    @Query("SELECT SUM(calories) FROM food_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalCaloriesInRange(startDate: String, endDate: String): Int?

    @Query("SELECT date, SUM(calories) as totalCalories FROM food_entries WHERE date >= :startDate GROUP BY date ORDER BY date")
    suspend fun getDailyCaloriesInRange(startDate: String): List<DailyCalorieSum>

    @Query("SELECT COUNT(DISTINCT date) FROM food_entries")
    suspend fun getDistinctFoodDays(): Int

    @Query("SELECT SUM(protein) FROM food_entries WHERE date >= :startDate AND date <= :endDate")
    suspend fun getTotalProteinInRange(startDate: String, endDate: String): Int?
}
