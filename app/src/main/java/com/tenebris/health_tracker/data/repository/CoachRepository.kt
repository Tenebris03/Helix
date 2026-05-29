package com.tenebris.health_tracker.data.repository

import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.ServerException
import com.google.ai.client.generativeai.type.content
import com.tenebris.health_tracker.data.model.CoachResult
import com.tenebris.health_tracker.data.service.CalendarContextResolver
import com.tenebris.health_tracker.data.service.CoachPromptBuilder
import com.tenebris.health_tracker.data.service.DeviceLocation
import com.tenebris.health_tracker.data.service.TrendAnalyzer
import com.tenebris.health_tracker.data.service.WeatherService
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json

class CoachRepository(
    private val trendAnalyzer: TrendAnalyzer,
    private val calendarResolver: CalendarContextResolver,
    private val weatherService: WeatherService
) {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCoachResponse(
        currentLog: String,
        apiKey: String,
        location: DeviceLocation? = null
    ): CoachResult {
        val generativeModel = GenerativeModel(
            modelName = "gemini-3-flash-preview",
            apiKey = apiKey,
        )

        val calendarCtx = calendarResolver.getRecentCalendarEvents()
        val weatherCtx = if (location != null) {
            weatherService.getCurrentWeatherDescription(location.latitude, location.longitude)
        } else {
            weatherService.getCurrentWeatherDescription()
        }
        val trends = trendAnalyzer.buildTrendSummary()

        val prompt = CoachPromptBuilder.build(
            currentLog = currentLog,
            calendarContext = calendarCtx,
            weatherContext = weatherCtx,
            historicalTrends = trends
        )

        val geminiPrompt = content { text(prompt) }

        for (attempt in 0 until 2) {
            try {
                val response = generativeModel.generateContent(geminiPrompt)
                val responseText = response.text ?: continue

                val cleaned = responseText
                    .replace(Regex("```json\\s?"), "")
                    .replace("```", "")
                    .trim()

                val coachResponse = json.decodeFromString<com.tenebris.health_tracker.data.model.CoachResponse>(cleaned)
                return CoachResult.Success(coachResponse)
            } catch (e: ServerException) {
                val msg = e.message?.lowercase() ?: ""
                when {
                    msg.contains("api key") || msg.contains("unauthorized") || msg.contains("permission") ->
                        return CoachResult.AuthError
                    msg.contains("rate") || msg.contains("quota") || msg.contains("too many") ->
                        return CoachResult.RateLimited
                }
                if (attempt < 1) {
                    delay(1000)
                } else {
                    return CoachResult.OtherError
                }
            } catch (_: Exception) {
                return CoachResult.OtherError
            }
        }
        return CoachResult.OtherError
    }
}
