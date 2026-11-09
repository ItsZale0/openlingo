@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.tedesco.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.ProfiliPreset
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.data.local.ProviderAI
import com.alessandro.tedesco.data.local.TipoProfilo
import com.alessandro.tedesco.data.local.providerEffettivo
import com.alessandro.tedesco.data.remote.UpdateState
import com.alessandro.tedesco.data.remote.UpdateSettings
import com.alessandro.tedesco.data.remote.UpdateWorker
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.sync.ReminderWorker
import com.alessandro.tedesco.sync.ImmersioneSeraleWorker
import com.alessandro.tedesco.sync.NotificheSettings
import com.alessandro.tedesco.sync.TemaSettings
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

private const val GUIDA_DOC_DEFAULT = ""

/** Forma card Material 3. */
private val GlassShape: Shape
    @Composable get() = MaterialTheme.shapes.medium

/** Superficie dei contenitori: token Material 3. */
@Composable
private fun settingsGlassColor(): Color = MaterialTheme.colorScheme.surfaceContainer

/** Superficie "liquid glass": trasparente, sottile bordo theme-tint, leggera ombra. */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = GlassShape,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth().clip(shape),
        shape = shape,
        colors = CardDefaults.cardColors(containerColor = settingsGlassColor()),
            ) { Column(Modifier.padding(horizontal = 18.dp, vertical = 16.dp), content = { content() }) }
}

/** Sezione impostazioni a tendina: header cliccabile con chevron + contenuto espandibile. */
@Composable
private fun SettingsExpandableSection(
    title: String,
    description: String? = null,
    defaultExpanded: Boolean = true,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    var expanded by rememberSaveable { mutableStateOf(defaultExpanded) }
    Card(
        modifier = modifier.fillMaxWidth().clip(GlassShape).animateContentSize(),
        shape = GlassShape,
        colors = CardDefaults.cardColors(containerColor = settingsGlassColor()),
            ) {
        Column(Modifier.padding(horizontal = 18.dp, vertical = 14.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(Modifier.weight(1f)) {
                    Text(
                        title, style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    description?.let {
                        Spacer(Modifier.height(2.dp))
                        Text(
                            it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2, overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = if (expanded) "Chiudi" else "Apri",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(32.dp)
                )
            }
            if (expanded) {
                Column(Modifier.fillMaxWidth()) {
                    Spacer(Modifier.height(12.dp))
                    content()
                }
            }
        }
    }
}

@Composable
private fun <T : Any> DropdownSelector(
    label: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val currentLabel = options.find { it.first == selected }?.second ?: "-"
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier.menuAnchor(),
            readOnly = true,
            value = currentLabel,
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = { Icon(imageVector = Icons.Filled.MoreVert, contentDescription = "Apri menu",
                tint = MaterialTheme.colorScheme.onSurfaceVariant) },
            shape = GlassShape,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                focusedLabelColor = MaterialTheme.colorScheme.primary,
                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurface
            )
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = GlassShape,
            ) {
            options.forEach { (value, name) ->
                DropdownMenuItem(
                    onClick = { onSelect(value); expanded = false },
                    text = {
                        Text(
                            name,
                            color = if (value == selected) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.outlinedButtonColors(
        contentColor = MaterialTheme.colorScheme.primary
    ),
    content: @Composable () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth().height(AltezzaBottonePrincipale()).clip(GlassShape),
        enabled = enabled,
        colors = colors,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) { content() }
}

@Composable
internal fun GlassTopBar(title: String) {
    CompactTopBar(title)
}

@Composable
fun ImpostazioniScreen(
    vm: TedescoViewModel,
    updater: UpdaterViewModel,
    onVaiABackup: () -> Unit = {},
    onIndietro: (() -> Unit)? = null
) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val profili by vm.profiliDisponibili.collectAsStateWithLifecycle(emptyList())
    val customWords by vm.enableCustomWords.collectAsStateWithLifecycle(false)
    val googleSheets by vm.enableGoogleSheets.collectAsStateWithLifecycle(false)
    val immersione by vm.didatticaImmersione.collectAsStateWithLifecycle(false)
    val ultimoSync by vm.ultimoSync.collectAsStateWithLifecycle(null)
    val updateState by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)
    val ultimoControllo by updater.ultimoControllo.collectAsStateWithLifecycle("")
    val contestoUpdate = LocalContext.current
    val scopeUpdate = rememberCoroutineScope()
    val impostazioniUpdate = remember { UpdateSettings(contestoUpdate) }
    var autoControllo by remember { mutableStateOf(true) }
    var autoDownload by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        autoControllo = impostazioniUpdate.autoControllo()
        autoDownload = impostazioniUpdate.autoDownload()
    }

    var cambiaProfilo by remember { mutableStateOf(false) }
    var esciSelezione by remember { mutableStateOf(false) }
    var confermaReset by remember { mutableStateOf(false) }
    var aggiungiParola by remember { mutableStateOf(false) }

    val temaSettings = remember(contestoUpdate) { TemaSettings(contestoUpdate) }
    var temaColore by remember { mutableStateOf("teal") }
    LaunchedEffect(Unit) { temaColore = temaSettings.colore.first() }

    var feedUrl by rememberSaveable {
        mutableStateOf(
            profilo?.config?.feedUrl?.takeIf { it.isNotBlank() }
                ?: ProfiliPreset.feedPredefinito(profilo?.config?.tipo ?: TipoProfilo.PERSONALIZZATO)
        )
    }
    var guidaDocId by rememberSaveable { mutableStateOf(profilo?.config?.guidaDocId ?: "") }
    var contestoUtente by rememberSaveable { mutableStateOf(profilo?.config?.contestoUtente ?: "") }
    var providerAI by rememberSaveable {
        mutableStateOf(profilo?.config?.providerEffettivo() ?: ProviderAI.PREDEFINITO)
    }
    var chiaviAI by remember { mutableStateOf(profilo?.config?.chiaviAI ?: emptyMap()) }
    var modelloAI by rememberSaveable { mutableStateOf(profilo?.config?.modelloAI ?: "") }

    // --- Sistema di notifiche (promemoria locali) ---
    var notRipassoAttivo by remember { mutableStateOf(true) }
    var notRipassoOra by remember { mutableIntStateOf(20) }
    var notImmAttiva by remember { mutableStateOf(false) }
    var notImmOra by remember { mutableIntStateOf(21) }
    val notificheImpostazioni = remember(contestoUpdate) { NotificheSettings(contestoUpdate) }
    LaunchedEffect(Unit) {
        notRipassoAttivo = notificheImpostazioni.ripassoAttivo()
        notRipassoOra = notificheImpostazioni.ripassoOra()
        notImmAttiva = notificheImpostazioni.immersioneSeraleAttiva()
        notImmOra = notificheImpostazioni.immersioneSeraleOra()
    }
    val oreDisponibili = (12..23).map { it to "%02d:00".format(it) }

    Scaffold(topBar = { CompactTopBar("Impostazioni", onBack = onIndietro) }, contentWindowInsets = WindowInsets(0)) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .fillMaxSize()
                
                .verticalScroll(rememberScrollState())
                .padding(horizontal = spaziaturaSchermo())
        ) {
            Column(Modifier.fillMaxWidth()) {

                // --- PROFILO ATTIVO ---
                GlassCard {
                    Column {
                        Text("Profilo attivo", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text(
                            profilo?.config?.nomeVisualizzato ?: "—",
                            style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        profilo?.config?.tipo?.descrizione?.let {
                            Spacer(Modifier.height(6.dp))
                            Text(it, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
                SettingsButton(onClick = { cambiaProfilo = true }) { Text("Cambia profilo") }
                Spacer(Modifier.height(8.dp))
                SettingsButton(onClick = { esciSelezione = true }) { Text("Torna alla scelta del profilo") }
                Spacer(Modifier.height(12.dp))

                // --- COLORE TEMA ---
                GlassCard {
                    Column {
                        Text("Colore tema", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(6.dp))
                        Text("Scegli il colore principale dell'app. \"Material You\" " +
                            "usa i colori del sistema (Android 12+).",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(Modifier.height(10.dp))
                        DropdownSelector(
                            label = "Colore tema",
                            options = TemaSettings.OPZIONI.map { it.first to it.second },
                            selected = temaColore,
                            onSelect = { id ->
                                temaColore = id
                                scopeUpdate.launch { temaSettings.setColore(id) }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // --- LIVELLO (menu a tendina) ---
                SettingsExpandableSection(
                    title = "Livello",
                    description = "Il contenuto si adatta al tuo livello CEFR."
                ) {
                    DropdownSelector(
                        label = "Livello CEFR",
                        options = listOf(
                            LivelloCEFR.A0 to "A0 – Principiante assoluto",
                            LivelloCEFR.A1 to "A1 – Principiante",
                            LivelloCEFR.A2 to "A2 – Elementare",
                            LivelloCEFR.B1 to "B1 – Intermedio",
                            LivelloCEFR.B2 to "B2 – Intermedio superiore"
                        ),
                        selected = profilo?.stato?.progresso?.livelloCorrente,
                        onSelect = { vm.cambiaLivello(it) }
                    )
                }
                Spacer(Modifier.height(12.dp))

                // --- MODALITÀ IMMERSIONE ---
                SettingsExpandableSection(
                    title = "Modalità Immersione",
                    description = "Percorso didattico separato: storie comprensibili, vocabolario in frasi, shadowing e ascolto passivo."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Attiva la modalità", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Cambia lo studio: niente tabelle, si impara leggendo e ascoltando",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = immersione,
                            onCheckedChange = { attivo -> vm.setDidatticaImmersione(attivo) }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // --- NOTIFICHE (promemoria locali) ---
                SettingsExpandableSection(
                    title = "Notifiche",
                    description = "Due promemoria giornalieri, locali e senza rete. Chiedi il permesso quando li attivi."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Promemoria ripasso", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Parole da ripassare, a un'ora stabile ogni giorno",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notRipassoAttivo,
                            onCheckedChange = { attivo ->
                                notRipassoAttivo = attivo
                                scopeUpdate.launch {
                                    notificheImpostazioni.setRipasso(attivo)
                                    if (attivo) ReminderWorker.schedule(contestoUpdate, notRipassoOra)
                                    else ReminderWorker.cancel(contestoUpdate)
                                }
                            }
                        )
                    }
                    if (notRipassoAttivo) {
                        Spacer(Modifier.height(4.dp))
                        DropdownSelector(
                            label = "Ora del promemoria",
                            options = oreDisponibili,
                            selected = notRipassoOra,
                            onSelect = { ora ->
                                notRipassoOra = ora
                                scopeUpdate.launch {
                                    notificheImpostazioni.setRipasso(true, ora)
                                    ReminderWorker.schedule(contestoUpdate, ora)
                                }
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Ascolto passivo serale", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Il bonus pre-sonno della Modalità Immersione (solo se attiva)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = notImmAttiva,
                            onCheckedChange = { attivo ->
                                notImmAttiva = attivo
                                scopeUpdate.launch {
                                    notificheImpostazioni.setImmersioneSerale(attiva = attivo)
                                    if (attivo) ImmersioneSeraleWorker.schedule(contestoUpdate, notImmOra)
                                    else ImmersioneSeraleWorker.cancel(contestoUpdate)
                                }
                            }
                        )
                    }
                    if (notImmAttiva) {
                        Spacer(Modifier.height(4.dp))
                        DropdownSelector(
                            label = "Ora dell'ascolto serale",
                            options = oreDisponibili,
                            selected = notImmOra,
                            onSelect = { ora ->
                                notImmOra = ora
                                scopeUpdate.launch {
                                    notificheImpostazioni.setImmersioneSerale(attiva = true, ora = ora)
                                    ImmersioneSeraleWorker.schedule(contestoUpdate, ora)
                                }
                            }
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // --- VOCABOLARIO PERSONALIZZATO (solo profili custom) ---
                if (customWords) {
                    SettingsExpandableSection(
                        title = "Vocabolario personalizzato",
                        description = if (googleSheets)
                            "Le tue parole sono sincronizzate da Google Sheets."
                        else "Aggiungi parole tue e usa un vocabolario personalizzato."
                    ) {
                        SettingsButton(onClick = { aggiungiParola = true }) { Text("Aggiungi una parola") }
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = feedUrl, onValueChange = { feedUrl = it },
                            label = { Text("URL vocabolario") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("https://raw.githubusercontent.com/...") },
                            shape = GlassShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        OutlinedButton(onClick = { feedUrl = "" }) {
                            Text("Rimuovi feed")
                        }
                        GlassExpandableSection(
                            title = "Info",
                            description = null
                        ) {
                            Text(
                                text = "Come creare il TUO vocabolario su GitHub:\n\n" +
                                "1. Vai su github.com e accedi (o crea un account gratis)\n" +
                                "2. Crea un repository pubblico\n" +
                                "3. Carica un file vokabeln.json\n" +
                                "4. Copia l'URL Raw e incollalo qui\n\n" +
                                "Formato: {\"words\":[{\"id\":\"1\",\"german\":\"der Hund\",\" " +
                                "\"italian\":\"il cane\",\"level\":\"A1\",\"lesson\":1}]}",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = guidaDocId, onValueChange = { guidaDocId = it },
                            label = { Text("Documento Google della guida") },
                            modifier = Modifier.fillMaxWidth(), singleLine = true,
                            placeholder = { Text("ID o URL del documento") },
                            shape = GlassShape,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                            )
                        )
                        Spacer(Modifier.height(8.dp))
                        GlassExpandableSection(
                            title = "Info",
                            description = null
                        ) {
                            Text(
                                text = "Come usare il TUO documento Google:\n\n" +
                                "1. Apri Google Docs e crea un documento\n" +
                                "2. Scrivi la guida (usa i Titoli)\n" +
                                "3. Condividi ��' Chiunque abbia il link ��' Visualizzatore\n" +
                                "4. Copia l'indirizzo e incollalo qui",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(12.dp)
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        SettingsButton(onClick = {
                            vm.aggiornaFeedUrl(feedUrl)
                            vm.aggiornaGuidaDocId(guidaDocId)
                        }) { Text("Salva") }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                
                // --- CONTESTO PER L'AI ---
                SettingsExpandableSection(
                    title = "Contesto e obiettivi",
                    description = "L'AI usa queste informazioni per scegliere esempi, " +
                        "esercizi e vocabolario adatti a te."
                ) {
                    OutlinedTextField(
                        value = contestoUtente,
                        onValueChange = { contestoUtente = it },
                        label = { Text("Professione, interessi, cosa vuoi saper fare") },
                        placeholder = { Text("Es. elettricista: parlare con clienti e fornitori") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        shape = GlassShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Resta sul dispositivo e viene inviato solo ai provider AI " +
                            "quando generi contenuti. Vuoto = nessuna personalizzazione.",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    SettingsButton(onClick = { vm.aggiornaContestoUtente(contestoUtente) }) {
                        Text("Salva contesto")
                    }
                }
                Spacer(Modifier.height(12.dp))

// --- TUTOR AI ---
                SettingsExpandableSection(
                    title = "Tutor AI",
                    description = "Facoltativo: scegli un provider e incolla la chiave. " +
                        "Senza chiave restano disponibili tutti i contenuti offline."
                ) {
                    ProviderAIChips(provider = providerAI, onProvider = { providerAI = it })
                    Spacer(Modifier.height(8.dp))
                    ChiaveAIField(
                        provider = providerAI,
                        valore = chiaviAI[providerAI.id].orEmpty(),
                        onValore = { valore ->
                            chiaviAI = if (valore.isBlank()) {
                                chiaviAI - providerAI.id
                            } else {
                                chiaviAI + (providerAI.id to valore)
                            }
                        }
                    )
                    Spacer(Modifier.height(8.dp))
                    ModelloAIField(
                        provider = providerAI,
                        valore = modelloAI,
                        onValore = { modelloAI = it }
                    )
                    Spacer(Modifier.height(8.dp))
                    NotaProviderAI(providerAI)
                    Spacer(Modifier.height(12.dp))
                    SettingsButton(onClick = {
                        vm.salvaConfigurazioneAI(providerAI, chiaviAI, modelloAI)
                    }) { Text("Salva configurazione AI") }
                }
                Spacer(Modifier.height(12.dp))

                // --- AGGIORNAMENTI ---
                SettingsExpandableSection(
                    title = "Aggiornamenti",
                    description = "L'app controlla da sola all'apertura e ti avvisa. Puoi anche forzare il controllo."
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Versione installata: ${updater.versioneCorrente ?: "?"} (${updater.versioneCorrenteCode})",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { updater.checkForUpdate() }) {
                            Icon(
                                Icons.Filled.Refresh,
                                contentDescription = "Controlla aggiornamenti ora",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    ultimoControllo.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Controllo automatico", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Ogni 24 ore, anche ad app chiusa (WorkManager)",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoControllo,
                            onCheckedChange = { attivo ->
                                autoControllo = attivo
                                scopeUpdate.launch {
                                    impostazioniUpdate.setAutoControllo(attivo)
                                    if (attivo) UpdateWorker.schedule(contestoUpdate)
                                    else UpdateWorker.annulla(contestoUpdate)
                                }
                            }
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("Scarica in automatico", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                "Scarica l'APK in background e avvisa quando è pronto",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = autoDownload,
                            onCheckedChange = { attivo ->
                                autoDownload = attivo
                                scopeUpdate.launch { impostazioniUpdate.setAutoDownload(attivo) }
                            }
                        )
                    }
                    Spacer(Modifier.height(10.dp))
                    when (val s = updateState) {
                        is UpdateState.Checking -> {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                Spacer(Modifier.width(8.dp))
                                Text("Controllo in corso…", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        is UpdateState.Available -> {
                            Text(
                                "Disponibile v${s.version.versionName} (${s.version.versionCode})",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            SettingsButton(onClick = { updater.downloadUpdate(s.version) }) {
                                Text("Scarica e installa")
                            }
                        }
                        is UpdateState.Downloading -> {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                LinearProgressIndicator(
                                    progress = { s.progress / 100f },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    "Download v${s.progress}%",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        is UpdateState.ReadyToInstall -> {
                            Text(
                                "Aggiornamento pronto all'installazione",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            SettingsButton(onClick = { updater.installUpdate(s.file) }) { Text("Installa ora") }
                        }
                        is UpdateState.NeedPermission -> {
                            Text(
                                "Serve il permesso per installare app sconosciute.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            SettingsButton(onClick = { updater.apriImpostazioniInstallazione() }) {
                                Text("Apri impostazioni")
                            }
                        }
                        is UpdateState.Error -> {
                            Text(
                                s.message,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.height(8.dp))
                            SettingsButton(onClick = { updater.checkForUpdate() }) { Text("Riprova") }
                        }
                        is UpdateState.UpToDate -> Text(
                            "Sei aggiornato all'ultima versione.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        else -> Text(
                            "Tocca l'icona in alto a destra per controllare.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Spacer(Modifier.height(12.dp))

                // --- CRASH REPORTER ---
                // Se l'app si e' interrotta, qui c'e' lo stack trace: e' l'unico modo
                // per capire il motivo senza un PC collegato.
                val ctx = LocalContext.current
                TedescoApp.ultimoCrash(ctx)?.let { trace ->
                    SettingsExpandableSection(
                        title = "Ultimo arresto anomalo",
                        description = "L'app si e' interrotta. Guarda lo stack trace."
                    ) {
                        Text(
                            trace.take(1200),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 220.dp)
                        )
                        Spacer(Modifier.height(10.dp))
                        SettingsButton(onClick = { TedescoApp.cancellaUltimoCrash(ctx) }) {
                            Text("Ho capito, cancella")
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }

                // --- DATI & SINCRONIZZAZIONE (consolidato) ---
                SettingsExpandableSection(
                    title = "Dati & sincronizzazione",
                    description = "Backup cloud, vocabolario e ripasso."
                ) {
                    Text("Backup cloud", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    SettingsButton(onClick = onVaiABackup) { Text("Apri backup cloud") }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Il backup contiene tutti i profili (parole, ripassi, test, progressi). " +
                            "Serve un token GitHub gratuito con permesso Contents sul repo di backup.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Vocabolario", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Ultimo aggiornamento: ${TedescoViewModel.formattaData(ultimoSync?.syncedAt ?: 0L)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ultimoSync?.message?.takeIf { it.isNotBlank() }?.let {
                        Spacer(Modifier.height(4.dp))
                        Text(it, style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Spacer(Modifier.height(8.dp))
                    SettingsButton(onClick = { vm.sincronizza() }) { Text("Sincronizza adesso") }
                    Spacer(Modifier.height(12.dp))
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Spacer(Modifier.height(12.dp))
                    Text("Ripasso", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Quando rispondi giusto l'intervallo si allunga: 1, 3, 7, 16, 35, 75, " +
                            "150, 300 giorni. Quando sbagli torna a domani.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))

                Spacer(Modifier.height(8.dp))

                
// --- CANCELLA DATI ---
                SettingsExpandableSection(
                    title = "Area pericolosa",
                    description = "Cancella i dati di questo profilo (gli altri profili non sono toccati)."
                ) {
                    Spacer(Modifier.height(8.dp))
                    SettingsButton(
                        onClick = { confermaReset = true },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    ) { Text("Cancella i dati di questo profilo") }
                }
            }
        }
    }

    // ---- Dialoghi (stile glass) ----
    if (cambiaProfilo) {
        GlassChangeProfileDialog(
            profili = profili,
            selezionato = profilo?.id,
            onDismiss = { cambiaProfilo = false },
            onSelect = { vm.selezionaProfilo(it); cambiaProfilo = false }
        )
    }
    if (esciSelezione) {
        AlertDialog(
            onDismissRequest = { esciSelezione = false },
            title = { Text("Tornare alla scelta del profilo?") },
            text = {
                Text(
                    "I dati di ${profilo?.config?.nomeVisualizzato ?: "questo profilo"} " +
                        "restano salvati. Potrai rientrare quando vuoi."
                )
            },
            confirmButton = {
                TextButton(onClick = { esciSelezione = false; vm.esciDalProfilo() }) { Text("Torna") }
            },
            dismissButton = { TextButton(onClick = { esciSelezione = false }) { Text("Annulla") } }
        )
    }
    if (confermaReset) {
        AlertDialog(
            onDismissRequest = { confermaReset = false },
            title = { Text("Cancellare i dati?") },
            text = {
                Text(
                    "Vengono rimossi vocaboli e ripetizioni solo di questo profilo. " +
                        "Gli altri profili non vengono toccati."
                )
            },
            confirmButton = {
                TextButton(onClick = { confermaReset = false; vm.reset() }) { Text("Cancella") }
            },
            dismissButton = { TextButton(onClick = { confermaReset = false }) { Text("Annulla") } }
        )
    }
    if (aggiungiParola) {
        AggiungiParolaDialog(
            onDismiss = { aggiungiParola = false },
            onConferma = { de, italiano, frase, articolo, pronuncia, lezione ->
                vm.aggiungiParolaCustom(de, italiano, frase, articolo, pronuncia, lezione, "")
                aggiungiParola = false
            }
        )
    }
}

@Composable
private fun GlassChangeProfileDialog(
    profili: List<ProfiloUtente>,
    selezionato: String?,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Cambia profilo") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Scegli con quale profilo continuare.", style = MaterialTheme.typography.bodySmall)
                profili.forEach { p ->
                    SettingsButton(onClick = { onSelect(p.id) }) {
                        Text(p.config.nomeVisualizzato)
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Chiudi") } }
    )
}

@Composable
private fun AggiungiParolaDialog(
    onDismiss: () -> Unit,
    onConferma: (String, String, String, String?, String?, Int) -> Unit
) {
    var tedesco by rememberSaveable { mutableStateOf("") }
    var italiano by rememberSaveable { mutableStateOf("") }
    var frase by rememberSaveable { mutableStateOf("") }
    var articolo by rememberSaveable { mutableStateOf("") }
    var pronuncia by rememberSaveable { mutableStateOf("") }
    var lezione by rememberSaveable { mutableStateOf("") }

    val valido = tedesco.isNotBlank() && italiano.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Nuova parola") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(value = tedesco, onValueChange = { tedesco = it },
                    label = { Text("Deutsch *") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = italiano, onValueChange = { italiano = it },
                    label = { Text("Italiano *") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = articolo, onValueChange = { articolo = it },
                    label = { Text("Articolo (der/die/das)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = pronuncia, onValueChange = { pronuncia = it },
                    label = { Text("Pronuncia") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = frase, onValueChange = { frase = it },
                    label = { Text("Frase d'esempio") }, modifier = Modifier.fillMaxWidth(), shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
                OutlinedTextField(value = lezione, onValueChange = { lezione = it.filter { c -> c.isDigit() } },
                    label = { Text("Lezione (numero)") }, singleLine = true, modifier = Modifier.fillMaxWidth(),
                    shape = GlassShape,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = MaterialTheme.colorScheme.primary))
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConferma(tedesco, italiano, frase,
                        articolo.ifBlank { null }, pronuncia.ifBlank { null },
                        lezione.toIntOrNull() ?: 0)
                },
                enabled = valido
            ) { Text("Aggiungi") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annulla") } }
    )
}
