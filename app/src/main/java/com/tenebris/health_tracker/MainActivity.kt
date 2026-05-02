package com.tenebris.health_tracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.tenebris.health_tracker.data.local.AppDatabase
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.ui.dashboard.DashboardScreen
import com.tenebris.health_tracker.ui.dashboard.DashboardViewModel
import com.tenebris.health_tracker.ui.onboarding.OnboardingScreen
import com.tenebris.health_tracker.ui.onboarding.OnboardingViewModel
import com.tenebris.health_tracker.ui.progress.ProgressScreen
import com.tenebris.health_tracker.ui.progress.ProgressViewModel
import com.tenebris.health_tracker.ui.settings.SettingsScreen
import com.tenebris.health_tracker.ui.settings.SettingsViewModel
import com.tenebris.health_tracker.ui.theme.HealthTrackerTheme
import com.tenebris.health_tracker.ui.theme.NothingRed

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        val db = AppDatabase.getDatabase(this)
        val userPrefs = UserPreferences(this)
        
        setContent {
            HealthTrackerTheme {
                HealthTrackerApp(db, userPrefs)
            }
        }
    }
}

@Composable
fun HealthTrackerApp(db: AppDatabase, userPrefs: UserPreferences) {
    val navController = rememberNavController()
    val isOnboarded by userPrefs.isOnboarded.collectAsState(initial = null)

    if (isOnboarded == null) return

    NavHost(
        navController = navController,
        startDestination = if (isOnboarded == true) "main" else "onboarding",
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {
        composable("onboarding") {
            val onboardingViewModel: OnboardingViewModel = viewModel(
                factory = object : ViewModelProvider.Factory {
                    override fun <T : ViewModel> create(modelClass: Class<T>): T {
                        @Suppress("UNCHECKED_CAST")
                        return OnboardingViewModel(userPrefs, db.weightDao()) as T
                    }
                }
            )
            OnboardingScreen(onboardingViewModel)
        }
        composable("main") {
            MainTabScreen(db, userPrefs)
        }
    }
}

@Composable
fun MainTabScreen(db: AppDatabase, userPrefs: UserPreferences) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    val items = listOf(
        TabItem("dashboard", "Dashboard", Icons.Default.Dashboard),
        TabItem("progress", "Progress", Icons.AutoMirrored.Filled.ShowChart),
        TabItem("settings", "Settings", Icons.Default.Settings)
    )

    Scaffold { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            NavHost(
                navController = navController, 
                startDestination = "dashboard",
                enterTransition = { androidx.compose.animation.EnterTransition.None },
                exitTransition = { androidx.compose.animation.ExitTransition.None }
            ) {
                composable("dashboard") {
                    val dashboardViewModel: DashboardViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return DashboardViewModel(db.foodDao(), db.weightDao(), userPrefs) as T
                            }
                        }
                    )
                    DashboardScreen(dashboardViewModel)
                }
                composable("progress") {
                    val progressViewModel: ProgressViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return ProgressViewModel(db.weightDao()) as T
                            }
                        }
                    )
                    ProgressScreen(progressViewModel)
                }
                composable("settings") {
                    val settingsViewModel: SettingsViewModel = viewModel(
                        factory = object : ViewModelProvider.Factory {
                            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                                @Suppress("UNCHECKED_CAST")
                                return SettingsViewModel(userPrefs, db.weightDao()) as T
                            }
                        }
                    )
                    SettingsScreen(settingsViewModel)
                }
            }

            // Floating Pill Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp)
                    .background(Color(0xFF1A1A1A), RoundedCornerShape(100.dp))
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    items.forEach { item ->
                        val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null // Remove ripple for instant feel
                                ) {
                                    navController.navigate(item.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                                .padding(12.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                item.icon,
                                contentDescription = item.label,
                                tint = if (selected) NothingRed else Color.Gray,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

data class TabItem(val route: String, val label: String, val icon: ImageVector)
