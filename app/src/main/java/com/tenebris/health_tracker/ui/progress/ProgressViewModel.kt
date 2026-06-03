package com.tenebris.health_tracker.ui.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.model.WeightEntry
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class ProgressState(
    val weightEntries: List<WeightEntry> = emptyList(),
)

class ProgressViewModel(
    private val weightDao: WeightDao,
) : ViewModel() {
    val state: StateFlow<ProgressState> =
        weightDao
            .getAllWeightEntries()
            .map { ProgressState(it) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProgressState())

    fun addWeight(weight: Float) {
        viewModelScope.launch {
            weightDao.insertWeight(
                WeightEntry(
                    weight = weight,
                    date = LocalDate.now().toString(),
                ),
            )
        }
    }

    fun deleteWeight(entry: WeightEntry) {
        viewModelScope.launch {
            weightDao.deleteWeight(entry)
        }
    }
}
