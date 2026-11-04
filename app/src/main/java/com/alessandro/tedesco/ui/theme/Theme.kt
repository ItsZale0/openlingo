package com.alessandro.tedesco.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// --- Tema Material 3 standard ---
// Nessuna palette custom: si usano gli schemi ufficiali Material 3.
// Android 12+ -> dynamic color (Material You): l'app prende i colori dal sistema.
// Altrimenti -> schema baseline Material 3 (darkColorScheme/lightColorScheme).

@Composable
fun TedescoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val brandLight = lightColorScheme(
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
    val brandDark = darkColorScheme(
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

    val tipografia = Typography().let { base ->
        base.copy(
            headlineMedium = base.headlineMedium.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.4).sp
            ),
            headlineSmall = base.headlineSmall.copy(
                fontWeight = FontWeight.SemiBold,
                letterSpacing = (-0.2).sp
            ),
            titleLarge = base.titleLarge.copy(fontWeight = FontWeight.SemiBold),
            titleMedium = base.titleMedium.copy(fontWeight = FontWeight.SemiBold),
            bodyLarge = base.bodyLarge.copy(lineHeight = 24.sp),
            labelSmall = base.labelSmall.copy(letterSpacing = 0.3.sp)
        )
    }

    val forme = Shapes(
        extraSmall = RoundedCornerShape(8.dp),
        small = RoundedCornerShape(12.dp),
        medium = RoundedCornerShape(18.dp),
        large = RoundedCornerShape(24.dp),
        extraLarge = RoundedCornerShape(30.dp)
    )

    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> brandDark
        else -> brandLight
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = tipografia,
        shapes = forme,
        content = content
    )
}

// Spaziature default (Uber) — retrocompatibilità
object Spaziature {
    val xs = 4.dp
    val sm = 8.dp
    val md = 20.dp
    val lg = 24.dp
    val xl = 24.dp
    val xxl = 32.dp
}

// Raggi default (Uber) — retrocompatibilità
object Raggi {
    val card = 20.dp
    val chip = 16.dp
    val bottone = 20.dp
}


// Breakpoints
object Breakpoints {
    const val COMPACT = 0
    const val MEDIUM = 600
    const val EXPANDED = 840
    const val LARGE = 1200
}

// Classi dimensione finestra
enum class WindowSize {
    COMPACT,
    MEDIUM,
    EXPANDED
}

// Larghezza minima finestra
private const val LARGHEZZA_MINIMA_DP = 320

@Composable
fun rememberWindowSizeClass(): WindowSize {
    val configuration = androidx.compose.ui.platform.LocalConfiguration.current
    val widthDp = configuration.screenWidthDp.coerceAtLeast(LARGHEZZA_MINIMA_DP)
    return androidx.compose.runtime.remember(widthDp) {
        when {
            widthDp < 600 -> WindowSize.COMPACT
            widthDp < 840 -> WindowSize.MEDIUM
            else -> WindowSize.EXPANDED
        }
    }
}

@Composable
fun dimensioneContenuto(): androidx.compose.ui.unit.Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> androidx.compose.ui.unit.Dp.Unspecified
        WindowSize.MEDIUM -> 600.dp
        WindowSize.EXPANDED -> 720.dp
    }
}

@Composable
fun spaziaturaSchermo(): androidx.compose.ui.unit.Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
}

@Composable
fun responsiveVerticalSpacing(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16
        WindowSize.MEDIUM -> 20
        WindowSize.EXPANDED -> 24
    }
}

@Composable
fun responsiveGridColumns(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 1
        WindowSize.MEDIUM -> 2
        WindowSize.EXPANDED -> 3
    }
}

@Composable
fun responsiveToolbarHeight(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 56
        WindowSize.MEDIUM -> 64
        WindowSize.EXPANDED -> 72
    }
}

@Composable
fun responsiveButtonHeight(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 48
        WindowSize.MEDIUM -> 52
        WindowSize.EXPANDED -> 56
    }
}

@Composable
fun responsiveCornerRadius(): Int {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 8
        WindowSize.MEDIUM -> 12
        WindowSize.EXPANDED -> 16
    }
}

@Composable
fun responsiveTypography(): Typography = MaterialTheme.typography

@Composable
fun responsiveColumnArrangement(): androidx.compose.foundation.layout.Arrangement.Vertical {
    return androidx.compose.foundation.layout.Arrangement.spacedBy(responsiveVerticalSpacing().dp)
}

@Composable
fun responsiveRowArrangement(): androidx.compose.foundation.layout.Arrangement.Horizontal {
    val spacing = when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 8
        WindowSize.MEDIUM -> 12
        WindowSize.EXPANDED -> 16
    }
    return androidx.compose.foundation.layout.Arrangement.spacedBy(spacing.dp)
}

@Composable
fun ResponsiveColumn(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit
) {
    val arrangement = responsiveColumnArrangement()
    val horizontalPadding = when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 16.dp
        WindowSize.MEDIUM -> 24.dp
        WindowSize.EXPANDED -> 32.dp
    }
    androidx.compose.foundation.layout.Column(
        modifier = modifier
            .padding(horizontal = horizontalPadding)
            .fillMaxSize(),
        verticalArrangement = arrangement,
        horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
        content = content
    )
}

@Composable
fun ResponsiveRow(
    modifier: Modifier = Modifier,
    content: @Composable androidx.compose.foundation.layout.RowScope.() -> Unit
) {
    val arrangement = responsiveRowArrangement()
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = arrangement,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
        content = content
    )
}

@Composable
fun AltezzaBottonePrincipale(): androidx.compose.ui.unit.Dp {
    return when (rememberWindowSizeClass()) {
        WindowSize.COMPACT -> 56.dp
        else -> 60.dp
    }
}

@Composable
fun TitoloSchermata(testo: String) {
    androidx.compose.material3.Text(
        text = testo,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold
    )
}
