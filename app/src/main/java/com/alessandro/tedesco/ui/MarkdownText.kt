package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp

/**
 * Renderer markdown UNICO dell'app (teoria lezioni, correzioni del tutor, feedback).
 *
 * Supporta: titoli (# ## ### ####), elenchi puntati (- * \u2022), elenchi numerati (1. 2.),
 * citazioni (>), separatori (---), righe vuote, **grassetto**, *corsivo*, `codice`,
 * link [testo](url) ridotti al solo testo.
 *
 * Le righe sono precalcolate una volta sola (un `remember`), così non si costruiscono
 * AnnotatedString a ogni ricomposizione.
 */
private enum class TipoRiga { TITOLO1, TITOLO2, TITOLO3, TITOLO4, PUNTATO, NUMERATO, CITAZIONE, SEPARATORE, TABELLA, VUOTO, PARAGRAFO }

internal enum class Allineamento { SX, CENTRO, DX }

/** Tabella markdown già parsata: intestazioni, corpo e allineamenti per colonna. */
internal data class TabellaMd(
    val intestazioni: List<AnnotatedString>,
    val righe: List<List<AnnotatedString>>,
    val allineamenti: List<Allineamento>
)

private data class RigaMd(
    val tipo: TipoRiga,
    val testo: AnnotatedString = AnnotatedString(""),
    val numero: String = "",
    val tabella: TabellaMd? = null
)

private val REGEX_INLINE = Regex("""\*\*(.+?)\*\*|__(.+?)__|`(.+?)`|\*(.+?)\*|_(.+?)_""")
private val REGEX_LINK = Regex("""\[(.+?)\]\((.*?)\)""")
private val REGEX_TITOLO = Regex("""^(#{1,6})\s*(.*)$""")
private val REGEX_NUMERATO = Regex("""^(\d{1,2})[.)]\s+(.*)$""")
private val REGEX_SEPARATORE = Regex("""^(-{3,}|\*{3,}|_{3,})$""")

private fun inline(testo: String, coloreCodice: Color): AnnotatedString {
    val pulito = REGEX_LINK.replace(testo, "$1")
    val builder = AnnotatedString.Builder()
    var ultimo = 0
    for (m in REGEX_INLINE.findAll(pulito)) {
        if (m.range.first > ultimo) builder.append(pulito.substring(ultimo, m.range.first))
        val g = m.groupValues
        when {
            g[1].isNotEmpty() -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g[1]) }
            g[2].isNotEmpty() -> builder.withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(g[2]) }
            g[3].isNotEmpty() -> builder.withStyle(
                SpanStyle(fontFamily = FontFamily.Monospace, background = coloreCodice)
            ) { append(g[3]) }
            g[4].isNotEmpty() -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(g[4]) }
            g[5].isNotEmpty() -> builder.withStyle(SpanStyle(fontStyle = FontStyle.Italic)) { append(g[5]) }
        }
        ultimo = m.range.last + 1
    }
    if (ultimo < pulito.length) builder.append(pulito.substring(ultimo))
    return builder.toAnnotatedString()
}

// ---- Tabelle (| a | b |  +  |---|---| ) ----

/**
 * Riga che può appartenere a una tabella. Basta UN pipe: il markdown ammette anche
 * `Wort | Bedeutung` senza pipe esterni (i modelli lo scrivono così).
 */
internal fun eRigaTabella(riga: String): Boolean = riga.contains('|')

/** Riga separatrice dell'intestazione: solo -, :, | e spazi, con almeno un -. */
internal fun eSeparatoreTabella(riga: String): Boolean {
    if (!riga.contains('|')) return false
    val pulita = riga.replace("|", "").trim()
    if (pulita.isEmpty() || !pulita.contains('-')) return false
    return pulita.all { it == '-' || it == ':' || it == ' ' }
}

/** Celle di una riga di tabella, senza i pipe esterni. */
internal fun celleTabella(riga: String): List<String> {
    var r = riga.trim()
    if (r.startsWith("|")) r = r.substring(1)
    if (r.endsWith("|")) r = r.dropLast(1)
    return r.split("|").map { it.trim() }
}

private fun allineamentoCella(cella: String): Allineamento {
    val c = cella.trim()
    val sx = c.startsWith(":")
    val dx = c.endsWith(":")
    return when {
        sx && dx -> Allineamento.CENTRO
        dx -> Allineamento.DX
        else -> Allineamento.SX
    }
}

/**
 * Se in `righe[indice]` inizia una tabella markdown (intestazione + separatore),
 * la costruisce e ritorna anche l'indice della prima riga DOPO la tabella.
 * Altrimenti null: la riga va trattata come testo normale.
 */
internal fun rilevaTabella(
    righe: List<String>,
    indice: Int,
    coloreCodice: Color = Color.Unspecified
): Pair<TabellaMd, Int>? {
    if (indice + 1 >= righe.size) return null
    val testa = righe[indice].trim()
    val sep = righe[indice + 1].trim()
    if (!eRigaTabella(testa) || !eSeparatoreTabella(sep)) return null

    val intestazioni = celleTabella(testa).map { inline(it, coloreCodice) }
    val allineamenti = List(intestazioni.size) { k ->
        allineamentoCella(celleTabella(sep).getOrElse(k) { "---" })
    }
    val corpo = mutableListOf<List<AnnotatedString>>()
    var j = indice + 2
    while (j < righe.size && eRigaTabella(righe[j].trim())) {
        val celle = celleTabella(righe[j].trim())
            .take(intestazioni.size)
            .map { inline(it, coloreCodice) }
        // Riempi le righe corte: senza padding le colonne non restano allineate.
        corpo.add(celle + List(intestazioni.size - celle.size) { AnnotatedString("") })
        j++
    }
    return TabellaMd(intestazioni, corpo, allineamenti) to j
}

private fun parseRighe(markdown: String, coloreCodice: Color): List<RigaMd> {
    val risultato = mutableListOf<RigaMd>()
    val righeGrezze = markdown.replace("\r\n", "\n").split("\n")
    var indice = 0
    while (indice < righeGrezze.size) {
        val riga = righeGrezze[indice].trim()

        // Tabella: consuma tutte le righe che le appartengono in un blocco solo.
        val tabella = rilevaTabella(righeGrezze, indice, coloreCodice)
        if (tabella != null) {
            risultato.add(RigaMd(TipoRiga.TABELLA, tabella = tabella.first))
            indice = tabella.second
            continue
        }

        when {
            riga.isEmpty() -> risultato.add(RigaMd(TipoRiga.VUOTO))
            REGEX_SEPARATORE.matches(riga) -> risultato.add(RigaMd(TipoRiga.SEPARATORE))
            REGEX_TITOLO.matches(riga) -> {
                val m = REGEX_TITOLO.find(riga)!!
                val livello = m.groupValues[1].length
                val testo = inline(m.groupValues[2], coloreCodice)
                val tipo = when (livello) {
                    1 -> TipoRiga.TITOLO1
                    2 -> TipoRiga.TITOLO2
                    3 -> TipoRiga.TITOLO3
                    else -> TipoRiga.TITOLO4
                }
                risultato.add(RigaMd(tipo, testo))
            }
            riga.startsWith("> ") || riga == ">" -> {
                risultato.add(RigaMd(TipoRiga.CITAZIONE, inline(riga.removePrefix(">").trim(), coloreCodice)))
            }
            REGEX_NUMERATO.matches(riga) -> {
                val m = REGEX_NUMERATO.find(riga)!!
                risultato.add(RigaMd(TipoRiga.NUMERATO, inline(m.groupValues[2], coloreCodice), m.groupValues[1]))
            }
            riga.startsWith("- ") || riga.startsWith("* ") || riga.startsWith("\u2022 ") -> {
                risultato.add(RigaMd(TipoRiga.PUNTATO, inline(riga.drop(2).trim(), coloreCodice)))
            }
            else -> risultato.add(RigaMd(TipoRiga.PARAGRAFO, inline(riga, coloreCodice)))
        }
        indice++
    }
    // Rimuove le righe vuote consecutive e quelle in testa/fondo
    val compattate = mutableListOf<RigaMd>()
    risultato.forEach { r ->
        if (r.tipo == TipoRiga.VUOTO && (compattate.isEmpty() || compattate.last().tipo == TipoRiga.VUOTO)) return@forEach
        compattate.add(r)
    }
    while (compattate.isNotEmpty() && compattate.last().tipo == TipoRiga.VUOTO) compattate.removeAt(compattate.lastIndex)
    return compattate
}

@Composable
fun MarkdownText(
    markdown: String,
    color: Color,
    modifier: Modifier = Modifier,
    stile: TextStyle = MaterialTheme.typography.bodyMedium,
) {
    // Colore del codice inline dal tema Material 3 (prima era fisso).
    val coloreCodice = MaterialTheme.colorScheme.surfaceContainer
    val righe = remember(markdown, coloreCodice) { parseRighe(markdown, coloreCodice) }
    Column(modifier = modifier) {
        righe.forEach { riga ->
            when (riga.tipo) {
                TipoRiga.TITOLO1 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                TipoRiga.TITOLO2 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 10.dp, bottom = 4.dp)
                )
                TipoRiga.TITOLO3 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = color,
                    modifier = Modifier.padding(top = 8.dp, bottom = 2.dp)
                )
                TipoRiga.TITOLO4 -> Text(
                    riga.testo,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = color,
                    modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)
                )
                TipoRiga.PUNTATO -> Row(modifier = Modifier.padding(start = 4.dp, top = 3.dp)) {
                    Text("\u2022", color = color, style = stile)
                    Spacer(Modifier.width(8.dp))
                    Text(riga.testo, style = stile, color = color)
                }
                TipoRiga.NUMERATO -> Row(modifier = Modifier.padding(start = 4.dp, top = 3.dp)) {
                    Text(t("${riga.numero}."), color = color, style = stile)
                    Spacer(Modifier.width(8.dp))
                    Text(riga.testo, style = stile, color = color)
                }
                TipoRiga.CITAZIONE -> Row(
                    modifier = Modifier
                        .height(IntrinsicSize.Min)
                        .padding(vertical = 3.dp)
                ) {
                    Box(
                        Modifier
                            .width(3.dp)
                            .fillMaxHeight()
                            .background(MaterialTheme.colorScheme.primary)
                    )
                    Spacer(Modifier.width(10.dp))
                    Text(
                        riga.testo,
                        style = stile,
                        color = color,
                        fontStyle = FontStyle.Italic
                    )
                }
                TipoRiga.TABELLA -> riga.tabella?.let { t ->
                    TabellaMarkdown(t, color, stile)
                }
                TipoRiga.SEPARATORE -> HorizontalDivider(
                    modifier = Modifier.padding(vertical = 8.dp),
                    color = color
                )
                TipoRiga.VUOTO -> Spacer(Modifier.height(8.dp))
                TipoRiga.PARAGRAFO -> Text(
                    riga.testo,
                    style = stile,
                    color = color,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }
        }
    }
}

/**
 * Tabella markdown: intestazione su fondo attenuato, bordi, colonne a peso uguale
 * e allineamento per colonna (|:---|:---:|---:|).
 */
@Composable
private fun TabellaMarkdown(tabella: TabellaMd, color: Color, stile: TextStyle) {
    val bordi = MaterialTheme.colorScheme.outlineVariant
    val forma = MaterialTheme.shapes.medium
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(forma)
            .border(1.dp, bordi, forma)
    ) {
        RigaTabella(
            celle = tabella.intestazioni,
            allineamenti = tabella.allineamenti,
            color = color,
            stile = stile,
            grassetto = true,
            sfondo = MaterialTheme.colorScheme.surfaceContainer
        )
        HorizontalDivider(color = bordi)
        tabella.righe.forEachIndexed { i, riga ->
            RigaTabella(
                celle = riga,
                allineamenti = tabella.allineamenti,
                color = color,
                stile = stile,
                grassetto = false,
                sfondo = Color.Transparent
            )
            if (i < tabella.righe.lastIndex) {
                HorizontalDivider(color = bordi)
            }
        }
    }
}

@Composable
private fun RigaTabella(
    celle: List<AnnotatedString>,
    allineamenti: List<Allineamento>,
    color: Color,
    stile: TextStyle,
    grassetto: Boolean,
    sfondo: Color
) {
    Row(modifier = Modifier.fillMaxWidth().background(sfondo)) {
        celle.forEachIndexed { i, cella ->
            Text(
                text = cella,
                style = stile,
                color = color,
                fontWeight = if (grassetto) FontWeight.SemiBold else null,
                textAlign = when (allineamenti.getOrNull(i)) {
                    Allineamento.CENTRO -> TextAlign.Center
                    Allineamento.DX -> TextAlign.End
                    else -> TextAlign.Start
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp, vertical = 7.dp)
            )
        }
    }
}
