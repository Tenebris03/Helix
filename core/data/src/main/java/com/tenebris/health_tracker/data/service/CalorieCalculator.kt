package com.tenebris.health_tracker.data.service

data class CalorieTargets(
    val bmr: Int,
    val tdee: Int,
    val adjustedTarget: Int,
    val proteinTarget: Int,
)

object CalorieCalculator {
    fun compute(
        weightKg: Float,
        heightCm: Int,
        age: Int,
        gender: String,
        goal: String,
        offset: Int,
        activityLevel: Float,
        proteinTarget: Int,
    ): CalorieTargets {
        val bmr =
            if (gender == "Male") {
                10 * weightKg + 6.25 * heightCm - 5 * age + 5
            } else {
                10 * weightKg + 6.25 * heightCm - 5 * age - 161
            }

        val tdee = bmr * activityLevel

        val adjustedTarget =
            when (goal) {
                "Lose" -> tdee - offset
                "Gain" -> tdee + offset
                else -> tdee
            }

        return CalorieTargets(
            bmr = bmr.toInt(),
            tdee = tdee.toInt(),
            adjustedTarget = adjustedTarget.toInt(),
            proteinTarget = proteinTarget,
        )
    }
}
