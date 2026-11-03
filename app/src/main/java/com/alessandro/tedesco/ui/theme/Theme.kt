package com.alessandro.tedesco.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
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
        primary = Color(0xFF006A6A),
        onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFF6FF7F7),
        onPrimaryContainer = Color(0xFF002020),
        secondary = Color(0xFF4A6363),
        onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFCCE8E8),
        onSecondaryContainer = Color(0xFF051F1F),
        tertiary = Color(0xFFB07A00),
        onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFFFDFA0),
        onTertiaryContainer = Color(0xFF261900),
        error = Color(0xFFBA1A1A),
        onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6),
        onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF4FBFB),
        onBackground = Color(0xFF191C1C),
        surface = Color(0xFFF4FBFB),
        onSurface = Color(0xFF191C1C),
        surfaceVariant = Color(0xFFDAE4E4),
        onSurfaceVariant = Color(0xFF3F4949),
        outline = Color(0xFF6F7979)
    )
    val brandDark = darkColorScheme(
        primary = Color(0xFF4DDADA),
        onPrimary = Color(0xFF003737),
        primaryContainer = Color(0xFF004F4F),
        onPrimaryContainer = Color(0xFF6FF7F7),
        secondary = Color(0xFFB0CCCC),
        onSecondary = Color(0xFF1B3535),
        secondaryContainer = Color(0xFF324B4B),
        onSecondaryContainer = Color(0xFFCCE8E8),
        tertiary = Color(0xFFFFB940),
        onTertiary = Color(0xFF3F2E00),
        tertiaryContainer = Color(0xFF5B4300),
        onTertiaryContainer = Color(0xFFFFDFA0),
        error = Color(0xFFFFB4AB),
        onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A),
        onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0E1414),
        onBackground = Color(0xFFE0E3E3),
        surface = Color(0xFF0E1414),
        onSurface = Color(0xFFE0E3E3),
        surfaceVariant = Color(0xFF3F4949),
        onSurfaceVariant = Color(0xFFBEC8C8),
        outline = Color(0xFF899393)
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
