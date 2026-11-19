package com.example.monethra.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val LightMonochromeColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF100C08),
    onPrimary = Color(0xFFF4F0E0),
    primaryContainer = WarmCream,
    onPrimaryContainer = SmokyBlack,
    secondary = MutedCharcoal,
    onSecondary = IvoryWhite,
    secondaryContainer = WarmCream,
    onSecondaryContainer = SmokyBlack,
    tertiary = SmokyBlack,
    onTertiary = IvoryWhite,
    tertiaryContainer = SoftSand,
    onTertiaryContainer = SmokyBlack,
    background = Color(0xFFF4F0E0),
    onBackground = Color(0xFF100C08),
    surface = Color(0xFFFAF8F0),
    onSurface = Color(0xFF100C08),
    surfaceVariant = Color(0xFFEBE5CE),
    onSurfaceVariant = Color(0xFF3A342C),
    inverseSurface = SmokyBlack,
    inverseOnSurface = IvoryWhite,
    inversePrimary = IvoryWhite,
    error = Color(0xFFB3261E),
    onError = Color(0xFFFFFFFF),
    outline = Color(0xFF100C08).copy(alpha = 0.18f),
    outlineVariant = Color(0xFF100C08).copy(alpha = 0.08f),
)

val DarkMonochromeColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFFF4F0E0),
    onPrimary = Color(0xFF100C08),
    primaryContainer = Color(0xFF262019),
    onPrimaryContainer = Color(0xFFF4F0E0),
    secondary = Color(0xFFD5CEB8),
    onSecondary = Color(0xFF100C08),
    secondaryContainer = Color(0xFF262019),
    onSecondaryContainer = Color(0xFFF4F0E0),
    tertiary = Color(0xFFF4F0E0),
    onTertiary = Color(0xFF100C08),
    tertiaryContainer = Color(0xFF262019),
    onTertiaryContainer = Color(0xFFF4F0E0),
    background = Color(0xFF100C08),
    onBackground = Color(0xFFF4F0E0),
    surface = Color(0xFF1A1510),
    onSurface = Color(0xFFF4F0E0),
    surfaceVariant = Color(0xFF262019),
    onSurfaceVariant = Color(0xFFD5CEB8),
    inverseSurface = Color(0xFFFAF8F0),
    inverseOnSurface = Color(0xFF100C08),
    inversePrimary = Color(0xFF100C08),
    error = Color(0xFFFF8A80),
    onError = Color(0xFF100C08),
    outline = Color(0xFFF4F0E0).copy(alpha = 0.20f),
    outlineVariant = Color(0xFFF4F0E0).copy(alpha = 0.10f),
)

val LightColorScheme = LightMonochromeColorScheme
val DarkColorScheme = DarkMonochromeColorScheme

@Composable
fun BudgetPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkMonochromeColorScheme else LightMonochromeColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        shapes = Shapes,
        typography = Typography,
        content = content,
    )
}

@Composable
fun MonethraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    BudgetPulseTheme(darkTheme = darkTheme, content = content)
}
