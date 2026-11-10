package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.GenderTrainer
import com.alessandro.tedesco.data.local.LivelloCEFR
import com.alessandro.tedesco.i18n.t
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

@Composable
fun GenderTrainerScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val parole by vm.parole.collectAsStateWithLifecycle(emptyList())

    val livelloProfilo = profilo?.stato?.progresso?.livelloCorrente ?: LivelloCEFR.A1
    val livelli = listOf(LivelloCEFR.A1, LivelloCEFR.A2, LivelloCEFR.B1, LivelloCEFR.B2)
        .filter { it.ordine <= LivelloCEFR.B2.ordine }

    var livelloSelezionato by remember(livelloProfilo) {
        mutableStateOf(
            livelli.firstOrNull { it == livelloProfilo } ?: LivelloCEFR.A1
        )
    }
    var sessioneId by remember { mutableIntStateOf(0) }
    var domande by remember { mutableStateOf<List<GenderTrainer.Domanda>>(emptyList()) }
    var indice by remember { mutableIntStateOf(0) }
    var scelta by remember { mutableStateOf<String?>(null) }
    var corrette by remember { mutableIntStateOf(0) }
    var risposte by remember { mutableIntStateOf(0) }

    LaunchedEffect(sessioneId, livelloSelezionato, parole.size) {
        val miste = GenderTrainer.domandeMiste(parole, livelloSelezionato, limite = null)
        domande = if (miste.isNotEmpty()) miste else GenderTrainer.domandeCurate(livelloSelezionato, limite = null)
        indice = 0
        scelta = null
        corrette = 0
        risposte = 0
    }

    ScreenScaffold(t("Allenatore articoli"), onBack = onIndietro) {
        Column(
            modifier = Modifier.widthIn(max = dimensioneContenuto()),
            verticalArrangement = Arrangement.spacedBy(spaziaturaSchermo())
        ) {
            Text(
                t("Scegli l'articolo giusto (der, die, das) per le parole del tuo lessico, in base al livello."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                livelli.forEach { lv ->
                    FilterChip(
                        selected = livelloSelezionato == lv,
                        onClick = { livelloSelezionato = lv; sessioneId++ },
                        label = { Text(lv.codiceBreve()) }
                    )
                }
            }

            if (domande.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            t("Nessuna parola con articolo disponibile per questo livello."),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            t("Aggiungi parole al lessico o importa la Wortliste/Lessico, poi riprova."),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        OutlinedButton(onClick = { sessioneId++ }) { Text(t("Riprova")) }
                    }
                }
                return@Column
            }

            val finito = indice >= domande.size
            if (finito) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Column(
                        Modifier.fillMaxWidth().padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(t("Sessione completata"), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("$corrette / $risposte " + t("corrette"), style = MaterialTheme.typography.headlineSmall)
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { sessioneId++ }) { Text(t("Nuova sessione")) }
                    }
                }
                return@Column
            }

            val domanda = domande[indice]
            val rispostaData = scelta != null
            val correttaRisposta = scelta == domanda.articolo

            Text(
                t("Parola") + " ${indice + 1}/${domande.size}" +
                    if (domanda.livello.isNotBlank()) " · ${domanda.livello}" else "",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
            ) {
                Column(
                    Modifier.fillMaxWidth().padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        domanda.parola,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    if (domanda.traduzione.isNotBlank()) {
                        Text(
                            t(domanda.traduzione),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                listOf("der", "die", "das").forEach { art ->
                    val colore = when {
                        !rispostaData -> MaterialTheme.colorScheme.secondaryContainer
                        art == domanda.articolo -> MaterialTheme.colorScheme.tertiaryContainer
                        art == scelta -> MaterialTheme.colorScheme.errorContainer
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    Button(
                        onClick = {
                            if (scelta != null) return@Button
                            scelta = art
                            risposte++
                            if (art == domanda.articolo) corrette++
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colore,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        ),
                        enabled = true
                    ) {
                        Text(art, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (rispostaData) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = if (correttaRisposta) MaterialTheme.colorScheme.tertiaryContainer
                        else MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            if (correttaRisposta) t("Corretto!") else t("Non corretto"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            t("Risposta corretta") + ": ${domanda.articolo} ${domanda.parola}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        if (domanda.esempio.isNotBlank()) {
                            Text(
                                domanda.esempio,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(onClick = {
                        indice++
                        scelta = null
                    }) {
                        Text(if (indice + 1 >= domande.size) t("Vedi risultato") else t("Prossima"))
                    }
                }
            }

            if (!rispostaData) {
                Text(
                    "$corrette " + t("corrette") + " · ${risposte} " + t("risposte"),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
