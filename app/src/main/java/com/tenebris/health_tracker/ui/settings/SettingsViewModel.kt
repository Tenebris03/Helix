package com.tenebris.health_tracker.ui.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.io.File
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
    val latestWeight: Float = 70f
)

class SettingsViewModel(
    private val userPreferences: UserPreferences,
    private val weightDao: WeightDao
) : ViewModel() {

    val state: StateFlow<SettingsState> = combine(
        userPreferences.goal,
        userPreferences.offset,
        userPreferences.activityLevel,
        userPreferences.gender,
        userPreferences.age,
        userPreferences.height,
        weightDao.getLatestWeightEntry()
    ) { params ->
        val goal = params[0] as String
        val offset = params[1] as Int
        val activityLevel = params[2] as Float
        val gender = params[3] as String
        val age = params[4] as Int
        val height = params[5] as Int
        val latestWeightEntry = params[6] as? WeightEntry
        
        val weightVal = latestWeightEntry?.weight ?: 70f
        
        val bmr = if (gender == "Male") {
            10 * weightVal + 6.25 * height - 5 * age + 5
        } else {
            10 * weightVal + 6.25 * height - 5 * age - 161
        }
        
        val proteinTarget = (weightVal * 2.0).toInt()

        SettingsState(
            bmr = bmr.toInt(),
            goal = goal,
            offset = offset,
            proteinTarget = proteinTarget,
            activityLevel = activityLevel,
            gender = gender,
            age = age,
            height = height,
            latestWeight = weightVal
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun updateSettings(
        bmr: Int, 
        goal: String, 
        offset: Int, 
        proteinTarget: Int, 
        activityLevel: Float,
        gender: String,
        age: Int,
        height: Int
    ) {
        viewModelScope.launch {
            userPreferences.saveOnboardingData(bmr, goal, offset, proteinTarget, activityLevel, gender, age, height)
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
                // Trigger a process restart or notify user to restart app for Room to reload
            }
        }
    }
}
