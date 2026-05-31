package com.tenebris.health_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tenebris.health_tracker.ui.dashboard.DashboardScreen
import com.tenebris.health_tracker.ui.dashboard.DashboardViewModel
import com.tenebris.health_tracker.ui.onboarding.OnboardingScreen
import com.tenebris.health_tracker.ui.onboarding.OnboardingViewModel
import com.tenebris.health_tracker.ui.progress.ProgressScreen
import com.tenebris.health_tracker.ui.progress.ProgressViewModel
import com.tenebris.health_tracker.ui.settings.SettingsScreen
import com.tenebris.health_tracker.ui.settings.SettingsViewModel
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.ui.coach.CoachViewModel
import com.tenebris.health_tracker.ui.theme.HealthTrackerTheme
import org.koin.androidx.compose.get as koinGet
import org.koin.androidx.compose.koinViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HealthTrackerTheme {
                HealthTrackerApp()
            }
        }
    }
}

@Composable
fun HealthTrackerApp() {
    val navController = rememberNavController()
    val userPrefs: UserPreferences = koinGet()
    val isOnboarded by userPrefs.isOnboarded.collectAsState(initial = null)

    if (isOnboarded == null) return

    NavHost(
        navController = navController,
        startDestination = if (isOnboarded == true) "main" else "onboarding",
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        composable("onboarding") {
            val onboardingViewModel: OnboardingViewModel = koinViewModel()
            OnboardingScreen(onboardingViewModel)
        }
        composable("main") {
            MainTabScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTabScreen() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        TabItem("dashboard", "Dashboard", Icons.Default.Dashboard),
        TabItem("progress", "Progress", Icons.AutoMirrored.Filled.ShowChart),
        TabItem("settings", "Settings", Icons.Default.Settings)
    )

    NavigationSuiteScaffold(
        navigationSuiteItems = {
            items.forEach { tab ->
                val selected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                item(
                    selected = selected,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    icon = {
                        val iconScale by animateFloatAsState(
                            targetValue = if (selected) 1.25f else 1f,
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            ),
                            label = "iconScale"
                        )
                        Icon(
                            tab.icon,
                            contentDescription = tab.label,
                            modifier = Modifier.graphicsLayer {
                                scaleX = iconScale
                                scaleY = iconScale
                            }
                        )
                    },
                    label = { Text(tab.label) }
                )
            }
        }
    ) {
        NavHost(
            navController = navController,
            startDestination = "dashboard",
            enterTransition = { androidx.compose.animation.EnterTransition.None },
            exitTransition = { androidx.compose.animation.ExitTransition.None },
            modifier = Modifier.fillMaxSize()
        ) {
            composable("dashboard") {
                val dashboardViewModel: DashboardViewModel = koinViewModel()
                val coachViewModel: CoachViewModel = koinViewModel()
                DashboardScreen(dashboardViewModel, coachViewModel)
            }
            composable("progress") {
                val progressViewModel: ProgressViewModel = koinViewModel()
                ProgressScreen(progressViewModel)
            }
            composable("settings") {
                val settingsViewModel: SettingsViewModel = koinViewModel()
                SettingsScreen(settingsViewModel)
            }
        }
    }
}

data class TabItem(val route: String, val label: String, val icon: ImageVector)
