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
import com.alessandro.tedesco.i18n.t

@Composable
fun SchermataProgresso(store: DesktopStore) {
    store.repo
    val pr: CalcoloProgresso.ProgressoReale = store.progressoReale()
    val unita = store.unita()
    val completate = unita.count { it.padronanza?.completata == true }

    Pagina {
        Testata(
            titolo = t("Progresso"),
            sottotitolo = t("Numeri reali, calcolati dai ripassi e dalle sessioni: niente stime")
        )

        CardSezione(
            titolo = t("Livello") + " ${pr.livello.label.substringBefore(" — ")}",
            sottotitolo = t("Obiettivo") + " ${pr.livelloObiettivo.label.substringBefore(" — ")}"
        ) {
            BarraProgresso(
                valore = pr.versoProssimoLivello,
                etichetta = pr.prossimoLivello?.let { t("Verso") + " ${it.label.substringBefore(" — ")}" }
                    ?: t("Livello massimo raggiunto"),
                dettaglio = t("media delle quattro competenze")
            )
            Spacer(Modifier.height(12.dp))
            Text(
                t("Dimensione più debole") + ": ${t(pr.dimensioneDebole)} (${pr.dimensioneDeboleValore.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                t(pr.azioneConsigliata),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kpi("${pr.paroleImparate}", t("parole verificate"), t("su") + " ${pr.paroleDisponibili}", modifier = Modifier.weight(1f))
            Kpi("${pr.ripassiTotali}", t("ripassi"), "${pr.accuratezza.toInt()}% " + t("accuratezza"),
                MaterialTheme.colorScheme.secondary, modifier = Modifier.weight(1f))
            Kpi("${pr.streakGiorni}", t("giorni di fila"), t("record") + " ${pr.streakRecord}",
                MaterialTheme.colorScheme.tertiary, modifier = Modifier.weight(1f))
            Kpi("${pr.giorniStudioTotali}", t("giorni di studio"), "${pr.minutiStudiati} " + t("minuti"),
                MaterialTheme.colorScheme.primary, modifier = Modifier.weight(1f))
        }

        CardSezione(titolo = t("Competenze"), sottotitolo = t("Le quattro aree della certificazione, misurate sui dati")) {
            BarraProgresso(pr.competenze.lessico / 100f, t("Lessico"), t("parole dimostrate nel quiz"))
            Spacer(Modifier.height(12.dp))
            BarraProgresso(pr.competenze.grammatica / 100f, t("Grammatica"), t("sessioni delle unità"), MaterialTheme.colorScheme.secondary)
            Spacer(Modifier.height(12.dp))
            BarraProgresso(pr.competenze.comprensione / 100f, t("Comprensione"), t("test di lettura"), MaterialTheme.colorScheme.tertiary)
            Spacer(Modifier.height(12.dp))
            BarraProgresso(pr.competenze.produzione / 100f, t("Produzione"), t("test di produzione"))
            Text(
                t("Soglia ufficiale Goethe/telc/ÖSD: 60% per modulo."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 10.dp)
            )
        }

        CardSezione(
            titolo = t("Percorso grammaticale"),
            sottotitolo = "$completate " + t("unità completate su") + " ${GrammaticaSyllabus.unita.size}"
        ) {
            BarraProgresso(
                valore = if (GrammaticaSyllabus.unita.isEmpty()) 0f
                else completate.toFloat() / GrammaticaSyllabus.unita.size,
                etichetta = t("Unità del percorso"),
                dettaglio = t("soglia di padronanza") + " ${GrammaticaSyllabus.SOGLIA_PADRONANZA}%"
            )
            Spacer(Modifier.height(10.dp))
            val ultime = unita.filter { it.padronanza?.completata == true }.takeLast(5)
            if (ultime.isEmpty()) {
                Text(
                    t("Nessuna unità completata: apri la prima lezione dagli Esercizi."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                ultime.forEach { voce ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(t(voce.unita.titolo), style = MaterialTheme.typography.bodySmall)
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
            titolo = t("Traguardi"),
            sottotitolo = "${pr.traguardiRaggiunti} " + t("raggiunti su") + " ${pr.traguardi.size}"
        ) {
            pr.traguardi.chunked(2).forEach { coppia ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    coppia.forEach { traguardo ->
                        Etichetta(
                            t(traguardo.nome),
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
            titolo = t("Riferimenti ufficiali"),
            sottotitolo = t("Wortliste e monte ore per il livello obiettivo")
        ) {
            RigaRiferimento(t("Wortliste ufficiale"), "${pr.wortlisteUfficiale} " + t("unità lessicali"))
            RigaRiferimento(t("Vocabolario attivo"), "${pr.vocabolarioAttivoUfficiale} " + t("parole circa"))
            RigaRiferimento(t("Ore guida cumulate"), "${pr.oreGuida.first}-${pr.oreGuida.last} " + t("ore"))
            RigaRiferimento(t("Parole mancanti al B1"), "${pr.paroleMancantiAlB1}")
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
