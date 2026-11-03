package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.DeduplicaEsercizi
import com.alessandro.tedesco.ui.theme.Spaziature
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TipoEsercizio
import com.alessandro.tedesco.data.local.toDomandaTest
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.launch

data class EsercizioScrittura(
    val id: String,
    val titolo: String,
    val istruzioni: String,
    val esempio: String,
    val suggerimenti: List<String>
)

fun getEserciziScrittura(profilo: ProfiloUtente?): List<EsercizioScrittura> {
    val base = listOf(
        EsercizioScrittura(
            id = "sc1",
            titolo = "Presentazione personale",
            istruzioni = "Scrivi 3-4 frasi su di te: come ti chiami, da dove vivi, cosa fai.",
            esempio = if (profilo?.config?.mostraContestoMedico == true)
                "Ich heiße Alessandro. Ich wohne in Bozen. Ich bin Physiotherapeut. Ich lerne Deutsch."
            else
                "Ich heiße Emma. Ich wohne in Bozen. Ich bin Studentin. Ich lerne Deutsch.",
            suggerimenti = listOf("Nome", "Città", "Lavoro/Studi", "Hobby")
        ),
        EsercizioScrittura(
            id = "sc2",
            titolo = "La tua giornata",
            istruzioni = "Descrivi la tua giornata tipica: quando ti alzi, cosa fai, quando lavori/studi.",
            esempio = "Ich stehe um 7 Uhr auf. Ich frühstücke um 8 Uhr. Ich arbeite von 9 bis 17 Uhr.",
            suggerimenti = listOf("Ore", "Attività", "Lavoro/Studio", "Tempo libero")
        ),
        EsercizioScrittura(
            id = "sc3",
            titolo = "Un messaggio al collega",
            istruzioni = "Scrivi un messaggio breve a un collega/compagno per chiedere un favore.",
            esempio = "Hallo Marco, kannst du mir bitte helfen? Ich habe eine Frage. Danke!",
            suggerimenti = listOf("Saluto", "Richiesta", "Ringraziamento")
        ),
        EsercizioScrittura(
            id = "sc5",
            titolo = "Email formale",
            istruzioni = "Scrivi un'email formale per chiedere un appuntamento.",
            esempio = "Sehr geehrte Frau Müller, ich möchte einen Termin vereinbaren. Vielen Dank.",
            suggerimenti = listOf("Oggetto", "Saluto formale", "Richiesta", "Chiusura")
        )
    )
    // Esercizio studio fisioterapia solo per profili medici
    if (profilo?.config?.mostraContestoMedico == true) {
        return base + EsercizioScrittura(
            id = "sc4",
            titolo = "Descrivi il tuo studio",
            istruzioni = "Descrivi il tuo studio di fisioterapia: dove è, cosa c'è dentro, come è organizzato.",
            esempio = "Mein Studio ist in der Stadtmitte. Es gibt ein Behandlungszimmer und ein Wartezimmer.",
            suggerimenti = listOf("Posizione", "Stanze", "Attrezzatura", "Personale")
        )
    }
    return base
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProduzioneScrittaScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit,
    profilo: ProfiloUtente? = null,
    onVaiACorrezioni: () -> Unit = {}
) {
    // Esercizi generati dall'AI (persistiti nel profilo)
    val eserciziGen by vm.eserciziGenProduzione.collectAsStateWithLifecycle()
    var esercizioSelezionato by remember { mutableStateOf<EsercizioScrittura?>(null) }
    var testoUtente by remember { mutableStateOf("") }
    var mostraCorrezione by remember { mutableStateOf(false) }
    val correzioneInCorso by vm.correzioneInCorso.collectAsStateWithLifecycle(false)
    val risposte by vm.risposte.collectAsStateWithLifecycle()
    val apiKey by vm.tutorApiKey.collectAsStateWithLifecycle("")
    // Ultima correzione ricevuta dal tutor per il testo inviato da questa schermata
    val ultimaCorrezione = risposte.firstOrNull { it.testo.trim() == testoUtente.trim() }?.correzione
    var generaInCorso by remember { mutableStateOf(false) }
    var erroreGen by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    // Lista di base + compiti d'esame Goethe/telc + esercizi generati dall'AI
    val listaBase = getEserciziScrittura(profilo)
    val taskEsame = com.alessandro.tedesco.data.TaskB1.scrittura.map { task ->
        EsercizioScrittura(
            id = task.id,
            titolo = "Esame B1: ${task.titolo}",
            istruzioni = task.istruzioni(),
            esempio = "",
            suggerimenti = task.punti
        )
    }
    val eserciziGeneratiConvenieti = DeduplicaEsercizi.distinti(eserciziGen) {
        DeduplicaEsercizi.chiave(it.domanda)
    }.map { gen ->
        EsercizioScrittura(
            id = gen.id,
            titolo = "Generato dall'AI",
            istruzioni = gen.domanda,
            esempio = gen.spiegazione,
            suggerimenti = gen.opzioni
        )
    }
    // Nota: la generazione avviene on-demand; carichiamo al primo entrata
    LaunchedEffect(Unit) { vm.caricaEserciziGenerati() }

    ScreenScaffold("Produzione Scritta", onBack = onIndietro, scrollable = false) {
        // Val locale: in questo modo lo smart cast funziona e non serve "!!"
        // (con un delegato 'var' il ramo else poteva leggere un valore gia' nullo).
        val esercizioCorrente = esercizioSelezionato

        if (esercizioCorrente == null) {
            // Lista esercizi
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(Spaziature.md)
            ) {
                item {
                    Text(
                        text = "Scegli un esercizio di scrittura",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = Spaziature.md)
                    )
                    // Genera nuovo esercizio infinito
                    Button(
                        onClick = {
                            scope.launch {
                                generaInCorso = true
                                erroreGen = null
                                runCatching {
                                    vm.generaEsercizi(TipoEsercizio.PRODUZIONE, quanti = 1)
                                }.onSuccess { generaInCorso = false }
                                  .onFailure { e ->
                                      generaInCorso = false
                                      erroreGen = e.message ?: "Errore generazione"
                                  }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !generaInCorso
                    ) {
                        if (generaInCorso) {
                            CircularProgressIndicator(
                                modifier = Modifier.height(20.dp).width(20.dp),
                                color = MaterialTheme.colorScheme.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                        }
                        Text(if (generaInCorso) "Generazione..." else "Genera nuovo esercizio")
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

                items(listaBase + taskEsame + eserciziGeneratiConvenieti) { esercizio ->
                    Card(
                        onClick = { esercizioSelezionato = esercizio },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(Spaziature.md),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(Spaziature.md))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = esercizio.titolo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = esercizio.istruzioni,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        } else {
            // Schermata esercizio
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header esercizio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = esercizioCorrente.titolo,
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(Spaziature.sm))
                        Text(
                            text = esercizioCorrente.istruzioni,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Esempio
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = "Esempio:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = esercizioCorrente.esempio,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Suggerimenti
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(Spaziature.md)) {
                        Text(
                            text = "Suggerimenti:",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        esercizioCorrente.suggerimenti.forEach { suggerimento ->
                            Row(
                                modifier = Modifier.padding(vertical = 2.dp)
                            ) {
                                Text("• ", style = MaterialTheme.typography.bodyMedium)
                                Text(suggerimento, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(Spaziature.md))

                // Campo di testo
                OutlinedTextField(
                    value = testoUtente,
                    onValueChange = { testoUtente = it },
                    label = { Text("Scrivi qui in tedesco...") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    maxLines = 10
                )

                Spacer(Modifier.height(Spaziature.md))

                // Pulsanti
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(Spaziature.md)
                ) {
                    OutlinedButton(
                        onClick = { esercizioSelezionato = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Indietro")
                    }
                    Button(
                        onClick = {
                            mostraCorrezione = true
                            vm.completaPassoGiornaliero("produzione")
                            // Invia DAVVERO il testo al tutor per la correzione
                            // (prima si limitava a mostrare un messaggio fisso).
                            vm.inviaRisposta(testoUtente)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = testoUtente.isNotBlank() && !correzioneInCorso
                    ) {
                        Text(if (correzioneInCorso) "Correggo…" else "Verifica")
                    }
                }

                // Correzione
                if (mostraCorrezione) {
                    Spacer(Modifier.height(Spaziature.md))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(Spaziature.md)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(Modifier.width(Spaziature.sm))
                                Text(
                                    text = "Correzione automatica",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Ogni testo scritto è un passo avanti.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            when {
                                correzioneInCorso -> {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                        Spacer(Modifier.width(Spaziature.sm))
                                        Text("Il tutor sta correggendo il tuo testo…",
                                            style = MaterialTheme.typography.bodyMedium)
                                    }
                                }
                                ultimaCorrezione != null -> {
                                    // CORREZIONE REALE dal tutor AI, con markdown renderizzato
                                    MarkdownText(
                                        markdown = ultimaCorrezione,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer
                                    )
                                    Spacer(Modifier.height(Spaziature.sm))
                                    TextButton(onClick = onVaiACorrezioni) {
                                        Text("Vedi tutte le correzioni")
                                    }
                                }
                                apiKey.isBlank() -> {
                                    Text(
                                        text = "Per ricevere la correzione serve una chiave API Groq gratuita " +
                                            "(Profilo → Tutor AI). Il testo e' stato salvato.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                                else -> {
                                    Text(
                                        text = "Correzione non disponibile al momento. Riprova.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            Text(
                                text = "Suggerimento: rileggi il testo ad alta voce per trovare errori.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }  // chiude Scaffold

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
