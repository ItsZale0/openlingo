package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.launch
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

data class MessaggioVoiceChat(
    val testo: String,
    val daUtente: Boolean,
    val corretta: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceChatScreen(vm: TedescoViewModel, onIndietro: () -> Unit) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val voiceHelper = remember { VoiceHelper(context) }
    val ttsHelper = rememberTtsHelper(context)

    var messaggi by remember {
        mutableStateOf(
            listOf(
                MessaggioVoiceChat(
                    testo = "Ciao! Sono il tuo tutor di tedesco. Parla con me in tedesco, ti risponderò e ti correggerò alla fine. Tieni premuto il microfono per parlare.",
                    daUtente = false
                )
            )
        )
    }
    var elaborazione by remember { mutableStateOf(false) }
    var correzione by remember { mutableStateOf<String?>(null) }
    var mostrandoCorrezione by remember { mutableStateOf(false) }

    // Cleanup
    DisposableEffect(Unit) {
        onDispose {
            voiceHelper.destroy()
            ttsHelper.shutdown()
        }
    }

    // Auto-scroll
    LaunchedEffect(messaggi.size) {
        if (messaggi.isNotEmpty()) {
            listState.animateScrollToItem(messaggi.size - 1)
        }
    }

    ScreenScaffold("Conversazione vocale", onBack = onIndietro, scrollable = false) {

            // Livello
            AssistChip(
                onClick = {},
                label = { Text("Livello: $livello") }
            )

            Spacer(Modifier.height(8.dp))

            // Messaggi
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(messaggi) { msg ->
                    MessaggioBubble(msg, ttsHelper)
                }

                if (elaborazione) {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Start
                        ) {
                            CircularProgressIndicator(modifier = Modifier.size(24.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Sto analizzando la conversazione...")
                        }
                    }
                }
            }

            // Correzione
            if (mostrandoCorrezione && correzione != null) {
                Spacer(Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer
                    )
                ) {
                    Column(Modifier.padding(12.dp).animateContentSize()) {
                        Text(
                            "Correzione",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            correzione ?: "",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Push-to-talk
            VoiceInputBar(
                voiceHelper = voiceHelper,
                onResult = { testo ->
                    scope.launch {
                        messaggi = messaggi + MessaggioVoiceChat(testo, daUtente = true)
                        elaborazione = true
                        try {
                            val risultato = vm.chiediAlTutor(
                                cronologia = messaggi.map { m ->
                                    if (m.daUtente) "user" to m.testo else "assistant" to m.testo
                                },
                                livello = livello,
                                lezione = lezioneCorrente
                            )
                            val risposta = risultato.getOrThrow()
                            messaggi = messaggi + MessaggioVoiceChat(risposta, daUtente = false)
                            ttsHelper.speak(risposta)
                        } catch (e: Exception) {
                            messaggi = messaggi + MessaggioVoiceChat(
                                "Errore: ${e.message}",
                                daUtente = false
                            )
                        } finally {
                            elaborazione = false
                        }
                    }
                },
                enabled = !elaborazione
            )

            Spacer(Modifier.height(8.dp))

    }
}

@Composable
fun MessaggioBubble(msg: MessaggioVoiceChat, ttsHelper: TtsHelper) {
    val alignment = if (msg.daUtente) Arrangement.End else Arrangement.Start
    val containerColor = if (msg.daUtente) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Card(
            modifier = Modifier.widthIn(max = 300.dp),
            colors = CardDefaults.cardColors(containerColor = containerColor),
            shape = MaterialTheme.shapes.large
        ) {
            Column(Modifier.padding(12.dp)) {
                Text(
                    text = msg.testo,
                    style = MaterialTheme.typography.bodyMedium
                )
                if (!msg.daUtente) {
                    Spacer(Modifier.height(4.dp))
                    IconButton(
                        onClick = { ttsHelper.speak(msg.testo) },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Ascolta",
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}