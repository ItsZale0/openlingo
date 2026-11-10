package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import com.alessandro.tedesco.data.local.WordEntity
import com.alessandro.tedesco.desktop.DesktopStore
import com.alessandro.tedesco.desktop.etichetta
import com.alessandro.tedesco.i18n.t

@Composable
fun SchermataRipasso(store: DesktopStore) {
    store.repo
    val inScadenza = store.paroleDaRipassare()
    val statistiche = store.statistiche()

    var avvio by remember { mutableStateOf(0) }
    var indice by remember { mutableStateOf(0) }
    var sapute by remember { mutableStateOf(0) }
    var mostrata by remember { mutableStateOf(false) }

    val coda: List<WordEntity> = remember(avvio, store.repo.profiloAttivoId) {
        val dovute = store.paroleDaRipassare(limite = 20)
        if (dovute.size >= 10) dovute
        else (dovute + store.paroleNuove(limite = 20 - dovute.size)).distinctBy { it.id }
    }

    Pagina {
        Testata(
            titolo = t("Ripasso"),
            sottotitolo = "${inScadenza.size} " + t("in scadenza") + " · ${store.paroleNuove().size} " + t("nuove da vedere") + " · ${statistiche.mature} " + t("parole mature") + " · " + t("ripetizione spaziata")
        )

        if (coda.isEmpty()) {
            CardSezione(
                titolo = t("Nessuna carta da ripassare"),
                sottotitolo = t("Il lessico del profilo è vuoto oppure tutto è già al suo posto")
            ) {
                Text(
                    t("Aggiungi parole dalla Wortliste: entrano subito nella coda e il ripasso parte da lì."),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Text(
                    t("Parole disponibili nella Wortliste") + ": ${WortlisteB1.tutte.size}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            return@Pagina
        }

        if (indice >= coda.size) {
            CardSezione(
                titolo = t("Sessione completata"),
                sottotitolo = "${coda.size} " + t("carte ripassate")
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "$sapute / ${coda.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        t("risposte \"lo sapevo\""),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    t("Le carte sapute tornano tra 1, 3, 7, 16, 35, 75, 150 o 300 giorni; quelle sbagliate tornano domani e abbassano la facilità."),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Button(onClick = {
                    avvio += 1
                    indice = 0
                    sapute = 0
                    mostrata = false
                }) {
                    Text(t("Nuova sessione"))
                }
            }
            return@Pagina
        }

        val parola = coda[indice]
        val recensione = store.stato.reviews[parola.id]
        val nuova = recensione == null || recensione.repetitions == 0

        Card(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .widthIn(max = 780.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(26.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Etichetta(if (nuova) t("Nuova") else t("Ripasso") + " ${recensione.repetitions}")
                    Text(
                        "${indice + 1} / ${coda.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(Modifier.height(12.dp))
                BarraLineare(
                    valore = (indice + 1).toFloat() / coda.size,
                    modifier = Modifier.fillMaxWidth().height(6.dp)
                )

                Spacer(Modifier.height(30.dp))
                Text(
                    parola.etichetta(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                if (parola.example.isNotBlank() && mostrata) {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        parola.example,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(Modifier.height(22.dp))
                if (!mostrata) {
                    OutlinedButton(onClick = { mostrata = true }) {
                        Text(t("Mostra traduzione"))
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                    ) {
                        Column(
                            Modifier.fillMaxWidth().padding(18.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                t(parola.italian),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (parola.article != null) {
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    t("articolo") + ": ${parola.article}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                                )
                            }
                        }
                    }
                    Spacer(Modifier.height(18.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        OutlinedButton(onClick = {
                            store.rispondiRipasso(parola.id, saputa = false)
                            mostrata = false
                            indice += 1
                        }) {
                            Text(t("Non lo sapevo"))
                        }
                        Button(onClick = {
                            store.rispondiRipasso(parola.id, saputa = true)
                            sapute += 1
                            mostrata = false
                            indice += 1
                        }) {
                            Text(t("Lo sapevo"))
                        }
                    }
                }
            }
        }

        Text(
            t("Il ripasso aggiorna anche le statistiche: accuratezza, parole mature e giorni di studio."),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
