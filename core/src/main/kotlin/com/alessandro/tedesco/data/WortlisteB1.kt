package com.alessandro.tedesco.data

/**
 * Wortliste B1 curata: 100+ parole ad alta frequenza, divise per tema, con
 * esempio. Non sostituisce la Wortliste ufficiale Goethe (copyright), ma dà
 * un nucleo stabile e corretto su cui costruire il lessico d'esame e quello
 * sanitario.
 */
object WortlisteB1 {

    data class Voce(
        val tedesco: String,
        val italiano: String,
        val esempio: String,
        val tema: String,
        val livello: String = "B1"
    )

    private val grezzo = """
        der Termin|appuntamento|Ich habe morgen einen Termin.|Lavoro
        die Bewerbung|domanda di lavoro|Ich schreibe eine Bewerbung.|Lavoro
        der Lebenslauf|curriculum|Mein Lebenslauf ist aktuell.|Lavoro
        das Vorstellungsgespräch|colloquio di lavoro|Das Vorstellungsgespräch ist am Montag.|Lavoro
        die Stelle|posto di lavoro|Ich suche eine Stelle in Brixen.|Lavoro
        der Arbeitgeber|datore di lavoro|Mein Arbeitgeber ist eine Klinik.|Lavoro
        der Arbeitnehmer|dipendente|Der Arbeitnehmer hat Rechte.|Lavoro
        die Ausbildung|formazione|Meine Ausbildung dauert drei Jahre.|Lavoro
        die Weiterbildung|formazione continua|Ich mache eine Weiterbildung.|Lavoro
        die Erfahrung|esperienza|Ich habe zwei Jahre Erfahrung.|Lavoro
        die Verantwortung|responsabilità|Ich übernehme Verantwortung.|Lavoro
        die Zusammenarbeit|collaborazione|Die Zusammenarbeit im Team ist gut.|Lavoro
        der Vertrag|contratto|Ich unterschreibe den Vertrag.|Lavoro
        die Kündigung|licenziamento|Die Kündigung muss schriftlich sein.|Lavoro
        das Gehalt|stipendio|Das Gehalt ist fair.|Lavoro
        die Arbeitszeit|orario di lavoro|Die Arbeitszeit ist flexibel.|Lavoro
        der Feierabend|fine giornata lavorativa|Endlich Feierabend!|Lavoro
        die Überstunde|ora straordinaria|Ich mache heute eine Überstunde.|Lavoro
        der Kollege|collega|Mein Kollege hilft mir.|Lavoro
        die Kollegin|collega (f.)|Meine Kollegin ist krank.|Lavoro
        die Gesundheit|salute|Gesundheit ist wichtig.|Salute
        die Krankheit|malattia|Die Krankheit dauert eine Woche.|Salute
        der Schmerz|dolore|Der Schmerz ist stark.|Salute
        die Behandlung|trattamento|Die Behandlung beginnt morgen.|Salute
        der Befund|referto|Der Befund ist da.|Salute
        die Überweisung|impegnativa|Ich brauche eine Überweisung.|Salute
        die Krankenkasse|cassa mutua|Die Krankenkasse zahlt die Therapie.|Salute
        die Ordination|studio medico|Die Ordination ist im Zentrum.|Salute
        das Rezept|ricetta|Der Arzt schreibt ein Rezept.|Salute
        die Physiotherapie|fisioterapia|Ich arbeite in der Physiotherapie.|Salute
        die Bewegung|movimento|Bewegung hilft gegen Schmerzen.|Salute
        die Übung|esercizio|Diese Übung machen Sie täglich.|Salute
        der Patient|paziente|Der Patient kommt um neun.|Salute
        die Patientin|paziente (f.)|Die Patientin hat Knieschmerzen.|Salute
        die Anamnese|anamnesi|Die Anamnese dauert zehn Minuten.|Salute
        die Diagnose|diagnosi|Die Diagnose ist klar.|Salute
        der Therapieplan|piano terapeutico|Wir erstellen einen Therapieplan.|Salute
        die Kontrolle|controllo|Die Kontrolle ist nächste Woche.|Salute
        die Entlassung|dimissione|Die Entlassung ist am Freitag.|Salute
        die Zuweisung|invio|Ich habe eine Zuweisung vom Arzt.|Salute
        die Gesellschaft|società|Die Gesellschaft verändert sich.|Società
        die Politik|politica|Politik interessiert mich.|Società
        die Wahl|elezione|Die Wahl ist im Herbst.|Società
        die Demokratie|democrazia|Demokratie braucht Beteiligung.|Società
        die Meinung|opinione|Meine Meinung ist anders.|Società
        die Diskussion|discussione|Die Diskussion war lang.|Società
        der Vorteil|vantaggio|Der Vorteil ist klar.|Società
        der Nachteil|svantaggio|Der Nachteil ist der Preis.|Società
        die Lösung|soluzione|Wir brauchen eine Lösung.|Società
        das Problem|problema|Das Problem ist komplex.|Società
        die Regel|regola|Diese Regel gilt für alle.|Società
        die Freiheit|libertà|Freiheit ist wichtig.|Società
        die Gleichberechtigung|parità di diritti|Gleichberechtigung ist ein Ziel.|Società
        die Integration|integrazione|Integration braucht Zeit.|Società
        die Migration|migrazione|Migration prägt die Gesellschaft.|Società
        die Umwelt|ambiente|Wir müssen die Umwelt schützen.|Ambiente
        der Klimawandel|cambiamento climatico|Der Klimawandel ist real.|Ambiente
        die Energie|energia|Wir brauchen saubere Energie.|Ambiente
        der Müll|rifiuti|Der Müll wird getrennt.|Ambiente
        das Recycling|riciclo|Recycling spart Ressourcen.|Ambiente
        die Verschmutzung|inquinamento|Die Verschmutzung nimmt zu.|Ambiente
        der Verkehr|traffico|Der Verkehr ist laut.|Ambiente
        die Nachhaltigkeit|sostenibilità|Nachhaltigkeit ist ein Ziel.|Ambiente
        die Ressource|risorsa|Wasser ist eine Ressource.|Ambiente
        der Naturschutz|tutela della natura|Naturschutz beginnt vor Ort.|Ambiente
        die Sonnenenergie|energia solare|Sonnenenergie ist kostenlos.|Ambiente
        das Windrad|pala eolica|Das Windrad steht auf dem Hügel.|Ambiente
        die Technologie|tecnologia|Technologie entwickelt sich schnell.|Tecnologia
        die Digitalisierung|digitalizzazione|Die Digitalisierung verändert die Arbeit.|Tecnologia
        das Internet|internet|Das Internet ist langsam.|Tecnologia
        die Daten|dati|Die Daten sind sicher.|Tecnologia
        die Sicherheit|sicurezza|Sicherheit ist wichtig.|Tecnologia
        die Künstliche Intelligenz|intelligenza artificiale|Künstliche Intelligenz hilft bei Texten.|Tecnologia
        das Gerät|dispositivo|Das Gerät ist neu.|Tecnologia
        die Anwendung|applicazione|Die Anwendung ist einfach.|Tecnologia
        die Entwicklung|sviluppo|Die Entwicklung dauert Jahre.|Tecnologia
        die Forschung|ricerca|Die Forschung braucht Geld.|Tecnologia
        die Kultur|cultura|Kultur verbindet Menschen.|Cultura
        die Tradition|tradizione|Die Tradition ist alt.|Cultura
        die Veranstaltung|evento|Die Veranstaltung ist heute.|Cultura
        die Ausstellung|mostra|Die Ausstellung ist interessant.|Cultura
        das Museum|museo|Das Museum ist geschlossen.|Cultura
        die Literatur|letteratura|Ich lese gern Literatur.|Cultura
        die Musik|musica|Musik hilft beim Lernen.|Cultura
        das Theater|teatro|Wir gehen ins Theater.|Cultura
        der Brauch|usanza|Der Brauch ist typisch für Südtirol.|Cultura
        die Geschichte|storia|Die Geschichte ist spannend.|Cultura
        die Emotion|emozione|Emotionen sind wichtig.|Emozioni
        die Freude|gioia|Die Freude war groß.|Emozioni
        die Angst|paura|Die Angst vor Fehlern ist normal.|Emozioni
        die Sorge|preoccupazione|Ich habe Sorgen.|Emozioni
        die Hoffnung|speranza|Die Hoffnung bleibt.|Emozioni
        die Enttäuschung|delusione|Die Enttäuschung war groß.|Emozioni
        die Wut|rabbia|Die Wut hilft nicht.|Emozioni
        die Geduld|pazienza|Geduld ist wichtig beim Lernen.|Emozioni
        das Vertrauen|fiducia|Vertrauen muss man aufbauen.|Emozioni
        die Motivation|motivazione|Meine Motivation ist hoch.|Emozioni
        der Stress|stress|Stress macht krank.|Emozioni
        die Entspannung|rilassamento|Entspannung ist wichtig.|Emozioni
        die Reise|viaggio|Die Reise war schön.|Viaggio
        die Unterkunft|alloggio|Die Unterkunft ist günstig.|Viaggio
        die Ankunft|arrivo|Die Ankunft ist um acht.|Viaggio
        die Abfahrt|partenza|Die Abfahrt ist morgen.|Viaggio
        der Aufenthalt|soggiorno|Der Aufenthalt dauert drei Tage.|Viaggio
        die Sehenswürdigkeit|attrazione|Die Sehenswürdigkeit ist berühmt.|Viaggio
        die Fahrkarte|biglietto|Ich kaufe eine Fahrkarte.|Viaggio
        der Anschluss|coincidenza|Der Anschluss ist knapp.|Viaggio
        die Verspätung|ritardo|Der Zug hat Verspätung.|Viaggio
        das Gepäck|bagaglio|Mein Gepäck ist schwer.|Viaggio
    """.trimIndent()

    /**
     * Nucleo A1/A2: vita quotidiana, casa, cibo, corpo e prime situazioni
     * sanitarie. Il quinto campo è il livello CEFR.
     */
    private val grezzoA1A2 = """
        die Familie|famiglia|Meine Familie wohnt in Brixen.|Famiglia|A1
        die Mutter|madre|Meine Mutter kocht gern.|Famiglia|A1
        der Vater|padre|Mein Vater arbeitet im Garten.|Famiglia|A1
        die Eltern|genitori|Meine Eltern sind zu Hause.|Famiglia|A1
        der Bruder|fratello|Mein Bruder lernt Deutsch.|Famiglia|A1
        die Schwester|sorella|Meine Schwester wohnt in Bozen.|Famiglia|A1
        das Kind|bambino|Das Kind spielt im Hof.|Famiglia|A1
        der Sohn|figlio|Ihr Sohn geht zur Schule.|Famiglia|A1
        die Tochter|figlia|Die Tochter hilft in der Küche.|Famiglia|A1
        der Freund|amico|Mein Freund kommt aus Deutschland.|Relazioni|A1
        die Freundin|amica|Meine Freundin spricht Italienisch.|Relazioni|A1
        das Haus|casa|Das Haus ist alt.|Casa|A1
        die Wohnung|appartamento|Die Wohnung ist klein.|Casa|A1
        das Zimmer|stanza|Mein Zimmer ist hell.|Casa|A1
        die Küche|cucina|Die Küche ist modern.|Casa|A1
        das Bad|bagno|Das Bad ist neben dem Schlafzimmer.|Casa|A1
        der Tisch|tavolo|Das Buch liegt auf dem Tisch.|Casa|A1
        der Stuhl|sedia|Der Stuhl ist bequem.|Casa|A1
        das Bett|letto|Das Bett ist groß.|Casa|A1
        die Tür|porta|Die Tür ist offen.|Casa|A1
        das Fenster|finestra|Das Fenster ist geschlossen.|Casa|A1
        das Wasser|acqua|Ich trinke viel Wasser.|Alimentazione|A1
        das Brot|pane|Das Brot ist frisch.|Alimentazione|A1
        der Apfel|mela|Der Apfel ist rot.|Alimentazione|A1
        die Milch|latte|Die Milch ist kalt.|Alimentazione|A1
        der Käse|formaggio|Der Käse kommt aus Südtirol.|Alimentazione|A1
        das Fleisch|carne|Ich esse kein Fleisch.|Alimentazione|A1
        das Gemüse|verdura|Das Gemüse ist frisch.|Alimentazione|A1
        die Stadt|città|Die Stadt ist klein.|Città|A1
        das Dorf|paese|Das Dorf liegt im Tal.|Città|A1
        die Straße|strada|Die Straße ist ruhig.|Città|A1
        der Platz|piazza|Der Platz ist im Zentrum.|Città|A1
        der Bahnhof|stazione|Der Bahnhof ist nah.|Città|A1
        der Arzt|medico|Der Arzt ist heute da.|Salute|A1
        die Ärztin|medica|Die Ärztin arbeitet im Krankenhaus.|Salute|A1
        das Krankenhaus|ospedale|Das Krankenhaus ist groß.|Salute|A1
        der Kopf|testa|Mein Kopf tut weh.|Corpo|A1
        der Arm|braccio|Mein Arm ist müde.|Corpo|A1
        das Bein|gamba|Das Bein tut weh.|Corpo|A1
        der Bauch|pancia|Mein Bauch ist voll.|Corpo|A1
        die Hand|mano|Wasch dir die Hände.|Corpo|A1
        der Fuß|piede|Mein Fuß ist kalt.|Corpo|A1
        die Schule|scuola|Die Schule beginnt um acht.|Istruzione|A1
        der Lehrer|insegnante|Der Lehrer erklärt die Regel.|Istruzione|A1
        die Lehrerin|insegnante (f.)|Die Lehrerin ist freundlich.|Istruzione|A1
        der Kurs|corso|Der Kurs dauert drei Monate.|Istruzione|A1
        das Buch|libro|Das Buch ist interessant.|Istruzione|A1
        der Tag|giorno|Der Tag war lang.|Tempo|A1
        die Woche|settimana|Die Woche beginnt am Montag.|Tempo|A1
        der Monat|mese|Der Monat ist fast vorbei.|Tempo|A1
        das Jahr|anno|Das Jahr hat zwölf Monate.|Tempo|A1
        heute|oggi|Heute lerne ich Deutsch.|Tempo|A1
        morgen|domani|Morgen habe ich einen Termin.|Tempo|A1
        gestern|ieri|Gestern war ich in Bozen.|Tempo|A1
        jetzt|adesso|Jetzt trinke ich Kaffee.|Tempo|A1
        immer|sempre|Ich bin immer pünktlich.|Tempo|A1
        oft|spesso|Ich gehe oft spazieren.|Tempo|A1
        manchmal|a volte|Manchmal koche ich allein.|Tempo|A1
        nie|mai|Ich bin nie müde.|Tempo|A1

        die Anmeldung|registrazione|Die Anmeldung ist online.|Burocrazia|A2
        das Formular|modulo|Bitte füllen Sie das Formular aus.|Burocrazia|A2
        der Ausweis|documento d'identità|Zeigen Sie bitte Ihren Ausweis.|Burocrazia|A2
        die Unterschrift|firma|Hier fehlt die Unterschrift.|Burocrazia|A2
        der Antrag|richiesta|Ich stelle einen Antrag.|Burocrazia|A2
        die Frist|termine|Die Frist endet am Freitag.|Burocrazia|A2
        die Bestätigung|conferma|Ich warte auf die Bestätigung.|Burocrazia|A2
        die Öffnungszeiten|orari di apertura|Die Öffnungszeiten sind im Internet.|Burocrazia|A2
        der Schalter|sportello|Bitte gehen Sie zum Schalter drei.|Burocrazia|A2
        die Steuer|tassa|Die Steuer ist dieses Jahr höher.|Denaro|A2
        die Rechnung|fattura|Die Rechnung ist schon bezahlt.|Denaro|A2
        das Konto|conto|Mein Konto ist leer.|Denaro|A2
        die Versicherung|assicurazione|Die Versicherung zahlt den Schaden.|Denaro|A2
        der Lohn|salario|Der Lohn kommt am Monatsende.|Lavoro|A2
        die Pause|pausa|Wir machen eine kurze Pause.|Lavoro|A2
        die Schicht|turno|Ich arbeite in der Frühschicht.|Lavoro|A2
        der Urlaub|ferie|Ich nehme im August Urlaub.|Lavoro|A2
        die Krankmeldung|comunicazione di malattia|Die Krankmeldung muss heute kommen.|Lavoro|A2
        die Apotheke|farmacia|Die Apotheke ist nebenan.|Salute|A2
        das Medikament|medicinale|Das Medikament hilft schnell.|Salute|A2
        die Tablette|compressa|Nehmen Sie eine Tablette am Tag.|Salute|A2
        die Untersuchung|visita|Die Untersuchung dauert zehn Minuten.|Salute|A2
        der Blutdruck|pressione sanguigna|Der Blutdruck ist normal.|Salute|A2
        das Fieber|febbre|Das Fieber ist gesunken.|Salute|A2
        die Erkältung|raffreddore|Ich habe eine Erkältung.|Salute|A2
        der Husten|tosse|Der Husten ist schlimm.|Salute|A2
        die Allergie|allergia|Ich habe eine Allergie gegen Nüsse.|Salute|A2
        der Notfall|emergenza|Im Notfall rufen Sie 112 an.|Salute|A2
        die Notaufnahme|pronto soccorso|Die Notaufnahme ist im Erdgeschoss.|Salute|A2
        der Zug|treno|Der Zug fährt um sieben.|Viaggio|A2
        der Bus|autobus|Der Bus kommt in fünf Minuten.|Viaggio|A2
        das Auto|auto|Das Auto steht vor dem Haus.|Viaggio|A2
        das Fahrrad|bicicletta|Ich fahre mit dem Fahrrad.|Viaggio|A2
        die Karte|biglietto o carta|Ich kaufe eine Karte.|Viaggio|A2
        die Richtung|direzione|In welche Richtung gehen Sie?|Viaggio|A2
        die Kreuzung|incrocio|An der Kreuzung links abbiegen.|Viaggio|A2
        die Nähe|vicinanza|Die Wohnung ist in der Nähe vom Zentrum.|Città|A2
        die Entfernung|distanza|Die Entfernung ist kurz.|Città|A2
        der Lärm|rumore|Der Lärm stört mich.|Città|A2
        die Ruhe|quiete|Ich brauche Ruhe.|Città|A2
        die Möglichkeit|possibilità|Es gibt viele Möglichkeiten.|Società|A2
        die Entscheidung|decisione|Die Entscheidung ist schwer.|Società|A2
        die Hilfe|aiuto|Danke für die Hilfe.|Relazioni|A2
        der Rat|consiglio|Danke für den Rat.|Relazioni|A2
        die Frage|domanda|Ich habe eine Frage.|Relazioni|A2
        die Antwort|risposta|Die Antwort ist richtig.|Relazioni|A2
        das Gespräch|conversazione|Das Gespräch war hilfreich.|Relazioni|A2
    """.trimIndent()

    /**
     * Lessico sanitario per il lavoro in reparto: termini che compaiono
     * nella documentazione, nelle consegne e nella comunicazione al paziente.
     */
    private val grezzoSanita = """
        die Aufnahme|accettazione, ricovero|Die Aufnahme ist um acht Uhr.|Salute|B1
        die Visite|visita medica|Die Visite beginnt um sieben.|Salute|B1
        die Pflege|cura|Die Pflege braucht Zeit.|Salute|B1
        die Pflegekraft|operatore sanitario|Die Pflegekraft hilft beim Waschen.|Salute|B1
        der Pflegebedarf|bisogno assistenziale|Der Pflegebedarf ist hoch.|Salute|B1
        die Mobilisation|mobilizzazione|Die Mobilisation beginnt früh.|Salute|B1
        die Lagerung|posizionamento|Die Lagerung muss regelmäßig wechseln.|Salute|B1
        der Verband|benda|Der Verband muss gewechselt werden.|Salute|B1
        die Wunde|ferita|Die Wunde ist sauber.|Salute|B1
        die Narbe|cicatrice|Die Narbe ist noch rot.|Salute|B1
        die Schwellung|gonfiore|Die Schwellung ist zurückgegangen.|Salute|B1
        die Bewegungstherapie|terapia del movimento|Die Bewegungstherapie hilft beim Gehen.|Salute|B1
        die Rehabilitation|riabilitazione|Die Rehabilitation dauert sechs Wochen.|Salute|B1
        der Rollstuhl|sedia a rotelle|Der Patient braucht einen Rollstuhl.|Salute|B1
        die Gehhilfe|ausilio per camminare|Die Gehhilfe steht neben dem Bett.|Salute|B1
        der Stock|bastone|Er geht mit einem Stock.|Salute|B1
        die Treppe|scala|Die Treppe ist rutschig.|Salute|B1
        der Aufzug|ascensore|Der Aufzug ist außer Betrieb.|Salute|B1
        die Station|reparto|Ich arbeite auf der Station.|Salute|B1
        die Ambulanz|ambulatorio|Die Ambulanz ist geöffnet.|Salute|B1
        die Praxis|studio medico|Die Praxis ist im ersten Stock.|Salute|B1
        der Facharzt|medico specialista|Der Facharzt kommt morgen.|Salute|B1
        die Krankengeschichte|storia clinica|Die Krankengeschichte ist vollständig.|Salute|B1
        die Medikamentenliste|elenco dei farmaci|Die Medikamentenliste ist aktuell.|Salute|B1
        die Nebenwirkung|effetto collaterale|Die Nebenwirkung ist selten.|Salute|B1
        die Dosierung|dosaggio|Die Dosierung steht auf dem Rezept.|Salute|B1
        die Injektion|iniezione|Die Injektion tut nicht weh.|Salute|B1
        die Blutentnahme|prelievo del sangue|Die Blutentnahme ist um sieben.|Salute|B1
        der Verbandwechsel|cambio della medicazione|Der Verbandwechsel erfolgt täglich.|Salute|B1
        die Wundkontrolle|controllo della ferita|Die Wundkontrolle ist wichtig.|Salute|B1
        die Schmerztherapie|terapia del dolore|Die Schmerztherapie wird angepasst.|Salute|B1
        die Sturzprophylaxe|prevenzione delle cadute|Die Sturzprophylaxe ist Teil der Pflege.|Salute|B1
        die Hygiene|igiene|Hygiene ist im Krankenhaus zentral.|Salute|B1
        die Desinfektion|disinfezione|Die Desinfektion der Hände dauert 30 Sekunden.|Salute|B1
        die Schutzausrüstung|dispositivi di protezione|Die Schutzausrüstung ist Pflicht.|Salute|B1
        der Notruf|chiamata di emergenza|Der Notruf ist kostenlos.|Salute|B1
        die Vitalzeichen|parametri vitali|Die Vitalzeichen sind stabil.|Salute|B1
        die Atmung|respirazione|Die Atmung ist ruhig.|Salute|B1
        der Puls|polso|Der Puls ist regelmäßig.|Salute|B1
        die Temperatur|temperatura|Die Temperatur ist erhöht.|Salute|B1
        der Blutzucker|glicemia|Der Blutzucker wird gemessen.|Salute|B1
        die Inkontinenz|incontinenza|Die Inkontinenz wird behandelt.|Salute|B1
        die Demenz|demenza|Die Demenz verändert den Alltag.|Salute|B1
        die Orientierung|orientamento|Die Orientierung fällt schwer.|Salute|B1
        der Sturz|caduta|Der Sturz hatte keine Folgen.|Salute|B1
        die Prothese|protesi|Die Prothese passt gut.|Salute|B1
        die OP|intervento|Die OP ist für morgen geplant.|Salute|B1
        die Narkose|anestesia|Die Narkose ist ungefährlich.|Salute|B1
        die Nachsorge|follow-up|Die Nachsorge ist wichtig.|Salute|B1
        die Angehörigen|familiari|Die Angehörigen warten draußen.|Salute|B1
        die Aufklärung|informazione al paziente|Die Aufklärung erfolgt vor der OP.|Salute|B1
        die Einwilligung|consenso|Die Einwilligung fehlt noch.|Salute|B1
        die Schweigepflicht|segreto professionale|Die Schweigepflicht gilt immer.|Salute|B1
        die Übergabe|passaggio di consegne|Die Übergabe ist um sieben.|Salute|B1
        der Dienstplan|piano turni|Der Dienstplan kommt am Freitag.|Lavoro|B1
        die Dokumentation|documentazione|Die Dokumentation muss vollständig sein.|Lavoro|B1
        die Wundversorgung|medicazione della ferita|Die Wundversorgung erfolgt steril.|Salute|B1
        die Medikamentengabe|somministrazione dei farmaci|Die Medikamentengabe wird dokumentiert.|Salute|B1
        die Schmerzskala|scala del dolore|Die Schmerzskala geht von null bis zehn.|Salute|B1
        die Sturzgefahr|rischio di caduta|Bei Sturzgefahr braucht der Patient Hilfe.|Salute|B1
        die Immobilität|immobilità|Die Immobilität erhöht das Risiko.|Salute|B1
        die Kontraktur|contrattura|Eine Kontraktur kann vermieden werden.|Salute|B1
        die Pneumonie|polmonite|Die Pneumonie muss früh erkannt werden.|Salute|B1
        die Thrombose|trombosi|Bewegung senkt das Thromboserisiko.|Salute|B1
        der Dekubitus|lesione da pressione|Der Dekubitus braucht eine spezielle Pflege.|Salute|B1
    """.trimIndent()

    val tutte: List<Voce> = (grezzo.lines() + grezzoA1A2.lines() + grezzoSanita.lines())
        .mapNotNull { riga ->
            val parti = riga.split("|")
            if (parti.size !in 4..5) return@mapNotNull null
            val tedesco = parti[0].trim()
            val italiano = parti[1].trim()
            if (tedesco.isEmpty() || italiano.isEmpty()) null
            else Voce(
                tedesco = tedesco,
                italiano = italiano,
                esempio = parti[2].trim(),
                tema = parti[3].trim(),
                livello = parti.getOrNull(4)?.trim()?.takeIf { it.isNotBlank() } ?: "B1"
            )
        }

    val temi: List<String> get() = tutte.map { it.tema }.distinct()

    val livelli: List<String> get() =
        listOf("A1", "A2", "B1", "B2").filter { livello ->
            tutte.any { it.livello == livello }
        }

    fun perLivello(livello: String): List<Voce> =
        tutte.filter { it.livello.equals(livello, ignoreCase = true) }

    fun perTema(tema: String): List<Voce> = tutte.filter { it.tema.equals(tema, ignoreCase = true) }

    fun cerca(query: String): List<Voce> {
        val q = query.trim()
        if (q.isEmpty()) return tutte
        return tutte.filter {
            it.tedesco.contains(q, ignoreCase = true) ||
                it.italiano.contains(q, ignoreCase = true)
        }
    }

    /** Articolo separato dalla parola (per il vocabolario). */
    fun articoloDi(tedesco: String): String? {
        val primo = tedesco.substringBefore(" ").lowercase()
        return if (primo in setOf("der", "die", "das")) primo else null
    }

    fun parolaSenzaArticolo(tedesco: String): String =
        if (articoloDi(tedesco) != null) tedesco.substringAfter(" ") else tedesco
}
