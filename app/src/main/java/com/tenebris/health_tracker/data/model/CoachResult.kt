package com.tenebris.health_tracker.data.model

sealed class CoachResult {
    data class Success(val response: CoachResponse) : CoachResult()
    data object AuthError : CoachResult()
    data object RateLimited : CoachResult()
    data object OtherError : CoachResult()
}
