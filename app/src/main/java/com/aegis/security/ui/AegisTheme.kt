package com.aegis.security.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val CyberBackground = Color(0xFF090D16)
val CyberSurface = Color(0xFF111827)
val CyberSurfaceVariant = Color(0xFF1E293B)
val CyberCard = Color(0xFF161F33)

val CyberPrimary = Color(0xFF00E5FF)
val CyberSecondary = Color(0xFF00E676)
val CyberAccent = Color(0xFF7C4DFF)

val CyberError = Color(0xFFFF5252)
val CyberWarning = Color(0xFFFFB74D)
val CyberSafe = Color(0xFF00E676)

val CyberTextPrimary = Color(0xFFF8FAFC)
val CyberTextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = CyberPrimary,
    onPrimary = Color(0xFF00363D),
    primaryContainer = Color(0xFF004F58),
    secondary = CyberSecondary,
    onSecondary = Color(0xFF003915),
    background = CyberBackground,
    surface = CyberSurface,
    surfaceVariant = CyberSurfaceVariant,
    onBackground = CyberTextPrimary,
    onSurface = CyberTextPrimary,
    onSurfaceVariant = CyberTextSecondary,
    error = CyberError
)

@Composable
fun AegisSecurityTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
