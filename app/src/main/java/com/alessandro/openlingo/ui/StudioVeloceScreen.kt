package com.alessandro.openlingo.ui

import com.alessandro.openlingo.i18n.t

import androidx.compose.foundation.BorderStroke
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.openlingo.data.CalcoloPercorsoGiornaliero
import com.alessandro.openlingo.data.EsercizioUnita
import com.alessandro.openlingo.data.GrammaticaSyllabus
import com.alessandro.openlingo.data.LezioneAperta
import com.alessandro.openlingo.data.LezionePercorso
import com.alessandro.openlingo.data.LessicoLivelli
import com.alessandro.openlingo.data.PercorsoLezioni
import com.alessandro.openlingo.data.PodcastData
import com.alessandro.openlingo.data.SessioneLezione
import com.alessandro.openlingo.data.StatoUnita
import com.alessandro.openlingo.data.TipoEsercizioGrammatica
import com.alessandro.openlingo.data.UnitaConStato
import com.alessandro.openlingo.data.ConsigliEsercizi
import com.alessandro.openlingo.data.remote.UpdaterViewModel
import com.alessandro.openlingo.ui.theme.AltezzaBottonePrincipale
import com.alessandro.openlingo.ui.theme.Spaziature
import com.alessandro.openlingo.ui.theme.dimensioneContenuto
import com.alessandro.openlingo.ui.theme.spaziaturaSchermo
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Headphones

/**
 * V2 - schermata unica: lezione + parole + esercizi, in un flusso lineare.
 * - Selettore di lezione nella barra in alto (chip t("Lezioni")).
 * - Impostazioni ed extra dentro il menu nascosto (icona in alto a destra).
 * - La teoria è markdown renderizzato (MarkdownText), mai testo grezzo.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudioVeloceScreen(
    vm: OpenLingoViewModel,
    updater: UpdaterViewModel,
    onVaiAEsercizi: () -> Unit = {},
    onVaiAImpostazioni: () -> Unit = {},
    onVaiAProgresso: () -> Unit = {},
    onVaiATutor: () -> Unit = {},
    onVaiABackup: () -> Unit = {},
    onVaiAPercorsoGrammatica: () -> Unit = {},
    onVaiANuoveParole: () -> Unit = {},
    onVaiAImmersione: () -> Unit = {},
    onNaviga: (String) -> Unit = {},
) {
    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    val lezioneCorrente by vm.lezioneCorrente.collectAsStateWithLifecycle(1)
    val lezioneAperta by vm.lezioneAperta.collectAsStateWithLifecycle(null)
    val lezioniPercorso by vm.lezioniPercorso.collectAsStateWithLifecycle(emptyList())
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())

    val pr by vm.progressoReale.collectAsStateWithLifecycle(null)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val inizialeProfilo = remember(profilo) {
        (profilo?.config?.nomeVisualizzato?.trim()?.firstOrNull()?.uppercase() ?: "A")
    }
    val nomeProfilo = remember(profilo) {
        profilo?.config?.nomeVisualizzato
            ?.trim()
            ?.substringBefore(" ")
            ?.takeIf { it.isNotBlank() }
    }

    var mostraSelettore by remember { mutableStateOf(false) }
    var mostraMenu by remember { mutableStateOf(false) }
    var mostraPiano by rememberSaveable { mutableStateOf(false) }
    // La lezione non occupa più la home: si apre solo quando l'utente sceglie
    // di continuare il percorso. Così la schermata iniziale resta pulita.
    var lezioneInFocus by rememberSaveable { mutableStateOf(false) }
    val paroleCustomAbilitate by vm.enableCustomWords.collectAsStateWithLifecycle(false)

    // Test di ingresso: mostrato finché non viene completato o saltato.
    val placementFatto by vm.placementFatto.collectAsStateWithLifecycle(false)

    // Percorso grammaticale: la card in home deve mostrare subito il prossimo passo.
    val percorsoGram by vm.percorsoGrammatica.collectAsStateWithLifecycle(
        GrammaticaSyllabus.percorso(emptyMap())
    )
    val prossimaGram = remember(percorsoGram) {
        percorsoGram.firstOrNull { it.stato == StatoUnita.DA_RIPASSARE }
            ?: percorsoGram.firstOrNull { it.stato == StatoUnita.DISPONIBILE }
            ?: percorsoGram.firstOrNull()
    }
    val completateGram = percorsoGram.count { it.padronanza?.completata == true }
    val pianoOggi by vm.percorsoGiornaliero.collectAsStateWithLifecycle(null)

    // Primo avvio senza vocabolario -> sincronizza dal feed (una sola volta).
    LaunchedEffect(parole.isEmpty()) {
        if (parole.isEmpty()) vm.sincronizza()
    }


    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0),
        topBar = {
            CompactTopBar(
                title = t("Studio"),
                actions = {
                    // ---- Selettore di lezione: icona discreta, senza chip ----
                    IconButton(onClick = { mostraSelettore = true }) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = t("Scegli lezione"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    // ---- Profilo ed extra: avatar tondo -> pannello dal basso ----
                    // Niente tre puntini: un solo tocco apre un pannello ordinato.
                    Surface(
                        onClick = { mostraMenu = true },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.size(38.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                inizialeProfilo,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.TopCenter
        ) {
            LazyColumn(
                modifier = Modifier
                    .widthIn(max = dimensioneContenuto())
                    .fillMaxWidth(),
                contentPadding = PaddingValues(
                    start = spaziaturaSchermo(),
                    end = spaziaturaSchermo(),
                    top = Spaziature.md,
                    bottom = 112.dp
                ),
                verticalArrangement = Arrangement.spacedBy(Spaziature.lg)
            ) {
                if (lezioneInFocus && lezioneAperta != null) {
                    item {
                        Column {
                            TextButton(
                                onClick = { lezioneInFocus = false },
                                contentPadding = PaddingValues(horizontal = 0.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(t("Torna alla home"))
                            }
                            Spacer(Modifier.height(Spaziature.sm))
                            SezioneLezionePercorso(
                                vm = vm,
                                stato = lezioneAperta!!,
                                onAscolta = { ttsHelper.speak(it) },
                                onVaiAPercorso = onVaiAPercorsoGrammatica
                            )
                        }
                    }
                } else {
                    item {
                        IntestazioneHome(
                            saluto = nomeProfilo?.let { t("Ciao") + ", $it" } ?: t("Ciao"),
                            data = pianoOggi?.data.orEmpty(),
                            streakGiorni = pr?.streakGiorni ?: 0
                        )
                    }

                    pianoOggi?.let { piano ->
                        item {
                            PianoOggiCard(
                                percorso = piano,
                                onApriProssimo = { passo ->
                                    if (passo.azione == "ripasso") vm.caricaSessione()
                                    onNaviga(passo.azione)
                                },
                                onVediPiano = { mostraPiano = true },
                                onRipassoLibero = {
                                    vm.caricaSessione()
                                    onNaviga("ripasso")
                                }
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                        ) {
                            AzioneRapida(
                                icona = Icons.Filled.Quiz,
                                etichetta = t("Esercizi"),
                                onClick = onVaiAEsercizi,
                                modifier = Modifier.weight(1f)
                            )
                            AzioneRapida(
                                icona = Icons.Filled.Refresh,
                                etichetta = t("Ripasso"),
                                onClick = {
                                    vm.caricaSessione()
                                    onNaviga("ripasso")
                                },
                                modifier = Modifier.weight(1f)
                            )
                            AzioneRapida(
                                icona = Icons.Filled.Translate,
                                etichetta = t("Grammatica"),
                                onClick = onVaiAPercorsoGrammatica,
                                modifier = Modifier.weight(1f)
                            )
                            AzioneRapida(
                                icona = Icons.Filled.BarChart,
                                etichetta = t("Progresso"),
                                onClick = onVaiAProgresso,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    lezioneAperta?.let { aperta ->
                        item {
                            PercorsoLezioneCard(
                                stato = aperta,
                                onApri = {
                                    vm.apriLezionePercorso(aperta.id)
                                    lezioneInFocus = true
                                }
                            )
                        }
                    } ?: item {
                        PercorsoGrammaticaCard(
                            prossima = prossimaGram,
                            completate = completateGram,
                            totale = percorsoGram.size,
                            onApri = onVaiAPercorsoGrammatica
                        )
                    }

                    if (!placementFatto) {
                        item {
                            PlacementTestCard(
                                onCompletato = { livello -> vm.impostaLivello(livello) },
                                onSalta = { vm.saltaPlacement() }
                            )
                        }
                    }
                }
            }
        }
    }

    // Pannello "profilo ed extra": dal basso, con icone e descrizioni.
    if (mostraMenu) {
        ModalBottomSheet(
            onDismissRequest = { mostraMenu = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(modifier = Modifier.padding(bottom = Spaziature.xl)) {
                Text(
                    profilo?.config?.nomeVisualizzato?.takeIf { it.isNotBlank() } ?: t("Il tuo profilo"),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = Spaziature.md, vertical = Spaziature.sm)
                )
                VocePannello(Icons.Filled.Quiz, t("Esercizi"), t("Tutti i tipi, sempre nuovi")) {
                    mostraMenu = false; onVaiAEsercizi()
                }
                VocePannello(Icons.Filled.Translate, t("Percorso grammaticale"), t("Le unità A0-B2, una alla volta")) {
                    mostraMenu = false; onVaiAPercorsoGrammatica()
                }
                VocePannello(
                    Icons.AutoMirrored.Filled.MenuBook,
                    t("Lessico A1-B2"),
                    "${LessicoLivelli.tutte.size} " + t("parole per livello, con esercizi")
                ) {
                    mostraMenu = false; onNaviga("lessico")
                }
                if (paroleCustomAbilitate) {
                    VocePannello(Icons.Filled.Add, t("Aggiungi parola"), t("Nuove parole nel ripasso")) {
                        mostraMenu = false; onVaiANuoveParole()
                    }
                }
                VocePannello(Icons.Filled.Waves, t("Modalità Immersione"), t("Storie, shadowing, ascolto")) {
                    mostraMenu = false; onVaiAImmersione()
                }
                VocePannello(Icons.Filled.Headphones, t("Podcast & Video"), t("Ascolto autentico per livello")) {
                    mostraMenu = false; onNaviga("podcast")
                }
                VocePannello(Icons.Filled.EmojiEvents, t("Progresso"), t("Livello, competenze, numeri")) {
                    mostraMenu = false; onVaiAProgresso()
                }
                VocePannello(Icons.Filled.SmartToy, t("Tutor AI"), t("Chiedi, correggi, esercitati")) {
                    mostraMenu = false; onVaiATutor()
                }
                VocePannello(Icons.Filled.CloudUpload, t("Backup"), t("Salva e ripristina i dati")) {
                    mostraMenu = false; onVaiABackup()
                }
                VocePannello(Icons.Filled.Settings, t("Impostazioni"), t("Profilo, chiave, aggiornamenti")) {
                    mostraMenu = false; onVaiAImpostazioni()
                }
            }
        }
    }

    // Selettore lezioni: pannello dal basso, raggruppato per livello.
    if (mostraSelettore) {
        SelettoreLezioniSheet(
            lezioni = lezioniPercorso,
            correnteId = lezioneAperta?.id,
            onSeleziona = { lezione ->
                mostraSelettore = false
                vm.apriLezionePercorso(lezione.id)
                lezioneInFocus = true
            },
            onDismiss = { mostraSelettore = false }
        )
    }

    // Piano completo: la home mostra solo il prossimo passo, il resto vive qui.
    if (mostraPiano && pianoOggi != null) {
        ModalBottomSheet(
            onDismissRequest = { mostraPiano = false },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = Spaziature.md)
                    .padding(bottom = Spaziature.xl)
            ) {
                Text(t("Piano di oggi"), style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "${pianoOggi!!.passiCompletati}/${pianoOggi!!.passiTotali} " + t("passi") + " · ${pianoOggi!!.minutiTotali} " + t("minuti"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spaziature.md))
                pianoOggi!!.passi.forEach { passo ->
                    PassoPianoRiga(
                        passo = passo,
                        onApri = {
                            mostraPiano = false
                            if (passo.azione == "ripasso") vm.caricaSessione()
                            onNaviga(passo.azione)
                        },
                        onCompleta = { vm.completaPassoGiornaliero(passo.id) }
                    )
                    Spacer(Modifier.height(Spaziature.sm))
                }
            }
        }
    }
}

@Composable
private fun IntestazioneHome(
    saluto: String,
    data: String,
    streakGiorni: Int
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            t(saluto),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            listOfNotNull(
                data.takeIf { it.isNotBlank() },
                if (streakGiorni > 0) "$streakGiorni " + t("giorni di fila") else null
            ).joinToString(" · "),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Piano di oggi compatto: in home resta solo il prossimo passo.
 * Il resto del piano è nel pannello t("Vedi piano").
 */
@Composable
private fun PianoOggiCard(
    percorso: CalcoloPercorsoGiornaliero.PercorsoGiornaliero,
    onApriProssimo: (CalcoloPercorsoGiornaliero.PassoPercorso) -> Unit,
    onVediPiano: () -> Unit,
    onRipassoLibero: () -> Unit
) {
    val prossimo = percorso.prossimoPasso
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        t("PIANO DI OGGI"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        prossimo?.let { t(it.titolo) } ?: t("Piano completato"),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        prossimo?.let { t(it.descrizione) } ?: t(percorso.messaggioMotivazionale),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                    )
                }
                Spacer(Modifier.width(Spaziature.sm))
                ProgressoOggiRing(
                    progresso = percorso.progressoGiornata,
                    completati = percorso.passiCompletati,
                    totali = percorso.passiTotali
                )
            }
            Spacer(Modifier.height(Spaziature.md))
            LinearProgressIndicator(
                progress = { percorso.progressoGiornata },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
            )
            Spacer(Modifier.height(Spaziature.sm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${percorso.passiCompletati}/${percorso.passiTotali} " + t("passi") + " · ${percorso.minutiTotali} min",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onVediPiano) {
                    Text(t("Vedi piano"))
                }
                Spacer(Modifier.width(4.dp))
                if (prossimo != null) {
                    Button(onClick = { onApriProssimo(prossimo) }) {
                        Text(if (percorso.passiCompletati == 0) t("Inizia") else t("Continua"))
                    }
                } else {
                    OutlinedButton(onClick = onRipassoLibero) {
                        Text(t("Ripasso"))
                    }
                }
            }
        }
    }
}

/** Card compatta della prossima lezione: la home non mostra più tutta la teoria. */
@Composable
private fun PercorsoLezioneCard(
    stato: LezioneAperta,
    onApri: () -> Unit
) {
    val lezione = stato.lezione
    val progresso = if (stato.totale == 0) 0f else stato.indice.toFloat() / stato.totale
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClickLabel = t("Apri") + " " + t(lezione.titolo),
                role = Role.Button,
                onClick = onApri
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        t("CONTINUA IL PERCORSO"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        t(lezione.titolo),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        t("Lezione") + " ${lezione.numero} · ${lezione.livello} · ${lezione.unita.minuti} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(Modifier.height(14.dp))
            if (progresso > 0f) {
                LinearProgressIndicator(
                    progress = { progresso.coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(MaterialTheme.shapes.extraSmall),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.outlineVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    if (stato.conclusa) t("Lezione conclusa · ripassa quando vuoi")
                    else "${stato.indice}/${stato.totale} " + t("esercizi completati"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    t("Inizia da qui: teoria breve e esercizi guidati."),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ProgressoOggiRing(
    progresso: Float,
    completati: Int,
    totali: Int
) {
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            progress = { progresso.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxSize(),
            strokeWidth = 6.dp,
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f),
            strokeCap = StrokeCap.Round
        )
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "${(progresso * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Text(
                "$completati/$totali",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
private fun PassoPianoRiga(
    passo: CalcoloPercorsoGiornaliero.PassoPercorso,
    onApri: () -> Unit,
    onCompleta: () -> Unit
) {
    val forma = MaterialTheme.shapes.medium
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(forma)
            .clickable(
                onClickLabel = t("Apri") + " " + t(passo.titolo),
                role = Role.Button,
                onClick = onApri
            ),
        shape = forma,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        if (passo.completato) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.primaryContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    if (passo.completato) Icons.Filled.Check else Icons.Filled.PlayArrow,
                    contentDescription = null,
                    tint = if (passo.completato) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    t(passo.titolo),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = if (passo.completato) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Text(
                    t(passo.descrizione),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (passo.completato) {
                Text(
                    t("Fatto"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                IconButton(onClick = onCompleta) {
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = t("Segna") + " " + t(passo.titolo) + " " + t("come fatto"),
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun AzioneRapida(
    icona: ImageVector,
    etichetta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 76.dp)
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClickLabel = t("Apri") + " " + etichetta,
                role = Role.Button,
                onClick = onClick
            ),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 6.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icona,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                etichetta,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}


@Composable
private fun PercorsoGrammaticaCard(
    prossima: UnitaConStato?,
    completate: Int,
    totale: Int,
    onApri: () -> Unit
) {
    val progresso = if (totale == 0) 0f else completate.toFloat() / totale
    val etichetta = when (prossima?.stato) {
        StatoUnita.DA_RIPASSARE -> t("Ripasso in scadenza")
        StatoUnita.DISPONIBILE -> t("Prossimo passo")
        StatoUnita.COMPLETATA -> t("Percorso completato")
        StatoUnita.BLOCCATA -> t("Unità bloccata")
        null -> t("Percorso grammaticale")
    }
    // Card compatta: niente secondo bottone a tutta larghezza. Tutta la
    // superficie è il bottone, così la home resta corta e leggibile.
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .clickable(
                onClickLabel = t("Apri il percorso grammaticale"),
                role = Role.Button,
                onClick = onApri
            ),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.Filled.Translate,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(
                        t("Percorso grammaticale"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.75f)
                    )
                    Text(
                        t(prossima?.unita?.titolo ?: "Tutto il percorso"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(18.dp)
                )
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (prossima != null) {
                        "$etichetta · ${prossima.unita.livello} · ${prossima.unita.minuti} min"
                    } else {
                        t("Teoria, esempi ed esercizi in ordine")
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    "$completate/$totale",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { progresso },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.15f)
            )
        }
    }
}

@Composable
private fun TeoriaCard(
    contenuto: String,
    onAscolta: () -> Unit
) {
    var aperta by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(
                        onClickLabel = if (aperta) t("Chiudi la teoria") else t("Apri la teoria"),
                        role = Role.Button
                    ) { aperta = !aperta }
                    .padding(Spaziature.md),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
                Spacer(Modifier.width(Spaziature.sm))
                Column(Modifier.weight(1f)) {
                    Text(
                        t("Teoria"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        if (aperta) t("Tocca per chiudere") else t("Tocca per leggere la spiegazione"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Icon(
                    imageVector = if (aperta) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (aperta) t("Chiudi la teoria") else t("Apri la teoria"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (aperta) {
                Column(
                    modifier = Modifier.padding(
                        start = Spaziature.md,
                        end = Spaziature.md,
                        bottom = Spaziature.md
                    )
                ) {
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(Spaziature.sm))
                    MarkdownText(
                        markdown = contenuto,
                        color = MaterialTheme.colorScheme.onSurface,
                        stile = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(Spaziature.sm))
                    TextButton(onClick = onAscolta) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        Text(t("Ascolta"))
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsiglioCard(
    titolo: String,
    motivo: String,
    onApri: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(40.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        Icons.Filled.EmojiEvents,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.width(Spaziature.sm))
            Column(Modifier.weight(1f)) {
                Text(
                    t("Consiglio per te"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    titolo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    motivo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.width(Spaziature.xs))
            TextButton(onClick = onApri) {
                Text(t("Apri"))
            }
        }
    }
}

/** Una riga del pannello dal basso: icona tonda + titolo + descrizione + freccia. */
@Composable
private fun VocePannello(
    icona: ImageVector,
    titolo: String,
    descrizione: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                onClickLabel = t("Apri") + " " + titolo,
                role = Role.Button,
                onClick = onClick
            )
            .padding(horizontal = Spaziature.md, vertical = Spaziature.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icona,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(titolo, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                descrizione,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Icon(
            Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp)
        )
    }
}

/**
 * La lezione del percorso in home: intestazione, teoria offline e esercizi.
 *
 * Il punto raggiunto è salvato nel profilo a ogni risposta, quindi chiudendo
 * l'app la lezione riparte dalla domanda giusta invece che da zero.
 */
@Composable
private fun SezioneLezionePercorso(
    vm: OpenLingoViewModel,
    stato: LezioneAperta,
    onAscolta: (String) -> Unit,
    onVaiAPercorso: () -> Unit
) {
    val unita = stato.lezione.unita

    // Ogni tentativo usa una sessione DIVERSA: la prima riprende quella salvata
    // (così chiudendo l'app si continua dalla stessa domanda), le successive
    // escludono gli esercizi appena fatti. Così non ci si ritrova le stesse
    // domande di 20 secondi prima.
    var tentativi by rememberSaveable(stato.id) { mutableIntStateOf(0) }
    var seme by rememberSaveable(stato.id) { mutableStateOf(System.currentTimeMillis()) }
    var errori by rememberSaveable(stato.id) { mutableStateOf(listOf<ErroreRisposta>()) }

    val eserciziAi by vm.eserciziUnitaAi.collectAsStateWithLifecycle()
    val banco = remember(unita.id, eserciziAi[unita.id]) {
        SessioneLezione.banco(unita, eserciziAi[unita.id].orEmpty())
    }
    val chiaveRotazione = if (tentativi == 0) stato.rotazione.sessione else null
    val sessione = remember(unita.id, tentativi, chiaveRotazione) {
        if (tentativi == 0) {
            SessioneLezione.sessione(banco, stato.rotazione, seme.toInt())
        } else {
            SessioneLezione.nuovaSessione(banco, stato.rotazione, seme.toInt())
        }
    }
    val eserciziSessione = sessione.esercizi
    val totale = eserciziSessione.size

    // Sessione nuova (primo avvio dopo l'aggiornamento o t("Riprova")): il punto
    // salvato si riferiva ad altre domande, quindi si riparte da zero.
    val sessioneNuova = stato.rotazione.sessione != sessione.rotazione.sessione

    LaunchedEffect(sessione.rotazione) {
        vm.aggiornaRotazione(unita.id, sessione.rotazione)
        vm.generaEserciziUnita(unita.id)
    }

    var indice by rememberSaveable(stato.id) {
        mutableIntStateOf(
            if (sessioneNuova) 0 else stato.progresso.indice.coerceIn(0, totale)
        )
    }
    var corrette by rememberSaveable(stato.id) {
        mutableIntStateOf(
            if (sessioneNuova) 0 else stato.progresso.corrette.coerceIn(0, totale)
        )
    }
    var scelta by rememberSaveable(stato.id) { mutableIntStateOf(-1) }
    var testo by rememberSaveable(stato.id) { mutableStateOf("") }
    var verificato by rememberSaveable(stato.id) { mutableStateOf(false) }

    val finita = indice >= totale
    val esercizio = eserciziSessione.getOrNull(indice)
    val ultimaDomanda = indice == totale - 1
    val rispostaGiusta = verificato && esercizio != null && when (esercizio.tipo) {
        TipoEsercizioGrammatica.SCELTA_MULTIPLA ->
            GrammaticaSyllabus.rispostaSceltaCorretta(esercizio, scelta.takeIf { it >= 0 })
        else -> GrammaticaSyllabus.rispostaTestoCorretta(esercizio, testo)
    }

    fun rispostaData(e: EsercizioUnita): String =
        if (e.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            e.opzioni.getOrNull(scelta).orEmpty()
        } else {
            testo.trim()
        }

    fun verifica() {
        val e = esercizio ?: return
        val giusto = when (e.tipo) {
            TipoEsercizioGrammatica.SCELTA_MULTIPLA ->
                GrammaticaSyllabus.rispostaSceltaCorretta(e, scelta.takeIf { it >= 0 })
            else -> GrammaticaSyllabus.rispostaTestoCorretta(e, testo)
        }
        verificato = true
        if (giusto) {
            corrette += 1
        } else {
            errori = errori + ErroreRisposta(indice, rispostaData(e))
        }
        // La sessione è persistita, quindi il punto salvato è sempre valido.
        vm.salvaProgressoLezione(stato.id, indice, corrette, totale, completata = false)
    }

    fun avanti() {
        if (indice < totale - 1) {
            indice += 1
            scelta = -1
            testo = ""
            verificato = false
            vm.salvaProgressoLezione(stato.id, indice, corrette, totale, completata = false)
        } else {
            indice = totale
            vm.salvaProgressoLezione(stato.id, totale, corrette, totale, completata = true)
            vm.salvaPadronanzaGrammatica(stato.id, corrette, totale)
            vm.completaPassoGiornaliero("grammatica")
        }
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t("LEZIONE") + " ${stato.lezione.numero}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${unita.livello} · ${unita.minuti} min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                t(unita.titolo),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            if (unita.sottotitolo.isNotBlank()) {
                Text(
                    t(unita.sottotitolo),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(Spaziature.sm))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
            ) {
                AssistChip(
                    onClick = onVaiAPercorso,
                    label = {
                        Text(
                            when {
                                finita -> t("Lezione conclusa")
                                indice > 0 -> t("Domanda") + " ${indice + 1} " + t("di") + " $totale"
                                else -> t("Inizia · $totale esercizi")
                            }
                        )
                    }
                )
                TextButton(onClick = onVaiAPercorso) {
                    Text(t("Percorso"))
                }
            }
        }
    }

    Spacer(Modifier.height(Spaziature.sm))

    TeoriaCard(
        contenuto = PercorsoLezioni.teoriaMarkdown(unita),
        onAscolta = {
            onAscolta((listOf(unita.titolo) + unita.teoria.regola).joinToString(". "))
        }
    )

    Spacer(Modifier.height(Spaziature.sm))

    if (finita) {
        RisultatoLezione(
            stato = stato,
            esercizi = eserciziSessione,
            errori = errori,
            corrette = corrette,
            totale = totale,
            onRiprova = {
                indice = 0
                corrette = 0
                scelta = -1
                testo = ""
                verificato = false
                errori = emptyList()
                tentativi += 1
                seme = System.currentTimeMillis()
            },
            onAvanti = { vm.avanzaLezionePercorso(stato.id) }
        )
        return
    }

    if (esercizio == null) return

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t("Esercizi"),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "${indice + 1}/$totale",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (indice + if (verificato) 1 else 0).toFloat() / totale.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(MaterialTheme.shapes.extraSmall),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.outlineVariant
            )
            Spacer(Modifier.height(Spaziature.md))
            Text(
                esercizio.domanda,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(Spaziature.sm))

            if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                val lettere = "ABCD"
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    esercizio.opzioni.forEachIndexed { i, opzione ->
                        RigaOpzione(
                            testo = opzione,
                            lettera = lettere.getOrElse(i) { '?' },
                            selezionata = scelta == i,
                            corretta = verificato && esercizio.rispostaCorretta == i,
                            verificato = verificato,
                            onClick = { if (!verificato) scelta = i }
                        )
                    }
                }
            } else {
                OutlinedTextField(
                    value = testo,
                    onValueChange = { if (!verificato) testo = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !verificato,
                    singleLine = true,
                    placeholder = { Text(t("Scrivi la risposta in tedesco")) }
                )
            }

            if (verificato) {
                Spacer(Modifier.height(Spaziature.sm))
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = if (rispostaGiusta) {
                        MaterialTheme.colorScheme.secondaryContainer
                    } else {
                        MaterialTheme.colorScheme.errorContainer
                    }
                ) {
                    Column(modifier = Modifier.padding(Spaziature.sm)) {
                        Text(
                            if (rispostaGiusta) t("Giusto!") else t("Non ancora"),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        if (esercizio.spiegazione.isNotBlank()) {
                            Text(
                                esercizio.spiegazione,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spaziature.md))
            Button(
                onClick = { if (verificato) avanti() else verifica() },
                enabled = verificato || scelta >= 0 || testo.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale())
            ) {
                Text(
                    when {
                        !verificato -> t("Verifica")
                        ultimaDomanda -> t("Concludi lezione")
                        else -> t("Avanti")
                    }
                )
            }
        }
    }
}

/** Una opzione di risposta: A/B/C/D + testo, colorata dopo la verifica. */
@Composable
private fun RigaOpzione(
    testo: String,
    lettera: Char,
    selezionata: Boolean,
    corretta: Boolean,
    verificato: Boolean,
    onClick: () -> Unit
) {
    val colore = when {
        corretta -> MaterialTheme.colorScheme.primaryContainer
        verificato && selezionata -> MaterialTheme.colorScheme.errorContainer
        selezionata -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    Surface(
        onClick = onClick,
        enabled = !verificato,
        shape = MaterialTheme.shapes.medium,
        color = colore,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spaziature.sm, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(
                        if (selezionata || corretta) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainer
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    lettera.toString(),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (selezionata || corretta) {
                        MaterialTheme.colorScheme.onPrimary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            Spacer(Modifier.width(Spaziature.sm))
            Text(testo, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

/** Risultato della lezione: punteggio, ripasso e passaggio al passo successivo. */
@Composable
private fun RisultatoLezione(
    stato: LezioneAperta,
    esercizi: List<EsercizioUnita>,
    errori: List<ErroreRisposta>,
    corrette: Int,
    totale: Int,
    onRiprova: () -> Unit,
    onAvanti: () -> Unit
) {
    val totaleValido = totale.coerceAtLeast(1)
    val percentuale = corrette * 100 / totaleValido
    val promosso = percentuale >= GrammaticaSyllabus.SOGLIA_PADRONANZA

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Text(
                if (promosso) t("Lezione superata") else t("Lezione conclusa"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Text(
                t("$corrette risposte corrette su $totaleValido · $percentuale%"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
            )
            if (!promosso) {
                Text(
                    t("Ti servono almeno") + " ${GrammaticaSyllabus.SOGLIA_PADRONANZA}% " + t("per la padronanza: ripeti la lezione quando vuoi."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                )
            }
            Spacer(Modifier.height(Spaziature.md))
            Button(
                onClick = onAvanti,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale())
            ) {
                Text(t("Lezione successiva"))
            }
            Spacer(Modifier.height(Spaziature.xs))
            TextButton(
                onClick = onRiprova,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("Ripeti con esercizi nuovi"))
            }
        }
    }

    Spacer(Modifier.height(Spaziature.md))
    RiepilogoErrori(esercizi = esercizi, errori = errori)
}

/**
 * Selettore lezioni: pannello dal basso raggruppato per livello.
 *
 * Non è una lista di numeri: mostra il percorso reale, con lezioni concluse,
 * da ripassare e ancora bloccate (prerequisiti mancanti).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelettoreLezioniSheet(
    lezioni: List<LezionePercorso>,
    correnteId: String?,
    onSeleziona: (LezionePercorso) -> Unit,
    onDismiss: () -> Unit
) {
    val completate = lezioni.count { it.completata }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spaziature.md)
        ) {
            Text(
                t("Lezioni"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                "$completate " + t("di") + " ${lezioni.size} " + t("completate") + " · " + t("percorso A0 → B2"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.sm))
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 520.dp),
            contentPadding = PaddingValues(
                start = Spaziature.md,
                end = Spaziature.md,
                bottom = Spaziature.xl
            ),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            lezioni.groupBy { it.livello }.forEach { (livello, gruppo) ->
                item(key = t("livello-$livello")) {
                    IntestazioneLivello(livello, gruppo)
                }
                items(gruppo, key = { it.id }) { lezione ->
                    RigaLezione(
                        lezione = lezione,
                        corrente = lezione.id == correnteId,
                        onSeleziona = { onSeleziona(lezione) }
                    )
                }
            }
        }
    }
}

@Composable
private fun IntestazioneLivello(livello: String, gruppo: List<LezionePercorso>) {
    val fatte = gruppo.count { it.completata }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = Spaziature.sm, bottom = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            livello,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(6.dp))
        Text(
            descrizioneLivello(livello),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.weight(1f))
        Text(
            "$fatte/${gruppo.size}",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun RigaLezione(
    lezione: LezionePercorso,
    corrente: Boolean,
    onSeleziona: () -> Unit
) {
    val statoTesto = when {
        !lezione.accessibile -> t("Bloccata")
        lezione.stato == StatoUnita.DA_RIPASSARE -> t("Da ripassare")
        lezione.completata -> t("Completata")
        corrente -> t("In corso")
        else -> t("Da fare")
    }
    Surface(
        onClick = onSeleziona,
        enabled = lezione.accessibile,
        shape = MaterialTheme.shapes.medium,
        color = if (corrente) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surfaceContainer
        },
        border = if (corrente) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = Spaziature.sm, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            !lezione.accessibile -> MaterialTheme.colorScheme.surface
                            lezione.completata -> MaterialTheme.colorScheme.secondaryContainer
                            else -> MaterialTheme.colorScheme.primaryContainer
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (lezione.accessibile) {
                    Text(
                        lezione.numero.toString(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Icon(
                        Icons.Filled.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(Modifier.width(Spaziature.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    t(lezione.titolo),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (lezione.accessibile) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    statoTesto,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (corrente) {
                        MaterialTheme.colorScheme.primary
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            if (lezione.completata) {
                Icon(
                    Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun descrizioneLivello(livello: String): String = when (livello.uppercase()) {
    "A0" -> t("Basi")
    "A1" -> t("Elementare")
    "A2" -> t("Base")
    "B1" -> t("Intermedio")
    "B2" -> t("Avanzato")
    else -> ""
}

