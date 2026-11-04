package com.alessandro.tedesco.data

/**
 * Podcast, video e corsi per livello di apprendimento.
 *
 * Contenuti curati e verificati: Deutsche Welle, Easy German, Slow German,
 * Deutschlandfunk, ARD Audiothek, canali YouTube didattici e show Spotify.
 * L'app apre il link nell'app/browser esterno: niente download, niente
 * hosting, nessun peso sull'APK.
 *
 * Questa sezione è l'unica che richiede rete: il resto dell'app resta
 * offline-first.
 */
object PodcastData {

    enum class Piattaforma(val etichetta: String) {
        YOUTUBE("YouTube"),
        SPOTIFY("Spotify"),
        DW("Deutsche Welle"),
        WEB("Web")
    }

    enum class Tipo(val etichetta: String) {
        PODCAST("Podcast"),
        VIDEO("Video"),
        CORSO("Corso")
    }

    enum class Velocita(val etichetta: String, val spiegazione: String) {
        LENTA("Lenta", "parlato rallentato e scandito"),
        MEDIA("Media", "parlato chiaro, quasi naturale"),
        NATURALE("Naturale", "velocità dei madrelingua")
    }

    data class Episodio(
        val id: String,
        val livello: String,
        val titolo: String,
        val canale: String,
        val descrizione: String,
        val piattaforma: Piattaforma,
        val tipo: Tipo,
        val url: String,
        val durata: String,
        val velocita: Velocita,
        val supporto: String,
        val tema: String,
        val consigliato: Boolean = false
    )

    data class Livello(
        val codice: String,
        val titolo: String,
        val obiettivo: String,
        val consiglio: String
    )

    val livelliInfo: List<Livello> = listOf(
        Livello(
            codice = "A1",
            titolo = "Primo ascolto",
            obiettivo = "Frasi brevi, vita quotidiana, parlato lento e scandito.",
            consiglio = "Ascolta 10 minuti al giorno con i sottotitoli. Non tradurre tutto: " +
                "cerca le parole che già conosci e lascia scorrere il resto."
        ),
        Livello(
            codice = "A2",
            titolo = "Storie e dialoghi",
            obiettivo = "Racconti guidati, interviste semplici, conversazioni di tutti i giorni.",
            consiglio = "Riascolta lo stesso episodio due volte: la prima con i sottotitoli, " +
                "la seconda senza. La seconda volta capirai molto di più."
        ),
        Livello(
            codice = "B1",
            titolo = "Notizie e podcast",
            obiettivo = "Attualità, cultura, opinioni: il tedesco che si usa davvero.",
            consiglio = "Scegli 3 parole nuove per episodio e usale in una frase scritta. " +
                "Ascoltare senza riutilizzare lascia poco."
        ),
        Livello(
            codice = "B2",
            titolo = "Tedesco autentico",
            obiettivo = "Talk show, approfondimenti, velocità naturale e registro colloquiale.",
            consiglio = "Ascolta senza testo, poi riassumi in 5 frasi. Se ci riesci, " +
                "hai capito davvero: è la prova che serve per il B2."
        )
    )

    val episodi: List<Episodio> = listOf(
        // ---------------- A1 ----------------
        Episodio(
            id = "a1-nicos-weg",
            livello = "A1",
            titolo = "Nicos Weg - A1",
            canale = "Deutsche Welle",
            descrizione = "Corso video gratuito in episodi brevi: Nico arriva in Germania e " +
                "impara il tedesco con lui. Ogni puntata ha esercizi e sottotitoli.",
            piattaforma = Piattaforma.DW,
            tipo = Tipo.CORSO,
            url = "https://learngerman.dw.com/en/nicos-weg/s-12758",
            durata = "5-10 min",
            velocita = Velocita.LENTA,
            supporto = "Sottotitoli DE/EN + esercizi",
            tema = "Vita quotidiana",
            consigliato = true
        ),
        Episodio(
            id = "a1-anja",
            livello = "A1",
            titolo = "Learn German with Anja",
            canale = "Anja",
            descrizione = "Lezioni brevi e divertenti sulle basi: saluti, numeri, verbi e " +
                "pronuncia. Spiegazioni semplici, ottime per iniziare.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@LearnGermanwithAnja",
            durata = "10-20 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE/EN",
            tema = "Basi e pronuncia"
        ),
        Episodio(
            id = "a1-lingoni",
            livello = "A1",
            titolo = "lingoni GERMAN - A1",
            canale = "lingoni GERMAN",
            descrizione = "Playlist strutturata per livello: grammatica spiegata passo passo " +
                "con esempi lenti e ripetuti. Perfetta per fissare le strutture.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@lingoniGERMAN",
            durata = "5-12 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE/EN",
            tema = "Grammatica di base"
        ),
        Episodio(
            id = "a1-coffee-break",
            livello = "A1",
            titolo = "Coffee Break German",
            canale = "Radio Lingua",
            descrizione = "Podcast a lezioni: una conversazione guidata per volta, con " +
                "ripetizioni e spiegazioni. Ideale per l'ascolto in movimento.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/3QiX0ZkjILyGWmpemackWB",
            durata = "15-20 min",
            velocita = Velocita.LENTA,
            supporto = "Note ed episodio guidato",
            tema = "Conversazione guidata"
        ),
        Episodio(
            id = "a1-slow-german",
            livello = "A1",
            titolo = "Slow German - Annik Rubens",
            canale = "Annik Rubens",
            descrizione = "Il classico podcast lento sulla vita tedesca: birrerie, riciclo, " +
                "tradizioni. Testo completo da leggere mentre ascolti.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/1LjZv4vFFBWfFPH8bHczGb",
            durata = "10-15 min",
            velocita = Velocita.LENTA,
            supporto = "Trascrizione su slowgerman.com",
            tema = "Cultura e vita tedesca"
        ),
        Episodio(
            id = "a1-stories",
            livello = "A1",
            titolo = "Learn German with Stories",
            canale = "German Stories",
            descrizione = "Storie progressive in tedesco semplice: la trama aiuta a capire " +
                "senza tradurre. Ottimo per allenare l'orecchio con continuità.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/3LJ4M70EO7ngyF0fiFA2AW",
            durata = "10-20 min",
            velocita = Velocita.LENTA,
            supporto = "Storie graduate A1/A2",
            tema = "Racconti"
        ),
        Episodio(
            id = "a1-deutsch-fuer-euch",
            livello = "A1",
            titolo = "Deutsch für Euch",
            canale = "Deutsch für Euch",
            descrizione = "Pronuncia, alfabeto e primi dialoghi spiegati in modo diretto. " +
                "Video corti da rivedere più volte.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@DeutschFuerEuch",
            durata = "5-10 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE",
            tema = "Pronuncia e basi"
        ),

        // ---------------- A2 ----------------
        Episodio(
            id = "a2-nicos-weg",
            livello = "A2",
            titolo = "Nicos Weg - A2",
            canale = "Deutsche Welle",
            descrizione = "La seconda parte del corso DW: dialoghi più lunghi, vita in " +
                "Germania e esercizi mirati. Il ponte naturale tra A1 e B1.",
            piattaforma = Piattaforma.DW,
            tipo = Tipo.CORSO,
            url = "https://learngerman.dw.com/en/nicos-weg/s-12758",
            durata = "5-10 min",
            velocita = Velocita.LENTA,
            supporto = "Sottotitoli DE/EN + esercizi",
            tema = "Vita quotidiana",
            consigliato = true
        ),
        Episodio(
            id = "a2-easy-german",
            livello = "A2",
            titolo = "Easy German - A2/B1",
            canale = "Easy Languages",
            descrizione = "Interviste per strada con sottotitoli tedesco/inglese: senti " +
                "come parlano davvero le persone, non l'audio di un corso.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@EasyGerman",
            durata = "8-15 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE/EN",
            tema = "Interviste per strada"
        ),
        Episodio(
            id = "a2-14-minuten",
            livello = "A2",
            titolo = "14 Minuten",
            canale = "14 Minuten",
            descrizione = "Video di circa un quarto d'ora su storia, cultura e curiosità " +
                "tedesche, con linguaggio chiaro e immagini di supporto.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@14Minuten",
            durata = "10-15 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE",
            tema = "Storia e cultura"
        ),
        Episodio(
            id = "a2-marija",
            livello = "A2",
            titolo = "Deutsch mit Marija",
            canale = "Deutsch mit Marija",
            descrizione = "Grammatica e lessico spiegati con calma, con esempi dalla vita " +
                "reale. Chiara sulle differenze tra italiano e tedesco.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@DeutschmitMarija",
            durata = "10-20 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE",
            tema = "Grammatica e lessico"
        ),
        Episodio(
            id = "a2-einfach-gesagt",
            livello = "A2",
            titolo = "Einfach gesagt!",
            canale = "Einfach gesagt!",
            descrizione = "Episodi brevi per A2-B1: un tema, un vocabolario essenziale e " +
                "un ritmo sostenibile. Perfetto per la routine giornaliera.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/549KA6AMWIczQUZETUbwx3",
            durata = "10-20 min",
            velocita = Velocita.LENTA,
            supporto = "Episodi brevi A2/B1",
            tema = "Conversazione"
        ),
        Episodio(
            id = "a2-stories",
            livello = "A2",
            titolo = "Learn German with Stories - A2",
            canale = "German Stories",
            descrizione = "Le storie crescono con te: frasi più lunghe e tempi verbali " +
                "nuovi, sempre dentro una trama comprensibile.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/3LJ4M70EO7ngyF0fiFA2AW",
            durata = "10-20 min",
            velocita = Velocita.LENTA,
            supporto = "Storie graduate A2",
            tema = "Racconti"
        ),
        Episodio(
            id = "a2-deutsch-fuer-euch",
            livello = "A2",
            titolo = "Deutsch für Euch - A2",
            canale = "Deutsch für Euch",
            descrizione = "Video di grammatica con esempi e confronti: casi, preposizioni " +
                "e ordine delle parole senza giri di parole.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@DeutschFuerEuch",
            durata = "5-10 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE",
            tema = "Grammatica"
        ),

        // ---------------- B1 ----------------
        Episodio(
            id = "b1-easy-german-podcast",
            livello = "B1",
            titolo = "Easy German Podcast",
            canale = "Easy German",
            descrizione = "Coco e Manuel parlano di Germania, lingua e attualità con " +
                "linguaggio chiaro. Il podcast didattico più completo per il B1.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/2rnCPyV8A9J80QtmGj2pAx",
            durata = "25-40 min",
            velocita = Velocita.MEDIA,
            supporto = "Trascrizione sul sito Easy German",
            tema = "Attualità e cultura",
            consigliato = true
        ),
        Episodio(
            id = "b1-dw-nachrichten",
            livello = "B1",
            titolo = "Langsam gesprochene Nachrichten",
            canale = "Deutsche Welle",
            descrizione = "Le notizie del giorno lette lentamente, con il testo completo. " +
                "Il modo migliore per passare dalle storie alle notizie vere.",
            piattaforma = Piattaforma.DW,
            tipo = Tipo.PODCAST,
            url = "https://learngerman.dw.com/de/langsam-gesprochene-nachrichten/s-12098",
            durata = "5-10 min",
            velocita = Velocita.LENTA,
            supporto = "Testo completo",
            tema = "Notizie"
        ),
        Episodio(
            id = "b1-top-thema",
            livello = "B1",
            titolo = "Top-Thema mit Vokabeln",
            canale = "Deutsche Welle",
            descrizione = "Un tema di attualità a settimana con glossario ed esercizi. " +
                "Ottimo per il lessico che serve davvero a un esame B1.",
            piattaforma = Piattaforma.DW,
            tipo = Tipo.PODCAST,
            url = "https://learngerman.dw.com/de/top-thema/s-5591",
            durata = "5-10 min",
            velocita = Velocita.MEDIA,
            supporto = "Testo + glossario + esercizi",
            tema = "Notizie e attualità"
        ),
        Episodio(
            id = "b1-auf-deutsch-gesagt",
            livello = "B1",
            titolo = "Auf Deutsch gesagt!",
            canale = "Robin Meinert",
            descrizione = "Conversazioni naturali su lavoro, viaggi e vita quotidiana. " +
                "Parla come un amico colto: ottimo per abituarsi ai ritmi veri.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/036NuzN8VdHMy2i6zfQGUU",
            durata = "30-45 min",
            velocita = Velocita.NATURALE,
            supporto = "Trascrizione sul sito",
            tema = "Conversazione naturale"
        ),
        Episodio(
            id = "b1-benjamin",
            livello = "B1",
            titolo = "Deutsch mit Benjamin",
            canale = "Benjamin",
            descrizione = "Sfumature, espressioni idiomatiche e piccoli errori tipici di chi " +
                "impara. Spiegazioni precise, utili anche per la produzione scritta.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@DeutschmitBenjamin",
            durata = "10-20 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli DE",
            tema = "Grammatica e stile"
        ),
        Episodio(
            id = "b1-get-germanized",
            livello = "B1",
            titolo = "Get Germanized",
            canale = "Get Germanized",
            descrizione = "Cultura, abitudini e curiosità tedesche raccontate con ironia. " +
                "Utile per capire i riferimenti culturali che restano fuori dai libri.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@GetGermanized",
            durata = "8-15 min",
            velocita = Velocita.MEDIA,
            supporto = "Sottotitoli EN",
            tema = "Cultura e società"
        ),
        Episodio(
            id = "b1-slow-german",
            livello = "B1",
            titolo = "Slow German - B1",
            canale = "Annik Rubens",
            descrizione = "Temi di cultura e società a velocità ridotta, con trascrizione. " +
                "Il passaggio ideale verso i podcast per madrelingua.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/1LjZv4vFFBWfFPH8bHczGb",
            durata = "10-15 min",
            velocita = Velocita.LENTA,
            supporto = "Trascrizione su slowgerman.com",
            tema = "Cultura"
        ),
        Episodio(
            id = "b1-ard-audiothek",
            livello = "B1",
            titolo = "ARD Audiothek",
            canale = "ARD",
            descrizione = "Il catalogo radiofonico pubblico tedesco: documentari, cultura e " +
                "programmi. Scegli un tema che ti interessa e ascolta senza testo.",
            piattaforma = Piattaforma.WEB,
            tipo = Tipo.PODCAST,
            url = "https://www.ardaudiothek.de/",
            durata = "variabile",
            velocita = Velocita.NATURALE,
            supporto = "Catalogo con filtri per tema",
            tema = "Radio e documentari"
        ),

        // ---------------- B2 ----------------
        Episodio(
            id = "b2-easy-german",
            livello = "B2",
            titolo = "Easy German - B2",
            canale = "Easy Languages",
            descrizione = "Interviste su politica, società e filosofia, con sottotitoli. " +
                "Il tedesco autentico senza rinunciare a un appiglio scritto.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@EasyGerman",
            durata = "10-20 min",
            velocita = Velocita.NATURALE,
            supporto = "Sottotitoli DE/EN",
            tema = "Società e politica",
            consigliato = true
        ),
        Episodio(
            id = "b2-gemischtes-hack",
            livello = "B2",
            titolo = "Gemischtes Hack",
            canale = "Felix Lobrecht & Tommi Schmitt",
            descrizione = "Due comici parlano di tutto: registro colloquiale, battute e " +
                "riferimenti culturali. Difficile ma formidabile per l'orecchio.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/7BTOsF2boKmlYr76BelijW",
            durata = "60-90 min",
            velocita = Velocita.NATURALE,
            supporto = "Video-podcast su Spotify",
            tema = "Conversazione colloquiale"
        ),
        Episodio(
            id = "b2-der-tag",
            livello = "B2",
            titolo = "Deutschlandfunk - Der Tag",
            canale = "Deutschlandfunk",
            descrizione = "Un tema al giorno spiegato fino in fondo, con ospiti e domande. " +
                "Il formato giusto per il tedesco delle notizie e delle istituzioni.",
            piattaforma = Piattaforma.WEB,
            tipo = Tipo.PODCAST,
            url = "https://www.deutschlandfunk.de/podcast-104.html",
            durata = "20-30 min",
            velocita = Velocita.NATURALE,
            supporto = "Trascrizione e articolo",
            tema = "Attualità e politica"
        ),
        Episodio(
            id = "b2-das-war-der-tag",
            livello = "B2",
            titolo = "Das war der Tag",
            canale = "Deutschlandfunk",
            descrizione = "La rassegna serale del giorno: corrispondenti dall'estero e " +
                "sintesi dei fatti. Ascolto ideale per il lessico politico.",
            piattaforma = Piattaforma.WEB,
            tipo = Tipo.PODCAST,
            url = "https://www.deutschlandfunk.de/das-war-der-tag-100.html",
            durata = "30-45 min",
            velocita = Velocita.NATURALE,
            supporto = "Trascrizione",
            tema = "Rassegna del giorno"
        ),
        Episodio(
            id = "b2-ard-documentari",
            livello = "B2",
            titolo = "ARD Audiothek - documentari",
            canale = "ARD",
            descrizione = "Documentari e audio-libri del servizio pubblico: lingua curata, " +
                "ritmi naturali e temi che vale la pena seguire.",
            piattaforma = Piattaforma.WEB,
            tipo = Tipo.PODCAST,
            url = "https://www.ardaudiothek.de/",
            durata = "30-60 min",
            velocita = Velocita.NATURALE,
            supporto = "Catalogo con filtri",
            tema = "Documentari e cultura"
        ),
        Episodio(
            id = "b2-dw-themen",
            livello = "B2",
            titolo = "DW - Temi e notizie",
            canale = "Deutsche Welle",
            descrizione = "Video e reportage sulle notizie dal mondo, in tedesco standard. " +
                "Utile per collegare il lessico dell'attualità alla comprensione orale.",
            piattaforma = Piattaforma.DW,
            tipo = Tipo.VIDEO,
            url = "https://www.dw.com/de/themen/s-9077",
            durata = "5-15 min",
            velocita = Velocita.NATURALE,
            supporto = "Sottotitoli DE",
            tema = "Notizie dal mondo"
        ),
        Episodio(
            id = "b2-benjamin",
            livello = "B2",
            titolo = "Deutsch mit Benjamin - B2",
            canale = "Benjamin",
            descrizione = "Sfumature di significato, registro e costruzioni avanzate. " +
                "Per scrivere e parlare in modo più preciso, non solo corretto.",
            piattaforma = Piattaforma.YOUTUBE,
            tipo = Tipo.VIDEO,
            url = "https://www.youtube.com/@DeutschmitBenjamin",
            durata = "10-20 min",
            velocita = Velocita.NATURALE,
            supporto = "Sottotitoli DE",
            tema = "Stile e sfumature"
        ),
        Episodio(
            id = "b2-auf-deutsch-gesagt",
            livello = "B2",
            titolo = "Auf Deutsch gesagt! - B2",
            canale = "Robin Meinert",
            descrizione = "Conversazioni lunghe e non semplificate, con trascrizione. " +
                "Per abituarsi a seguire un discorso senza perdere il filo.",
            piattaforma = Piattaforma.SPOTIFY,
            tipo = Tipo.PODCAST,
            url = "https://open.spotify.com/show/036NuzN8VdHMy2i6zfQGUU",
            durata = "30-45 min",
            velocita = Velocita.NATURALE,
            supporto = "Trascrizione sul sito",
            tema = "Conversazione"
        )
    )

    /** Normalizza il livello del profilo (A0, B1.2, "B1 - soglia", ...) a A1/A2/B1/B2. */
    fun normalizza(livello: String?): String {
        val codice = livello
            ?.uppercase()
            ?.trim()
            ?.substringBefore(" ")
            ?.substringBefore(".")
            ?.substringBefore("-")
            ?.trim()
            .orEmpty()
        return when {
            codice.startsWith("A0") || codice.startsWith("A1") -> "A1"
            codice.startsWith("A2") -> "A2"
            codice.startsWith("B1") -> "B1"
            codice.startsWith("B2") || codice.startsWith("C1") || codice.startsWith("C2") -> "B2"
            else -> "A1"
        }
    }

    fun perLivello(livello: String?): List<Episodio> {
        val codice = normalizza(livello)
        return episodi.filter { it.livello == codice }
    }

    fun perPiattaforma(piattaforma: Piattaforma): List<Episodio> =
        episodi.filter { it.piattaforma == piattaforma }

    fun consigliatoPer(livello: String?): Episodio? =
        perLivello(livello).firstOrNull { it.consigliato } ?: perLivello(livello).firstOrNull()

    fun filtra(
        livello: String?,
        tipo: Tipo? = null,
        piattaforma: Piattaforma? = null
    ): List<Episodio> = perLivello(livello).filter { ep ->
        (tipo == null || ep.tipo == tipo) &&
            (piattaforma == null || ep.piattaforma == piattaforma)
    }

    fun conteggio(livello: String?): Int = perLivello(livello).size

    fun livelli(): List<String> = livelliInfo.map { it.codice }

    fun info(livello: String?): Livello =
        livelliInfo.firstOrNull { it.codice == normalizza(livello) } ?: livelliInfo.first()

    fun tipoPerId(id: String): Tipo? = episodi.firstOrNull { it.id == id }?.tipo
}
