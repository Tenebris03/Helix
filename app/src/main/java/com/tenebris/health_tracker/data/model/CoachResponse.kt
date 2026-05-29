package com.tenebris.health_tracker.data.model

import kotlinx.serialization.Serializable

@Serializable
data class CoachResponse(
    val criticalAlert: Boolean,
    val reasonHeadline: String,
    val reasonBody: String
)
