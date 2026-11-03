package com.alessandro.tedesco.ui

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.Canvas
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.CalcoloProgresso
import com.alessandro.tedesco.data.CertificazioniUfficiali
import com.alessandro.tedesco.data.ErroreEntity
import com.alessandro.tedesco.data.ReportInsegnante
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

/**
 * Progresso ricostruito da zero: UNA schermata, UNA fonte (CalcoloProgresso),
 * solo dati reali. Niente carte sovrapposte, niente KPI fantasma.
 *
 * Struttura:
 *  1. Livello e obiettivo
 *  2. Numeri chiave (parole, verificate, ripassi, accuratezza, streak, minuti)
 *  3. Cosa fare adesso (dimensione piu' debole)
 *  4. Le 4 competenze (barre con soglia ufficiale 60%)
 *  5. Produzione scritta (testi, media, trend, evoluzione)
 *  6. Traguardi
 */
@Composable
fun ProgressoScreen(vm: TedescoViewModel, onIndietro: (() -> Unit)? = null) {
    val p by vm.progressoReale.collectAsStateWithLifecycle(null)
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val errori by vm.errori.collectAsStateWithLifecycle(emptyList())

    ScreenScaffold("Progresso", onBack = onIndietro) {
        val pr = p
        if (pr == null) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    "Caricamento…",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            return@ScreenScaffold
        }

        Column(
            modifier = Modifier.widthIn(max = dimensioneContenuto()).animateContentSize(),
            verticalArrangement = Arrangement.spacedBy(spaziaturaSchermo())
        ) {
            // ---- Messaggio motivante ----
            val messaggio = when {
                pr.streakGiorni >= 30 -> "Un mese di costanza. Sei un esempio!"
                pr.streakGiorni >= 14 -> "Due settimane di fila. Il tedesco diventa naturale."
                pr.streakGiorni >= 7 -> "Una settimana intera! Continua così."
                pr.streakGiorni >= 3 -> "Ottimo inizio. La costanza paga."
                pr.paroleImparate >= 100 -> "Hai imparato oltre 100 parole. Bravo!"
                else -> "Ogni passo conta. Continua a studiare!"
            }
            Text(
                messaggio,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            // ---- 1. Livello ----
            CardLivello(pr)

            // ---- 2. Numeri chiave ----
            CardNumeriChiave(pr)

            // ---- 3. Cosa fare adesso ----
            CardProssimoPasso(pr)

            // ---- 4. Competenze ----
            CardCompetenze(pr)

            // ---- 5. Produzione scritta ----
            CardProduzione(pr)

            // ---- 6. Traguardi ----
            CardTraguardi(pr)

            // ---- 7. Report per l'insegnante ----
            CardReportInsegnante(
                pr = pr,
                profilo = profilo,
                errori = errori,
                data = TedescoViewModel.formattaData(System.currentTimeMillis())
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
private fun CardLivello(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                "Il tuo livello",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                pr.livello.label,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                pr.livello.descrizione,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(8.dp))
            Text(
                when {
                    pr.streakGiorni >= 7 -> "🔥 ${pr.streakGiorni} giorni di fila. Sei inarrestabile!"
                    pr.streakGiorni >= 3 -> "🔥 ${pr.streakGiorni} giorni di fila. Continua così!"
                    pr.streakGiorni > 0 -> "🔥 ${pr.streakGiorni} giorno di fila. Ogni giorno conta."
                    else -> "Inizia oggi il tuo streak: basta un esercizio."
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            val prossimo = pr.prossimoLivello
            Spacer(Modifier.height(12.dp))
            if (prossimo != null) {
                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("Verso ${prossimo.label.substringBefore(" ")}", style = MaterialTheme.typography.bodyMedium)
                    Text(
                        "${(pr.versoProssimoLivello * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { pr.versoProssimoLivello },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    "Serve il 60% in TUTTE le 4 competenze (soglia ufficiale Goethe/telc/ÖSD)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    "Hai raggiunto il tuo obiettivo: ${pr.livelloObiettivo.label}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun CardNumeriChiave(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                "I tuoi numeri",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Tutto dati reali: niente stime, niente fantasia.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi("Parole", "${pr.paroleImparate}", "su ${pr.paroleDisponibili} disponibili",
                    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                Kpi("Verificate", "${pr.paroleImparate - pr.paroleFragili}", "dimostrate nel quiz",
                    MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi("Ripassi", "${pr.ripassiTotali}", "totali",
                    MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
                Kpi("Accuratezza", "${(pr.accuratezza * 100).toInt()}%", "risposte giuste",
                    MaterialTheme.colorScheme.primary, Modifier.weight(1f))
            }
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Kpi("Streak", "${pr.streakGiorni}g", "record ${pr.streakRecord}g",
                    MaterialTheme.colorScheme.tertiary, Modifier.weight(1f))
                Kpi("Studio", "${pr.minutiStudiati}m", "minuti stimati",
                    MaterialTheme.colorScheme.secondary, Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun Kpi(
    label: String,
    valore: String,
    sotto: String,
    colore: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                valore,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = colore
            )
            Text(
                sotto,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun CardProssimoPasso(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                "Cosa fare adesso",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Ti manca soprattutto: ${pr.dimensioneDebole} (${pr.dimensioneDeboleValore.toInt()}%)",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(2.dp))
            Text(
                pr.azioneConsigliata,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Composable
private fun CardCompetenze(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                "Le 4 competenze",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Il livello sale solo quando TUTTE superano il 60% (la più debole comanda)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            BarraCompetenza("Lessico", pr.competenze.lessico)
            BarraCompetenza("Grammatica", pr.competenze.grammatica)
            BarraCompetenza("Comprensione", pr.competenze.comprensione)
            BarraCompetenza("Produzione", pr.competenze.produzione)
        }
    }
}

@Composable
private fun BarraCompetenza(nome: String, valore: Float) {
    val v = valore.coerceIn(0f, 100f)
    val ok = v >= CertificazioniUfficiali.SOGLIA_SUPERAMENTO
    val colore = if (ok) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
    Column(Modifier.padding(vertical = 5.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nome, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = if (ok) "${v.toInt()}% ✓" else "${v.toInt()}%",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (ok) colore else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { v / 100f },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp),
            color = if (ok) colore else MaterialTheme.colorScheme.outline,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun CardProduzione(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Text(
                "Produzione scritta",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Testi liberi valutati sui 4 criteri ufficiali Goethe (Erfüllung, Kohärenz, Wortschatz, Strukturen)",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))

            if (pr.produzioniScritte == 0) {
                Text(
                    "Nessuna produzione scritta valutata. Scrivi un testo in Produzione Scritta " +
                        "e premi Verifica: il tutor lo valuta e il punteggio entra qui.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Kpi("Testi", "${pr.produzioniScritte}", "valutati",
                        MaterialTheme.colorScheme.primary, Modifier.weight(1f))
                    Kpi(
                        "Media",
                        "${pr.mediaProduzione.toInt()}%",
                        if (pr.mediaProduzione >= 60f) "soglia superata" else "soglia 60%",
                        if (pr.mediaProduzione >= 60f) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.error,
                        Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Trend", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    val trend = pr.trendProduzione
                    val (testo, colore) = when {
                        trend > 0.5f -> "↑ migliorando" to MaterialTheme.colorScheme.primary
                        trend < -0.5f -> "↓ peggiorando" to MaterialTheme.colorScheme.error
                        else -> "→ stabile" to MaterialTheme.colorScheme.onSurfaceVariant
                    }
                    Text(
                        testo,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = colore
                    )
                }

                if (pr.criterioDeboleProduzione != "N/A") {
                    Spacer(Modifier.height(6.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Criterio più debole", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            "${pr.criterioDeboleProduzione} (${pr.criterioDeboleValore.toInt()}%)",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                if (pr.storicoProduzione.size >= 2) {
                    Spacer(Modifier.height(12.dp))
                    Text("Evoluzione", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    GraficoEvoluzione(pr)
                }

                if (pr.ultimoPraedikatProduzione.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Ultimo risultato", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            pr.ultimoPraedikatProduzione,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    "Soglia ufficiale: 60% (Goethe/telc/ÖSD)",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun GraficoEvoluzione(pr: CalcoloProgresso.ProgressoReale) {
    val punteggi = pr.storicoProduzione.map { it.punteggioTotale }
    val maxP = punteggi.maxOrNull() ?: 100f
    val minP = punteggi.minOrNull() ?: 0f
    val range = (maxP - minP).coerceAtLeast(1f)
    // Colore del grafico dal tema Material 3 (prima era un viola fisso).
    val coloreLinea = MaterialTheme.colorScheme.primary

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
    ) {
        val w = size.width
        val h = size.height
        val step = if (punteggi.size > 1) w / (punteggi.size - 1) else w

        val path = Path()
        punteggi.forEachIndexed { i, p ->
            val x = i * step
            val y = h - ((p - minP) / range * h)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        drawPath(path, color = coloreLinea, style = Stroke(width = 3f))

        punteggi.forEachIndexed { i, p ->
            val x = i * step
            val y = h - ((p - minP) / range * h)
            drawCircle(color = coloreLinea, radius = 4f, center = Offset(x, y))
        }
    }
}

@Composable
private fun CardTraguardi(pr: CalcoloProgresso.ProgressoReale) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(Modifier.fillMaxWidth().padding(12.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Traguardi", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Text(
                    "${pr.traguardiRaggiunti}/${pr.traguardi.size}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(Modifier.height(12.dp))
            pr.traguardi.chunked(2).forEach { coppia ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    coppia.forEach { t ->
                        Row(
                            modifier = Modifier
                                .weight(1f)
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(if (t.raggiunto) t.icona else "○", style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(0.dp))
                            Column(Modifier.padding(start = 8.dp)) {
                                Text(
                                    t.nome,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (t.raggiunto) FontWeight.SemiBold else FontWeight.Normal,
                                    color = if (t.raggiunto) MaterialTheme.colorScheme.onSurface
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (coppia.size == 1) Spacer(Modifier.weight(1f))
                }
            }
        }
    }
}

/**
 * Esporta un report leggibile (o il CSV degli errori) tramite il foglio di
 * condivisione di Android. Nessun permesso, nessun file temporaneo: il docente
 * riceve il testo via mail, WhatsApp o Drive.
 */
@Composable
private fun CardReportInsegnante(
    pr: CalcoloProgresso.ProgressoReale,
    profilo: ProfiloUtente?,
    errori: List<ErroreEntity>,
    data: String
) {
    val context = LocalContext.current
    var conferma by remember { mutableStateOf<String?>(null) }
    val punteggi = profilo?.stato?.progresso?.punteggiLezioni.orEmpty()
    val nome = profilo?.config?.nomeVisualizzato.orEmpty()

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                "Report per l'insegnante",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Uno snapshot dei dati reali: competenze, lessico, punteggi delle lezioni " +
                    "e quaderno errori. Utile per un colloquio o per il tutoraggio.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = {
                    condividi(
                        context = context,
                        oggetto = "Report tedesco - ${nome.ifBlank { "studente" }}",
                        testo = ReportInsegnante.genera(
                            nome = nome,
                            livelloObiettivo = pr.livelloObiettivo,
                            pr = pr,
                            errori = errori,
                            punteggiLezioni = punteggi,
                            data = data
                        )
                    )
                    conferma = "Report pronto: scegli come condividerlo."
                }) {
                    Text("Condividi report")
                }
                OutlinedButton(onClick = {
                    condividi(
                        context = context,
                        oggetto = "Errori tedesco - ${nome.ifBlank { "studente" }}",
                        testo = ReportInsegnante.csvErrori(errori)
                    )
                    conferma = "CSV pronto: aprilo con Excel o Fogli."
                }) {
                    Text("CSV errori")
                }
            }
            conferma?.let {
                Text(
                    it,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

private fun condividi(context: Context, oggetto: String, testo: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_SUBJECT, oggetto)
        putExtra(Intent.EXTRA_TEXT, testo)
    }
    runCatching {
        context.startActivity(Intent.createChooser(intent, oggetto))
    }
}
