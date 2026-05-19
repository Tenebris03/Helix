package com.tenebris.health_tracker.data.remote

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.tenebris.health_tracker.data.model.FoodEntry
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import java.time.LocalDate

class FoodVisionService(private val generativeModel: GenerativeModel) {

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun analyzeFoodImage(bitmap: Bitmap): FoodEntry? {
        val prompt = content {
            image(bitmap)
            text(
                """
                You are a highly accurate nutritional analysis assistant. 
                Analyze this food image and provide an estimation of its nutritional content.
                
                Identify the primary food item.
                Estimate the calories and protein content PER 100g of this food item.
                Do NOT estimate the portion size, provide values for exactly 100g.
                Use realistic average values for the identified dish.
                
                Return ONLY a valid JSON object in the following format, with no preamble or explanation:
                {
                  "name": "Exact Food Name",
                  "calories": 250,
                  "protein": 15
                }
                
                The calories and protein values can be decimal numbers.
                If you see multiple items, analyze the most prominent one.
                If you cannot identify the food, provide your best guess based on visual cues.
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
                    FoodEntry(
                        name = dto.name,
                        calories = dto.calories.toInt(),
                        protein = dto.protein.toInt(),
                        date = LocalDate.now().toString()
                    )
                }
            } catch (e: ServerException) {
                if (attempt < 2) { // Retry for the first and second attempt
                    val delayMillis = 2000L * (attempt + 1)
                    Log.w("FoodVisionService", "Server error (503), retrying in $delayMillis ms (Attempt ${attempt + 1}/3)")
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
}

@kotlinx.serialization.Serializable
private data class FoodVisionDto(
    val name: String,
    val calories: Double,
    val protein: Double
)
