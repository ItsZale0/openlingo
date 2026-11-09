package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import android.content.Context
import android.speech.tts.TextToSpeech
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.alessandro.tedesco.data.ParolaTedesca
import com.alessandro.tedesco.data.SessionState
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RipassoScreen(vm: TedescoViewModel, onIndietro: () -> Unit, onVaiANuove: () -> Unit = {}) {
    val sessionState by vm.sessione.collectAsStateWithLifecycle(SessionState())
    val caricamento by vm.caricamento.collectAsStateWithLifecycle(false)

    val context = LocalContext.current
    val ttsHelper = rememberTtsHelper(context)

    // Carica la sessione dalla fonte unica (stessa lista del contatore).
    // Senza questo la schermata mostrava una sessione vuota o vecchia: "niente da
    // ripassare" a schermo aperto, "2 da ripassare" a schermo chiuso.
    LaunchedEffect(Unit) { vm.caricaSessione() }

    // Se la sessione è stata completata, il passo t("Ripasso") del piano di oggi
    // si spunta da solo. La spunta manuale resta disponibile nella home.
    LaunchedEffect(sessionState.totale, sessionState.cartaCorrente) {
        if (sessionState.cartaCorrente == null && sessionState.totale > 0) {
            vm.completaPassoGiornaliero("ripasso")
        }
    }

    ScreenScaffold(t("Ripasso"), onBack = onIndietro, scrollable = false) {

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .widthIn(max = dimensioneContenuto())
            ) {
                LinearProgressIndicator(
                    progress = { sessionState.progresso },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                )

                Spacer(Modifier.height(28.dp))

            val carta = sessionState.cartaCorrente

            if (caricamento) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(t("Preparo le carte..."))
                }
            } else if (carta == null) {
                // sessione finita (o vuota)
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.animateContentSize()) {
                        if (sessionState.totale == 0) {
                            // Nulla da ripassare: guida verso le nuove parole
                            Text(
                                text = t("Nessuna parola da ripassare ora"),
                                style = MaterialTheme.typography.headlineSmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = t("Impara nuove parole dalla sezione Lessico!"),
                                style = MaterialTheme.typography.bodyLarge,
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = onVaiANuove,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale())
                            ) { Text(t("Impara nuove parole")) }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = {
                                    vm.nuovaSessione()
                                    onIndietro()
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(AltezzaBottonePrincipale())
                            ) { Text(t("Chiudi")) }
                        } else {
                        Text(
                            text = t("Fatto!"),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            text = when {
                                sessionState.sbagliate.isEmpty() -> t("Perfetto! Tutte le parole consolidate.")
                                sessionState.sbagliate.size <= 2 -> t("Quasi perfetto. Rivedi le ${sessionState.sbagliate.size} parole segnalate.")
                                else -> t("Continua a ripassare: la memoria si rafforza con la pratica.")
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(Modifier.height(12.dp))
                        Text(
                            text = t("${sessionState.totale} parole ripassate"),
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (sessionState.sbagliate.isNotEmpty()) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "Da rivedere: ${sessionState.sbagliate.joinToString(", ")}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error,
                                textAlign = TextAlign.Center
                            )
                        }
                        Spacer(Modifier.height(32.dp))
                        Button(
                            onClick = {
                                vm.nuovaSessione()
                                onIndietro()
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(AltezzaBottonePrincipale())
                        ) { Text(t("Chiudi")) }
                        }
                    }
                }
            } else {
                // fronte — card centrata verticalmente nello spazio disponibile
                Spacer(Modifier.weight(1f))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = ParolaTedesca.conArticolo(carta.article, carta.german),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.SemiBold,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(Modifier.size(12.dp))
                            IconButton(onClick = { ttsHelper.speak(ParolaTedesca.conArticolo(carta.article, carta.german)) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = t("Ascolta pronuncia"),
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        carta.pronunciation?.let {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = "[ $it ]",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                // retro: appare subito, niente animazione che fa aspettare
                if (sessionState.rispostaMostrata) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = carta.italian,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (carta.example.isNotBlank()) {
                            Spacer(Modifier.height(14.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = carta.example,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                                    textAlign = TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { ttsHelper.speak(carta.example) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = t("Ascolta frase"),
                                        modifier = Modifier.size(20.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                if (!sessionState.rispostaMostrata) {
                    Button(
                        onClick = { vm.rivela() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                    ) { Text(t("Mostra la risposta")) }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        OutlinedButton(
                            onClick = { vm.rispondi(false) },
                            modifier = Modifier
                                .weight(1f)
                                .height(AltezzaBottonePrincipale())
                        ) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(t("Non lo so"))
                        }
                        Button(
                            onClick = { vm.rispondi(true) },
                            modifier = Modifier
                                .weight(1f)
                                .height(AltezzaBottonePrincipale()),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            Icon(
                                Icons.Filled.Done,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(Modifier.size(8.dp))
                            Text(t("Lo so"))
                        }
                    }
                }
            }
            }

    }
}