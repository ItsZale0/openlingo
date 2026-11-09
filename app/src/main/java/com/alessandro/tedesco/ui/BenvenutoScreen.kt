package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.alessandro.tedesco.data.local.LivelloIniziale
import com.alessandro.tedesco.data.local.ProfiloUtente
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo

/**
 * Schermata mostrata mentre i profili vengono letti da DataStore.
 * Evita di far lampeggiare il benvenuto a chi ha già i suoi dati.
 */
@Composable
fun CaricamentoIniziale() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

/**
 * Primo avvio: non esiste alcun profilo.
 *
 * L'utente crea il proprio profilo e sceglie il livello di partenza. Il
 * contesto scritto qui (professione, interessi, obiettivi) viene usato dall'AI
 * per personalizzare lezioni, esercizi e vocabolario.
 */
@Composable
fun BenvenutoScreen(vm: TedescoViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spaziaturaSchermo(), vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = dimensioneContenuto())) {
            Text(
                "Benvenuto",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                "Crea il tuo profilo per iniziare. I dati restano sul dispositivo: " +
                    "l'app funziona anche senza connessione.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            FormNuovoProfilo(
                testoBottone = "Crea profilo e inizia",
                onCrea = { nome, livello, contesto, medico ->
                    vm.creaProfilo(nome, livello, contesto, medico)
                }
            )
        }
    }
}

/** Scelta del profilo quando l'app è avviata senza profilo attivo. */
@Composable
fun SceltaProfiloScreen(vm: TedescoViewModel) {
    val profili by vm.profiliDisponibili.collectAsStateWithLifecycle(emptyList())
    var creaNuovo by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = spaziaturaSchermo(), vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Column(modifier = Modifier.widthIn(max = dimensioneContenuto())) {
            Text(
                "Chi sta studiando?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Ogni profilo ha i suoi progressi, separati dagli altri.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            profili.forEach { profilo ->
                CardProfilo(profilo = profilo, onEntra = { vm.selezionaProfilo(profilo.id) })
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))
            if (creaNuovo) {
                FormNuovoProfilo(
                    testoBottone = "Crea profilo",
                    onCrea = { nome, livello, contesto, medico ->
                        vm.creaProfilo(nome, livello, contesto, medico)
                        creaNuovo = false
                    }
                )
            } else {
                TextButton(onClick = { creaNuovo = true }) {
                    Text("Crea un nuovo profilo")
                }
            }
        }
    }
}

@Composable
private fun CardProfilo(profilo: ProfiloUtente, onEntra: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(Modifier.weight(1f)) {
                Text(
                    profilo.config.nomeVisualizzato,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    "Livello ${profilo.config.livelloIniziale.label}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Button(onClick = onEntra) { Text("Entra") }
        }
    }
}

@Composable
private fun FormNuovoProfilo(
    testoBottone: String,
    onCrea: (nome: String, livello: LivelloIniziale, contesto: String, medico: Boolean) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var livello by remember { mutableStateOf(LivelloIniziale.A0) }
    var contesto by remember { mutableStateOf("") }
    var medico by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Come ti chiami?") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text(
            "Livello di partenza",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            LivelloIniziale.entries.forEach { l ->
                FilterChip(
                    selected = livello == l,
                    onClick = { livello = l },
                    label = { Text(l.label) }
                )
            }
        }
        Spacer(Modifier.height(12.dp))

        OutlinedTextField(
            value = contesto,
            onValueChange = { contesto = it },
            label = { Text("Contesto e obiettivi (facoltativo)") },
            placeholder = { Text("Es. elettricista: vorrei parlare con i clienti in cantiere") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            "Serve all'AI per scegliere esempi e vocabolario adatti a te. " +
                "Puoi cambiarlo quando vuoi nelle impostazioni.",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Contesto sanitario", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Esercizi e vocaboli con lessico medico/ospedaliero.",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = medico, onCheckedChange = { medico = it })
        }
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                onCrea(nome.trim(), livello, contesto.trim(), medico)
            },
            enabled = nome.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(testoBottone)
        }
    }
}
