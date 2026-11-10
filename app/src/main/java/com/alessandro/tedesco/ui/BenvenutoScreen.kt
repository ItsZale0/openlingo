package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.alessandro.tedesco.R
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
import com.alessandro.tedesco.data.local.ProviderAI
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
 * L'utente crea il proprio profilo, sceglie il livello di partenza e descrive
 * chi è e cosa vuole imparare. Quella descrizione è la base con cui l'AI
 * personalizza tutor, lezioni, esercizi e vocabolario: non serve più un
 * interruttore separato per il contesto sanitario.
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
                stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(6.dp))
            Text(
                stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(20.dp))

            FormNuovoProfilo(
                testoBottone = t("Crea profilo e inizia"),
                onCrea = { nome, livello, contesto, provider, chiavi, modello ->
                    vm.creaProfilo(
                        nome = nome,
                        livello = livello,
                        contesto = contesto,
                        providerAI = provider,
                        chiaviAI = chiavi.mapKeys { it.key.id },
                        modelloAI = modello
                    )
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
                t("Chi sta studiando?"),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text(
                t("Ogni profilo ha i suoi progressi, separati dagli altri."),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))

            profili.forEach { profilo ->
                CardProfilo(
                    profilo = profilo,
                    onEntra = { vm.selezionaProfilo(profilo.id) },
                    onElimina = { vm.eliminaProfilo(profilo.id) }
                )
                Spacer(Modifier.height(8.dp))
            }

            Spacer(Modifier.height(8.dp))
            if (creaNuovo) {
                FormNuovoProfilo(
                    testoBottone = t("Crea profilo"),
                    onCrea = { nome, livello, contesto, provider, chiavi, modello ->
                        vm.creaProfilo(
                            nome = nome,
                            livello = livello,
                            contesto = contesto,
                            providerAI = provider,
                            chiaviAI = chiavi.mapKeys { it.key.id },
                            modelloAI = modello
                        )
                        creaNuovo = false
                    }
                )
            } else {
                TextButton(onClick = { creaNuovo = true }) {
                    Text(t("Crea un nuovo profilo"))
                }
            }
        }
    }
}

@Composable
private fun CardProfilo(
    profilo: ProfiloUtente,
    onEntra: () -> Unit,
    onElimina: () -> Unit
) {
    var conferma by remember { mutableStateOf(false) }

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
                    t("Livello ${profilo.config.livelloIniziale.label}"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(onClick = { conferma = true }) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = t("Elimina profilo"),
                    tint = MaterialTheme.colorScheme.error
                )
            }
            Button(onClick = onEntra) { Text(t("Entra")) }
        }
    }

    if (conferma) {
        AlertDialog(
            onDismissRequest = { conferma = false },
            title = { Text(t("Eliminare il profilo?")) },
            text = {
                Text(
                    t("Verranno eliminati tutti i dati di \"${profilo.config.nomeVisualizzato}\": parole, ripassi, lezioni, grammatica e conversazioni. L'operazione non è reversibile.")
                )
            },
            confirmButton = {
                TextButton(onClick = { conferma = false; onElimina() }) {
                    Text(t("Elimina"), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { conferma = false }) { Text(t("Annulla")) }
            }
        )
    }
}

@Composable
private fun FormNuovoProfilo(
    testoBottone: String,
    onCrea: (
        nome: String,
        livello: LivelloIniziale,
        contesto: String,
        provider: ProviderAI,
        chiavi: Map<ProviderAI, String>,
        modello: String
    ) -> Unit
) {
    var nome by remember { mutableStateOf("") }
    var livello by remember { mutableStateOf(LivelloIniziale.A0) }
    var contesto by remember { mutableStateOf("") }

    var provider by remember { mutableStateOf(ProviderAI.PREDEFINITO) }
    var chiavi by remember { mutableStateOf<Map<ProviderAI, String>>(emptyMap()) }
    var modello by remember { mutableStateOf("") }
    var mostraAltreChiavi by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text(stringResource(R.string.what_is_your_name)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))

        Text(
            stringResource(R.string.starting_level),
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
            label = { Text(stringResource(R.string.description_label)) },
            placeholder = {
                Text(stringResource(R.string.description_hint))
            },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(4.dp))
        Text(
            t("L'AI usa questa descrizione per scegliere esempi, esercizi, lezioni e vocabolario adatti a te. Se lavori in ambito sanitario, scrivilo qui: è una personalizzazione come le altre. Puoi cambiarla quando vuoi."),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        Text(
            t("AI (facoltativa)"),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(Modifier.height(4.dp))
        Text(
            t("Scegli un provider e incolla la chiave. Senza chiave l'app funziona lo stesso: restano tutti i contenuti offline, ma niente tutor e generazione AI."),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(8.dp))

        ProviderAIChips(provider = provider, onProvider = { provider = it })
        Spacer(Modifier.height(8.dp))
        ChiaveAIField(
            provider = provider,
            valore = chiavi[provider].orEmpty(),
            onValore = { chiavi = chiavi + (provider to it) }
        )
        Spacer(Modifier.height(8.dp))
        ModelloAIField(
            provider = provider,
            valore = modello,
            onValore = { modello = it }
        )
        Spacer(Modifier.height(4.dp))
        TextButton(onClick = { mostraAltreChiavi = !mostraAltreChiavi }) {
            Text(
                if (mostraAltreChiavi) t("Nascondi le altre chiavi")
                else t("Aggiungi chiavi per altri provider")
            )
        }
        if (mostraAltreChiavi) {
            ProviderAI.entries.filter { it != provider }.forEach { altro ->
                Spacer(Modifier.height(8.dp))
                ChiaveAIField(
                    provider = altro,
                    valore = chiavi[altro].orEmpty(),
                    onValore = { chiavi = chiavi + (altro to it) }
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        NotaProviderAI(provider)
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val pulite = chiavi
                    .mapValues { (_, chiave) -> chiave.trim() }
                    .filterValues { it.isNotEmpty() }
                onCrea(nome.trim(), livello, contesto.trim(), provider, pulite, modello.trim())
            },
            enabled = nome.isNotBlank(),
            modifier = Modifier.fillMaxWidth().height(52.dp)
        ) {
            Text(testoBottone)
        }
    }
}

