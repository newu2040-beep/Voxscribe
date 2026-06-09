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

private val DarkColorScheme =
    darkColorScheme(
        primary = GlassSecondary,         // Bright sky blue
        secondary = GlassSecondary,       // Light blue accent
        tertiary = GlassTertiary,
        background = GlassDarkBackground,
        surface = GlassDarkCardBg,
        onPrimary = Color(0xFF070B14),
        onSecondary = Color(0xFF070B14),
        onBackground = GlassDarkTextPrimary,
        onSurface = GlassDarkTextPrimary
    )

private val LightColorScheme =
    lightColorScheme(
        primary = GlassPrimaryBuy,        // Deep royal cobalt blue
        secondary = GlassSecondary,       // Cool light blue
        tertiary = GlassTertiary,
        background = GlassLightBackground,
        surface = GlassLightCardBg,
        onPrimary = Color.White,
        onSecondary = Color.White,
        onBackground = GlassLightTextPrimary,
        onSurface = GlassLightTextPrimary
    )

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit,
) {
    val colorScheme =
        when {
            dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                val context = LocalContext.current
                if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
            }

            darkTheme -> DarkColorScheme
            else -> LightColorScheme
        }

    MaterialTheme(colorScheme = colorScheme, typography = Typography, content = content)
}
