package com.tenebris.health_tracker.data.service

class HeuristicFoodProblemDetector : FoodProblemDetector {

    override suspend fun isProblematic(foodName: String, kcal: Int, hour: Int): Boolean {
        val problematic = kcal > 300 && hour >= 21
        val hasRedFlag = RED_FLAGS.any { foodName.contains(it, ignoreCase = true) }
        val isLightSnack = kcal < 150
        return (hasRedFlag && problematic) || (hasRedFlag && kcal > 500) && !isLightSnack
    }

    companion object {
        private val RED_FLAGS = listOf(
            "burger", "fries", "soda", "candy", "chips", "pizza", "donut",
            "milkshake", "ice cream", "cookie", "cake", "pastry", "brownie",
            "chocolate bar", "nugget", "fried", "bacon", "sausage", "noodle",
            "instant", "microwave", "frozen meal", "fast food"
        )
    }
}
