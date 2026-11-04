package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.StatoUnita
import com.alessandro.tedesco.data.UnitaConStato
import com.alessandro.tedesco.desktop.DesktopStore
import com.alessandro.tedesco.desktop.Schermata

@Composable
fun SchermataEsercizi(
    store: DesktopStore,
    onApriUnita: (String) -> Unit,
    onVaiA: (Schermata) -> Unit
) {
    store.repo
    val unita = store.unita()
    val prossima = store.prossimaUnita()
    val completate = unita.count { it.stato == StatoUnita.COMPLETATA }
    val daRipassare = store.paroleDaRipassare().size

    Pagina {
        Testata(
            titolo = "Esercizi",
            sottotitolo = "$completate unità completate su ${unita.size} · tutto il percorso A0-B2"
        )

        if (prossima != null) {
            CardSezione(
                titolo = "Consigliato ora",
                sottotitolo = "La prossima unità in linea con il tuo livello"
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Etichetta(prossima.unita.livello)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(prossima.unita.titolo, style = MaterialTheme.typography.titleMedium)
                        Text(
                            prossima.unita.sottotitolo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = { onApriUnita(prossima.unita.id) }) {
                        Text("Apri")
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StrumentoRiga(
                icona = Icons.Filled.Refresh,
                titolo = "Ripasso flashcard",
                testo = if (daRipassare > 0) "$daRipassare carte in scadenza" else "Nessuna scadenza: ${store.paroleNuove().size} nuove da vedere",
                onClick = { onVaiA(Schermata.RIPASSO) },
                modifier = Modifier.weight(1f)
            )
            StrumentoRiga(
                icona = Icons.AutoMirrored.Filled.MenuBook,
                titolo = "Wortliste A1-B1",
                testo = "Aggiungi parole curate al lessico",
                onClick = { onVaiA(Schermata.WORTLISTE) },
                modifier = Modifier.weight(1f)
            )
            StrumentoRiga(
                icona = Icons.Filled.Headphones,
                titolo = "Podcast e video",
                testo = "Ascolto autentico per livello",
                onClick = { onVaiA(Schermata.PODCAST) },
                modifier = Modifier.weight(1f)
            )
        }

        val livelli = listOf("A0", "A1", "A2", "B1", "B2")
        livelli.forEach { livello ->
            val gruppo = unita.filter { it.unita.livello.equals(livello, ignoreCase = true) }
            if (gruppo.isEmpty()) return@forEach
            Text(
                "Livello $livello",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 8.dp)
            )
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                gruppo.forEach { voce ->
                    RigaUnita(voce = voce, onApri = { onApriUnita(voce.unita.id) })
                }
            }
        }
    }
}

@Composable
private fun RigaUnita(voce: UnitaConStato, onApri: () -> Unit) {
    val (testoStato, coloreStato) = when {
        voce.stato == StatoUnita.COMPLETATA -> "Completata" to MaterialTheme.colorScheme.tertiary
        voce.stato == StatoUnita.DA_RIPASSARE -> "Da ripassare" to MaterialTheme.colorScheme.secondary
        voce.stato == StatoUnita.DISPONIBILE -> "Disponibile" to MaterialTheme.colorScheme.primary
        voce.accessibile -> "Apri libero" to MaterialTheme.colorScheme.primary
        else -> "Bloccata" to MaterialTheme.colorScheme.onSurfaceVariant
    }
    val apribile = voce.accessibile

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = apribile, onClick = onApri),
        shape = MaterialTheme.shapes.medium,
        color = if (apribile) MaterialTheme.colorScheme.surfaceContainer
        else MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.4f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (voce.stato) {
                    StatoUnita.COMPLETATA -> Icons.Filled.CheckCircle
                    StatoUnita.BLOCCATA -> Icons.Filled.Lock
                    else -> Icons.Filled.School
                },
                contentDescription = null,
                tint = if (apribile) coloreStato else MaterialTheme.colorScheme.outline
            )
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    voce.unita.titolo,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    "${voce.unita.sottotitolo} · ${voce.unita.minuti} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            voce.padronanza?.let { pad ->
                if (pad.totali > 0) {
                    Text(
                        "${pad.percentuale}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = coloreStato
                    )
                    Spacer(Modifier.width(12.dp))
                }
            }
            Etichetta(
                testoStato,
                colore = coloreStato.copy(alpha = 0.16f),
                coloreTesto = coloreStato
            )
            if (apribile) {
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onApri) { Text("Apri") }
            }
        }
    }
}

@Composable
private fun StrumentoRiga(
    icona: ImageVector,
    titolo: String,
    testo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icona, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(titolo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    testo,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private fun coloreStato(stato: StatoUnita): Color = when (stato) {
    StatoUnita.COMPLETATA -> Color(0xFF1F7A5C)
    StatoUnita.DA_RIPASSARE -> Color(0xFF2C5F8A)
    StatoUnita.DISPONIBILE -> Color(0xFF2F5BEA)
    StatoUnita.BLOCCATA -> Color(0xFF74777F)
}
