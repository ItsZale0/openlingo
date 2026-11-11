package com.alessandro.openlingo.data.remote

import com.alessandro.openlingo.data.local.WordEntity
import com.alessandro.openlingo.data.local.WordSource
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Formato del file JSON pubblicato dall'insegnante.
 * UTF-8 obbligatorio: contiene umlaut (ä ö ü) e la ß.
 */
@Serializable
data class FeedDto(
    val version: Int = 1,
    val generatedAt: String = "",
    val guida: GuidaDto? = null,
    val words: List<WordDto> = emptyList(),
    val lessons: List<LessonDto> = emptyList(),
    val lezioneCorrente: Int = 1,
    val piano: PianoDto? = null,
    val progresso: ProgressoDto? = null,
    val sessioni: List<SessioneDto> = emptyList()
)

/** Piano di studio verso il B2, con tappe e certificazioni. */
@Serializable
data class PianoDto(
    val obiettivo: String = "",
    val orizzonte: String = "",
    val tappe: List<TappaDto> = emptyList(),
    val certificazioni: List<CertificazioneDto> = emptyList(),
    val risorse: List<RisorsaDto> = emptyList()
)

@Serializable
data class TappaDto(
    val nome: String = "",
    val descrizione: String = "",
    val lezioni: String = "",
    val stato: String = ""
)

@Serializable
data class CertificazioneDto(
    val nome: String = "",
    val ente: String = "",
    val livello: String = "",
    val note: String = "",
    val url: String = ""
)

@Serializable
data class RisorsaDto(
    val nome: String = "",
    val tipo: String = "",
    val nota: String = ""
)

/** Streak e contatori correnti. */
@Serializable
data class ProgressoDto(
    val lezioneCorrente: Int = 1,
    val streakCorrente: Int = 0,
    val streakRecord: Int = 0,
    val totaleFatte: Int = 0,
    val paroleTotali: Int = 0,
    val paroleMature: Int = 0
)

/** Tipo di sessione strutturata. */
@Serializable
data class SessioneDto(
    val tipo: String = "",
    val titolo: String = "",
    val descrizione: String = "",
    val durata: Int = 0
)

@Serializable
data class GuidaDto(
    val titolo: String = "",
    val docUrl: String = "",
    val sezioni: List<SezioneDto> = emptyList()
)

@Serializable
data class SezioneDto(
    val titolo: String = "",
    val testo: String = ""
)

@Serializable
data class LessonDto(
    val numero: Int = 1,
    val titolo: String = "",
    val data: String = "",
    val pdfUrl: String = "",
    val contenuto: String = "",
    val audioUrl: String = ""
)

@Serializable
data class WordDto(
    val id: String,
    val german: String,
    val italian: String,
    val example: String = "",
    val article: String? = null,
    val pronunciation: String? = null,
    val level: String = "A1",
    val lesson: Int = 1,
    val tags: List<String> = emptyList(),
    val archived: Boolean = false
)

/**
 * Converte una parola del feed in entita' locale.
 *
 * Se la parola esiste gia' si conservano `createdAt` e `source`: cosi' il
 * confronto "cambiata si'/no" nel merge guarda solo i contenuti veri e non
 * risulta sempre diverso per via del timestamp.
 */
fun WordDto.toEntity(now: Long, precedente: WordEntity? = null): WordEntity = WordEntity(
    id = id,
    german = german,
    italian = italian,
    example = example,
    article = article,
    pronunciation = pronunciation,
    level = level,
    lesson = lesson,
    tags = tags.joinToString(","),
    archived = archived,
    createdAt = precedente?.createdAt ?: now,
    source = precedente?.source ?: WordSource.SYNCED
)
