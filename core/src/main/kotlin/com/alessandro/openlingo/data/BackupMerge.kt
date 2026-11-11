package com.alessandro.openlingo.data

import com.alessandro.openlingo.data.local.EsercizioGenEntity
import com.alessandro.openlingo.data.local.FeedbackEntry
import com.alessandro.openlingo.data.local.GuidaEntity
import com.alessandro.openlingo.data.local.PianoEntity
import com.alessandro.openlingo.data.local.ProfiloStato
import com.alessandro.openlingo.data.local.ProfiloUtente
import com.alessandro.openlingo.data.local.ProfiliRepository
import com.alessandro.openlingo.data.local.ProgressoFeedEntity
import com.alessandro.openlingo.data.local.ProgressoLezione
import com.alessandro.openlingo.data.local.ProgressoUtente
import com.alessandro.openlingo.data.local.ReviewEntity
import com.alessandro.openlingo.data.local.TestLivello
import com.alessandro.openlingo.data.local.WordEntity
import kotlin.math.max

/**
 * UNISCE due fotografie dei dati (locale e backup cloud) senza perderne nessuna.
 *
 * Serve perché lo stesso account vive su più dispositivi (telefono Android e
 * desktop Windows). Prima il ripristino *sostituiva* i dati locali con quelli
 * del cloud: chi aveva ripassato o studiato grammatica/lezioni sull'altro
 * dispositivo vedeva i progressi sparire. Qui ogni dato viene confrontato
 * campo per campo e si tiene sempre la versione più avanzata:
 *
 * - lessico: unione per id (le parole mancanti non spariscono, i tag si sommano);
 * - ripasso SRS: per ogni parola vince la carta con l'ultima revisione più
 *   recente (a parità, più ripetizioni/intervallo più lungo);
 * - grammatica: per ogni unità vince il risultato più avanzato (completata,
 *   poi percentuale più alta), con ripassi ed eventuale prossima scadenza uniti;
 * - lezioni: per ogni lezione vince il progresso più avanti (indice più alto /
 *   completata), punteggi = massimo;
 * - streak, punti, badge, piano, guida, sessioni e log: unione o massimo.
 *
 * L'operazione è simmetrica e ritentabile: unirla due volte dà lo stesso
 * risultato, quindi può essere rilanciata a ogni salvataggio/ripristino senza
 * effetti collaterali.
 */
object BackupMerge {

    /** Unisce due repository completi (tutti i profili). */
    fun fondi(locale: ProfiliRepository, remoto: ProfiliRepository): ProfiliRepository {
        if (remoto.profili.isEmpty()) return locale
        if (locale.profili.isEmpty()) return remoto

        val ids = LinkedHashSet<String>()
        ids += locale.profili.keys
        ids += remoto.profili.keys

        // I profili eliminati (tombstone) non vengono mai "resuscitati" dal
        // merge: se uno dei due lati li ha cancellati, restano cancellati.
        val eliminati = locale.profiliEliminati + remoto.profiliEliminati
        ids.removeAll(eliminati)

        val profili = ids.associateWith { id ->
            val l = locale.profili[id]
            val r = remoto.profili[id]
            when {
                l == null -> r!!
                r == null -> l
                else -> fondi(l, r)
            }
        }

        val attivo = locale.profiloAttivoId?.takeIf { profili.containsKey(it) && it !in eliminati }
            ?: remoto.profiloAttivoId?.takeIf { profili.containsKey(it) && it !in eliminati }
            ?: profili.keys.firstOrNull()

        return ProfiliRepository(profili = profili, profiloAttivoId = attivo, profiliEliminati = eliminati)
    }

    /** Unisce due profili: configurazione locale (completata dal cloud) e stato unito. */
    fun fondi(locale: ProfiloUtente, remoto: ProfiloUtente): ProfiloUtente = locale.copy(
        config = locale.config.copy(
            // Le preferenze locali vincono, ma un campo vuoto (es. chiave o token
            // inseriti solo sull'altro dispositivo) viene recuperato dal cloud.
            googleSheetId = locale.config.googleSheetId?.takeIf { it.isNotBlank() }
                ?: remoto.config.googleSheetId,
            feedUrl = locale.config.feedUrl.ifBlank { remoto.config.feedUrl },
            guidaDocId = locale.config.guidaDocId?.takeIf { it.isNotBlank() }
                ?: remoto.config.guidaDocId,
            tutorApiKey = locale.config.tutorApiKey.ifBlank { remoto.config.tutorApiKey },
            geminiApiKey = locale.config.geminiApiKey.ifBlank { remoto.config.geminiApiKey },
            providerAI = locale.config.providerAI ?: remoto.config.providerAI,
            // Ogni chiave locale vince; le chiavi presenti solo nel cloud
            // vengono recuperate (es. provider configurato su un altro dispositivo).
            chiaviAI = remoto.config.chiaviAI + locale.config.chiaviAI,
            modelloAI = locale.config.modelloAI.ifBlank { remoto.config.modelloAI },
            palette = locale.config.palette.ifBlank { remoto.config.palette },
            stileDesign = locale.config.stileDesign.ifBlank { remoto.config.stileDesign }
        ),
        stato = fondi(locale.stato, remoto.stato),
        creatoIl = minNonZero(locale.creatoIl, remoto.creatoIl),
        ultimoAccesso = max(locale.ultimoAccesso, remoto.ultimoAccesso)
    )

    /** Unisce lo stato di un profilo: è qui che vivono lessico e progressi. */
    fun fondi(locale: ProfiloStato, remoto: ProfiloStato): ProfiloStato {
        val giorno = piuRecente(locale.ultimoGiornoStudio, remoto.ultimoGiornoStudio)
        // Lo streak appartiene al giorno più recente di studio: quello del
        // dispositivo che ha studiato per ultimo è il valore vero.
        val streak = when {
            locale.ultimoGiornoStudio == remoto.ultimoGiornoStudio ->
                max(locale.streakGiorni, remoto.streakGiorni)
            giorno == locale.ultimoGiornoStudio -> locale.streakGiorni
            else -> remoto.streakGiorni
        }

        val dataPassi = piuRecente(locale.passiGiornoData, remoto.passiGiornoData)
        val passi = when {
            locale.passiGiornoData == remoto.passiGiornoData ->
                (locale.passiGiornoIds + remoto.passiGiornoIds).distinct()
            dataPassi == locale.passiGiornoData -> locale.passiGiornoIds
            else -> remoto.passiGiornoIds
        }

        val unito = ProfiloStato(
            parole = fondiParole(locale.parole, remoto.parole),
            reviews = fondiReviews(locale.reviews, remoto.reviews),
            feedLog = (locale.feedLog + remoto.feedLog)
                .distinctBy { it.syncedAt to it.status }
                .sortedBy { it.syncedAt }
                .takeLast(30),
            guida = fondiGuida(locale.guida, remoto.guida),
            progresso = fondi(locale.progresso, remoto.progresso),
            lezioneContenuto = locale.lezioneContenuto.ifBlank { remoto.lezioneContenuto },
            lezioniGenerate = unisciMappe(locale.lezioniGenerate, remoto.lezioniGenerate),
            // L'ETag del dispositivo locale non descrive il contenuto unito:
            // azzerarlo costringe il prossimo sync a riscaricare tutto il feed
            // (nessun dato perso, solo una richiesta in più). Lo si azzera
            // SOLO se l'unione ha cambiato qualcosa: se il cloud non portava
            // nulla di nuovo, l'ETag resta quello locale e il feed non viene
            // riscaricato inutilmente ad ogni sincronizzazione automatica.
            etag = locale.etag,
            lastSync = max(locale.lastSync, remoto.lastSync),
            risposte = fondiRisposte(locale.risposte, remoto.risposte),
            piano = fondiPiano(locale.piano, remoto.piano),
            progressoFeed = fondiProgressoFeed(locale.progressoFeed, remoto.progressoFeed),
            sessioni = if (locale.sessioni.isNotEmpty()) locale.sessioni else remoto.sessioni,
            conversazioni = (locale.conversazioni + remoto.conversazioni)
                .distinctBy { it.id }
                .sortedBy { it.timestamp },
            streakGiorni = streak,
            streakRecord = maxOf(
                locale.streakRecord, remoto.streakRecord,
                locale.streakGiorni, remoto.streakGiorni, streak
            ),
            ultimoGiornoStudio = giorno,
            badgeSbloccati = (locale.badgeSbloccati + remoto.badgeSbloccati).distinct(),
            puntiTotali = max(locale.puntiTotali, remoto.puntiTotali),
            eserciziGeneratiComprensione = fondiEsercizi(
                locale.eserciziGeneratiComprensione, remoto.eserciziGeneratiComprensione
            ),
            eserciziGeneratiProduzione = fondiEsercizi(
                locale.eserciziGeneratiProduzione, remoto.eserciziGeneratiProduzione
            ),
            eserciziGeneratiAscolto = fondiEsercizi(
                locale.eserciziGeneratiAscolto, remoto.eserciziGeneratiAscolto
            ),
            eserciziGeneratiGrammatica = fondiEsercizi(
                locale.eserciziGeneratiGrammatica, remoto.eserciziGeneratiGrammatica
            ),
            ripassiLog = (locale.ripassiLog + remoto.ripassiLog)
                .distinctBy { Triple(it.wordId, it.timestamp, it.verifica) }
                .sortedBy { it.timestamp },
            errori = fondiErrori(locale.errori, remoto.errori),
            placementFatto = locale.placementFatto || remoto.placementFatto,
            padronanzaGrammatica = fondiMappe(
                locale.padronanzaGrammatica,
                remoto.padronanzaGrammatica,
                ::fondiPadronanza
            ),
            lezionePercorsoId = locale.lezionePercorsoId.ifBlank { remoto.lezionePercorsoId },
            progressiLezioni = fondiMappe(
                locale.progressiLezioni,
                remoto.progressiLezioni,
                ::fondiProgressoLezione
            ),
            passiGiornoData = dataPassi ?: "",
            passiGiornoIds = passi,
            // La rotazione descrive la sessione in corso: quella locale (se
            // esiste) è la più vicina a ciò che l'utente sta vedendo.
            rotazioniEsercizi = remoto.rotazioniEsercizi + locale.rotazioniEsercizi,
            eserciziUnitaAi = fondiMappeListe(locale.eserciziUnitaAi, remoto.eserciziUnitaAi)
        )
        // Se l'unione non ha cambiato nulla, l'ETag locale resta valido e il
        // feed non viene riscaricato inutilmente ad ogni sync automatico.
        return if (unito == locale) unito else unito.copy(etag = "")
    }

    // ------------------------------------------------------------- progresso----------------------------------------- progresso

    /** Unisce i progressi complessivi (livello, test, punteggi). */
    fun fondi(locale: ProgressoUtente, remoto: ProgressoUtente): ProgressoUtente =
        ProgressoUtente(
            livelloCorrente = if (remoto.livelloCorrente.ordine > locale.livelloCorrente.ordine) {
                remoto.livelloCorrente
            } else locale.livelloCorrente,
            xpTotale = max(locale.xpTotale, remoto.xpTotale),
            paroleApprese = max(locale.paroleApprese, remoto.paroleApprese),
            paroleInCorso = max(locale.paroleInCorso, remoto.paroleInCorso),
            lezioniCompletate = max(locale.lezioniCompletate, remoto.lezioniCompletate),
            accuratezzaMedia = max(locale.accuratezzaMedia, remoto.accuratezzaMedia),
            streakGiorni = max(locale.streakGiorni, remoto.streakGiorni),
            ultimoTest = testPiuRecente(locale.ultimoTest, remoto.ultimoTest),
            storicoTest = (locale.storicoTest + remoto.storicoTest)
                .distinctBy { it.id }
                .sortedBy { it.timestamp },
            ultimaAttivita = max(locale.ultimaAttivita, remoto.ultimaAttivita),
            // L'obiettivo è una scelta dell'utente: resta quello locale.
            obiettivoLivello = locale.obiettivoLivello,
            lezioneCorrente = max(locale.lezioneCorrente, remoto.lezioneCorrente),
            dataInizioLezione = minNonZero(locale.dataInizioLezione, remoto.dataInizioLezione),
            testGrammatica = (locale.testGrammatica + remoto.testGrammatica)
                .distinctBy { it.data }
                .sortedBy { it.data },
            testComprensione = (locale.testComprensione + remoto.testComprensione)
                .distinctBy { it.data }
                .sortedBy { it.data },
            testProduzione = (locale.testProduzione + remoto.testProduzione)
                .distinctBy { it.data }
                .sortedBy { it.data },
            valutazioniProduzione = (locale.valutazioniProduzione + remoto.valutazioniProduzione)
                .distinctBy { it.data }
                .sortedBy { it.data },
            testB1 = (locale.testB1 + remoto.testB1)
                .distinctBy { it.id }
                .sortedBy { it.data },
            testAdattivi = (locale.testAdattivi + remoto.testAdattivi)
                .distinctBy { it.data }
                .sortedBy { it.data },
            testAscolto = (locale.testAscolto + remoto.testAscolto)
                .distinctBy { it.data }
                .sortedBy { it.data },
            erroriGrammatica = (locale.erroriGrammatica + remoto.erroriGrammatica).distinct(),
            punteggiLezioni = unisciMassimi(locale.punteggiLezioni, remoto.punteggiLezioni)
        )

    // ------------------------------------------------------------- lessico

    private fun fondiParole(locale: List<WordEntity>, remoto: List<WordEntity>): List<WordEntity> {
        if (remoto.isEmpty()) return locale
        val per = LinkedHashMap<String, WordEntity>(locale.size + remoto.size)
        locale.forEach { per[it.id] = it }
        remoto.forEach { w -> per[w.id] = per[w.id]?.let { fondiParola(it, w) } ?: w }
        return per.values.toList()
    }

    private fun fondiParola(locale: WordEntity, remoto: WordEntity): WordEntity = locale.copy(
        german = locale.german.ifBlank { remoto.german },
        italian = locale.italian.ifBlank { remoto.italian },
        example = locale.example.ifBlank { remoto.example },
        article = locale.article?.takeIf { it.isNotBlank() } ?: remoto.article,
        pronunciation = locale.pronunciation?.takeIf { it.isNotBlank() } ?: remoto.pronunciation,
        level = locale.level.ifBlank { remoto.level },
        tags = (locale.tags.split(",") + remoto.tags.split(","))
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .distinct()
            .joinToString(","),
        // Archiviare è una scelta esplicita dell'utente: vale su entrambi.
        archived = locale.archived || remoto.archived,
        createdAt = minNonZero(locale.createdAt, remoto.createdAt)
    )

    private fun fondiReviews(
        locale: Map<String, ReviewEntity>,
        remoto: Map<String, ReviewEntity>
    ): Map<String, ReviewEntity> {
        if (remoto.isEmpty()) return locale
        val out = LinkedHashMap<String, ReviewEntity>(locale.size + remoto.size)
        out.putAll(locale)
        remoto.forEach { (id, carta) ->
            out[id] = out[id]?.let { fondoCarta(it, carta) } ?: carta
        }
        return out
    }

    /**
     * Fra due carte della stessa parola tiene quella più avanti nel ripasso.
     * NON si sommano i contatori: sarebbero doppi conteggi dello stesso studio.
     */
    private fun fondoCarta(locale: ReviewEntity, remoto: ReviewEntity): ReviewEntity {
        val ultimaLocale = locale.lastReviewedAt ?: 0L
        val ultimaRemota = remoto.lastReviewedAt ?: 0L
        return when {
            ultimaLocale != ultimaRemota -> if (ultimaLocale > ultimaRemota) locale else remoto
            locale.repetitions != remoto.repetitions ->
                if (locale.repetitions > remoto.repetitions) locale else remoto
            locale.intervalDays != remoto.intervalDays ->
                if (locale.intervalDays > remoto.intervalDays) locale else remoto
            else -> if (locale.lapses <= remoto.lapses) locale else remoto
        }
    }

    // ----------------------------------------------------- grammatica e lezioni

    private fun fondiPadronanza(
        locale: PadronanzaGrammatica,
        remoto: PadronanzaGrammatica
    ): PadronanzaGrammatica {
        val base = when {
            locale.completata != remoto.completata ->
                if (locale.completata) locale else remoto
            locale.percentuale != remoto.percentuale ->
                if (locale.percentuale > remoto.percentuale) locale else remoto
            locale.totali != remoto.totali ->
                if (locale.totali > remoto.totali) locale else remoto
            else -> if (locale.ultimaVolta >= remoto.ultimaVolta) locale else remoto
        }
        return base.copy(
            unitaId = base.unitaId.ifBlank { locale.unitaId.ifBlank { remoto.unitaId } },
            completata = locale.completata || remoto.completata,
            ripassi = max(locale.ripassi, remoto.ripassi),
            ultimaVolta = max(locale.ultimaVolta, remoto.ultimaVolta),
            prossimaRipasso = max(locale.prossimaRipasso, remoto.prossimaRipasso)
        )
    }

    private fun fondiProgressoLezione(
        locale: ProgressoLezione,
        remoto: ProgressoLezione
    ): ProgressoLezione {
        val base = when {
            locale.completata != remoto.completata ->
                if (locale.completata) locale else remoto
            locale.indice != remoto.indice ->
                if (locale.indice > remoto.indice) locale else remoto
            locale.corrette != remoto.corrette ->
                if (locale.corrette > remoto.corrette) locale else remoto
            else -> if (locale.aggiornato >= remoto.aggiornato) locale else remoto
        }
        // Se la lezione è completata l'indice è già alla fine: il massimo non
        // può "riaprire" una lezione chiusa.
        return base.copy(
            completata = locale.completata || remoto.completata,
            indice = max(locale.indice, remoto.indice),
            corrette = max(locale.corrette, remoto.corrette),
            totali = max(locale.totali, remoto.totali),
            aggiornato = max(locale.aggiornato, remoto.aggiornato)
        )
    }

    // ------------------------------------------------------- esercizi e log

    private fun fondiEsercizi(
        locale: List<EsercizioGenEntity>,
        remoto: List<EsercizioGenEntity>
    ): List<EsercizioGenEntity> {
        if (remoto.isEmpty()) return locale
        val per = LinkedHashMap<String, EsercizioGenEntity>(locale.size + remoto.size)
        locale.forEach { per[it.id] = it }
        remoto.forEach { e ->
            per[e.id] = per[e.id]?.let { l ->
                l.copy(
                    completato = l.completato || e.completato,
                    dataCompletamento = max(l.dataCompletamento, e.dataCompletamento)
                )
            } ?: e
        }
        return per.values.toList()
    }

    private fun fondiErrori(
        locale: List<ErroreEntity>,
        remoto: List<ErroreEntity>
    ): List<ErroreEntity> {
        if (remoto.isEmpty()) return locale
        val per = LinkedHashMap<String, ErroreEntity>(locale.size + remoto.size)
        locale.forEach { per[it.chiave] = it }
        remoto.forEach { e ->
            per[e.chiave] = per[e.chiave]?.let { l ->
                l.copy(
                    risolto = l.risolto || e.risolto,
                    ultimaVolta = max(l.ultimaVolta, e.ultimaVolta),
                    ripetizioni = max(l.ripetizioni, e.ripetizioni),
                    intervalloGiorni = max(l.intervalloGiorni, e.intervalloGiorni),
                    prossimaRipasso = max(l.prossimaRipasso, e.prossimaRipasso),
                    creato = minNonZero(l.creato, e.creato)
                )
            } ?: e
        }
        return per.values.toList()
    }

    private fun fondiRisposte(
        locale: List<FeedbackEntry>,
        remoto: List<FeedbackEntry>
    ): List<FeedbackEntry> {
        if (remoto.isEmpty()) return locale
        val per = LinkedHashMap<String, FeedbackEntry>(locale.size + remoto.size)
        locale.forEach { per[it.id] = it }
        remoto.forEach { r ->
            per[r.id] = per[r.id]?.let { l ->
                // Una correzione ricevuta è informazione in più: non si perde.
                if (l.correzione.isNullOrBlank() && !r.correzione.isNullOrBlank()) {
                    r.copy(corretto = l.corretto || r.corretto)
                } else {
                    l.copy(
                        corretto = l.corretto || r.corretto,
                        correzione = l.correzione ?: r.correzione
                    )
                }
            } ?: r
        }
        return per.values.sortedBy { it.timestamp }
    }

    // ------------------------------------------------------------ contenuti

    private fun fondiGuida(locale: GuidaEntity?, remoto: GuidaEntity?): GuidaEntity? = when {
        locale == null -> remoto
        remoto == null -> locale
        remoto.sezioni.size > locale.sezioni.size -> remoto
        else -> locale
    }

    private fun fondiPiano(locale: PianoEntity?, remoto: PianoEntity?): PianoEntity? = when {
        locale == null -> remoto
        remoto == null -> locale
        else -> PianoEntity(
            obiettivo = locale.obiettivo.ifBlank { remoto.obiettivo },
            orizzonte = locale.orizzonte.ifBlank { remoto.orizzonte },
            tappe = (locale.tappe + remoto.tappe).distinctBy { it.nome.trim().lowercase() },
            certificazioni = (locale.certificazioni + remoto.certificazioni)
                .distinctBy { it.nome.trim().lowercase() },
            risorse = (locale.risorse + remoto.risorse)
                .distinctBy { it.nome.trim().lowercase() }
        )
    }

    private fun fondiProgressoFeed(
        locale: ProgressoFeedEntity?,
        remoto: ProgressoFeedEntity?
    ): ProgressoFeedEntity? = when {
        locale == null -> remoto
        remoto == null -> locale
        else -> ProgressoFeedEntity(
            lezioneCorrente = max(locale.lezioneCorrente, remoto.lezioneCorrente),
            streakCorrente = max(locale.streakCorrente, remoto.streakCorrente),
            streakRecord = max(locale.streakRecord, remoto.streakRecord),
            totaleFatte = max(locale.totaleFatte, remoto.totaleFatte),
            paroleTotali = max(locale.paroleTotali, remoto.paroleTotali),
            paroleMature = max(locale.paroleMature, remoto.paroleMature)
        )
    }

    // -------------------------------------------------------------- utilità

    private fun <V> unisciMappe(locale: Map<String, V>, remoto: Map<String, V>): Map<String, V> {
        if (remoto.isEmpty()) return locale
        if (locale.isEmpty()) return remoto
        val out = LinkedHashMap<String, V>(locale.size + remoto.size)
        out.putAll(remoto)
        out.putAll(locale) // i valori locali (più freschi) vincono
        return out
    }

    private fun <V> fondiMappe(
        locale: Map<String, V>,
        remoto: Map<String, V>,
        fondi: (V, V) -> V
    ): Map<String, V> {
        if (remoto.isEmpty()) return locale
        val out = LinkedHashMap<String, V>(locale.size + remoto.size)
        out.putAll(locale)
        remoto.forEach { (k, v) -> out[k] = out[k]?.let { fondi(it, v) } ?: v }
        return out
    }

    private fun fondiMappeListe(
        locale: Map<String, List<EsercizioGenEntity>>,
        remoto: Map<String, List<EsercizioGenEntity>>
    ): Map<String, List<EsercizioGenEntity>> {
        if (remoto.isEmpty()) return locale
        val out = LinkedHashMap<String, List<EsercizioGenEntity>>(locale.size + remoto.size)
        out.putAll(locale)
        remoto.forEach { (k, lista) ->
            out[k] = out[k]?.let { fondiEsercizi(it, lista) } ?: lista
        }
        return out
    }

    private fun unisciMassimi(locale: Map<Int, Int>, remoto: Map<Int, Int>): Map<Int, Int> {
        if (remoto.isEmpty()) return locale
        val out = LinkedHashMap<Int, Int>(locale.size + remoto.size)
        out.putAll(locale)
        remoto.forEach { (k, v) -> out[k] = max(out[k] ?: v, v) }
        return out
    }

    private fun testPiuRecente(a: TestLivello?, b: TestLivello?): TestLivello? = when {
        a == null -> b
        b == null -> a
        else -> if (a.timestamp >= b.timestamp) a else b
    }

    private fun piuRecente(a: String?, b: String?): String? = when {
        a.isNullOrBlank() -> b
        b.isNullOrBlank() -> a
        else -> if (a >= b) a else b
    }

    private fun minNonZero(a: Long, b: Long): Long = when {
        a <= 0L -> b
        b <= 0L -> a
        else -> minOf(a, b)
    }
}
