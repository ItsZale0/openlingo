package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NuoveParoleScreen(vm: TedescoViewModel, onIndietro: () -> Unit = {}) {
    val parole: List<WordEntity> by vm.parole.collectAsStateWithLifecycle(emptyList())
    val lezioni: List<Int> by vm.numeriLezioni.collectAsStateWithLifecycle(emptyList())
    val caricamento: Boolean by vm.caricamento.collectAsStateWithLifecycle(false)

    var ricerca by remember { mutableStateOf("") }
    var lezioneFiltrata by remember { mutableStateOf<Int?>(null) }
    var mostraAggiungiParola by remember { mutableStateOf(false) }
    val customWordsAbilitate by vm.enableCustomWords.collectAsStateWithLifecycle(false)

    // un solo motore TTS per tutta la schermata, non uno per riga
    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    // Titoli descrittivi delle lezioni del vocabolario (A0 → B2)
    val lezioneTitoli = mapOf(
        1 to t("Presentarsi (ich bin, ich heiße, ich wohne)"),
        2 to t("Verbi regolari (-en → ich lerne, du lernst)"),
        3 to t("Articoli: der / die / das + generi"),
        4 to t("Verbi irregolari (essen → isst, sprechen → sprichst)"),
        5 to t("Accusativo: den, die, das + verbi transitivi"),
        6 to t("Separabili: aufstehen, anrufen, einkaufen"),
        7 to t("Perfetto: ich habe gelernt / ich bin gegangen"),
        8 to t("Dativo: dem, der, dem + verbi (helfen, geben)"),
        9 to t("Preposizioni: in, auf, unter, über + caso"),
        10 to t("Modali: können, müssen, wollen, sollen, dürfen, möchten")
    )

    val filtrate = parole.filter { w: WordEntity ->
        val okTesto = ricerca.isBlank() ||
            w.german.contains(ricerca, ignoreCase = true) ||
            w.italian.contains(ricerca, ignoreCase = true)
        val okLezione = lezioneFiltrata == null || w.lesson == lezioneFiltrata
        okTesto && okLezione
    }

    val maxLarghezza = dimensioneContenuto()

    ScreenScaffold(t("Parole"), onBack = onIndietro, actions = {
        if (customWordsAbilitate) {
            IconButton(onClick = { mostraAggiungiParola = true }) {
                Icon(Icons.Filled.Add, contentDescription = t("Aggiungi parola"))
            }
        }
        IconButton(onClick = { vm.sincronizza() }, enabled = !caricamento) {
            Icon(Icons.Filled.Refresh, contentDescription = t("Aggiorna ora"))
        }
    }, scrollable = false) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = maxLarghezza)
            ) {
                Text(
                    t("Ogni parola nuova è un passo avanti. Cerca e impara!"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(Spaziature.xs))
                OutlinedTextField(
                    value = ricerca,
                    onValueChange = { ricerca = it },
                    label = { Text(t("Cerca in tedesco o italiano")) },
                    singleLine = true,
                    shape = MaterialTheme.shapes.medium,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )

                LazyRow(
                    modifier = Modifier,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterChip(
                            selected = lezioneFiltrata == null,
                            onClick = { lezioneFiltrata = null },
                            label = { Text(t("Tutte")) }
                        )
                    }
                    items(lezioni) { l: Int ->
                        FilterChip(
                            selected = lezioneFiltrata == l,
                            onClick = { lezioneFiltrata = if (lezioneFiltrata == l) null else l },
                            label = { Text(t("Lezione $l")) }
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))

                lezioneFiltrata?.let { l ->
                    val titolo = lezioneTitoli[l] ?: t("Lezione $l")
                    Text(
                        text = "L${l.toString().padStart(2, '0')} — $titolo",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    )
                    Spacer(Modifier.height(4.dp))
                }

                when {
                    caricamento && parole.isEmpty() -> Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }

                    filtrate.isEmpty() -> EmptyParole()

                    else -> {
                        Text(
                            text = t("${filtrate.size} parole"),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        LazyColumn(
                            contentPadding = PaddingValues(
                                bottom = 24.dp
                            ),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filtrate, key = { it.id }) { w: WordEntity ->
                                WordRow(
                                    w,
                                    onAscolta = { ttsHelper.speak(w.german) },
                                    onAscoltaFrase = { ttsHelper.speak(w.example) }
                                )
                            }
                            item {
                                Spacer(Modifier.height(12.dp))
                                Button(
                                    onClick = { vm.segnaParoleStudiate(filtrate.map { it.id }) },
                                    modifier = Modifier.fillMaxWidth().height(AltezzaBottonePrincipale())
                                ) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(t("Segna come studiato"))
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (mostraAggiungiParola) {
        AggiungiParolaDialog(
            onDismiss = { mostraAggiungiParola = false },
            onConferma = { de, italiano, frase, articolo, pronuncia, lezione ->
                vm.aggiungiParolaCustom(de, italiano, frase, articolo, pronuncia, lezione, "")
                mostraAggiungiParola = false
            }
        )
    }
}

@Composable
private fun WordRow(w: WordEntity, onAscolta: () -> Unit, onAscoltaFrase: () -> Unit = {}) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    text = ParolaTedesca.conArticolo(w.article, w.german),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = t(w.italian),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                w.example.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(
                            onClick = { onAscoltaFrase() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = t("Ascolta frase"),
                                modifier = Modifier.size(16.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Text(
                text = "L${w.lesson.toString().padStart(2, '0')}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .background(
                        MaterialTheme.colorScheme.primaryContainer,
                        MaterialTheme.shapes.small
                    )
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            )

            Spacer(Modifier.width(6.dp))

            IconButton(onClick = onAscolta) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = t("Ascolta pronuncia"),
                    modifier = Modifier.size(24.dp),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun AggiungiParolaDialog(
    onDismiss: () -> Unit,
    onConferma: (String, String, String, String?, String?, Int) -> Unit
) {
    var tedesco by remember { mutableStateOf("") }
    var italiano by remember { mutableStateOf("") }
    var frase by remember { mutableStateOf("") }
    var articolo by remember { mutableStateOf("") }
    var pronuncia by remember { mutableStateOf("") }
    var lezione by remember { mutableStateOf("") }

    val valido = tedesco.isNotBlank() && italiano.isNotBlank()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(t("Nuova parola")) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tedesco,
                    onValueChange = { tedesco = it },
                    label = { Text("Deutsch *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = italiano,
                    onValueChange = { italiano = it },
                    label = { Text(t("Italiano *")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = articolo,
                    onValueChange = { articolo = it },
                    label = { Text(t("Articolo (der/die/das)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = pronuncia,
                    onValueChange = { pronuncia = it },
                    label = { Text(t("Pronuncia")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = frase,
                    onValueChange = { frase = it },
                    label = { Text(t("Frase d'esempio")) },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = lezione,
                    onValueChange = { lezione = it.filter { c -> c.isDigit() } },
                    label = { Text(t("Lezione (numero)")) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConferma(
                        tedesco,
                        italiano,
                        frase,
                        articolo.ifBlank { null },
                        pronuncia.ifBlank { null },
                        lezione.toIntOrNull() ?: 0
                    )
                },
                enabled = valido
            ) { Text(t("Aggiungi")) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(t("Annulla")) }
        }
    )
}

@Composable
private fun EmptyParole() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(t("Nessuna parola"), style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            t("Tocca l'icona di aggiornamento in alto per scaricare il vocabolario."),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}
