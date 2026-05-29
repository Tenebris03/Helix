package com.tenebris.health_tracker.ui.dashboard

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.local.ProfileDao
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.repository.FoodRepository
import com.tenebris.health_tracker.data.repository.VisionRepository
import android.graphics.Bitmap
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tenebris.health_tracker.data.worker.InvisibleCoachWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@Immutable
data class DashboardState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entries: List<FoodEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 150,
    val recentEntries: List<FoodEntry> = emptyList()
)

@Stable
sealed class ScannerState {
    object Idle : ScannerState()
    object Loading : ScannerState()
    data class Success(val name: String, val calories100g: Int, val protein100g: Int) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

class DashboardViewModel(
    private val repository: FoodRepository,
    private val visionRepository: VisionRepository,
    private val weightDao: com.tenebris.health_tracker.data.local.WeightDao,
    private val profileDao: ProfileDao,
    private val userPreferences: UserPreferences,
    private val application: android.app.Application
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState

    init {
        viewModelScope.launch {
            if (profileDao.getLatestProfile().first() == null) {
                val goal = userPreferences.goal.first()
                val offset = userPreferences.offset.first()
                val activity = userPreferences.activityLevel.first()
                val gender = userPreferences.gender.first()
                val age = userPreferences.age.first()
                val height = userPreferences.height.first()
                val protein = userPreferences.proteinTarget.first()
                
                profileDao.insertProfile(
                    ProfileEntry(
                        date = LocalDate.now().toString(),
                        activityLevel = activity,
                        height = height,
                        age = age,
                        gender = gender,
                        goal = goal,
                        offset = offset,
                        proteinTarget = protein
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DashboardState> = _selectedDate.flatMapLatest { date ->
        combine(
            weightDao.getWeightAtDate(date.toString()),
            profileDao.getProfileAtDate(date.toString()),
            userPreferences.goal,
            userPreferences.offset,
            userPreferences.proteinTarget,
            userPreferences.activityLevel,
            userPreferences.gender,
            userPreferences.age,
            userPreferences.height
        ) { params: Array<Any?> ->
            val weightEntry = params[0] as? WeightEntry
            val profileEntry = params[1] as? ProfileEntry
            val pGoal = params[2] as String
            val pOffset = params[3] as Int
            val pProtein = params[4] as Int
            val pActivity = params[5] as Float
            val pGender = params[6] as String
            val pAge = params[7] as Int
            val pHeight = params[8] as Int

            val weightVal = weightEntry?.weight ?: 70f
            val goal = profileEntry?.goal ?: pGoal
            val offset = profileEntry?.offset ?: pOffset
            val activityLevel = profileEntry?.activityLevel ?: pActivity
            val gender = profileEntry?.gender ?: pGender
            val age = profileEntry?.age ?: pAge
            val height = profileEntry?.height ?: pHeight
            val proteinTarget = profileEntry?.proteinTarget ?: pProtein
            
            // Recalculate BMR dynamically: 10*weight + 6.25*height - 5*age + s
            val bmr = if (gender == "Male") {
                10 * weightVal + 6.25 * height - 5 * age + 5
            } else {
                10 * weightVal + 6.25 * height - 5 * age - 161
            }

            // TDEE = BMR * PAL
            val tdee = bmr * activityLevel
            val adjustedTarget = when (goal) {
                "Lose" -> tdee - offset
                "Gain" -> tdee + offset
                else -> tdee
            }
            
            Triple(date, adjustedTarget.toInt(), proteinTarget)
        }.flowOn(Dispatchers.Default).flatMapLatest { (date, targetCal, targetProt) ->
            combine(
                repository.getEntriesByDate(date),
                repository.getUniqueRecentEntries()
            ) { entries, recent ->
                DashboardState(
                    selectedDate = date,
                    entries = entries,
                    totalCalories = entries.sumOf { it.calories },
                    totalProtein = entries.sumOf { it.protein },
                    targetCalories = targetCal,
                    targetProtein = targetProt,
                    recentEntries = recent
                )
            }.flowOn(Dispatchers.Default)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addFood(name: String, kcal: Int, protein: Int) {
        viewModelScope.launch {
            repository.addFoodEntry(
                FoodEntry(
                    name = name,
                    calories = kcal,
                    protein = protein,
                    date = _selectedDate.value.toString()
                )
            )
            _scannerState.value = ScannerState.Idle
            triggerCoach("$name: ${kcal}kcal, ${protein}g protein")
        }
    }

    private fun triggerCoach(mealLog: String) {
        val workRequest = OneTimeWorkRequestBuilder<InvisibleCoachWorker>()
            .setInputData(workDataOf(InvisibleCoachWorker.KEY_MEAL_LOG to mealLog))
            .build()
        WorkManager.getInstance(application)
            .enqueueUniqueWork("invisible_coach", ExistingWorkPolicy.REPLACE, workRequest)
    }

    fun deleteFood(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFoodEntry(entry)
        }
    }

    fun onBarcodeScanned(barcode: String) {
        if (_scannerState.value is ScannerState.Loading) return
        
        _scannerState.value = ScannerState.Loading
        viewModelScope.launch {
            repository.getProductByBarcode(barcode)
                .onSuccess { product ->
                    _scannerState.value = ScannerState.Success(
                        name = product.name,
                        calories100g = product.calories100g,
                        protein100g = product.protein100g
                    )
                }
                .onFailure { error ->
                    _scannerState.value = ScannerState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun onFoodImageCaptured(bitmap: Bitmap) {
        if (_scannerState.value is ScannerState.Loading) return

        _scannerState.value = ScannerState.Loading
        viewModelScope.launch {
            visionRepository.recognizeFood(bitmap)
                .onSuccess { entry ->
                    _scannerState.value = ScannerState.Success(
                        name = entry.name,
                        calories100g = entry.calories,
                        protein100g = entry.protein
                    )
                }
                .onFailure { error ->
                    _scannerState.value = ScannerState.Error(error.message ?: "Vision error")
                }
        }
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}
