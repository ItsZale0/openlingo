package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.CalcoloProgresso
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.desktop.DesktopStore

@Composable
fun SchermataProgresso(store: DesktopStore) {
    store.repo
    val pr: CalcoloProgresso.ProgressoReale = store.progressoReale()
    val unita = store.unita()
    val completate = unita.count { it.padronanza?.completata == true }

    Pagina {
        Testata(
            titolo = "Progresso",
            sottotitolo = "Numeri reali, calcolati dai ripassi e dalle sessioni: niente stime"
        )

        CardSezione(
            titolo = "Livello ${pr.livello.label.substringBefore(" — ")}",
            sottotitolo = "Obiettivo ${pr.livelloObiettivo.label.substringBefore(" — ")}"
        ) {
            BarraProgresso(
                valore = pr.versoProssimoLivello,
                etichetta = pr.prossimoLivello?.let { "Verso ${it.label.substringBefore(" — ")}" }
                    ?: "Livello massimo raggiunto",
                dettaglio = "media delle quattro competenze"
            )
            Spacer(Modifier.height(12.dp))
            Text(
                "Dimensione più debole: ${pr.dimensioneDebole} (${pr.dimensioneDeboleValore.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                pr.azioneConsigliata,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kpi("${pr.paroleImparate}", "parole verificate", "su ${pr.paroleDisponibili}", modifier = Modifier.weight(1f))
            Kpi("${pr.ripassiTotali}", "ripassi", "${pr.accuratezza.toInt()}% accuratezza",
                MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
            Kpi("${pr.streakGiorni}", "giorni di fila", "record ${pr.streakRecord}",
                MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            Kpi("${pr.giorniStudioTotali}", "giorni di studio", "${pr.minutiStudiati} minuti",
                MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
        }

        CardSezione(titolo = "Competenze", sottotitolo = "Le quattro aree della certificazione, misurate sui dati") {
            BarraProgresso(pr.competenze.lessico / 100f, "Lessico", "parole dimostrate nel quiz")
            Spacer(Modifier.height(12.dp))
            BarraProgresso(pr.competenze.grammatica / 100f, "Grammatica", "sessioni delle unità", MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))
            BarraProgresso(pr.competenze.comprensione / 100f, "Comprensione", "test di lettura", MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(12.dp))
            BarraProgresso(pr.competenze.produzione / 100f, "Produzione", "test di produzione")
            Text(
                "Soglia ufficiale Goethe/telc/ÖSD: 60% per modulo.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        CardSezione(
            titolo = "Percorso grammaticale",
            sottotitolo = "$completate unità completate su ${GrammaticaSyllabus.unita.size}"
        ) {
            BarraProgresso(
                valore = if (GrammaticaSyllabus.unita.isEmpty()) 0f
                else completate.toFloat() / GrammaticaSyllabus.unita.size,
                etichetta = "Unità del percorso",
                dettaglio = "soglia di padronanza ${GrammaticaSyllabus.SOGLIA_PADRONANZA}%"
            )
            Spacer(Modifier.height(10.dp))
            val ultime = unita.filter { it.padronanza?.completata == true }.takeLast(5)
            if (ultime.isEmpty()) {
                Text(
                    "Nessuna unità completata: apri la prima lezione dagli Esercizi.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ultime.forEach { voce ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(voce.unita.titolo, style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${voce.padronanza?.percentuale ?: 0}%",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.tertiary
                        )
                    }
                }
            }
        }

        CardSezione(
            titolo = "Traguardi",
            sottotitolo = "${pr.traguardiRaggiunti} raggiunti su ${pr.traguardi.size}"
        ) {
            pr.traguardi.chunked(2).forEach { coppia ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    coppia.forEach { traguardo ->
                        Etichetta(
                            traguardo.nome,
                            colore = if (traguardo.raggiunto) MaterialTheme.colorScheme.tertiaryContainer
                            else MaterialTheme.colorScheme.surfaceContainerHigh,
                            coloreTesto = if (traguardo.raggiunto) MaterialTheme.colorScheme.onTertiaryContainer
                            else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (coppia.size == 1) Spacer(modifier = Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }
        }

        CardSezione(
            titolo = "Riferimenti ufficiali",
            sottotitolo = "Wortliste e monte ore per il livello obiettivo"
        ) {
            RigaRiferimento("Wortliste ufficiale", "${pr.wortlisteUfficiale} unità lessicali")
            RigaRiferimento("Vocabolario attivo", "${pr.vocabolarioAttivoUfficiale} parole circa")
            RigaRiferimento("Ore guida cumulate", "${pr.oreGuida.first}-${pr.oreGuida.last} ore")
            RigaRiferimento("Parole mancanti al B1", "${pr.paroleMancantiAlB1}")
        }
    }
}

@Composable
private fun RigaRiferimento(etichetta: String, valore: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(etichetta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.width(12.dp))
        Text(valore, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
    }
}
