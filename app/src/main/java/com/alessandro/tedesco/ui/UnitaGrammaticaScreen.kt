@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.animation.animateContentSize
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.EsempioGrammatica
import com.alessandro.tedesco.data.EsercizioUnita
import com.alessandro.tedesco.data.GrammaticaSyllabus
import com.alessandro.tedesco.data.PercorsoLezioni
import com.alessandro.tedesco.data.local.RotazioneUnita
import com.alessandro.tedesco.data.SessioneLezione
import com.alessandro.tedesco.data.TabellaGrammatica
import com.alessandro.tedesco.data.TipoEsercizioGrammatica
import com.alessandro.tedesco.data.UnitaGrammatica
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Una unità del percorso grammaticale.
 *
 * Flusso: teoria -> esercizi -> verifica di padronanza.
 * Il risultato viene salvato una sola volta, alla fine della sessione.
 */
@Composable
fun UnitaGrammaticaScreen(
    vm: TedescoViewModel,
    unitaId: String,
    onIndietro: () -> Unit,
    onCompletata: () -> Unit
) {
    val unita = remember(unitaId) { GrammaticaSyllabus.perId(unitaId) }
    if (unita == null) {
        ScreenScaffold(title = t("Unità non trovata"), onBack = onIndietro) {
            Text(t("Questa unità non esiste o non è più disponibile."))
        }
        return
    }

    val context = LocalContext.current
    val tts = rememberTtsHelper(context)

    var scheda by rememberSaveable(unitaId) { mutableStateOf(0) }
    var indice by rememberSaveable(unitaId) { mutableStateOf(0) }
    var corrette by rememberSaveable(unitaId) { mutableStateOf(0) }
    var scelta by rememberSaveable(unitaId) { mutableStateOf(-1) }
    var testo by rememberSaveable(unitaId) { mutableStateOf("") }
    var verificato by rememberSaveable(unitaId) { mutableStateOf(false) }
    var finito by rememberSaveable(unitaId) { mutableStateOf(false) }
    var salvato by rememberSaveable(unitaId) { mutableStateOf(false) }
    // Seme della sessione: nuovo a ogni apertura della schermata e a ogni
    // t("Riprova"), così le domande cambiano davvero da un tentativo all'altro.
    var seme by rememberSaveable(unitaId) { mutableStateOf(System.currentTimeMillis()) }
    var tentativi by rememberSaveable(unitaId) { mutableStateOf(0) }
    var errori by rememberSaveable(unitaId) { mutableStateOf(listOf<ErroreRisposta>()) }

    // Banco della lezione: esercizi scritti + quelli generati dall'AI.
    val rotazioni by vm.rotazioniEsercizi.collectAsStateWithLifecycle()
    val eserciziAi by vm.eserciziUnitaAi.collectAsStateWithLifecycle()
    val banco = remember(unita.id, eserciziAi[unita.id]) {
        SessioneLezione.banco(unita, eserciziAi[unita.id].orEmpty())
    }
    // Sessione: quella salvata (ripresa identica se si riapre la lezione),
    // altrimenti una nuova che NON contiene gli esercizi del tentativo prima.
    val sessioneSalvata = rotazioni[unita.id]?.sessione
    val chiaveRotazione = if (tentativi == 0) sessioneSalvata else null
    val sessione = remember(unita.id, tentativi, chiaveRotazione) {
        val rotazione = rotazioni[unita.id] ?: RotazioneUnita()
        if (tentativi == 0) {
            SessioneLezione.sessione(banco, rotazione, seme.toInt())
        } else {
            SessioneLezione.nuovaSessione(banco, rotazione, seme.toInt())
        }
    }
    val eserciziSessione = sessione.esercizi

    // Salva la sessione in corso: così riaprendo la lezione si riprende la
    // stessa sequenza invece di ricominciare da capo.
    LaunchedEffect(sessione.rotazione) {
        vm.aggiornaRotazione(unita.id, sessione.rotazione)
    }
    // Esercizi nuovi generati dall'AI: solo quando si fanno davvero gli
    // esercizi (non mentre si legge la teoria) e mai bloccante.
    LaunchedEffect(scheda, tentativi) {
        if (scheda == 1) vm.generaEserciziUnita(unita.id)
    }

    val esercizio = eserciziSessione.getOrNull(indice)
    val percentuale = if (eserciziSessione.isEmpty()) 0
    else corrette * 100 / eserciziSessione.size
    val promosso = percentuale >= GrammaticaSyllabus.SOGLIA_PADRONANZA

    fun resetPratica(nuovaSessione: Boolean = false) {
        indice = 0
        corrette = 0
        scelta = -1
        testo = ""
        verificato = false
        finito = false
        salvato = false
        errori = emptyList()
        if (nuovaSessione) {
            tentativi += 1
            seme = System.currentTimeMillis()
        }
    }

    fun rispostaData(e: EsercizioUnita): String =
        if (e.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            e.opzioni.getOrNull(scelta).orEmpty()
        } else {
            testo.trim()
        }

    fun verificaRisposta() {
        val e = esercizio ?: return
        val corretto = if (e.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
            GrammaticaSyllabus.rispostaSceltaCorretta(
                e,
                scelta.takeIf { it >= 0 }
            )
        } else {
            GrammaticaSyllabus.rispostaTestoCorretta(e, testo)
        }
        verificato = true
        if (corretto) {
            corrette += 1
        } else {
            errori = errori + ErroreRisposta(indice, rispostaData(e))
        }
    }

    fun prossimaDomanda() {
        if (indice < eserciziSessione.lastIndex) {
            indice += 1
            scelta = -1
            testo = ""
            verificato = false
        } else {
            finito = true
        }
    }

    LaunchedEffect(finito) {
        if (finito && !salvato) {
            vm.salvaPadronanzaGrammatica(
                unitaId = unita.id,
                corrette = corrette,
                totali = eserciziSessione.size
            )
            vm.completaPassoGiornaliero("grammatica")
            salvato = true
        }
    }

    ScreenScaffold(
        title = t("${unita.livello} - ${unita.titolo}"),
        onBack = onIndietro
    ) {
        IntestazioneUnita(unita)

        Spacer(Modifier.height(Spaziature.md))

        Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)) {
            FilterChip(
                selected = scheda == 0,
                onClick = { scheda = 0 },
                label = { Text(t("Teoria")) }
            )
            FilterChip(
                selected = scheda == 1,
                onClick = {
                    if (finito) resetPratica(nuovaSessione = true)
                    scheda = 1
                },
                label = { Text(t("Esercizi")) }
            )
            if (unita.teoria.esempi.isNotEmpty()) {
                FilterChip(
                    selected = scheda == 2,
                    onClick = { scheda = 2 },
                    label = { Text(t("Dettato")) }
                )
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        if (scheda == 0) {
            TeoriaUnita(
                unita = unita,
                onAscolta = { tts.speak(it) },
                onVaiEsercizi = {
                    if (finito) resetPratica(nuovaSessione = true)
                    scheda = 1
                }
            )
        } else if (scheda == 2) {
            DettatoUnita(
                unita = unita,
                onAscolta = { tts.speak(it) }
            )
        } else if (finito) {
            RisultatoUnita(
                unita = unita,
                esercizi = eserciziSessione,
                errori = errori,
                corrette = corrette,
                percentuale = percentuale,
                promosso = promosso,
                onRiprova = {
                    resetPratica(nuovaSessione = true)
                    scheda = 1
                },
                onTornaPercorso = onCompletata,
                onRivediTeoria = { scheda = 0 }
            )
        } else if (esercizio != null) {
            EsercizioUnitaCard(
                unita = unita,
                esercizio = esercizio,
                indice = indice,
                totale = eserciziSessione.size,
                scelta = scelta,
                testo = testo,
                verificato = verificato,
                onScelta = { scelta = it },
                onTesto = { testo = it },
                onVerifica = { verificaRisposta() },
                onAvanti = { prossimaDomanda() },
                onAscolta = { tts.speak(it) }
            )
        }

        // Spazio per non finire sotto la bolla del tutor.
        Spacer(Modifier.height(80.dp))
    }
}

@Composable
private fun IntestazioneUnita(unita: UnitaGrammatica) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(Spaziature.md),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(52.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        unita.livello,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
            Spacer(Modifier.width(Spaziature.sm))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    unita.titolo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    unita.sottotitolo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    unita.obiettivo,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    t("${unita.minuti} minuti - ${unita.esercizi.size} esercizi"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

@Composable
private fun TeoriaUnita(
    unita: UnitaGrammatica,
    onAscolta: (String) -> Unit,
    onVaiEsercizi: () -> Unit
) {
    Column {
        SezioneTitolo(t("Regola"), t("Leggi con calma: è il cuore della unità."))
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceContainer
            ),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(Spaziature.md)) {
                unita.teoria.regola.forEach { regola ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            "-",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.width(6.dp))
                        MarkdownText(
                            markdown = regola,
                            color = MaterialTheme.colorScheme.onSurface,
                            stile = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        unita.teoria.tabelle.forEach { tabella ->
            TabellaCard(tabella)
            Spacer(Modifier.height(Spaziature.md))
        }

        SezioneTitolo(t("Esempi"), t("Tocca l'altoparlante per ascoltare."))
        unita.teoria.esempi.forEach { esempio ->
            EsempioCard(esempio = esempio, onAscolta = { onAscolta(esempio.tedesco) })
            Spacer(Modifier.height(Spaziature.sm))
        }

        if (unita.teoria.erroriTipici.isNotEmpty()) {
            Spacer(Modifier.height(Spaziature.sm))
            SezioneTitolo(t("Errori tipici"), t("Gli errori più frequenti di chi parla italiano."))
            unita.teoria.erroriTipici.forEach { errore ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Column(Modifier.padding(Spaziature.sm)) {
                        Row(verticalAlignment = Alignment.Top) {
                            Text(
                                "X",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                errore.sbagliato,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        Row(verticalAlignment = Alignment.Top) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.width(6.dp))
                            Text(
                                errore.corretto,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Spacer(Modifier.height(2.dp))
                        Text(
                            errore.spiegazione,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        if (unita.teoria.merksatz.isNotBlank()) {
            Spacer(Modifier.height(Spaziature.md))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(Modifier.padding(Spaziature.md)) {
                    Text(
                        t("Merksatz"),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                    Text(
                        unita.teoria.merksatz,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onTertiaryContainer
                    )
                }
            }
        }

        // Regole che gli esercizi mettono alla prova ma che la teoria non dice
        // ancora. NON contiene le risposte: sarebbe un aiuto, non una lezione.
        // Collassabile: è una nota di contesto, non il cuore della lezione.
        val promemoria = remember(unita) { PercorsoLezioni.promemoriaEsercizi(unita) }
        if (promemoria.isNotEmpty()) {
            Spacer(Modifier.height(Spaziature.md))
            var promemoriaAperto by rememberSaveable { mutableStateOf(false) }
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainer
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(Modifier.padding(Spaziature.md).animateContentSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { promemoriaAperto = !promemoriaAperto },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text(
                                t("Promemoria"),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                t("Le regole che gli esercizi mettono alla prova, senza svelare le risposte."),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            imageVector = if (promemoriaAperto) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (promemoriaAperto) t("Chiudi") else t("Apri"),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (promemoriaAperto) {
                        Spacer(Modifier.height(Spaziature.sm))
                        promemoria.forEach { testo ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 3.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.width(6.dp))
                                Text(
                                    testo,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(Spaziature.lg))
        Button(
            onClick = onVaiEsercizi,
            modifier = Modifier
                .fillMaxWidth()
                .height(AltezzaBottonePrincipale())
        ) {
            Text(t("Passa agli esercizi"))
            Spacer(Modifier.width(Spaziature.sm))
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
        Spacer(Modifier.height(Spaziature.sm))
    }
}

@Composable
private fun TabellaCard(tabella: TabellaGrammatica) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.sm)) {
            Text(
                tabella.titolo,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))

            // Con 4+ colonne la tabella non entra su un telefono: meglio
            // scorrere in orizzontale che spezzare t("Nominativo") in due righe.
            val molteColonne = tabella.intestazioni.size >= 4
            if (molteColonne) {
                val scroll = rememberScrollState()
                Column(Modifier.horizontalScroll(scroll)) {
                    TabellaRiga(
                        celle = tabella.intestazioni,
                        intestazione = true,
                        larghezzeFisse = true
                    )
                    HorizontalDivider(
                        modifier = Modifier.padding(vertical = 4.dp),
                        color = MaterialTheme.colorScheme.outlineVariant
                    )
                    tabella.righe.forEach { riga ->
                        TabellaRiga(
                            celle = riga,
                            intestazione = false,
                            larghezzeFisse = true
                        )
                    }
                }
            } else {
                TabellaRiga(
                    celle = tabella.intestazioni,
                    intestazione = true,
                    larghezzeFisse = false
                )
                HorizontalDivider(
                    modifier = Modifier.padding(vertical = 4.dp),
                    color = MaterialTheme.colorScheme.outlineVariant
                )
                tabella.righe.forEach { riga ->
                    TabellaRiga(
                        celle = riga,
                        intestazione = false,
                        larghezzeFisse = false
                    )
                }
            }

            if (tabella.nota.isNotBlank()) {
                Spacer(Modifier.height(4.dp))
                Text(
                    tabella.nota,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TabellaRiga(
    celle: List<String>,
    intestazione: Boolean,
    larghezzeFisse: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        celle.forEachIndexed { indice, cella ->
            val modifier = if (larghezzeFisse) {
                Modifier.width(if (indice == 0) 112.dp else 92.dp)
            } else {
                Modifier.weight(1f)
            }
            Text(
                cella,
                modifier = modifier.padding(horizontal = 2.dp),
                style = if (intestazione) {
                    MaterialTheme.typography.labelSmall
                } else {
                    MaterialTheme.typography.bodySmall
                },
                fontWeight = if (intestazione) FontWeight.Bold else FontWeight.Normal,
                color = if (intestazione) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

@Composable
private fun EsempioCard(
    esempio: EsempioGrammatica,
    onAscolta: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = Spaziature.md, top = Spaziature.sm, bottom = Spaziature.sm),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    esempio.tedesco,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    esempio.italiano,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (esempio.nota.isNotBlank()) {
                    Text(
                        esempio.nota,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            IconButton(onClick = onAscolta) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = t("Ascolta l'esempio"),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
internal fun EsercizioUnitaCard(
    unita: UnitaGrammatica,
    esercizio: EsercizioUnita,
    indice: Int,
    totale: Int,
    scelta: Int,
    testo: String,
    verificato: Boolean,
    onScelta: (Int) -> Unit,
    onTesto: (String) -> Unit,
    onVerifica: () -> Unit,
    onAvanti: () -> Unit,
    onAscolta: (String) -> Unit
) {
    val rispostaCorretta = when (esercizio.tipo) {
        TipoEsercizioGrammatica.SCELTA_MULTIPLA ->
            esercizio.opzioni.getOrNull(esercizio.rispostaCorretta).orEmpty()

        else -> esercizio.rispostaTesto
    }
    val corretto = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
        GrammaticaSyllabus.rispostaSceltaCorretta(
            esercizio,
            scelta.takeIf { it >= 0 }
        )
    } else {
        GrammaticaSyllabus.rispostaTestoCorretta(esercizio, testo)
    }
    val etichettaInput = when (esercizio.tipo) {
        TipoEsercizioGrammatica.COMPLETAMENTO -> t("Scrivi la parola mancante")
        TipoEsercizioGrammatica.TRASFORMAZIONE -> t("Scrivi la frase trasformata")
        TipoEsercizioGrammatica.PRODUZIONE -> t("Scrivi la traduzione")
        TipoEsercizioGrammatica.SCELTA_MULTIPLA -> ""
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    t("Esercizio ${indice + 1} di $totale"),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.weight(1f)
                )
                Text(
                    t("${unita.livello} - ${unita.titolo}"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(4.dp))
            LinearProgressIndicator(
                progress = { (indice + if (verificato) 1 else 0).toFloat() / totale.coerceAtLeast(1) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
            )
            Spacer(Modifier.height(Spaziature.md))
            Text(
                esercizio.domanda,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            if (esercizio.suggerimento.isNotBlank() && !verificato) {
                Spacer(Modifier.height(2.dp))
                Text(
                    esercizio.suggerimento,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(Modifier.height(Spaziature.sm))

            if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                esercizio.opzioni.forEachIndexed { i, opzione ->
                    val selezionato = scelta == i
                    val corretta = i == esercizio.rispostaCorretta
                    val colore = when {
                        verificato && corretta -> MaterialTheme.colorScheme.primaryContainer
                        verificato && selezionato -> MaterialTheme.colorScheme.errorContainer
                        selezionato -> MaterialTheme.colorScheme.secondaryContainer
                        else -> MaterialTheme.colorScheme.surface
                    }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clickable(enabled = !verificato) { onScelta(i) },
                        shape = MaterialTheme.shapes.large,
                        colors = CardDefaults.cardColors(containerColor = colore),
                        border = BorderStroke(
                            if (selezionato && !verificato) 2.dp else 1.dp,
                            if (selezionato && !verificato) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant
                            }
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(Spaziature.sm),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (selezionato) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.surfaceContainer
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(
                                        "ABCD".getOrElse(i) { '?' }.toString(),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (selezionato) {
                                            MaterialTheme.colorScheme.onPrimary
                                        } else {
                                            MaterialTheme.colorScheme.onSurfaceVariant
                                        }
                                    )
                                }
                            }
                            Spacer(Modifier.width(Spaziature.sm))
                            Text(
                                opzione,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { onAscolta(opzione) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = t("Ascolta l'opzione"),
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            if (verificato && corretta) {
                                Icon(
                                    Icons.Filled.Check,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            } else {
                OutlinedTextField(
                    value = testo,
                    onValueChange = onTesto,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(etichettaInput) },
                    enabled = !verificato,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.None,
                        autoCorrectEnabled = false
                    ),
                    singleLine = false,
                    minLines = 1,
                    maxLines = 3
                )
            }

            if (verificato) {
                Spacer(Modifier.height(Spaziature.sm))
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateContentSize(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (corretto) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(Modifier.padding(Spaziature.sm)) {
                        Text(
                            if (corretto) t("Corretto!") else t("Non corretto"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        if (corretto) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                t("Ottimo lavoro!"),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (!corretto) {
                            Spacer(Modifier.height(2.dp))
                            Text(
                                t("Risposta corretta: $rispostaCorretta"),
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        if (esercizio.spiegazione.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(
                                esercizio.spiegazione,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { onAscolta(rispostaCorretta) }) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(Modifier.width(4.dp))
                            Text(t("Ascolta la risposta"))
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spaziature.md))

            if (!verificato) {
                Button(
                    onClick = onVerifica,
                    enabled = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                        scelta >= 0
                    } else {
                        testo.isNotBlank()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AltezzaBottonePrincipale())
                ) {
                    Text(t("Verifica risposta"))
                }
            } else {
                Button(
                    onClick = onAvanti,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AltezzaBottonePrincipale())
                ) {
                    Text(
                        if (indice < totale - 1) t("Prossima domanda")
                        else t("Vedi risultato")
                    )
                }
            }
        }
    }
}

/**
 * Dettato breve: il TTS legge una frase della unità, l'utente la scrive.
 * Non modifica la padronanza: è un esercizio di ascolto e ortografia.
 */
@Composable
private fun DettatoUnita(
    unita: UnitaGrammatica,
    onAscolta: (String) -> Unit
) {
    val esempi = remember(unita.id) {
        unita.teoria.esempi
            .map { it.tedesco }
            .filter { it.length in 8..110 }
            .ifEmpty { unita.teoria.esempi.map { it.tedesco } }
    }
    if (esempi.isEmpty()) {
        Text(t("Questa unità non ha ancora esempi da dettare."))
        return
    }

    var indice by rememberSaveable(unita.id) { mutableStateOf(0) }
    var testo by rememberSaveable(unita.id) { mutableStateOf("") }
    var verificato by rememberSaveable(unita.id) { mutableStateOf(false) }
    val frase = esempi[indice % esempi.size]
    // Accetta anche la scrittura senza umlaut/ß, come nel resto degli esercizi.
    val corretta = GrammaticaSyllabus.traslittera(testo) ==
        GrammaticaSyllabus.traslittera(frase)

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Text(
                t("Dettato ${indice + 1} di ${esempi.size}"),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                t("Ascolta la frase e scrivila in tedesco."),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Puoi riascoltare quante volte vuoi. Non contano maiuscole e punteggiatura.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.sm))
            Button(
                onClick = { onAscolta(frase) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(Spaziature.xs))
                Text(t("Ascolta la frase"))
            }
            Spacer(Modifier.height(Spaziature.sm))
            OutlinedTextField(
                value = testo,
                onValueChange = { testo = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(t("Scrivi ciò che senti")) },
                enabled = !verificato,
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.None,
                    autoCorrectEnabled = false
                ),
                singleLine = false,
                minLines = 2,
                maxLines = 4
            )
            if (verificato) {
                Spacer(Modifier.height(Spaziature.sm))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (corretta) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Column(Modifier.padding(Spaziature.sm)) {
                        Text(
                            if (corretta) t("Dettato corretto!") else t("Riascolta e confronta"),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(2.dp))
                        Text(
                            frase,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
            Spacer(Modifier.height(Spaziature.md))
            if (!verificato) {
                Button(
                    onClick = { verificato = true },
                    enabled = testo.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AltezzaBottonePrincipale())
                ) {
                    Text(t("Verifica dettato"))
                }
            } else {
                Button(
                    onClick = {
                        indice = (indice + 1) % esempi.size
                        testo = ""
                        verificato = false
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(AltezzaBottonePrincipale())
                ) {
                    Text(t("Prossima frase"))
                }
            }
        }
    }
}

/** Una risposta sbagliata, tenuta per il riepilogo finale. */
internal data class ErroreRisposta(
    val posizione: Int,
    val rispostaData: String
) : java.io.Serializable

/**
 * Riepilogo degli errori: per ogni sbaglio mostra la domanda, la risposta data,
 * quella corretta e la spiegazione. È il punto in cui lo studente capisce
 * l'errore invece di limitarsi a rifare la serie fino all'80%.
 */
@Composable
internal fun RiepilogoErrori(
    esercizi: List<EsercizioUnita>,
    errori: List<ErroreRisposta>
) {
    if (errori.isEmpty()) return
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.4f))
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Text(
                t("Dove hai sbagliato"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
                t("Rileggi questi punti prima di riprovare: sono quelli in cui la regola non è ancora chiara."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            errori.forEach { errore ->
                val esercizio = esercizi.getOrNull(errore.posizione) ?: return@forEach
                val corretta = if (esercizio.tipo == TipoEsercizioGrammatica.SCELTA_MULTIPLA) {
                    esercizio.opzioni.getOrNull(esercizio.rispostaCorretta).orEmpty()
                } else {
                    esercizio.rispostaTesto
                }
                Spacer(Modifier.height(Spaziature.sm))
                HorizontalDivider(color = MaterialTheme.colorScheme.error.copy(alpha = 0.25f))
                Spacer(Modifier.height(Spaziature.sm))
                Text(
                    t("${errore.posizione + 1}. ${esercizio.domanda}"),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    "La tua risposta: ${errore.rispostaData.ifBlank { "nessuna" }}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
                Text(
                    t("Risposta corretta: $corretta"),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
                if (esercizio.spiegazione.isNotBlank()) {
                    Spacer(Modifier.height(2.dp))
                    Text(
                        esercizio.spiegazione,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RisultatoUnita(
    unita: UnitaGrammatica,
    esercizi: List<EsercizioUnita>,
    errori: List<ErroreRisposta>,
    corrette: Int,
    percentuale: Int,
    promosso: Boolean,
    onRiprova: () -> Unit,
    onTornaPercorso: () -> Unit,
    onRivediTeoria: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (promosso) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.secondaryContainer
            }
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spaziature.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                if (promosso) t("Unità completata") else t("Ripassa ancora"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                t("$corrette / ${esercizi.size}"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                t("$percentuale% di risposte corrette"),
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                if (promosso) {
                    "Hai raggiunto la soglia dell'80%. La prossima unità è sbloccata; il ripasso tornera' tra 1, 3, 7 e 16 giorni."
                } else {
                    t("Serve almeno l'80% per sbloccare la unità successiva. Rileggi la regola e riprova: gli errori sono la parte più utile.")
                },
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.md))
            Button(
                onClick = onTornaPercorso,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale())
            ) {
                Text(t("Torna al percorso"))
            }
            Spacer(Modifier.height(Spaziature.sm))
            Button(
                onClick = onRiprova,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(AltezzaBottonePrincipale())
            ) {
                Text(t("Riprova con esercizi nuovi"))
            }
            TextButton(onClick = onRivediTeoria) {
                Text(t("Rivedi la teoria"))
            }
        }
    }

    Spacer(Modifier.height(Spaziature.md))
    RiepilogoErrori(
        esercizi = esercizi,
        errori = errori
    )

    Spacer(Modifier.height(Spaziature.md))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(Modifier.padding(Spaziature.md)) {
            Text(
                t("Cosa porti via"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(Spaziature.xs))
            Text(
                unita.teoria.merksatz,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(Spaziature.xs))
            Text(
                t("Ripasso programmato: 1, 3, 7 e 16 giorni. La unità resta accessibile anche quando è completata."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SezioneTitolo(titolo: String, sottotitolo: String = "") {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            titolo,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (sottotitolo.isNotBlank()) {
            Text(
                sottotitolo,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
