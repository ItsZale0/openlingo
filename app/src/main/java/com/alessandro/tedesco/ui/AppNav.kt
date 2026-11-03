package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import com.alessandro.tedesco.BuildConfig
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.BackupEsecutore
import com.alessandro.tedesco.data.BackupSettings
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.alessandro.tedesco.data.remote.BackupViewModel
import com.alessandro.tedesco.data.remote.UpdaterViewModel

/**
 * V2 - una sola schermata principale (Studio Veloce).
 * Impostazioni, progresso, tutor e backup vivono in un menu nascosto e sono
 * raggiungili da qui: nessuna bottom bar, nessuna navigazione in vista.
 *
 * Sopra il NavHost vivono due overlay sempre disponibili:
 * - TutorBubble: il tutor come bolla laterale (stile Meta AI su WhatsApp)
 * - il banner di aggiornamento, dentro la schermata principale
 */
@Composable
fun AppNav(
    modifier: Modifier = Modifier
) {
    val app = LocalContext.current.applicationContext as TedescoApp
    val factory = remember { ViewModelProvider.AndroidViewModelFactory(app) }
    val vm = viewModel<TedescoViewModel>(factory = factory)
    val updater = viewModel<UpdaterViewModel>(factory = factory)

    // Auto-update: un controllo all'apertura e poi al massimo ogni 6 ore quando
    // l'app torna in primo piano. Prima ogni ON_RESUME faceva una richiesta HTTP.
    LaunchedEffect(Unit) {
        updater.checkForUpdateSeNecessario()
    }
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val osservatore = LifecycleEventObserver { _, evento ->
            if (evento == Lifecycle.Event.ON_RESUME) updater.checkForUpdateSeNecessario()
        }
        lifecycleOwner.lifecycle.addObserver(osservatore)
        onDispose { lifecycleOwner.lifecycle.removeObserver(osservatore) }
    }

    // Backup automatico in-app: ogni 60s se abilitato (hash-dedup evita upload inutili).
    // In background il BackupWorker (WorkManager) gestisce il 15-min.
    val appContext = LocalContext.current.applicationContext as TedescoApp
    val backupSettings = remember { BackupSettings(appContext) }
    val autoBackup by backupSettings.autoFlow.collectAsStateWithLifecycle(false)
    LaunchedEffect(autoBackup) {
        if (!autoBackup) return@LaunchedEffect
        while (isActive) {
            BackupEsecutore.esegui(appContext, BuildConfig.VERSION_NAME, forza = false)
            delay(60_000)
        }
    }

    val nav = rememberNavController()

    Box(modifier) {
        NavHost(
            navController = nav,
            startDestination = "studio",
            enterTransition = { fadeIn(animationSpec = tween(200)) },
            exitTransition = { fadeOut(animationSpec = tween(200)) },
            popEnterTransition = { fadeIn(animationSpec = tween(200)) },
            popExitTransition = { fadeOut(animationSpec = tween(200)) }
        ) {
            composable("studio") {
                StudioVeloceScreen(
                    vm = vm,
                    updater = updater,
                    onVaiAEsercizi = { nav.navigate("esercizi") },
                    onVaiAImpostazioni = { nav.navigate("impostazioni") },
                    onVaiAProgresso = { nav.navigate("progresso") },
                    onVaiATutor = { nav.navigate("tutor") },
                    onVaiABackup = { nav.navigate("backup") },
                    onVaiAPercorsoGrammatica = { nav.navigate("percorso_grammatica") },
                    onVaiANuoveParole = { nav.navigate("nuove_parole") },
                    onVaiAImmersione = { nav.navigate("immersione") },
                    onNaviga = { route -> nav.navigate(route) }
                )
            }
            composable("percorso_grammatica") {
                PercorsoGrammaticaScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() },
                    onApriUnita = { id -> nav.navigate("unita_grammatica/$id") }
                )
            }
            composable("unita_grammatica/{id}") { entry ->
                UnitaGrammaticaScreen(
                    vm = vm,
                    unitaId = entry.arguments?.getString("id").orEmpty(),
                    onIndietro = { nav.popBackStack() },
                    onCompletata = { nav.popBackStack() }
                )
            }
            composable("immersione") {
                DidatticaScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() },
                    onVaiAProduzione = { nav.navigate("produzione") },
                    onVaiAUnitaGrammatica = { id -> nav.navigate("unita_grammatica/$id") }
                )
            }
            composable("esercizi") {
                EserciziScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() },
                    onVaiA = { route -> nav.navigate(route) }
                )
            }
            composable("grammatica") {
                GrammaticaScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("comprensione") {
                ComprensioneScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            // Produzione = sezione di SCRITTURA: si scrive un testo e lo corregge il tutor
            // (criteri Goethe). Il quiz a scelta multipla è "espressioni", più sotto.
            composable("produzione") {
                val profiloAttivo by vm.profiloAttivo.collectAsStateWithLifecycle()
                ProduzioneScrittaScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() },
                    profilo = profiloAttivo,
                    onVaiACorrezioni = { nav.navigate("tutor") }
                )
            }
            composable("espressioni") {
                ProduzioneScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("verifica") {
                VerificaScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("ascolto") {
                AscoltoScreen(vm = vm, onFatto = { nav.popBackStack() })
            }
            composable("ripasso") {
                RipassoScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() },
                    onVaiANuove = { nav.navigate("nuove_parole") }
                )
            }
            composable("nuove_parole") {
                NuoveParoleScreen(
                    vm = vm,
                    onIndietro = { nav.popBackStack() }
                )
            }
            composable("produzione_attiva") {
                ProduzioneAttivaScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("errori") {
                QuadernoErroriScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("pronuncia") {
                PronunciaScreen(onIndietro = { nav.popBackStack() })
            }
            composable("sprechuebungen") {
                SprechuebungenScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("sudtirol") {
                SudtirolScreen(onIndietro = { nav.popBackStack() })
            }
            composable("podcast") {
                PodcastScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("wortliste") {
                WortlisteScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("dialoghi") {
                DialoghiScreen(onIndietro = { nav.popBackStack() })
            }
            composable("progresso") {
                ProgressoScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("tutor") {
                TutorChatScreen(vm = vm, onIndietro = { nav.popBackStack() })
            }
            composable("impostazioni") {
                ImpostazioniScreen(
                    vm = vm,
                    updater = updater,
                    onVaiABackup = { nav.navigate("backup") },
                    onIndietro = { nav.popBackStack() }
                )
            }
            composable("backup") {
                BackupScreen(
                    vm = viewModel<BackupViewModel>(factory = factory),
                    onIndietro = { nav.popBackStack() }
                )
            }
        }

        // Bolla del tutor: sopra ogni schermata, come Meta AI su WhatsApp
        TutorBubble(vm = vm)

        // Popup aggiornamenti: sovraimposto a tutto, non scorre con il contenuto
        UpdateOverlay(updater = updater)
    }
}
