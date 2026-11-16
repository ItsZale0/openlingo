@file:OptIn(ExperimentalMaterial3Api::class)

package com.alessandro.openlingo.ui

import com.alessandro.openlingo.i18n.t

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

/**
 * Testo degli esercizi: con un long-press su una parola si apre il dialog
 * "Aggiungi al vocabolario": l'AI estrae articolo/significato/esempio e la
 * parola entra nel lessico e nel ripasso (dopo conferma).
 */
@Composable
fun TestoConAggiungiParola(
    testo: String,
    vm: OpenLingoViewModel,
    style: TextStyle = MaterialTheme.typography.bodyLarge,
    modifier: Modifier = Modifier
) {
    var layoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    var parolaSelezionata by remember { mutableStateOf("") }
    var dialogRichiesto by remember { mutableStateOf(false) }
    var estratta by remember { mutableStateOf<OpenLingoViewModel.ParolaEstratta?>(null) }
    var errore by remember { mutableStateOf<String?>(null) }
    var caricamento by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    fun avviaAnalisi(parola: String) {
        parolaSelezionata = parola
        dialogRichiesto = true
        caricamento = true
        errore = null
        estratta = null
        scope.launch {
            vm.analizzaEAggiungiParola(parola)
                .onSuccess { estratta = it }
                .onFailure { errore = it.message ?: t("Errore") }
            caricamento = false
        }
    }

    Text(
        testo,
        style = style,
        modifier = modifier.pointerInput(testo) {
            detectTapGestures(
                onLongPress = { offset ->
                    layoutResult?.let { lr ->
                        val pos = lr.getOffsetForPosition(offset).coerceIn(0, testo.length - 1)
                        val range = lr.getWordBoundary(pos)
                        val parola = testo.substring(range.start, range.end).trim { it.isWhitespace() || it in ".,!?;:()\"«»" }
                        if (parola.isNotEmpty()) avviaAnalisi(parola)
                    }
                }
            )
        },
        onTextLayout = { layoutResult = it }
    )

    if (dialogRichiesto) {
        DialogAggiuntaParola(
            parolaSelezionata = parolaSelezionata,
            caricamento = caricamento,
            errore = errore,
            estratta = estratta,
            onConferma = { p -> vm.confermaParolaEstratta(p); dialogRichiesto = false },
            onChiudi = { dialogRichiesto = false }
        )
    }
}

/**
 * Variante con parola gia' nota: bottone "Aggiungi" accanto al testo, utile
 * quando la UI conosce gia' la parola (es. opzioni di un quiz).
 */
@Composable
fun AggiungiParolaButton(
    parola: String,
    vm: OpenLingoViewModel,
    label: String = t("Aggiungi al vocabolario")
) {
    var dialogRichiesto by remember { mutableStateOf(false) }
    var estratta by remember { mutableStateOf<OpenLingoViewModel.ParolaEstratta?>(null) }
    var errore by remember { mutableStateOf<String?>(null) }
    var caricamento by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    OutlinedButton(onClick = {
        dialogRichiesto = true
        caricamento = true
        errore = null
        estratta = null
        scope.launch {
            vm.analizzaEAggiungiParola(parola)
                .onSuccess { estratta = it }
                .onFailure { errore = it.message ?: t("Errore") }
            caricamento = false
        }
    }) {
        Icon(Icons.Filled.Add, contentDescription = null)
        Text(" $label")
    }

    if (dialogRichiesto) {
        DialogAggiuntaParola(
            parolaSelezionata = parola,
            caricamento = caricamento,
            errore = errore,
            estratta = estratta,
            onConferma = { p -> vm.confermaParolaEstratta(p); dialogRichiesto = false },
            onChiudi = { dialogRichiesto = false }
        )
    }
}

@Composable
private fun DialogAggiuntaParola(
    parolaSelezionata: String,
    caricamento: Boolean,
    errore: String?,
    estratta: OpenLingoViewModel.ParolaEstratta?,
    onConferma: (OpenLingoViewModel.ParolaEstratta) -> Unit,
    onChiudi: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onChiudi,
        title = { Text(t("Aggiungi \"$parolaSelezionata\" al lessico")) },
        text = {
            when {
                caricamento -> {
                    CircularProgressIndicator()
                    Text(t("L'AI sta cercando articolo e significato..."))
                }
                errore != null -> Text(t("Errore: ") + errore)
                estratta != null -> {
                    val p = estratta
                    Text(
                        buildString {
                            append(p.articolo?.let { "$it " } ?: "")
                            append(p.tedesco).append("\n")
                            append(p.italiano).append("\n")
                            if (p.esempio.isNotBlank()) append("\"${p.esempio}\"\n")
                            append("[${p.livello}]")
                        }
                    )
                }
            }
        },
        confirmButton = {
            Button(enabled = estratta != null, onClick = { estratta?.let(onConferma) }) {
                Text(t("Aggiungi e ripassa"))
            }
        },
        dismissButton = {
            TextButton(onClick = onChiudi) { Text(t("Annulla")) }
        }
    )
}
