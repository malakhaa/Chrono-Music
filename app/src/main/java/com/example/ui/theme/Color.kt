package com.example.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Obsidian Studio Dark (Default Chrono Music Palette)
val ObsidianBg = Color(0xFF070A11)
val ObsidianSurface = Color(0xFF0E1422)
val ObsidianSurfaceVariant = Color(0xFF161E31)
val ObsidianContainerHigh = Color(0xFF1E2840)
val ElectricCyan = Color(0xFF00E5FF)
val CyanMuted = Color(0xFF005766)
val TubeAmber = Color(0xFFFFB300)
val AmberMuted = Color(0xFF573B00)
val StudioViolet = Color(0xFF9D7BFF)
val CrispWhite = Color(0xFFEEF4FF)
val SlateSecondary = Color(0xFF9BA8C4)
val HairlineDark = Color(0xFF23304D)
val ErrorCrimson = Color(0xFFFF5C77)
val HiResGold = Color(0xFFFFD54F)
val CertifiedEmerald = Color(0xFF2CE69B)

// Alabaster Minimal Light
val AlabasterBg = Color(0xFFF4F6FA)
val AlabasterSurface = Color(0xFFFFFFFF)
val AlabasterSurfaceVariant = Color(0xFFE7ECF5)
val DeepTeal = Color(0xFF006876)
val WarmBronze = Color(0xFF8F5200)
val DeepViolet = Color(0xFF5B3CC4)
val InkBlack = Color(0xFF0F172A)
val InkMuted = Color(0xFF475569)
val HairlineLight = Color(0xFFCBD5E1)

// WCAG AAA High-Contrast Palette (>= 7:1 Contrast Ratios)
val HighContrastBlack = Color(0xFF000000)
val HighContrastSurface = Color(0xFF0A0A0A)
val HighContrastSurfaceVariant = Color(0xFF141414)
val HighContrastCyan = Color(0xFF00FFFF)
val HighContrastYellow = Color(0xFFFFEA00)
val HighContrastWhite = Color(0xFFFFFFFF)
val HighContrastBorder = Color(0xFFFFFFFF)

val ChronoDarkColorScheme = darkColorScheme(
    primary = ElectricCyan,
    onPrimary = Color(0xFF002026),
    primaryContainer = CyanMuted,
    onPrimaryContainer = Color(0xFFB5F8FF),
    secondary = TubeAmber,
    onSecondary = Color(0xFF291800),
    secondaryContainer = AmberMuted,
    onSecondaryContainer = Color(0xFFFFE099),
    tertiary = StudioViolet,
    onTertiary = Color(0xFF1D0052),
    background = ObsidianBg,
    onBackground = CrispWhite,
    surface = ObsidianSurface,
    onSurface = CrispWhite,
    surfaceVariant = ObsidianSurfaceVariant,
    onSurfaceVariant = SlateSecondary,
    surfaceContainerHigh = ObsidianContainerHigh,
    outline = HairlineDark,
    error = ErrorCrimson,
    onError = Color(0xFF37000B)
)

val ChronoLightColorScheme = lightColorScheme(
    primary = DeepTeal,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFB3F0FA),
    onPrimaryContainer = Color(0xFF001F25),
    secondary = WarmBronze,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFFFDFB5),
    onSecondaryContainer = Color(0xFF2E1700),
    tertiary = DeepViolet,
    onTertiary = Color.White,
    background = AlabasterBg,
    onBackground = InkBlack,
    surface = AlabasterSurface,
    onSurface = InkBlack,
    surfaceVariant = AlabasterSurfaceVariant,
    onSurfaceVariant = InkMuted,
    outline = HairlineLight,
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

val ChronoHighContrastColorScheme = darkColorScheme(
    primary = HighContrastCyan,
    onPrimary = HighContrastBlack,
    primaryContainer = HighContrastCyan,
    onPrimaryContainer = HighContrastBlack,
    secondary = HighContrastYellow,
    onSecondary = HighContrastBlack,
    secondaryContainer = HighContrastYellow,
    onSecondaryContainer = HighContrastBlack,
    tertiary = HighContrastWhite,
    onTertiary = HighContrastBlack,
    background = HighContrastBlack,
    onBackground = HighContrastWhite,
    surface = HighContrastSurface,
    onSurface = HighContrastWhite,
    surfaceVariant = HighContrastSurfaceVariant,
    onSurfaceVariant = HighContrastWhite,
    surfaceContainerHigh = HighContrastSurfaceVariant,
    outline = HighContrastBorder,
    error = Color(0xFFFF6E6E),
    onError = HighContrastBlack
)
