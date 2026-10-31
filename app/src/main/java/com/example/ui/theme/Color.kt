package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Atelier Library Dark & Light Palette
val EspressoDark = Color(0xFF140E0B)
val EspressoSurface = Color(0xFF1F1612)
val EspressoCard = Color(0xFF2B1F1A)
val EspressoBorder = Color(0xFF3E2E26)

val WarmParchmentBg = Color(0xFFFBF6EE)
val WarmParchmentSurface = Color(0xFFF3EAD8)
val WarmParchmentCard = Color(0xFFEDE0C8)
val WarmInkPrimary = Color(0xFF231812)
val WarmInkSecondary = Color(0xFF5C473B)

// Accents & Bookbindery Ribbon Colors
val CrimsonRibbon = Color(0xFFD94E34)
val AntiqueBrass = Color(0xFFD49B35)
val EmeraldFolio = Color(0xFF2E7D5B)
val Cobaltplate = Color(0xFF3A6EA5)
val IvoryGold = Color(0xFFF5E6C8)
val MutedSand = Color(0xFFB8A390)

// Reader Paper Stock Themes
enum class PaperTheme(
    val displayName: String,
    val pageBackground: Color,
    val pageBackside: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accentColor: Color,
    val spineShadowColor: Color,
    val tableBackground: Color,
    val isDark: Boolean
) {
    PARCHMENT(
        displayName = "Florentine Parchment",
        pageBackground = Color(0xFFF7EFE1),
        pageBackside = Color(0xFFEADBC3),
        primaryText = Color(0xFF241913),
        secondaryText = Color(0xFF655042),
        accentColor = Color(0xFFB8432C),
        spineShadowColor = Color(0xFF1A0F0A),
        tableBackground = Color(0xFF1B130F),
        isDark = false
    ),
    IVORY(
        displayName = "Ivory Cotton",
        pageBackground = Color(0xFFFCFBF8),
        pageBackside = Color(0xFFEFEDE6),
        primaryText = Color(0xFF18181B),
        secondaryText = Color(0xFF52525B),
        accentColor = Color(0xFFC84B31),
        spineShadowColor = Color(0xFF09090B),
        tableBackground = Color(0xFF202024),
        isDark = false
    ),
    SAGE(
        displayName = "Botanical Sage",
        pageBackground = Color(0xFFEEF3EC),
        pageBackside = Color(0xFFDCE6D9),
        primaryText = Color(0xFF18241B),
        secondaryText = Color(0xFF475C4C),
        accentColor = Color(0xFF2E7D5B),
        spineShadowColor = Color(0xFF0E1710),
        tableBackground = Color(0xFF131C15),
        isDark = false
    ),
    MIDNIGHT(
        displayName = "Midnight Velvet",
        pageBackground = Color(0xFF1E1916),
        pageBackside = Color(0xFF2A221E),
        primaryText = Color(0xFFEDE2D3),
        secondaryText = Color(0xFFA69584),
        accentColor = Color(0xFFE27055),
        spineShadowColor = Color(0xFF000000),
        tableBackground = Color(0xFF0E0B09),
        isDark = true
    )
}
