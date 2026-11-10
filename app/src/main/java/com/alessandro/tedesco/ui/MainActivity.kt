package com.alessandro.tedesco.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.activity.ComponentActivity
import androidx.lifecycle.lifecycleScope
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.TedescoApp
import com.alessandro.tedesco.data.BackupEsecutore
import com.alessandro.tedesco.BuildConfig
import com.alessandro.tedesco.data.BackupSettings
import com.alessandro.tedesco.i18n.Traduttore
import com.alessandro.tedesco.sync.LinguaSettings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import com.alessandro.tedesco.sync.TemaSettings
import java.util.Locale
import com.alessandro.tedesco.ui.theme.TedescoTheme

class MainActivity : ComponentActivity() {
    /**
     * Il motore TTS condiviso si libera quando l'app non e' piu' in primo piano:
     * nessuna risorsa audio resta impegnata mentre l'app e' in background.
     */
    override fun onStop() {
        super.onStop()
        TtsCondiviso.chiudi()
        // Solo il flush LOCALE resta sincrono: e' veloce e garantisce i dati
        // anche se il processo viene killato subito dopo.
        runBlocking(Dispatchers.IO) {
            runCatching { TedescoApp.get(this@MainActivity).profileManagerInstance.flush() }
        }
        // Il backup su rete (MB via OkHttp) NON deve bloccare il main thread:
        // onStop arriva anche alla ROTAZIONE, e il runBlocking di prima
        // congelava lo schermo (ANR: "schermata nera" alla rotazione).
        // Il mutex di BackupEsecutore evita sovrapposizioni col timer
        // in-app e col worker di sistema.
        val app = applicationContext
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            runCatching {
                if (BackupSettings(app).autoAbilitato()) {
                    BackupEsecutore.esegui(app, BuildConfig.VERSION_NAME, forza = false)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        // Applica la lingua scelta dall'utente (IT/EN) alle risorse.
        val lingua = runCatching {
            kotlinx.coroutines.runBlocking {
                LinguaSettings(applicationContext).lingua.first()
            }
        }.getOrDefault("it")
        Traduttore.lingua = lingua
        val locale = Locale(lingua)
        Locale.setDefault(locale)
        val config = resources.configuration
        config.setLocale(locale)
        config.setLayoutDirection(locale)
        @Suppress("DEPRECATION")
        resources.updateConfiguration(config, resources.displayMetrics)
        val app = application as TedescoApp

        // Android 13+: senza questo permesso le notifiche del worker di
        // auto-update (e dei promemoria) vengono scartate in silenzio.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        setContent {
            val temaSettings = remember { TemaSettings(this@MainActivity) }
            val temaColore by temaSettings.colore.collectAsStateWithLifecycle(initialValue = "teal")
            val temaModalita by temaSettings.modalita.collectAsStateWithLifecycle(initialValue = "system")
            val dynamicColor = temaColore == "material_you"
            val darkTheme = when (temaModalita) {
                "light" -> false
                "dark" -> true
                else -> isSystemInDarkTheme()
            }
            TedescoTheme(
                darkTheme = darkTheme,
                dynamicColor = dynamicColor,
                temaColore = if (dynamicColor) "teal" else temaColore
            ) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNav()
                }
            }
        }
    }
}
