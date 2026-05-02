package com.tenebris.health_tracker.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val NothingBackground = Color.Black // Pure Black
val NothingRed = Color(0xFFD71921)
val NothingSurface = Color(0xFF1A1A1A) // Slightly lighter for cards

private val DarkColorScheme = darkColorScheme(
    primary = Color.White,
    secondary = Color.Gray,
    tertiary = NothingRed,
    background = NothingBackground,
    surface = NothingSurface,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = NothingRed
)

private val LightColorScheme = lightColorScheme(
    primary = Color.Black,
    secondary = Color.Gray,
    tertiary = NothingRed,
    background = Color.White,
    surface = Color(0xFFF5F5F5),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = Color.Black,
    onSurface = Color.Black,
    error = NothingRed
)

@Composable
fun HealthTrackerTheme(
    darkTheme: Boolean = true, // Force dark theme for the requested look
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
