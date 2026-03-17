package com.dicoding.sentinel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DeepNavy = Color(0xFF0F172A)
val Charcoal = Color(0xFF1E293B)
val NeonCyan = Color(0xFF00D2FF)
val ElectricBlue = Color(0xFF3B82F6)
val ErrorRed = Color(0xFFEF4444)
val SurfaceGray = Color(0xFF334155)

private val DarkColorScheme = darkColorScheme(
    primary = NeonCyan,
    secondary = ElectricBlue,
    tertiary = SurfaceGray,
    background = DeepNavy,
    surface = Charcoal,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
    error = ErrorRed,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricBlue,
    secondary = NeonCyan,
    tertiary = SurfaceGray,
    background = Color.White,
    surface = Color(0xFFF1F5F9),
    onPrimary = Color.White,
    onSecondary = Color.Black,
    onTertiary = Color.White,
    onBackground = DeepNavy,
    onSurface = DeepNavy,
    error = ErrorRed,
    onError = Color.White
)

@Composable
fun SentinelTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = MaterialTheme.typography, // Use default for now, can customize later
        content = content
    )
}
