package com.tenebris.health_tracker.navigation

sealed class Route(
    val route: String,
) {
    data object Onboarding : Route("onboarding")

    data object Main : Route("main")

    data object Dashboard : Route("dashboard")

    data object Progress : Route("progress")

    data object Settings : Route("settings")
}
