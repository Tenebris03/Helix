package com.tenebris.health_tracker.ui.coach

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tenebris.health_tracker.data.pref.UserPreferences
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

@Immutable
data class CoachUiState(
    val headline: String? = null,
    val body: String? = null,
    val visible: Boolean = false,
    val apiKeyInvalid: Boolean = false,
)

class CoachViewModel(
    private val userPreferences: UserPreferences,
) : ViewModel() {
    val state: StateFlow<CoachUiState> =
        combine(
            userPreferences.coachHeadline,
            userPreferences.coachBody,
            userPreferences.coachApiKeyValid,
        ) { headline, body, apiKeyValid ->
            CoachUiState(
                headline = headline,
                body = body,
                visible = headline != null && body != null,
                apiKeyInvalid = !apiKeyValid,
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CoachUiState())

    fun dismiss() {
        viewModelScope.launch {
            userPreferences.clearCoachResponse()
        }
    }
}
