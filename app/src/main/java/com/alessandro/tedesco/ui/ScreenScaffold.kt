@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.tedesco.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

/**
 * Scaffold base CENTRALIZZATO: gestisce tutti gli insets di sistema in un solo punto.
 * - Status bar inset applicato UNA sola volta sopra la top bar compatta.
 * - Top bar compatta: status bar + ~8dp + titolo (+ back opzionale + azioni).
 * - IME (tastiera) padding per gli schermi con input.
 * - Contenuto con padding orizzontale, larghezza max e clearance in basso (24dp)
 *   cosi' l'ultimo elemento e' raggiungibile sopra bottom nav / gesture bar.
 *
 * La bottom navigation vive nello Scaffold root di AppNav (gestisce il padding basso
 * del NavHost); qui aggiungiamo solo la clearance interna.
 */
private val BottomClearance = 32.dp

/** Top bar compatta coerente su tutte le schermate. */
@Composable
fun CompactTopBar(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
) {
    Column(Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.background)) {
        // Inset reale della status bar (include notch/cutout in portrait) — applicato qui, una sola volta
        Spacer(Modifier.windowInsetsPadding(WindowInsets.statusBars))
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (onBack != null) {
                IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Indietro")
                }
                Spacer(Modifier.width(8.dp))
            }
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            actions()
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.45f))
    }
}

/**
 * Scaffold riutilizzabile per tutte le schermate.
 * @param scrollable se true avvolge il contenuto in verticalScroll con contentPadding corretto.
 */
@Composable
fun ScreenScaffold(
    title: String,
    onBack: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {},
    scrollable: Boolean = true,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .imePadding()
    ) {
        CompactTopBar(title = title, onBack = onBack, actions = actions)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .then(if (scrollable) Modifier.verticalScroll(rememberScrollState()) else Modifier),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .widthIn(max = dimensioneContenuto())
                    .padding(horizontal = spaziaturaSchermo())
                    .padding(top = 16.dp, bottom = BottomClearance)
            ) {
                content()
            }
        }
    }
}
