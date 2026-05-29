package com.tenebris.health_tracker.data.service

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.util.concurrent.TimeUnit

class WeatherService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(5, TimeUnit.SECONDS)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    suspend fun getCurrentWeatherDescription(
        latitude: Double = DEFAULT_LAT,
        longitude: Double = DEFAULT_LON
    ): String {
        return try {
            val request = Request.Builder()
                .url("$BASE_URL?latitude=$latitude&longitude=$longitude&current_weather=true")
                .build()

            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return ""

            val body = response.body?.string() ?: return ""
            val dto = json.decodeFromString<OpenMeteoResponse>(body)
            val code = dto.currentWeather.weatherCode
            val temp = dto.currentWeather.temperature

            val label = weatherCodes[code] ?: "Unknown"
            "${label}, ${temp}°C"
        } catch (_: Exception) {
            ""
        }
    }

    companion object {
        private const val BASE_URL = "https://api.open-meteo.com/v1/forecast"
        private const val DEFAULT_LAT = 52.52
        private const val DEFAULT_LON = 13.405

        private val weatherCodes = mapOf(
            0 to "Clear", 1 to "Mainly clear", 2 to "Partly cloudy", 3 to "Overcast",
            45 to "Foggy", 48 to "Depositing rime fog",
            51 to "Light drizzle", 53 to "Moderate drizzle", 55 to "Dense drizzle",
            61 to "Slight rain", 63 to "Moderate rain", 65 to "Heavy rain",
            71 to "Slight snow", 73 to "Moderate snow", 75 to "Heavy snow",
            80 to "Slight rain showers", 81 to "Moderate rain showers", 82 to "Violent rain showers",
            95 to "Thunderstorm", 96 to "Thunderstorm with slight hail", 99 to "Thunderstorm with heavy hail"
        )
    }
}

@Serializable
private data class OpenMeteoResponse(
    val currentWeather: CurrentWeather
)

@Serializable
private data class CurrentWeather(
    val temperature: Double,
    val weatherCode: Int
)
