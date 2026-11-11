package com.alessandro.openlingo.desktop.ui

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
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.alessandro.openlingo.data.PodcastData
import com.alessandro.openlingo.desktop.DesktopStore
import java.awt.Desktop
import java.net.URI
import com.alessandro.openlingo.i18n.t

@Composable
fun SchermataPodcast(store: DesktopStore) {
    store.repo
    val livelloProfilo = PodcastData.normalizza(store.stato.progresso.livelloCorrente.codiceBreve())
    var livelloScelto by remember { mutableStateOf<String?>(null) }
    var tipo by remember { mutableStateOf<PodcastData.Tipo?>(null) }
    var messaggio by remember { mutableStateOf("") }

    val livello = livelloScelto ?: livelloProfilo
    val info = PodcastData.info(livello)
    val episodi = remember(livello, tipo) { PodcastData.filtra(livello, tipo) }

    Pagina {
        Testata(
            titolo = t("Podcast e video"),
            sottotitolo = t("Ascolto autentico per il livello $livello · i link si aprono nel browser")
        )

        CardSezione(titolo = t(info.titolo), sottotitolo = t("Livello $livello")) {
            Text(t(info.obiettivo), style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.height(6.dp))
            Text(
                t(info.consiglio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            PodcastData.livelli().forEach { codice ->
                FilterChip(
                    selected = livello == codice,
                    onClick = { livelloScelto = codice },
                    label = { Text(codice) }
                )
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = tipo == null,
                onClick = { tipo = null },
                label = { Text(t("Tutti")) }
            )
            PodcastData.Tipo.entries.forEach { t ->
                FilterChip(
                    selected = tipo == t,
                    onClick = { tipo = t },
                    label = { Text(t(t.etichetta)) }
                )
            }
        }

        if (messaggio.isNotBlank()) {
            Text(
                messaggio,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.error
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            LazyColumn(modifier = Modifier.fillMaxWidth().height(520.dp).padding(10.dp)) {
                items(episodi, key = { it.id }) { episodio ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    t(episodio.titolo),
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                if (episodio.consigliato) {
                                    Spacer(Modifier.width(8.dp))
                                    Etichetta(
                                        t("Consigliato"),
                                        MaterialTheme.colorScheme.tertiaryContainer,
                                        MaterialTheme.colorScheme.onTertiaryContainer
                                    )
                                }
                            }
                            Text(
                                "${episodio.canale} · ${episodio.durata} · ${t(episodio.velocita.etichetta)} · ${t(episodio.tema)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                t(episodio.descrizione),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.height(4.dp))
                            Text(
                                t("Supporto") + ": ${episodio.supporto}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.width(12.dp))
                        Etichetta(t(episodio.piattaforma.etichetta))
                        Spacer(Modifier.width(10.dp))
                        OutlinedButton(onClick = {
                            val esito = apriLink(episodio.url)
                            messaggio = if (esito) "" else t("Impossibile aprire il link nel browser.")
                        }) {
                            Icon(Icons.Filled.OpenInNew, contentDescription = null)
                            Spacer(Modifier.width(4.dp))
                            Text(t("Apri"))
                        }
                    }
                }
            }
        }

        Text(
            t("Serve una connessione solo per il contenuto esterno: il resto dell'app funziona offline."),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

private fun apriLink(url: String): Boolean = runCatching {
    if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
        Desktop.getDesktop().browse(URI(url))
        true
    } else false
}.getOrDefault(false)
