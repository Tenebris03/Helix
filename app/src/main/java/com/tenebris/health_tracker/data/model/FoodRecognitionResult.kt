package com.tenebris.health_tracker.data.model

data class FoodRecognitionResult(
    val name: String,
    val calories100g: Int,
    val protein100g: Int,
    val estimatedWeightGrams: Int
)
