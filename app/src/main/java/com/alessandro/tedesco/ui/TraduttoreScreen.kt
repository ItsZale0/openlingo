package com.alessandro.tedesco.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.alessandro.tedesco.ui.theme.dimensioneContenuto
import com.alessandro.tedesco.ui.theme.spaziaturaSchermo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.IOException
import java.net.URLEncoder
import java.util.Locale
import com.alessandro.tedesco.ui.theme.Spaziature
import com.alessandro.tedesco.ui.theme.AltezzaBottonePrincipale
import com.alessandro.tedesco.ui.theme.TitoloSchermata

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TraduttoreScreen(vm: TedescoViewModel) {
    val scope = rememberCoroutineScope()
    var testo by remember { mutableStateOf("") }
    var traduzione by remember { mutableStateOf("") }
    var caricamento by remember { mutableStateOf(false) }
    var errore by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val ttsHelperTedesco = rememberTtsHelper(context, Locale.GERMAN)
    val ttsHelperItaliano = rememberTtsHelper(context, Locale.ITALIAN)

    ScreenScaffold("Traduttore") {

            Column(
                modifier = Modifier.widthIn(max = dimensioneContenuto()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    "Traduci parole e frasi dal tedesco all'italiano. Ogni parola è un tassello!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value = testo,
                    onValueChange = { testo = it },
                    label = { Text("Testo in tedesco") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    trailingIcon = {
                        if (testo.isNotBlank()) {
                            IconButton(onClick = { ttsHelperTedesco.speak(testo) }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Ascolta testo tedesco",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                )

                Button(
                    onClick = {
                        if (testo.isNotBlank()) {
                            caricamento = true
                            errore = null
                            scope.launch {
                                try {
                                    val risultato = withContext(Dispatchers.IO) {
                                        traduci(testo)
                                    }
                                    traduzione = risultato
                                } catch (e: Exception) {
                                    errore = "Errore: ${e.message}"
                                } finally {
                                    caricamento = false
                                }
                            }
                        }
                    },
                    enabled = testo.isNotBlank() && !caricamento,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Translate, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (caricamento) "Traduzione..." else "Traduci")
                }

                if (errore != null) {
                    Text(
                        errore ?: "",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall
                    )
                }

                if (traduzione.isNotBlank()) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    "Traduzione",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                                IconButton(onClick = { ttsHelperItaliano.speak(traduzione) }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Ascolta traduzione",
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                }
                            }
                            Spacer(Modifier.height(4.dp))
                            Text(
                                traduzione,
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }

                    Button(
                        onClick = {
                            vm.aggiungiParolaCustom(
                                german = testo,
                                italian = traduzione,
                                example = "",
                                article = null,
                                pronunciation = null,
                                lesson = 0,
                                tags = "tradotto"
                            )
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Aggiungi al vocabolario")
                    }
                }

                Spacer(Modifier.height(32.dp))
            }

    }
}

private fun traduci(testo: String): String {
    val url = "https://api.mymemory.translated.net/get?q=" +
            URLEncoder.encode(testo, "UTF-8") + "&langpair=de|it"
    val risultato = java.net.URL(url).readText()
    val json = kotlinx.serialization.json.Json.parseToJsonElement(risultato).jsonObject
    val translated = json["responseData"]?.jsonObject?.get("translatedText")?.jsonPrimitive?.content
        ?: throw IOException("Traduzione non disponibile")
    // MyMemory a volte risponde con "MYMEMORY WARNING: QUERY LENGTH LIMIT..."
    if (translated.startsWith("MYMEMORY WARNING") || translated.startsWith("QUERY LENGTH LIMIT")) {
        throw IOException("Limite di traduzione raggiunto, riprova più tardi")
    }
    return translated
}
