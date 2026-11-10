package com.alessandro.tedesco.desktop.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.desktop.DesktopStore
import com.alessandro.tedesco.i18n.t

/**
 * Schermata mostrata quando non esiste alcun profilo (primo avvio) o quando
 * nessun profilo è attivo.
 *
 * Non viene creato niente in automatico: nome, livello e contesto li sceglie
 * chi usa l'app. Se i profili esistono già, vengono elencati per rientrare.
 */
@Composable
fun SchermataBenvenuto(store: DesktopStore) {
    val profili = store.elencoProfili

    Box(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState())
            .padding(horizontal = 32.dp, vertical = 32.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
            Text(
                t("Benvenuto"),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                t("Crea il tuo profilo per iniziare. I dati restano su questo computer: l'app funziona anche senza connessione."),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            if (profili.isNotEmpty()) {
                CardSezione(
                    titolo = t("Profili esistenti"),
                    sottotitolo = t("Ogni profilo ha progressi separati dagli altri")
                ) {
                    profili.forEach { p ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(Modifier.weight(1f)) {
                                Text(
                                    p.config.nomeVisualizzato,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                Text(
                                    "${t("Livello")} ${p.config.livelloIniziale.label} · " +
                                        "${p.stato.parole.size} ${t("parole")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(onClick = { store.attivaProfilo(p.id) }) {
                                Text(t("Entra"))
                            }
                        }
                    }
                }
                Spacer(Modifier.height(20.dp))
            }

            CardSezione(
                titolo = if (profili.isEmpty()) t("Il tuo profilo") else t("Nuovo profilo"),
                sottotitolo = t("Puoi cambiare tutto in seguito dalle impostazioni")
            ) {
                ModuloProfilo(onCrea = { nome, livello, contesto, medico ->
                    store.creaProfilo(nome, livello, contesto, medico)
                })
            }
        }
    }
}

@Composable
private fun ModuloProfilo(
    onCrea: (String, LivelloIniziale, String, Boolean) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var livello by remember { mutableStateOf(LivelloIniziale.A0) }
    var contesto by remember { mutableStateOf("") }
    var medico by remember { mutableStateOf(false) }

    Text(t("Come ti chiami?"), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = nome,
        onValueChange = { nome = it },
        singleLine = true,
        placeholder = { Text(t("Nome del profilo")) },
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(16.dp))

    Text(t("Livello di partenza"), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(6.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        LivelloIniziale.entries.forEach { l ->
            FilterChip(
                selected = livello == l,
                onClick = { livello = l },
                label = { Text(l.label) }
            )
        }
    }
    Spacer(Modifier.height(16.dp))

    Text(t("Contesto e obiettivi (facoltativo)"), style = MaterialTheme.typography.bodyMedium)
    Spacer(Modifier.height(2.dp))
    Text(
        t("Professione, interessi, cosa vuoi saper fare: l'AI sceglie esempi e vocabolario adatti a te."),
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(Modifier.height(6.dp))
    OutlinedTextField(
        value = contesto,
        onValueChange = { contesto = it },
        placeholder = { Text(t("Es. elettricista: parlare con clienti e fornitori")) },
        minLines = 3,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(14.dp))

    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(t("Contesto sanitario"), style = MaterialTheme.typography.bodyMedium)
            Text(
                t("Esercizi e vocaboli con lessico medico/ospedaliero."),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(checked = medico, onCheckedChange = { medico = it })
    }
    Spacer(Modifier.height(18.dp))

    Button(
        onClick = { onCrea(nome.trim(), livello, contesto.trim(), medico) },
        enabled = nome.isNotBlank(),
        modifier = Modifier.height(48.dp)
    ) {
        Text(t("Crea profilo e inizia"))
    }
}
