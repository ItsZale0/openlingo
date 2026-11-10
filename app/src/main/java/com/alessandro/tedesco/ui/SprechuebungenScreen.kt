package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.layout.*
import androidx.compose.animation.animateContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.RotoreEsercizi
import com.alessandro.tedesco.data.Sprechuebungen
import com.alessandro.tedesco.ui.theme.Spaziature
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

/**
 * Sprechübungen: esercizi di produzione orale. L'utente legge il prompt,
 * parla, si registra e confronta con la risposta modello (letta dal TTS).
 * Copre i formati del Goethe-Zertifikat (Sprechen Teil 1-3) e le
 * Partnerseiten della Grammatik Aktiv.
 */
@Composable
fun SprechuebungenScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    val context = LocalContext.current
    val tts = rememberTtsHelper(context, Locale.GERMAN)
    val registratore = remember { RegistratoreAudio(context) }
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livello = profilo?.stato?.progresso?.livelloCorrente?.codiceBreve() ?: "A1"
    val mostraMedico by vm.mostraContestoMedico.collectAsStateWithLifecycle()
    val contesto by vm.contestoUtente.collectAsStateWithLifecycle()
    val mostraSanitario = remember(mostraMedico, contesto) {
        Sprechuebungen.contestoSanitarioRichiesto(mostraMedico, contesto)
    }
    val prompts = remember(livello, mostraSanitario) { Sprechuebungen.perLivello(livello, mostraSanitario) }
    // Ogni giorno si riparte da un esercizio diverso del banco: la stessa
    // lista "gira" senza bisogno di salvare nulla.
    val indiciGiorno = remember(livello, prompts, mostraSanitario) {
        RotoreEsercizi.ordine(t("sprech-$livello-$mostraSanitario"), prompts.size)
    }
    var indice by remember(livello, mostraSanitario) { mutableIntStateOf(0) }
    var registrando by remember { mutableStateOf(false) }
    var messaggio by remember { mutableStateOf<String?>(null) }
    var permessoNegato by remember { mutableStateOf(false) }
    var valutazione by remember(indice) { mutableIntStateOf(0) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concesso ->
        if (concesso) {
            permessoNegato = false
            if (registratore.avvia() != null) {
                registrando = true
                messaggio = t("Registrazione in corso: parla, poi premi Stop.")
            }
        } else {
            permessoNegato = true
        }
    }

    fun avviaRegistrazione() {
        tts.stop()
        registratore.fermaRiproduzione()
        val concesso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!concesso) {
            launcher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (registratore.avvia() != null) {
            registrando = true
            messaggio = t("Registrazione in corso: parla, poi premi Stop.")
        } else {
            messaggio = t("Microfono non disponibile su questo dispositivo.")
        }
    }

    fun fermaRegistrazione() {
        registratore.ferma()
        registrando = false
        messaggio = t("Registrazione pronta: riascoltala e confrontala con il modello.")
    }

    DisposableEffect(Unit) {
        onDispose { registratore.rilascia(); tts.stop() }
    }

    // Se l'app perde il primo piano mentre registri (chiamata, switch app,
    // riempimento della batteria col microfono attivo), ferma subito la
    // registrazione: MediaRecorder attivo in background fa killare il processo
    // da Android e all'utente appare come una chiusura/crash improvvisa.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val osservatore = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP && registratore.inRegistrazione) {
                registratore.ferma()
                registrando = false
                messaggio = t("Registrazione interrotta: riascoltala e confronta con il modello.")
            }
        }
        lifecycleOwner.lifecycle.addObserver(osservatore)
        onDispose { lifecycleOwner.lifecycle.removeObserver(osservatore) }
    }

    val prompt = prompts.getOrNull(indiciGiorno.getOrNull(indice) ?: indice)

    ScreenScaffold("Sprechübungen", onBack = onIndietro) {
        if (prompt == null || prompts.isEmpty()) {
            Text(t("Nessun esercizio per questo livello."))
            return@ScreenScaffold
        }
        // Nessun verticalScroll qui: ScreenScaffold avvolge gia' il contenuto
        // in uno scroll verticale. Due scroll annidati fanno crashare Compose
        // (IllegalStateException: infinite max height constraints, v1.47.0).
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            Text(
                t("Esercizi di produzione orale: leggi la situazione, parla, registrati e confronta con il modello.") + " " + t("Livello ${prompt.livello}."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                t("${indice + 1} di ${prompts.size}"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(
                        t(prompt.situazione).uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        t(prompt.prompt),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        t("Vocabolario utile") + ": " + prompt.vocabolario.joinToString(", ") { t(it) },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            // Modello
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(t("Risposta modello"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(prompt.modello, style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
                        OutlinedButton(onClick = { tts.speak(prompt.modello, 0.8f) }) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("Ascolta (lento)"))
                        }
                        OutlinedButton(onClick = { tts.speak(prompt.modello, 1f) }) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("Ascolta"))
                        }
                    }
                }
            }

            // Registrazione
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(t("La tua registrazione"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
                        Button(
                            onClick = { if (registrando) fermaRegistrazione() else avviaRegistrazione() },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(if (registrando) Icons.Filled.Stop else Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(if (registrando) "Stop" else t("Registra"))
                        }
                        OutlinedButton(
                            onClick = { registratore.ultimoFile?.let { registratore.riproduci(it) } },
                            enabled = registratore.ultimoFile != null && !registrando,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text(t("Riascolta"))
                        }
                    }
                    Column(Modifier.fillMaxWidth().animateContentSize()) {
                        messaggio?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        if (permessoNegato) {
                            Spacer(Modifier.height(6.dp))
                            Text(t("Permesso microfono negato. Attivalo nelle impostazioni."), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }

            // Autovalutazione
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(t("Autovalutazione"), style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(t("Quanto è stata fluente la tua risposta? (1 = molto difficile, 5 = fluida)"), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        (1..5).forEach { v ->
                            FilterChip(
                                selected = valutazione == v,
                                onClick = { valutazione = v },
                                label = { Text(v.toString()) }
                            )
                        }
                    }
                }
            }

            // Navigazione
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
            ) {
                OutlinedButton(
                    onClick = { if (indice > 0) indice-- },
                    enabled = indice > 0,
                    modifier = Modifier.weight(1f)
                ) { Text(t("Precedente")) }
                Button(
                    onClick = { if (indice < prompts.size - 1) { indice++; valutazione = 0 } },
                    enabled = indice < prompts.size - 1,
                    modifier = Modifier.weight(1f)
                ) { Text(t("Prossimo")) }
            }
        }
    }
}
