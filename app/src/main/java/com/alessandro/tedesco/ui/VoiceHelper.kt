package com.alessandro.tedesco.ui

import com.alessandro.tedesco.i18n.t

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Locale

/**
 * Stati del flusso vocale push-to-talk.
 */
enum class VoiceState {
    IDLE,           // In attesa
    LISTENING,      // In ascolto
    PROCESSING,     // Elaborazione risposta
    SPEAKING,       // TTS in riproduzione
    ERROR           // Errore
}

/**
 * Helper per la conversazione vocale push-to-talk.
 *
 * Gestisce il ciclo completo: ascolto -> riconoscimento -> invio al tutor -> risposta TTS.
 * Usa SpeechRecognizer (riconoscimento vocale offline/online di Android) e TextToSpeech.
 *
 * Uso:
 *   val voiceHelper = remember { VoiceHelper(context) }
 *   PushToTalkButton(voiceHelper = voiceHelper, onResult = { text -> ... })
 */
class VoiceHelper(private val context: Context) {

    var state by mutableStateOf(VoiceState.IDLE)
        private set

    var partialText by mutableStateOf("")
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    private var speechRecognizer: SpeechRecognizer? = null
    private var tts: TextToSpeech? = null
    private var ttsReady = false
    private var pendingText: String? = null
    private var onResultCallback: ((String) -> Unit)? = null
    private var onStateChange: ((VoiceState) -> Unit)? = null

    // NOTA: il motore TTS non viene piu' creato nel costruttore. Prima bastava
    // aprire la schermata vocale per istanziare un TextToSpeech (collegamento al
    // servizio + caricamento della lingua). Ora nasce alla prima riproduzione.

    private fun initTts() {
        if (tts != null) return
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.GERMAN
                ttsReady = true
                // Se c'era un testo in attesa, ora può essere riprodotto
                pendingText?.let {
                    pendingText = null
                    doSpeak(it)
                }
            }
        }
    }

    /**
     * Avvia l'ascolto vocale.
     * @param onResult callback inviata con il testo riconosciuto
     * @param onStateChange callback opzionale per cambi di stato
     */
    fun startListening(
        onResult: (String) -> Unit,
        onStateChange: ((VoiceState) -> Unit)? = null
    ) {
        if (state == VoiceState.LISTENING) return

        // Reset stato
        errorMessage = null
        partialText = ""
        onResultCallback = onResult
        this.onStateChange = onStateChange

        // Verifica disponibilità SpeechRecognizer
        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            state = VoiceState.ERROR
            errorMessage = t("Riconoscimento vocale non disponibile su questo dispositivo")
            onStateChange?.invoke(state)
            return
        }

        // Crea e configura il recognizer
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    state = VoiceState.LISTENING
                    partialText = ""
                    errorMessage = null
                    onStateChange?.invoke(state)
                }

                override fun onBeginningOfSpeech() {
                    state = VoiceState.LISTENING
                    onStateChange?.invoke(state)
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Potrebbe essere usato per animazione volume
                }

                override fun onBufferReceived(buffer: ByteArray?) {}

                override fun onEndOfSpeech() {
                    // L'utente ha smesso di parlare
                }

                override fun onError(error: Int) {
                    state = VoiceState.ERROR
                    errorMessage = when (error) {
                        SpeechRecognizer.ERROR_AUDIO -> t("Errore audio")
                        SpeechRecognizer.ERROR_CLIENT -> t("Errore client")
                        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> t("Permesso microfono negato")
                        SpeechRecognizer.ERROR_NETWORK -> t("Errore di rete")
                        SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> t("Timeout di rete")
                        SpeechRecognizer.ERROR_NO_MATCH -> t("Non ho capito, riprova")
                        SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> t("Riconoscitore occupato")
                        SpeechRecognizer.ERROR_SERVER -> t("Errore del server")
                        SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> t("Nessun suono rilevato")
                        else -> t("Errore sconosciuto ($error)")
                    }
                    onStateChange?.invoke(state)
                }

                override fun onResults(results: Bundle?) {
                    val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognized = matches?.firstOrNull()?.trim().orEmpty()
                    if (recognized.isNotEmpty()) {
                        partialText = recognized
                        state = VoiceState.PROCESSING
                        onStateChange?.invoke(state)
                        onResultCallback?.invoke(recognized)
                    } else {
                        state = VoiceState.ERROR
                        errorMessage = t("Non ho capito, riprova")
                        onStateChange?.invoke(state)
                    }
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        ?.firstOrNull()?.trim().orEmpty()
                    if (partial.isNotEmpty()) {
                        partialText = partial
                    }
                }

                override fun onEvent(eventType: Int, params: Bundle?) {}
            })

            // Intent di riconoscimento
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, "de-DE")
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1)
            }

            startListening(intent)
        }
    }

    /**
     * Ferma l'ascolto vocale.
     */
    fun stopListening() {
        speechRecognizer?.stopListening()
        state = VoiceState.IDLE
        onStateChange?.invoke(state)
    }

    /**
     * Ferma tutto: ascolto e TTS.
     */
    fun stopAll() {
        speechRecognizer?.stopListening()
        tts?.stop()
        state = VoiceState.IDLE
        partialText = ""
        onStateChange?.invoke(state)
    }

    /**
     * Parla il testo dato (TTS in tedesco).
     * Se TTS non è ancora pronto, il testo viene messo in coda e riprodotto appena possibile.
     */
    fun speak(text: String) {
        if (text.isBlank()) return

        if (ttsReady) {
            doSpeak(text)
        } else {
            // TTS non ancora pronto: salva il testo e riproduci quando pronto
            pendingText = text
            // Assicurati che TTS sia inizializzato
            initTts()
        }
    }

    private fun doSpeak(text: String) {
        if (!ttsReady) return

        tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
            override fun onStart(utteranceId: String?) {
                state = VoiceState.SPEAKING
                onStateChange?.invoke(state)
            }

            override fun onDone(utteranceId: String?) {
                state = VoiceState.IDLE
                onStateChange?.invoke(state)
            }

            override fun onError(utteranceId: String?) {
                state = VoiceState.IDLE
                onStateChange?.invoke(state)
            }
        })

        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "voice_${System.currentTimeMillis()}")
    }

    /**
     * Rilascia le risorse.
     */
    fun destroy() {
        speechRecognizer?.destroy()
        speechRecognizer = null
        tts?.stop()
        tts?.shutdown()
        tts = null
        ttsReady = false
        pendingText = null
        state = VoiceState.IDLE
    }
}