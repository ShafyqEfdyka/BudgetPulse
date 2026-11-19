package com.example.monethra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/**
 * BudgetPulse Monochrome Palette (formerly MonethraPastelPalette).
 *
 * Provides smooth monochrome gradients and subtle container fills
 * blending Ivory White (#F4F0E0) and tonal shades in Light Mode,
 * and Smoky Black (#100C08) and tonal shades in Dark Mode.
 */
object BudgetPulsePastelPalette {
    val SmokyBlack = Color(0xFF100C08)
    val IvoryWhite = Color(0xFFF4F0E0)
    val WarmCream = Color(0xFFEBE5CE)
    val SoftSand = Color(0xFFE2DAC0)
    val ElevatedSurface = Color(0xFFFAF8F0)
    val MutedCharcoal = Color(0xFF4A453E)

    // Remapped brand tokens
    val PastelPeriwinkle = SmokyBlack
    val SoftLavenderTint = WarmCream
    val PastelMintGreen = IvoryWhite
    val PastelBlushPink = SoftSand
    val PastelButterCream = ElevatedSurface
    val PastelSkyBlue = WarmCream
    val SoftLilacMist = SoftSand
    val WarmAlabasterPastel = IvoryWhite

    val allColors: List<Color> = listOf(
        IvoryWhite,
        WarmCream,
        SoftSand,
        ElevatedSurface,
    )

    fun getMonochromeLinearGradient(darkTheme: Boolean = false): Brush {
        val colors = if (darkTheme) {
            listOf(
                Color(0xFF100C08),
                Color(0xFF18130E),
                Color(0xFF211A13),
            )
        } else {
            listOf(
                Color(0xFFF4F0E0),
                Color(0xFFEDE6CE),
                Color(0xFFE4DBC0),
            )
        }
        return Brush.linearGradient(
            colors = colors,
            start = Offset(0f, 0f),
            end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY),
        )
    }

    fun getRandomThreeColorGradient(darkTheme: Boolean = false): Brush = getMonochromeLinearGradient(darkTheme)
}

typealias BudgetPulseMonochromePalette = BudgetPulsePastelPalette
typealias MonethraPastelPalette = BudgetPulsePastelPalette

@Composable
fun rememberMonochromeBackgroundGradient(
    darkTheme: Boolean = MaterialTheme.colorScheme.background == Color(0xFF100C08),
): Brush {
    return remember(darkTheme) {
        BudgetPulsePastelPalette.getMonochromeLinearGradient(darkTheme)
    }
}
