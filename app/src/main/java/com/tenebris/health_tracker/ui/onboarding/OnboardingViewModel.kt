package com.tenebris.health_tracker.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.local.ProfileDao
import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.model.ProfileEntry
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate

data class OnboardingState(
    val gender: String = "Male",
    val age: String = "",
    val height: String = "",
    val weight: String = "",
    val activityLevel: Float = 1.2f,
    val goal: String = "Maintain",
    val offset: Int = 0
)

class OnboardingViewModel(
    private val userPreferences: UserPreferences,
    private val weightDao: WeightDao,
    private val profileDao: ProfileDao
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun updateGender(gender: String) { _state.value = _state.value.copy(gender = gender) }
    fun updateAge(age: String) { _state.value = _state.value.copy(age = age) }
    fun updateHeight(height: String) { _state.value = _state.value.copy(height = height) }
    fun updateWeight(weight: String) { _state.value = _state.value.copy(weight = weight) }
    fun updateActivityLevel(level: Float) { _state.value = _state.value.copy(activityLevel = level) }
    fun updateGoal(goal: String) { _state.value = _state.value.copy(goal = goal) }
    fun updateOffset(offset: Int) { _state.value = _state.value.copy(offset = offset.coerceIn(0, 1000)) }

    fun completeOnboarding() {
        val s = _state.value
        val ageVal = s.age.toIntOrNull() ?: 25
        val heightVal = s.height.toDoubleOrNull() ?: 170.0
        val weightVal = s.weight.toDoubleOrNull() ?: 70.0

        // Mifflin-St Jeor Equation (Base BMR)
        val bmr = if (s.gender == "Male") {
            10 * weightVal + 6.25 * heightVal - 5 * ageVal + 5
        } else {
            10 * weightVal + 6.25 * heightVal - 5 * ageVal - 161
        }

        val proteinTarget = (weightVal * 2.0).toInt()

        viewModelScope.launch {
            val date = LocalDate.now().toString()
            weightDao.insertWeight(
                WeightEntry(
                    weight = weightVal.toFloat(),
                    date = date
                )
            )
            profileDao.insertProfile(
                ProfileEntry(
                    date = date,
                    activityLevel = s.activityLevel,
                    height = heightVal.toInt(),
                    age = ageVal,
                    gender = s.gender,
                    goal = s.goal,
                    offset = s.offset,
                    proteinTarget = proteinTarget
                )
            )
            userPreferences.saveOnboardingData(
                bmr.toInt(), 
                s.goal, 
                s.offset, 
                proteinTarget, 
                s.activityLevel,
                s.gender,
                ageVal,
                heightVal.toInt()
            )
        }
    }
}
