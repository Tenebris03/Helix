package com.tenebris.health_tracker.data.service

import com.tenebris.health_tracker.data.local.FoodDao
import com.tenebris.health_tracker.data.local.ProfileDao
import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import java.time.LocalDate
import kotlin.random.Random

class TestDataSeeder(
    private val foodDao: FoodDao,
    private val weightDao: WeightDao,
    private val profileDao: ProfileDao,
    private val userPreferences: UserPreferences,
) {
    suspend fun seed() {
        val today = LocalDate.now()

        insertFoodEntries(today)
        insertWeightEntries(today)
        insertProfileEntry(today)
        savePreferences()
        userPreferences.clearCoachInterventionTimestamp()
    }

    private suspend fun insertFoodEntries(today: LocalDate) {
        for (daysAgo in 60 downTo 0) {
            val date = today.minusDays(daysAgo.toLong())
            val daySeed = daysAgo * 31 + today.dayOfYear
            val rng = Random(daySeed.toLong())

            val count = 3 + rng.nextInt(3)
            val selected = MEALS.shuffled(rng).take(count)

            for (meal in selected) {
                foodDao.insertEntry(
                    FoodEntry(
                        name = meal.name,
                        calories = meal.calories,
                        protein = meal.protein,
                        fat = meal.fat,
                        carbohydrates = meal.carbs,
                        fiber = meal.fiber,
                        date = date.toString(),
                    ),
                )
            }
        }
    }

    private suspend fun insertWeightEntries(today: LocalDate) {
        val startWeight = 80.0f
        val endWeight = 77.0f

        for (i in 0..12) {
            val daysAgo = i * 5
            if (daysAgo > 60) break
            val weight = startWeight - (startWeight - endWeight) * daysAgo / 60f
            weightDao.insertWeight(
                WeightEntry(
                    weight = weight,
                    date = today.minusDays(daysAgo.toLong()).toString(),
                ),
            )
        }
    }

    private suspend fun insertProfileEntry(today: LocalDate) {
        val startDate = today.minusDays(60)
        profileDao.insertProfile(
            ProfileEntry(
                date = startDate.toString(),
                activityLevel = 1.4f,
                height = 175,
                age = 28,
                gender = "Male",
                goal = "Lose",
                offset = 300,
                proteinTarget = 160,
            ),
        )
    }

    private suspend fun savePreferences() {
        val bmr = (10 * 77f + 6.25f * 175 - 5 * 28 + 5).toInt()
        val tdee = (bmr * 1.4f).toInt()
        userPreferences.saveOnboardingData(
            tdee = tdee,
            goal = "Lose",
            offset = 300,
            proteinTarget = 160,
            activityLevel = 1.4f,
            gender = "Male",
            age = 28,
            height = 175,
        )
    }

    private data class FoodData(
        val name: String,
        val calories: Int,
        val protein: Int,
        val fat: Int,
        val carbs: Int,
        val fiber: Int,
    )

    private companion object {
        private val MEALS =
            listOf(
                FoodData("Oatmeal with berries", 350, 12, 5, 60, 8),
                FoodData("Greek yogurt & granola", 280, 15, 8, 35, 4),
                FoodData("Scrambled eggs & toast", 320, 22, 18, 18, 2),
                FoodData("Protein shake", 200, 30, 3, 10, 1),
                FoodData("Banana", 105, 1, 0, 27, 3),
                FoodData("Apple with peanut butter", 250, 8, 14, 28, 5),
                FoodData("Mixed nuts", 180, 6, 16, 6, 3),
                FoodData("Chicken salad", 420, 35, 15, 20, 4),
                FoodData("Turkey sandwich", 380, 25, 12, 40, 3),
                FoodData("Vegetable soup", 150, 6, 4, 22, 5),
                FoodData("Tuna wrap", 410, 30, 14, 38, 2),
                FoodData("Grilled salmon & rice", 550, 40, 18, 45, 1),
                FoodData("Beef stir-fry", 450, 32, 16, 35, 4),
                FoodData("Pasta with pesto", 480, 14, 22, 55, 3),
                FoodData("Green smoothie", 180, 5, 2, 35, 6),
                FoodData("Burger and fries", 750, 30, 38, 65, 2),
                FoodData("Late night pizza", 600, 24, 22, 65, 2),
                FoodData("Donut", 250, 3, 14, 30, 0),
                FoodData("Cookie", 180, 2, 8, 25, 1),
                FoodData("Coffee with milk", 50, 2, 2, 5, 0),
            )
    }
}
