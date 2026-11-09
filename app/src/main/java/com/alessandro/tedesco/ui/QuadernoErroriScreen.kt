package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.ErroriSRS
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Quaderno degli errori: SRS separato dal lessico. Ogni errore reale torna
 * dopo 1, 3, 7, 16, 35, 75, 150 giorni; dopo 5 richiami riusciti è risolto.
 */
@Composable
fun QuadernoErroriScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    val tutti by vm.errori.collectAsStateWithLifecycle(emptyList())
    val daRipassare by vm.erroriDaRipassare.collectAsStateWithLifecycle(emptyList())
    val aperti = remember(tutti) { tutti.filter { !it.risolto } }
    val risolti = remember(tutti) { tutti.count { it.risolto } }

    ScreenScaffold(t("Quaderno errori"), onBack = onIndietro) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(Spaziature.md),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Filled.ErrorOutline, contentDescription = null, modifier = Modifier.size(24.dp))
                    Spacer(Modifier.width(Spaziature.sm))
                    Column(Modifier.weight(1f)) {
                        Text(
                            "${aperti.size} errori da consolidare",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (daRipassare.isNotEmpty())
                                "${daRipassare.size} pronti da ripassare adesso"
                            else t("Nessun ripasso in scadenza: torna più tardi."),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (risolti > 0) {
                        TextButton(onClick = { vm.svuotaErroriRisolti() }) {
                            Text(t("Pulisci ($risolti)"))
                        }
                    }
                }
            }

            if (aperti.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.fillMaxWidth().padding(Spaziature.lg)) {
                        Text(
                            t("Nessun errore registrato"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Gli errori di grammatica e produzione finiscono qui automaticamente. " +
                                t("Fai esercizi per riempire il quaderno."),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            val apertiOrdinati = remember(aperti) { aperti.sortedBy { it.prossimaRipasso } }
            apertiOrdinati.forEach { errore ->
                val inScadenza = ErroriSRS.daRipassare(errore)
                var mostraRisposta by remember(errore.id) { mutableStateOf(false) }
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (inScadenza)
                            MaterialTheme.colorScheme.surfaceContainer
                        else MaterialTheme.colorScheme.surface
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                "${errore.tipo} - ${errore.livello}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.weight(1f)
                            )
                            if (!inScadenza) {
                                Text(
                                    "tra ${ErroriSRS.giorniAllaScadenza(errore)} g",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            IconButton(onClick = { vm.eliminaErrore(errore.id) }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = t("Elimina errore"),
                                    modifier = Modifier.size(18.dp),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text(errore.domanda, style = MaterialTheme.typography.bodyLarge)
                        if (errore.rispostaData.isNotBlank()) {
                            Text(
                                "Hai scritto: ${errore.rispostaData}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Spacer(Modifier.height(Spaziature.sm))
                        if (mostraRisposta) {
                            Text(
                                "Risposta corretta: ${errore.rispostaCorretta}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            if (errore.spiegazione.isNotBlank()) {
                                Spacer(Modifier.height(4.dp))
                                Text(errore.spiegazione, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
                                Button(
                                    onClick = { vm.rispondiErrore(errore.id, saputo = true) },
                                    modifier = Modifier.weight(1f)
                                ) { Text(t("Sapevo")) }
                                OutlinedButton(
                                    onClick = { vm.rispondiErrore(errore.id, saputo = false) },
                                    modifier = Modifier.weight(1f)
                                ) { Text(t("Da rivedere")) }
                            }
                        } else {
                            OutlinedButton(
                                onClick = { mostraRisposta = true },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Filled.Visibility, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.width(Spaziature.sm))
                                Text(t("Mostra risposta"))
                            }
                        }
                    }
                }
            }

            if (risolti > 0) {
                Spacer(Modifier.height(Spaziature.sm))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(Spaziature.md),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(20.dp))
                        Spacer(Modifier.width(Spaziature.sm))
                        Text(
                            "$risolti errori consolidati dopo 5 richiami",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
