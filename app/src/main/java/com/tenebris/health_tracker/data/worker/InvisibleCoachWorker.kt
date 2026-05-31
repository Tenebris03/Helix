package com.tenebris.health_tracker.data.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.tenebris.health_tracker.data.local.AppDatabase
import com.tenebris.health_tracker.data.model.CoachResult
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
import org.koin.java.KoinJavaComponent.get

class InvisibleCoachWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val currentMealLog = inputData.getString(KEY_MEAL_LOG) ?: return@withContext Result.failure()

            val appDb: AppDatabase = get(AppDatabase::class.java)
            val userPrefs: UserPreferences = get(UserPreferences::class.java)
            val encryptedStorage: EncryptedStorageManager = get(EncryptedStorageManager::class.java)
            val coachRepo: CoachRepository = get(CoachRepository::class.java)
            val locationProvider: LocationProvider = get(LocationProvider::class.java)

            val distinctDays = appDb.foodDao().getDistinctFoodDays()
            if (distinctDays < 14) return@withContext Result.success()

            val lastIntervention = userPrefs.lastCoachIntervention.first()
            if (System.currentTimeMillis() - lastIntervention < COOLDOWN_MS) {
                return@withContext Result.success()
            }

            val apiKey = encryptedStorage.getApiKey() ?: return@withContext Result.success()

            val deviceLocation = locationProvider.getLastKnownLocation()

            val result = coachRepo.getCoachResponse(currentMealLog, apiKey, deviceLocation)

            when (result) {
                is CoachResult.AuthError -> {
                    userPrefs.setCoachApiKeyValid(false)
                }
                is CoachResult.RateLimited -> {
                    return@withContext Result.retry()
                }
                is CoachResult.Success -> {
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
                is CoachResult.OtherError -> {
                    // Silent fail
                }
            }

            Result.success()
        } catch (_: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_MEAL_LOG = "MEAL_LOG"
        private const val COOLDOWN_MS = 12 * 60 * 60 * 1000L
    }
}
