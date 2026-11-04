package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.AlertDialog
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.CalcoloPercorsoGiornaliero
import com.alessandro.tedesco.desktop.DesktopStore
import com.alessandro.tedesco.desktop.Schermata

@Composable
fun SchermataHome(
    store: DesktopStore,
    onVaiA: (Schermata) -> Unit,
    onApriUnita: (String) -> Unit
) {
    store.repo
    val profilo = store.profilo
    val piano = store.pianoOggi()
    val progresso = store.progressoReale()
    val prossima = store.prossimaUnita()

    val nome = profilo?.config?.nomeVisualizzato?.substringBefore(" ") ?: "studente"
    val prossimoPasso = piano.prossimoPasso

    var mostraPiano by remember { mutableStateOf(false) }

    Pagina {
        Testata(
            titolo = "Ciao, $nome",
            sottotitolo = "Piano di oggi: ${piano.passiCompletati}/${piano.passiTotali} passi, circa ${piano.minutiTotali} minuti."
        )

        // Hero compatto: solo il prossimo passo, il piano completo nel pannello.
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
                    verticalAlignment = Alignment.Top
                ) {
                    Column(Modifier.weight(1f)) {
                        Text(
                            "PIANO DI OGGI",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.65f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            prossimoPasso?.titolo ?: "Piano completato",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            prossimoPasso?.descrizione ?: piano.messaggioMotivazionale,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.82f)
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            "${piano.passiCompletati}/${piano.passiTotali}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
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
                    modifier = Modifier.fillMaxWidth().height(6.dp),
                    colore = MaterialTheme.colorScheme.primary,
                    traccia = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.16f)
                )
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${piano.minutiCompletati} di ${piano.minutiTotali} minuti",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f),
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = { mostraPiano = true }) {
                        Text("Vedi piano")
                    }
                    Spacer(Modifier.width(4.dp))
                    if (prossimoPasso != null) {
                        Button(onClick = {
                            apriPasso(prossimoPasso, onVaiA, onApriUnita)
                        }) {
                            Text(if (piano.passiCompletati == 0) "Inizia" else "Continua")
                        }
                    } else {
                        OutlinedButton(onClick = { onVaiA(Schermata.RIPASSO) }) {
                            Text("Ripasso")
                        }
                    }
                }
            }
        }

        // Azioni rapide essenziali.
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AzioneRapidaDesktop(
                icona = Icons.Filled.Refresh,
                etichetta = "Ripasso",
                onClick = { onVaiA(Schermata.RIPASSO) },
                modifier = Modifier.weight(1f)
            )
            AzioneRapidaDesktop(
                icona = Icons.Filled.Translate,
                etichetta = "Grammatica",
                onClick = { onVaiA(Schermata.ESERCIZI) },
                modifier = Modifier.weight(1f)
            )
            AzioneRapidaDesktop(
                icona = Icons.AutoMirrored.Filled.MenuBook,
                etichetta = "Wortliste",
                onClick = { onVaiA(Schermata.WORTLISTE) },
                modifier = Modifier.weight(1f)
            )
            AzioneRapidaDesktop(
                icona = Icons.Filled.BarChart,
                etichetta = "Progresso",
                onClick = { onVaiA(Schermata.PROGRESSO) },
                modifier = Modifier.weight(1f)
            )
        }

        // Continua il percorso: una sola card.
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

        // Accesso rapido al progresso.
        OutlinedButton(onClick = { onVaiA(Schermata.PROGRESSO) }) {
            Icon(Icons.Filled.BarChart, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(
                "Livello ${progresso.livello.codiceBreve()} · " +
                    "${progresso.traguardiRaggiunti}/${progresso.traguardi.size} traguardi · vedi progresso"
            )
        }
    }

    if (mostraPiano) {
        AlertDialog(
            onDismissRequest = { mostraPiano = false },
            confirmButton = {
                TextButton(onClick = { mostraPiano = false }) { Text("Chiudi") }
            },
            title = { Text("Piano di oggi") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    piano.passi.forEach { passo ->
                        RigaPasso(
                            passo = passo,
                            onApri = {
                                mostraPiano = false
                                apriPasso(passo, onVaiA, onApriUnita)
                            },
                            onCompleta = { store.completaPasso(passo.id) }
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        piano.messaggioMotivazionale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        )
    }
}

@Composable
private fun AzioneRapidaDesktop(
    icona: ImageVector,
    etichetta: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .heightIn(min = 84.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                icona,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                etichetta,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Medium,
                maxLines = 1
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