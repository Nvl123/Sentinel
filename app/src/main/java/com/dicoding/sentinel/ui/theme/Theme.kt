package com.dicoding.sentinel.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val MdPrimary = Color(0xFF0061A4)
val MdOnPrimary = Color(0xFFFFFFFF)
val MdPrimaryContainer = Color(0xFFD1E4FF)
val MdOnPrimaryContainer = Color(0xFF001D36)

val MdSecondary = Color(0xFF535F70)
val MdOnSecondary = Color(0xFFFFFFFF)
val MdSecondaryContainer = Color(0xFFD7E3F7)
val MdOnSecondaryContainer = Color(0xFF101C2B)

val MdTertiary = Color(0xFF6B5778)
val MdOnTertiary = Color(0xFFFFFFFF)
val MdTertiaryContainer = Color(0xFFF2DAFF)
val MdOnTertiaryContainer = Color(0xFF251431)

val MdBackground = Color(0xFFFDFCFF)
val MdOnBackground = Color(0xFF1A1C1E)
val MdSurface = Color(0xFFFDFCFF)
val MdOnSurface = Color(0xFF1A1C1E)
val MdSurfaceVariant = Color(0xFFDFE2EB)
val MdOnSurfaceVariant = Color(0xFF43474E)

val MdError = Color(0xFFBA1A1A)
val MdOnError = Color(0xFFFFFFFF)

private val LightColorScheme = lightColorScheme(
    primary = MdPrimary,
    onPrimary = MdOnPrimary,
    primaryContainer = MdPrimaryContainer,
    onPrimaryContainer = MdOnPrimaryContainer,
    secondary = MdSecondary,
    onSecondary = MdOnSecondary,
    secondaryContainer = MdSecondaryContainer,
    onSecondaryContainer = MdOnSecondaryContainer,
    tertiary = MdTertiary,
    onTertiary = MdOnTertiary,
    tertiaryContainer = MdTertiaryContainer,
    onTertiaryContainer = MdOnTertiaryContainer,
    background = MdBackground,
    onBackground = MdOnBackground,
    surface = MdSurface,
    onSurface = MdOnSurface,
    surfaceVariant = MdSurfaceVariant,
    onSurfaceVariant = MdOnSurfaceVariant,
    error = MdError,
    onError = MdOnError
)

private val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF9ECAFF),
    onPrimary = Color(0xFF003258),
    primaryContainer = Color(0xFF00497D),
    onPrimaryContainer = MdPrimaryContainer,
    secondary = Color(0xFFBBC7DB),
    onSecondary = Color(0xFF253140),
    secondaryContainer = Color(0xFF3B4858),
    onSecondaryContainer = MdSecondaryContainer,
    background = MdOnBackground,
    onBackground = MdBackground,
    surface = MdOnBackground,
    onSurface = MdBackground,
    surfaceVariant = MdOnSurfaceVariant,
    onSurfaceVariant = MdSurfaceVariant,
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
