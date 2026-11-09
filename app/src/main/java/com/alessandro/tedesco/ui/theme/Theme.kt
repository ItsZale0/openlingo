package com.alessandro.tedesco.ui.theme

import android.os.Build
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
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
    temaColore: String = "teal",
    content: @Composable () -> Unit
) {
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
        darkTheme -> schemaScuro(temaColore)
        else -> schemaChiaro(temaColore)
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = tipografia,
        shapes = forme,
        content = content
    )
}

private fun schemaScuro(id: String): ColorScheme = when (id) {
    "navy" -> darkColorScheme(
        primary = Color(0xFFB4C5FF), onPrimary = Color(0xFF002A78),
        primaryContainer = Color(0xFF003F9F), onPrimaryContainer = Color(0xFFDCE4FF),
        secondary = Color(0xFFBEC6DC), onSecondary = Color(0xFF283041),
        secondaryContainer = Color(0xFF3E4659), onSecondaryContainer = Color(0xFFDAE2F9),
        tertiary = Color(0xFFFFB95C), onTertiary = Color(0xFF452B00),
        tertiaryContainer = Color(0xFF624000), onTertiaryContainer = Color(0xFFFFDDB3),
        background = Color(0xFF0D1220), onBackground = Color(0xFFE1E2EC),
        surface = Color(0xFF11162A), onSurface = Color(0xFFE1E2EC),
        surfaceVariant = Color(0xFF44464F), onSurfaceVariant = Color(0xFFC4C6D0),
        surfaceContainerLowest = Color(0xFF080C18), surfaceContainerLow = Color(0xFF13182A),
        surfaceContainer = Color(0xFF161C30), surfaceContainerHigh = Color(0xFF1F2538),
        surfaceContainerHighest = Color(0xFF293044), outline = Color(0xFF8E9099),
        outlineVariant = Color(0xFF44464F), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
    )
    "indigo" -> darkColorScheme(
        primary = Color(0xFFC4B5FD), onPrimary = Color(0xFF2E1065),
        primaryContainer = Color(0xFF4C1D95), onPrimaryContainer = Color(0xFFEDE9FE),
        secondary = Color(0xFFCBC3E8), onSecondary = Color(0xFF302B46),
        secondaryContainer = Color(0xFF474060), onSecondaryContainer = Color(0xFFE7E0FF),
        tertiary = Color(0xFFF9A8D4), onTertiary = Color(0xFF500724),
        tertiaryContainer = Color(0xFF701A3E), onTertiaryContainer = Color(0xFFFCE7F3),
        background = Color(0xFF0F0D1A), onBackground = Color(0xFFE6E1F4),
        surface = Color(0xFF13111F), onSurface = Color(0xFFE6E1F4),
        surfaceVariant = Color(0xFF49454F), onSurfaceVariant = Color(0xFFCBC4CF),
        surfaceContainerLowest = Color(0xFF0A0814), surfaceContainerLow = Color(0xFF151322),
        surfaceContainer = Color(0xFF181626), surfaceContainerHigh = Color(0xFF221F33),
        surfaceContainerHighest = Color(0xFF2C293F), outline = Color(0xFF948F99),
        outlineVariant = Color(0xFF49454F), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
    )
    "forest" -> darkColorScheme(
        primary = Color(0xFF86EFAC), onPrimary = Color(0xFF052E16),
        primaryContainer = Color(0xFF14532D), onPrimaryContainer = Color(0xFFDCFCE7),
        secondary = Color(0xFFB4D4BE), onSecondary = Color(0xFF1F3326),
        secondaryContainer = Color(0xFF344E3C), onSecondaryContainer = Color(0xFFD0E8D6),
        tertiary = Color(0xFFFDE68A), onTertiary = Color(0xFF451A03),
        tertiaryContainer = Color(0xFF6B2D0A), onTertiaryContainer = Color(0xFFFEF3C7),
        background = Color(0xFF0A120D), onBackground = Color(0xFFDEE5E0),
        surface = Color(0xFF0E1611), onSurface = Color(0xFFDEE5E0),
        surfaceVariant = Color(0xFF3F4942), onSurfaceVariant = Color(0xFFBFC9C2),
        surfaceContainerLowest = Color(0xFF060C08), surfaceContainerLow = Color(0xFF101A13),
        surfaceContainer = Color(0xFF131E16), surfaceContainerHigh = Color(0xFF1C281F),
        surfaceContainerHighest = Color(0xFF263329), outline = Color(0xFF89948C),
        outlineVariant = Color(0xFF3F4942), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
    )
    "rose" -> darkColorScheme(
        primary = Color(0xFFFDA4AF), onPrimary = Color(0xFF4C0519),
        primaryContainer = Color(0xFF881337), onPrimaryContainer = Color(0xFFFFE4E6),
        secondary = Color(0xFFE4BCC2), onSecondary = Color(0xFF3B2428),
        secondaryContainer = Color(0xFF53393D), onSecondaryContainer = Color(0xFFF5DCE0),
        tertiary = Color(0xFFFCD34D), onTertiary = Color(0xFF451A03),
        tertiaryContainer = Color(0xFF6B2D0A), onTertiaryContainer = Color(0xFFFEF3C7),
        background = Color(0xFF130A0D), onBackground = Color(0xFFEEE0E3),
        surface = Color(0xFF170E11), onSurface = Color(0xFFEEE0E3),
        surfaceVariant = Color(0xFF524347), onSurfaceVariant = Color(0xFFD7C1C5),
        surfaceContainerLowest = Color(0xFF0E0608), surfaceContainerLow = Color(0xFF1A1013),
        surfaceContainer = Color(0xFF1E1316), surfaceContainerHigh = Color(0xFF281A1E),
        surfaceContainerHighest = Color(0xFF332226), outline = Color(0xFFA08C90),
        outlineVariant = Color(0xFF524347), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
    )
    "amber" -> darkColorScheme(
        primary = Color(0xFFFCD34D), onPrimary = Color(0xFF451A03),
        primaryContainer = Color(0xFF78350F), onPrimaryContainer = Color(0xFFFEF3C7),
        secondary = Color(0xFFD9C9A8), onSecondary = Color(0xFF352D1E),
        secondaryContainer = Color(0xFF4D4330), onSecondaryContainer = Color(0xFFF1E5CC),
        tertiary = Color(0xFF86EFAC), onTertiary = Color(0xFF052E16),
        tertiaryContainer = Color(0xFF14532D), onTertiaryContainer = Color(0xFFDCFCE7),
        background = Color(0xFF12100A), onBackground = Color(0xFFEAE4D8),
        surface = Color(0xFF16140E), onSurface = Color(0xFFEAE4D8),
        surfaceVariant = Color(0xFF4F493D), onSurfaceVariant = Color(0xFFD2C9B8),
        surfaceContainerLowest = Color(0xFF0D0B06), surfaceContainerLow = Color(0xFF19170F),
        surfaceContainer = Color(0xFF1D1A12), surfaceContainerHigh = Color(0xFF27231A),
        surfaceContainerHighest = Color(0xFF322D22), outline = Color(0xFF9B9384),
        outlineVariant = Color(0xFF4F493D), error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6)
    )
    "purple" -> darkColorScheme()
    "pink" -> darkColorScheme(
        primary = Color(0xFFFFB1C8), onPrimary = Color(0xFF5E1133),
        primaryContainer = Color(0xFF7B2949), onPrimaryContainer = Color(0xFFFFD9E2)
    )
    "orange" -> darkColorScheme(
        primary = Color(0xFFFFB77C), onPrimary = Color(0xFF4A2800),
        primaryContainer = Color(0xFF6A3B00), onPrimaryContainer = Color(0xFFFFDCC3)
    )
    "cyan" -> darkColorScheme(
        primary = Color(0xFF81D8E3), onPrimary = Color(0xFF00363D),
        primaryContainer = Color(0xFF004F58), onPrimaryContainer = Color(0xFF9EF0FB)
    )
    "red" -> darkColorScheme(
        primary = Color(0xFFFFB4AB), onPrimary = Color(0xFF690005),
        primaryContainer = Color(0xFF93000A), onPrimaryContainer = Color(0xFFFFDAD6)
    )
    "material" -> darkColorScheme()
    else -> darkColorScheme(
        primary = Color(0xFF8FD6CD), onPrimary = Color(0xFF003733),
        primaryContainer = Color(0xFF17433F), onPrimaryContainer = Color(0xFFB8F0E8),
        secondary = Color(0xFFB4C9C2), onSecondary = Color(0xFF1F3530),
        secondaryContainer = Color(0xFF334B45), onSecondaryContainer = Color(0xFFD0E8E0),
        tertiary = Color(0xFFE8C08A), onTertiary = Color(0xFF4A2F0C),
        tertiaryContainer = Color(0xFF5B3E1C), onTertiaryContainer = Color(0xFFFBE3C4),
        error = Color(0xFFFFB4AB), onError = Color(0xFF690005),
        errorContainer = Color(0xFF93000A), onErrorContainer = Color(0xFFFFDAD6),
        background = Color(0xFF0D1413), onBackground = Color(0xFFDEE5E1),
        surface = Color(0xFF111817), onSurface = Color(0xFFDEE5E1),
        surfaceVariant = Color(0xFF3F4946), onSurfaceVariant = Color(0xFFBFC9C4),
        surfaceContainerLowest = Color(0xFF080E0D), surfaceContainerLow = Color(0xFF131B19),
        surfaceContainer = Color(0xFF161E1C), surfaceContainerHigh = Color(0xFF1F2825),
        surfaceContainerHighest = Color(0xFF293330), outline = Color(0xFF89948F),
        outlineVariant = Color(0xFF3F4946)
    )
}

private fun schemaChiaro(id: String): ColorScheme = when (id) {
    "navy" -> lightColorScheme(
        primary = Color(0xFF003F9F), onPrimary = Color.White,
        primaryContainer = Color(0xFFDCE4FF), onPrimaryContainer = Color(0xFF00174B),
        secondary = Color(0xFF565E71), onSecondary = Color.White,
        secondaryContainer = Color(0xFFDAE2F9), onSecondaryContainer = Color(0xFF131B2C),
        tertiary = Color(0xFF7D5700), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFFDDB3), onTertiaryContainer = Color(0xFF271900),
        background = Color(0xFFF8F9FC), onBackground = Color(0xFF1A1C20),
        surface = Color(0xFFFDFDFF), onSurface = Color(0xFF1A1C20),
        surfaceVariant = Color(0xFFE1E2EC), onSurfaceVariant = Color(0xFF44474E),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF0F1F6),
        surfaceContainer = Color(0xFFEAEAF1), surfaceContainerHigh = Color(0xFFE4E4EC),
        surfaceContainerHighest = Color(0xFFDEDEE6), outline = Color(0xFF74777F),
        outlineVariant = Color(0xFFC4C6D0), error = Color(0xFFBA1A1A), onError = Color.White
    )
    "indigo" -> lightColorScheme(
        primary = Color(0xFF4C1D95), onPrimary = Color.White,
        primaryContainer = Color(0xFFEDE9FE), onPrimaryContainer = Color(0xFF2E1065),
        secondary = Color(0xFF5F5873), onSecondary = Color.White,
        secondaryContainer = Color(0xFFE7E0FF), onSecondaryContainer = Color(0xFF1B1628),
        tertiary = Color(0xFF9F1239), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFCE7F3), onTertiaryContainer = Color(0xFF3A0A1E),
        background = Color(0xFFF9F7FC), onBackground = Color(0xFF1C1A20),
        surface = Color(0xFFFDFBFF), onSurface = Color(0xFF1C1A20),
        surfaceVariant = Color(0xFFE8E3EE), onSurfaceVariant = Color(0xFF49454F),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF1EEF7),
        surfaceContainer = Color(0xFFECE8F3), surfaceContainerHigh = Color(0xFFE6E1EE),
        surfaceContainerHighest = Color(0xFFE0DBE8), outline = Color(0xFF7A757F),
        outlineVariant = Color(0xFFCBC4CF), error = Color(0xFFBA1A1A), onError = Color.White
    )
    "forest" -> lightColorScheme(
        primary = Color(0xFF14532D), onPrimary = Color.White,
        primaryContainer = Color(0xFFDCFCE7), onPrimaryContainer = Color(0xFF052E16),
        secondary = Color(0xFF4B6352), onSecondary = Color.White,
        secondaryContainer = Color(0xFFD0E8D6), onSecondaryContainer = Color(0xFF0F2015),
        tertiary = Color(0xFF92400E), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFEF3C7), onTertiaryContainer = Color(0xFF3B1A02),
        background = Color(0xFFF6FAF7), onBackground = Color(0xFF191C1A),
        surface = Color(0xFFFBFDFA), onSurface = Color(0xFF191C1A),
        surfaceVariant = Color(0xFFDFE5E0), onSurfaceVariant = Color(0xFF444A44),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFEDF3EE),
        surfaceContainer = Color(0xFFE8EEE9), surfaceContainerHigh = Color(0xFFE2E8E3),
        surfaceContainerHighest = Color(0xFFDCE2DD), outline = Color(0xFF757B75),
        outlineVariant = Color(0xFFC3C9C3), error = Color(0xFFBA1A1A), onError = Color.White
    )
    "rose" -> lightColorScheme(
        primary = Color(0xFF881337), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFE4E6), onPrimaryContainer = Color(0xFF4C0519),
        secondary = Color(0xFF6B4F53), onSecondary = Color.White,
        secondaryContainer = Color(0xFFF5DCE0), onSecondaryContainer = Color(0xFF241517),
        tertiary = Color(0xFF92400E), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFFEF3C7), onTertiaryContainer = Color(0xFF3B1A02),
        background = Color(0xFFFBF6F7), onBackground = Color(0xFF1D191A),
        surface = Color(0xFFFDF9FA), onSurface = Color(0xFF1D191A),
        surfaceVariant = Color(0xFFEDDFE2), onSurfaceVariant = Color(0xFF524347),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF4EAED),
        surfaceContainer = Color(0xFFEFE4E7), surfaceContainerHigh = Color(0xFFE9DDE0),
        surfaceContainerHighest = Color(0xFFE3D7DA), outline = Color(0xFF857377),
        outlineVariant = Color(0xFFD7C1C5), error = Color(0xFFBA1A1A), onError = Color.White
    )
    "amber" -> lightColorScheme(
        primary = Color(0xFF78350F), onPrimary = Color.White,
        primaryContainer = Color(0xFFFEF3C7), onPrimaryContainer = Color(0xFF451A03),
        secondary = Color(0xFF655B49), onSecondary = Color.White,
        secondaryContainer = Color(0xFFF1E5CC), onSecondaryContainer = Color(0xFF201B10),
        tertiary = Color(0xFF14532D), onTertiary = Color.White,
        tertiaryContainer = Color(0xFFDCFCE7), onTertiaryContainer = Color(0xFF052E16),
        background = Color(0xFFFBF9F4), onBackground = Color(0xFF1C1A16),
        surface = Color(0xFFFDFBF6), onSurface = Color(0xFF1C1A16),
        surfaceVariant = Color(0xFFECE5D8), onSurfaceVariant = Color(0xFF4F493D),
        surfaceContainerLowest = Color.White, surfaceContainerLow = Color(0xFFF4EFE4),
        surfaceContainer = Color(0xFFEFEAE0), surfaceContainerHigh = Color(0xFFE9E4DA),
        surfaceContainerHighest = Color(0xFFE3DED3), outline = Color(0xFF807869),
        outlineVariant = Color(0xFFD2C9B8), error = Color(0xFFBA1A1A), onError = Color.White
    )
    "purple" -> lightColorScheme()
    "pink" -> lightColorScheme(
        primary = Color(0xFF9C415E), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFD9E2), onPrimaryContainer = Color(0xFF3E001D)
    )
    "orange" -> lightColorScheme(
        primary = Color(0xFF8F4C00), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDCC3), onPrimaryContainer = Color(0xFF2E1500)
    )
    "cyan" -> lightColorScheme(
        primary = Color(0xFF006874), onPrimary = Color.White,
        primaryContainer = Color(0xFF9EF0FB), onPrimaryContainer = Color(0xFF001F24)
    )
    "red" -> lightColorScheme(
        primary = Color(0xFFBA1A1A), onPrimary = Color.White,
        primaryContainer = Color(0xFFFFDAD6), onPrimaryContainer = Color(0xFF410002)
    )
    "material" -> lightColorScheme()
    else -> lightColorScheme(
        primary = Color(0xFF1F5F5B), onPrimary = Color(0xFFFFFFFF),
        primaryContainer = Color(0xFFD6EFEA), onPrimaryContainer = Color(0xFF0A2522),
        secondary = Color(0xFF5A6B66), onSecondary = Color(0xFFFFFFFF),
        secondaryContainer = Color(0xFFE2EBE7), onSecondaryContainer = Color(0xFF15201D),
        tertiary = Color(0xFF8A5A22), onTertiary = Color(0xFFFFFFFF),
        tertiaryContainer = Color(0xFFF6E2C8), onTertiaryContainer = Color(0xFF2B1A05),
        error = Color(0xFFBA1A1A), onError = Color(0xFFFFFFFF),
        errorContainer = Color(0xFFFFDAD6), onErrorContainer = Color(0xFF410002),
        background = Color(0xFFF7FAF8), onBackground = Color(0xFF191C1B),
        surface = Color(0xFFFBFDFB), onSurface = Color(0xFF191C1B),
        surfaceVariant = Color(0xFFE1E8E4), onSurfaceVariant = Color(0xFF44504C),
        surfaceContainerLowest = Color(0xFFFFFFFF),
        surfaceContainerLow = Color(0xFFF1F6F3),
        surfaceContainer = Color(0xFFEDF3F0),
        surfaceContainerHigh = Color(0xFFE7EEEA),
        surfaceContainerHighest = Color(0xFFE1E8E4),
        outline = Color(0xFF70807A),
        outlineVariant = Color(0xFFC9D4CF)
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


