package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.URLEncoder
import com.alessandro.tedesco.data.GrammaticaB1
import com.alessandro.tedesco.data.DeduplicaEsercizi
import com.alessandro.tedesco.data.EsercizioGrammatica
import com.alessandro.tedesco.data.toEsercizioGrammatica
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TipoEsercizio
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.CardDefaults
import com.alessandro.tedesco.data.CategoriaGrammatica
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

private fun etichettaCategoria(categoria: CategoriaGrammatica): String = when (categoria) {
    CategoriaGrammatica.VERBI_TEMPI -> "Verbi e tempi"
    CategoriaGrammatica.VERBI_MODALI -> "Verbi modali"
    CategoriaGrammatica.PASSIV -> "Passivo"
    CategoriaGrammatica.KONJUNKTIV_II -> "Konjunktiv II"
    CategoriaGrammatica.RELATIVSATZ -> "Frasi relative"
    CategoriaGrammatica.KONNEKTOREN -> "Congiunzioni"
    CategoriaGrammatica.PRAEPOSITIONEN -> "Preposizioni"
    CategoriaGrammatica.ADJEKTIVDEKLINATION -> "Declinazione aggettivi"
    CategoriaGrammatica.ARTICOLI -> "Articoli"
    CategoriaGrammatica.INFINITIV_ZU -> "Infinito con zu"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GrammaticaScreen(vm: TedescoViewModel, onFatto: () -> Unit = {}) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val eserciziGen by vm.eserciziGenGrammatica.collectAsStateWithLifecycle()
    var generaInCorso by remember { mutableStateOf(false) }
    var erroreGen by remember { mutableStateOf<String?>(null) }
    var esercizi by remember { mutableStateOf<List<EsercizioGrammatica>>(emptyList()) }
    var caricato by remember { mutableStateOf(false) }
    var indice by remember { mutableStateOf(0) }
    var tentativo by remember { mutableIntStateOf(0) }

    // Batch SEMPRE fresco: nessun set statico riciclato. Se la generazione fallisce
    // si riusa l'ultimo batch AI non ancora completato, altrimenti si mostra "Riprova".
    LaunchedEffect(tentativo) {
        caricato = false
        erroreGen = null
        esercizi = emptyList()
        vm.caricaEserciziGenerati()
        val freschi = try {
            vm.generaEsercizi(TipoEsercizio.GRAMMATICA, quanti = 10)
        } catch (e: Throwable) {
            erroreGen = e.localizedMessage ?: "Errore di rete"
            emptyList()
        }
        // Deduplica per contenuto: stessa domanda ripetuta nel batch = un solo
        // esercizio (gli id generati sono sempre nuovi, quindi non bastano).
        val freschiUnici = DeduplicaEsercizi.distinti(freschi) {
            DeduplicaEsercizi.chiave(it.domanda, it.fraseTedesca)
        }
        esercizi = if (freschiUnici.isNotEmpty()) {
            freschiUnici.map {
                EsercizioGrammatica(
                    id = it.id,
                    categoria = CategoriaGrammatica.perLivello(livello).firstOrNull() ?: CategoriaGrammatica.VERBI_TEMPI,
                    lezione = lezioneCorrente,
                    domanda = it.domanda,
                    opzioni = it.opzioni,
                    rispostaCorretta = it.rispostaCorretta,
                    spiegazione = it.spiegazione,
                    esempio = it.spiegazione
                )
            }
        } else {
            DeduplicaEsercizi.distinti(eserciziGen.filterNot { it.completato }) {
                DeduplicaEsercizi.chiave(it.domanda, it.fraseTedesca)
            }.map { it.toEsercizioGrammatica(lezioneCorrente, livello) }
        }
        indice = 0
        caricato = true
    }
    var rispostaSelezionata by remember { mutableStateOf<Int?>(null) }
    var risultato by remember { mutableStateOf<Boolean?>(null) }
    var punteggio by remember { mutableStateOf(0) }
    var completato by remember { mutableStateOf(false) }
    var testoTraduzione by remember { mutableStateOf<String?>(null) }
    var traduzione by remember { mutableStateOf("") }
    var caricamentoTraduzione by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val erroriRef = remember { mutableListOf<String>() }

    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    ScreenScaffold("Grammatica") {

            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                if (!caricato) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator()
                            Spacer(Modifier.height(12.dp))
                            Text("Genero esercizi nuovi...")
                        }
                    }
                } else if (esercizi.isEmpty() && !completato) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
                } else if (!completato && indice in esercizi.indices) {
                    val esercizio = esercizi[indice]

                    // Categoria
                    AssistChip(
                        onClick = { },
                        label = { Text(etichettaCategoria(esercizio.categoria)) }
                    )

                    // Domanda con traduttore e TTS
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.medium
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = esercizio.domanda,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { ttsHelper.speak(esercizio.domanda) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Ascolta domanda",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        testoTraduzione = esercizio.domanda
                                        caricamentoTraduzione = true
                                        scope.launch {
                                            try {
                                                traduzione = withContext(Dispatchers.IO) {
                                                    traduci(esercizio.domanda)
                                                }
                                            } catch (e: Throwable) {
                                                traduzione = "Errore: ${e.message}"
                                            } finally {
                                                caricamentoTraduzione = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Translate,
                                        contentDescription = "Traduci",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                            if (testoTraduzione == esercizio.domanda && traduzione.isNotBlank()) {
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    text = traduzione,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }

                    // Opzioni con traduttore e TTS
                    esercizio.opzioni.forEachIndexed { index, opzione ->
                        val selezionato = rispostaSelezionata == index
                        val colore = when {
                            risultato == null -> MaterialTheme.colorScheme.surface
                            index == esercizio.rispostaCorretta -> MaterialTheme.colorScheme.primary
                            selezionato -> MaterialTheme.colorScheme.error
                            else -> MaterialTheme.colorScheme.surface
                        }

                        Card(
                            onClick = {
                                if (risultato == null) {
                                    rispostaSelezionata = index
                                    risultato = index == esercizio.rispostaCorretta
                                    if (risultato == true) {
                                        punteggio++
                                    } else {
                                        erroriRef.add(esercizio.domanda)
                                        vm.registraErrore(
                                            tipo = "grammatica",
                                            domanda = esercizio.domanda,
                                            rispostaCorretta = esercizio.opzioni.getOrElse(esercizio.rispostaCorretta) { "" },
                                            rispostaData = esercizio.opzioni.getOrElse(index) { "" },
                                            spiegazione = esercizio.spiegazione,
                                            livello = livello.substringBefore(" ").trim()
                                        )
                                    }
                                    vm.segnaEsercizioCompletato(TipoEsercizio.GRAMMATICA, esercizio.id)
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = colore)
                        ) {
                            Row(
                                modifier = Modifier.padding(Spaziature.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = opzione,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { ttsHelper.speak(opzione) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Ascolta opzione",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        testoTraduzione = opzione
                                        caricamentoTraduzione = true
                                        scope.launch {
                                            try {
                                                traduzione = withContext(Dispatchers.IO) {
                                                    traduci(opzione)
                                                }
                                            } catch (e: Throwable) {
                                                traduzione = "Errore: ${e.message}"
                                            } finally {
                                                caricamentoTraduzione = false
                                            }
                                        }
                                    }
                                ) {
                                    Icon(
                                        Icons.Filled.Translate,
                                        contentDescription = "Traduci",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }

                    // Spiegazione con traduzione
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
                                    text = if (risultato == true) "✓ Corretto! 🎉" else "✗ Sbagliato",
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (risultato == true) "Ottimo!" else "Riprova.",
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(text = esercizio.spiegazione)
                                Text(
                                    text = esercizio.esempio,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                                Spacer(Modifier.height(8.dp))
                                var mostraTraduzione by remember { mutableStateOf(false) }
                                var traduzioneEsempio by remember { mutableStateOf("") }
                                if (!mostraTraduzione) {
                                    TextButton(
                                        onClick = {
                                            mostraTraduzione = true
                                            caricamentoTraduzione = true
                                            scope.launch {
                                                try {
                                                    traduzioneEsempio = withContext(Dispatchers.IO) {
                                                        traduci(esercizio.esempio)
                                                    }
                                                } catch (e: Throwable) {
                                                    traduzioneEsempio = "Errore: ${e.message}"
                                                } finally {
                                                    caricamentoTraduzione = false
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Filled.Translate, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("Mostra traduzione")
                                    }
                                } else {
                                    Text(
                                        text = traduzioneEsempio,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = {
                                val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + esercizio.id
                                var next = indice + 1
                                while (next < esercizi.size && esercizi[next].id in idsCompletati) next++
                                if (next < esercizi.size) {
                                    indice = next
                                    rispostaSelezionata = null
                                    risultato = null
                                } else {
                                    completato = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            val idsCompletati = eserciziGen.filter { it.completato }.map { it.id }.toSet() + esercizio.id
                            var next = indice + 1
                            while (next < esercizi.size && esercizi[next].id in idsCompletati) next++
                            Text(if (next < esercizi.size) "Prossimo" else "Vedi risultato")
                        }
                    }
                } else {
                    // Risultato finale
                    val punteggioPct = if (esercizi.isNotEmpty()) (punteggio * 100 / esercizi.size) else 0
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
                                text = "Punteggio: $punteggio/${esercizi.size}",
                                style = MaterialTheme.typography.headlineMedium
                            )
                            Text(
                                text = "$punteggioPct%",
                                style = MaterialTheme.typography.displaySmall
                            )
                            if (erroriRef.isNotEmpty()) {
                                Spacer(Modifier.height(12.dp))
                                Text(
                                    text = "Errori da ripassare: ${erroriRef.size}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }

                    // Salva risultato e errori
                    LaunchedEffect(completato) {
                        if (completato) {
                            vm.salvaTestGrammatica(
                                punteggio = punteggioPct.toFloat(),
                                errori = erroriRef.size,
                                totale = esercizi.size,
                                domandeErrate = erroriRef.toList()
                            )
                        }
                    }

                    // Fine sessione: torna ad Altro; rientrando si generano 10 esercizi nuovi.
                    Button(
                        onClick = onFatto,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Fatto")
                    }
                    if (erroreGen != null) {
                        Text(
                            text = erroreGen ?: "",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp)
                        )
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

private fun traduci(testo: String): String {
    val url = "https://api.mymemory.translated.net/get?q=" +
            URLEncoder.encode(testo, "UTF-8") + "&langpair=de|it"
    val risultato = java.net.URL(url).readText()
    val json = kotlinx.serialization.json.Json.parseToJsonElement(risultato).jsonObject
    val translated = json["responseData"]?.jsonObject?.get("translatedText")?.jsonPrimitive?.content
        ?: throw IOException("Traduzione non disponibile")
    if (translated.startsWith("MYMEMORY WARNING") || translated.startsWith("QUERY LENGTH LIMIT")) {
        throw IOException("Limite di traduzione raggiunto, riprova più tardi")
    }
    return translated
}
