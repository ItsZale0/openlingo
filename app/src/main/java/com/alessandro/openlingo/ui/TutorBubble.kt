package com.alessandro.openlingo.ui

import com.alessandro.openlingo.i18n.t

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.openlingo.data.local.ConversazioneEntry
import kotlinx.coroutines.launch

/**
 * Tutor AI come bolla laterale (stile Meta AI su WhatsApp):
 * - chiusa: FAB circolare in basso a destra
 * - aperta: pannello chat ancorato in basso a destra, sopra il contenuto
 *
 * Vive sopra il NavHost (in AppNav), quindi e' disponibile su ogni schermata.
 * La conversazione e' persistita nel profilo come la schermata completa.
 */
/** Conversazione tenuta in memoria: oltre questi messaggi i piu' vecchi cadono. */
private const val LIMITE_CHAT = 100

@Composable
fun TutorBubble(vm: OpenLingoViewModel) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)

    var aperto by remember { mutableStateOf(false) }
    var messaggi by remember { mutableStateOf<List<MessaggioTutor>>(emptyList()) }
    var contatoreId by remember { mutableLongStateOf(0L) }
    /** Messaggi già presenti nello storico: si salva solo oltre questo numero. */
    var messaggiCaricati by remember { mutableIntStateOf(0) }
    var input by remember { mutableStateOf(androidx.compose.ui.text.input.TextFieldValue("")) }
    var caricamento by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    val keyboard = LocalSoftwareKeyboardController.current

    // Carica la conversazione salvata (o il messaggio di benvenuto).
    // La chiave è SOLO l'id del profilo: cambiando profilo si ricarica il suo
    // storico, senza risalvare in loop la stessa conversazione.
    LaunchedEffect(profilo?.id) {
        val p = profilo ?: return@LaunchedEffect
        val storico = p.stato.conversazioni
            .filter { it.tipo == "tutor" }
            .map { MessaggioTutor(it.testo, it.ruolo == "utente", it.correzione != null) }
        // Id unici per posizione: piu' messaggi identici devono restare distinguibili.
        messaggi = (if (storico.isNotEmpty()) storico else listOf(
            MessaggioTutor(
                testo = t("Ciao! Sono il tuo tutor di tedesco. Chiedimi grammatica, esempi o dubbi. Rispondo in italiano."),
                daUtente = false
            )
        )).mapIndexed { i, m -> m.copy(id = i.toLong()) }
        contatoreId = messaggi.size.toLong()
        messaggiCaricati = messaggi.size
    }

    // Salva solo quando arrivano messaggi NUOVI (non a ogni ricomposizione e
    // non quando cambia solo il profilo): prima questo effetto riscriveva la
    // conversazione intera in loop, consumando CPU e batteria.
    LaunchedEffect(messaggi.size, profilo?.id) {
        val p = profilo ?: return@LaunchedEffect
        if (messaggi.size <= messaggiCaricati) return@LaunchedEffect
        val conversazioni = messaggi.mapIndexed { index, msg ->
            ConversazioneEntry(
                id = "tutor_${System.currentTimeMillis()}_$index",
                tipo = "tutor",
                ruolo = if (msg.daUtente) "utente" else "tutor",
                testo = msg.testo,
                timestamp = System.currentTimeMillis() - (messaggi.size - index) * 1000,
                correzione = if (msg.errore) msg.testo else null
            )
        }
        vm.saveConversazioni(p.id, "tutor", conversazioni)
        messaggiCaricati = messaggi.size
    }

    /** Aggiunge un messaggio con id unico e tiene in memoria solo gli ultimi LIMITE_CHAT. */
    fun aggiungi(m: MessaggioTutor): List<MessaggioTutor> {
        contatoreId += 1
        return (messaggi + m.copy(id = contatoreId)).takeLast(LIMITE_CHAT)
    }

    fun invia(testo: String) {
        keyboard?.hide()
        val t = testo.trim()
        if (t.isBlank() || caricamento) return
        messaggi = aggiungi(MessaggioTutor(t, daUtente = true))
        input = androidx.compose.ui.text.input.TextFieldValue("")
        caricamento = true
        scope.launch {
            val cronologia = messaggi
                .filterNot { it.errore }
                .map { if (it.daUtente) "utente" to it.testo else "tutor" to it.testo }
            val risultato = vm.chiediAlTutor(cronologia, livello, lezioneCorrente)
            risultato.fold(
                onSuccess = { risposta -> messaggi = aggiungi(MessaggioTutor(risposta, daUtente = false)) },
                onFailure = { e -> messaggi = aggiungi(MessaggioTutor(e.message ?: t("Errore sconosciuto"), daUtente = false, errore = true)) }
            )
            caricamento = false
        }
    }

    LaunchedEffect(messaggi.size) {
        if (messaggi.isNotEmpty()) listState.animateScrollToItem(messaggi.lastIndex)
    }

    Box(Modifier.fillMaxSize()) {
        if (!aperto) {
            // Chiusa: bolla circolare
            Box(Modifier.align(Alignment.BottomEnd).padding(12.dp)) {
                SmallFloatingActionButton(
                    onClick = { aperto = true },
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(56.dp)
                ) {
                    Icon(
                        Icons.Filled.SmartToy,
                        contentDescription = t("Apri il tutor"),
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        } else {
            // Aperta: pannello chat sopra il contenuto
            Box(
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .widthIn(min = 300.dp, max = 400.dp)
                    .fillMaxHeight(0.62f)
            ) {
                Surface(
                    shape = MaterialTheme.shapes.extraLarge,
                    tonalElevation = 3.dp,
                    shadowElevation = 10.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .imePadding()
                    ) {
                        // Header
                        Surface(
                            tonalElevation = 2.dp,
                            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(start = 16.dp, end = 4.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer,
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            Icons.Filled.SmartToy,
                                            contentDescription = null,
                                            modifier = Modifier.size(20.dp),
                                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    }
                                }
                                Spacer(Modifier.size(10.dp))
                                Column(Modifier.weight(1f)) {
                                    Text(
                                        t("Tutor AI"),
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        t("Livello $livello"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { aperto = false }, modifier = Modifier.size(40.dp)) {
                                    Icon(
                                        Icons.Filled.Close,
                                        contentDescription = t("Chiudi"),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }

                        // Messaggi
                        LazyColumn(
                            state = listState,
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp),
                            contentPadding = PaddingValues(vertical = 6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(messaggi, key = { it.id }) { msg ->
                                BollaMessaggio(msg)
                            }
                            if (caricamento) {
                                item {
                                    Row(
                                        Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.Start
                                    ) {
                                        Surface(
                                            shape = MaterialTheme.shapes.large,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(2.dp)
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier
                                                    .padding(10.dp)
                                                    .size(18.dp),
                                                strokeWidth = 2.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        // Input
                        Surface(tonalElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 10.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                OutlinedTextField(
                                    value = input,
                                    onValueChange = { input = it },
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text(t("Chiedi al tutor…")) },
                                    maxLines = 3,
                                    shape = MaterialTheme.shapes.extraLarge,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                    keyboardActions = KeyboardActions(onSend = { invia(input.text) })
                                )
                                IconButton(
                                    onClick = { invia(input.text) },
                                    enabled = !caricamento && input.text.isNotBlank(),
                                    modifier = Modifier.size(44.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.Send,
                                        contentDescription = t("Invia"),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BollaMessaggio(msg: MessaggioTutor) {
    val allineaADestra = msg.daUtente
    val colore = when {
        msg.errore -> MaterialTheme.colorScheme.errorContainer
        allineaADestra -> MaterialTheme.colorScheme.primaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainer
    }
    val testoColore = when {
        msg.errore -> MaterialTheme.colorScheme.onErrorContainer
        allineaADestra -> MaterialTheme.colorScheme.onPrimaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(Modifier.fillMaxWidth(), contentAlignment = if (allineaADestra) Alignment.CenterEnd else Alignment.CenterStart) {
        Surface(
            shape = MaterialTheme.shapes.large,
            color = colore,
            modifier = Modifier.widthIn(max = 260.dp)
        ) {
            if (msg.daUtente) {
                Text(
                    msg.testo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = testoColore,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            } else {
                MarkdownText(
                    markdown = msg.testo,
                    color = testoColore,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                )
            }
        }
    }
}
