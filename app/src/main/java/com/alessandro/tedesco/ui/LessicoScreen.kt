package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.EsercizioLessico
import com.alessandro.tedesco.data.LessicoLivelli
import com.alessandro.tedesco.data.TipoEsercizioLessico
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

/** Riepilogo di un livello nella sezione Lessico. */
data class StatoLessicoLivello(
    val livello: String,
    val curate: Int,
    val nelProfilo: Int,
    val daRipassare: Int,
    val temi: List<String> = emptyList()
)

/** Sessione di esercizi sul lessico (stato tenuto dal ViewModel). */
data class StatoSessioneLessico(
    val livello: String = "A1",
    val esercizi: List<EsercizioLessico> = emptyList(),
    val indice: Int = 0,
    val mostrata: Boolean = false,
    val corretta: Boolean = false,
    val rispostaData: String = "",
    val giuste: Int = 0,
    val sbagliate: List<String> = emptyList()
) {
    val corrente: EsercizioLessico? get() = esercizi.getOrNull(indice)
    val totale: Int get() = esercizi.size
    val finita: Boolean get() = indice >= esercizi.size
    val progresso: Float get() = if (totale == 0) 0f else indice.toFloat() / totale
}

/**
 * Lessico per livello (A1-B2).
 *
 * Una sezione per livello, ognuna con:
 * - il conteggio delle parole curate e quante sono già nel profilo;
 * - t("Aggiungi al ripasso"): importa tutto il livello, creando le carte SRS;
 * - t("Esercizi"): 6 tipi generati offline (significato, tedesco, articolo,
 *   cloze, scrivi, ascolto), mescolati, così si allena anche il richiamo attivo.
 *
 * L'elenco sotto è consultabile e filtrabile: ogni parola mostra se è già
 * stata importata, per capire cosa manca.
 */
@Composable
fun LessicoScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit,
    onVaiAEserciziLessico: () -> Unit
) {
    val stato by vm.statoLessico.collectAsStateWithLifecycle(emptyList())
    val paroleProfilo by vm.parole.collectAsStateWithLifecycle(emptyList())
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)

    var livelloScelto by remember { mutableStateOf("A1") }
    var query by remember { mutableStateOf("") }

    val chiaviProfilo = remember(paroleProfilo) {
        paroleProfilo.mapTo(HashSet()) { LessicoLivelli.chiaveParola(it.german, it.article) }
    }
    val riepilogo = stato.firstOrNull { it.livello == livelloScelto }
    val voci = remember(livelloScelto, query) {
        val tutte = LessicoLivelli.perLivello(livelloScelto)
        val q = query.trim()
        if (q.isBlank()) tutte else tutte.filter {
            it.tedesco.contains(q, ignoreCase = true) || it.italiano.contains(q, ignoreCase = true) || t(it.italiano).contains(q, ignoreCase = true)
        }
    }

    ScreenScaffold("Lessico A1-B2", onBack = onIndietro, scrollable = false) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text = t("${LessicoLivelli.tutte.size} parole curate, divise in parole nuove per ") +
                    "livello. I traguardi sono cumulativi (A1 650, A2 1.300, B1 2.400, B2 3.000): " +
                    "aggiungi un livello al ripasso e allenati in sei modi diversi.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                LessicoLivelli.LIVELLI.forEach { livello ->
                    val s = stato.firstOrNull { it.livello == livello }
                    FilterChip(
                        selected = livelloScelto == livello,
                        onClick = { livelloScelto = livello },
                        label = {
                            Text(
                                if (s == null) livello else t("$livello · ${s.nelProfilo}/${s.curate}")
                            )
                        }
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            if (riepilogo != null) {
                Card(
                    modifier = Modifier.fillMaxWidth().animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.fillMaxWidth().padding(14.dp)) {
                        Text(
                            t("Livello ${riepilogo.livello}"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            t("${riepilogo.curate} parole nuove di ${riepilogo.livello} · ") +
                                t("${riepilogo.nelProfilo} già nel tuo lessico") +
                                if (riepilogo.daRipassare > 0) " · ${riepilogo.daRipassare} da ripassare"
                                else "",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        val coperte = LessicoLivelli.cumulativo(riepilogo.livello)
                        val obiettivo = LessicoLivelli.targetCumulativo(riepilogo.livello)
                        Spacer(Modifier.height(8.dp))
                        Text(
                            t("Copertura fino a ${riepilogo.livello}: ") +
                                "%,d di %,d parole (obiettivo cumulativo)".format(coperte, obiettivo),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (coperte.toFloat() / obiettivo).coerceIn(0f, 1f) },
                            modifier = Modifier.fillMaxWidth(),
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            trackColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.25f)
                        )
                        if (riepilogo.temi.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                "Temi: " + riepilogo.temi.take(8).joinToString(" · "),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            OutlinedButton(
                                onClick = { vm.aggiungiLessico(livelloScelto) },
                                enabled = !caricamento,
                                modifier = Modifier.weight(1f)
                            ) { Text(t("Aggiungi al ripasso")) }
                            Button(
                                onClick = {
                                    vm.avviaEserciziLessico(livelloScelto)
                                    onVaiAEserciziLessico()
                                },
                                enabled = !caricamento,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Filled.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(Modifier.size(6.dp))
                                Text(t("Esercizi"))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(12.dp))
            }

            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                placeholder = { Text(t("Cerca in tedesco o in italiano")) }
            )

            Spacer(Modifier.height(8.dp))

            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(voci, key = { t("${it.livello}|${it.tedesco}") }) { voce ->
                    val presente = LessicoLivelli.chiaveParola(voce.tedesco) in chiaviProfilo
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                RoundedCornerShape(10.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                voce.tedesco,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                t(voce.italiano),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (voce.tema.isNotBlank()) {
                            Text(
                                voce.tema,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.size(8.dp))
                        }
                        if (presente) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = t("Già nel lessico"),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                item { Spacer(Modifier.height(12.dp)) }
            }
        }
    }
}
