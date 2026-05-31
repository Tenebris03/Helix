package com.tenebris.health_tracker.data.service

interface FoodProblemDetector {
    suspend fun isProblematic(foodName: String, kcal: Int, hour: Int): Boolean
}
