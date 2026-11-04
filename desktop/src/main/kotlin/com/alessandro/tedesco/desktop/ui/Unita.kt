package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.EsempioGrammatica
import com.alessandro.tedesco.data.EsercizioUnita
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.SessioneLezione
import com.alessandro.tedesco.data.SessioneUnita
import com.alessandro.tedesco.data.TabellaGrammatica
import com.alessandro.tedesco.data.TipoEsercizioGrammatica
import com.alessandro.tedesco.data.UnitaGrammatica
import com.alessandro.tedesco.desktop.DesktopStore
import kotlin.random.Random

@Composable
fun SchermataUnita(store: DesktopStore, unitaId: String, onIndietro: () -> Unit) {
    store.repo
    val unita = GrammaticaSyllabus.perId(unitaId)
    if (unita == null) {
        Pagina {
            Text("Unità non trovata.", style = MaterialTheme.typography.bodyLarge)
            OutlinedButton(onClick = onIndietro) { Text("Torna agli esercizi") }
        }
        return
    }

    var scheda by remember(unitaId) { mutableStateOf(0) }
    val padronanza = store.padronanza(unitaId)
    // La sessione vive qui, non dentro la scheda: cambiare fra Teoria ed
    // Esercizi non deve far ripartire i quesiti da capo.
    val stato = remember(unitaId) {
        StatoSessione(SessioneLezione.sessione(unita.esercizi, store.rotazione(unita.id)))
    }

    Pagina {
        TextButton(onClick = onIndietro) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text("Esercizi")
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Etichetta(unita.livello)
                    Spacer(Modifier.width(10.dp))
                    Text(
                        "${unita.minuti} min",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)
                    )
                    Spacer(Modifier.weight(1f))
                    if (padronanza != null && padronanza.totali > 0) {
                        Text(
                            "Padronanza ${padronanza.percentuale}%",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    unita.titolo,
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    unita.sottotitolo,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f)
                )
                Spacer(Modifier.height(10.dp))
                Text(
                    "Obiettivo: ${unita.obiettivo}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        TabRow(selectedTabIndex = scheda) {
            Tab(
                selected = scheda == 0,
                onClick = { scheda = 0 },
                text = { Text("Teoria") }
            )
            Tab(
                selected = scheda == 1,
                onClick = { scheda = 1 },
                text = { Text("Esercizi (${unita.esercizi.size})") }
            )
        }

        if (scheda == 0) {
            TeoriaUnita(unita = unita, onVaiEsercizi = { scheda = 1 })
        } else {
            EserciziUnita(
                store = store,
                unita = unita,
                stato = stato,
                onVersoPercorso = onIndietro
            )
        }
    }
}

// ---------------------------------------------------------------- teoria

@Composable
private fun TeoriaUnita(unita: UnitaGrammatica, onVaiEsercizi: () -> Unit) {
    val teoria = unita.teoria

    CardSezione(titolo = "Regola", sottotitolo = "Il cuore della unità, in ordine") {
        teoria.regola.forEach { regola ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    "•",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(8.dp))
                Text(regola, style = MaterialTheme.typography.bodyMedium)
            }
        }
    }

    teoria.tabelle.forEach { tabella ->
        TabellaCard(tabella)
    }

    if (teoria.esempi.isNotEmpty()) {
        CardSezione(titolo = "Esempi", sottotitolo = "Tedesco e italiano, a confronto") {
            teoria.esempi.forEach { esempio ->
                EsempioCard(esempio)
                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (teoria.erroriTipici.isNotEmpty()) {
        CardSezione(titolo = "Errori tipici", sottotitolo = "Le trappole per chi parla italiano") {
            teoria.erroriTipici.forEach { errore ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Close,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            errore.sbagliato,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                errore.corretto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            errore.spiegazione,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }

    if (teoria.merksatz.isNotBlank()) {
        CardSezione(colore = MaterialTheme.colorScheme.tertiaryContainer) {
            Text(
                "Merksatz",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                teoria.merksatz,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
    }

    Button(onClick = onVaiEsercizi) {
        Text("Passa agli esercizi (${unita.esercizi.size})")
    }
}

@Composable
private fun TabellaCard(tabella: TabellaGrammatica) {
    CardSezione(titolo = tabella.titolo) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh
        ) {
            Column(Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp)) {
                    tabella.intestazioni.forEach { intestazione ->
                        Text(
                            intestazione,
                            modifier = Modifier.weight(1f),
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                tabella.righe.forEach { riga ->
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        riga.forEach { cella ->
                            Text(
                                cella,
                                modifier = Modifier.weight(1f),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }
        if (tabella.nota.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(
                tabella.nota,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EsempioCard(esempio: EsempioGrammatica) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(esempio.tedesco, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
            Text(
                esempio.italiano,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (esempio.nota.isNotBlank()) {
                Text(
                    esempio.nota,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

// ------------------------------------------------------------- esercizi

private class StatoSessione(sessione: SessioneUnita) {
    var sessione by mutableStateOf(sessione)
    var indice by mutableStateOf(0)
    var corrette by mutableStateOf(0)
    var scelta by mutableStateOf<Int?>(null)
    var testo by mutableStateOf("")
    var esito by mutableStateOf<Boolean?>(null)
    var finito by mutableStateOf(false)

    val esercizioCorrente: EsercizioUnita?
        get() = sessione.esercizi.getOrNull(indice)
}

@Composable
private fun EserciziUnita(
    store: DesktopStore,
    unita: UnitaGrammatica,
    stato: StatoSessione,
    onVersoPercorso: () -> Unit
) {
    val esercizio = stato.esercizioCorrente

    if (esercizio == null || stato.finito) {
        RisultatoSessione(
            corrette = stato.corrette,
            totali = stato.sessione.esercizi.size,
            onRipeti = {
                val rotazione = stato.sessione.rotazione
                val nuova = SessioneLezione.nuovaSessione(
                    banco = unita.esercizi,
                    rotazione = rotazione,
                    seme = Random.nextInt(1, Int.MAX_VALUE)
                )
                stato.sessione = nuova
                stato.indice = 0
                stato.corrette = 0
                stato.scelta = null
                stato.testo = ""
                stato.esito = null
                stato.finito = false
            },
            onTornaPercorso = onVersoPercorso
        )
        return
    }

    val totale = stato.sessione.esercizi.size
    CardSezione(
        titolo = "Esercizio ${stato.indice + 1} di $totale",
        sottotitolo = tipoEsercizio(esercizio.tipo)
    ) {
        BarraLineare(
            valore = stato.indice.toFloat() / totale,
            modifier = Modifier.fillMaxWidth().height(6.dp)
        )
        Spacer(Modifier.height(14.dp))
        Text(esercizio.domanda, style = MaterialTheme.typography.bodyLarge)
        if (esercizio.suggerimento.isNotBlank() && stato.esito == null) {
            Spacer(Modifier.height(6.dp))
            Text(
                "Suggerimento: ${esercizio.suggerimento}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(14.dp))

        if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            esercizio.opzioni.forEachIndexed { indice, opzione ->
                val selezionata = stato.scelta == indice
                val sbloccata = stato.esito == null
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable(enabled = sbloccata) { stato.scelta = indice },
                    shape = RoundedCornerShape(10.dp),
                    color = when {
                        !sbloccata && indice == esercizio.rispostaCorretta ->
                            MaterialTheme.colorScheme.tertiaryContainer
                        !sbloccata && selezionata -> MaterialTheme.colorScheme.errorContainer
                        selezionata -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surfaceContainerHigh
                    },
                    border = BorderStroke(
                        1.dp,
                        if (selezionata) MaterialTheme.colorScheme.primary
                        else Color.Transparent
                    )
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "${'A' + indice}",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.width(12.dp))
                        Text(opzione, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        } else {
            OutlinedTextField(
                value = stato.testo,
                onValueChange = { stato.testo = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                enabled = stato.esito == null,
                label = { Text("La tua risposta") },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { verifica(esercizio, stato) })
            )
        }

        Spacer(Modifier.height(14.dp))
        if (stato.esito == null) {
            Button(
                onClick = { verifica(esercizio, stato) },
                enabled = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                    stato.scelta != null
                } else {
                    stato.testo.isNotBlank()
                }
            ) {
                Text("Verifica")
            }
        } else {
            val corretto = stato.esito == true
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = if (corretto) MaterialTheme.colorScheme.tertiaryContainer
                    else MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(Modifier.fillMaxWidth().padding(14.dp)) {
                    Text(
                        if (corretto) "Corretto" else "Non corretto",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    if (!corretto) {
                        val attesa = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                            esercizio.opzioni.getOrNull(esercizio.rispostaCorretta).orEmpty()
                        } else {
                            esercizio.rispostaTesto
                        }
                        if (attesa.isNotBlank()) {
                            Text(
                                "Risposta attesa: $attesa",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    if (esercizio.spiegazione.isNotBlank()) {
                        Spacer(Modifier.height(4.dp))
                        Text(
                            esercizio.spiegazione,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
            Spacer(Modifier.height(10.dp))
            Button(onClick = {
                val correttoOra = stato.esito == true
                val nuoveCorrette = stato.corrette + if (correttoOra) 1 else 0
                if (stato.indice + 1 >= totale) {
                    stato.corrette = nuoveCorrette
                    stato.finito = true
                    store.salvaSessione(
                        unitaId = unita.id,
                        corrette = nuoveCorrette,
                        totali = totale,
                        rotazione = stato.sessione.rotazione
                    )
                } else {
                    stato.corrette = nuoveCorrette
                    stato.indice += 1
                    stato.scelta = null
                    stato.testo = ""
                    stato.esito = null
                }
            }) {
                Text(if (stato.indice + 1 >= totale) "Vedi risultato" else "Avanti")
            }
        }
    }
}

private fun verifica(esercizio: EsercizioUnita, stato: StatoSessione) {
    stato.esito = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
        stato.scelta == esercizio.rispostaCorretta
    } else {
        GrammaticaSyllabus.rispostaTestoCorretta(esercizio, stato.testo)
    }
}

@Composable
private fun RisultatoSessione(
    corrette: Int,
    totali: Int,
    onRipeti: () -> Unit,
    onTornaPercorso: () -> Unit
) {
    val percentuale = if (totali == 0) 0 else corrette * 100 / totali
    val promosso = percentuale >= GrammaticaSyllabus.SOGLIA_PADRONANZA
    CardSezione(
        titolo = if (promosso) "Unità superata" else "Rivediamo la regola",
        sottotitolo = "Soglia di padronanza: ${GrammaticaSyllabus.SOGLIA_PADRONANZA}%"
    ) {
        Text(
            "$corrette risposte corrette su $totali ($percentuale%)",
            style = MaterialTheme.typography.headlineSmall,
            color = if (promosso) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.error
        )
        Spacer(Modifier.height(8.dp))
        Text(
            if (promosso) {
                "L'unità è stata completata: tornerà nel ripasso tra 1, 3, 7 o 16 giorni."
            } else {
                "Rileggi la regola e riprova: al ${GrammaticaSyllabus.SOGLIA_PADRONANZA}% l'unità risulta completata."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Button(onClick = onRipeti) { Text("Ripeti la sessione") }
            OutlinedButton(onClick = onTornaPercorso) { Text("Torna al percorso") }
        }
    }
}

private fun tipoEsercizio(tipo: TipoEsercizioGrammatica): String = when (tipo) {
    TipoEsercizioGrammatica.SCELTA_MULTIPLA -> "Scelta multipla"
    TipoEsercizioGrammatica.COMPLETAMENTO -> "Completamento"
    TipoEsercizioGrammatica.TRASFORMAZIONE -> "Trasformazione"
    TipoEsercizioGrammatica.PRODUZIONE -> "Produzione"
}
