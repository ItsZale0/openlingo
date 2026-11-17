package com.alessandro.openlingo.ui

import com.alessandro.openlingo.i18n.t

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * FAB globale: evidenzia/copia una parola da QUALSIASI schermata (o scrivila)
 * e l'AI la estrae, la traduce e la aggiunge al lessico e al ripasso.
 * Funziona in tutta l'app, non solo negli esercizi.
 */
@Composable
fun AggiungiParolaFab(vm: OpenLingoViewModel, modifier: Modifier = Modifier) {
    var aperto by remember { mutableStateOf(false) }
    var testo by remember { mutableStateOf("") }
    var estratta by remember { mutableStateOf<OpenLingoViewModel.ParolaEstratta?>(null) }
    var errore by remember { mutableStateOf<String?>(null) }
    var caricamento by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    FloatingActionButton(
        modifier = modifier,
        onClick = {
            aperto = true
            testo = ""
            estratta = null
            errore = null
        },
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Icon(Icons.Filled.Add, contentDescription = t("Aggiungi parola al vocabolario"))
    }

    if (aperto) {
        AlertDialog(
            onDismissRequest = { aperto = false },
            title = { Text(t("Aggiungi una parola al lessico")) },
            text = {
                Column {
                    Text(
                        t("Scrivi o incolla una parola tedesca (anche tradotta): l'AI trovera' articolo, significato ed esempio."),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.padding(top = 8.dp))
                    OutlinedTextField(
                        value = testo,
                        onValueChange = { testo = it },
                        label = { Text(t("Parola da aggiungere")) },
                        placeholder = { Text(t("es. der Tisch / la sedia / schön")) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !caricamento
                    )
                    if (caricamento) {
                        Spacer(Modifier.padding(top = 8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator()
                            Spacer(Modifier.width(10.dp))
                            Text(t("L'AI sta cercando articolo e significato..."))
                        }
                    }
                    if (errore != null) {
                        Spacer(Modifier.padding(top = 8.dp))
                        Text(t("Errore: ") + errore.orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                    if (estratta != null) {
                        val p = estratta!!
                        Spacer(Modifier.padding(top = 8.dp))
                        Text(
                            buildString {
                                append(p.articolo?.let { "$it " } ?: "")
                                append(p.tedesco).append("\n")
                                append(p.italiano).append("\n")
                                if (p.esempio.isNotBlank()) append("\"${p.esempio}\"\n")
                                append("[${p.livello}]")
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    enabled = !caricamento && (estratta != null || testo.isNotBlank()),
                    onClick = {
                        if (estratta != null) {
                            vm.confermaParolaEstratta(estratta!!)
                            aperto = false
                        } else {
                            caricamento = true
                            errore = null
                            scope.launch {
                                vm.analizzaEAggiungiParola(testo.trim())
                                    .onSuccess { estratta = it }
                                    .onFailure { errore = it.message ?: t("Errore") }
                                caricamento = false
                            }
                        }
                    }
                ) {
                    Text(if (estratta != null) t("Aggiungi e ripassa") else t("Cerca con AI"))
                }
            },
            dismissButton = {
                TextButton(onClick = { aperto = false }) { Text(t("Annulla")) }
            }
        )
    }
}
