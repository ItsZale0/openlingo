package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/** Intestazione di sezione: titolo grande e sottotitolo, con azioni a destra. */
@Composable
fun Testata(
    titolo: String,
    sottotitolo: String,
    azioni: @Composable RowScope.() -> Unit = {}
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(titolo, style = MaterialTheme.typography.headlineSmall)
            if (sottotitolo.isNotBlank()) {
                Text(
                    sottotitolo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), content = azioni)
    }
}

/** Card Material 3 con titolo opzionale: il contenitore standard delle sezioni. */
@Composable
fun CardSezione(
    titolo: String? = null,
    sottotitolo: String? = null,
    modifier: Modifier = Modifier,
    colore: Color = MaterialTheme.colorScheme.surfaceContainer,
    contenuto: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colore),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            if (titolo != null) {
                Text(titolo, style = MaterialTheme.typography.titleMedium)
                if (sottotitolo != null) {
                    Text(
                        sottotitolo,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
            }
            contenuto()
        }
    }
}

/** Numero chiave con etichetta: la versione desktop dei KPI dell'app. */
@Composable
fun Kpi(
    valore: String,
    etichetta: String,
    dettaglio: String? = null,
    colore: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.padding(horizontal = 16.dp, vertical = 14.dp)) {
            Text(
                valore,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = colore
            )
            Text(etichetta, style = MaterialTheme.typography.bodySmall)
            if (dettaglio != null) {
                Text(
                    dettaglio,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/** Barra lineare senza il "punto" finale: a 0% deve leggersi come vuota. */
@Composable
fun BarraLineare(
    valore: Float,
    modifier: Modifier = Modifier,
    colore: Color = MaterialTheme.colorScheme.primary,
    traccia: Color = MaterialTheme.colorScheme.surfaceVariant
) {
    LinearProgressIndicator(
        progress = { valore.coerceIn(0f, 1f) },
        modifier = modifier,
        color = colore,
        trackColor = traccia,
        drawStopIndicator = {}
    )
}

/** Barra con etichetta e valore percentuale, usata per competenze e progressi. */
@Composable
fun BarraProgresso(
    valore: Float,
    etichetta: String,
    dettaglio: String? = null,
    colore: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Column(modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(etichetta, style = MaterialTheme.typography.bodyMedium)
            Text(
                "${(valore.coerceIn(0f, 1f) * 100).toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = colore
            )
        }
        if (dettaglio != null) {
            Text(
                dettaglio,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(6.dp))
        BarraLineare(
            valore = valore,
            modifier = Modifier.fillMaxWidth().height(8.dp),
            colore = colore
        )
    }
}

/** Chip compatto per livello o stato. */
@Composable
fun Etichetta(
    testo: String,
    colore: Color = MaterialTheme.colorScheme.secondaryContainer,
    coloreTesto: Color = MaterialTheme.colorScheme.onSecondaryContainer
) {
    Surface(shape = RoundedCornerShape(8.dp), color = colore) {
        Text(
            testo,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = coloreTesto
        )
    }
}

/** Contenitore centrato con larghezza massima leggibile su schermi larghi. */
@Composable
fun ColonnaContenuto(
    modifier: Modifier = Modifier,
    contenuto: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier.widthIn(max = 960.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        content = contenuto
    )
}

@Composable
fun SpazioOrizzontale(dp: Int = 8) {
    Spacer(Modifier.width(dp.dp))
}

/** Contenitore standard delle schermate: margini e scroll verticale. */
@Composable
fun Pagina(contenuto: @Composable ColumnScope.() -> Unit) {
    Box(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 16.dp)
    ) {
        Column(Modifier.fillMaxWidth().padding(bottom = 40.dp), content = contenuto)
    }
}
