package com.tenebris.health_tracker.data.model

data class FoodRecognitionResult(
    val name: String,
    val calories100g: Int,
    val protein100g: Int,
    val fat100g: Int = 0,
    val carbohydrates100g: Int = 0,
    val fiber100g: Int = 0,
    val estimatedWeightGrams: Int
)
