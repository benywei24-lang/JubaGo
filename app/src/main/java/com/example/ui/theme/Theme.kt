package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = JubaGreenLight,
    onPrimary = JubaGreenDark,
    primaryContainer = JubaGreenPrimary,
    onPrimaryContainer = Color.White,
    secondary = JubaGoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = JubaGoldDark,
    onSecondaryContainer = Color.White,
    tertiary = NileBlueTertiary,
    background = DarkBackground,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = Color(0xFFE2E8E4),
    onSurface = Color(0xFFE2E8E4)
)

private val LightColorScheme = lightColorScheme(
    primary = JubaGreenPrimary,
    onPrimary = Color.White,
    primaryContainer = JubaGreenContainer,
    onPrimaryContainer = JubaGreenDark,
    secondary = JubaGoldSecondary,
    onSecondary = Color.Black,
    secondaryContainer = JubaGoldContainer,
    onSecondaryContainer = JubaGoldDark,
    tertiary = NileBlueTertiary,
    onTertiary = Color.White,
    tertiaryContainer = NileBlueContainer,
    background = LightBackground,
    surface = LightSurface,
    surfaceVariant = LightSurfaceVariant,
    onBackground = Color(0xFF131A16),
    onSurface = Color(0xFF131A16),
    outline = LightOutline
)

@Composable
fun JubaGoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Keep brand South Sudan emerald & gold
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
