package com.tenebris.health_tracker.data.repository

import com.tenebris.health_tracker.data.local.CachedProductDao
import com.tenebris.health_tracker.data.local.FoodDao
import com.tenebris.health_tracker.data.model.CachedProduct
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.remote.OpenFoodFactsApi
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

class FoodRepository(
    private val foodDao: FoodDao,
    private val cachedProductDao: CachedProductDao,
    private val api: OpenFoodFactsApi
) {
    fun getEntriesByDate(date: LocalDate): Flow<List<FoodEntry>> {
        return foodDao.getEntriesByDate(date.toString())
    }

    suspend fun addFoodEntry(entry: FoodEntry) {
        foodDao.insertEntry(entry)
    }

    suspend fun deleteFoodEntry(entry: FoodEntry) {
        foodDao.deleteEntry(entry)
    }

    fun getUniqueRecentEntries(): Flow<List<FoodEntry>> {
        return foodDao.getUniqueRecentEntries()
    }

    suspend fun getProductByBarcode(barcode: String): Result<CachedProduct> {
        // 1. Check local cache
        val cached = cachedProductDao.getProductByBarcode(barcode)
        if (cached != null) {
            // Update last accessed time
            cachedProductDao.insertProduct(cached.copy(lastAccessed = System.currentTimeMillis()))
            return Result.success(cached)
        }

        // 2. Fetch from API
        return try {
            val response = api.getProduct(barcode)
            if (response.status == 1 && response.product != null) {
                val product = response.product
                val calories = product.nutriments?.calories100g?.toInt() ?: 0
                val protein = product.nutriments?.proteins100g?.toInt() ?: 0
                val fat = product.nutriments?.fat100g?.toInt() ?: 0
                val carbs = product.nutriments?.carbohydrates100g?.toInt() ?: 0
                val fiber = product.nutriments?.fiber100g?.toInt() ?: 0
                val name = product.productName ?: "Unknown Product"
                
                val newCached = CachedProduct(
                    barcode = barcode,
                    name = name,
                    calories100g = calories,
                    protein100g = protein,
                    fat100g = fat,
                    carbohydrates100g = carbs,
                    fiber100g = fiber
                )
                
                cachedProductDao.insertProduct(newCached)
                cachedProductDao.trimCache()
                Result.success(newCached)
            } else {
                Result.failure(Exception("Product not found: ${response.statusVerbose}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
