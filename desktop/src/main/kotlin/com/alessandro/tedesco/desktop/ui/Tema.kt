package com.alessandro.tedesco.desktop.ui

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.Shapes
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.dp

private val Scuro = darkColorScheme(
    primary = Color(0xFF8FD6CD),
    onPrimary = Color(0xFF003733),
    primaryContainer = Color(0xFF17433F),
    onPrimaryContainer = Color(0xFFB8F0E8),
    secondary = Color(0xFFB4C9C2),
    onSecondary = Color(0xFF1F3530),
    secondaryContainer = Color(0xFF334B45),
    onSecondaryContainer = Color(0xFFD0E8E0),
    tertiary = Color(0xFFE8C08A),
    onTertiary = Color(0xFF4A2F0C),
    tertiaryContainer = Color(0xFF5B3E1C),
    onTertiaryContainer = Color(0xFFFBE3C4),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF0D1413),
    onBackground = Color(0xFFDEE5E1),
    surface = Color(0xFF111817),
    onSurface = Color(0xFFDEE5E1),
    surfaceVariant = Color(0xFF3F4946),
    onSurfaceVariant = Color(0xFFBFC9C4),
    surfaceContainerLowest = Color(0xFF080E0D),
    surfaceContainerLow = Color(0xFF131B19),
    surfaceContainer = Color(0xFF161E1C),
    surfaceContainerHigh = Color(0xFF1F2825),
    surfaceContainerHighest = Color(0xFF293330),
    outline = Color(0xFF89948F),
    outlineVariant = Color(0xFF3F4946)
)

private val Chiaro = lightColorScheme(
    primary = Color(0xFF1F5F5B),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFD6EFEA),
    onPrimaryContainer = Color(0xFF0A2522),
    secondary = Color(0xFF5A6B66),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFE2EBE7),
    onSecondaryContainer = Color(0xFF15201D),
    tertiary = Color(0xFF8A5A22),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFF6E2C8),
    onTertiaryContainer = Color(0xFF2B1A05),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF7FAF8),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFFBFDFB),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFE1E8E4),
    onSurfaceVariant = Color(0xFF44504C),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF1F6F3),
    surfaceContainer = Color(0xFFEDF3F0),
    surfaceContainerHigh = Color(0xFFE7EEEA),
    surfaceContainerHighest = Color(0xFFE1E8E4),
    outline = Color(0xFF70807A),
    outlineVariant = Color(0xFFC9D4CF)
)

private val Tipografia = Typography().let { base ->
    base.copy(
        headlineMedium = base.headlineMedium.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.4).sp),
        headlineSmall = base.headlineSmall.copy(fontWeight = FontWeight.SemiBold, letterSpacing = (-0.2).sp),
        titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
        titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
        bodyLarge = base.bodyLarge.copy(lineHeight = 24.sp),
        labelSmall = base.labelSmall.copy(letterSpacing = 0.3.sp)
    )
}

private val Forme = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(30.dp)
)

@Composable
fun TemaTedesco(scuro: Boolean, contenuto: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (scuro) Scuro else Chiaro,
        typography = Tipografia,
        shapes = Forme,
        content = contenuto
    )
}