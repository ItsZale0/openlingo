package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import android.Manifest
import androidx.compose.animation.animateContentSize
import android.content.Context
import com.alessandro.tedesco.data.RotoreEsercizi
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.alessandro.tedesco.data.Pronuncia
import com.alessandro.tedesco.ui.theme.Spaziature
import java.io.File
import java.util.Locale

enum class SezionePronuncia(val etichetta: String) {
    COPPIE("Coppie minime"),
    FRASI(t("Frasi")),
    CONSIGLI(t("Regole"))
}

/** Registratore audio minimale: un solo file in cache, riascoltabile. */
class RegistratoreAudio(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var player: MediaPlayer? = null
    var ultimoFile: File? = null
        private set

    val inRegistrazione: Boolean get() = recorder != null

    @Suppress("DEPRECATION")
    fun avvia(): File? {
        if (recorder != null) return ultimoFile
        val file = File(context.cacheDir, "pronuncia_${System.currentTimeMillis()}.m4a")
        val r = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(context)
        else MediaRecorder()
        return try {
            r.setAudioSource(MediaRecorder.AudioSource.MIC)
            r.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            r.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            r.setAudioSamplingRate(44100)
            r.setAudioEncodingBitRate(96000)
            r.setOutputFile(file.absolutePath)
            r.prepare()
            r.start()
            recorder = r
            ultimoFile = file
            file
        } catch (e: Exception) {
            runCatching { r.release() }
            recorder = null
            null
        }
    }

    fun ferma() {
        val r = recorder ?: return
        recorder = null
        runCatching { r.stop() }
        runCatching { r.release() }
    }

    fun riproduci(file: File) {
        fermaRiproduzione()
        player = MediaPlayer().apply {
            runCatching {
                setDataSource(file.absolutePath)
                setOnCompletionListener { release() }
                prepare()
                start()
            }
        }
    }

    fun fermaRiproduzione() {
        player?.let { runCatching { it.release() } }
        player = null
    }

    fun rilascia() {
        ferma()
        fermaRiproduzione()
    }
}

/**
 * Pronuncia: coppie minime, scioglilingua, frasi cliniche e regole.
 * Si ascolta il modello tedesco (anche rallentato) e ci si registra per
 * confrontare la propria produzione.
 */
@Composable
fun PronunciaScreen(
    onIndietro: () -> Unit = {}
) {
    val context = LocalContext.current
    val tts = rememberTtsHelper(context, Locale.GERMAN)
    val registratore = remember { RegistratoreAudio(context) }
    var sezione by remember { mutableStateOf(SezionePronuncia.COPPIE) }
    var idAttivo by remember { mutableStateOf<String?>(null) }
    var registrando by remember { mutableStateOf(false) }
    var permessoNegato by remember { mutableStateOf(false) }
    var messaggio by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { concesso ->
        if (concesso) {
            permessoNegato = false
            if (registratore.avvia() != null) {
                registrando = true
                messaggio = t("Registrazione in corso: leggi la frase, poi premi Stop.")
            }
        } else {
            permessoNegato = true
        }
    }

    fun avviaRegistrazione(id: String) {
        tts.stop()
        registratore.fermaRiproduzione()
        val concesso = ContextCompat.checkSelfPermission(
            context, Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED
        if (!concesso) {
            idAttivo = id
            launcher.launch(Manifest.permission.RECORD_AUDIO)
            return
        }
        if (registratore.avvia() != null) {
            idAttivo = id
            registrando = true
            messaggio = t("Registrazione in corso: leggi la frase, poi premi Stop.")
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

    // Come in Sprechuebungen: niente microfono attivo quando l'app perde il
    // primo piano (Android ucciderebbe il processo e sembrerebbe un crash).
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val osservatore = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_STOP && registratore.inRegistrazione) {
                registratore.ferma()
                registrando = false
                messaggio = t("Registrazione interrotta: riascoltala e confrontala con il modello.")
            }
        }
        lifecycleOwner.lifecycle.addObserver(osservatore)
        onDispose { lifecycleOwner.lifecycle.removeObserver(osservatore) }
    }

    ScreenScaffold(t("Pronuncia"), onBack = onIndietro) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            Text(
                "Ascolta il modello (anche a 0,7x), ripeti e registrati. " +
                    t("Il riconoscimento vocale non basta: l'orecchio va allenato sulle coppie minime."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
            ) {
                SezionePronuncia.entries.forEach { s ->
                    FilterChip(
                        selected = s == sezione,
                        onClick = { sezione = s },
                        label = { Text(s.etichetta) }
                    )
                }
            }

            Column(Modifier.fillMaxWidth().animateContentSize()) {
                messaggio?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                if (permessoNegato) {
                    Text(
                        t("Permesso microfono negato: puoi comunque ascoltare i modelli."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            when (sezione) {
                SezionePronuncia.COPPIE -> {
                    RotoreEsercizi.ordine("pronuncia-coppie", Pronuncia.coppie.size)
                        .map { Pronuncia.coppie[it] }.forEach { coppia ->
                        val id = "${coppia.a}-${coppia.b}"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        coppia.a,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text("  vs  ", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(
                                        coppia.b,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    "${coppia.italianoA} / ${coppia.italianoB}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(coppia.nota, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                Spacer(Modifier.height(Spaziature.sm))
                                Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)) {
                                    OutlinedButton(onClick = { tts.speak(coppia.a, velocita = 0.7f) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(coppia.a, maxLines = 1)
                                    }
                                    OutlinedButton(onClick = { tts.speak(coppia.b, velocita = 0.7f) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(coppia.b, maxLines = 1)
                                    }
                                    OutlinedButton(onClick = {
                                        tts.speak("${coppia.a}. ${coppia.b}", velocita = 0.8f)
                                    }) {
                                        Icon(Icons.Filled.GraphicEq, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
                                }
                                Spacer(Modifier.height(Spaziature.sm))
                                RegistrazioneRow(
                                    attivo = idAttivo == id,
                                    registrando = registrando,
                                    haRegistrazione = registratore.ultimoFile != null,
                                    onRegistra = { avviaRegistrazione(id) },
                                    onFerma = { fermaRegistrazione() },
                                    onRiproduci = { registratore.ultimoFile?.let { registratore.riproduci(it) } }
                                )
                            }
                        }
                    }
                }

                SezionePronuncia.FRASI -> {
                    val frasi = Pronuncia.scioglilingua + Pronuncia.frasiCliniche
                    val idFrasi = remember { RotoreEsercizi.ordine("pronuncia-frasi", frasi.size) }
                    idFrasi.forEachIndexed { i, f ->
                        val frase = frasi[f]
                        val id = "frase_$f"
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                                Text(frase, style = MaterialTheme.typography.bodyLarge)
                                Spacer(Modifier.height(Spaziature.sm))
                                Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)) {
                                    OutlinedButton(onClick = { tts.speak(frase, velocita = 0.7f) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text("0,7x")
                                    }
                                    OutlinedButton(onClick = { tts.speak(frase, velocita = 1f) }) {
                                        Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                                        Spacer(Modifier.width(4.dp))
                                        Text(t("Normale"))
                                    }
                                }
                                Spacer(Modifier.height(Spaziature.sm))
                                RegistrazioneRow(
                                    attivo = idAttivo == id,
                                    registrando = registrando,
                                    haRegistrazione = registratore.ultimoFile != null,
                                    onRegistra = { avviaRegistrazione(id) },
                                    onFerma = { fermaRegistrazione() },
                                    onRiproduci = { registratore.ultimoFile?.let { registratore.riproduci(it) } }
                                )
                            }
                        }
                    }
                }

                SezionePronuncia.CONSIGLI -> {
                    Pronuncia.consigli.forEach { consiglio ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                                Text(
                                    consiglio.titolo,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(consiglio.testo, style = MaterialTheme.typography.bodyMedium)
                                Spacer(Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        "Esempio: ${consiglio.esempio}",
                                        style = MaterialTheme.typography.bodySmall,
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedButton(onClick = { tts.speak(consiglio.esempio, velocita = 0.8f) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                                    }
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
private fun RegistrazioneRow(
    attivo: Boolean,
    registrando: Boolean,
    haRegistrazione: Boolean,
    onRegistra: () -> Unit,
    onFerma: () -> Unit,
    onRiproduci: () -> Unit
) {
    if (registrando && attivo) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spaziature.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = onFerma,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Stop")
            }
            Text(t("Registrazione in corso..."), style = MaterialTheme.typography.bodySmall)
        }
    } else {
        Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
            OutlinedButton(onClick = onRegistra) {
                Icon(Icons.Filled.Mic, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text(if (haRegistrazione) t("Riregistra") else t("Registrati"))
            }
            if (haRegistrazione) {
                OutlinedButton(onClick = onRiproduci) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(t("Riascolta"))
                }
            }
        }
    }
}
