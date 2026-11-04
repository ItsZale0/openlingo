package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.clickable
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.desktop.DesktopStore
import com.alessandro.tedesco.desktop.Schermata

@Composable
fun SchermataHome(
    store: DesktopStore,
    onVaiA: (Schermata) -> Unit,
    onApriUnita: (String) -> Unit
) {
    // Legge lo stato: ogni azione (ripasso, esercizio, passo) fa ricomporre.
    store.repo
    val profilo = store.profilo
    val piano = store.pianoOggi()
    val progresso = store.progressoReale()
    val statistiche = store.statistiche()
    val prossima = store.prossimaUnita()

    val nome = profilo?.config?.nomeVisualizzato?.substringBefore(" ") ?: "studente"

    Pagina {
        Testata(
            titolo = "Ciao, $nome",
            sottotitolo = "Piano di oggi: ${piano.passiTotali} passi, circa ${piano.minutiTotali} minuti. " +
                "Tutto offline, senza AI."
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Column(Modifier.fillMaxWidth().padding(22.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            "Piano di oggi",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            piano.data.replaceFirstChar { it.uppercase() },
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${piano.passiCompletati}/${piano.passiTotali}",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            "passi fatti",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                        )
                    }
                }

                Spacer(Modifier.height(14.dp))
                BarraLineare(
                    valore = piano.progressoGiornata,
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    colore = MaterialTheme.colorScheme.primary,
                    traccia = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.18f)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "${piano.minutiCompletati} di ${piano.minutiTotali} minuti · ${piano.passiTotali} passi",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                )

                Spacer(Modifier.height(16.dp))
                piano.passi.forEach { passo ->
                    RigaPasso(
                        passo = passo,
                        onApri = { apriPasso(passo, onVaiA, onApriUnita) },
                        onCompleta = { store.completaPasso(passo.id) }
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    piano.messaggioMotivazionale,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Kpi(
                valore = "${statistiche.paroleTotali}",
                etichetta = "parole in lessico",
                dettaglio = "${statistiche.nuove} ancora da vedere",
                modifier = Modifier.weight(1f)
            )
            Kpi(
                valore = "${statistiche.mature}",
                etichetta = "parole mature",
                dettaglio = "riconosciute a ripetizione",
                colore = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
            Kpi(
                valore = "${statistiche.daRipassare}",
                etichetta = "da ripassare",
                dettaglio = "in scadenza oggi",
                colore = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            Kpi(
                valore = "${statistiche.accuratezza.toInt()}%",
                etichetta = "accuratezza",
                dettaglio = "${statistiche.ripassiTotali} ripassi",
                colore = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
        }

        val unita = prossima
        if (unita != null) {
            CardSezione(
                titolo = "Continua il percorso",
                sottotitolo = "La prossima lezione consigliata per il tuo livello"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Etichetta(unita.unita.livello)
                            Spacer(Modifier.width(8.dp))
                            Text(
                                "${unita.unita.minuti} min",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(Modifier.height(6.dp))
                        Text(unita.unita.titolo, style = MaterialTheme.typography.titleMedium)
                        Text(
                            unita.unita.obiettivo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        val padronanza = unita.padronanza
                        if (padronanza != null && padronanza.totali > 0) {
                            Spacer(Modifier.height(10.dp))
                            BarraProgresso(
                                valore = padronanza.percentuale / 100f,
                                etichetta = "Padronanza",
                                dettaglio = "${padronanza.corrette}/${padronanza.totali} risposte corrette"
                            )
                        }
                    }
                    Spacer(Modifier.width(20.dp))
                    Button(onClick = { onApriUnita(unita.unita.id) }) {
                        Icon(Icons.Filled.PlayArrow, contentDescription = null)
                        Spacer(Modifier.width(6.dp))
                        Text("Apri lezione")
                    }
                }
            }
        } else {
            CardSezione(titolo = "Percorso completato", sottotitolo = "Tutte le unità sono state affrontate") {
                Text(
                    "Ripassa le unità completate o lavora sul lessico: il percorso A0-B2 è finito.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Text("Strumenti", style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Strumento(
                icona = Icons.Filled.Refresh,
                titolo = "Ripasso",
                testo = "${store.paroleDaRipassare().size} carte in scadenza oggi",
                onClick = { onVaiA(Schermata.RIPASSO) },
                modifier = Modifier.weight(1f)
            )
            Strumento(
                icona = Icons.Filled.School,
                titolo = "Esercizi",
                testo = "${GrammaticaSyllabus.unita.size} unità di grammatica",
                onClick = { onVaiA(Schermata.ESERCIZI) },
                modifier = Modifier.weight(1f)
            )
            Strumento(
                icona = Icons.AutoMirrored.Filled.MenuBook,
                titolo = "Wortliste",
                testo = "Parole curate A1-B1 da aggiungere",
                onClick = { onVaiA(Schermata.WORTLISTE) },
                modifier = Modifier.weight(1f)
            )
            Strumento(
                icona = Icons.Filled.Headphones,
                titolo = "Podcast",
                testo = "Ascolto autentico per livello",
                onClick = { onVaiA(Schermata.PODCAST) },
                modifier = Modifier.weight(1f)
            )
        }

        OutlinedButton(onClick = { onVaiA(Schermata.PROGRESSO) }) {
            Icon(Icons.Filled.BarChart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Livello ${progresso.livello.codiceBreve()} · " +
                    "${progresso.traguardiRaggiunti}/${progresso.traguardi.size} traguardi · vedi progresso"
            )
        }
    }
}

@Composable
private fun RigaPasso(
    passo: CalcoloPercorsoGiornaliero.PassoPercorso,
    onApri: () -> Unit,
    onCompleta: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.55f)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = if (passo.completato) Icons.Filled.CheckCircle else Icons.Filled.PlayArrow,
                contentDescription = null,
                tint = if (passo.completato) MaterialTheme.colorScheme.tertiary
                else MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size(22.dp)
            )
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    passo.titolo,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    passo.descrizione,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (passo.completato) {
                Etichetta("Fatto", MaterialTheme.colorScheme.tertiaryContainer, MaterialTheme.colorScheme.onTertiaryContainer)
            } else {
                TextButton(onClick = onApri) { Text("Apri") }
                Spacer(Modifier.width(4.dp))
                OutlinedButton(onClick = onCompleta) { Text("Segna fatto") }
            }
        }
    }
}

@Composable
private fun Strumento(
    icona: ImageVector,
    titolo: String,
    testo: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(16.dp)) {
            Icon(icona, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(10.dp))
            Text(titolo, style = MaterialTheme.typography.titleSmall)
            Text(
                testo,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

private fun apriPasso(
    passo: CalcoloPercorsoGiornaliero.PassoPercorso,
    onVaiA: (Schermata) -> Unit,
    onApriUnita: (String) -> Unit
) {
    when {
        passo.azione == "ripasso" -> onVaiA(Schermata.RIPASSO)
        passo.azione == "wortliste" -> onVaiA(Schermata.WORTLISTE)
        passo.azione.startsWith("unita_grammatica/") ->
            onApriUnita(passo.azione.removePrefix("unita_grammatica/"))
        passo.azione == "grammatica" -> onVaiA(Schermata.ESERCIZI)
        else -> onVaiA(Schermata.ESERCIZI)
    }
}
