package com.tenebris.health_tracker.data.model

import androidx.room.ColumnInfo

data class DailyCalorieSum(
    val date: String,
    val totalCalories: Int
)
