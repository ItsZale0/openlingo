package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.clickable
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Hearing
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.CalcoloProgresso
import com.alessandro.tedesco.data.ConsigliEsercizi
import com.alessandro.tedesco.data.CertificazioniUfficiali
import com.alessandro.tedesco.data.LessicoLivelli
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RecordVoiceOver
import com.alessandro.tedesco.data.Dialoghi
import com.alessandro.tedesco.data.PodcastData
import com.alessandro.tedesco.data.Sudtirol
import com.alessandro.tedesco.data.WortlisteB1

/**
 * Hub degli esercizi: tutti i tipi che esistevano n'app, organizzati.
 *
 * In cima un CONSIGLIO DINAMICO: in base ai progressi reali (parole fragili,
 * competenza piu' debole) suggerisce il tipo di esercizio piu' utile ora.
 * Sotto un selettore: una card per ogni tipo, con icona, descrizione e dato.
 */
@Composable
fun EserciziScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {},
    onVaiA: (String) -> Unit = {}
) {
    val p by vm.progressoReale.collectAsStateWithLifecycle(null)
    val pr = p
    val erroriAperti by vm.errori.collectAsStateWithLifecycle(emptyList())
    val consiglio = ConsigliEsercizi.consiglio(pr)

    ScreenScaffold(t("Esercizi"), onBack = onIndietro) {
        Column(
            modifier = Modifier.widthIn(max = dimensioneContenuto()),
            verticalArrangement = Arrangement.spacedBy(spaziaturaSchermo())
        ) {
            // ---- Consiglio dinamico ----
            Card(
                modifier = Modifier.fillMaxWidth().animateContentSize(),
                shape = MaterialTheme.shapes.medium,
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                
            ) {
                Column(Modifier.fillMaxWidth().padding(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Filled.EmojiEvents,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            t("Consiglio per te"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(
                        t(consiglio.titolo),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        t(consiglio.motivo),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Spacer(Modifier.height(12.dp))
                    Button(
                        onClick = { onVaiA(consiglio.route) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = MaterialTheme.shapes.large
                    ) {
                        Text(t("Inizia"))
                    }
                }
            }

            // ---- Selettore: 3 sezioni raggruppate ----
            sezioniEsercizi().forEach { (titoloSezione, ids) ->
                Text(
                    titoloSezione,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(Modifier.height(4.dp))
                ids.mapNotNull { tipoPerId(it) }.chunked(2).forEach { riga ->
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        riga.forEach { tipo ->
                            CardTipoEsercizio(
                                tipo = tipo,
                                stat = statPerTipo(tipo.id, pr, erroriAperti.count { !it.risolto }),
                                onClick = { onVaiA(tipo.route) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (riga.size == 1) Spacer(Modifier.weight(1f))
                    }
                }
            }

            // Nota soglia ufficiale
            Spacer(Modifier.height(8.dp))
            Text(
                t("Soglia ufficiale Goethe/telc/ÖSD: 60% per competenza. Il livello sale solo quando tutte e 4 le competenze superano la soglia."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

private data class TipoEsercizio(
    val id: String,
    val route: String,
    val nome: String,
    val descrizione: String,
    val icona: ImageVector
)

@Composable
private fun tipiEsercizio(): List<TipoEsercizio> = listOf(
    TipoEsercizio("grammatica", "grammatica", t("Grammatica"), t("Regole e forme, una alla volta"),
        Icons.Filled.School),
    TipoEsercizio("gender_trainer", "gender_trainer", t("Allenatore articoli"), t("Indovina der, die o das con le parole del tuo lessico"),
        Icons.Filled.Translate),
    TipoEsercizio("comprensione", "comprensione", t("Lettura"), t("Un testo calibrato al tuo livello"),
        Icons.Filled.Quiz),
    TipoEsercizio("produzione", "produzione", t("Produzione scritta"), t("Scrivi un testo: lo corregge il tutor"),
        Icons.Filled.Edit),
    TipoEsercizio("espressioni", "espressioni", t("Espressioni rapide"), t("Quiz: scegli la frase tedesca giusta"),
        Icons.AutoMirrored.Filled.Chat),
    TipoEsercizio("verifica", "verifica", t("Verifica lessico"), t("Dimostra di sapere le parole studiate"),
        Icons.Filled.Translate),
    TipoEsercizio("ascolto", "ascolto", t("Ascolto"), t("Audio con domande per l'orecchio"),
        Icons.Filled.Hearing),
    TipoEsercizio("ripasso", "ripasso", t("Ripasso"), t("Ripassa le parole sbagliate o fragili"),
        Icons.Filled.Refresh),
    TipoEsercizio("lessico", "lessico", t("Lessico A1-B2"),
        "${LessicoLivelli.tutte.size} " + t("parole per livello, 6 tipi di esercizio"),
        Icons.AutoMirrored.Filled.MenuBook),
    TipoEsercizio("produzione_attiva", "produzione_attiva", t("Produzione attiva"),
        t("Scrivi, completa, riordina: recupero attivo"), Icons.Filled.Create),
    TipoEsercizio("errori", "errori", t("Quaderno errori"),
        t("Gli errori tornano al momento giusto (SRS)"), Icons.Filled.ErrorOutline),
    TipoEsercizio("dialoghi", "dialoghi", t("Dialoghi"),
        t("Conversazioni a due voci con domande"), Icons.Filled.Headphones),
    TipoEsercizio("pronuncia", "pronuncia", t("Pronuncia"),
        t("Coppie minime, registro e confronto"), Icons.Filled.RecordVoiceOver),
    TipoEsercizio("sprechuebungen", "sprechuebungen", t("Sprechübungen"),
        t("Parla su un tema e confrontati con il modello"), Icons.Filled.RecordVoiceOver),
    TipoEsercizio("wortliste", "wortliste", t("Wortliste A1-B1"),
        "${WortlisteB1.tutte.size} " + t("parole curate da portare nel lessico"), Icons.AutoMirrored.Filled.MenuBook),
    TipoEsercizio("sudtirol", "sudtirol", t("Südtirol / Austria"),
        t("Varianti locali e lessico sanitario"), Icons.Filled.Place),
    TipoEsercizio("podcast", "podcast", t("Podcast & Video"),
        t("Ascolto autentico per livello: YouTube, Spotify, DW"), Icons.Filled.Headphones)
)

/**
 * Le 15 tipologie organizzate in 3 sezioni: cosi' la scansione visiva
 * e' a colpo d'occhio invece di un ammasso unico di card.
 */
@Composable
private fun sezioniEsercizi(): List<Pair<String, List<String>>> = listOf(
    t("Competenze") to listOf("grammatica", "gender_trainer", "comprensione", "produzione", "espressioni", "verifica", "ascolto"),
    t("Ripasso e consolidamento") to listOf("lessico", "ripasso", "produzione_attiva", "errori"),
    t("Esplora") to listOf("dialoghi", "pronuncia", "sprechuebungen", "wortliste", "sudtirol", "podcast")
)

@Composable
private fun tipoPerId(id: String): TipoEsercizio? =
    tipiEsercizio().firstOrNull { it.id == id }

private fun statPerTipo(
    id: String,
    pr: CalcoloProgresso.ProgressoReale?,
    erroriAperti: Int = 0
): String {
    if (pr == null) return t("Per iniziare")
    val soglia = CertificazioniUfficiali.SOGLIA_SUPERAMENTO
    return when (id) {
        "grammatica" -> "${pr.competenze.grammatica.toInt()}% ${if (pr.competenze.grammatica >= soglia) "✓" else ""}"
        "comprensione" -> "${pr.competenze.comprensione.toInt()}% ${if (pr.competenze.comprensione >= soglia) "✓" else ""}"
        "produzione" -> if (pr.produzioniScritte > 0) "${pr.produzioniScritte} " + t("testi") + " - ${pr.mediaProduzione.toInt()}%"
            else t("Nessun testo ancora")
        "espressioni" -> t("Quiz rapido")
        "verifica" -> "${pr.paroleImparate}/${pr.paroleDisponibili} " + t("verificate")
        "gender_trainer" -> t("der, die, das")
        "ascolto" -> t("Livello") + " ${pr.livello.label.substringBefore(" ")}"
        "ripasso" -> if (pr.paroleFragili > 0) "${pr.paroleFragili} " + t("da ripassare") else t("Tutto a posto")
        "lessico" -> "${LessicoLivelli.tutte.size} " + t("parole A1-B2")
        "produzione_attiva" -> "72 " + t("esercizi offline")
        "errori" -> if (erroriAperti > 0) "$erroriAperti " + t("da consolidare") else t("Nessun errore aperto")
        "dialoghi" -> "${Dialoghi.dialoghi.size} " + t("dialoghi")
        "pronuncia" -> t("Ascolta e registrati")
        "wortliste" -> "${WortlisteB1.tutte.size} " + t("parole A1-B1")
        "sudtirol" -> "${Sudtirol.varianti.size} " + t("varianti + lessico")
        "podcast" -> "${PodcastData.episodi.size} " + t("contenuti per livello")
        else -> ""
    }
}

@Composable
private fun CardTipoEsercizio(
    tipo: TipoEsercizio,
    stat: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clickable(
                onClickLabel = t("Apri") + " ${tipo.nome}",
                role = Role.Button,
                onClick = onClick
            )
            .padding(vertical = 2.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer),
        
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(38.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        tipo.icona,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Text(
                tipo.nome,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                tipo.descrizione,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stat,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
