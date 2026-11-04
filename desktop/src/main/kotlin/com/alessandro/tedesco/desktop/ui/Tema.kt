package com.alessandro.tedesco.desktop.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val Blu = Color(0xFF2F5BEA)
private val BluChiaro = Color(0xFFDCE4FF)
private val Verde = Color(0xFF1F7A5C)
private val Ambra = Color(0xFF9A6400)

private val Scuro = darkColorScheme(
    primary = Color(0xFFAEC6FF),
    onPrimary = Color(0xFF002E6B),
    primaryContainer = Color(0xFF1B3E7A),
    onPrimaryContainer = Color(0xFFDCE4FF),
    secondary = Color(0xFF9FCBF5),
    onSecondary = Color(0xFF003354),
    secondaryContainer = Color(0xFF17476B),
    onSecondaryContainer = Color(0xFFD3E8FF),
    tertiary = Color(0xFF8FD6B8),
    onTertiary = Color(0xFF003828),
    tertiaryContainer = Color(0xFF1B4F3C),
    onTertiaryContainer = Color(0xFFA9F2D0),
    background = Color(0xFF111318),
    onBackground = Color(0xFFE3E2E6),
    surface = Color(0xFF14161B),
    onSurface = Color(0xFFE3E2E6),
    surfaceVariant = Color(0xFF44474E),
    onSurfaceVariant = Color(0xFFC4C6D0),
    surfaceContainer = Color(0xFF1C1F25),
    surfaceContainerHigh = Color(0xFF262A31),
    outline = Color(0xFF8E9099),
    outlineVariant = Color(0xFF44474E),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005)
)

private val Chiaro = lightColorScheme(
    primary = Blu,
    onPrimary = Color.White,
    primaryContainer = BluChiaro,
    onPrimaryContainer = Color(0xFF00174B),
    secondary = Color(0xFF2C5F8A),
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD3E8FF),
    onSecondaryContainer = Color(0xFF001E31),
    tertiary = Verde,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFA9F2D0),
    onTertiaryContainer = Color(0xFF002116),
    background = Color(0xFFF8F9FC),
    onBackground = Color(0xFF1A1C20),
    surface = Color(0xFFFDFDFF),
    onSurface = Color(0xFF1A1C20),
    surfaceVariant = Color(0xFFE1E2EC),
    onSurfaceVariant = Color(0xFF44474E),
    surfaceContainer = Color(0xFFF0F1F6),
    surfaceContainerHigh = Color(0xFFE7E8EF),
    outline = Color(0xFF74777F),
    outlineVariant = Color(0xFFC4C6D0),
    error = Color(0xFFBA1A1A),
    onError = Color.White
)

private val Tipografia = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        labelSmall = base.labelSmall.copy(letterSpacing = 0.4.sp)
    )
}

@Composable
fun TemaTedesco(scuro: Boolean, contenuto: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (scuro) Scuro else Chiaro,
        typography = Tipografia,
        content = contenuto
    )
}

val AmbraAvviso = Ambra
