package com.alessandro.tedesco.ui

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.EserciziLessico
import com.alessandro.tedesco.data.LessicoLivelli
import com.alessandro.tedesco.data.TipoEsercizioLessico
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.dimensioneContenuto

/**
 * Esercizi sul lessico del livello scelto.
 *
 * Sei tipi mescolati (significato, parola tedesca, articolo, cloze, scrivi,
 * ascolto) perché riconoscere una parola è più facile che produrla: alternando
 * le due cose si allena anche il richiamo attivo. L'esito di ogni risposta va
 * nella stessa coda SRS del ripasso ([TedescoViewModel.rispondiLessico]).
 */
@Composable
fun EserciziLessicoScreen(vm: TedescoViewModel, onIndietro: () -> Unit) {
    val stato by vm.sessioneLessico.collectAsStateWithLifecycle(StatoSessioneLessico())
    val context = LocalContext.current
    val tts = rememberTtsHelper(context)

    val esercizio = stato.corrente

    // L'esercizio di ascolto deve pronunciare la parola appena compare.
    LaunchedEffect(stato.indice) {
        if (esercizio?.tipo == TipoEsercizioLessico.ASCOLTO) {
            tts.speak(EserciziLessico.tedescoConArticolo(esercizio.parola))
        }
    }

    ScreenScaffold("Esercizi lessico", onBack = onIndietro, scrollable = false) {
        Column(modifier = Modifier.fillMaxSize()) {
            LinearProgressIndicator(
                progress = { if (stato.totale == 0) 0f else stato.progresso },
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )
            Spacer(Modifier.height(16.dp))

            when {
                stato.totale == 0 -> StatoVuoto(
                    vm = vm,
                    livello = stato.livello
                )

                stato.finita -> Riepilogo(
                    stato = stato,
                    onRipeti = { vm.ripetiLessico() },
                    onChiudi = {
                        vm.chiudiLessico()
                        onIndietro()
                    }
                )

                esercizio != null -> EsercizioCorrente(
                    vm = vm,
                    stato = stato,
                    esercizio = esercizio,
                    onAscolta = { tts.speak(EserciziLessico.tedescoConArticolo(esercizio.parola)) }
                )
            }
        }
    }
}

@Composable
private fun StatoVuoto(vm: TedescoViewModel, livello: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Nessun esercizio pronto",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(8.dp))
        Text(
            "Scegli un livello: le parole vengono preparate e aggiunte al ripasso.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        LessicoLivelli.LIVELLI.forEach { lv ->
            Button(
                onClick = { vm.avviaEserciziLessico(lv) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) { Text("Esercizi $lv (${LessicoLivelli.conteggio(lv)})") }
        }
    }
}

@Composable
private fun Riepilogo(
    stato: StatoSessioneLessico,
    onRipeti: () -> Unit,
    onChiudi: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().animateContentSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Sessione completata",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "${stato.giuste} su ${stato.totale} corrette",
            style = MaterialTheme.typography.titleMedium
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Le parole giuste tornano più avanti, quelle sbagliate tornano domani.",
            style = MaterialTheme.typography.bodyMedium,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (stato.sbagliate.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(
                "Da rivedere: " + stato.sbagliate.distinct().take(8).joinToString(", "),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onRipeti,
            modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
        ) { Text("Nuovo giro") }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = onChiudi,
            modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
        ) { Text("Torna al lessico") }
    }
}

@Composable
private fun EsercizioCorrente(
    vm: TedescoViewModel,
    stato: StatoSessioneLessico,
    esercizio: com.alessandro.tedesco.data.EsercizioLessico,
    onAscolta: () -> Unit
) {
    var testo by remember(stato.indice) { mutableStateOf("") }

    // Il contenuto può superare l'altezza su schermi piccoli: scroll interno.
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "${stato.indice + 1} / ${stato.totale}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                "Livello ${stato.livello}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Spacer(Modifier.height(16.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(18.dp)) {
                Text(
                    esercizio.tipo.etichetta,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        esercizio.domanda,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.weight(1f)
                    )
                    if (esercizio.tipo == TipoEsercizioLessico.ASCOLTO) {
                        IconButton(onClick = onAscolta) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Ascolta di nuovo",
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        if (esercizio.aScelta) {
            esercizio.opzioni.forEach { opzione ->
                val scelta = stato.mostrata && stato.rispostaData.trim() == opzione.trim()
                val giusta = stato.mostrata && opzione.trim() == esercizio.corretta.trim()
                val colori = when {
                    giusta -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                    scelta -> ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                    else -> ButtonDefaults.outlinedButtonColors()
                }
                if (giusta || scelta) {
                    Button(
                        onClick = {},
                        enabled = false,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = colori
                    ) { Text(opzione) }
                } else {
                    OutlinedButton(
                        onClick = { vm.rispondiLessico(opzione) },
                        enabled = !stato.mostrata,
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                    ) { Text(opzione) }
                }
            }
        } else {
            OutlinedTextField(
                value = testo,
                onValueChange = { testo = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = !stato.mostrata,
                label = { Text("La tua risposta in tedesco") }
            )
            Spacer(Modifier.height(10.dp))
            if (!stato.mostrata) {
                Button(
                    onClick = { vm.rispondiLessico(testo) },
                    enabled = testo.isNotBlank(),
                    modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
                ) { Text("Controlla") }
            }
        }

        if (stato.mostrata) {
            Spacer(Modifier.height(16.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (stato.corretta) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            if (stato.corretta) Icons.Filled.Check else Icons.Filled.Close,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            if (stato.corretta) "Giusto" else "Non è questa",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Risposta corretta: ${esercizio.corretta}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (esercizio.spiegazione.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            esercizio.spiegazione,
                            style = MaterialTheme.typography.bodySmall,
                            fontStyle = FontStyle.Italic
                        )
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            Button(
                onClick = { vm.avantiLessico() },
                modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
            ) { Text(if (stato.indice + 1 >= stato.totale) "Vedi il risultato" else "Avanti") }
        }

        Spacer(Modifier.height(24.dp))
    }
}
