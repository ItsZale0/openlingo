package com.alessandro.tedesco.desktop

import androidx.compose.material3.VerticalDivider
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.rememberWindowState
import com.alessandro.tedesco.desktop.ui.Etichetta
import com.alessandro.tedesco.desktop.ui.SchermataEsercizi
import com.alessandro.tedesco.desktop.ui.SchermataHome
import com.alessandro.tedesco.desktop.ui.SchermataImpostazioni
import com.alessandro.tedesco.desktop.ui.SchermataPodcast
import com.alessandro.tedesco.desktop.ui.SchermataProgresso
import com.alessandro.tedesco.desktop.ui.SchermataRipasso
import com.alessandro.tedesco.desktop.ui.SchermataUnita
import com.alessandro.tedesco.desktop.ui.SchermataWortliste
import com.alessandro.tedesco.desktop.ui.TemaTedesco
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

enum class Schermata(val titolo: String, val icona: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    ESERCIZI("Esercizi", Icons.Filled.School),
    RIPASSO("Ripasso", Icons.Filled.Refresh),
    WORTLISTE("Wortliste", Icons.AutoMirrored.Filled.MenuBook),
    PODCAST("Podcast", Icons.Filled.Headphones),
    PROGRESSO("Progresso", Icons.Filled.BarChart),
    IMPOSTAZIONI("Impostazioni", Icons.Filled.Settings)
}

fun main() = application {
    val store = remember { DesktopStore() }
    var schermata by remember { mutableStateOf(Schermata.HOME) }
    var unitaId by remember { mutableStateOf<String?>(null) }

    Window(
        onCloseRequest = ::exitApplication,
        title = "Tedesco — corso A0-B2",
        // La finestra parte massimizzata: su schermi piccoli (o con scala 125%)
        // una finestra fissa rischierebbe di uscire dallo schermo.
        state = rememberWindowState(
            width = 1280.dp,
            height = 820.dp,
            placement = WindowPlacement.Maximized
        )
    ) {
        // All'apertura (e a ogni cambio profilo) il vocabolario del corso si
        // aggiorna da solo, in sottofondo: l'utente trova subito le sue parole
        // senza premere nulla. Se non c'e' rete resta il lessico locale e
        // l'errore finisce nel log del feed.
        LaunchedEffect(store.repo.profiloAttivoId) {
            if (store.feedUrl.isNotBlank()) {
                withContext(Dispatchers.IO) { store.sincronizzaFeed() }
            }
            // Sincronizzazione automatica del cloud (grammatica e lezioni
            // incluse): scarica, unisce e carica solo se qualcosa e' cambiato.
            // Offline o senza token non succede nulla, senza errori visibili.
            withContext(Dispatchers.IO) { store.sincronizzaCloudAutomatica() }
        }

        // Sincronizzazione periodica: ogni 5 minuti, in sottofondo, per tenere
        // telefono e desktop allineati senza intervento dell'utente.
        LaunchedEffect(Unit) {
            while (true) {
                delay(5 * 60 * 1000L)
                withContext(Dispatchers.IO) { store.sincronizzaCloudAutomatica() }
            }
        }

        TemaTedesco(scuro = store.temaScuro()) {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Row(Modifier.fillMaxSize()) {
                    BarraNavigazione(
                        schermata = schermata,
                        onScegli = {
                            unitaId = null
                            schermata = it
                        }
                    )
                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                    Column(Modifier.weight(1f).fillMaxHeight()) {
                        BarraSuperiore(store)
                        Box(Modifier.weight(1f).fillMaxWidth()) {
                            val unita = unitaId
                            when {
                                unita != null -> SchermataUnita(
                                    store = store,
                                    unitaId = unita,
                                    onIndietro = { unitaId = null }
                                )

                                else -> when (schermata) {
                                    Schermata.HOME -> SchermataHome(
                                        store = store,
                                        onVaiA = { schermata = it },
                                        onApriUnita = { unitaId = it }
                                    )

                                    Schermata.ESERCIZI -> SchermataEsercizi(
                                        store = store,
                                        onApriUnita = { unitaId = it },
                                        onVaiA = { schermata = it }
                                    )

                                    Schermata.RIPASSO -> SchermataRipasso(store)
                                    Schermata.WORTLISTE -> SchermataWortliste(store)
                                    Schermata.PODCAST -> SchermataPodcast(store)
                                    Schermata.PROGRESSO -> SchermataProgresso(store)
                                    Schermata.IMPOSTAZIONI -> SchermataImpostazioni(
                                        store = store,
                                        onCambiaProfilo = {
                                            unitaId = null
                                            schermata = Schermata.HOME
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BarraNavigazione(schermata: Schermata, onScegli: (Schermata) -> Unit) {
    NavigationRail(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        header = {
            Column(
                modifier = Modifier.padding(vertical = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TEDESCO",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "desktop",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    ) {
        Spacer(Modifier.height(8.dp))
        Schermata.entries.forEach { voce ->
            NavigationRailItem(
                selected = schermata == voce,
                onClick = { onScegli(voce) },
                icon = { Icon(voce.icona, contentDescription = null) },
                label = { Text(voce.titolo, style = MaterialTheme.typography.labelMedium) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onSecondaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.onSurface,
                    indicatorColor = MaterialTheme.colorScheme.secondaryContainer
                )
            )
        }
    }
}

@Composable
private fun BarraSuperiore(store: DesktopStore) {
    var menuAperto by remember { mutableStateOf(false) }
    val profilo = store.profilo

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 28.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                profilo?.config?.nomeVisualizzato ?: "Nessun profilo",
                style = MaterialTheme.typography.titleMedium
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Etichetta(store.stato.progresso.livelloCorrente.label.substringBefore(" — "))
                Text(
                    "obiettivo ${store.stato.progresso.obiettivoLivello.label.substringBefore(" — ")}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    "${store.stato.streakGiorni} giorni di fila",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            IconButton(onClick = { store.commutaTema() }) {
                Icon(
                    if (store.temaScuro()) Icons.Filled.LightMode else Icons.Filled.DarkMode,
                    contentDescription = "Cambia tema"
                )
            }
            Box {
                TextButton(onClick = { menuAperto = true }) {
                    Icon(Icons.Filled.SwapHoriz, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Profilo")
                }
                DropdownMenu(expanded = menuAperto, onDismissRequest = { menuAperto = false }) {
                    store.elencoProfili.forEach { p ->
                        DropdownMenuItem(
                            text = {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(p.config.nomeVisualizzato)
                                    if (p.id == store.repo.profiloAttivoId) {
                                        Spacer(Modifier.width(8.dp))
                                        Icon(
                                            Icons.Filled.Check,
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = {
                                store.attivaProfilo(p.id)
                                menuAperto = false
                            }
                        )
                    }
                }
            }
        }
    }
}
