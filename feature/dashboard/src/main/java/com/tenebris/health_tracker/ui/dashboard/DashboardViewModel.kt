package com.tenebris.health_tracker.ui.dashboard

import android.graphics.Bitmap
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.MealType
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.pref.UserPreferences.PrefsSnapshot
import com.tenebris.health_tracker.data.repository.FoodRepository
import com.tenebris.health_tracker.data.repository.ProfileRepository
import com.tenebris.health_tracker.data.repository.VisionRepository
import com.tenebris.health_tracker.data.repository.WeightRepository
import com.tenebris.health_tracker.data.service.CalorieCalculator
import com.tenebris.health_tracker.data.service.CalorieTargets
import com.tenebris.health_tracker.data.service.FoodProblemDetector
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
    val totalFat: Int = 0,
    val totalCarbs: Int = 0,
    val totalFiber: Int = 0,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 150,
    val currentWeight: Float = 70f,
    val recentEntries: List<FoodEntry> = emptyList(),
)

private data class TargetsWithWeight(
    val targets: CalorieTargets,
    val weightKg: Float,
)

@Stable
sealed class ScannerState {
    object Idle : ScannerState()

    object Loading : ScannerState()

    data class Success(
        val name: String,
        val calories100g: Int,
        val protein100g: Int,
        val fat100g: Int = 0,
        val carbohydrates100g: Int = 0,
        val fiber100g: Int = 0,
        val estimatedWeightGrams: Int = 100,
    ) : ScannerState()

    data class Error(
        val message: String,
    ) : ScannerState()
}

class DashboardViewModel(
    private val repository: FoodRepository,
    private val visionRepository: VisionRepository,
    private val weightRepository: WeightRepository,
    private val profileRepository: ProfileRepository,
    private val userPreferences: UserPreferences,
    private val workManager: WorkManager,
    private val foodProblemDetector: FoodProblemDetector,
) : ViewModel() {
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState

    private val _editingEntry = MutableStateFlow<FoodEntry?>(null)
    val editingEntry: StateFlow<FoodEntry?> = _editingEntry

    init {
        viewModelScope.launch {
            profileRepository.getLatestProfile().take(1).collect { latest ->
                if (latest == null) {
                    val goal = userPreferences.goal.first()
                    val offset = userPreferences.offset.first()
                    val activity = userPreferences.activityLevel.first()
                    val gender = userPreferences.gender.first()
                    val age = userPreferences.age.first()
                    val height = userPreferences.height.first()
                    val protein = userPreferences.proteinTarget.first()

                    profileRepository.upsertProfile(
                        ProfileEntry(
                            date = LocalDate.now().toString(),
                            activityLevel = activity,
                            height = height,
                            age = age,
                            gender = gender,
                            goal = goal,
                            offset = offset,
                            proteinTarget = protein,
                        ),
                    )
                }
            }
        }
    }

    private val prefsFlow: Flow<PrefsSnapshot> = userPreferences.snapshot

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DashboardState> =
        _selectedDate
            .flatMapLatest { date ->
                combine(
                    weightRepository.getWeightAtDate(date.toString()),
                    profileRepository.getProfileAtDate(date.toString()),
                    prefsFlow,
                ) { weightEntry, profileEntry, prefs ->
                    val weightVal = weightEntry?.weight ?: 70f
                    val goal = profileEntry?.goal ?: prefs.goal
                    val offset = profileEntry?.offset ?: prefs.offset
                    val activityLevel = profileEntry?.activityLevel ?: prefs.activityLevel
                    val gender = profileEntry?.gender ?: prefs.gender
                    val age = profileEntry?.age ?: prefs.age
                    val height = profileEntry?.height ?: prefs.height
                    val proteinTarget = profileEntry?.proteinTarget ?: prefs.proteinTarget

                    val targets = CalorieCalculator.compute(
                        weightKg = weightVal,
                        heightCm = height,
                        age = age,
                        gender = gender,
                        goal = goal,
                        offset = offset,
                        activityLevel = activityLevel,
                        proteinTarget = proteinTarget,
                    )
                    TargetsWithWeight(targets, weightVal)
                }.flatMapLatest { (targets, weightVal) ->
                    combine(
                        repository.getEntriesByDate(date),
                        repository.getUniqueRecentEntries(),
                    ) { entries, recent ->
                        DashboardState(
                            selectedDate = date,
                            entries = entries,
                            totalCalories = entries.sumOf { it.calories },
                            totalProtein = entries.sumOf { it.protein },
                            totalFat = entries.sumOf { it.fat },
                            totalCarbs = entries.sumOf { it.carbohydrates },
                            totalFiber = entries.sumOf { it.fiber },
                            targetCalories = targets.adjustedTarget,
                            targetProtein = targets.proteinTarget,
                            currentWeight = weightVal,
                            recentEntries = recent,
                        )
                    }
                }.flowOn(Dispatchers.Default)
            }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), DashboardState())

    fun selectDate(date: LocalDate) {
        _selectedDate.value = date
    }

    fun addFood(
        name: String,
        kcal: Int,
        protein: Int,
        fat: Int = 0,
        carbs: Int = 0,
        fiber: Int = 0,
        mealType: MealType = MealType.SNACK,
    ) {
        viewModelScope.launch {
            repository.addFoodEntry(
                FoodEntry(
                    name = name,
                    calories = kcal,
                    protein = protein,
                    fat = fat,
                    carbohydrates = carbs,
                    fiber = fiber,
                    date = _selectedDate.value.toString(),
                    mealType = mealType,
                ),
            )
            _scannerState.value = ScannerState.Idle
            val hour =
                java.time.LocalTime
                    .now()
                    .hour
            if (foodProblemDetector.isProblematic(name, kcal, hour)) {
                val remaining = state.value.targetCalories - state.value.totalCalories - kcal
                triggerCoach("$name: ${kcal}kcal, ${protein}g protein", remaining)
            }
        }
    }

    private fun triggerCoach(
        mealLog: String,
        remainingCalories: Int,
    ) {
        val workRequest =
            OneTimeWorkRequestBuilder<InvisibleCoachWorker>()
                .setInputData(
                    workDataOf(
                        InvisibleCoachWorker.KEY_MEAL_LOG to mealLog,
                        InvisibleCoachWorker.KEY_REMAINING_CALORIES to remainingCalories,
                    ),
                ).build()
        workManager.enqueueUniqueWork("invisible_coach", ExistingWorkPolicy.REPLACE, workRequest)
    }

    fun deleteFood(entry: FoodEntry) {
        viewModelScope.launch {
            repository.deleteFoodEntry(entry)
        }
    }

    fun startEdit(entry: FoodEntry) {
        _editingEntry.value = entry
    }

    fun cancelEdit() {
        _editingEntry.value = null
    }

    fun saveEdit(entry: FoodEntry) {
        viewModelScope.launch {
            repository.updateFoodEntry(entry)
            _editingEntry.value = null
        }
    }

    fun onBarcodeScanned(barcode: String) {
        if (_scannerState.value is ScannerState.Loading || _scannerState.value is ScannerState.Success) return

        _scannerState.value = ScannerState.Loading
        viewModelScope.launch {
            repository
                .getProductByBarcode(barcode)
                .onSuccess { product ->
                    _scannerState.value =
                        ScannerState.Success(
                            name = product.name,
                            calories100g = product.calories100g,
                            protein100g = product.protein100g,
                            fat100g = product.fat100g,
                            carbohydrates100g = product.carbohydrates100g,
                            fiber100g = product.fiber100g,
                        )
                }.onFailure { error ->
                    _scannerState.value = ScannerState.Error(error.message ?: "Unknown error")
                }
        }
    }

    fun onFoodImageCaptured(bitmap: Bitmap) {
        if (_scannerState.value is ScannerState.Loading || _scannerState.value is ScannerState.Success) return

        _scannerState.value = ScannerState.Loading
        viewModelScope.launch {
            visionRepository
                .recognizeFood(bitmap)
                .onSuccess { result ->
                    _scannerState.value =
                        ScannerState.Success(
                            name = result.name,
                            calories100g = result.calories100g,
                            protein100g = result.protein100g,
                            fat100g = result.fat100g,
                            carbohydrates100g = result.carbohydrates100g,
                            fiber100g = result.fiber100g,
                            estimatedWeightGrams = result.estimatedWeightGrams,
                        )
                }.onFailure { error ->
                    _scannerState.value = ScannerState.Error(error.message ?: "Vision error")
                }
        }
    }

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}
