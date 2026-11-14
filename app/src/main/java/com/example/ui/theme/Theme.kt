package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.platform.LocalContext
import com.example.data.model.ThemeMode

data class ChronoVisualTokens(
    val isHighContrast: Boolean = false,
    val isDark: Boolean = true,
    val glassIntensity: Float = 0.72f,
    val reducedMotion: Boolean = false
)

val LocalChronoVisualTokens = staticCompositionLocalOf { ChronoVisualTokens() }

@Composable
fun ChronoMusicTheme(
    themeMode: ThemeMode = ThemeMode.DARK,
    highContrastMode: Boolean = false,
    dynamicColor: Boolean = false,
    glassIntensity: Float = 0.72f,
    reducedMotion: Boolean = false,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val useDark = when (themeMode) {
        ThemeMode.DARK -> true
        ThemeMode.LIGHT -> false
        ThemeMode.SYSTEM -> systemDark
    }

    val colorScheme = when {
        highContrastMode -> ChronoHighContrastColorScheme
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (useDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        useDark -> ChronoDarkColorScheme
        else -> ChronoLightColorScheme
    }

    val tokens = ChronoVisualTokens(
        isHighContrast = highContrastMode,
        isDark = useDark || highContrastMode,
        glassIntensity = if (highContrastMode) 1f else glassIntensity,
        reducedMotion = reducedMotion
    )

    CompositionLocalProvider(LocalChronoVisualTokens provides tokens) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = ChronoTypography,
            content = content
        )
    }
}
