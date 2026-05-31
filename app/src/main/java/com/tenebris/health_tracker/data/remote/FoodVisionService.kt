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

class FoodVisionService(private val generativeModel: GenerativeModel) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeFoodImage(bitmap: Bitmap): FoodRecognitionResult? {
        val prompt = content {
            image(bitmap)
            text(
                """
                You are a highly accurate nutritional analysis assistant.
                Analyze this food image.

                Identify the primary food item and estimate:
                1. The food name (short, generic)
                2. The estimated portion size on the plate in grams
                3. Nutritional values PER 100g as a fallback estimate (calories, protein, fat, carbohydrates, fiber)

                Return ONLY a valid JSON object with no preamble:
                {
                  "name": "Grilled Chicken Salad",
                  "estimatedWeightGrams": 300,
                  "fallbackCalories100g": 150,
                  "fallbackProtein100g": 25,
                  "fallbackFat100g": 5,
                  "fallbackCarbs100g": 10,
                  "fallbackFiber100g": 3
                }
                """.trimIndent(),
            )
        }

        repeat(3) { attempt ->
            try {
                val response = generativeModel.generateContent(prompt)
                val responseText = response.text
                Log.d("FoodVisionService", "Raw response: $responseText")

                val cleanedResponse = responseText
                    ?.replace(Regex("```json\\s?"), "")
                    ?.replace("```", "")
                    ?.trim()

                return cleanedResponse?.let {
                    Log.d("FoodVisionService", "Cleaned response: $it")
                    val dto = json.decodeFromString<FoodVisionDto>(it)
                    FoodRecognitionResult(
                        name = dto.name,
                        calories100g = dto.fallbackCalories100g,
                        protein100g = dto.fallbackProtein100g,
                        fat100g = dto.fallbackFat100g,
                        carbohydrates100g = dto.fallbackCarbs100g,
                        fiber100g = dto.fallbackFiber100g,
                        estimatedWeightGrams = dto.estimatedWeightGrams
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
        val estimatedWeightGrams: Int,
        val fallbackCalories100g: Int,
        val fallbackProtein100g: Int,
        val fallbackFat100g: Int = 0,
        val fallbackCarbs100g: Int = 0,
        val fallbackFiber100g: Int = 0
    )
}
