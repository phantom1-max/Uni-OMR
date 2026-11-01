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
    primary = Color(0xFF60A5FA),
    onPrimary = Color(0xFF0F172A),
    primaryContainer = Color(0xFF1D4ED8),
    onPrimaryContainer = Color(0xFFDBEAFE),
    secondary = Color(0xFF3B82F6),
    onSecondary = Color.White,
    tertiary = CorrectGreen,
    error = Color(0xFFFF5252),
    background = SlateDarkBg,
    surface = SlateDarkCard,
    onBackground = TextLight,
    onSurface = TextLight
)

private val LightColorScheme = lightColorScheme(
    primary = IndigoPrimary, // #2563EB - Lighter vibrant royal blue
    onPrimary = Color.White,
    primaryContainer = IndigoLight,
    onPrimaryContainer = IndigoPrimary,
    secondary = IndigoSecondary,
    onSecondary = Color.White,
    tertiary = CorrectGreen,
    error = ErrorRed,
    background = SlateLightBg,
    surface = SlateCardBg,
    onBackground = TextPrimary,
    onSurface = TextPrimary
)

@Composable
fun UniOmrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
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

// Backwards compatibility alias
@Composable
fun OmniOmrTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = UniOmrTheme(darkTheme, dynamicColor, content)
