package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.data.local.ProviderAI

/**
 * Componenti condivisi per configurare l'AI.
 *
 * Sono usati sia nell'onboarding (primo profilo) sia nelle impostazioni, così
 * la scelta di provider, chiave e modello è identica nei due punti.
 */

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProviderAIChips(
    provider: ProviderAI,
    onProvider: (ProviderAI) -> Unit
) {
    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        ProviderAI.entries.forEach { p ->
            FilterChip(
                selected = p == provider,
                onClick = { onProvider(p) },
                label = { Text(p.nome) }
            )
        }
    }
}

@Composable
fun ChiaveAIField(
    provider: ProviderAI,
    valore: String,
    onValore: (String) -> Unit
) {
    OutlinedTextField(
        value = valore,
        onValueChange = onValore,
        label = { Text(t("Chiave ${provider.nome}")) },
        placeholder = { Text(provider.chiaveSuggerita) },
        singleLine = true,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun ModelloAIField(
    provider: ProviderAI,
    valore: String,
    onValore: (String) -> Unit
) {
    OutlinedTextField(
        value = valore,
        onValueChange = onValore,
        label = { Text(t("Modello (facoltativo)")) },
        placeholder = { Text(provider.modelloPredefinito) },
        singleLine = true,
        modifier = Modifier.fillMaxWidth()
    )
    Spacer(Modifier.height(4.dp))
    Text(
        "Vuoto = ${provider.modelloPredefinito}. " +
            "Suggeriti: ${provider.modelliSuggeriti.joinToString(", ")}",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun NotaProviderAI(provider: ProviderAI) {
    Text(
        "La chiave si crea su ${provider.urlChiave}. Resta sul dispositivo e viene " +
            "inviata solo al provider scelto quando usi le funzioni AI.",
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}
