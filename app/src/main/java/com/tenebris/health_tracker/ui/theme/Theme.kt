package com.tenebris.health_tracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ExpressiveDarkColorScheme =
    darkColorScheme(
        primary = ExpressivePrimary,
        onPrimary = ExpressiveOnPrimary,
        primaryContainer = ExpressivePrimaryContainer,
        onPrimaryContainer = ExpressiveOnPrimaryContainer,
        secondary = ExpressiveSecondary,
        onSecondary = ExpressiveOnSecondary,
        secondaryContainer = ExpressiveSecondaryContainer,
        onSecondaryContainer = ExpressiveOnSecondaryContainer,
        tertiary = ExpressiveTertiary,
        onTertiary = ExpressiveOnTertiary,
        tertiaryContainer = ExpressiveTertiaryContainer,
        onTertiaryContainer = ExpressiveOnTertiaryContainer,
        error = ExpressiveError,
        onError = ExpressiveOnError,
        errorContainer = ExpressiveErrorContainer,
        onErrorContainer = ExpressiveOnErrorContainer,
        background = ExpressiveBackground,
        onBackground = ExpressiveOnBackground,
        surface = ExpressiveSurface,
        onSurface = ExpressiveOnSurface,
        surfaceVariant = ExpressiveSurfaceVariant,
        onSurfaceVariant = ExpressiveOnSurfaceVariant,
        outline = ExpressiveOutline,
        outlineVariant = ExpressiveOutlineVariant,
        inverseSurface = ExpressiveInverseSurface,
        inverseOnSurface = ExpressiveInverseOnSurface,
        inversePrimary = ExpressiveInversePrimary,
        surfaceTint = ExpressiveSurfaceTint,
        surfaceDim = ExpressiveSurfaceDim,
        surfaceBright = ExpressiveSurfaceBright,
        surfaceContainerLowest = ExpressiveSurfaceContainerLowest,
        surfaceContainerLow = ExpressiveSurfaceContainerLow,
        surfaceContainer = ExpressiveSurfaceContainer,
        surfaceContainerHigh = ExpressiveSurfaceContainerHigh,
        surfaceContainerHighest = ExpressiveSurfaceContainerHighest,
    )

private val ExpressiveLightColorScheme =
    lightColorScheme(
        primary = ExpressivePrimaryLight,
        onPrimary = ExpressiveOnPrimaryLight,
        primaryContainer = ExpressivePrimaryContainerLight,
        onPrimaryContainer = ExpressiveOnPrimaryContainerLight,
        secondary = ExpressiveSecondaryLight,
        onSecondary = ExpressiveOnSecondaryLight,
        secondaryContainer = ExpressiveSecondaryContainerLight,
        onSecondaryContainer = ExpressiveOnSecondaryContainerLight,
        tertiary = ExpressiveTertiaryLight,
        onTertiary = ExpressiveOnTertiaryLight,
        tertiaryContainer = ExpressiveTertiaryContainerLight,
        onTertiaryContainer = ExpressiveOnTertiaryContainerLight,
        error = ExpressiveErrorLight,
        onError = ExpressiveOnErrorLight,
        errorContainer = ExpressiveErrorContainerLight,
        onErrorContainer = ExpressiveOnErrorContainerLight,
        background = ExpressiveBackgroundLight,
        onBackground = ExpressiveOnBackgroundLight,
        surface = ExpressiveSurfaceLight,
        onSurface = ExpressiveOnSurfaceLight,
        surfaceVariant = ExpressiveSurfaceVariantLight,
        onSurfaceVariant = ExpressiveOnSurfaceVariantLight,
        outline = ExpressiveOutlineLight,
        outlineVariant = ExpressiveOutlineVariantLight,
        inverseSurface = ExpressiveInverseSurfaceLight,
        inverseOnSurface = ExpressiveInverseOnSurfaceLight,
        inversePrimary = ExpressiveInversePrimaryLight,
        surfaceTint = ExpressiveSurfaceTintLight,
        surfaceDim = ExpressiveSurfaceDimLight,
        surfaceBright = ExpressiveSurfaceBrightLight,
        surfaceContainerLowest = ExpressiveSurfaceContainerLowestLight,
        surfaceContainerLow = ExpressiveSurfaceContainerLowLight,
        surfaceContainer = ExpressiveSurfaceContainerLight,
        surfaceContainerHigh = ExpressiveSurfaceContainerHighLight,
        surfaceContainerHighest = ExpressiveSurfaceContainerHighestLight,
    )

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun HealthTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }
            darkTheme -> ExpressiveDarkColorScheme
            else -> ExpressiveLightColorScheme
        }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            val controller = WindowCompat.getInsetsController(window, view)
            controller.isAppearanceLightStatusBars = !darkTheme
            controller.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialExpressiveTheme(
        colorScheme = colorScheme,
        typography = ExpressiveTypography,
        shapes = ExpressiveShapes,
        content = content,
    )
}
