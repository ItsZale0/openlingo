package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Bottone push-to-talk per conversazione vocale.
 *
 * L'utente tiene premuto il bottone per parlare, rilascia per inviare.
 * Mostra lo stato visivo: in ascolto (pulsante rosso che pulsa), in elaborazione, in riproduzione.
 *
 * @param voiceHelper l'istanza di VoiceHelper
 * @param onResult callback inviata con il testo riconosciuto
 * @param modifier modifier Compose
 * @param enabled se false il bottone è disabilitato
 */
@Composable
fun PushToTalkButton(
    voiceHelper: VoiceHelper,
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val state by remember { derivedStateOf { voiceHelper.state } }
    val partialText by remember { derivedStateOf { voiceHelper.partialText } }
    val errorMessage by remember { derivedStateOf { voiceHelper.errorMessage } }

    // Colore del bottone in base allo stato (senza animazione)
    val backgroundColor = when (state) {
        VoiceState.LISTENING -> MaterialTheme.colorScheme.error
        VoiceState.PROCESSING -> MaterialTheme.colorScheme.tertiary
        VoiceState.SPEAKING -> MaterialTheme.colorScheme.primary
        VoiceState.ERROR -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }

    val contentColor = when (state) {
        VoiceState.LISTENING -> MaterialTheme.colorScheme.onError
        VoiceState.ERROR -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }

    // Gestione press-and-hold
    Box(
        modifier = modifier
            .size(64.dp)
            .background(backgroundColor, CircleShape)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                detectTapGestures(
                    onPress = {
                        // Premuto: inizia ascolto
                        voiceHelper.startListening(onResult = onResult)
                        // Aspetta il rilascio
                        tryAwaitRelease()
                        // Rilascio: ferma ascolto
                        voiceHelper.stopListening()
                    }
                )
            }
            .semantics {
                contentDescription = when (state) {
                    VoiceState.LISTENING -> t("In ascolto... rilascia per inviare")
                    VoiceState.PROCESSING -> t("Elaborazione in corso...")
                    VoiceState.SPEAKING -> t("Sto parlando...")
                    VoiceState.ERROR -> t("Errore") + ": ${errorMessage ?: t("sconosciuto")}"
                    else -> t("Tieni premuto per parlare")
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (state == VoiceState.LISTENING || state == VoiceState.PROCESSING) {
                Icons.Filled.Stop
            } else {
                Icons.Filled.Mic
            },
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(28.dp)
        )
    }

    // Testo parziale durante l'ascolto
    if (state == VoiceState.LISTENING && partialText.isNotBlank()) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = partialText,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }

    // Messaggio di errore
    if (state == VoiceState.ERROR && errorMessage != null) {
        Spacer(Modifier.height(8.dp))
        Text(
            text = errorMessage ?: "",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Barra vocale completa con bottone push-to-talk e indicatore di stato.
 * Da usare nelle schermate di chat (Tutor, Roleplay).
 */
@Composable
fun VoiceInputBar(
    voiceHelper: VoiceHelper,
    onResult: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    hint: String = t("Tieni premuto per parlare")
) {
    val state by remember { derivedStateOf { voiceHelper.state } }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Indicatore di stato testuale
        val statusText = when (state) {
            VoiceState.IDLE -> hint
            VoiceState.LISTENING -> t("Ascolto in corso...")
            VoiceState.PROCESSING -> t("Elaborazione...")
            VoiceState.SPEAKING -> t("Sto parlando...")
            VoiceState.ERROR -> t("Errore")
        }

        Text(
            text = statusText,
            style = MaterialTheme.typography.labelMedium,
            color = when (state) {
                VoiceState.LISTENING -> MaterialTheme.colorScheme.error
                VoiceState.ERROR -> MaterialTheme.colorScheme.error
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            },
            modifier = Modifier.padding(bottom = 8.dp)
        )

        PushToTalkButton(
            voiceHelper = voiceHelper,
            onResult = onResult,
            enabled = enabled
        )
    }
}
