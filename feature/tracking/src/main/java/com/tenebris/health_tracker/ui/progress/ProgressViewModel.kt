package com.tenebris.health_tracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.model.WeightEntry
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.repository.WeightRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProgressState(
    val weightEntries: List<WeightEntry> = emptyList(),
    val targetWeight: Float = 70f,
)

class ProgressViewModel(
    private val weightRepository: WeightRepository,
    private val userPreferences: UserPreferences,
) : ViewModel() {
    @OptIn(ExperimentalCoroutinesApi::class)
    val state: StateFlow<ProgressState> =
        combine(
            weightRepository.getAllWeightEntries(),
            userPreferences.targetWeight,
        ) { entries, target ->
            ProgressState(entries, target)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressState())

    fun addWeight(weight: Float) {
        viewModelScope.launch {
            weightRepository.insertWeight(
                WeightEntry(
                    weight = weight,
                    date = LocalDate.now().toString(),
                ),
            )
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightRepository.deleteWeight(entry)
        }
    }
}
