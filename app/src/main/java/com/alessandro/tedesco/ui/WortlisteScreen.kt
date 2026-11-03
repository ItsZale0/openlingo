package com.alessandro.tedesco.ui

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.WortlisteB1
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Wortliste curata A1-B1 (oltre 280 parole ad alta frequenza): ricerca,
 * filtro per livello e tema, aggiunta al proprio lessico con un tocco.
 */
@Composable
fun WortlisteScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    var query by remember { mutableStateOf("") }
    var tema by remember { mutableStateOf<String?>(null) }
    var livello by remember { mutableStateOf<String?>(null) }
    var aggiunte by remember { mutableStateOf<Set<String>>(emptySet()) }
    val temi = remember { WortlisteB1.temi }
    val livelli = remember { WortlisteB1.livelli }
    val voci = remember(query, tema, livello) {
        var lista = when {
            query.isNotBlank() -> WortlisteB1.cerca(query)
            tema != null -> WortlisteB1.perTema(tema!!)
            else -> WortlisteB1.tutte
        }
        if (query.isNotBlank() && tema != null) {
            lista = lista.filter { it.tema == tema }
        }
        if (livello != null) {
            lista = lista.filter { it.livello == livello }
        }
        lista
    }

    Column(Modifier.fillMaxSize()) {
        CompactTopBar(title = "Wortliste A1-B1", onBack = onIndietro)
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = Spaziature.md, vertical = Spaziature.sm)
        ) {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Cerca in tedesco o italiano") },
                singleLine = true
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                "Ogni parola imparata e un passo avanti. Cerca e ripassa!",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.sm))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
            ) {
                FilterChip(
                    selected = livello == null,
                    onClick = { livello = null },
                    label = { Text("Tutti i livelli") }
                )
                livelli.forEach { l ->
                    FilterChip(
                        selected = livello == l,
                        onClick = { livello = if (livello == l) null else l },
                        label = { Text(l) }
                    )
                }
            }
            Spacer(Modifier.height(Spaziature.xs))
            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
            ) {
                FilterChip(
                    selected = tema == null,
                    onClick = { tema = null },
                    label = { Text("Tutti i temi") }
                )
                temi.forEach { t ->
                    FilterChip(
                        selected = tema == t,
                        onClick = { tema = if (tema == t) null else t },
                        label = { Text(t) }
                    )
                }
            }
            Text(
                "${voci.size} parole - tocca + per aggiungerle al tuo lessico",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = Spaziature.md,
                end = Spaziature.md,
                top = Spaziature.xs,
                bottom = Spaziature.xxl
            ),
            verticalArrangement = Arrangement.spacedBy(Spaziature.xs)
        ) {
            items(voci, key = { it.tedesco }) { voce ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = Spaziature.md, vertical = Spaziature.sm),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.Bottom) {
                                val articolo = WortlisteB1.articoloDi(voce.tedesco)
                                if (articolo != null) {
                                    Text(
                                        "$articolo ",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    WortlisteB1.parolaSenzaArticolo(voce.tedesco),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Text(
                                voce.italiano,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (voce.esempio.isNotBlank()) {
                                Text(
                                    voce.esempio,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Text(
                                "${voce.livello} · ${voce.tema}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (voce.tedesco in aggiunte) {
                            Icon(
                                Icons.Filled.CheckCircle,
                                contentDescription = "Aggiunta",
                                modifier = Modifier.size(22.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        } else {
                            IconButton(onClick = {
                                vm.aggiungiParolaWortliste(voce.tedesco, voce.italiano, voce.esempio, voce.livello)
                                aggiunte = aggiunte + voce.tedesco
                            }) {
                                Icon(
                                    Icons.Filled.AddCircleOutline,
                                    contentDescription = "Aggiungi al lessico",
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
