package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val AtelierDarkColorScheme = darkColorScheme(
    primary = AntiqueBrass,
    onPrimary = EspressoDark,
    primaryContainer = Color(0xFF42301B),
    onPrimaryContainer = IvoryGold,
    secondary = CrimsonRibbon,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFF4B2018),
    onSecondaryContainer = Color(0xFFFFDAD3),
    tertiary = EmeraldFolio,
    onTertiary = Color.White,
    background = EspressoDark,
    onBackground = IvoryGold,
    surface = EspressoSurface,
    onSurface = IvoryGold,
    surfaceVariant = EspressoCard,
    onSurfaceVariant = MutedSand,
    outline = EspressoBorder
)

private val AtelierLightColorScheme = lightColorScheme(
    primary = Color(0xFF8C321E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFF6DEC2),
    onPrimaryContainer = WarmInkPrimary,
    secondary = AntiqueBrass,
    onSecondary = EspressoDark,
    secondaryContainer = Color(0xFFF1E2C5),
    onSecondaryContainer = WarmInkPrimary,
    tertiary = EmeraldFolio,
    onTertiary = Color.White,
    background = WarmParchmentBg,
    onBackground = WarmInkPrimary,
    surface = WarmParchmentSurface,
    onSurface = WarmInkPrimary,
    surfaceVariant = WarmParchmentCard,
    onSurfaceVariant = WarmInkSecondary,
    outline = Color(0xFFD2C1A8)
)

@Composable
fun Folio3DTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) AtelierDarkColorScheme else AtelierLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
