package com.alessandro.tedesco.ui

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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.DomandaTest
import com.alessandro.tedesco.data.DeduplicaEsercizi
import com.alessandro.tedesco.data.remote.EsercizioGenerato
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.data.local.toDomandaTest
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.TipoDomanda
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduzioneScreen(vm: TedescoViewModel, onFatto: () -> Unit = {}) {
    val eserciziGen by vm.eserciziGenProduzione.collectAsStateWithLifecycle()
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle()
    val livelloUtente = profilo?.stato?.progresso?.livelloCorrente ?: LivelloCEFR.A0
    var elenco by remember { mutableStateOf<List<DomandaTest>>(emptyList()) }
    var caricato by remember { mutableStateOf(false) }
    var tentativo by remember { mutableIntStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var indice by remember { mutableIntStateOf(0) }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableIntStateOf(0) }
    var erroreGen by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Batch SEMPRE fresco: nessun set statico riciclato. Se la generazione fallisce
    // si riusa l'ultimo batch generato dall'AI non ancora completato; se non c'e'
    // nemmeno quello, si mostra l'errore con "Riprova".
    LaunchedEffect(tentativo) {
        caricato = false
        erroreGen = null
        elenco = emptyList()
        vm.caricaEserciziGenerati()
        val freschi: List<EsercizioGenerato> = try {
            vm.generaEsercizi(TipoEsercizio.PRODUZIONE, quanti = 10)
        } catch (e: Throwable) {
            erroreGen = e.localizedMessage ?: "Errore di rete"
            emptyList()
        }
        // Deduplica per contenuto: l'AI può restituire la stessa domanda più
        // volte (id nuovi, testo identico). Senza questo la schermata mostrava
        // 18 domande che in realtà erano 5 ripetute.
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
                    tagGrammatica = null
                )
            }
        } else {
            DeduplicaEsercizi.distinti(eserciziGen.filterNot { it.completato }) {
                DeduplicaEsercizi.chiave(it.domanda, it.fraseTedesca)
            }.map { it.toDomandaTest() }
        }
        indice = 0
        rispostaSelezionata = null
        risultato = null
        caricato = true
    }

    // Stato vuoto: la generazione non ha prodotto nulla. Niente fallback statico.
    if (elenco.isEmpty() && caricato && !completato) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(12.dp)
            ) {
                Text("Nessun esercizio generato", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))
                Text(
                    erroreGen ?: "Controlla la connessione e riprova.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = { tentativo++ }) { Text("Riprova") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onFatto) { Text("Torna indietro") }
            }
        }
    }

    if (caricato && !completato && elenco.isNotEmpty() && indice in elenco.indices) {
        val domanda = elenco[indice]
        val totaleGen = eserciziGen.size

        // Spiegazione + pulsanti (solo quando la risposta e' fatta)
        val mostraSpiegazione = risultato != null

        // Prossimo: salta quelli gia' completati (in banco)
        val idsCompletati = remember(eserciziGen, domanda.id) {
            eserciziGen.filter { it.completato }.map { it.id }.toSet() + domanda.id
        }
        var prossimoIdx by remember { mutableIntStateOf(0) }
        val puoiPassare = remember(indice, elenco, idsCompletati) {
            var next = indice + 1
            while (next < elenco.size && elenco[next].id in idsCompletati) next++
            next
        }

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!caricato) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Genero esercizi nuovi...")
                        }
                    }
                } else if (!completato) {
                    // Progress + livello: si vede che il quiz e' calibrato sul profilo
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Domanda ${indice + 1}/${elenco.size}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Livello ${livelloUtente.codiceBreve()}",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.height(14.dp))

                    // Domanda
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Text(
                            text = domanda.domanda,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.padding(Spaziature.md)
                        )
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
                                    if (risultato == true) punteggio++
                                    vm.segnaEsercizioCompletato(TipoEsercizio.PRODUZIONE, domanda.id)
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

                    // Spiegazione
                    if (mostraSpiegazione) {
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
                                    text = if (risultato == true) "✓ Corretto! 🎉" else "✗ Sbagliato",
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = if (risultato == true) "Ottimo!" else "Riprova.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(text = domanda.spiegazione)
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    vm.eliminaEsercizioGenerato(TipoEsercizio.PRODUZIONE, domanda.id)
                                    elenco = elenco.filterNot { it.id == domanda.id }
                                    rispostaSelezionata = null
                                    risultato = null
                                    if (indice >= elenco.size) indice = (elenco.size - 1).coerceAtLeast(0)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Elimina")
                            }
                            Button(
                                onClick = {
                                    if (puoiPassare < elenco.size) {
                                        indice = puoiPassare
                                        rispostaSelezionata = null
                                        risultato = null
                                    } else {
                                        completato = true
                                    }
                                },
                                modifier = Modifier.weight(2f)
                            ) {
                                Text(if (puoiPassare < elenco.size) "Prossimo" else "Vedi risultato")
                            }
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
                                text = "Punteggio: $punteggio/${elenco.size}",
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
                            vm.salvaTestProduzione(
                                punteggio = punteggioPct.toFloat(),
                                errori = errori,
                                totale = elenco.size
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Button(
                        onClick = onFatto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Fatto")
                    }
                }
            }
        }
    }

    // Dialogo errore chiave API per generazione esercizi infiniti
    if (erroreGen?.contains("Chiave API") == true) {
        AlertDialog(
            onDismissRequest = { erroreGen = null },
            title = { Text("Chiave API necessaria") },
            text = { Text("Per generare esercizi infiniti inserisci una chiave Groq gratuita in Profilo → Tutor AI.") },

            confirmButton = { TextButton(onClick = { erroreGen = null }) { Text("OK") } }
        )
    }
}