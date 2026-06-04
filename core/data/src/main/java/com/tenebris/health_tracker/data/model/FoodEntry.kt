package com.tenebris.health_tracker.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MealType {
    BREAKFAST,
    LUNCH,
    DINNER,
    SNACK,
}

@Entity(tableName = "food_entries")
data class FoodEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val calories: Int,
    val protein: Int,
    val fat: Int = 0,
    val carbohydrates: Int = 0,
    val fiber: Int = 0,
    val date: String, // ISO-8601 format yyyy-MM-dd
    val mealType: MealType = MealType.SNACK,
)
