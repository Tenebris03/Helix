package com.tenebris.health_tracker.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tenebris.health_tracker.data.local.AppDatabase
import com.tenebris.health_tracker.data.pref.EncryptedStorageManager
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.repository.CoachRepository
import com.tenebris.health_tracker.data.service.CalendarContextResolver
import com.tenebris.health_tracker.data.service.CoachNotificationDispatcher
import com.tenebris.health_tracker.data.service.LocationProvider
import com.tenebris.health_tracker.data.service.TrendAnalyzer
import com.tenebris.health_tracker.data.service.WeatherService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

class InvisibleCoachWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentMealLog = inputData.getString(KEY_MEAL_LOG) ?: return@withContext Result.failure()

            val appDb = AppDatabase.getDatabase(applicationContext)
            val encryptedStorage = EncryptedStorageManager(applicationContext)
            val userPrefs = UserPreferences(applicationContext)

            // Cold-start gate: mindestens 14 Tage Daten
            val distinctDays = appDb.foodDao().getDistinctFoodDays()
            if (distinctDays < 14) return@withContext Result.success()

            // Throttling gate: max 1x pro 12h
            val lastIntervention = userPrefs.lastCoachIntervention.first()
            if (System.currentTimeMillis() - lastIntervention < COOLDOWN_MS) {
                return@withContext Result.success()
            }

            // API-Key-Check
            val apiKey = encryptedStorage.getApiKey() ?: return@withContext Result.success()

            // Services aufbauen
            val trendAnalyzer = TrendAnalyzer(appDb.foodDao(), appDb.weightDao())
            val calendarResolver = CalendarContextResolver(applicationContext)
            val weatherService = WeatherService()
            val locationProvider = LocationProvider(applicationContext)
            val coachRepo = CoachRepository(trendAnalyzer, calendarResolver, weatherService)

            // Letzte Position abrufen (optional)
            val deviceLocation = locationProvider.getLastKnownLocation()

            // Coach aufrufen
            val result = coachRepo.getCoachResponse(currentMealLog, apiKey, deviceLocation)

            when (result) {
                is com.tenebris.health_tracker.data.model.CoachResult.AuthError -> {
                    userPrefs.setCoachApiKeyValid(false)
                }
                is com.tenebris.health_tracker.data.model.CoachResult.RateLimited -> {
                    // Beim nächsten Worker-Durchlauf neu versuchen (retry)
                    return@withContext Result.retry()
                }
                is com.tenebris.health_tracker.data.model.CoachResult.Success -> {
                    val response = result.response
                    if (response.criticalAlert) {
                        userPrefs.saveCoachResponse(response.reasonHeadline, response.reasonBody)
                        userPrefs.saveCoachInterventionTimestamp()
                        CoachNotificationDispatcher.triggerTactileAlert(
                            applicationContext,
                            response.reasonHeadline,
                            response.reasonBody
                        )
                    }
                }
                is com.tenebris.health_tracker.data.model.CoachResult.OtherError -> {
                    // Silent fail – nächstes Food-Log triggert erneut
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_MEAL_LOG = "MEAL_LOG"
        private const val COOLDOWN_MS = 12 * 60 * 60 * 1000L // 12 Stunden
    }
}
