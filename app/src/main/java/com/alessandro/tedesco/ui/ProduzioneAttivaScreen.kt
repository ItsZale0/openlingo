package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.BancaProduttiva
import com.alessandro.tedesco.data.EsercizioProduttivo
import com.alessandro.tedesco.data.RisposteProduttive
import com.alessandro.tedesco.data.TipoProduttivo
import com.alessandro.tedesco.ui.theme.Spaziature
import java.util.Locale

/**
 * Esercizi PRODUTTIVI: lo studente scrive o ricostruisce la frase, non sceglie
 * tra quattro opzioni. Banca curata offline (72 esercizi) + correzione tollerante.
 */
@Composable
fun ProduzioneAttivaScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.codiceBreve() ?: "A1"
    val tts = rememberTtsHelper(LocalContext.current, Locale.GERMAN)

    var tipo by remember { mutableStateOf(TipoProduttivo.TRADUZIONE) }
    val esercizi = remember(livello, tipo) {
        BancaProduttiva.perLivello(livello, tipo).ifEmpty { BancaProduttiva.perTipo(tipo) }
    }
    var indice by remember(tipo, livello) { mutableIntStateOf(0) }
    var input by remember(tipo, livello, indice) { mutableStateOf("") }
    var esito by remember(tipo, livello, indice) { mutableStateOf<Boolean?>(null) }
    var corrette by remember(tipo, livello) { mutableIntStateOf(0) }
    var fatte by remember(tipo, livello) { mutableIntStateOf(0) }

    // Stato riordino (parole scelte / disponibili)
    var scelte by remember(tipo, livello, indice) { mutableStateOf<List<String>>(emptyList()) }
    var disponibili by remember(tipo, livello, indice) {
        mutableStateOf(esercizi.getOrNull(indice)?.parole?.shuffled() ?: emptyList())
    }

    val esercizio = esercizi.getOrNull(indice)

    ScreenScaffold(t("Produzione attiva"), onBack = onIndietro) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            Text(
                "Qui non si indovina: si produce. Scrivi la frase, completa il buco, " +
                    "riordina o scrivi sotto dettatura.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Selettore tipo
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
            ) {
                TipoProduttivo.entries.forEach { t ->
                    FilterChip(
                        selected = t == tipo,
                        onClick = { tipo = t },
                        label = { Text(t.etichetta) }
                    )
                }
            }

            if (esercizio == null) {
                Text(t("Nessun esercizio per questo livello."), style = MaterialTheme.typography.bodyMedium)
                return@Column
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    t("Livello ${esercizio.livello} - ${esercizio.tipo.etichetta}"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    t("$corrette / $fatte"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                val ascolto = esercizio.daAscoltare
                Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                    Text(esercizio.consegna, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(Spaziature.sm))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            esercizio.prompt,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f)
                        )
                        if (ascolto != null) {
                            IconButton(onClick = { tts.speak(ascolto, velocita = 0.8f) }) {
                                Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = t("Ascolta"))
                            }
                        }
                    }
                }
            }

            if (esercizio.tipo == TipoProduttivo.RIORDINO) {
                // Parole scelte
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(Modifier.fillMaxWidth().padding(Spaziature.sm)) {
                        if (scelte.isEmpty()) {
                            Text(
                                t("Tocca le parole qui sotto nell'ordine giusto."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        } else {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
                            ) {
                                scelte.forEachIndexed { i, parola ->
                                    OutlinedButton(
                                        onClick = {
                                            if (esito == null) {
                                                scelte = scelte.filterIndexed { idx, _ -> idx != i }
                                                disponibili = disponibili + parola
                                            }
                                        }
                                    ) { Text(parola) }
                                }
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
                ) {
                    disponibili.forEachIndexed { i, parola ->
                        OutlinedButton(
                            onClick = {
                                if (esito == null) {
                                    disponibili = disponibili.filterIndexed { idx, _ -> idx != i }
                                    scelte = scelte + parola
                                }
                            }
                        ) { Text(parola) }
                    }
                }
            } else {
                OutlinedTextField(
                    value = input,
                    onValueChange = { if (esito == null) input = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(if (esercizio.tipo == TipoProduttivo.CLOZE) t("Parola mancante") else t("La tua frase in tedesco")) },
                    minLines = 2,
                    enabled = esito == null,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        imeAction = ImeAction.Done
                    )
                )
            }

            if (esito == null) {
                Button(
                    onClick = {
                        val risposta = if (esercizio.tipo == TipoProduttivo.RIORDINO) scelte.joinToString(" ") else input
                        val giusto = RisposteProduttive.valuta(risposta, esercizio)
                        esito = giusto
                        fatte++
                        if (giusto) corrette++ else vm.registraErrore(
                            tipo = esercizio.tipo.name.lowercase(),
                            domanda = esercizio.prompt,
                            rispostaCorretta = esercizio.risposta,
                            rispostaData = risposta,
                            spiegazione = esercizio.spiegazione,
                            livello = esercizio.livello
                        )
                    },
                    enabled = if (esercizio.tipo == TipoProduttivo.RIORDINO) scelte.isNotEmpty() else input.isNotBlank(),
                    modifier = Modifier.fillMaxWidth()
                ) { Text(t("Controlla")) }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (esito == true) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                if (esito == true) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.width(Spaziature.sm))
                            Text(
                                if (esito == true) "Corretto! 🎉" else t("Non ancora"),
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleSmall
                            )
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (esito == true) t("Ottimo!") else t("Continua a provare."),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        Spacer(Modifier.height(Spaziature.sm))
                        Text(
                            t("Risposta: ${RisposteProduttive.rispostaMostrata(esercizio)}"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (esercizio.spiegazione.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(esercizio.spiegazione, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                Button(
                    onClick = {
                        val prossimo = (indice + 1) % esercizi.size
                        indice = prossimo
                        input = ""
                        esito = null
                        scelte = emptyList()
                        disponibili = esercizi[prossimo].parole.shuffled()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(Spaziature.sm))
                    Text(t("Prossimo esercizio"))
                }
            }

            Spacer(Modifier.height(Spaziature.sm))
            Text(
                "Suggerimento: scrivi senza guardare la soluzione. " +
                    t("Se sbagli, l'errore finisce nel Quaderno errori e torna al momento giusto."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/** Anteprima compatta, usata dal Quaderno errori se serve mostrare un esercizio. */
@Composable
fun RigaEsercizioProduttivo(esercizio: EsercizioProduttivo) {
    Text(
        t("${esercizio.tipo.etichetta}: ${esercizio.prompt}"),
        style = MaterialTheme.typography.bodyMedium
    )
}
