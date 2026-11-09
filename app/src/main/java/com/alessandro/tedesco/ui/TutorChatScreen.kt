package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.runtime.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.material.icons.filled.VolumeOff
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata
import com.alessandro.tedesco.data.local.ConversazioneEntry
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.ProfileManager
import com.alessandro.tedesco.data.remote.TutorService
import com.alessandro.tedesco.TedescoApp
import android.app.Application

data class MessaggioTutor(
    val testo: String,
    val daUtente: Boolean,
    val errore: Boolean = false,
    /**
     * Id unico del messaggio. Senza, due messaggi con lo stesso testo hanno lo
     * stesso hashCode(): il LazyColumn della bolla li usa come chiave e va in crash
     * con "Key was already used" dopo pochi giri di chat.
     */
    val id: Long = 0L
)

/** Conversazione tenuta in memoria: oltre questi messaggi i piu' vecchi cadono. */
private const val LIMITE_CHAT = 100

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TutorChatScreen(vm: TedescoViewModel, onIndietro: (() -> Unit)? = null) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.label?.substringBefore(" ") ?: "A0"
    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val apiKey by vm.tutorApiKey.collectAsStateWithLifecycle("")
    val modelloTutor by vm.modelloTutor.collectAsStateWithLifecycle(null)
    val modelli by vm.modelliDisponibili.collectAsStateWithLifecycle()
    val ragionamento by vm.ragionamento.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { vm.caricaModelli() }
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // Carica conversazione persistente dal profilo quando disponibile
    var messaggi by remember { mutableStateOf<List<MessaggioTutor>>(emptyList()) }
    var contatoreId by remember { mutableLongStateOf(0L) }
    var storicoCaricato by remember { mutableStateOf(false) }
    LaunchedEffect(profilo?.id) {
        val p = profilo
        if (p != null && !storicoCaricato) {
            val storico = p.stato.conversazioni
                .filter { it.tipo == "tutor" }
                .map { MessaggioTutor(it.testo, it.ruolo == "utente", it.correzione != null) }
            // Id unici per posizione: la stessa frase inviata due volte non deve collidere.
            messaggi = (if (storico.isNotEmpty()) storico else listOf(
                MessaggioTutor(
                    testo = t("Ciao! Sono il tuo tutor di tedesco. Chiedimi quello che vuoi: grammatica, esempi, esercizi, dubbi. Rispondo in italiano."),
                    daUtente = false
                )
            )).mapIndexed { i, m -> m.copy(id = i.toLong()) }
            contatoreId = messaggi.size.toLong()
            storicoCaricato = true
        }
    }
    var input by remember { mutableStateOf(TextFieldValue("")) }
    val keyboard = LocalSoftwareKeyboardController.current
    var caricamento by remember { mutableStateOf(false) }
    var rispostaAnimata by remember { mutableStateOf("") }
    var animazioneAttiva by remember { mutableStateOf(false) }
    var rispostaDaAnimare by remember { mutableStateOf("") }

    // Salva conversazione nel profilo quando cambiano i messaggi
    LaunchedEffect(messaggi, profilo) {
        val p = profilo
        if (p != null && storicoCaricato) {
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
        }
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
        input = TextFieldValue("")
        caricamento = true

        scope.launch {
            val cronologia = messaggi
                .filterNot { it.errore }
                .map { if (it.daUtente) "utente" to it.testo else "tutor" to it.testo }

            val risultato = vm.chiediAlTutor(cronologia, livello, lezioneCorrente)
            risultato.fold(
                onSuccess = { risposta ->
                    messaggi = aggiungi(MessaggioTutor(risposta, daUtente = false))
                    // Avvia animazione di digitazione
                    rispostaAnimata = ""
                    rispostaDaAnimare = risposta
                    animazioneAttiva = true
                },
                onFailure = { e ->
                    messaggi = aggiungi(MessaggioTutor(e.message ?: t("Errore sconosciuto"), daUtente = false, errore = true))
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
                    if (onIndietro != null) {
                        IconButton(onClick = onIndietro, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = t("Indietro"))
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Icon(
                        Icons.Filled.School,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            t("Tutor di tedesco"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                        )
                        Text(
                            "Livello $livello · lezione $lezioneCorrente",
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
                .fillMaxSize()
                // Solo il padding superiore (per stare sotto la topBar).
                // Il fondo lo gestiscono NavHost (nav bar) + imePadding (tastiera).
                .padding(top = padding.calculateTopPadding())
        ) {
            // Selettore modello
            ModelloSelector(
                modello = modelloTutor,
                modelli = modelli,
                onModelloCambiato = { vm.cambiaModelloTutor(it) }
            )

            RagionamentoSelector(
                ragionamento = ragionamento,
                onCambia = { vm.cambiaRagionamento(it) }
            )

            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = spaziaturaSchermo()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(messaggi, key = { it.id }) { msg ->
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

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
                    .imePadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = input,
                        onValueChange = { input = it },
                        placeholder = { Text(t("Chiedi qualcosa…")) },
                        modifier = Modifier.weight(1f),
                        maxLines = 3,
                        enabled = !caricamento,
                        singleLine = false
                    )
                    Spacer(Modifier.width(8.dp))
                    FilledIconButton(
                        onClick = { invia(input.text); input = TextFieldValue("") },
                        enabled = input.text.isNotBlank() && !caricamento
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = t("Invia"))
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
    modelli: List<Pair<String?, String>>,
    onModelloCambiato: (String?) -> Unit
) {
    var espanso by remember { mutableStateOf(false) }
    val etichetta = modelli.firstOrNull { it.first == modello }?.second
        ?: modello?.substringAfterLast('/')?.replaceFirstChar { it.uppercase() }
        ?: t("Automatico")

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
private fun RagionamentoSelector(ragionamento: String?, onCambia: (String?) -> Unit) {
    val opzioni = listOf(
        null to t("Ragionamento: Auto"),
        "low" to t("Ragionamento: Basso"),
        "medium" to t("Ragionamento: Medio"),
        "high" to t("Ragionamento: Alto")
    )
    var espanso by remember { mutableStateOf(false) }
    val etichetta = opzioni.firstOrNull { it.first == ragionamento }?.second ?: t("Ragionamento: Auto")
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spaziaturaSchermo(), vertical = 4.dp)
    ) {
        AssistChip(
            onClick = { espanso = true },
            label = { Text(etichetta, style = MaterialTheme.typography.bodySmall) },
            trailingIcon = {
                Icon(Icons.Filled.ExpandMore, contentDescription = null, modifier = Modifier.size(16.dp))
            }
        )
        DropdownMenu(expanded = espanso, onDismissRequest = { espanso = false }) {
            opzioni.forEach { (id, nome) ->
                DropdownMenuItem(text = { Text(nome) }, onClick = { onCambia(id); espanso = false })
            }
        }
    }
}

@Composable
private fun MessaggioBubble(
    msg: MessaggioTutor,
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
            modifier = Modifier.widthIn(max = 300.dp).animateContentSize()
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
