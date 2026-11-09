package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.TutorService
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

data class MessaggioRoleplay(
    val testo: String,
    val daUtente: Boolean,
    val errore: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoleplayScreen(vm: TedescoViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val apiKey by vm.tutorApiKey.collectAsStateWithLifecycle("")
    val modelloTutor by vm.modelloTutor.collectAsStateWithLifecycle(null)
    val scenario by vm.roleplayScenario.collectAsStateWithLifecycle(null)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val scenarioCorrente = scenario
    var messaggi by remember {
        mutableStateOf(
            listOf(
                MessaggioRoleplay(
                    testo = if (scenarioCorrente != null)
                        "Hai scelto: ${scenarioCorrente.titolo}. ${scenarioCorrente.situazione} Il tuo ruolo: ${scenarioCorrente.ruoloUtente}. Il mio ruolo: ${scenarioCorrente.ruoloAI}. Iniziamo!"
                    else
                        t("Scegli uno scenario per iniziare il gioco di ruolo."),
                    daUtente = false
                )
            )
        )
    }
    var input by remember { mutableStateOf("") }
    var caricamento by remember { mutableStateOf(false) }
    var rispostaAnimata by remember { mutableStateOf("") }
    var animazioneAttiva by remember { mutableStateOf(false) }
    var rispostaDaAnimare by remember { mutableStateOf("") }

    fun invia(testo: String) {
        val t = testo.trim()
        if (t.isBlank() || caricamento) return
        messaggi = messaggi + MessaggioRoleplay(t, daUtente = true)
        input = ""
        caricamento = true

        scope.launch {
            val cronologia = messaggi
                .filterNot { it.errore }
                .map { if (it.daUtente) "utente" to it.testo else "tutor" to it.testo }

            val risultato = vm.chiediAlRoleplay(cronologia, livello, lezioneCorrente)
            risultato.fold(
                onSuccess = { risposta ->
                    messaggi = messaggi + MessaggioRoleplay(risposta, daUtente = false)
                    rispostaAnimata = ""
                    rispostaDaAnimare = risposta
                    animazioneAttiva = true
                },
                onFailure = { e ->
                    messaggi = messaggi + MessaggioRoleplay(e.message ?: t("Errore sconosciuto"), daUtente = false, errore = true)
                }
            )
            caricamento = false
        }
    }

    // Animazione di digitazione
    LaunchedEffect(animazioneAttiva, rispostaDaAnimare) {
        if (animazioneAttiva && rispostaDaAnimare.isNotEmpty()) {
            val lunghezza = rispostaDaAnimare.length
            for (i in 0..lunghezza step 3) {
                rispostaAnimata = rispostaDaAnimare.substring(0, i.coerceAtMost(lunghezza))
                delay(15)
            }
            rispostaAnimata = rispostaDaAnimare
            animazioneAttiva = false
        }
    }

    LaunchedEffect(messaggi.size) {
        if (messaggi.isNotEmpty()) listState.animateScrollToItem(messaggi.lastIndex)
    }

    Scaffold(
        topBar = {
            Surface(
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().statusBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { vm.terminaRoleplay() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Indietro"))
                    }
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.AutoMirrored.Filled.Chat,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            t("Gioco di ruolo"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            scenario?.titolo ?: t("Scegli uno scenario"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            if (apiKey.isBlank()) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = spaziaturaSchermo(), vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "Per il gioco di ruolo serve una chiave API Groq. " +
                            t("Vai in Profilo → Tutor AI e inseriscila."),
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }

            // Selettore modello
            ModelloSelector(
                modello = modelloTutor,
                onModelloCambiato = { vm.cambiaModelloTutor(it) }
            )

            if (scenarioCorrente == null) {
                // Selezione scenario
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spaziaturaSchermo()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(TutorService.SCENARI) { s ->
                        Card(
                            onClick = { vm.avviaRoleplay(s) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(12.dp).animateContentSize()) {
                                Text(
                                    s.titolo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    s.descrizione,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    "Tu: ${s.ruoloUtente} · Io: ${s.ruoloAI}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            } else {
                // Conversazione
                LazyColumn(
                    state = listState,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(horizontal = spaziaturaSchermo()),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(messaggi) { msg ->
                        MessaggioBubble(
                            msg = msg,
                            isUltima = msg == messaggi.last(),
                            animazioneAttiva = animazioneAttiva,
                            rispostaAnimata = rispostaAnimata
                        )
                    }
                    if (caricamento) {
                        item {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    t("Sto scrivendo…"),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                Surface(tonalElevation = 3.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .padding(8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = input,
                            onValueChange = { input = it },
                            placeholder = { Text(t("Scrivi in tedesco…")) },
                            modifier = Modifier.weight(1f),
                            maxLines = 3,
                            enabled = !caricamento
                        )
                        Spacer(Modifier.width(8.dp))
                        FilledIconButton(
                            onClick = { invia(input) },
                            enabled = input.isNotBlank() && !caricamento
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = t("Invia"))
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ModelloSelector(
    modello: String?,
    onModelloCambiato: (String?) -> Unit
) {
    var espanso by remember { mutableStateOf(false) }
    val modelli = listOf(
        null to t("Automatico (sceglie il più veloce)"),
        "nvidia/nemotron-3.5-lightning:free" to "Nemotron 3.5 Lightning (free)",
        "liquid/lfm-2.5-2.6b:free" to "LFM 2.5 (free)",
        "inclusionai/ling-3.0-flash-sante:free" to "Ling 3.0 Flash (free)",
        "google/gemma-4-31b-it:free" to "Gemma 4 31B (free)",
        "google/gemma-4-26b-a4b-it:free" to "Gemma 4 26B (free)",
        "nvidia/nemotron-3-super-120b-a12b:free" to "Nemotron 3 Super 120B (free)"
    )
    val etichetta = modelli.firstOrNull { it.first == modello }?.second ?: t("Automatico (free)")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spaziaturaSchermo(), vertical = 4.dp)
    ) {
        AssistChip(
            onClick = { espanso = true },
            label = { Text(etichetta, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = { Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp)) }
        )
        DropdownMenu(
            expanded = espanso,
            onDismissRequest = { espanso = false }
        ) {
            modelli.forEach { (id, nome) ->
                DropdownMenuItem(
                    text = { Text(nome) },
                    onClick = {
                        onModelloCambiato(id)
                        espanso = false
                    }
                )
            }
        }
    }
}

@Composable
private fun MessaggioBubble(
    msg: MessaggioRoleplay,
    isUltima: Boolean,
    animazioneAttiva: Boolean,
    rispostaAnimata: String
) {
    val isUser = msg.daUtente
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Card(
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(
                containerColor = when {
                    isUser -> MaterialTheme.colorScheme.primary
                    msg.errore -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceContainer
                }
            ),
            modifier = Modifier.widthIn(max = 300.dp)
        ) {
            val colore = when {
                isUser -> MaterialTheme.colorScheme.onPrimary
                msg.errore -> MaterialTheme.colorScheme.onErrorContainer
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
            if (isUser) {
                Text(
                    text = msg.testo,
                    modifier = Modifier.padding(12.dp),
                    color = colore,
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Column(modifier = Modifier.padding(12.dp)) {
                    val testoMostrato = if (animazioneAttiva && isUltima) rispostaAnimata else msg.testo
                    MarkdownText(markdown = testoMostrato, color = colore)
                }
            }
        }
    }
}
