package com.tenebris.health_tracker.data.service

import com.tenebris.health_tracker.data.local.FoodDao
import com.tenebris.health_tracker.data.local.WeightDao
import java.time.LocalDate

class TrendAnalyzer(
    private val foodDao: FoodDao,
    private val weightDao: WeightDao,
) {
    suspend fun buildTrendSummary(): String {
        val today = LocalDate.now()
        val sevenDaysAgo = today.minusDays(7).toString()
        val thirtyDaysAgo = today.minusDays(30).toString()

        val totalCal7 = foodDao.getTotalCaloriesInRange(sevenDaysAgo, today.toString()) ?: 0
        val totalCal30 = foodDao.getTotalCaloriesInRange(thirtyDaysAgo, today.toString()) ?: 0
        val totalProt7 = foodDao.getTotalProteinInRange(sevenDaysAgo, today.toString()) ?: 0
        val totalProt30 = foodDao.getTotalProteinInRange(thirtyDaysAgo, today.toString()) ?: 0

        val dailyCal7: List<Int> = foodDao.getDailyCaloriesInRange(sevenDaysAgo).map { it.totalCalories }
        val dailyCal30: List<Int> = foodDao.getDailyCaloriesInRange(thirtyDaysAgo).map { it.totalCalories }

        val avgCal7 = if (dailyCal7.isNotEmpty()) totalCal7 / dailyCal7.size else 0
        val avgCal30 = if (dailyCal30.isNotEmpty()) totalCal30 / dailyCal30.size else 0
        val avgProt7 = if (dailyCal7.isNotEmpty()) totalProt7 / dailyCal7.size else 0
        val avgProt30 = if (dailyCal30.isNotEmpty()) totalProt30 / dailyCal30.size else 0

        val avgWeight7 = weightDao.getAverageWeightSince(sevenDaysAgo)
        val avgWeight30 = weightDao.getAverageWeightSince(thirtyDaysAgo)

        val stdDev =
            if (dailyCal7.size >= 2) {
                val mean = dailyCal7.average()
                kotlin.math.sqrt(dailyCal7.sumOf { (it - mean) * (it - mean) } / (dailyCal7.size - 1))
            } else {
                0.0
            }

        val anomaly =
            dailyCal7.maxOrNull()?.let { max ->
                if (stdDev > 0 && max > avgCal7 + 2 * stdDev) {
                    val days = dailyCal7.filter { it > avgCal7 + 1.5 * stdDev }.size
                    "Anomaly: $days day(s) with calorie spikes >1.5σ above 7-day mean"
                } else {
                    null
                }
            } ?: "No significant anomalies"

        return buildString {
            appendLine("7-day avg calories: $avgCal7/day")
            appendLine("30-day avg calories: $avgCal30/day")
            appendLine("7-day avg protein: ${avgProt7}g/day")
            appendLine("30-day avg protein: ${avgProt30}g/day")
            if (avgWeight7 != null && avgWeight30 != null) {
                appendLine("7-day avg weight: ${"%.1f".format(avgWeight7)}kg")
                appendLine("30-day avg weight: ${"%.1f".format(avgWeight30)}kg")
            }
            appendLine("Anomaly detection: $anomaly")
        }
    }
}
