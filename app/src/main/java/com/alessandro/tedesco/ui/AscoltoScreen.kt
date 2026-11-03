package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.alessandro.tedesco.data.AscoltoData
import com.alessandro.tedesco.data.DeduplicaEsercizi
import com.alessandro.tedesco.data.EsercizioAscolto
import com.alessandro.tedesco.data.local.ProfiloUtente

import com.alessandro.tedesco.data.remote.EsercizioGenerato
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Spaziature
import kotlinx.coroutines.launch

/**
 * Ascolto (Hörverstehen).
 *
 * Prima regola: funziona SEMPRE, anche senza rete e senza AI. Gli esercizi
 * locali di [AscoltoData] sono già calibrati per livello e vengono mostrati
 * subito; la generazione con l'AI è un extra su richiesta, non un requisito.
 * Così la schermata non resta più vuota quando la generazione fallisce.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AscoltoScreen(vm: TedescoViewModel, profilo: ProfiloUtente? = null, onFatto: () -> Unit = {}) {
    val livelloProfilo = profilo?.stato?.progresso?.livelloCorrente?.codiceBreve()
    val livelli = remember(profilo) {
        (AscoltoData.livelliDisponibili(profilo) + listOfNotNull(livelloProfilo))
            .distinct()
            .sorted()
    }
    var livelloSelezionato by remember(profilo) {
        mutableStateOf(
            livelloProfilo?.takeIf { it in livelli } ?: livelli.firstOrNull() ?: "A1"
        )
    }

    // Giro corrente: cambiarlo ricostruisce la serie locale (mescolata).
    var giro by remember { mutableIntStateOf(0) }
    var indice by remember { mutableIntStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableIntStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var mostraTraduzione by remember { mutableStateOf(false) }

    var eserciziAi by remember { mutableStateOf<List<EsercizioAscolto>>(emptyList()) }
    var generaInCorso by remember { mutableStateOf(false) }
    var erroreGen by remember { mutableStateOf<String?>(null) }

    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    // Serie locale: sempre disponibile, nessuna rete.
    val eserciziLocali = remember(livelloSelezionato, profilo, giro) {
        val esatti = AscoltoData.eserciziPerLivello(livelloSelezionato, profilo)
        if (esatti.isNotEmpty()) esatti.shuffled()
        else {
            // Livello senza esercizi locali (es. B2): si usa il più alto disponibile.
            val disponibili = AscoltoData.livelliDisponibili(profilo)
            val ripiego = disponibili.lastOrNull { it <= livelloSelezionato }
                ?: disponibili.lastOrNull().orEmpty()
            AscoltoData.eserciziPerLivello(ripiego, profilo).shuffled()
        }
    }

    val esercizi = remember(eserciziLocali, eserciziAi) {
        // Prima per id, poi per contenuto: l'AI può riproporre la stessa
        // domanda sulla stessa frase con un id nuovo.
        DeduplicaEsercizi.distinti(
            (eserciziAi + eserciziLocali).distinctBy { it.id }
        ) { DeduplicaEsercizi.chiave(it.domanda, it.fraseTedesca) }
    }

    // Reset quando cambia livello o serie: niente punteggi "ereditati".
    LaunchedEffect(livelloSelezionato, giro) {
        indice = 0
        rispostaSelezionata = null
        risultato = null
        punteggio = 0
        completato = false
        mostraTraduzione = false
    }

    fun generaConAi() {
        if (generaInCorso) return
        generaInCorso = true
        erroreGen = null
        scope.launch {
            try {
                val generati: List<EsercizioGenerato> = vm.generaEsercizi(
                    TipoEsercizio.ASCOLTO,
                    quanti = 8
                )
                if (generati.isNotEmpty()) {
                    val nuovi = generati.map { esercizioGeneratoAAscolto(it) }
                    eserciziAi = (nuovi + eserciziAi).distinctBy { it.id }
                    indice = 0
                    rispostaSelezionata = null
                    risultato = null
                    punteggio = 0
                    completato = false
                    mostraTraduzione = false
                } else {
                    erroreGen = "Nessun esercizio generato. La serie locale resta disponibile."
                }
            } catch (e: Throwable) {
                // Throwable: un errore di rete non deve chiudere l'app.
                erroreGen = e.localizedMessage ?: e.javaClass.simpleName
            } finally {
                generaInCorso = false
            }
        }
    }

    ScreenScaffold(
        title = "Ascolto (Hörverstehen)",
        scrollable = true
    ) {
        // Nessun verticalScroll qui: ScreenScaffold avvolge gia' il contenuto in
        // uno scroll verticale (due scroll annidati fanno crashare Compose).
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(Spaziature.md)
        ) {
            // Selettore livello: stabile, costruito dai livelli realmente disponibili.
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                livelli.forEach { livello ->
                    val selezionato = livello == livelloSelezionato
                    Card(
                        onClick = { livelloSelezionato = livello },
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(
                            containerColor = if (selezionato) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            }
                        )
                    ) {
                        Text(
                            text = livello,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = if (selezionato) FontWeight.Bold else FontWeight.Normal,
                            color = if (selezionato) {
                                MaterialTheme.colorScheme.onPrimary
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            if (!completato && esercizi.isEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            "Nessun esercizio per ${livelloSelezionato}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Scegli un altro livello oppure genera una serie nuova.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Button(onClick = { giro++ }, modifier = Modifier.fillMaxWidth()) {
                            Text("Ricarica esercizi")
                        }
                    }
                }
            }

            if (!completato && esercizi.isNotEmpty() && indice in esercizi.indices) {
                val esercizio = esercizi[indice]

                Text(
                    text = "Esercizio ${indice + 1}/${esercizi.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(Spaziature.md),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Ascolta la frase in tedesco:",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(8.dp))
                        // Alcuni esercizi generati dall'IA non hanno una frase
                        // dedicata: in quel caso si legge la domanda, altrimenti
                        // il bottone parlerebbe una stringa vuota (nessun suono).
                        val testoAudio = esercizio.fraseTedesca.ifBlank { esercizio.domanda }
                        Button(
                            onClick = { ttsHelper.speak(testoAudio) },
                            enabled = testoAudio.isNotBlank(),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Ascolta"
                            )
                            Spacer(Modifier.width(6.dp))
                            Text("Ascolta")
                        }
                        if (mostraTraduzione) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = esercizio.traduzioneItaliana,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        } else {
                            Spacer(Modifier.height(4.dp))
                            TextButton(onClick = { mostraTraduzione = true }) {
                                Text("Mostra traduzione", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium
                ) {
                    Text(
                        text = esercizio.domanda,
                        style = MaterialTheme.typography.bodyLarge,
                        modifier = Modifier.padding(Spaziature.md)
                    )
                }

                esercizio.opzioni.forEachIndexed { index, opzione ->
                    val selezionato = rispostaSelezionata == index
                    val colore = when {
                        risultato == null -> MaterialTheme.colorScheme.surface
                        index == esercizio.rispostaCorretta -> MaterialTheme.colorScheme.primaryContainer
                        selezionato -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surface
                    }

                    Card(
                        onClick = {
                            if (risultato == null) {
                                rispostaSelezionata = index
                                val giusto = index == esercizio.rispostaCorretta
                                risultato = giusto
                                if (giusto) punteggio++
                                // Solo gli esercizi AI hanno un id da segnare come
                                // completato: per quelli locali evitiamo una scrittura
                                // su DataStore a ogni risposta.
                                if (eserciziAi.any { it.id == esercizio.id }) {
                                    vm.segnaEsercizioCompletato(TipoEsercizio.ASCOLTO, esercizio.id)
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = colore)
                    ) {
                        Text(
                            text = opzione,
                            modifier = Modifier.padding(Spaziature.md)
                        )
                    }
                }

                if (risultato != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (risultato == true) {
                                MaterialTheme.colorScheme.secondaryContainer
                            } else {
                                MaterialTheme.colorScheme.errorContainer
                            }
                        )
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Text(
                                text = if (risultato == true) "Corretto! 🎉" else "Sbagliato",
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                text = if (risultato == true) "Ottimo lavoro!" else "Riprova, puoi farcela.",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(text = esercizio.spiegazione)
                        }
                    }

                    Button(
                        onClick = {
                            if (indice < esercizi.lastIndex) {
                                indice += 1
                                rispostaSelezionata = null
                                risultato = null
                                mostraTraduzione = false
                            } else {
                                completato = true
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (indice < esercizi.lastIndex) "Prossimo" else "Vedi risultato")
                    }
                }
            } else if (completato) {
                val eserciziTotali = esercizi.size.coerceAtLeast(1)
                val punteggioPct = punteggio * 100 / eserciziTotali
                val errori = eserciziTotali - punteggio

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
                            text = "Punteggio: $punteggio/$eserciziTotali",
                            style = MaterialTheme.typography.headlineMedium
                        )
                        Text(
                            text = "$punteggioPct%",
                            style = MaterialTheme.typography.displaySmall
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Errori: $errori",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                LaunchedEffect(completato) {
                    if (completato) {
                        vm.salvaTestAscolto(
                            punteggio = punteggioPct.toFloat(),
                            errori = errori,
                            totale = eserciziTotali,
                            livello = livelloSelezionato
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                Button(
                    onClick = {
                        giro++
                        indice = 0
                        rispostaSelezionata = null
                        risultato = null
                        punteggio = 0
                        completato = false
                        mostraTraduzione = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Nuova serie")
                }
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onFatto,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Fatto")
                }
            }

            // AI: extra, non prerequisito. Non parte da sola: risparmia rete e batteria.
            if (!completato) {
                Spacer(Modifier.height(4.dp))
                Box(modifier = Modifier.fillMaxWidth()) {
                    if (generaInCorso) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(modifier = Modifier.height(18.dp).width(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "Genero esercizi nuovi…",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        TextButton(onClick = { generaConAi() }) {
                            Text("Genera esercizi nuovi con AI")
                        }
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    if (erroreGen != null) {
        AlertDialog(
            onDismissRequest = { erroreGen = null },
            title = { Text("AI non disponibile") },
            text = {
                Text(
                    "${erroreGen}\n\nGli esercizi locali funzionano comunque: " +
                        "puoi continuare senza connessione."
                )
            },
            confirmButton = { TextButton(onClick = { erroreGen = null }) { Text("OK") } }
        )
    }
}

/** Conversione EsercizioGenerato (AI) -> EsercizioAscolto. */
fun esercizioGeneratoAAscolto(g: EsercizioGenerato): EsercizioAscolto = EsercizioAscolto(
    id = g.id,
    fraseTedesca = g.fraseTedesca ?: "",
    traduzioneItaliana = g.traduzioneItaliana ?: "",
    domanda = g.domanda,
    opzioni = g.opzioni,
    rispostaCorretta = g.rispostaCorretta.coerceIn(0, g.opzioni.lastIndex.coerceAtLeast(0)),
    spiegazione = g.spiegazione,
    livello = g.livello
)
