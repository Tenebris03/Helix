package com.tenebris.health_tracker.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.tenebris.health_tracker.data.model.FoodRecognitionResult
import com.tenebris.health_tracker.data.remote.FoodGatekeeper
import com.tenebris.health_tracker.data.remote.FoodVisionService
import com.tenebris.health_tracker.data.remote.OpenFoodFactsApi

class VisionRepository(
    private val visionService: FoodVisionService,
    private val foodGatekeeper: FoodGatekeeper,
    private val openFoodFactsApi: OpenFoodFactsApi
) {

    suspend fun recognizeFood(bitmap: Bitmap): Result<FoodRecognitionResult> {
        return try {
            if (!foodGatekeeper.isFood(bitmap)) {
                return Result.failure(Exception("Not food - no food detected in image"))
            }

            val geminiResult = visionService.analyzeFoodImage(bitmap)
                ?: return Result.failure(Exception("Failed to recognize food in image"))

            val offProduct = searchOpenFoodFacts(geminiResult.name)

            if (offProduct != null) {
                val cals = offProduct.nutriments?.calories100g?.toInt() ?: geminiResult.calories100g
                val pro = offProduct.nutriments?.proteins100g?.toInt() ?: geminiResult.protein100g
                val fat = offProduct.nutriments?.fat100g?.toInt() ?: geminiResult.fat100g
                val carb = offProduct.nutriments?.carbohydrates100g?.toInt() ?: geminiResult.carbohydrates100g
                val fib = offProduct.nutriments?.fiber100g?.toInt() ?: geminiResult.fiber100g
                val name = offProduct.productName ?: geminiResult.name

                Log.d("VisionRepository", "OpenFoodFacts match: $name (${cals}kcal/${pro}g per 100g)")

                Result.success(FoodRecognitionResult(
                    name = name,
                    calories100g = cals,
                    protein100g = pro,
                    fat100g = fat,
                    carbohydrates100g = carb,
                    fiber100g = fib,
                    estimatedWeightGrams = geminiResult.estimatedWeightGrams
                ))
            } else {
                Log.d("VisionRepository", "No OpenFoodFacts match, using Gemini estimate")

                Result.success(geminiResult)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun searchOpenFoodFacts(query: String): com.tenebris.health_tracker.data.remote.Product? {
        return try {
            val response = openFoodFactsApi.searchProducts(query)
            response.products.firstOrNull { product ->
                product.nutriments?.calories100g != null
            }
        } catch (e: Exception) {
            Log.w("VisionRepository", "OpenFoodFacts search failed", e)
            null
        }
    }
}
