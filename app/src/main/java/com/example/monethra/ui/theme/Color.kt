package com.example.monethra.ui.theme

import androidx.compose.ui.graphics.Color

// High-Contrast Monochrome Palette
val SmokyBlack = Color(0xFF100C08)          // Primary Ink / Accent
val IvoryWhite = Color(0xFFF4F0E0)          // Primary Surface / Canvas
val ElevatedCardSurface = Color(0xFFFAF8F0) // Elevated Card Surface Light
val ElevatedCardSurfaceDark = Color(0xFF1A1510) // Elevated Card Surface Dark
val ElevatedCardSurfaceWhite = Color(0xFFFFFFFF)
val MutedCharcoal = Color(0xFF4A453E)       // Expense indicator / secondary contrast
val WarmCream = Color(0xFFEBE5CE)           // Subtle container fill step 1
val SoftSand = Color(0xFFE2DAC0)            // Subtle container fill step 2
val IvoryTonalLight = Color(0xFFE5DEC4)

// Dark Mode Tokens
val DarkSurfaceVariant = Color(0xFF262019)
val DarkOnSurfaceVariant = Color(0xFFD5CEB8)
val DarkSmokyShade = Color(0xFF1E1812)

val BorderMuted = Color(0xFF100C08).copy(alpha = 0.14f)
val SecondaryMuted = Color(0xFF100C08).copy(alpha = 0.65f)

// Core typography and UI tokens
val PrimaryText = SmokyBlack
val SecondaryText = SecondaryMuted
val WarmIvory = IvoryWhite
val White = Color(0xFFFFFFFF)

// Monochrome indicators:
// Income: solid #100C08 fill/badge
val IncomeGreen = SmokyBlack
val IncomeGreenLight = WarmCream

// Expense: muted charcoal #4A453E / outline
val ExpenseRed = MutedCharcoal
val ExpenseRedLight = SoftSand

// Brand and button accents remapped to Smoky Black
val PastelPeriwinkle = SmokyBlack
val DeepIndigo = SmokyBlack

// Legacy palette aliases mapped to monochrome values to ensure full compatibility
val PowderBlue = ElevatedCardSurface
val SageGreen = WarmCream
val DustyPink = SoftSand
val ButterCream = WarmCream
val SoftLavender = WarmCream

/**
 * Resolves a contrasting icon tint for category badge circular containers.
 * Light Ivory / monochrome light backgrounds (#F4F0E0, #FAF8F0, #EBE5CE, #E2DAC0, etc.)
 * use Smoky Black (#100C08), while colored category backgrounds (amber, blue, purple, etc.)
 * use White (#FFFFFF).
 */
fun getCategoryBadgeIconTint(backgroundColor: Color): Color {
    val isLightIvory = backgroundColor == IvoryWhite ||
        backgroundColor == WarmIvory ||
        backgroundColor == ElevatedCardSurface ||
        backgroundColor == WarmCream ||
        backgroundColor == SoftSand ||
        backgroundColor == Color(0xFFF4F0E0) ||
        backgroundColor == Color(0xFFFAF8F0) ||
        backgroundColor == Color(0xFFEBE5CE) ||
        backgroundColor == Color(0xFFE2DAC0) ||
        backgroundColor == Color(0xFFFFFFFF)

    return if (isLightIvory) {
        SmokyBlack
    } else {
        Color.White
    }
}
