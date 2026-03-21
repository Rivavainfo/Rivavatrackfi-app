package com.trackfi.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

val StandardColorScheme = darkColorScheme(
    primary = PrimarySky,
    primaryContainer = PrimaryContainer,
    secondary = SecondaryPink,
    secondaryContainer = SecondaryContainer,
    tertiary = TertiaryEmerald,
    onPrimary = SurfaceContainerLow, // Use dark for text on primary
    background = SurfaceDark,
    onBackground = OnDarkSurface,
    surface = SurfaceContainerLow,
    onSurface = OnDarkSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    outlineVariant = OutlineVariant
)

val PremiumColorScheme = darkColorScheme(
    primary = PrimarySky,
    primaryContainer = PrimaryContainer,
    secondary = SecondaryPink,
    secondaryContainer = SecondaryContainer,
    tertiary = TertiaryEmerald,
    onPrimary = SurfaceContainerLow,
    background = SurfaceDark,
    onBackground = OnDarkSurface,
    surface = SurfaceContainerLow,
    onSurface = OnDarkSurface,
    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnDarkSurfaceVariant,
    error = ErrorRed,
    errorContainer = ErrorContainer,
    outlineVariant = OutlineVariant
)

@Composable
fun TrackFiTheme(
    isPremium: Boolean = false,
    content: @Composable () -> Unit
) {
    // Both are identical for the new unified theme, leaving structure for future premium-only features.
    val colorScheme = if (isPremium) PremiumColorScheme else StandardColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = android.graphics.Color.TRANSPARENT
            window.navigationBarColor = android.graphics.Color.TRANSPARENT
            WindowCompat.setDecorFitsSystemWindows(window, false)
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
