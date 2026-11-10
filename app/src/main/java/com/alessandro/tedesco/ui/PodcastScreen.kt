package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.animateContentSize
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Subtitles
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.PodcastData
import com.alessandro.tedesco.ui.theme.Spaziature

/**
 * Podcast, video e corsi per livello.
 *
 * Tutti i contenuti sono curati e verificati: si aprono nell'app esterna
 * (YouTube, Spotify, browser). Nessun download, nessun hosting, nessun peso
 * sull'APK. La schermata è organizzata per livello e filtrabile per tipo e
 * piattaforma, con un contenuto "scelto per te" per ogni livello.
 */
@Composable
fun PodcastScreen(
    vm: TedescoViewModel,
    onIndietro: () -> Unit = {}
) {
    val context = LocalContext.current
    val profilo by vm.profiloAttivo.collectAsStateWithLifecycle(null)
    val livelloProfilo = remember(profilo) {
        PodcastData.normalizza(profilo?.stato?.progresso?.livelloCorrente?.codiceBreve())
    }
    var livelloScelto by rememberSaveable { mutableStateOf<String?>(null) }
    // Finchè l'utente non sceglie, segue il livello del profilo (che arriva
    // da DataStore in modo asincrono): niente selezione bloccata su "A1".
    val livelloSelezionato = livelloScelto ?: livelloProfilo
    var tipoFiltro by rememberSaveable { mutableStateOf<String?>(null) }
    var piattaformaFiltro by rememberSaveable { mutableStateOf<String?>(null) }

    val tipo = tipoFiltro?.let { nome ->
        PodcastData.Tipo.entries.firstOrNull { it.name == nome }
    }
    val piattaforma = piattaformaFiltro?.let { nome ->
        PodcastData.Piattaforma.entries.firstOrNull { it.name == nome }
    }

    val filtrati = remember(livelloSelezionato, tipoFiltro, piattaformaFiltro) {
        PodcastData.filtra(livelloSelezionato, tipo, piattaforma)
    }
    val inEvidenza = remember(livelloSelezionato, tipoFiltro, piattaformaFiltro) {
        if (tipoFiltro == null && piattaformaFiltro == null) {
            PodcastData.consigliatoPer(livelloSelezionato)
        } else null
    }
    val lista = remember(filtrati, inEvidenza) {
        if (inEvidenza == null) filtrati else filtrati.filter { it.id != inEvidenza.id }
    }
    val infoLivello = remember(livelloSelezionato) { PodcastData.info(livelloSelezionato) }
    val filtriAttivi = tipoFiltro != null || piattaformaFiltro != null

    fun apri(url: String) {
        runCatching {
            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
        }.onFailure {
            Toast.makeText(context, t("Nessuna app disponibile per aprire il link"), Toast.LENGTH_SHORT).show()
        }
    }

    ScreenScaffold(
        title = "Podcast & Video",
        onBack = onIndietro,
        actions = {
            IconButton(
                onClick = {
                    val pool = PodcastData.perLivello(livelloSelezionato)
                    pool.randomOrNull()?.let { apri(it.url) }
                }
            ) {
                Icon(
                    Icons.Filled.Shuffle,
                    contentDescription = t("Apri un contenuto a caso per il livello $livelloSelezionato")
                )
            }
        }
    ) {
        HeroPodcast()

        Spacer(Modifier.height(Spaziature.md))

        Text(
            t("Scegli il livello"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(Spaziature.sm))
        SelettoreLivello(
            selezionato = livelloSelezionato,
            onSeleziona = { livelloScelto = it }
        )

        Spacer(Modifier.height(Spaziature.md))

        CardLivello(info = infoLivello)

        Spacer(Modifier.height(Spaziature.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                t("Filtra"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            if (filtriAttivi) {
                TextButton(
                    onClick = {
                        tipoFiltro = null
                        piattaformaFiltro = null
                    }
                ) {
                    Text(t("Azzera"))
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
        ) {
            FilterChip(
                selected = tipoFiltro == null,
                onClick = { tipoFiltro = null },
                label = { Text(t("Tutti")) },
                leadingIcon = if (tipoFiltro == null) {
                    { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                } else null
            )
            PodcastData.Tipo.entries.forEach { t ->
                val selezionato = tipoFiltro == t.name
                FilterChip(
                    selected = selezionato,
                    onClick = { tipoFiltro = if (selezionato) null else t.name },
                    label = { Text(t(t.etichetta)) },
                    leadingIcon = if (selezionato) {
                        { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    } else null
                )
            }
        }

        Spacer(Modifier.height(Spaziature.xs))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
        ) {
            FilterChip(
                selected = piattaformaFiltro == null,
                onClick = { piattaformaFiltro = null },
                label = { Text(t("Tutte")) }
            )
            PodcastData.Piattaforma.entries.forEach { p ->
                val selezionata = piattaformaFiltro == p.name
                FilterChip(
                    selected = selezionata,
                    onClick = { piattaformaFiltro = if (selezionata) null else p.name },
                    label = { Text(t(p.etichetta)) },
                    leadingIcon = {
                        Icon(
                            iconaPiattaforma(p),
                            contentDescription = null,
                            modifier = Modifier.size(FilterChipDefaults.IconSize)
                        )
                    }
                )
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        Text(
            when {
                filtriAttivi -> "${filtrati.size} " + t("contenuti con questi filtri")
                else -> "${filtrati.size} " + t("contenuti per il livello") + " ${infoLivello.codice}"
            },
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        if (inEvidenza != null) {
            Spacer(Modifier.height(Spaziature.sm))
            CardInEvidenza(episodio = inEvidenza, onApri = { apri(inEvidenza.url) })
        }

        if (lista.isEmpty() && inEvidenza == null) {
            Spacer(Modifier.height(Spaziature.sm))
            StatoVuoto(
                onAzzera = {
                    tipoFiltro = null
                    piattaformaFiltro = null
                }
            )
        } else {
            Spacer(Modifier.height(Spaziature.sm))
            lista.forEach { ep ->
                CardEpisodio(episodio = ep, onApri = { apri(ep.url) })
                Spacer(Modifier.height(Spaziature.sm))
            }
        }

        Spacer(Modifier.height(Spaziature.md))

        ComeAscoltare()
    }
}

// ---------------------------------------------------------------------------
// Hero
// ---------------------------------------------------------------------------

@Composable
private fun HeroPodcast() {
    val gradiente = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.tertiaryContainer
        )
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(gradiente)
    ) {
        Icon(
            imageVector = Icons.Filled.Headphones,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .offset(x = 36.dp)
                .size(150.dp)
                .alpha(0.10f)
        )
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Text(
                t("Ascolto autentico"),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("Il tedesco che si parla davvero"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("Podcast, video e corsi scelti per il tuo livello, gratis. Si aprono in YouTube, Spotify o nel browser."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.md))
            Row(horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)) {
                StatPill("${PodcastData.episodi.size}", t("contenuti"))
                StatPill("4", t("livelli"))
                StatPill(t("Gratis"), t("sempre"))
            }
        }
    }
}

@Composable
private fun StatPill(valore: String, etichetta: String) {
    Surface(
        shape = CircleShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.72f),
        contentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                valore,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.width(4.dp))
            Text(etichetta, style = MaterialTheme.typography.labelSmall)
        }
    }
}

// ---------------------------------------------------------------------------
// Livello
// ---------------------------------------------------------------------------

@Composable
private fun SelettoreLivello(
    selezionato: String,
    onSeleziona: (String) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
    ) {
        PodcastData.livelli().forEach { liv ->
            val sel = liv == selezionato
            val shape = MaterialTheme.shapes.medium
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(shape)
                    .clickable { onSeleziona(liv) },
                shape = shape,
                color = if (sel) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surfaceContainer,
                contentColor = if (sel) MaterialTheme.colorScheme.onPrimary
                else MaterialTheme.colorScheme.onSurface,
                border = if (sel) null
                else BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                Column(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        liv,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        "${PodcastData.conteggio(liv)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = androidx.compose.material3.LocalContentColor.current.copy(alpha = 0.72f)
                    )
                }
            }
        }
    }
}

@Composable
private fun CardLivello(info: PodcastData.Livello) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    t(info.titolo),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Text(
                        info.codice,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                t(info.obiettivo),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.sm))
            Surface(
                shape = MaterialTheme.shapes.small,
                color = MaterialTheme.colorScheme.primaryContainer,
                contentColor = MaterialTheme.colorScheme.onPrimaryContainer
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    Icon(
                        Icons.Filled.Lightbulb,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        t(info.consiglio),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Contenuti
// ---------------------------------------------------------------------------

@Composable
private fun CardInEvidenza(
    episodio: PodcastData.Episodio,
    onApri: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f))
    ) {
        Column(modifier = Modifier.padding(Spaziature.md)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = MaterialTheme.shapes.small,
                    color = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Text(
                        t("Scelto per te"),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
                Spacer(Modifier.weight(1f))
                BadgePiattaforma(episodio.piattaforma)
            }

            Spacer(Modifier.height(Spaziature.sm))

            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBox(episodio = episodio, dimensione = 54.dp)
                Spacer(Modifier.width(Spaziature.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        t(episodio.titolo),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        episodio.canale,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(Modifier.height(Spaziature.sm))

            Text(
                t(episodio.descrizione),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(Spaziature.sm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
            ) {
                MetaTag(
                    icona = Icons.Filled.Schedule,
                    testo = t(episodio.durata),
                    modifier = Modifier.weight(1f, fill = false)
                )
                MetaTag(
                    icona = Icons.Filled.Speed,
                    testo = t(episodio.velocita.etichetta),
                    modifier = Modifier.weight(1f, fill = false)
                )
                MetaTag(
                    icona = Icons.Filled.Subtitles,
                    testo = t(episodio.supporto),
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(Spaziature.md))

            Button(
                onClick = onApri,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.medium
            ) {
                Icon(
                    iconaPiattaforma(episodio.piattaforma),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(ctaPiattaforma(episodio.piattaforma))
            }
        }
    }
}

@Composable
private fun CardEpisodio(
    episodio: PodcastData.Episodio,
    onApri: () -> Unit
) {
    Card(
        onClick = onApri,
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column {
            Row(modifier = Modifier.padding(Spaziature.md)) {
                IconBox(episodio = episodio, dimensione = 48.dp)
                Spacer(Modifier.width(Spaziature.sm))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        t(episodio.titolo),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(2.dp))
                    Text(
                        "${episodio.canale} · ${t(episodio.tipo.etichetta)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        t(episodio.descrizione),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
                    ) {
                        MetaTag(
                            icona = Icons.Filled.Schedule,
                            testo = t(episodio.durata),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                        MetaTag(
                            icona = Icons.Filled.Speed,
                            testo = t(episodio.velocita.etichetta),
                            modifier = Modifier.weight(1f, fill = false)
                        )
                    }
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(
                modifier = Modifier.padding(horizontal = Spaziature.md, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                BadgePiattaforma(episodio.piattaforma)
                Spacer(Modifier.weight(1f))
                Text(
                    t("Apri"),
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(4.dp))
                Icon(
                    Icons.AutoMirrored.Filled.OpenInNew,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun IconBox(episodio: PodcastData.Episodio, dimensione: androidx.compose.ui.unit.Dp) {
    val (contenitore, contenuto) = coloriPiattaforma(episodio.piattaforma)
    Surface(
        shape = MaterialTheme.shapes.medium,
        color = contenitore,
        contentColor = contenuto,
        modifier = Modifier.size(dimensione)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                iconaPiattaforma(episodio.piattaforma),
                contentDescription = null,
                modifier = Modifier.size(dimensione * 0.5f)
            )
        }
    }
}

@Composable
private fun BadgePiattaforma(piattaforma: PodcastData.Piattaforma) {
    val (contenitore, contenuto) = coloriPiattaforma(piattaforma)
    Surface(
        shape = CircleShape,
        color = contenitore,
        contentColor = contenuto
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                iconaPiattaforma(piattaforma),
                contentDescription = null,
                modifier = Modifier.size(12.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(
                t(piattaforma.etichetta),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun MetaTag(
    icona: ImageVector,
    testo: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            icona,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(14.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            testo,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

// ---------------------------------------------------------------------------
// Stati di supporto
// ---------------------------------------------------------------------------

@Composable
private fun StatoVuoto(onAzzera: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spaziature.lg),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Filled.Headphones,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(Spaziature.sm))
            Text(
                t("Nessun contenuto con questi filtri"),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("Prova un altro tipo o un'altra piattaforma."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(Spaziature.sm))
            TextButton(onClick = onAzzera) { Text(t("Mostra tutti i contenuti")) }
        }
    }
}

@Composable
private fun ComeAscoltare() {
    var expanded by rememberSaveable { mutableStateOf(false) }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(modifier = Modifier.padding(Spaziature.md).animateContentSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Lightbulb,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(Spaziature.sm))
                Text(
                    t("Come sfruttare la sezione"),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                    contentDescription = if (expanded) t("Chiudi") else t("Apri"),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(18.dp)
                )
            }
            if (expanded) {
                Spacer(Modifier.height(Spaziature.sm))
                PuntoAscolto("1", t("Scegli un contenuto solo e portalo a termine: la costanza vale più della quantità."))
                PuntoAscolto("2", t("Prima ascolta con il supporto scritto, poi riascolta senza: il secondo passaggio è quello che fa crescere."))
                PuntoAscolto("3", t("Chiudi con 3 parole nuove e una frase tua: l'ascolto diventa lessico attivo."))
                Spacer(Modifier.height(Spaziature.sm))
                Text(
                    t("Serve una connessione: i link si aprono in YouTube, Spotify o nel browser.") + " " +
                        t("Il resto dell'app resta offline."),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun PuntoAscolto(numero: String, testo: String) {
    Row(modifier = Modifier.padding(vertical = 3.dp)) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    numero,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Spacer(Modifier.width(Spaziature.sm))
        Text(
            testo,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// ---------------------------------------------------------------------------
// Helper di presentazione
// ---------------------------------------------------------------------------

private fun iconaPiattaforma(piattaforma: PodcastData.Piattaforma): ImageVector =
    when (piattaforma) {
        PodcastData.Piattaforma.YOUTUBE -> Icons.Filled.SmartDisplay
        PodcastData.Piattaforma.SPOTIFY -> Icons.Filled.Podcasts
        PodcastData.Piattaforma.DW -> Icons.Filled.Public
        PodcastData.Piattaforma.WEB -> Icons.Filled.Language
    }

@Composable
private fun coloriPiattaforma(piattaforma: PodcastData.Piattaforma): Pair<Color, Color> =
    when (piattaforma) {
        PodcastData.Piattaforma.YOUTUBE ->
            MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        PodcastData.Piattaforma.SPOTIFY ->
            MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        PodcastData.Piattaforma.DW ->
            MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.onPrimaryContainer
        PodcastData.Piattaforma.WEB ->
            MaterialTheme.colorScheme.surfaceContainerHighest to MaterialTheme.colorScheme.onSurface
    }

private fun ctaPiattaforma(piattaforma: PodcastData.Piattaforma): String =
    when (piattaforma) {
        PodcastData.Piattaforma.YOUTUBE -> t("Guarda su YouTube")
        PodcastData.Piattaforma.SPOTIFY -> t("Ascolta su Spotify")
        PodcastData.Piattaforma.DW -> t("Apri su Deutsche Welle")
        PodcastData.Piattaforma.WEB -> t("Apri sul sito")
    }