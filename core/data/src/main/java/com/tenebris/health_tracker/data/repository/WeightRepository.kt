package com.tenebris.health_tracker.data.repository

import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.model.WeightEntry
import kotlinx.coroutines.flow.Flow

class WeightRepository(
    private val weightDao: WeightDao,
) {
    fun getAllWeightEntries(): Flow<List<WeightEntry>> = weightDao.getAllWeightEntries()

    fun getLatestWeightEntry(): Flow<WeightEntry?> = weightDao.getLatestWeightEntry()

    fun getWeightAtDate(date: String): Flow<WeightEntry?> = weightDao.getWeightAtDate(date)

    suspend fun insertWeight(entry: WeightEntry) = weightDao.insertWeight(entry)

    suspend fun deleteWeight(entry: WeightEntry) = weightDao.deleteWeight(entry)

    suspend fun updateWeight(entry: WeightEntry) = weightDao.updateWeight(entry)

    suspend fun getWeightByDate(date: String): WeightEntry? = weightDao.getWeightByDate(date)

    suspend fun getAverageWeightSince(startDate: String): Float? = weightDao.getAverageWeightSince(startDate)
}
