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
import com.alessandro.tedesco.sync.TemaSettings
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
            // Material 3: dynamic color su Android 12+, altrimenti schema baseline.
            // App pensata dark: per seguire il sistema basta togliere darkTheme = true.
            TedescoTheme(darkTheme = true, temaColore = temaColore) {
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
