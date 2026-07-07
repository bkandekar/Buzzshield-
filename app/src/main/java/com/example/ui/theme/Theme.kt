package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val BuzzShieldColorScheme = darkColorScheme(
    primary = ElectricLime,
    onPrimary = DeepTeal,
    secondary = WhatsAppGreen,
    onSecondary = Color.White,
    background = DeepTeal,
    onBackground = TextWhite,
    surface = PrimarySurface,
    onSurface = TextWhite,
    surfaceVariant = DarkGreyBg,
    onSurfaceVariant = TextMuted,
    error = Color(0xFFFF5252),
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force the Brand Dark theme
    dynamicColor: Boolean = false, // Force consistent branding
    content: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = BuzzShieldColorScheme,
        typography = Typography,
        content = content
    )
}
