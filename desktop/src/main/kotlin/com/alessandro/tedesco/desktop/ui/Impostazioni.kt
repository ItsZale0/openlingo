package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.ProfiliPreset
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.desktop.DesktopStore
import com.alessandro.tedesco.desktop.Versione
import com.alessandro.tedesco.i18n.t
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.awt.Desktop
import java.io.File
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import com.alessandro.tedesco.i18n.t

private val versione: String by lazy { Versione.conBuild }

private fun dataOra(millis: Long): String =
    if (millis <= 0L) t("mai")
    else LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))


@Composable
fun SchermataImpostazioni(store: DesktopStore, onCambiaProfilo: () -> Unit) {
    store.repo
    store.preferenze
    val profilo = store.profilo
    var nome by remember(profilo?.id) {
        mutableStateOf(profilo?.config?.nomeVisualizzato.orEmpty())
    }
    var messaggio by remember { mutableStateOf("") }
    var confermaSvuota by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    var operazioneInCorso by remember { mutableStateOf("") }
    var feedTesto by remember(profilo?.id) { mutableStateOf(profilo?.config?.feedUrl.orEmpty()) }
    var contestoTesto by remember(profilo?.id) {
        mutableStateOf(profilo?.config?.contestoUtente.orEmpty())
    }
    var tokenTesto by remember { mutableStateOf(store.tokenCloud) }
    var repoTesto by remember { mutableStateOf(store.repoCloud) }
    var downloadInAttesa by remember { mutableStateOf<DesktopStore.RisultatoCloud.Trovato?>(null) }

    // Le chiamate di rete sono bloccanti nel :core (condivise con Android):
    // qui girano su Dispatchers.IO e la UI resta reattiva.
    fun esegui(etichetta: String, azione: () -> DesktopStore.EsitoRete) {
        if (operazioneInCorso.isNotEmpty()) return
        operazioneInCorso = etichetta
        messaggio = ""
        scope.launch {
            val esito = withContext(Dispatchers.IO) { runCatching(azione) }
                .getOrElse { DesktopStore.EsitoRete(false, it.message ?: t("Errore sconosciuto")) }
            messaggio = esito.messaggio
            operazioneInCorso = ""
        }
    }

    fun scaricaBackup() {
        if (operazioneInCorso.isNotEmpty()) return
        operazioneInCorso = "scarica"
        messaggio = ""
        scope.launch {
            when (val esito = withContext(Dispatchers.IO) { store.scaricaBackupCloud() }) {
                is DesktopStore.RisultatoCloud.Trovato -> downloadInAttesa = esito
                is DesktopStore.RisultatoCloud.Errore -> messaggio = esito.messaggio
            }
            operazioneInCorso = ""
        }
    }

    Pagina {
        Testata(
            titolo = t("Impostazioni"),
            sottotitolo = t("Profilo, livello, aspetto e dati locali · versione $versione")
        )

        // ----------------------------------------------------------- profilo
        CardSezione(
            titolo = t("Profilo attivo"),
            sottotitolo = t("Ogni profilo ha dati e progressi separati dagli altri")
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                store.elencoProfili.forEach { p ->
                    val attivo = p.id == store.repo.profiloAttivoId
                    ProfiloCard(
                        nome = p.config.nomeVisualizzato,
                        descrizione = t(p.config.tipo.descrizione),
                        livello = p.stato.progresso.livelloCorrente.label.substringBefore(" — "),
                        attivo = attivo,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            store.attivaProfilo(p.id)
                            messaggio = t("Profilo attivo") + ": ${p.config.nomeVisualizzato}"
                            onCambiaProfilo()
                        }
                    )
                }
            }

            Spacer(Modifier.height(18.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = nome,
                    onValueChange = { nome = it },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    label = { Text(t("Nome visualizzato")) }
                )
                Spacer(Modifier.width(10.dp))
                Button(
                    onClick = {
                        store.rinominaProfilo(nome)
                        messaggio = t("Nome aggiornato.")
                    },
                    enabled = nome.isNotBlank() && nome != profilo?.config?.nomeVisualizzato
                ) {
                    Text(t("Salva nome"))
                }
            }
        }

        // ---------------------------------------------------- contesto per l'AI
        CardSezione(
            titolo = t("Contesto e obiettivi"),
            sottotitolo = t("L'AI usa queste informazioni per scegliere esempi, esercizi e vocabolario")
        ) {
            OutlinedTextField(
                value = contestoTesto,
                onValueChange = { contestoTesto = it },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3,
                label = { Text(t("Professione, interessi, cosa vuoi saper fare")) },
                placeholder = { Text(t("Es. elettricista: parlare con clienti e fornitori")) }
            )
            Spacer(Modifier.height(6.dp))
            Text(
                t("Resta su questo computer e viene inviato solo ai provider AI quando generi contenuti. Vuoto = nessuna personalizzazione."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            Button(
                onClick = {
                    store.impostaContestoUtente(contestoTesto)
                    messaggio = t("Contesto salvato.")
                },
                enabled = contestoTesto.trim() != profilo?.config?.contestoUtente.orEmpty()
            ) {
                Text(t("Salva contesto"))
            }
        }

        // ------------------------------------------------------------ livello
        CardSezione(
            titolo = t("Livello e obiettivo"),
            sottotitolo = t("Il livello attuale resta quello reale raggiunto: qui imposti la partenza e il traguardo")
        ) {
            Text(t("Livello di partenza"), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LivelloIniziale.entries.forEach { livello ->
                    FilterChip(
                        selected = profilo?.config?.livelloIniziale == livello,
                        onClick = {
                            store.impostaLivelloEObiettivo(
                                iniziale = livello,
                                obiettivo = store.stato.progresso.obiettivoLivello
                            )
                            messaggio = t("Livello di partenza") + ": ${livello.label}"
                        },
                        label = { Text(livello.label) }
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(t("Obiettivo"), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                LivelloCEFR.entries
                    .filter { it != LivelloCEFR.A0 }
                    .forEach { livello ->
                        FilterChip(
                            selected = store.stato.progresso.obiettivoLivello == livello,
                            onClick = {
                                val iniziale = profilo?.config?.livelloIniziale ?: LivelloIniziale.A0
                                store.impostaLivelloEObiettivo(iniziale, livello)
                                messaggio = t("Obiettivo") + ": ${livello.codiceBreve()}"
                            },
                            label = { Text(livello.codiceBreve()) }
                        )
                    }
            }

            Spacer(Modifier.height(12.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Column(Modifier.padding(12.dp)) {
                    Text(
                        t("Livello attuale") + ": ${store.stato.progresso.livelloCorrente.label}",
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        t("Il livello attuale avanza solo con i test e con le competenze dimostrate, non perché lo imposti a mano."),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // ------------------------------------------------------------- aspetto
        CardSezione(titolo = t("Aspetto"), sottotitolo = t("Tema Material 3 chiaro o scuro")) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(Modifier.weight(1f)) {
                    Text(t("Tema scuro"), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        t("La preferenza viene salvata in preferenze.json"),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Switch(
                    checked = store.temaScuro(),
                    onCheckedChange = { store.commutaTema() }
                )
            }
        }

        // ------------------------------------------------------------- colore
        CardSezione(titolo = t("Colore tema"), sottotitolo = t("Colore principale dell'app")) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "teal" to "Teal", "navy" to "Blu navy", "indigo" to "Indaco",
                    "forest" to t("Verde"), "rose" to t("Rosa"), "amber" to t("Ambra")
                ).forEach { (id, label) ->
                    FilterChip(
                        selected = store.temaColore() == id,
                        onClick = { store.setTemaColore(id) },
                        label = { Text(label) }
                    )
                }
            }
        }

        // ------------------------------------------------------------ lessico
        CardSezione(
            titolo = t("Lessico"),
            sottotitolo = "${store.stato.parole.size} " + t("parole nel profilo attivo")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(
                        t("Importa la Wortliste curata A1-B1"),
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        t("Aggiunge le parole mancanti al lessico, senza duplicati."),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = {
                    val aggiunte = store.importaWortliste()
                    messaggio = if (aggiunte == 0) t("Nessuna parola nuova: erano già tutte presenti.")
                    else t("Aggiunte $aggiunte parole.")
                }) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Importa"))
                }
            }
        }

        // ---------------------------------------------------- sincronizzazione
        CardSezione(
            titolo = t("Sincronizzazione"),
            sottotitolo = t("Stesso vocabolario e stesso backup GitHub dell'app Android")
        ) {
            Text(t("Feed del vocabolario"), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                t("Scarica le parole pubblicate su un repository GitHub e le unisce al lessico di questo profilo con le stesse regole dell'app. Il feed è facoltativo: senza URL si usa il lessico incluso nell'app."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = feedTesto,
                onValueChange = { feedTesto = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(t("URL del feed (JSON)")) }
            )
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    enabled = operazioneInCorso.isEmpty(),
                    onClick = {
                        store.impostaFeedUrl(feedTesto)
                        esegui("feed") { store.sincronizzaFeed() }
                    }
                ) {
                    if (operazioneInCorso == "feed") {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Filled.Sync, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(t("Sincronizza ora"))
                }
                OutlinedButton(
                    enabled = operazioneInCorso.isEmpty(),
                    onClick = {
                        feedTesto = ""
                        store.impostaFeedUrl("")
                        messaggio = t("Feed rimosso: resta il lessico incluso nell'app.")
                    }
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Rimuovi feed"))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                t("Ultima sincronizzazione") + ": ${dataOra(store.ultimaSincronizzazione)} · ${store.stato.parole.size} " + t("parole nel lessico"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            store.stato.feedLog.lastOrNull()?.let { log ->
                val statoEsito = if (log.status == "ok") t("OK") else t("errore")
                Text(
                    t("Ultimo esito") + ": $statoEsito · ${dataOra(log.syncedAt)} · ${log.message.orEmpty()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (log.status == "ok") MaterialTheme.colorScheme.onSurfaceVariant
                    else MaterialTheme.colorScheme.error
                )
            }

            HorizontalDivider(
                modifier = Modifier.padding(vertical = 18.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )

            Text(t("Backup GitHub"), style = MaterialTheme.typography.titleSmall)
            Spacer(Modifier.height(2.dp))
            Text(
                t("Carica tutti i profili (parole, ripassi, grammatica e lezioni) sul repository privato e ripristinali su un altro dispositivo. Il caricamento UNISCE i progressi fatti altrove: niente viene sovrascritto con dati meno avanzati.") + " " + t("File del profilo attivo") + ": ${store.nomeFileBackup} " + t("(il ripristino cerca anche tedesco-backup.json)"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = tokenTesto,
                onValueChange = {
                    tokenTesto = it
                    store.salvaTokenCloud(it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(t("Token GitHub (Contents: Read and write)")) },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = repoTesto,
                onValueChange = {
                    repoTesto = it
                    store.salvaRepoCloud(it)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text(t("Repository (owner/nome)")) },
                supportingText = { Text(t("Vuoto = tedesco-backup dell'account del token")) }
            )
            Spacer(Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    enabled = operazioneInCorso.isEmpty(),
                    onClick = { esegui("verifica") { store.provaConnessioneCloud() } }
                ) {
                    if (operazioneInCorso == "verifica") {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                    Text(t("Prova connessione"))
                }
                Button(
                    enabled = operazioneInCorso.isEmpty() && store.stato.parole.isNotEmpty(),
                    onClick = { esegui("carica") { store.salvaSulCloud() } }
                ) {
                    if (operazioneInCorso == "carica") {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Filled.CloudUpload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(t("Carica backup"))
                }
                OutlinedButton(
                    enabled = operazioneInCorso.isEmpty(),
                    onClick = { scaricaBackup() }
                ) {
                    if (operazioneInCorso == "scarica") {
                        CircularProgressIndicator(
                            modifier = Modifier.width(18.dp).height(18.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                    } else {
                        Icon(Icons.Filled.CloudDownload, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                    }
                    Text(t("Scarica backup"))
                }
            }
            Spacer(Modifier.height(8.dp))
            Text(
                t("Ultimo backup caricato") + ": ${dataOra(store.ultimoBackupCloud)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                t("Ultima sincronizzazione automatica") + ": ${dataOra(store.ultimaSincronizzazioneCloud)} " + t("(ogni 5 minuti, se il token è inserito)"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (store.sincronizzazioneCloudInCorso) {
                Text(
                    t("Sincronizzazione in corso..."),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                t("Il token resta solo su questo computer, in preferenze.json. Usa un token fine-grained limitato al repo di backup."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // -------------------------------------------------------------- dati
        CardSezione(
            titolo = t("Dati locali"),
            sottotitolo = t("Parole, ripassi e progressi vivono in un file JSON leggibile, senza account")
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.surfaceContainerHigh
            ) {
                Text(
                    store.percorsoDati,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.labelMedium
                )
            }
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    val esito = apriCartella(File(store.percorsoDati))
                    if (!esito) messaggio = t("Impossibile aprire la cartella dei dati.")
                }) {
                    Icon(Icons.Filled.FolderOpen, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Apri cartella"))
                }
                OutlinedButton(onClick = {
                    messaggio = esportaBackup(store)
                }) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Esporta backup"))
                }
                Button(onClick = {
                    messaggio = runCatching {
                        val file = store.esportaDatiCompleti()
                        t("Esportazione completa salvata in") + " ${file.absolutePath}"
                    }.getOrElse { t("Esportazione non riuscita") + ": ${it.message}" }
                }) {
                    Icon(Icons.Filled.Download, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Esporta tutti i dati"))
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                t("«Esporta tutti i dati» crea un unico file JSON con tutti i profili, le preferenze e il tema: è il file da conservare o importare altrove. Il token GitHub non viene incluso. «Esporta backup» copia solo profili.json nella cartella Download."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // ------------------------------------------------------------ ripristino
        CardSezione(
            titolo = t("Ripristino"),
            sottotitolo = t("Azzera solo i progressi del profilo attivo: i profili restano")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(t("Svuota il profilo"), style = MaterialTheme.typography.bodyMedium)
                    Text(
                        t("Rimuove parole, ripassi, unità completate e piano di oggi. Il lessico può essere reimportato dalla Wortliste."),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                OutlinedButton(onClick = { confermaSvuota = true }) {
                    Icon(Icons.Filled.Delete, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Svuota"))
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Deutsch desktop · ${versione}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                t("Funziona offline: feed e backup si aggiornano solo quando li chiedi."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (messaggio.isNotBlank()) {
            Text(
                messaggio,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    downloadInAttesa?.let { trovato ->
        val r = trovato.riepilogo
        AlertDialog(
            onDismissRequest = { downloadInAttesa = null },
            title = { Text(t("Ripristinare il backup?")) },
            text = {
                Text(
                    t("Backup del") + " ${dataOra(r.creatoIl)}" +
                        (if (r.versioneApp.isBlank()) "" else  " (" + t("versione") + " ${r.versioneApp})") +
                        ":\n" + "${r.numProfili} " + t("profili") + " · ${r.numParole} " + t("parole") + " · ${r.numRipassi} " + t("ripassi") + " · ${r.numRisposte} " + t("risposte") + " · ${r.numUnitaGrammatica} " + t("unità di grammatica") + " · ${r.numLezioni} " + t("lezioni") + "." + "\n\n" +
                        t("I progressi del backup vengono UNITI a quelli di questo computer: per ogni parola, unità e lezione resta la versione più avanzata, quindi niente viene perso.")
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    val contenuto = trovato.contenuto
                    downloadInAttesa = null
                    esegui("ripristino") { store.applicaBackupCloud(contenuto) }
                }) { Text(t("Ripristina")) }
            },
            dismissButton = {
                TextButton(onClick = { downloadInAttesa = null }) { Text(t("Annulla")) }
            }
        )
    }

    if (confermaSvuota) {
        AlertDialog(
            onDismissRequest = { confermaSvuota = false },
            title = { Text(t("Svuotare il profilo?")) },
            text = {
                Text(t("Verranno cancellati i dati del profilo attivo su questo computer. L'operazione non si può annullare."))
            },
            confirmButton = {
                TextButton(onClick = {
                    store.svuotaProfilo()
                    confermaSvuota = false
                    messaggio = t("Profilo svuotato.")
                }) { Text(t("Svuota")) }
            },
            dismissButton = {
                TextButton(onClick = { confermaSvuota = false }) { Text(t("Annulla")) }
            }
        )
    }
}

@Composable
private fun ProfiloCard(
    nome: String,
    descrizione: String,
    livello: String,
    attivo: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier.clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (attivo) MaterialTheme.colorScheme.secondaryContainer
        else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    Icons.Filled.Person,
                    contentDescription = null,
                    tint = if (attivo) MaterialTheme.colorScheme.onSecondaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    nome,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (attivo) {
                    Spacer(Modifier.width(8.dp))
                    Icon(
                        Icons.Filled.Check,
                        contentDescription = t("Profilo attivo"),
                        tint = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            Spacer(Modifier.height(6.dp))
            Text(
                t(descrizione),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Etichetta(livello)
        }
    }
}

private fun apriCartella(cartella: File): Boolean = runCatching {
    cartella.mkdirs()
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
        Desktop.getDesktop().open(cartella)
        true
    } else false
}.getOrDefault(false)

private fun esportaBackup(store: DesktopStore): String = runCatching {
    val origine = File(store.percorsoDati, "profili.json")
    if (!origine.exists()) return t("Nessun dato da esportare.")
    val download = File(System.getProperty("user.home"), "Downloads").takeIf { it.isDirectory }
        ?: File(System.getProperty("user.home"))
    val marca = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
    val destinazione = File(download, "tedesco-desktop-backup-$marca.json")
    origine.copyTo(destinazione, overwrite = true)
    t("Backup salvato in") + " ${destinazione.absolutePath}"
}.getOrElse { t("Esportazione non riuscita") + ": ${it.message}" }
