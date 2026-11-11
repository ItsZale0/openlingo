package com.alessandro.openlingo.ui

import com.alessandro.openlingo.i18n.t

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.TextButton
import com.alessandro.openlingo.data.QuizData
import com.alessandro.openlingo.data.remote.TipoEsercizio
import com.alessandro.openlingo.data.DeduplicaEsercizi
import com.alessandro.openlingo.data.local.DomandaTest
import com.alessandro.openlingo.data.local.LivelloCEFR
import com.alessandro.openlingo.data.local.TipoDomanda
import com.alessandro.openlingo.data.local.toDomandaTest
import com.alessandro.openlingo.ui.theme.AltezzaBottonePrincipale
import com.alessandro.openlingo.ui.theme.Spaziature
import com.alessandro.openlingo.ui.theme.TitoloSchermata
import com.alessandro.openlingo.ui.theme.dimensioneContenuto
import com.alessandro.openlingo.ui.theme.spaziaturaSchermo
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.CircularProgressIndicator
import kotlinx.coroutines.launch
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComprensioneScreen(vm: OpenLingoViewModel, onFatto: () -> Unit = {}) {
    // Esercizi generati dall'AI (persistiti nel profilo) — combinati con il pool statico
    val eserciziGen by vm.eserciziGenComprensione.collectAsStateWithLifecycle()
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle()
    val livelloUtente = profilo?.stato?.progresso?.livelloCorrente ?: LivelloCEFR.A0
    var elenco by remember { mutableStateOf<List<DomandaTest>>(emptyList()) }
    var caricato by remember { mutableStateOf(false) }
    var tentativo by remember { mutableIntStateOf(0) }
    var indice by remember { mutableIntStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableIntStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var generaInCorso by remember { mutableStateOf(false) }
    var erroreGen by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)
    val scope = rememberCoroutineScope()

    // Batch SEMPRE fresco: nessun set statico riciclato. Se la generazione fallisce
    // si riusa l'ultimo batch AI non ancora completato, altrimenti si mostra t("Riprova").
    LaunchedEffect(tentativo) {
        caricato = false
        erroreGen = null
        elenco = emptyList()
        vm.caricaEserciziGenerati()
        val freschi = try {
            vm.generaEsercizi(TipoEsercizio.COMPRENSIONE, quanti = 10)
        } catch (e: Throwable) {
            erroreGen = e.localizedMessage ?: t("Errore di rete")
            emptyList()
        }
        // Deduplica per contenuto: il modello può ripetere la stessa domanda
        // più volte nello stesso batch (id diversi, testo uguale).
        val freschiUnici = DeduplicaEsercizi.distinti(freschi) {
            DeduplicaEsercizi.chiave(it.domanda, it.fraseTedesca)
        }
        elenco = if (freschiUnici.isNotEmpty()) {
            freschiUnici.map {
                DomandaTest(
                    id = it.id,
                    tipo = TipoDomanda.GRAMMATICA,
                    domanda = it.domanda,
                    opzioni = it.opzioni,
                    rispostaCorretta = it.rispostaCorretta,
                    spiegazione = it.spiegazione,
                    livelloRichiesto = LivelloCEFR.entries.firstOrNull { l -> l.label == it.livello } ?: LivelloCEFR.A0,
                    tagGrammatica = null,
                    testoLettura = it.testoLettura,
                    glossario = it.glossario
                )
            }
        } else {
            val cached = DeduplicaEsercizi.distinti(eserciziGen.filterNot { it.completato }) {
                DeduplicaEsercizi.chiave(it.domanda, it.fraseTedesca)
            }.map {
                DomandaTest(
                    id = it.id,
                    tipo = TipoDomanda.GRAMMATICA,
                    domanda = it.domanda,
                    opzioni = it.opzioni,
                    rispostaCorretta = it.rispostaCorretta.coerceIn(0, it.opzioni.lastIndex.coerceAtLeast(0)),
                    spiegazione = it.spiegazione,
                    livelloRichiesto = LivelloCEFR.entries.firstOrNull { l -> l.label == it.livello } ?: LivelloCEFR.A0,
                    tagGrammatica = null,
                    testoLettura = it.testoLettura,
                    glossario = it.glossario
                )
            }
            if (cached.isNotEmpty()) cached else {
                // Fallback senza AI / senza rete: default predefiniti (quiz comprensione).
                val ordine = LivelloCEFR.entries.indexOf(livelloUtente).coerceAtLeast(0)
                QuizData.domandeComprensione.filter {
                    LivelloCEFR.entries.indexOf(it.livelloRichiesto) <= ordine
                }.ifEmpty { QuizData.domandeComprensione }.shuffled().take(10)
            }
        }
        indice = 0
        caricato = true
    }

    ScreenScaffold(t("Comprensione")) {

            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                if (!caricato) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text(t("Genero esercizi nuovi..."))
                        }
                    }
                } else if (elenco.isEmpty() && !completato) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text(t("Nessun esercizio generato"), style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            Text(
                                erroreGen ?: t("Controlla la connessione e riprova."),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(onClick = { tentativo++ }) { Text(t("Riprova")) }
                            Spacer(Modifier.height(8.dp))
                            OutlinedButton(onClick = onFatto) { Text(t("Torna indietro")) }
                        }
                    }
                } else if (!completato && indice in elenco.indices) {
                    val domanda = elenco[indice]

                    // Progress
                    Text(
                        text = t("Domanda") + " ${indice + 1}/${elenco.size} · ${eserciziGen.size} " + t("generati"),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Testo di lettura (Lesen): il testo tedesco su cui vertono le domande.
                    val testo = domanda.testoLettura
                    if (!testo.isNullOrBlank()) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(Spaziature.md)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = t("Leggi il testo"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { ttsHelper.speak(testo) }) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = t("Ascolta il testo"),
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = testo,
                                    style = MaterialTheme.typography.bodyMedium
                                )

                                // Glossario: parole difficili del testo con traduzione,
                                // ognuna aggiungibile al dizionario con un tap.
                                if (domanda.glossario.isNotEmpty()) {
                                    Spacer(Modifier.height(12.dp))
                                    Text(
                                        text = t("Parole nuove (tocca per aggiungerle al dizionario)"),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.height(4.dp))
                                    domanda.glossario.forEach { voce ->
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = voce.tedesco,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                modifier = Modifier.weight(1f)
                                            )
                                            Text(
                                                text = "= ${t(voce.italiano)}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                modifier = Modifier.weight(1f)
                                            )
                                            IconButton(
                                                onClick = {
                                                    vm.aggiungiParolaCustom(
                                                        german = voce.tedesco,
                                                        italian = voce.italiano,
                                                        example = "",
                                                        article = null,
                                                        pronunciation = null,
                                                        lesson = 0,
                                                        tags = "lesen"
                                                    )
                                                }
                                            ) {
                                                Icon(
                                                    Icons.Filled.Add,
                                                    contentDescription = t("Aggiungi al dizionario"),
                                                    tint = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Domanda
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = t(domanda.domanda),
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { ttsHelper.speak(domanda.domanda) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = t("Ascolta domanda"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Opzioni
                    domanda.opzioni.forEachIndexed { index, opzione ->
                        val selezionato = rispostaSelezionata == index
                        val colore = when {
                            risultato == null -> MaterialTheme.colorScheme.surface
                            index == domanda.rispostaCorretta -> MaterialTheme.colorScheme.primary
                            selezionato -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            onClick = {
                                if (risultato == null) {
                                    rispostaSelezionata = index
                                    risultato = index == domanda.rispostaCorretta
                                    if (risultato == true) {
                                        punteggio++
                                    }
                                    vm.segnaEsercizioCompletato(TipoEsercizio.COMPRENSIONE, domanda.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colore)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spaziature.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = t(opzione),
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { ttsHelper.speak(opzione) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = t("Ascolta opzione"),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Spiegazione
                    if (risultato != null) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .animateContentSize(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (risultato == true)
                                    MaterialTheme.colorScheme.primaryContainer
                                else
                                    MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(Spaziature.md)) {
                                Text(
                                    text = if (risultato == true) t("Corretto! 🎉") else t("Sbagliato"),
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (risultato == true) t("Ottimo lavoro, continua così!") else t("Non preoccuparti, riprova."),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(text = t(domanda.spiegazione))
                            }
                        }

                        Button(
                            onClick = {
                                val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + domanda.id
                                var next = indice + 1
                                while (next < elenco.size && elenco[next].id in idsCompletati) next++
                                if (next < elenco.size) {
                                    indice = next
                                    rispostaSelezionata = null
                                    risultato = null
                                } else {
                                    completato = true
                                    vm.completaPassoGiornaliero("comprensione")
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + domanda.id
                            var next = indice + 1
                            while (next < elenco.size && elenco[next].id in idsCompletati) next++
                            Text(if (next < elenco.size) t("Prossimo") else t("Vedi risultato"))
                        }
                    }
                } else {
                    // Risultato finale
                    val punteggioPct = if (elenco.isNotEmpty()) (punteggio * 100 / elenco.size) else 0
                    val errori = elenco.size - punteggio

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(Spaziature.lg),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = t("Punteggio") + ": $punteggio/${elenco.size}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "$punteggioPct%",
                                style = MaterialTheme.typography.displaySmall
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = t("Errori") + ": $errori",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                    }

                    // Salva risultato
                    LaunchedEffect(completato) {
                        if (completato) {
                            vm.salvaTestComprensione(
                                punteggio = punteggioPct.toFloat(),
                                errori = errori,
                                totale = elenco.size
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    if (erroreGen != null) {
                        Text(
                            text = erroreGen ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    Spacer(Modifier.height(8.dp))

                    // Fine sessione: torna ad Altro; rientrando si generano 10 esercizi nuovi.
                    Button(
                        onClick = onFatto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text(t("Fatto"))
                    }
                }
            }

    }

    // Dialogo errore chiave API
    if (erroreGen?.contains(t("Chiave API")) == true) {
        AlertDialog(
            onDismissRequest = { erroreGen = null },
            title = { Text(t("Chiave API necessaria")) },
            text = { Text(t("Per generare esercizi infiniti inserisci una chiave Groq gratuita in Profilo → Tutor AI.")) },
            confirmButton = {
                TextButton(onClick = { erroreGen = null }) { Text(t("OK")) }
            }
        )
    }
}
