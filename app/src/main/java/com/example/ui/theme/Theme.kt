package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Apk18proDarkColorScheme = darkColorScheme(
    primary = CinemaRed,
    onPrimary = Color.White,
    primaryContainer = CinemaRedDark,
    onPrimaryContainer = Color.White,
    secondary = CinemaGold,
    onSecondary = Color.Black,
    secondaryContainer = DarkSurfaceElevated,
    onSecondaryContainer = CinemaGold,
    tertiary = CinemaAmber,
    onTertiary = Color.Black,
    background = DarkBackground,
    onBackground = DarkTextPrimary,
    surface = DarkSurface,
    onSurface = DarkTextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = DarkTextSecondary,
    outline = CardBorder,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun Apk18proTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = Apk18proDarkColorScheme,
        typography = Typography,
        content = content
    )
}

