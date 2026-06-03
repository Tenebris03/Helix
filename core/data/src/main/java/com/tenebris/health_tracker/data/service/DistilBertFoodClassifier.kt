package com.tenebris.health_tracker.data.service

class DistilBertFoodClassifier(
    private val detector: FoodProblemDetector = HeuristicFoodProblemDetector(),
) : FoodProblemDetector {
    override suspend fun isProblematic(
        foodName: String,
        kcal: Int,
        hour: Int,
    ): Boolean = detector.isProblematic(foodName, kcal, hour)
}
