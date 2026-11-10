package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.remote.UpdateState
import com.alessandro.tedesco.data.remote.UpdaterViewModel
import com.alessandro.tedesco.ui.theme.Spaziature

@Composable
fun UpdateOverlay(updater: UpdaterViewModel) {
    val state by updater.state.collectAsStateWithLifecycle(UpdateState.Idle)

    if (state is UpdateState.Idle || state is UpdateState.UpToDate) return

    val st = state

    Dialog(
        onDismissRequest = { updater.dismiss() },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .padding(Spaziature.md),
            shape = MaterialTheme.shapes.large,
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHigh),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            Column(Modifier.padding(Spaziature.md)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                Icons.Filled.SystemUpdate,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                    Spacer(Modifier.width(Spaziature.sm))
                    Text(
                        when (st) {
                            is UpdateState.Checking -> t("Controllo aggiornamenti...")
                            is UpdateState.Available -> t("Aggiornamento disponibile") + ": v${(st as UpdateState.Available).version.versionName}"
                            is UpdateState.Downloading -> t("Download") + " ${(st as UpdateState.Downloading).progress}%"
                            is UpdateState.ReadyToInstall -> t("Aggiornamento pronto all'installazione")
                            is UpdateState.NeedPermission -> t("Serve il permesso per installare app")
                            is UpdateState.Error -> t("Errore aggiornamento")
                            else -> t("Aggiornamento")
                        },
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    if (st is UpdateState.Available || st is UpdateState.Error) {
                        IconButton(onClick = { updater.dismiss() }) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = t("Ignora"),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                when (st) {
                    is UpdateState.Checking -> {
                        LinearProgressIndicator(
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                                .height(6.dp)
                                .clip(MaterialTheme.shapes.extraSmall)
                        )
                    }
                    is UpdateState.Available -> {
                        val v = (st as UpdateState.Available).version
                        Text(
                            t("Versione") + " ${v.versionName} (${v.versionCode})",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Button(
                            onClick = { updater.downloadUpdate(v) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = MaterialTheme.shapes.large
                        ) { Text(t("Scarica e installa")) }
                        TextButton(
                            onClick = { updater.skipUpdate(v) },
                            modifier = Modifier.fillMaxWidth()
                        ) { Text(t("Salta questo aggiornamento")) }
                    }
                    is UpdateState.Downloading -> {
                        val p = (st as UpdateState.Downloading).progress
                        Column(
                            Modifier.padding(top = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            LinearProgressIndicator(
                                progress = { p / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(MaterialTheme.shapes.extraSmall)
                            )
                            Text(
                                "$p% " + t("scaricato"),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                    is UpdateState.ReadyToInstall -> {
                        val f = (st as UpdateState.ReadyToInstall).file
                        Text(
                            t("APK scaricato") + ": ${f.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                            modifier = Modifier.padding(top = 4.dp)
                        )
                        Button(
                            onClick = { updater.installUpdate(f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp),
                            shape = MaterialTheme.shapes.large
                        ) { Text(t("Installa ora")) }
                    }
                    is UpdateState.NeedPermission -> {
                        Column(Modifier.padding(top = 8.dp)) {
                            TextButton(onClick = { updater.apriImpostazioniInstallazione() }) {
                                Text(t("Apri impostazioni di installazione"))
                            }
                        }
                    }
                    is UpdateState.Error -> {
                        val msg = (st as UpdateState.Error).message
                        Text(
                            msg,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                        Row(
                            Modifier.fillMaxWidth().padding(top = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(Spaziature.sm)
                        ) {
                            TextButton(onClick = { updater.checkForUpdate() }) { Text(t("Riprova")) }
                            TextButton(onClick = { updater.dismiss() }) { Text(t("Ignora")) }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}
