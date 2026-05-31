package com.tenebris.health_tracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.local.ProfileDao
import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.EncryptedStorageManager
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.pref.UserPreferences.PrefsSnapshot
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.InputStream
import java.io.OutputStream

data class SettingsState(
    val bmr: Int = 2000,
    val goal: String = "Maintain",
    val offset: Int = 0,
    val proteinTarget: Int = 150,
    val activityLevel: Float = 1.2f,
    val gender: String = "Male",
    val age: Int = 25,
    val height: Int = 170,
    val latestWeight: Float = 70f,
    val apiKey: String = ""
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val weightDao: WeightDao,
    private val profileDao: ProfileDao,
    private val encryptedStorage: EncryptedStorageManager
) : ViewModel() {

    private val _apiKey = MutableStateFlow(encryptedStorage.getApiKey() ?: "")
    val apiKey: StateFlow<String> = _apiKey

    val state: StateFlow<SettingsState> = combine(
        userPreferences.snapshot,
        weightDao.getLatestWeightEntry()
    ) { prefs, latestWeightEntry ->

        val weightVal = latestWeightEntry?.weight ?: 70f

        val bmr = if (prefs.gender == "Male") {
            10 * weightVal + 6.25 * prefs.height - 5 * prefs.age + 5
        } else {
            10 * weightVal + 6.25 * prefs.height - 5 * prefs.age - 161
        }

        val proteinTarget = (weightVal * 2.0).toInt()

        SettingsState(
            bmr = bmr.toInt(),
            goal = prefs.goal,
            offset = prefs.offset,
            proteinTarget = proteinTarget,
            activityLevel = prefs.activityLevel,
            gender = prefs.gender,
            age = prefs.age,
            height = prefs.height,
            latestWeight = weightVal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun updateSettings(
        goal: String,
        offset: Int,
        activityLevel: Float,
        gender: String,
        age: Int,
        height: Int
    ) {
        viewModelScope.launch {
            val latestWeight = weightDao.getLatestWeightEntry().first()?.weight ?: 70f
            
            val bmr = if (gender == "Male") {
                10 * latestWeight + 6.25 * height - 5 * age + 5
            } else {
                10 * latestWeight + 6.25 * height - 5 * age - 161
            }

            val proteinTarget = (latestWeight * 2.0).toInt()

            profileDao.insertProfile(
                ProfileEntry(
                    date = java.time.LocalDate.now().toString(),
                    activityLevel = activityLevel,
                    height = height,
                    age = age,
                    gender = gender,
                    goal = goal,
                    offset = offset,
                    proteinTarget = proteinTarget
                )
            )
            userPreferences.saveOnboardingData(bmr.toInt(), goal, offset, proteinTarget, activityLevel, gender, age, height)
        }
    }

    fun updateApiKey(key: String) {
        _apiKey.value = key
        encryptedStorage.saveApiKey(key)
        viewModelScope.launch {
            userPreferences.setCoachApiKeyValid(true)
        }
    }

    fun clearApiKey() {
        _apiKey.value = ""
        encryptedStorage.clearApiKey()
        viewModelScope.launch {
            userPreferences.setCoachApiKeyValid(false)
        }
    }

    fun exportData(context: Context, outputStream: OutputStream?) {
        viewModelScope.launch {
            outputStream?.use { output ->
                val dbFile = context.getDatabasePath("health_tracker_db")
                if (dbFile.exists()) {
                    dbFile.inputStream().use { input ->
                        input.copyTo(output)
                    }
                }
            }
        }
    }

    fun importData(context: Context, inputStream: InputStream?) {
        viewModelScope.launch {
            inputStream?.use { input ->
                val dbFile = context.getDatabasePath("health_tracker_db")
                dbFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        }
    }
}
