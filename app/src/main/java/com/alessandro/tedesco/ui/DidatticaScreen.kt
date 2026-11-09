package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.Composto
import com.alessandro.tedesco.data.DidatticaImmersione
import com.alessandro.tedesco.data.DomandaComprensione
import com.alessandro.tedesco.data.FraseImmersione
import com.alessandro.tedesco.data.FraseShadowing
import com.alessandro.tedesco.data.StoriaImmersione
import com.alessandro.tedesco.data.UnitaGrammatica
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.delay

/**
 * Modalità Immersione: una sessione guidata di ~20 minuti in 5 tappe.
 *
 * Non è una pagina da scorrere: è un percorso con un passo attivo alla volta,
 * una barra di avanzamento sempre visibile e un player audio integrato.
 *
 *  1. Bussola  — perché il tedesco è logico (2 min)
 *  2. Input    — episodio della serie, 3 parole pre-insegnate, comprensione (6 min)
 *  3. Raccolta — 5 frasi con la parola nascosta: recupero a memoria + feedback (5 min)
 *  4. Shadowing— ripetizione a voce alta, lenta e poi a velocità piena (4 min)
 *  5. Ascolto  — playlist in loop, timer, da svegli prima di dormire (3 min)
 *
 * Didattica: input al 95%+ di parole note, narrow reading (stessi personaggi),
 * retrieval practice con feedback, ri-ascolto dello stesso materiale.
 */
@Composable
fun DidatticaScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {},
    onVaiAProduzione: () -> Unit = {},
    onVaiAUnitaGrammatica: (String) -> Unit = {}
) {
    val context = LocalContext.current
    val tts = rememberTtsHelper(context)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.codiceBreve() ?: "A1"

    var tappa by rememberSaveable(livello) { mutableIntStateOf(0) }
    var giro by rememberSaveable(livello) { mutableIntStateOf(0) }

    val storia = remember(livello, giro) { DidatticaImmersione.storiaAttiva(livello, giro) }
    val regole = remember(storia) { DidatticaImmersione.regoleDellaStoria(storia) }
    val miniera = remember(livello, giro) { DidatticaImmersione.frasiNuove(livello, giro) }
    val composti = remember(livello) { DidatticaImmersione.composti(livello) }
    val shadowing = remember(livello) { DidatticaImmersione.shadowing(livello) }
    val playlist = remember(livello, giro) { DidatticaImmersione.frasiPassive(livello) }

    // --- Stato della storia (si azzera a ogni nuova sessione) ---
    var traduzioniVisibili by rememberSaveable(giro) { mutableStateOf(false) }
    val rivelate = remember(giro) { mutableStateMapOf<Int, Boolean>() }
    val scelte = remember(giro) { mutableStateMapOf<Int, Int>() }

    // --- Stato della raccolta ---
    val significatiAperti = remember(giro) { mutableStateMapOf<Int, Boolean>() }
    val giudizi = remember(giro) { mutableStateMapOf<Int, Boolean>() }

    // --- Stato dello shadowing ---
    var indiceShadow by rememberSaveable(giro) { mutableIntStateOf(0) }
    val ripetizioni = remember(giro) { mutableStateMapOf<Int, Int>() }

    // --- Player ---
    var letturaAttiva by remember { mutableStateOf(false) }
    var indiceLettura by remember { mutableIntStateOf(0) }
    var ascoltoAttivo by remember { mutableStateOf(false) }
    var indiceAscolto by remember { mutableIntStateOf(0) }
    var timerMinuti by rememberSaveable { mutableIntStateOf(0) }
    var loopAscolto by rememberSaveable { mutableStateOf(true) }
    var velocitaAscolto by rememberSaveable { mutableFloatStateOf(0.9f) }

    // --- Voce tedesca: avviso solo dopo il primo tentativo andato a vuoto ---
    var haToccatoAudio by remember { mutableStateOf(false) }
    var avvisoVoce by remember { mutableStateOf(false) }
    LaunchedEffect(haToccatoAudio) {
        if (!haToccatoAudio) return@LaunchedEffect
        delay(3_000)
        avvisoVoce = !TtsCondiviso.linguaPronta
    }

    fun ascolta(testo: String, velocita: Float = 1f) {
        haToccatoAudio = true
        tts.speak(testo, velocita)
    }

    // Cambiare tappa ferma sempre l'audio: niente voce che continua altrove.
    LaunchedEffect(tappa) {
        letturaAttiva = false
        ascoltoAttivo = false
        tts.stop()
    }
    DisposableEffect(Unit) {
        onDispose { tts.stop() }
    }

    // Lettura sequenziale della storia (evidenzia la frase in corso).
    LaunchedEffect(letturaAttiva, indiceLettura, storia) {
        if (!letturaAttiva) return@LaunchedEffect
        if (indiceLettura >= storia.frasi.size) {
            letturaAttiva = false
            return@LaunchedEffect
        }
        ascolta(storia.frasi[indiceLettura].tedesco, 0.9f)
        var attesa = 0L
        while (letturaAttiva && !tts.isSpeaking && attesa < 3_000L) {
            delay(100)
            attesa += 100
        }
        if (!tts.isSpeaking) {
            letturaAttiva = false
            avvisoVoce = true
            return@LaunchedEffect
        }
        while (letturaAttiva && tts.isSpeaking) delay(120)
        if (!letturaAttiva) return@LaunchedEffect
        delay(650)
        if (letturaAttiva) indiceLettura += 1
    }

    // Ascolto passivo: una frase alla volta, pausa proporzionale alla lunghezza.
    LaunchedEffect(ascoltoAttivo, indiceAscolto, playlist, velocitaAscolto) {
        if (!ascoltoAttivo) return@LaunchedEffect
        if (indiceAscolto >= playlist.size) {
            if (loopAscolto) {
                indiceAscolto = 0
            } else {
                ascoltoAttivo = false
            }
            return@LaunchedEffect
        }
        val frase = playlist[indiceAscolto]
        ascolta(frase, velocitaAscolto)
        var attesa = 0L
        while (ascoltoAttivo && !tts.isSpeaking && attesa < 3_000L) {
            delay(100)
            attesa += 100
        }
        if (!tts.isSpeaking) {
            ascoltoAttivo = false
            avvisoVoce = true
            return@LaunchedEffect
        }
        while (ascoltoAttivo && tts.isSpeaking) delay(150)
        if (!ascoltoAttivo) return@LaunchedEffect
        val pausa = (frase.length * 55L).coerceIn(1_200L, 6_500L)
        var riposata = 0L
        while (ascoltoAttivo && riposata < pausa) {
            delay(100)
            riposata += 100
        }
        if (ascoltoAttivo) indiceAscolto += 1
    }

    // Timer serale: spegne la playlist dopo 15/30 minuti.
    LaunchedEffect(ascoltoAttivo, timerMinuti) {
        if (!ascoltoAttivo || timerMinuti <= 0) return@LaunchedEffect
        var secondi = 0
        while (ascoltoAttivo && secondi < timerMinuti * 60) {
            delay(1_000)
            secondi += 1
        }
        if (ascoltoAttivo) {
            ascoltoAttivo = false
            tts.stop()
        }
    }

    fun nuovaSessione() {
        tts.stop()
        letturaAttiva = false
        ascoltoAttivo = false
        indiceLettura = 0
        indiceAscolto = 0
        indiceShadow = 0
        giro += 1
        tappa = 0
    }

    val tappaInfo = Tappa.entries.getOrNull(tappa)

    Scaffold(
        topBar = { CompactTopBar(t("Modalità Immersione"), onBack = onIndietro) },
        bottomBar = {
            BarraNavigazione(
                tappa = tappa,
                onIndietro = { if (tappa > 0) tappa -= 1 else onIndietro() },
                onAvanti = { if (tappa < 5) tappa += 1 },
                onChiudi = onIndietro
            )
        },
        contentWindowInsets = WindowInsets(0)
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
        ) {
            IntestazioneSessione(
                tappa = tappa,
                livello = DidatticaImmersione.perLivello(livello),
                storia = storia,
                tappaInfo = tappaInfo
            )
            Box(Modifier.weight(1f)) {
                AnimatedContent(
                    targetState = tappa,
                    transitionSpec = {
                        if (targetState > initialState) {
                            (slideInHorizontally { it / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it / 4 } + fadeOut())
                        } else {
                            (slideInHorizontally { -it / 4 } + fadeIn()) togetherWith
                                (slideOutHorizontally { it / 4 } + fadeOut())
                        }
                    },
                    label = "tappa",
                    modifier = Modifier.fillMaxSize()
                ) { corrente ->
                    when (corrente) {
                        0 -> PassoBussola(
                            livello = DidatticaImmersione.perLivello(livello),
                            storia = storia,
                            onInizia = { tappa = 1 }
                        )
                        1 -> PassoInput(
                            storia = storia,
                            regole = regole,
                            onApriRegola = onVaiAUnitaGrammatica,
                            traduzioniVisibili = traduzioniVisibili,
                            onToggleTraduzioni = { traduzioniVisibili = !traduzioniVisibili },
                            letturaAttiva = letturaAttiva,
                            indiceLettura = indiceLettura,
                            onPlayTutto = {
                                indiceLettura = 0
                                letturaAttiva = true
                            },
                            onFerma = {
                                letturaAttiva = false
                                tts.stop()
                            },
                            onAscoltaFrase = { testo ->
                                letturaAttiva = false
                                ascolta(testo)
                            },
                            scelte = scelte,
                            onRispondi = { indice, opzione -> scelte[indice] = opzione },
                            avvisoVoce = avvisoVoce
                        )
                        2 -> PassoRaccolta(
                            frasi = miniera,
                            composti = composti,
                            significatiAperti = significatiAperti,
                            giudizi = giudizi,
                            onApri = { i -> significatiAperti[i] = true },
                            onGiudica = { i, sapeva -> giudizi[i] = sapeva },
                            onAscolta = { ascolta(it) }
                        )
                        3 -> PassoShadowing(
                            frasi = shadowing,
                            indice = indiceShadow.coerceIn(0, (shadowing.size - 1).coerceAtLeast(0)),
                            ripetizioni = ripetizioni,
                            onIndice = { indiceShadow = it },
                            onRipeti = { i -> ripetizioni[i] = (ripetizioni[i] ?: 0) + 1 },
                            onAscolta = { testo, velocita -> ascolta(testo, velocita) }
                        )
                        4 -> PassoAscolto(
                            playlist = playlist,
                            attivo = ascoltoAttivo,
                            indice = indiceAscolto,
                            timerMinuti = timerMinuti,
                            loop = loopAscolto,
                            velocita = velocitaAscolto,
                            avvisoVoce = avvisoVoce,
                            onPlay = {
                                indiceAscolto = 0
                                ascoltoAttivo = true
                            },
                            onFerma = {
                                ascoltoAttivo = false
                                tts.stop()
                            },
                            onTimer = { timerMinuti = it },
                            onLoop = { loopAscolto = it },
                            onVelocita = { velocitaAscolto = it }
                        )
                        else -> PassoRiepilogo(
                            storia = storia,
                            regole = regole,
                            onApriRegola = onVaiAUnitaGrammatica,
                            frasiRaccolte = miniera.size,
                            frasiShadowing = shadowing.size,
                            frasiAscolto = playlist.size,
                            daRivedere = giudizi.count { (_, sapeva) -> !sapeva },
                            onProduzione = onVaiAProduzione,
                            onNuovaSessione = { nuovaSessione() },
                            onHome = onIndietro
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// Struttura della sessione
// ----------------------------------------------------------------

private enum class Tappa(
    val titolo: String,
    val sottotitolo: String,
    val minuti: Int,
    val icona: ImageVector
) {
    BUSSOLA(t("Bussola"), t("Il tedesco è logico"), 2, Icons.Filled.Lightbulb),
    INPUT("Input", t("Leggi e ascolta"), 6, Icons.AutoMirrored.Filled.MenuBook),
    RACCOLTA(t("Raccolta"), t("Parole dentro le frasi"), 5, Icons.Filled.Bookmark),
    SHADOWING("Shadowing", t("Ripeti ad alta voce"), 4, Icons.Filled.GraphicEq),
    ASCOLTO("Ascolto", t("Prima di dormire"), 3, Icons.Filled.Headphones)
}

@Composable
private fun IntestazioneSessione(
    tappa: Int,
    livello: String,
    storia: StoriaImmersione,
    tappaInfo: Tappa?
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = spaziaturaSchermo(), vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.primaryContainer
            ) {
                Text(
                    livello,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
            Spacer(Modifier.width(8.dp))
            Text(
                "${storia.serie} · episodio ${storia.episodio}",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.weight(1f))
            Text(
                if (tappaInfo != null) "~${tappaInfo.minuti} min" else "20 min",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(5) { i ->
                val colore by animateColorAsState(
                    targetValue = when {
                        i < tappa -> MaterialTheme.colorScheme.primary
                        i == tappa -> MaterialTheme.colorScheme.primary
                        else -> MaterialTheme.colorScheme.outlineVariant
                    },
                    label = "segmento$i"
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(5.dp)
                        .clip(CircleShape)
                        .background(colore)
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            if (tappaInfo != null) {
                "Passo ${tappa + 1} di 5 · ${tappaInfo.titolo} — ${tappaInfo.sottotitolo}"
            } else {
                t("Sessione completata · 5 passi su 5")
            },
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun BarraNavigazione(
    tappa: Int,
    onIndietro: () -> Unit,
    onAvanti: () -> Unit,
    onChiudi: () -> Unit
) {
    Surface(
        tonalElevation = 3.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = spaziaturaSchermo(), vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onIndietro) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(if (tappa == 0) t("Esci") else t("Indietro"))
            }
            Spacer(Modifier.weight(1f))
            if (tappa < 5) {
                Button(
                    onClick = onAvanti,
                    modifier = Modifier.heightIn(min = 48.dp)
                ) {
                    Text(if (tappa == 4) t("Concludi") else t("Avanti"))
                    Spacer(Modifier.width(6.dp))
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
                }
            } else {
                Button(onClick = onChiudi, modifier = Modifier.heightIn(min = 48.dp)) {
                    Text(t("Torna alla home"))
                }
            }
        }
    }
}

private val paddingPasso = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp)

// ----------------------------------------------------------------
// 1. Bussola
// ----------------------------------------------------------------

@Composable
private fun PassoBussola(
    livello: String,
    storia: StoriaImmersione,
    onInizia: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 4.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.secondaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
            ) {
                Column(Modifier.padding(22.dp)) {
                    Text(
                        "SERIE · ${storia.serie.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        t("Il tedesco non è caos."),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        t("Un sistema di mattoni: se capisci il mattone, capisci la casa. Cinque nuovi oggi, senza tradurre parola per parola."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                    Spacer(Modifier.height(14.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        EtichettaHero("Livello $livello")
                        EtichettaHero("${storia.copertura}% parole note")
                    }
                }
            }
        }

        item {
            Text(
                t("Oggi in cinque tappe"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
        }

        itemsIndexed(Tappa.entries) { i, t ->
            RigaTappa(
                numero = i + 1,
                icona = t.icona,
                titolo = t.titolo,
                descrizione = when (t) {
                    Tappa.BUSSOLA -> t("Perché funziona: input prima, produzione dopo.")
                    Tappa.INPUT -> "Episodio \"${storia.titolo}\" con tre parole pre-insegnate."
                    Tappa.RACCOLTA -> t("Cinque frasi: il significato lo recuperi tu.")
                    Tappa.SHADOWING -> t("La voce del nativo: ascolti e ripeti sopra.")
                    Tappa.ASCOLTO -> t("Playlist in loop, anche con timer serale.")
                },
                minuti = t.minuti
            )
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        t("Regola d'oro: 80% input (leggere e ascoltare), 20% produzione. Qui fai l'80%; la scrittura la trovi in Produzione."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = onInizia,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 54.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(t("Inizia la sessione"))
            }
        }
    }
}

@Composable
private fun EtichettaHero(testo: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Text(
            testo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RigaTappa(
    numero: Int,
    icona: ImageVector,
    titolo: String,
    descrizione: String,
    minuti: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icona,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(19.dp)
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    "$numero. $titolo",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.weight(1f))
                Text(
                    "$minuti min",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                descrizione,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ----------------------------------------------------------------
// 2. Input: storia + comprensione
// ----------------------------------------------------------------

@Composable
private fun PassoInput(
    storia: StoriaImmersione,
    regole: List<UnitaGrammatica>,
    onApriRegola: (String) -> Unit,
    traduzioniVisibili: Boolean,
    onToggleTraduzioni: () -> Unit,
    letturaAttiva: Boolean,
    indiceLettura: Int,
    onPlayTutto: () -> Unit,
    onFerma: () -> Unit,
    onAscoltaFrase: (String) -> Unit,
    scelte: Map<Int, Int>,
    onRispondi: (Int, Int) -> Unit,
    avvisoVoce: Boolean
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingPasso,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        "EPISODIO ${storia.episodio} · ${storia.serie.uppercase()}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        storia.titolo,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        storia.contesto,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistenzaEtichetta("${storia.copertura}% parole note")
                        Spacer(Modifier.width(8.dp))
                        Spacer(Modifier.weight(1f))
                        FilledTonalButton(
                            onClick = if (letturaAttiva) onFerma else onPlayTutto
                        ) {
                            Icon(
                                if (letturaAttiva) Icons.Filled.Stop else Icons.Filled.PlayArrow,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(if (letturaAttiva) t("Ferma") else t("Ascolta tutto"))
                        }
                    }
                }
            }
        }

        if (regole.isNotEmpty()) {
            item {
                RegoleCollegate(regole = regole, onApri = onApriRegola)
            }
        }

        if (avvisoVoce) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        t("La voce tedesca non risponde: il telefono non ha un motore di sintesi con l'audio in tedesco. Controlla Impostazioni di sistema → Sintesi vocale. La sessione funziona lo stesso, ma senza audio."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item {
            Text(
                t("Prima di leggere: tre parole"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                t("Pre-insegnarle tiene il testo sopra la soglia del 95% di parole note: capisci la storia, non la decifri."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        itemsIndexed(storia.paroleChiave) { _, g ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceContainer)
                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp, end = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    g.parola,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    " = ${g.significato}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { onAscoltaFrase(g.parola) }) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = "Ascolta ${g.parola}",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(Modifier.padding(vertical = 8.dp)) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, end = 8.dp, top = 6.dp, bottom = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            t("La storia"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        TextButton(onClick = onToggleTraduzioni) {
                            Icon(
                                if (traduzioniVisibili) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(t("Traduzioni"))
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    storia.frasi.forEachIndexed { i, frase ->
                        RigaStoria(
                            frase = frase,
                            inRiproduzione = letturaAttiva && i == indiceLettura,
                            traduzioneVisibile = traduzioniVisibili,
                            onAscolta = { onAscoltaFrase(frase.tedesco) }
                        )
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                t("Hai capito?"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                t("Rispondi a memoria, senza rileggere. Il feedback spiega il perché."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        itemsIndexed(storia.domande) { indice, domanda ->
            SchedaDomanda(
                numero = indice + 1,
                domanda = domanda,
                scelta = scelte[indice],
                onScegli = { opzione -> onRispondi(indice, opzione) }
            )
        }
    }
}

/**
 * Le regole grammaticali dietro il testo appena letto.
 *
 * L'immersione non spiega la grammatica: la mostra. Questo blocco dà il
 * "dove guardare" e apre l'unità che la spiega, con teoria ed esercizi.
 */
@Composable
private fun RegoleCollegate(
    regole: List<UnitaGrammatica>,
    onApri: (String) -> Unit
) {
    if (regole.isEmpty()) return
    Column(Modifier.fillMaxWidth()) {
        Text(
            t("La grammatica dietro il testo"),
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        regole.forEach { unita ->
            Surface(
                onClick = { onApri(unita.id) },
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.MenuBook,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(19.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            unita.titolo,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                        Text(
                            "${unita.livello} · ${unita.minuti} min · regola ed esercizi",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
        }
        Text(
            "L'immersione fa riconoscere la struttura; la regola spiega perché funziona. Usale insieme: prima la regola, poi il testo.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun AssistenzaEtichetta(testo: String) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Text(
            testo,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

@Composable
private fun RigaStoria(
    frase: FraseImmersione,
    inRiproduzione: Boolean,
    traduzioneVisibile: Boolean,
    onAscolta: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (inRiproduzione) {
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)
                } else {
                    Color.Transparent
                }
            )
            .clickable(
                onClickLabel = t("Ascolta la frase"),
                role = Role.Button,
                onClick = onAscolta
            )
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                frase.tedesco,
                style = MaterialTheme.typography.bodyLarge,
                lineHeight = 24.sp,
                fontWeight = if (inRiproduzione) FontWeight.SemiBold else FontWeight.Normal,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(8.dp))
            Icon(
                if (inRiproduzione) Icons.Filled.GraphicEq else Icons.AutoMirrored.Filled.VolumeUp,
                contentDescription = null,
                tint = if (inRiproduzione) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                },
                modifier = Modifier.size(17.dp)
            )
        }
        if (traduzioneVisibile) {
            Text(
                frase.italiano,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 2.dp)
            )
        }
    }
}

@Composable
private fun SchedaDomanda(
    numero: Int,
    domanda: DomandaComprensione,
    scelta: Int?,
    onScegli: (Int) -> Unit
) {
    val rispostaGiusta = domanda.corretta(scelta)
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                "$numero. ${domanda.domanda}",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(8.dp))
            domanda.opzioni.forEachIndexed { i, opzione ->
                val selezionata = scelta == i
                val corretta = scelta != null && i == domanda.rispostaCorretta
                val sfondo = when {
                    corretta -> MaterialTheme.colorScheme.primaryContainer
                    selezionata -> MaterialTheme.colorScheme.errorContainer
                    else -> Color.Transparent
                }
                val coloreTesto = when {
                    corretta -> MaterialTheme.colorScheme.onPrimaryContainer
                    selezionata -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurface
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(sfondo)
                        .clickable(enabled = scelta == null) { onScegli(i) }
                        .padding(horizontal = 10.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(22.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    corretta -> MaterialTheme.colorScheme.primary
                                    selezionata -> MaterialTheme.colorScheme.error
                                    else -> MaterialTheme.colorScheme.surfaceVariant
                                }
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (corretta || selezionata) {
                            Icon(
                                if (corretta) Icons.Filled.Check else Icons.Filled.Close,
                                contentDescription = null,
                                tint = if (corretta) {
                                    MaterialTheme.colorScheme.onPrimary
                                } else {
                                    MaterialTheme.colorScheme.onError
                                },
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                    Spacer(Modifier.width(10.dp))
                    Text(
                        opzione,
                        style = MaterialTheme.typography.bodyMedium,
                        color = coloreTesto,
                        fontWeight = if (corretta || selezionata) FontWeight.SemiBold else FontWeight.Normal
                    )
                }
                Spacer(Modifier.height(2.dp))
            }
            if (scelta != null && domanda.spiegazione.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        if (rispostaGiusta) Icons.Filled.CheckCircle else Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = if (rispostaGiusta) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        domanda.spiegazione,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// 3. Raccolta: retrieval practice + composti
// ----------------------------------------------------------------

@Composable
private fun PassoRaccolta(
    frasi: List<FraseImmersione>,
    composti: List<Composto>,
    significatiAperti: Map<Int, Boolean>,
    giudizi: Map<Int, Boolean>,
    onApri: (Int) -> Unit,
    onGiudica: (Int, Boolean) -> Unit,
    onAscolta: (String) -> Unit
) {
    val daRivedere = giudizi.count { (_, sapeva) -> !sapeva }
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingPasso,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                t("Cinque frasi, cinque parole"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("La parola è evidenziata. Prima recupera il significato a memoria, poi tocca la frase per controllare: è il recupero attivo che fa restare la parola, non la rilettura."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        itemsIndexed(frasi) { i, frase ->
            val aperta = significatiAperti[i] == true
            val giudizio = giudizi[i]
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .clickable { if (!aperta) onApri(i) },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when (giudizio) {
                        true -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        false -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
                        null -> MaterialTheme.colorScheme.surfaceContainer
                    }
                )
            ) {
                Column(Modifier.padding(16.dp).animateContentSize()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "FRASE ${i + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.weight(1f))
                        IconButton(onClick = { onAscolta(frase.tedesco) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = t("Ascolta la frase"),
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    Text(
                        evidenzia(
                            testo = frase.tedesco,
                            parola = frase.parolaChiave,
                            colore = MaterialTheme.colorScheme.primary
                        ),
                        style = MaterialTheme.typography.bodyLarge,
                        lineHeight = 25.sp
                    )
                    Spacer(Modifier.height(6.dp))
                    if (!aperta) {
                        Text(
                            t("Tocca per il significato"),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Text(
                            frase.italiano,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "${frase.parolaChiave} = ${frase.significatoChiave}",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = giudizio == true,
                                onClick = { onGiudica(i, true) },
                                label = { Text(t("La sapevo")) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                            FilterChip(
                                selected = giudizio == false,
                                onClick = { onGiudica(i, false) },
                                label = { Text(t("Da rivedere")) },
                                leadingIcon = {
                                    Icon(Icons.Filled.Repeat, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.height(4.dp))
            Text(
                t("Composti: ogni pezzo apre dieci porte"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                t("Non memorizzare la parola intera: guarda i mattoni. Fenster + Bank = davanzale, e Fenster lo ritrovi in dieci parole."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        itemsIndexed(composti) { _, c ->
            SchedaComposto(c)
        }

        if (daRivedere > 0) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.Repeat,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(Modifier.width(10.dp))
                        Text(
                            "$daRivedere da rivedere: le ritrovi nel ripasso e nell'ascolto di oggi. Nessun giudizio, solo un promemoria.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SchedaComposto(c: Composto) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(
                c.composto,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                c.parti.forEachIndexed { i, parte ->
                    if (i > 0) {
                        Text(
                            "+",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Text(
                            parte,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    c.italiano,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(Modifier.height(2.dp))
            Text(
                c.parti.zip(c.significati).joinToString(" + ") { it.second },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/** Evidenzia la parola chiave dentro la frase, senza perdere gli accenti. */
private fun evidenzia(testo: String, parola: String, colore: Color): AnnotatedString {
    if (parola.isBlank()) return AnnotatedString(testo)
    val inizio = testo.indexOf(parola, ignoreCase = true)
    if (inizio < 0) return AnnotatedString(testo)
    return buildAnnotatedString {
        append(testo.substring(0, inizio))
        withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = colore)) {
            append(testo.substring(inizio, inizio + parola.length))
        }
        append(testo.substring(inizio + parola.length))
    }
}

// ----------------------------------------------------------------
// 4. Shadowing
// ----------------------------------------------------------------

@Composable
private fun PassoShadowing(
    frasi: List<FraseShadowing>,
    indice: Int,
    ripetizioni: Map<Int, Int>,
    onIndice: (Int) -> Unit,
    onRipeti: (Int) -> Unit,
    onAscolta: (String, Float) -> Unit
) {
    if (frasi.isEmpty()) return
    val frase = frasi[indice.coerceIn(0, frasi.size - 1)]
    val fatte = (ripetizioni[indice] ?: 0).coerceAtMost(3)

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingPasso,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                t("Ombreggia la voce"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("Ascolta prima piano, poi a velocità piena, e ripeti sopra la voce senza aspettare la fine. Non serve capire ogni parola: serve il ritmo."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        "FRASE ${indice + 1} DI ${frasi.size}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                    )
                    Spacer(Modifier.height(10.dp))
                    Text(
                        frase.tedesco,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 34.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        frase.italiano,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.GraphicEq,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(
                            t("Ritmo"),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            frase.notaRitmo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { onAscolta(frase.tedesco, 0.6f) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(
                        Icons.AutoMirrored.Filled.VolumeUp,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(t("Piano"))
                }
                Button(
                    onClick = { onAscolta(frase.tedesco, 1f) },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(t("Ascolta"))
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            t("Ripetizioni"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.weight(1f))
                        repeat(3) { i ->
                            Box(
                                modifier = Modifier
                                    .padding(start = 4.dp)
                                    .size(12.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (i < fatte) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outlineVariant
                                    )
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        if (fatte >= 3) {
                            t("Tre ripetizioni fatte: la frase è tua. Passa alla prossima o ripeti ancora.")
                        } else {
                            t("Ripeti ad alta voce e tocca il pulsante: tre volte è il minimo per scaldare la bocca.")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    Button(
                        onClick = { onRipeti(indice) },
                        enabled = fatte < 3,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text(if (fatte < 3) "Ho ripetuto (${fatte}/3)" else t("Fatto (3/3)"))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { onIndice((indice - 1).coerceAtLeast(0)) },
                    enabled = indice > 0,
                    modifier = Modifier.weight(1f)
                ) { Text(t("Precedente")) }
                OutlinedButton(
                    onClick = { onIndice((indice + 1).coerceAtMost(frasi.size - 1)) },
                    enabled = indice < frasi.size - 1,
                    modifier = Modifier.weight(1f)
                ) { Text(t("Prossima")) }
            }
        }
    }
}

// ----------------------------------------------------------------
// 5. Ascolto passivo
// ----------------------------------------------------------------

@Composable
private fun PassoAscolto(
    playlist: List<String>,
    attivo: Boolean,
    indice: Int,
    timerMinuti: Int,
    loop: Boolean,
    velocita: Float,
    avvisoVoce: Boolean,
    onPlay: () -> Unit,
    onFerma: () -> Unit,
    onTimer: (Int) -> Unit,
    onLoop: (Boolean) -> Unit,
    onVelocita: (Float) -> Unit
) {
    val inCorso = playlist.getOrNull(indice)
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingPasso,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                t("Ascolta, poi dormi"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("Il bonus pre-sonno: ascolta da sveglio, con le luci basse, e poi dormi. È il sonno a consolidare ciò che hai ascoltato, non l'audio durante il sonno."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (avvisoVoce) {
            item {
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        t("La voce tedesca non risponde: controlla Impostazioni di sistema → Sintesi vocale."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        IconButton(
                            onClick = if (attivo) onFerma else onPlay,
                            modifier = Modifier.size(76.dp)
                        ) {
                            Icon(
                                if (attivo) Icons.Filled.Pause else Icons.Filled.PlayArrow,
                                contentDescription = if (attivo) t("Metti in pausa") else t("Avvia l'ascolto"),
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(38.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    Text(
                        if (attivo) t("In ascolto…") else t("Pronto"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        inCorso ?: t("La playlist del livello"),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(10.dp))
                    LinearProgressIndicator(
                        progress = {
                            if (playlist.isEmpty()) 0f
                            else ((indice + if (attivo) 1 else 0).toFloat() / playlist.size).coerceIn(0f, 1f)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(CircleShape),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "${(indice + 1).coerceAtMost(playlist.size)} di ${playlist.size} frasi",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Text(
                t("Velocità"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = velocita < 0.85f,
                    onClick = { onVelocita(0.8f) },
                    label = { Text(t("Lenta")) }
                )
                FilterChip(
                    selected = velocita >= 0.85f,
                    onClick = { onVelocita(0.9f) },
                    label = { Text(t("Naturale")) }
                )
            }
        }

        item {
            Text(
                t("Timer serale"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = timerMinuti == 0,
                    onClick = { onTimer(0) },
                    label = { Text("Off") }
                )
                FilterChip(
                    selected = timerMinuti == 15,
                    onClick = { onTimer(15) },
                    label = { Text("15 min") },
                    leadingIcon = {
                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
                FilterChip(
                    selected = timerMinuti == 30,
                    onClick = { onTimer(30) },
                    label = { Text("30 min") },
                    leadingIcon = {
                        Icon(Icons.Filled.Timer, contentDescription = null, modifier = Modifier.size(16.dp))
                    }
                )
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceContainer
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Repeat,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            t("Ripeti la playlist"),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            t("Alla fine ricomincia dalla prima frase"),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = loop, onCheckedChange = onLoop)
                }
            }
        }
    }
}

// ----------------------------------------------------------------
// Riepilogo
// ----------------------------------------------------------------

@Composable
private fun PassoRiepilogo(
    storia: StoriaImmersione,
    regole: List<UnitaGrammatica>,
    onApriRegola: (String) -> Unit,
    frasiRaccolte: Int,
    frasiShadowing: Int,
    frasiAscolto: Int,
    daRivedere: Int,
    onProduzione: () -> Unit,
    onNuovaSessione: () -> Unit,
    onHome: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = paddingPasso,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(28.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(
                                MaterialTheme.colorScheme.primaryContainer,
                                MaterialTheme.colorScheme.tertiaryContainer
                            )
                        )
                    )
            ) {
                Column(Modifier.padding(22.dp)) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        t("Sessione completata"),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "${storia.titolo} · ${storia.serie}, episodio ${storia.episodio}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                    )
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                )
            ) {
                Column(Modifier.padding(18.dp)) {
                    Text(
                        t("Cosa hai fatto"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(10.dp))
                    RigaRiepilogo(t("Storia letta e ascoltata"), "${storia.frasi.size} frasi")
                    RigaRiepilogo(t("Parole raccolte in frasi"), "$frasiRaccolte")
                    RigaRiepilogo(t("Frasi ombreggiate"), "$frasiShadowing")
                    RigaRiepilogo(t("Frasi in playlist"), "$frasiAscolto")
                    if (daRivedere > 0) {
                        RigaRiepilogo(t("Da rivedere"), "$daRivedere")
                    }
                }
            }
        }

        if (regole.isNotEmpty()) {
            item {
                RegoleCollegate(regole = regole, onApri = onApriRegola)
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.secondaryContainer
            ) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "Hai fatto l'80%: input, raccolta, voce e ascolto. Il 20% che chiude il cerchio è produrre: scrivi due frasi tue con le parole di oggi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
        }

        item {
            Button(
                onClick = onProduzione,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Text(t("Vai alla produzione (20%)"))
                Spacer(Modifier.width(6.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
        item {
            OutlinedButton(
                onClick = onNuovaSessione,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 52.dp)
            ) {
                Icon(Icons.Filled.Repeat, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
                Text(t("Nuova sessione (altro episodio)"))
            }
        }
        item {
            TextButton(
                onClick = onHome,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(t("Torna alla home"))
            }
        }
        item {
            Text(
                t("Prossima sessione: stesso mondo, episodio nuovo. La ripetizione dei personaggi e delle strutture è voluta: è il narrow reading."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontStyle = FontStyle.Italic
            )
        }
    }
}

@Composable
private fun RigaRiepilogo(etichetta: String, valore: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            etichetta,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(1f)
        )
        Text(
            valore,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
    }
}
