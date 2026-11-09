package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import android.speech.tts.TextToSpeech
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
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.data.VerificaLessico
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.dimensioneContenuto

/**
 * Quiz di VERIFICA del lessico: scelta multipla oggettiva.
 * Sostituisce il self-report (t("Lo so")/t("Non lo so")): qui si DIMOSTRA di sapere
 * scegliendo il significato corretto tra i distrattori.
 */
@Composable
fun VerificaScreen(vm: TedescoViewModel, onIndietro: () -> Unit) {
    val domande by vm.domandeVerifica.collectAsStateWithLifecycle(emptyList())
    val esito by vm.esitoVerifica.collectAsStateWithLifecycle(null)
    val indice by vm.indiceVerifica.collectAsStateWithLifecycle(0)
    val scelta by vm.sceltaVerifica.collectAsStateWithLifecycle<String?>(null)

    val context = LocalContext.current
    val tts = rememberTtsHelper(context)

    LaunchedEffect(Unit) { vm.avviaVerifica() }

    ScreenScaffold(t("Verifica lessico"), onBack = onIndietro, scrollable = false) {
        Column(Modifier.fillMaxSize().widthIn(max = dimensioneContenuto())) {
            val totale = domande.size
            if (totale > 0) {
                LinearProgressIndicator(
                    progress = { indice.toFloat() / totale.toFloat() },
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )
            }
            Spacer(Modifier.height(12.dp))

            val e = esito
            if (e != null) {
                // --- Riepilogo finale ---
                Column(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(t("Verifica completata"), style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(12.dp))
                    Text(t("${e.corrette} / ${e.totale} corrette"),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (e.punteggio >= 60f) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.error)
                    Spacer(Modifier.height(4.dp))
                    Text(t("${e.punteggio.toInt()}%"), style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(t("Cosa significa"), style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                t("Hai DIMOSTRATO di sapere ${e.paroleVerificate.size} ") +
                                    (if (e.paroleVerificate.size == 1) "parola" else "parole") +
                                    ". Queste contano nel tuo lessico.",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            if (e.paroleDaRivedere.isNotEmpty()) {
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    t("${e.paroleDaRivedere.size} da rivedere: le ritroverai nel quiz."),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = onIndietro,
                        modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
                    ) { Text(t("Fatto")) }
                }
            } else if (totale == 0) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(t("Hai verificato tutto il lessico disponibile"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = TextAlign.Center)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Non ci sono parole nuove da dimostrare. Le parole gia' verificate " +
                                t("tornano nel Ripasso secondo la spaced repetition."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = onIndietro,
                            modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
                        ) { Text(t("Fatto")) }
                    }
                }
            } else {
                val d = domande.getOrNull(indice)
                if (d == null) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(t("Caricamento…"))
                    }
                } else {
                    Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                        Text(t("${indice + 1} di $totale"), style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        Text(t("Cosa significa questa parola?"), style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(14.dp))

                        // Parola tedesca
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ParolaTedesca.conArticolo(d.parola.article, d.parola.german),
                                    fontSize = 28.sp, fontWeight = FontWeight.SemiBold,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                Spacer(Modifier.size(10.dp))
                                IconButton(onClick = { tts.speak(ParolaTedesca.conArticolo(d.parola.article, d.parola.german)) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, t("Ascolta"),
                                        modifier = Modifier.size(22.dp),
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                            }
                        }
                        Spacer(Modifier.height(18.dp))

                        // Opzioni
                        d.opzioni.forEach { opzione ->
                            val rivelato = scelta != null
                            val eCorretta = opzione == d.corretta
                            val eScelta = opzione == scelta
                            val bordo = when {
                                rivelato && eCorretta -> MaterialTheme.colorScheme.primary
                                rivelato && eScelta -> MaterialTheme.colorScheme.error
                                else -> MaterialTheme.colorScheme.outlineVariant
                            }
                            OutlinedButton(
                                onClick = { if (scelta == null) vm.rispondiVerifica(opzione) },
                                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                shape = MaterialTheme.shapes.large,
                                border = androidx.compose.foundation.BorderStroke(2.dp, bordo),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    containerColor = when {
                                        rivelato && eCorretta ->
                                            MaterialTheme.colorScheme.primaryContainer
                                        rivelato && eScelta ->
                                            MaterialTheme.colorScheme.errorContainer
                                        else -> Color.Transparent
                                    }
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(opzione, modifier = Modifier.weight(1f),
                                        style = MaterialTheme.typography.bodyLarge,
                                        textAlign = TextAlign.Start)
                                    if (rivelato && eCorretta) {
                                        Icon(Icons.Filled.Check, null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(20.dp))
                                    } else if (rivelato && eScelta) {
                                        Icon(Icons.Filled.Close, null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }

                        if (scelta != null) {
                            Spacer(Modifier.height(14.dp))
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .animateContentSize(),
                                shape = MaterialTheme.shapes.medium,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                                    Text(
                                        if (scelta == d.corretta) "Corretto 🎉" else t("Sbagliato"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = if (scelta == d.corretta) MaterialTheme.colorScheme.primary
                                                else MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.height(2.dp))
                                    Text(
                                        if (scelta == d.corretta) t("Ottimo!") else t("Riprova."),
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    if (d.parola.example.isNotBlank()) {
                                        Spacer(Modifier.height(4.dp))
                                        Text(d.parola.example, style = MaterialTheme.typography.bodyMedium,
                                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic)
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { vm.prossimaVerifica() },
                                modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
                            ) { Text(if (indice + 1 >= totale) t("Vedi risultato") else t("Avanti")) }
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
