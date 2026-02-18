package com.mylauncher.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.mylauncher.data.model.AccentColor

private fun darkColorScheme(accent: Color) = darkColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.3f),
    onPrimaryContainer = Color.White,
    surface = Color(0xFF1A1A1A),
    onSurface = Color.White,
    background = Color(0xFF000000),
    onBackground = Color.White,
    surfaceVariant = Color(0xFF2A2A2A),
    onSurfaceVariant = Color(0xFFCCCCCC),
    outline = Color(0xFF444444)
)

private fun lightColorScheme(accent: Color) = lightColorScheme(
    primary = accent,
    onPrimary = Color.White,
    primaryContainer = accent.copy(alpha = 0.12f),
    onPrimaryContainer = Color.Black,
    surface = Color.White,
    onSurface = Color.Black,
    background = Color(0xFFF5F5F5),
    onBackground = Color.Black,
    surfaceVariant = Color(0xFFEEEEEE),
    onSurfaceVariant = Color(0xFF333333)
)

@Composable
fun MyLauncherTheme(
    accentColor: AccentColor = AccentColor.COBALT,
    isDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(accentColor.color)
    } else {
        lightColorScheme(accentColor.color)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MetroTypography,
        content = content
    )
}
