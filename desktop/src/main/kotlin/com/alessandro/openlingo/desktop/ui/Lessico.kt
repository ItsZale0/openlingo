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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.openlingo.data.EserciziLessico
import com.alessandro.openlingo.data.EsercizioLessico
import com.alessandro.openlingo.data.LessicoLivelli
import com.alessandro.openlingo.data.TipoEsercizioLessico
import com.alessandro.openlingo.desktop.DesktopStore
import com.alessandro.openlingo.i18n.t

/**
 * Lessico per livello (A1-B2) su desktop: stessa base condivisa dell'app
 * ([LessicoLivelli]) e stesso motore di esercizi ([EserciziLessico]).
 *
 * Una scheda per livello con quante parole sono già nel profilo e due azioni:
 * importare il livello nel ripasso oppure allenarsi. Gli esercizi di ascolto
 * sono esclusi su desktop (non c'è sintesi vocale): restano gli altri cinque
 * tipi, incluso il richiamo attivo "scrivi in tedesco".
 */
@Composable
fun SchermataLessico(store: DesktopStore) {
    store.repo

    var livello by remember { mutableStateOf("A1") }
    var ricerca by remember { mutableStateOf("") }
    var messaggio by remember { mutableStateOf("") }

    var sessione by remember { mutableStateOf<List<EsercizioLessico>>(emptyList()) }
    var indice by remember { mutableStateOf(0) }
    var mostrata by remember { mutableStateOf(false) }
    var corretta by remember { mutableStateOf(false) }
    var risposta by remember { mutableStateOf("") }
    var giuste by remember { mutableStateOf(0) }
    var sbagliate by remember { mutableStateOf<List<String>>(emptyList()) }

    fun avviaEsercizi() {
        if (store.paroleDelLivello(livello).size < 4) store.aggiungiLessico(livello)
        sessione = EserciziLessico
            .costruisci(store.paroleDelLivello(livello), 14, store.stato.parole)
            .filter { it.tipo != TipoEsercizioLessico.ASCOLTO }
            .take(12)
        indice = 0
        mostrata = false
        corretta = false
        risposta = ""
        giuste = 0
        sbagliate = emptyList()
        messaggio = if (sessione.isEmpty()) t("Nessuna parola disponibile per $livello.") else ""
    }

    // ------------------------------------------------------------- esercizi
    if (sessione.isNotEmpty()) {
        val esercizio = sessione.getOrNull(indice)
        Pagina {
            Testata(
                titolo = t("Lessico $livello"),
                sottotitolo = if (esercizio == null) t("Sessione completata")
                else t("Esercizio") + " ${indice + 1} " + t("di") + " ${sessione.size} · " + t(esercizio.tipo.etichetta)
            )

            if (esercizio == null) {
                CardSezione(
                    titolo = t("Sessione completata"),
                    sottotitolo = "$giuste " + t("corrette su") + " ${sessione.size}"
                ) {
                    Text(
                        t("Le parole giuste tornano più avanti, quelle sbagliate tornano domani."),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (sbagliate.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        Text(
                            t("Da rivedere: ") + sbagliate.distinct().take(8).joinToString(", "),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(onClick = { avviaEsercizi() }) { Text(t("Nuovo giro")) }
                        OutlinedButton(onClick = { sessione = emptyList() }) { Text(t("Torna al lessico")) }
                    }
                }
                return@Pagina
            }

            BarraLineare(
                valore = indice.toFloat() / sessione.size,
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                    Text(
                        t(esercizio.domanda),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (mostrata) {
                        Spacer(Modifier.height(10.dp))
                        Text(
                            if (corretta) t("Giusto") else t("Non è questa"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (corretta) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.error
                        )
                        Text(
                            t("Risposta corretta") + ": ${esercizio.corretta}",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (esercizio.spiegazione.isNotBlank()) {
                            Text(
                                t(esercizio.spiegazione),
                                style = MaterialTheme.typography.bodySmall,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (!mostrata && esercizio.aScelta) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    esercizio.opzioni.forEach { opzione ->
                        OutlinedButton(
                            onClick = {
                                risposta = opzione
                                corretta = opzione.trim() == esercizio.corretta.trim()
                                mostrata = true
                                if (corretta) giuste += 1
                                else sbagliate = sbagliate + EserciziLessico.tedescoConArticolo(esercizio.parola)
                                store.rispondiVerifica(esercizio.parola.id, corretta)
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(t(opzione)) }
                    }
                }
            } else if (!mostrata) {
                OutlinedTextField(
                    value = risposta,
                    onValueChange = { risposta = it },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text(t("La tua risposta in tedesco")) }
                )
                Spacer(Modifier.height(10.dp))
                Button(
                    onClick = {
                        corretta = EserciziLessico.rispostaScrittaCorretta(esercizio, risposta)
                        mostrata = true
                        if (corretta) giuste += 1
                        else sbagliate = sbagliate + EserciziLessico.tedescoConArticolo(esercizio.parola)
                        store.rispondiVerifica(esercizio.parola.id, corretta)
                    },
                    enabled = risposta.isNotBlank()
                ) { Text("Controlla") }
            } else {
                Button(onClick = {
                    indice += 1
                    mostrata = false
                    corretta = false
                    risposta = ""
                }) {
                    Text(if (indice + 1 >= sessione.size) t("Vedi il risultato") else t("Avanti"))
                }
            }
        }
        return
    }

    // ---------------------------------------------------------------- elenco
    val voci = remember(livello, ricerca) {
        val tutte = LessicoLivelli.perLivello(livello)
        val q = ricerca.trim()
        if (q.isBlank()) tutte else tutte.filter {
            it.tedesco.contains(q, ignoreCase = true) || it.italiano.contains(q, ignoreCase = true)
        }
    }
    val nelProfilo = voci.count { store.giaNelLessico(it) }
    val curateLivello = LessicoLivelli.conteggio(livello)
    val coperte = LessicoLivelli.cumulativo(livello)
    val obiettivo = LessicoLivelli.targetCumulativo(livello)

    Pagina {
        Testata(
            titolo = t("Lessico A1-B2"),
            sottotitolo = t("${LessicoLivelli.tutte.size} parole curate · copertura fino a $livello: ${"%,d".format(coperte)} di ${"%,d".format(obiettivo)} parole")
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            LessicoLivelli.LIVELLI.forEach { lv ->
                FilterChip(
                    selected = livello == lv,
                    onClick = { livello = lv },
                    label = { Text("$lv (${LessicoLivelli.conteggio(lv)})") }
                )
            }
        }

        CardSezione(
            titolo = t("Livello $livello"),
            sottotitolo = if (LessicoLivelli.temi(livello).isNotEmpty())
                t("Temi: ") + LessicoLivelli.temi(livello).take(8).joinToString(" · ")
            else null
        ) {
            Text(
                t("$curateLivello parole nuove di $livello · $nelProfilo già nel tuo lessico. I traguardi sono cumulativi: A1 650, A2 1.300, B1 2.400, B2 3.000."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                t("Copertura fino a $livello: ${"%,d".format(coperte)} di ${"%,d".format(obiettivo)} parole"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            BarraLineare(
                valore = (coperte.toFloat() / obiettivo).coerceIn(0f, 1f),
                modifier = Modifier.fillMaxWidth().height(6.dp)
            )
            Spacer(Modifier.height(10.dp))
            Text(
                t("Importa il livello per portare le parole nel ripasso."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedButton(onClick = {
                    val aggiunte = store.aggiungiLessico(livello)
                    messaggio = if (aggiunte > 0) t("Aggiunte $aggiunte parole $livello al ripasso.")
                    else t("Le parole $livello sono già nel tuo lessico.")
                }) { Text(t("Aggiungi al ripasso")) }
                Button(onClick = { avviaEsercizi() }) {
                    Icon(Icons.Filled.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(6.dp))
                    Text(t("Esercizi"))
                }
            }
            if (messaggio.isNotBlank()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    messaggio,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        OutlinedTextField(
            value = ricerca,
            onValueChange = { ricerca = it },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
            label = { Text(t("Cerca in tedesco o in italiano")) }
        )

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surfaceContainer
        ) {
            if (voci.isEmpty()) {
                Text(
                    t("Nessuna parola per questa ricerca."),
                    modifier = Modifier.padding(22.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                LazyColumn(modifier = Modifier.fillMaxWidth().height(520.dp).padding(8.dp)) {
                    items(voci, key = { it.livello + "|" + it.tedesco }) { voce ->
                        val presente = store.giaNelLessico(voce)
                        Row(
                            modifier = Modifier.fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    voce.tedesco,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    t(voce.italiano),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(
                                t(voce.tema),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(Modifier.width(12.dp))
                            if (presente) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = t("Già nel lessico"),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
