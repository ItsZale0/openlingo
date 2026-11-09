package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.BuildConfig
import com.alessandro.tedesco.data.BackupCloud
import com.alessandro.tedesco.data.BackupEsecutore
import com.alessandro.tedesco.data.remote.BackupStato
import com.alessandro.tedesco.data.remote.BackupViewModel
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Backup cloud su GitHub. Carica tutti i profili (parole, ripassi, test,
 * progressi) su un repo privato e li ripristina.
 */
@Composable
fun BackupScreen(vm: BackupViewModel, onIndietro: () -> Unit) {
    val token by vm.token.collectAsStateWithLifecycle("")
    val repo by vm.repo.collectAsStateWithLifecycle("")
    val ultimo by vm.ultimoBackup.collectAsStateWithLifecycle(0L)
    val stato by vm.stato.collectAsStateWithLifecycle()
    val daRipristinare by vm.daRipristinare.collectAsStateWithLifecycle(null)

    val auto by vm.auto.collectAsStateWithLifecycle(false)
    val ultimoAuto by vm.ultimoAuto.collectAsStateWithLifecycle(0L)
    var tokenInput by remember(token) { mutableStateOf(token) }
    var repoInput by remember(repo) { mutableStateOf(repo) }
    var confermaRipristino by remember { mutableStateOf(false) }
    var confermaSostituzione by remember { mutableStateOf(false) }

    val contesto = LocalContext.current
    val scope = rememberCoroutineScope()
    var messaggioExport by remember { mutableStateOf("") }
    val esporta = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult
        scope.launch {
            messaggioExport = "Esportazione in corso…"
            runCatching {
                val testo = withContext(Dispatchers.IO) {
                    BackupEsecutore.esportaTutto(contesto, BuildConfig.VERSION_NAME)
                }
                withContext(Dispatchers.IO) {
                    contesto.contentResolver.openOutputStream(uri)?.use {
                        it.write(testo.toByteArray(Charsets.UTF_8))
                    } ?: error("Impossibile scrivere il file")
                }
            }.onSuccess {
                messaggioExport = "Esportazione completata."
            }.onFailure {
                messaggioExport = "Esportazione non riuscita: ${it.message}"
            }
        }
    }

    ScreenScaffold("Backup cloud", onBack = onIndietro) {
        Column(
            modifier = Modifier.fillMaxWidth().widthIn(max = dimensioneContenuto()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Spacer(Modifier.height(4.dp))
            Text(
                "Salvataggio cloud dei progressi su GitHub.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(4.dp))

            // --- Cosa viene salvato ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Cosa viene salvato", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "TUTTI i profili con parole, ripassi, log, test, progressi, " +
                            "conversazioni e correzioni. Il backup va su un repo GitHub privato.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (ultimo > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ultimo backup: " + SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ITALIAN)
                                .format(Date(ultimo)),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // --- Esportazione completa su file ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Esporta tutti i dati", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Salva un file JSON con tutti i profili (parole, ripassi, " +
                            "lezioni, grammatica, conversazioni), le notifiche e il tema. " +
                            "Puoi conservarlo o importarlo su un altro dispositivo.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Attenzione: il file contiene anche le chiavi API dei profili. " +
                            "Trattalo come un documento riservato. Il token GitHub non è incluso.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            val marca = LocalDateTime.now()
                                .format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                            esporta.launch("deutsch-dati-$marca.json")
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Esporta su file") }
                    if (messaggioExport.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            messaggioExport,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // --- Configurazione ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Text("Configurazione", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = repoInput,
                        onValueChange = { repoInput = it },
                        label = { Text("Repo di backup") },
                        placeholder = { Text("ItsZale0/tedesco-backup") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Formato: utente/nome-repo (es. ItsZale0/tedesco-backup).",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "File di backup del profilo attivo: ${vm.nomeFileBackup.collectAsStateWithLifecycle(BackupCloud.NOME_FILE).value}. " +
                            "Il ripristino cerca anche il backup condiviso tedesco-backup.json.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.height(10.dp))
                    OutlinedTextField(
                        value = tokenInput,
                        onValueChange = { tokenInput = it },
                        label = { Text("Token GitHub") },
                        placeholder = { Text("github_pat_… o ghp_…") },
                        singleLine = true,
                        visualTransformation = PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(6.dp))
                    TutorialGrigioBackup(
                        "PASSI:\n" +
                            "1. Crea un repo privato su github.com/new (es. tedesco-backup).\n" +
                            "2. Vai su github.com/settings/personal-access-tokens/new\n" +
                            "3. In \"Repository access\" scegli SOLO il repo di backup.\n" +
                            "4. In \"Permissions\" → Repository permissions → Contents: Read and write.\n" +
                            "5. Genera il token (inizia con github_pat_) e incollalo qui sopra.\n" +
                            "Il token resta solo sul telefono: non viene inviato da nessuna parte " +
                            "tranne a GitHub."
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = {
                            vm.salvaRepo(repoInput)
                            vm.salvaToken(tokenInput)
                            vm.provaConnessione()
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Salva e prova connessione") }
                }
            }

            // --- Stato ---
            when (val s = stato) {
                is BackupStato.InCorso -> Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Operazione in corso…")
                    }
                }
                is BackupStato.Ok -> MessaggioStato(s.messaggio, errore = false)
                is BackupStato.Errore -> MessaggioStato(s.messaggio, errore = true)
                BackupStato.Idle -> {}
            }

            // --- Azioni ---
            Button(
                onClick = { vm.eseguiBackup(com.alessandro.tedesco.BuildConfig.VERSION_NAME) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = token.isNotBlank() && stato !is BackupStato.InCorso
            ) { Text("Carica backup ora") }

            OutlinedButton(
                onClick = { vm.preparaRipristino() },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                enabled = token.isNotBlank() && stato !is BackupStato.InCorso
            ) { Text("Ripristina dal backup") }

            // --- Backup automatico ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Backup automatico",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text(
                                if (auto) "Attivo · ogni 5 minuti (15 min in background)"
                                else "Disattivato",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        androidx.compose.material3.Switch(
                            checked = auto,
                            onCheckedChange = { vm.impostaAuto(it) },
                            enabled = token.isNotBlank()
                        )
                    }
                    if (ultimoAuto > 0) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Ultimo automatico: " + SimpleDateFormat("d MMM HH:mm", Locale.ITALIAN)
                                .format(Date(ultimoAuto)),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (!token.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text("Inserisci il token per attivare il backup automatico",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Salva solo quando qualcosa cambia (confronto hash): " +
                            "non consuma richieste GitHub inutilmente.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    // --- Conferma ripristino: unione con i dati locali ---
    val riep = daRipristinare
    if (riep != null && confermaRipristino) {
        AlertDialog(
            onDismissRequest = { confermaRipristino = false; vm.annullaRipristino() },
            title = { Text("Unire il backup ai dati di questo telefono?") },
            text = {
                Column {
                    Text("Il backup contiene:")
                    Spacer(Modifier.height(8.dp))
                    Text("• ${riep.numProfili} profili")
                    Text("• ${riep.numParole} parole")
                    Text("• ${riep.numRipassi} ripassi registrati")
                    Text("• ${riep.numRisposte} correzioni")
                    Text("• ${riep.numUnitaGrammatica} unità di grammatica")
                    Text("• ${riep.numLezioni} lezioni")
                    Spacer(Modifier.height(8.dp))
                    Text("Data: " + SimpleDateFormat("d MMM yyyy, HH:mm", Locale.ITALIAN)
                        .format(Date(riep.creatoIl)))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "I progressi vengono UNITI a quelli del telefono: per ogni parola, " +
                            "unità di grammatica e lezione resta la versione più avanzata. " +
                            "Nessuna delle due parti viene cancellata.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    confermaRipristino = false
                    vm.confermaRipristino()
                }) { Text("Sì, unisci") }
            },
            dismissButton = {
                TextButton(onClick = { confermaRipristino = false; vm.annullaRipristino() }) {
                    Text("Annulla")
                }
            }
        )
    } else if (riep != null) {
        AlertDialog(
            onDismissRequest = { vm.annullaRipristino() },
            title = { Text("Backup trovato") },
            text = {
                Column {
                    Text("• ${riep.numProfili} profili")
                    Text("• ${riep.numParole} parole")
                    Text("• ${riep.numRipassi} ripassi")
                    Text("• ${riep.numUnitaGrammatica} unità di grammatica")
                    Text("• ${riep.numLezioni} lezioni")
                    Spacer(Modifier.height(8.dp))
                    Text("Vuoi unirlo ai dati di questo telefono?")
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { confermaRipristino = true }) { Text("Unisci") }
                    TextButton(onClick = { confermaSostituzione = true }) { Text("Sostituisci") }
                }
            },
            dismissButton = {
                TextButton(onClick = { vm.annullaRipristino() }) { Text("Annulla") }
            }
        )
    }

    // --- Conferma sostituzione: sovrascrive i dati locali col backup ---
    if (riep != null && confermaSostituzione) {
        AlertDialog(
            onDismissRequest = { confermaSostituzione = false; vm.annullaRipristino() },
            title = { Text("Sostituire i dati di questo telefono col backup?") },
            text = {
                Column {
                    Text("Il backup contiene:")
                    Spacer(Modifier.height(8.dp))
                    Text("• ${riep.numProfili} profili")
                    Text("• ${riep.numParole} parole")
                    Text("• ${riep.numRipassi} ripassi registrati")
                    Text("• ${riep.numRisposte} correzioni")
                    Text("• ${riep.numUnitaGrammatica} unità di grammatica")
                    Text("• ${riep.numLezioni} lezioni")
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "ATTENZIONE: i dati attuali di questo telefono verranno " +
                            "sovrascritti dal backup. I progressi fatti dopo l'ultimo " +
                            "backup su questo dispositivo andranno persi.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    confermaSostituzione = false
                    vm.confermaRipristinoSostituendo()
                }) { Text("Sì, sostituisci") }
            },
            dismissButton = {
                TextButton(onClick = { confermaSostituzione = false; vm.annullaRipristino() }) {
                    Text("Annulla")
                }
            }
        )
    }
}

@Composable
private fun MessaggioStato(messaggio: String, errore: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = if (errore) MaterialTheme.colorScheme.errorContainer
            else MaterialTheme.colorScheme.primaryContainer)
    ) {
        Text(
            messaggio,
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = if (errore) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@Composable
private fun TutorialGrigioBackup(testo: String) {
    GlassExpandableSection(
        title = "Come creo il token?",
        description = null
    ) {
        Text(
            testo,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}
