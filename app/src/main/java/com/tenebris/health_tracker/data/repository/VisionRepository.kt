package com.tenebris.health_tracker.data.repository

import android.graphics.Bitmap
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.remote.FoodVisionService

class VisionRepository(private val visionService: FoodVisionService) {

    suspend fun recognizeFood(bitmap: Bitmap): Result<FoodEntry> {
        return try {
            val entry = visionService.analyzeFoodImage(bitmap)
            if (entry != null) {
                Result.success(entry)
            } else {
                Result.failure(Exception("Failed to recognize food in image"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
