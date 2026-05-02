package com.tenebris.health_tracker.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.model.FoodEntry
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.repository.FoodRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.LocalDate

data class DashboardState(
    val selectedDate: LocalDate = LocalDate.now(),
    val entries: List<FoodEntry> = emptyList(),
    val totalCalories: Int = 0,
    val totalProtein: Int = 0,
    val targetCalories: Int = 2000,
    val targetProtein: Int = 150
)

sealed class ScannerState {
    object Idle : ScannerState()
    object Loading : ScannerState()
    data class Success(val name: String, val calories100g: Int, val protein100g: Int) : ScannerState()
    data class Error(val message: String) : ScannerState()
}

class DashboardViewModel(
    private val repository: FoodRepository,
    private val weightDao: com.tenebris.health_tracker.data.local.WeightDao,
    private val userPreferences: UserPreferences
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    private val _scannerState = MutableStateFlow<ScannerState>(ScannerState.Idle)
    val scannerState: StateFlow<ScannerState> = _scannerState

    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<DashboardState> = combine(
        _selectedDate,
        userPreferences.goal,
        userPreferences.offset,
        userPreferences.proteinTarget,
        userPreferences.activityLevel,
        userPreferences.gender,
        userPreferences.age,
        userPreferences.height,
        weightDao.getLatestWeightEntry()
    ) { params ->
        val date = params[0] as LocalDate
        val goal = params[1] as String
        val offset = params[2] as Int
        val proteinTarget = params[3] as Int
        val activityLevel = params[4] as Float
        val gender = params[5] as String
        val age = params[6] as Int
        val height = params[7] as Int
        val latestWeight = params[8] as? WeightEntry
        
        val weightVal = latestWeight?.weight ?: 70f
        
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
    }.flatMapLatest { (date, targetCal, targetProt) ->
        repository.getEntriesByDate(date).map { entries ->
            DashboardState(
                selectedDate = date,
                entries = entries,
                totalCalories = entries.sumOf { it.calories },
                totalProtein = entries.sumOf { it.protein },
                targetCalories = targetCal,
                targetProtein = targetProt
            )
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
        }
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

    fun resetScanner() {
        _scannerState.value = ScannerState.Idle
    }
}
