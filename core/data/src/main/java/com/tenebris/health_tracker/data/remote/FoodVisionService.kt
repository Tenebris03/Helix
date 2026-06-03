package com.tenebris.health_tracker.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.tenebris.health_tracker.data.model.FoodRecognitionResult
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.math.roundToInt

class FoodVisionService(
    private val generativeModel: GenerativeModel,
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeFoodImage(bitmap: Bitmap): FoodRecognitionResult? {
        val prompt =
            content {
                image(bitmap)
                text(
                    """
                    You are a highly accurate nutritional analysis assistant specialized in visual food recognition.
                    
                    Your task is to:
                    1. Identify the primary food item(s) in the image with high precision.
                       - Distinguish between home-cooked items and branded products.
                       - If it looks like a simple dish (e.g., "Scrambled Eggs"), do NOT guess a specific brand or packaged product name unless clearly visible.
                    2. Estimate the total portion size of the visible food in grams.
                    3. Provide estimated nutritional values PER 100g for this specific food.

                    Return ONLY a valid JSON object with no preamble:
                    {
                      "name": "Generic Food Name",
                      "estimatedWeightGrams": 0.0,
                      "fallbackCalories100g": 0.0,
                      "fallbackProtein100g": 0.0,
                      "fallbackFat100g": 0.0,
                      "fallbackCarbs100g": 0.0,
                      "fallbackFiber100g": 0.0
                    }
                    """.trimIndent(),
                )
            }

        repeat(3) { attempt ->
            try {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text
                Log.d("FoodVisionService", "Raw response: $responseText")

                val cleanedResponse =
                    responseText
                        ?.replace(Regex("```json\\s?"), "")
                        ?.replace("```", "")
                        ?.trim()

                return cleanedResponse?.let {
                    Log.d("FoodVisionService", "Cleaned response: $it")
                    val dto = json.decodeFromString<FoodVisionDto>(it)
                    FoodRecognitionResult(
                        name = dto.name,
                        calories100g = dto.fallbackCalories100g.roundToInt(),
                        protein100g = dto.fallbackProtein100g.roundToInt(),
                        fat100g = dto.fallbackFat100g.roundToInt(),
                        carbohydrates100g = dto.fallbackCarbs100g.roundToInt(),
                        fiber100g = dto.fallbackFiber100g.roundToInt(),
                        estimatedWeightGrams = dto.estimatedWeightGrams.roundToInt(),
                    )
                }
            } catch (e: ServerException) {
                if (attempt < 2) {
                    val delayMillis = 2000L * (attempt + 1)
                    Log.w("FoodVisionService", "Server error, retrying in $delayMillis ms (Attempt ${attempt + 1}/3)")
                    delay(delayMillis)
                } else {
                    Log.e("FoodVisionService", "Server error after 3 attempts", e)
                }
            } catch (e: Exception) {
                Log.e("FoodVisionService", "Error analyzing image", e)
                return null
            }
        }
        return null
    }

    @Serializable
    private data class FoodVisionDto(
        val name: String,
        val estimatedWeightGrams: Double,
        val fallbackCalories100g: Double,
        val fallbackProtein100g: Double,
        val fallbackFat100g: Double = 0.0,
        val fallbackCarbs100g: Double = 0.0,
        val fallbackFiber100g: Double = 0.0,
    )
}
