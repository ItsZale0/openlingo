package com.alessandro.openlingo.desktop.ui

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
import androidx.compose.material.icons.filled.Translate
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
import com.alessandro.openlingo.data.StatoUnita
import com.alessandro.openlingo.data.LessicoLivelli
import com.alessandro.openlingo.data.UnitaConStato
import com.alessandro.openlingo.desktop.DesktopStore
import com.alessandro.openlingo.desktop.Schermata
import com.alessandro.openlingo.i18n.t

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
            titolo = t("Esercizi"),
            sottotitolo = "$completate " + t("unità completate su") + " ${unita.size} · " + t("tutto il percorso A0-B2")
        )

        if (prossima != null) {
            CardSezione(
                titolo = t("Consigliato ora"),
                sottotitolo = t("La prossima unità in linea con il tuo livello")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Etichetta(prossima.unita.livello)
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(t(prossima.unita.titolo), style = MaterialTheme.typography.titleMedium)
                        Text(
                            t(prossima.unita.sottotitolo),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    OutlinedButton(onClick = { onApriUnita(prossima.unita.id) }) {
                        Text(t("Apri"))
                    }
                }
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StrumentoRiga(
                icona = Icons.Filled.Refresh,
                titolo = t("Ripasso flashcard"),
                testo = if (daRipassare > 0) "$daRipassare " + t("carte in scadenza") else t("Nessuna scadenza") + ": ${store.paroleNuove().size} " + t("nuove da vedere"),
                onClick = { onVaiA(Schermata.RIPASSO) },
                modifier = Modifier.weight(1f)
            )
            StrumentoRiga(
                icona = Icons.Filled.Translate,
                titolo = t("Lessico A1-B2"),
                testo = "${LessicoLivelli.tutte.size} " + t("parole, sei tipi di esercizio"),
                onClick = { onVaiA(Schermata.LESSICO) },
                modifier = Modifier.weight(1f)
            )
            StrumentoRiga(
                icona = Icons.Filled.School,
                titolo = t("Allenatore articoli"),
                testo = t("Indovina der, die o das con le parole del tuo lessico"),
                onClick = { onVaiA(Schermata.GENDER_TRAINER) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StrumentoRiga(
                icona = Icons.AutoMirrored.Filled.MenuBook,
                titolo = t("Wortliste A1-B1"),
                testo = t("Aggiungi parole curate al lessico"),
                onClick = { onVaiA(Schermata.WORTLISTE) },
                modifier = Modifier.weight(1f)
            )
            StrumentoRiga(
                icona = Icons.Filled.Headphones,
                titolo = t("Podcast e video"),
                testo = t("Ascolto autentico per livello"),
                onClick = { onVaiA(Schermata.PODCAST) },
                modifier = Modifier.weight(1f)
            )
        }

        val livelli = listOf("A0", "A1", "A2", "B1", "B2")
        livelli.forEach { livello ->
            val gruppo = unita.filter { it.unita.livello.equals(livello, ignoreCase = true) }
            if (gruppo.isEmpty()) return@forEach
            Text(
                t("Livello") + " $livello",
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
        voce.stato == StatoUnita.COMPLETATA -> t("Completata") to MaterialTheme.colorScheme.tertiary
        voce.stato == StatoUnita.DA_RIPASSARE -> t("Da ripassare") to MaterialTheme.colorScheme.secondary
        voce.stato == StatoUnita.DISPONIBILE -> t("Disponibile") to MaterialTheme.colorScheme.primary
        voce.accessibile -> t("Apri libero") to MaterialTheme.colorScheme.primary
        else -> t("Bloccata") to MaterialTheme.colorScheme.onSurfaceVariant
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
                    t(voce.unita.titolo),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    t(voce.unita.sottotitolo) + " · ${voce.unita.minuti} " + t("min"),
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
                TextButton(onClick = onApri) { Text(t("Apri")) }
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
