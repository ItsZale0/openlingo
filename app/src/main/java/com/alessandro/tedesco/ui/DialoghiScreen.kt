package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.Dialoghi
import com.alessandro.tedesco.data.Dialoghi.DialogoAscolto
import com.alessandro.tedesco.data.RotoreEsercizi
import com.alessandro.tedesco.ui.theme.Spaziature
import java.util.Locale

/**
 * Dialoghi per l'ascolto: conversazioni realistiche a due voci, con traduzione
 * a richiesta e domande di comprensione. Le due voci sono differenziate dal
 * pitch del TTS, così si capisce chi parla.
 */
@Composable
fun DialoghiScreen(
    onIndietro: () -> Unit = {}
) {
    val tts = rememberTtsHelper(LocalContext.current, Locale.GERMAN)
    var aperto by remember { mutableStateOf<DialogoAscolto?>(null) }

    val dialogo = aperto
    if (dialogo == null) {
        Column(Modifier.fillMaxSize()) {
            CompactTopBar(title = "Dialoghi", onBack = onIndietro)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(Spaziature.md),
                verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
            ) {
                Text(
                    "Ascolto a due voci: prima senza traduzione, poi con. " +
                        t("Rispondi alle domande senza rileggere."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                RotoreEsercizi.ordine("dialoghi", Dialoghi.dialoghi.size)
                    .map { Dialoghi.dialoghi[it] }.forEach { d ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { aperto = d },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(Spaziature.md).animateContentSize()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    d.titolo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.weight(1f)
                                )
                                Text(
                                    d.livello,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                d.traduzione,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "${d.battute.size} battute - ${d.domande.size} domande",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
        return
    }

    Column(Modifier.fillMaxSize()) {
        CompactTopBar(title = dialogo.titolo, onBack = {
            tts.stop()
            aperto = null
        })
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(Spaziature.md),
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            Text(
                dialogo.traduzione,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
                OutlinedButton(onClick = {
                    tts.speak(dialogo.battute.joinToString(" ") { it.testo }, velocita = 0.9f)
                }) {
                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t("Ascolta tutto"))
                }
                OutlinedButton(onClick = { tts.stop() }) {
                    Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t("Ferma"))
                }
            }

            dialogo.battute.forEach { battuta ->
                val eA = battuta.parlante == "A"
                var traduzioneVisibile by remember(battuta.testo) { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (eA)
                            MaterialTheme.colorScheme.surfaceContainer
                        else MaterialTheme.colorScheme.surfaceContainerHigh
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(Spaziature.sm)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                if (eA) "A" else "B",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.width(20.dp)
                            )
                            Text(
                                battuta.testo,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = {
                                tts.speak(
                                    battuta.testo,
                                    velocita = 0.85f,
                                    pitch = if (eA) 1.05f else 0.9f
                                )
                            }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = t("Ascolta battuta"),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        TextButton(
                            onClick = { traduzioneVisibile = !traduzioneVisibile },
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 0.dp, vertical = 0.dp)
                        ) {
                            Text(
                                if (traduzioneVisibile) battuta.traduzione else t("Mostra traduzione"),
                                style = MaterialTheme.typography.labelSmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spaziature.sm))
            Text(
                t("Domande di comprensione"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            var risposte by remember(dialogo.id) { mutableStateOf<Map<Int, Int>>(emptyMap()) }
            dialogo.domande.forEachIndexed { qi, domanda ->
                val scelta = risposte[qi]
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                        Text(domanda.domanda, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(Spaziature.xs))
                        domanda.opzioni.forEachIndexed { oi, opzione ->
                            val verificato = scelta != null
                            val giusta = oi == domanda.corretta
                            val sfondo = when {
                                !verificato -> MaterialTheme.colorScheme.surface
                                giusta -> MaterialTheme.colorScheme.primaryContainer
                                scelta == oi -> MaterialTheme.colorScheme.errorContainer
                                else -> MaterialTheme.colorScheme.surface
                            }
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 2.dp)
                                    .clickable { if (scelta == null) risposte = risposte + (qi to oi) },
                                colors = CardDefaults.cardColors(containerColor = sfondo)
                            ) {
                                Row(
                                    Modifier.fillMaxWidth().padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(opzione, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
                                    if (verificato && giusta) {
                                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    } else if (verificato && scelta == oi) {
                                        Icon(Icons.Filled.Close, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                "Punteggio: ${dialogo.domande.indices.count { risposte[it] == dialogo.domande[it].corretta }} / ${dialogo.domande.size}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            TextButton(onClick = { risposte = emptyMap() }) { Text(t("Riprova le domande")) }
        }
    }
}
