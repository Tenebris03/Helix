package com.tenebris.health_tracker.data.repository

import android.graphics.Bitmap
import android.util.Log
import com.tenebris.health_tracker.data.model.FoodRecognitionResult
import com.tenebris.health_tracker.data.remote.FoodGatekeeper
import com.tenebris.health_tracker.data.remote.FoodVisionService

class VisionRepository(
    private val visionService: FoodVisionService,
    private val foodGatekeeper: FoodGatekeeper,
) {
    suspend fun recognizeFood(bitmap: Bitmap): Result<FoodRecognitionResult> {
        return try {
            if (!foodGatekeeper.isFood(bitmap)) {
                return Result.failure(Exception("Not food - no food detected in image"))
            }

            val geminiResult =
                visionService.analyzeFoodImage(bitmap)
                    ?: return Result.failure(Exception("Failed to recognize food in image"))

            Log.d("VisionRepository", "Using Gemini estimate: ${geminiResult.name}")
            Result.success(geminiResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
