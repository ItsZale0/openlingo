package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.Sudtirol
import com.alessandro.tedesco.ui.theme.Spaziature
import java.util.Locale

enum class SezioneSudtirol(val etichetta: String) {
    VARIANTI(t("Austriaco")),
    LESSICO("Lessico"),
    FRASI(t("Frasi"))
}

/**
 * Modulo Südtirol/Austria: la lingua che serve a Bressanone non è solo il
 * tedesco standard. Varianti austriache, lessico sanitario/amministrativo e
 * frasi da ambulatorio.
 */
@Composable
fun SudtirolScreen(
    onIndietro: () -> Unit = {}
) {
    val tts = rememberTtsHelper(LocalContext.current, Locale.GERMAN)
    var sezione by remember { mutableStateOf(SezioneSudtirol.VARIANTI) }

    ScreenScaffold("Südtirol / Austria", onBack = onIndietro) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(Spaziature.sm)
        ) {
            Text(
                "A Bressanone e in Austria molte parole quotidiane sono diverse dallo standard. " +
                    t("Capirle è parte della competenza reale, non un vezzo. Ogni variante è un arricchimento!"),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(Spaziature.xs)
            ) {
                SezioneSudtirol.entries.forEach { s ->
                    FilterChip(
                        selected = s == sezione,
                        onClick = { sezione = s },
                        label = { Text(t(s.etichetta)) }
                    )
                }
            }

            when (sezione) {
                SezioneSudtirol.VARIANTI -> {
                    Text(
                        t("Variante austriaca → tedesco standard"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Sudtirol.varianti.forEach { v ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(Spaziature.md),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    Text(v.austriaco, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
                                    Text(
                                        "standard: ${v.standard}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(t(v.italiano), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                }
                                IconButton(onClick = { tts.speak("${v.austriaco}. ${v.standard}", velocita = 0.85f) }) {
                                    Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = t("Ascolta"), modifier = Modifier.size(20.dp))
                                }
                            }
                        }
                    }
                }

                SezioneSudtirol.LESSICO -> {
                    val categorie = remember { Sudtirol.lessico.map { it.categoria }.distinct() }
                    categorie.forEach { categoria ->
                        Text(
                            categoria,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Sudtirol.lessico.filter { it.categoria == categoria }.forEach { voce ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(horizontal = Spaziature.md, vertical = Spaziature.sm),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Text(voce.tedesco, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
                                        Text(t(voce.italiano), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    IconButton(onClick = { tts.speak(voce.tedesco, velocita = 0.85f) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = t("Ascolta"), modifier = Modifier.size(20.dp))
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(Spaziature.sm))
                    }
                }

                SezioneSudtirol.FRASI -> {
                    Text(
                        t("Frasi da ambulatorio"),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    Sudtirol.frasi.forEach { frase ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)
                        ) {
                            Column(Modifier.fillMaxWidth().padding(Spaziature.md)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        frase.tedesco,
                                        style = MaterialTheme.typography.bodyLarge,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(onClick = { tts.speak(frase.tedesco, velocita = 0.85f) }) {
                                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = t("Ascolta"), modifier = Modifier.size(20.dp))
                                    }
                                }
                                Text(
                                    frase.italiano,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (frase.nota.isNotBlank()) {
                                    Spacer(Modifier.height(4.dp))
                                    Text(frase.nota, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
