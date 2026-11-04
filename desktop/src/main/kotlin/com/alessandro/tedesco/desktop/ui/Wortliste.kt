package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import com.alessandro.tedesco.desktop.DesktopStore

@Composable
fun SchermataWortliste(store: DesktopStore) {
    store.repo
    var ricerca by remember { mutableStateOf("") }
    var livello by remember { mutableStateOf<String?>(null) }
    var messaggio by remember { mutableStateOf("") }

    val nelLessico = store.stato.parole.map { it.id }.toSet()
    val elenco = remember(ricerca, livello) {
        val base = if (ricerca.isBlank()) {
            if (livello == null) WortlisteB1.tutte else WortlisteB1.perLivello(livello!!)
        } else {
            WortlisteB1.cerca(ricerca).let { trovate ->
                if (livello == null) trovate else trovate.filter { it.livello == livello }
            }
        }
        base.distinctBy { it.tedesco.lowercase() }
    }
    val aggiungibili = elenco.count { voce -> !store.giaNelLessico(voce) }

    Pagina {
        Testata(
            titolo = "Wortliste A1-B1",
            sottotitolo = "${WortlisteB1.tutte.size} parole curate · ${nelLessico.size} nel tuo lessico · " +
                "$aggiungibili da aggiungere in questa vista"
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = ricerca,
                onValueChange = { ricerca = it },
                modifier = Modifier.weight(1f),
                singleLine = true,
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                label = { Text("Cerca in tedesco o in italiano") }
            )
            Spacer(Modifier.width(12.dp))
            Button(
                onClick = {
                    val aggiunte = store.importaWortliste()
                    messaggio = if (aggiunte == 0) "Tutte le parole erano già nel lessico."
                    else "Aggiunte $aggiunte parole al lessico."
                }
            ) {
                Text("Importa tutto")
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf<String?>(null, "A1", "A2", "B1").forEach { codice ->
                FilterChip(
                    selected = livello == codice,
                    onClick = { livello = codice },
                    label = { Text(codice ?: "Tutti") }
                )
            }
        }

        if (messaggio.isNotBlank()) {
            Text(
                messaggio,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            if (elenco.isEmpty()) {
                Text(
                    "Nessuna parola per questa ricerca.",
                    modifier = Modifier.padding(22.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(560.dp).padding(8.dp)) {
                    items(elenco, key = { it.tedesco.lowercase() + it.livello }) { voce ->
                        val presente = store.giaNelLessico(voce)
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        voce.tedesco,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Etichetta(voce.livello)
                                }
                                Text(
                                    voce.italiano,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (voce.esempio.isNotBlank()) {
                                    Text(
                                        voce.esempio,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                voce.tema,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            if (presente) {
                                OutlinedButton(onClick = {}, enabled = false) {
                                    Icon(Icons.Filled.Check, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Nel lessico")
                                }
                            } else {
                                Button(onClick = {
                                    val aggiunta = store.aggiungiParola(voce)
                                    messaggio = if (aggiunta) "Aggiunta: ${voce.tedesco}"
                                    else "Già presente nel lessico."
                                }) {
                                    Icon(Icons.Filled.Add, contentDescription = null)
                                    Spacer(Modifier.width(4.dp))
                                    Text("Aggiungi")
                                }
                            }
                        }
                    }
                }
            }
        }

        Text(
            "Le parole aggiunte entrano nella coda di ripasso: la prima risposta crea la carta SRS.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
