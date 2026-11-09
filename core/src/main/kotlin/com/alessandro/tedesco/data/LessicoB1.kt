package com.alessandro.tedesco.data

/**
 * Lessico curato B1: tedesco|italiano|esempio|tema.
 *
 * 1.125 parole nuove rispetto ad A1+A2 (contando anche [WortlisteB1]); con i
 * livelli precedenti la copertura arriva a 2.503 parole (traguardo cumulativo
 * B1: 2.400). Le voci già presenti nei livelli precedenti sono state rimosse.
 */
object LessicoB1 {

    val grezzo: String = """
        die Bewerbung|candidatura|Meine Bewerbung ist fertig.|Lavoro
        die Stellenanzeige|annuncio di lavoro|Die Stellenanzeige ist interessant.|Lavoro
        das Anschreiben|lettera di accompagnamento|Das Anschreiben muss kurz sein.|Lavoro
        die Qualifikation|qualifica|Die Qualifikation passt zur Stelle.|Lavoro
        die Berufserfahrung|esperienza professionale|Ich habe viel Berufserfahrung.|Lavoro
        die Probezeit|periodo di prova|Die Probezeit dauert sechs Monate.|Lavoro
        der Arbeitsvertrag|contratto di lavoro|Der Arbeitsvertrag ist befristet.|Lavoro
        die Arbeitszeit|orario di lavoro|Die Arbeitszeit ist flexibel.|Lavoro
        die Teilzeit|tempo parziale|Ich arbeite in Teilzeit.|Lavoro
        die Vollzeit|tempo pieno|Er sucht eine Vollzeitstelle.|Lavoro
        der Feiertag|giorno festivo|Der Feiertag ist am Montag.|Lavoro
        die Pause|pausa|Wir machen eine kurze Pause.|Lavoro
        die Kantine|mensa aziendale|Das Essen in der Kantine ist gut.|Lavoro
        die Fortbildung|aggiornamento|Die Fortbildung ist Pflicht.|Lavoro
        die Beförderung|promozione|Sie hat eine Beförderung bekommen.|Lavoro
        die Kündigungsfrist|termine di preavviso|Die Kündigungsfrist beträgt drei Monate.|Lavoro
        die Arbeitslosigkeit|disoccupazione|Die Arbeitslosigkeit ist gestiegen.|Lavoro
        der Mangel|carestia|Es gibt einen Mangel an Pflegekräften.|Lavoro
        die Fachkraft|specialista|Wir suchen eine Fachkraft.|Lavoro
        der Auszubildende|apprendista|Der Auszubildende lernt drei Jahre.|Lavoro
        der Praktikant|stagista|Der Praktikant bleibt sechs Monate.|Lavoro
        das Praktikum|tirocinio|Das Praktikum war sehr lehrreich.|Lavoro
        die Verhandlung|negoziazione|Die Verhandlung war schwierig.|Lavoro
        die Einigung|accordo|Wir haben eine Einigung gefunden.|Lavoro
        der Kompromiss|compromesso|Der Kompromiss ist akzeptabel.|Lavoro
        die Gewerkschaft|sindacato|Die Gewerkschaft verhandelt mit der Firma.|Lavoro
        der Streik|sciopero|Der Streik dauert zwei Tage.|Lavoro
        die Rente|pensione|Er geht mit sechzig in Rente.|Lavoro
        die Krankenkasse|cassa malati|Die Krankenkasse übernimmt die Kosten.|Lavoro
        die Lohnsteuer|imposta sul salario|Die Lohnsteuer wird automatisch abgezogen.|Lavoro
        die Patientenverfügung|disposizioni anticipate di trattamento|Die Patientenverfügung liegt in der Akte.|Sanità
        die Aufklärung|informazione al paziente|Die Aufklärung erfolgt vor der Operation.|Sanità
        die Einwilligung|consenso|Der Patient hat die Einwilligung unterschrieben.|Sanità
        die Diagnose|diagnosi|Die Diagnose ist noch unklar.|Sanità
        die Therapie|terapia|Die Therapie dauert sechs Wochen.|Sanità
        die Nebenwirkung|effetto collaterale|Die Nebenwirkung ist selten.|Sanità
        die Dosierung|dosaggio|Die Dosierung steht auf dem Rezept.|Sanità
        die Infusion|infusione|Die Infusion läuft langsam.|Sanità
        der Verband|benda|Der Verband muss gewechselt werden.|Sanità
        die Naht|cucitura|Die Naht ist gut verheilt.|Sanità
        die Reha|riabilitazione|Die Reha beginnt im Mai.|Sanità
        die Pflege|cura|Die Pflege braucht Zeit.|Sanità
        der Pflegebedürftige|persona bisognosa di cure|Der Pflegebedürftige bekommt Hilfe.|Sanità
        die Pflegekraft|operatore sanitario|Die Pflegekraft hat Nachtdienst.|Sanità
        die Übergabe|passaggio di consegne|Die Übergabe ist um sieben.|Sanità
        die Visite|visita medica|Die Visite ist um acht Uhr.|Sanità
        die Anamnese|anamnesi|Die Anamnese ist vollständig.|Sanità
        die Vitalparameter|parametri vitali|Die Vitalparameter sind stabil.|Sanità
        die Desinfektion|disinfezione|Die Desinfektion der Hände ist Pflicht.|Sanità
        die Hygiene|igiene|Hygiene ist im Krankenhaus zentral.|Sanità
        der Notruf|chiamata di emergenza|Der Notruf kam um 3 Uhr.|Sanità
        die Wiederbelebung|rianimazione|Die Wiederbelebung war erfolgreich.|Sanità
        die Chroniker|paziente cronico|Chroniker brauchen regelmäßige Kontrollen.|Sanità
        die Vorsorgeuntersuchung|esame preventivo|Die Vorsorgeuntersuchung ist gratis.|Sanità
        der Risikofaktor|fattore di rischio|Rauchen ist ein Risikofaktor.|Sanità
        die Studie|studio|Die Studie zeigt ein klares Ergebnis.|Sanità
        die Statistik|statistica|Die Statistik ist eindeutig.|Sanità
        die Ursache|causa|Die Ursache ist unbekannt.|Sanità
        die Folge|conseguenza|Die Folge ist eine lange Therapie.|Sanità
        der Zusammenhang|nesso|Es gibt einen Zusammenhang zwischen Stress und Schlaf.|Sanità
        die Wahrscheinlichkeit|probabilità|Die Wahrscheinlichkeit ist gering.|Sanità
        die Maßnahme|misura|Die Maßnahme war notwendig.|Sanità
        die Gesellschaft|società|Die Gesellschaft verändert sich.|Società
        die Bevölkerung|popolazione|Die Bevölkerung wächst langsam.|Società
        die Regierung|governo|Die Regierung hat entschieden.|Società
        die Verwaltung|amministrazione|Die Verwaltung ist langsam.|Società
        die Behörde|autorità|Die Behörde antwortet schriftlich.|Società
        das Amt|ufficio pubblico|Das Amt ist am Nachmittag zu.|Società
        die Steuererklärung|dichiarazione dei redditi|Die Steuererklärung ist fällig.|Società
        der Ausweis|documento|Der Ausweis läuft 2027 ab.|Società
        die Aufenthaltsgenehmigung|permesso di soggiorno|Die Aufenthaltsgenehmigung ist gültig.|Società
        die Staatsbürgerschaft|cittadinanza|Sie hat die Staatsbürgerschaft beantragt.|Società
        die Wahl|elezione|Die Wahl ist im Herbst.|Società
        die Partei|partito|Die Partei hat viele Stimmen verloren.|Società
        der Bürgermeister|sindaco|Der Bürgermeister wohnt hier.|Società
        die Gemeinde|comune|Die Gemeinde organisiert ein Fest.|Società
        die Steuer|tassa|Die Steuer ist dieses Jahr höher.|Società
        die Gebühr|tassa|Die Gebühr beträgt zwanzig Euro.|Società
        die Versicherung|assicurazione|Die Versicherung deckt den Schaden.|Società
        der Vertrag|contratto|Der Vertrag läuft ein Jahr.|Società
        die Kündigung|disdetta|Die Kündigung ist schriftlich nötig.|Società
        die Wohnungssuche|ricerca casa|Die Wohnungssuche ist schwierig.|Società
        die Miete|affitto|Die Miete ist bezahlbar.|Società
        der Umzug|trasloco|Der Umzug war teuer.|Società
        die Nachbarschaft|vicinato|Die Nachbarschaft ist freundlich.|Società
        der Verein|associazione|Der Verein hat viele Mitglieder.|Società
        das Ehrenamt|volontariato|Das Ehrenamt ist wichtig.|Società
        die Spende|donazione|Die Spende kam der Familie zugute.|Società
        der Klimawandel|cambiamento climatico|Der Klimawandel ist eine Tatsache.|Ambiente
        der CO2-Ausstoß|emissioni di CO2|Der CO2-Ausstoß muss sinken.|Ambiente
        die Nachhaltigkeit|sostenibilità|Nachhaltigkeit ist ein Ziel.|Ambiente
        die erneuerbare Energie|energia rinnovabile|Erneuerbare Energie ist wichtig.|Ambiente
        die Solaranlage|impianto solare|Die Solaranlage liefert Strom.|Ambiente
        die Dürre|siccità|Die Dürre hat die Ernte zerstört.|Ambiente
        die Überschwemmung|allagamento|Die Überschwemmung war schwer.|Ambiente
        die Wiederverwertung|riciclo|Die Wiederverwertung spart Rohstoffe.|Ambiente
        der Rohstoff|materia prima|Rohstoffe werden teurer.|Ambiente
        der Verbrauch|consumo|Der Verbrauch ist zu hoch.|Ambiente
        die Verschwendung|spreco|Die Verschwendung von Lebensmitteln ist ein Problem.|Ambiente
        das Insekt|insetto|Insekten sind wichtig für die Natur.|Ambiente
        die Art|specie|Diese Art ist bedroht.|Ambiente
        der Wald|bosco|Der Wald ist geschützt.|Ambiente
        der See|lago|Der See ist im Sommer warm.|Ambiente
        die Küste|costa|Die Küste ist gefährdet.|Ambiente
        die Landschaft|paesaggio|Die Landschaft ist einzigartig.|Ambiente
        die Gegend|zona|Die Gegend ist ruhig.|Ambiente
        die Grenze|confine|Die Grenze ist nur zehn Kilometer entfernt.|Ambiente
        die Industrie|industria|Die Industrie bietet Arbeitsplätze.|Economia
        der Handel|commercio|Der Handel wächst wieder.|Economia
        die Wirtschaft|economia|Die Wirtschaft erholt sich.|Economia
        der Markt|mercato|Der Markt ist gesättigt.|Economia
        die Nachfrage|domanda|Die Nachfrage ist groß.|Economia
        das Angebot|offerta|Das Angebot ist begrenzt.|Economia
        der Gewinn|profitto|Der Gewinn ist gestiegen.|Economia
        der Verlust|perdita|Der Verlust war hoch.|Economia
        die Kosten|spese|Die Kosten sind gestiegen.|Economia
        der Umsatz|fatturato|Der Umsatz ist stabil.|Economia
        die Investition|investimento|Die Investition lohnt sich.|Economia
        die Schuld|debito|Die Schuld muss zurückgezahlt werden.|Economia
        der Kredit|credito|Der Kredit läuft zehn Jahre.|Economia
        die Zinsen|interessi|Die Zinsen sind niedrig.|Economia
        die Inflation|inflazione|Die Inflation ist spürbar.|Economia
        die Krise|crisi|Die Krise trifft kleine Betriebe.|Economia
        der Wettbewerb|concorrenza|Der Wettbewerb ist hart.|Economia
        die Kundschaft|clientela|Die Kundschaft ist zufrieden.|Economia
        der Service|servizio|Der Service ist schnell.|Economia
        die Qualität|qualità|Die Qualität stimmt.|Economia
        der Ruf|reputazione|Der Ruf der Firma ist gut.|Economia
        die Ausbildung|formazione|Die Ausbildung ist grundlegend.|Istruzione
        der Abschluss|diploma|Der Abschluss ist anerkannt.|Istruzione
        die Zulassung|ammissione|Die Zulassung zum Studium ist schwer.|Istruzione
        die Vorlesung|lezione universitaria|Die Vorlesung war langweilig.|Istruzione
        das Seminar|seminario|Das Seminar findet online statt.|Istruzione
        die Fachhochschule|scuola superiore specializzata|Die Fachhochschule bietet diesen Kurs an.|Istruzione
        die Lehre|apprendistato|Die Lehre dauert vier Jahre.|Istruzione
        der Lehrling|apprendista|Der Lehrling lernt im Betrieb.|Istruzione
        das Zeugnis|certificato|Das Zeugnis ist übersetzt.|Istruzione
        die Bewertung|valutazione|Die Bewertung erfolgt nach Punkten.|Istruzione
        der Durchschnitt|media|Der Durchschnitt ist gut.|Istruzione
        die Nachhilfe|ripetizioni|Sie gibt Nachhilfe in Mathe.|Istruzione
        die Lernmethode|metodo di studio|Die Lernmethode ist effektiv.|Istruzione
        die Konzentration|concentrazione|Die Konzentration lässt nach.|Istruzione
        die Erinnerung|ricordo|Die Erinnerung ist noch frisch.|Istruzione
        der Wortschatz|lessico|Mein Wortschatz wächst.|Istruzione
        die Flüssigkeit|fluidità|Die Flüssigkeit beim Sprechen ist wichtig.|Istruzione
        der Akzent|accento|Der Akzent ist leicht zu verstehen.|Istruzione
        die Sicherheit|sicurezza|Mit der Zeit kommt Sicherheit.|Istruzione
        die Freundschaft|amicizia|Die Freundschaft hält seit Jahren.|Emozioni
        die Liebe|amore|Die Liebe braucht Zeit.|Emozioni
        die Eifersucht|gelosia|Eifersucht zerstört Vertrauen.|Emozioni
        die Hoffnung|speranza|Die Hoffnung stirbt zuletzt.|Emozioni
        die Enttäuschung|delusione|Die Enttäuschung war groß.|Emozioni
        die Überraschung|sorpresa|Die Überraschung ist gelungen.|Emozioni
        die Neugier|curiosità|Neugier treibt uns an.|Emozioni
        der Mut|coraggio|Mut ist ansteckend.|Emozioni
        die Scham|vergogna|Sie hat die Scham überwunden.|Emozioni
        der Stolz|orgoglio|Sein Stolz hat ihn gebremst.|Emozioni
        die Wut|rabbia|Die Wut war schnell vorbei.|Emozioni
        die Trauer|lutto|Die Trauer braucht Zeit.|Emozioni
        die Einsamkeit|solitudine|Einsamkeit kann krank machen.|Emozioni
        die Gelassenheit|serenità|Gelassenheit hilft im Alltag.|Emozioni
        das Mitleid|compassione|Mitleid ist keine Lösung.|Emozioni
        die Zufriedenheit|soddisfazione|Zufriedenheit ist ein gutes Ziel.|Emozioni
        der Humor|umorismo|Humor verbindet Menschen.|Emozioni
        das Bedürfnis|bisogno|Das Bedürfnis nach Schlaf ist groß.|Emozioni
        die Belastung|carico|Die Belastung ist hoch.|Emozioni
        die Erschöpfung|esaurimento|Die Erschöpfung war deutlich.|Emozioni
        der Umgang|rapporto|Der Umgang mit Stress will gelernt sein.|Emozioni
        die Rücksicht|riguardo|Nimm bitte Rücksicht auf ihn.|Emozioni
        das Vertrauen|fiducia|Vertrauen ist die Basis.|Emozioni
        der Respekt|rispetto|Respekt ist wichtig.|Emozioni
        die Geduld|pazienza|Geduld zahlt sich aus.|Emozioni
        die Technik|tecnologia|Die Technik entwickelt sich schnell.|Media
        die Digitalisierung|digitalizzazione|Die Digitalisierung verändert die Arbeit.|Media
        das Netzwerk|rete|Das Netzwerk ist sicher.|Media
        die Daten|dati|Die Daten sind geschützt.|Media
        der Datenschutz|protezione dei dati|Datenschutz ist gesetzlich geregelt.|Media
        die Sicherheitslücke|falla di sicurezza|Die Sicherheitslücke wurde geschlossen.|Media
        der Zugang|accesso|Der Zugang ist gesperrt.|Media
        das Konto|account|Mein Konto wurde gesperrt.|Media
        die Anwendung|applicazione|Die Anwendung ist kostenlos.|Media
        die Website|sito web|Die Website ist neu.|Media
        der Inhalt|contenuto|Der Inhalt ist problematisch.|Media
        die Quelle|fonte|Die Quelle ist unklar.|Media
        die Fälschung|falso|Die Fälschung ist offensichtlich.|Media
        die Veröffentlichung|pubblicazione|Die Veröffentlichung war ein Fehler.|Media
        die Kritik|critica|Die Kritik war berechtigt.|Media
        die Diskussion|discussione|Die Diskussion war lang.|Media
        die Debatte|dibattito|Die Debatte ist wichtig.|Media
        der Vortrag|conferenza|Der Vortrag war spannend.|Media
        die Präsentation|presentazione|Die Präsentation dauert zwanzig Minuten.|Media
        die Grafik|grafico|Die Grafik zeigt den Trend.|Media
        die Werbung|pubblicità|Die Werbung zielt auf junge Leute.|Media
        der Roman|romanzo|Der Roman ist sehr gut.|Cultura
        die Erzählung|racconto|Die Erzählung ist kurz.|Cultura
        die Geschichte|storia|Die Geschichte ist wahr.|Cultura
        die Poesie|poesia|Poesie liest er selten.|Cultura
        das Theater|teatro|Das Theater ist ausverkauft.|Cultura
        die Ausstellung|mostra|Die Ausstellung ist kostenlos.|Cultura
        das Museum|museo|Das Museum ist montags zu.|Cultura
        das Gemälde|dipinto|Das Gemälde ist wertvoll.|Cultura
        die Skulptur|scultura|Die Skulptur steht im Garten.|Cultura
        die Architektur|architettura|Die Architektur ist beeindruckend.|Cultura
        die Tradition|tradizione|Die Tradition wird gepflegt.|Cultura
        der Brauch|usanza|Der Brauch ist regional.|Cultura
        das Brauchtum|tradizioni popolari|Das Brauchtum ist lebendig.|Cultura
        die Religion|religione|Die Religion spielt eine Rolle.|Cultura
        die Kirche|chiesa|Die Kirche ist alt.|Cultura
        der Glaube|fede|Der Glaube gibt Halt.|Cultura
        die Feier|celebrazione|Die Feier war schön.|Cultura
        das Konzert|concerto|Das Konzert beginnt um acht.|Cultura
        die Bühne|palco|Die Bühne ist groß.|Cultura
        das Publikum|pubblico|Das Publikum war begeistert.|Cultura
        der Künstler|artista|Der Künstler lebt in Wien.|Cultura
        der Schriftsteller|scrittore|Der Schriftsteller ist berühmt.|Cultura
        die Bedeutung|significato|Die Bedeutung ist unklar.|Cultura
        die Herkunft|origine|Die Herkunft des Wortes ist lateinisch.|Cultura
        die Entwicklung|sviluppo|Die Entwicklung ist positiv.|Cultura
        die Veränderung|cambiamento|Die Veränderung braucht Zeit.|Cultura
        die Verbesserung|miglioramento|Die Verbesserung ist sichtbar.|Cultura
        die Verschlechterung|peggioramento|Die Verschlechterung kam plötzlich.|Cultura
        der Fortschritt|progresso|Der Fortschritt ist langsam.|Cultura
        der Rückgang|calo|Der Rückgang ist deutlich.|Cultura
        der Anstieg|aumento|Der Anstieg war steil.|Cultura
        die Wirkung|effetto|Die Wirkung ist stark.|Cultura
        der Zweck|scopo|Der Zweck ist klar.|Cultura
        die Absicht|intenzione|Die Absicht war gut.|Cultura
        der Eindruck|impressione|Der Eindruck war positiv.|Cultura
        die Erwartung|aspettativa|Die Erwartung war zu hoch.|Cultura
        die Voraussetzung|presupposto|Die Voraussetzung ist Erfahrung.|Cultura
        die Bedingung|condizione|Die Bedingung ist akzeptabel.|Cultura
        der Gegensatz|contrasto|Der Gegensatz ist offensichtlich.|Cultura
        der Unterschied|differenza|Der Unterschied ist klein.|Cultura
        die Ähnlichkeit|somiglianza|Die Ähnlichkeit ist verblüffend.|Cultura
        der Vergleich|confronto|Der Vergleich ist hilfreich.|Cultura
        die Ausnahme|eccezione|Das ist eine Ausnahme.|Cultura
        die Regel|regola|Die Regel gilt für alle.|Cultura
        der Fall|caso|In diesem Fall rufe ich an.|Cultura
        die Lage|situazione|Die Lage ist schwierig.|Cultura
        der Zustand|stato|Der Zustand ist stabil.|Cultura
        die Lösung|soluzione|Die Lösung ist einfach.|Cultura
        der Vorschlag|proposta|Der Vorschlag wurde angenommen.|Cultura
        die Entscheidung|decisione|Die Entscheidung ist gefallen.|Cultura
        die Möglichkeit|possibilità|Die Möglichkeit besteht.|Cultura
        die Notwendigkeit|necessità|Die Notwendigkeit ist klar.|Cultura
        die Schwierigkeit|difficoltà|Die Schwierigkeit liegt im Detail.|Cultura
        die Herausforderung|sfida|Die Herausforderung ist groß.|Cultura
        berücksichtigen|considerare|Bitte berücksichtigen Sie die Kosten.|Verbi B1
        beeinflussen|influenzare|Das Wetter beeinflusst die Stimmung.|Verbi B1
        sich auswirken|avere effetto|Der Preis wirkt sich auf die Nachfrage aus.|Verbi B1
        verursachen|causare|Der Unfall verursachte einen Stau.|Verbi B1
        vermeiden|evitare|Wir vermeiden Fehler.|Verbi B1
        verhindern|impedire|Der Regen verhinderte das Fest.|Verbi B1
        ermöglichen|rendere possibile|Das Stipendium ermöglicht das Studium.|Verbi B1
        erfordern|richiedere|Die Stelle erfordert Erfahrung.|Verbi B1
        erfüllen|soddisfare|Er erfüllt alle Bedingungen.|Verbi B1
        übernehmen|assumere|Ich übernehme die Verantwortung.|Verbi B1
        übertragen|delegare|Er überträgt mir die Aufgabe.|Verbi B1
        zusammenfassen|riassumere|Fassen Sie den Text zusammen.|Verbi B1
        darstellen|rappresentare|Die Grafik stellt den Trend dar.|Verbi B1
        beschreiben|descrivere|Beschreiben Sie den Ablauf.|Verbi B1
        erläutern|illustrare|Können Sie das erläutern?|Verbi B1
        begründen|motivare|Begründen Sie Ihre Antwort.|Verbi B1
        beurteilen|valutare|Der Arzt beurteilt den Zustand.|Verbi B1
        bewerten|valutare|Die Kommission bewertet die Arbeit.|Verbi B1
        entscheiden|decidere|Wir entscheiden morgen.|Verbi B1
        sich entschließen|decidersi|Ich habe mich entschlossen zu bleiben.|Verbi B1
        verzichten|rinunciare|Ich verzichte auf Zucker.|Verbi B1
        sich beschäftigen|occuparsi|Er beschäftigt sich mit dem Thema.|Verbi B1
        sich bemühen|impegnarsi|Sie bemüht sich sehr.|Verbi B1
        sich gewöhnen|abituarsi|Ich gewöhne mich an das Klima.|Verbi B1
        sich verhalten|comportarsi|Wie verhält sich der Patient?|Verbi B1
        sich verändern|cambiare|Die Stadt verändert sich.|Verbi B1
        sich verbessern|migliorare|Mein Deutsch verbessert sich.|Verbi B1
        sich verschlechtern|peggiorare|Sein Zustand verschlechtert sich.|Verbi B1
        sich erhöhen|aumentare|Die Kosten erhöhen sich.|Verbi B1
        sinken|diminuire|Die Zahlen sinken.|Verbi B1
        steigen|salire|Die Preise steigen.|Verbi B1
        zunehmen|aumentare|Die Nachfrage nimmt zu.|Verbi B1
        abnehmen|diminuire|Die Zahl nimmt ab.|Verbi B1
        gelten|valere|Die Regel gilt ab Montag.|Verbi B1
        betreffen|riguardare|Die Änderung betrifft alle.|Verbi B1
        gehören|appartenere|Das Buch gehört mir.|Verbi B1
        verfügen|disporre|Die Klinik verfügt über moderne Geräte.|Verbi B1
        benötigen|avere bisogno|Wir benötigen mehr Zeit.|Verbi B1
        gewährleisten|garantire|Wir gewährleisten die Sicherheit.|Verbi B1
        überprüfen|verificare|Bitte überprüfen Sie die Angaben.|Verbi B1
        feststellen|constatare|Der Arzt stellte eine Infektion fest.|Verbi B1
        anerkennen|riconoscere|Der Abschluss wird anerkannt.|Verbi B1
        ausdrücken|esprimere|Drücken Sie Ihre Meinung aus.|Verbi B1
        äußern|esprimere|Er äußerte Bedenken.|Verbi B1
        zustimmen|essere d'accordo|Ich stimme dem Plan zu.|Verbi B1
        ablehnen|rifiutare|Die Versicherung lehnte den Antrag ab.|Verbi B1
        anbieten|offrire|Wir bieten einen Kurs an.|Verbi B1
        empfehlen|consigliare|Ich empfehle diesen Arzt.|Verbi B1
        warnen|avvertire|Die Behörde warnt vor dem Wasser.|Verbi B1
        hinweisen|segnalare|Ich weise auf den Fehler hin.|Verbi B1
        aufmerksam machen|richiamare l'attenzione|Ich mache Sie auf die Frist aufmerksam.|Verbi B1
        berichten|riferire|Die Zeitung berichtet über den Fall.|Verbi B1
        informieren|informare|Bitte informieren Sie mich.|Verbi B1
        sich informieren|informarsi|Ich informiere mich über den Kurs.|Verbi B1
        veröffentlichen|pubblicare|Die Studie wurde veröffentlicht.|Verbi B1
        diskutieren|discutere|Wir diskutieren über die Kosten.|Verbi B1
        sich einigen|accordarsi|Wir haben uns geeinigt.|Verbi B1
        verhandeln|negoziare|Sie verhandeln über den Preis.|Verbi B1
        teilnehmen|partecipare|Ich nehme an der Sitzung teil.|Verbi B1
        stattfinden|avere luogo|Die Sitzung findet morgen statt.|Verbi B1
        organisieren|organizzare|Wer organisiert das Fest?|Verbi B1
        planen|pianificare|Wir planen den Umzug.|Verbi B1
        vorbereiten|preparare|Ich bereite die Unterlagen vor.|Verbi B1
        durchführen|eseguire|Die Untersuchung wird morgen durchgeführt.|Verbi B1
        abschließen|concludere|Wir schließen das Projekt ab.|Verbi B1
        fortsetzen|continuare|Wir setzen die Arbeit fort.|Verbi B1
        aufgeben|rinunciare|Gib nicht so schnell auf.|Verbi B1
        erreichen|raggiungere|Wir haben das Ziel erreicht.|Verbi B1
        erledigen|sbrigare|Ich erledige das heute.|Verbi B1
        sich kümmern|occuparsi|Ich kümmere mich um den Termin.|Verbi B1
        zuständig sein|essere competente|Wer ist dafür zuständig?|Verbi B1
        verantwortlich sein|essere responsabile|Sie ist für die Station verantwortlich.|Verbi B1
        zuverlässig|affidabile|Er ist sehr zuverlässig.|Aggettivi B1
        pünktlich|puntuale|Der Zug war pünktlich.|Aggettivi B1
        flexibel|flessibile|Die Arbeitszeiten sind flexibel.|Aggettivi B1
        anspruchsvoll|impegnativo|Die Aufgabe ist anspruchsvoll.|Aggettivi B1
        deutlich|chiaro|Der Unterschied ist deutlich.|Aggettivi B1
        genauer|più preciso|Bitte genauer erklären.|Aggettivi B1
        ungefähr|circa|Es dauert ungefähr eine Stunde.|Aggettivi B1
        sofort|subito|Kommen Sie sofort!|Aggettivi B1
        demnächst|prossimamente|Der Termin ist demnächst.|Aggettivi B1
        bisher|finora|Bisher gab es keine Probleme.|Aggettivi B1
        derzeit|attualmente|Derzeit ist die Station voll.|Aggettivi B1
        künftig|in futuro|Künftig machen wir das anders.|Aggettivi B1
        grundsätzlich|in linea di principio|Grundsätzlich ist das möglich.|Aggettivi B1
        wahrscheinlich|probabilmente|Wahrscheinlich kommt er später.|Aggettivi B1
        möglicherweise|possibilmente|Möglicherweise ist es zu spät.|Aggettivi B1
        notwendig|necessario|Das ist nicht notwendig.|Aggettivi B1
        sinnvoll|sensato|Die Maßnahme ist sinnvoll.|Aggettivi B1
        geeignet|adatto|Das Mittel ist geeignet.|Aggettivi B1
        betroffen|colpito|Die Region ist stark betroffen.|Aggettivi B1
        zufrieden|soddisfatto|Ich bin mit dem Ergebnis zufrieden.|Aggettivi B1
        enttäuscht|deluso|Er war sehr enttäuscht.|Aggettivi B1
        überrascht|sorpreso|Ich bin überrascht.|Aggettivi B1
        bereit|pronto|Ich bin bereit zu helfen.|Aggettivi B1
        in der Lage sein|essere in grado|Er ist nicht in der Lage zu arbeiten.|Aggettivi B1
        die Berufswahl|scelta professionale|Die Berufswahl ist wichtig.|Lavoro B1
        der Arbeitstag|giornata lavorativa|Der Arbeitstag beginnt früh.|Lavoro B1
        die Arbeitswoche|settimana lavorativa|Die Arbeitswoche hat fünf Tage.|Lavoro B1
        der Arbeitsweg|tragitto per il lavoro|Der Arbeitsweg ist kurz.|Lavoro B1
        die Teamarbeit|lavoro di squadra|Teamarbeit ist wichtig.|Lavoro B1
        die Führungskraft|dirigente|Die Führungskraft ist neu.|Lavoro B1
        der Vorgesetzte|superiore|Der Vorgesetzte ist zufrieden.|Lavoro B1
        die Anweisung|istruzione|Die Anweisung ist klar.|Lavoro B1
        die Fertigkeit|abilità pratica|Diese Fertigkeit ist gefragt.|Lavoro B1
        die Umschulung|riqualificazione|Die Umschulung dauert ein Jahr.|Lavoro B1
        der Workshop|workshop|Der Workshop ist praktisch.|Lavoro B1
        das Zertifikat|certificato|Das Zertifikat ist gültig.|Lavoro B1
        der Test|test|Der Test war leicht.|Lavoro B1
        das Arbeitszeugnis|referenza di lavoro|Das Arbeitszeugnis ist positiv.|Lavoro B1
        die Befristung|contratto a termine|Die Befristung endet im Mai.|Lavoro B1
        die Gehaltsverhandlung|negoziazione salariale|Die Gehaltsverhandlung war erfolgreich.|Lavoro B1
        die Lohnerhöhung|aumento di stipendio|Die Lohnerhöhung kommt im Juni.|Lavoro B1
        der Bonus|bonus|Der Bonus ist freiwillig.|Lavoro B1
        die Prämie|premio|Die Prämie wird ausgezahlt.|Lavoro B1
        der Urlaubsantrag|richiesta di ferie|Der Urlaubsantrag ist genehmigt.|Lavoro B1
        die Elternzeit|congedo parentale|Die Elternzeit dauert ein Jahr.|Lavoro B1
        der Minijob|mini lavoro|Der Minijob ist steuerfrei.|Lavoro B1
        die Selbstständigkeit|lavoro autonomo|Die Selbstständigkeit hat Vorteile.|Lavoro B1
        der Freiberufler|libero professionista|Der Freiberufler arbeitet zu Hause.|Lavoro B1
        der Arbeitsmarkt|mercato del lavoro|Der Arbeitsmarkt ist schwierig.|Lavoro B1
        der Personalchef|responsabile del personale|Der Personalchef führt das Gespräch.|Lavoro B1
        die Personalabteilung|ufficio del personale|Die Personalabteilung ist im ersten Stock.|Lavoro B1
        der Betriebsrat|consiglio di fabbrica|Der Betriebsrat vertritt die Mitarbeiter.|Lavoro B1
        die Sozialversicherung|assicurazione sociale|Die Sozialversicherung ist Pflicht.|Lavoro B1
        die Arbeitslosenversicherung|assicurazione disoccupazione|Die Arbeitslosenversicherung hilft.|Lavoro B1
        das Bruttogehalt|stipendio lordo|Das Bruttogehalt steht im Vertrag.|Lavoro B1
        das Nettogehalt|stipendio netto|Das Nettogehalt ist niedriger.|Lavoro B1
        der Abzug|deduzione|Der Abzug ist gesetzlich.|Lavoro B1
        die Sicherheitsvorschrift|norma di sicurezza|Die Sicherheitsvorschrift gilt für alle.|Lavoro B1
        die Schutzkleidung|indumenti protettivi|Die Schutzkleidung ist Pflicht.|Lavoro B1
        das Betriebsfest|festa aziendale|Das Betriebsfest ist im Dezember.|Lavoro B1
        die Versammlung|assemblea|Die Versammlung beginnt um zehn.|Lavoro B1
        die Tagesordnung|ordine del giorno|Die Tagesordnung hat fünf Punkte.|Lavoro B1
        der Beschluss|delibera|Der Beschluss ist gefallen.|Lavoro B1
        die Abstimmung|votazione|Die Abstimmung war knapp.|Lavoro B1
        der Konflikt|conflitto|Der Konflikt ist gelöst.|Lavoro B1
        die Vollmacht|procura|Die Vollmacht liegt vor.|Lavoro B1
        der Stempel|timbro|Der Stempel fehlt.|Lavoro B1
        die Kopie|copia|Die Kopie ist unscharf.|Lavoro B1
        die Ablage|archivio|Die Ablage ist geordnet.|Lavoro B1
        der Vorgang|pratica|Der Vorgang ist abgeschlossen.|Lavoro B1
        die Akte|fascicolo|Die Akte ist dick.|Lavoro B1
        das Dokument|documento|Das Dokument ist wichtig.|Lavoro B1
        die Meldung|segnalazione|Die Meldung kam spät.|Lavoro B1
        die Mitteilung|comunicazione|Die Mitteilung ist neu.|Lavoro B1
        die Ankündigung|annuncio|Die Ankündigung war überraschend.|Lavoro B1
        die Auskunft|informazione|Die Auskunft ist kostenlos.|Lavoro B1
        die Anfrage|richiesta|Die Anfrage ist eingegangen.|Lavoro B1
        die Beschwerde|reclamo|Die Beschwerde ist berechtigt.|Lavoro B1
        die Ablehnung|rifiuto|Die Ablehnung kam per Post.|Lavoro B1
        die Verschiebung|spostamento|Die Verschiebung ist nötig.|Lavoro B1
        die Telefonkonferenz|teleconferenza|Die Telefonkonferenz beginnt um neun.|Lavoro B1
        die Videokonferenz|videoconferenza|Die Videokonferenz war gut.|Lavoro B1
        die Währung|valuta|Die Währung ist stabil.|Denaro
        die Ausgabe|spesa|Die Ausgabe war geplant.|Denaro
        die Einnahme|entrata|Die Einnahme ist gestiegen.|Denaro
        das Einkommen|reddito|Das Einkommen ist regelmäßig.|Denaro
        das Vermögen|patrimonio|Das Vermögen ist klein.|Denaro
        der Kassenbon|scontrino|Der Kassenbon ist wichtig.|Denaro
        die Abbuchung|addebito|Die Abbuchung ist erfolgt.|Denaro
        der Dauerauftrag|ordine permanente|Der Dauerauftrag läuft monatlich.|Denaro
        das Sparkonto|conto di risparmio|Das Sparkonto ist rentabel.|Denaro
        die PIN|PIN|Die PIN ist geheim.|Denaro
        das Online-Banking|home banking|Online-Banking ist praktisch.|Denaro
        die Bescheinigung|certificato|Die Bescheinigung liegt bei.|Denaro
        die Urkunde|atto|Die Urkunde ist beglaubigt.|Burocrazia
        die Geburtsurkunde|atto di nascita|Die Geburtsurkunde fehlt.|Burocrazia
        die Heiratsurkunde|atto di matrimonio|Die Heiratsurkunde ist nötig.|Burocrazia
        die Abmeldung|cancellazione|Die Abmeldung ist kostenlos.|Burocrazia
        die Ummeldung|cambio di residenza|Die Ummeldung ist Pflicht.|Burocrazia
        der Beamte|funzionario|Der Beamte ist freundlich.|Burocrazia
        die Öffnungszeiten|orari di apertura|Die Öffnungszeiten stehen an der Tür.|Burocrazia
        die Wartezeit|tempo di attesa|Die Wartezeit ist lang.|Burocrazia
        die Warteschlange|coda|Die Warteschlange ist kurz.|Burocrazia
        der Zeuge|testimone|Der Zeuge sagt die Wahrheit.|Diritto
        die Anzeige|denuncia|Die Anzeige ist erstattet.|Diritto
        die Rettung|salvataggio|Die Rettung kam schnell.|Diritto
        die Politik|politica|Die Politik ist kompliziert.|Società B1
        der Wahlkampf|campagna elettorale|Der Wahlkampf ist kurz.|Società B1
        der Politiker|politico|Der Politiker spricht laut.|Società B1
        der Staat|stato|Der Staat zahlt die Schule.|Società B1
        die Nation|nazione|Die Nation ist stolz.|Società B1
        die Gemeinschaft|comunità|Die Gemeinschaft hilft sich.|Società B1
        das Mitglied|membro|Das Mitglied ist neu.|Società B1
        der Bürger|cittadino|Der Bürger hat Rechte.|Società B1
        die Bürgerin|cittadina|Die Bürgerin beschwert sich.|Società B1
        die Öffentlichkeit|pubblico|Die Öffentlichkeit ist informiert.|Società B1
        die Presse|stampa|Die Presse berichtet darüber.|Società B1
        der Benutzer|utente|Der Benutzer ist neu.|Società B1
        das Profil|profilo|Das Profil ist öffentlich.|Società B1
        der Beitrag|contributo|Der Beitrag ist hilfreich.|Società B1
        der Kommentar|commento|Der Kommentar war böse.|Società B1
        das Argument|argomento|Das Argument überzeugt mich.|Società B1
        das Lob|lode|Das Lob freut mich.|Società B1
        das Projekt|progetto|Das Projekt ist beendet.|Società B1
        der Misserfolg|insuccesso|Der Misserfolg war schmerzhaft.|Società B1
        die Chance|possibilità|Die Chance ist groß.|Società B1
        die Gelegenheit|occasione|Die Gelegenheit kommt nie wieder.|Società B1
        das Risiko|rischio|Das Risiko ist gering.|Società B1
        die Vorsicht|prudenza|Vorsicht ist besser als Nachsicht.|Società B1
        das Interesse|interesse|Das Interesse ist geweckt.|Società B1

        die Demokratie|democrazia|Die Demokratie lebt von Wahlen.|SocietÃ  B1
        die Diktatur|dittatura|Die Diktatur verbietet Kritik.|SocietÃ  B1
        die Freiheit|libertÃ|Die Freiheit ist wichtig.|SocietÃ  B1
        die Gleichheit|uguaglianza|Die Gleichheit ist ein Ziel.|SocietÃ  B1
        die KriminalitÃ¤t|criminalitÃ|Die KriminalitÃ¤t ist gesunken.|SocietÃ  B1
        das GefÃ¤ngnis|carcere|Das GefÃ¤ngnis ist voll.|SocietÃ  B1
        das Opfer|vittima|Das Opfer sagte aus.|SocietÃ  B1
        der TÃ¤ter|autore del reato|Der TÃ¤ter ist bekannt.|SocietÃ  B1
        die Armut|povertÃ|Die Armut wÃ¤chst.|SocietÃ  B1
        der Reichtum|ricchezza|Der Reichtum ist ungleich verteilt.|SocietÃ  B1
        die Bildung|istruzione|Bildung ist der SchlÃ¼ssel.|SocietÃ  B1
        die Sitte|usanza|Die Sitte ist alt.|SocietÃ  B1
        der Wert|valore|Der Wert ist schwer zu messen.|SocietÃ  B1
        die Norm|norma|Die Norm gilt fÃ¼r alle.|SocietÃ  B1
        die Moral|morale|Die Moral ist umstritten.|SocietÃ  B1
        die Dienstleistung|servizio|Die Dienstleistung ist gefragt.|Economia B1
        die Produktion|produzione|Die Produktion lÃ¤uft gut.|Economia B1
        der Konsum|consumo|Der Konsum steigt.|Economia B1
        der Verbraucher|consumatore|Der Verbraucher hat Rechte.|Economia B1
        der Anbieter|fornitore|Der Anbieter ist zuverlÃ¤ssig.|Economia B1
        die Schulden|debiti|Die Schulden sind hoch.|Economia B1
        der Kundendienst|servizio clienti|Der Kundendienst hilft.|Economia B1
        die Marke|marca|Die Marke ist bekannt.|Economia B1
        das Produkt|prodotto|Das Produkt ist neu.|Economia B1
        die Technologie|tecnologia|Die Technologie entwickelt sich schnell.|Tecnologia B1
        das GerÃ¤t|apparecchio|Das GerÃ¤t ist kaputt.|Tecnologia B1
        die Maschine|macchina|Die Maschine lÃ¤uft.|Tecnologia B1
        der Download|download|Der Download dauert lange.|Tecnologia B1
        der Upload|upload|Der Upload ist fertig.|Tecnologia B1
        der Speicher|memoria|Der Speicher ist voll.|Tecnologia B1
        das Interview|intervista|Das Interview war lang.|Tecnologia B1
        die Medien|media|Die Medien berichten darÃ¼ber.|Tecnologia B1
        die Kommunikation|comunicazione|Die Kommunikation ist wichtig.|Tecnologia B1
        die Kunst|arte|Die Kunst ist frei.|Cultura B1
        die Literatur|letteratura|Die Literatur ist reich.|Cultura B1
        das Gedicht|poesia|Das Gedicht ist kurz.|Cultura B1
        das MÃ¤rchen|fiaba|Das MÃ¤rchen ist bekannt.|Cultura B1
        die Sage|leggenda|Die Sage ist alt.|Cultura B1
        die Legende|leggenda|Die Legende ist berÃ¼hmt.|Cultura B1
        die Biografie|biografia|Die Biografie ist neu.|Cultura B1
        das Ballett|balletto|Das Ballett ist elegant.|Cultura B1
        das Orchester|orchestra|Das Orchester spielt gut.|Cultura B1
        die Band|band|Die Band ist laut.|Cultura B1
        der SÃ¤nger|cantante|Der SÃ¤nger ist bekannt.|Cultura B1
        der Schauspieler|attore|Der Schauspieler ist berÃ¼hmt.|Cultura B1
        der Regisseur|regista|Der Regisseur ist jung.|Cultura B1
        der Autor|autore|Der Autor lebt in Berlin.|Cultura B1
        das Kunstwerk|opera d'arte|Das Kunstwerk ist wertvoll.|Cultura B1
        das GemÃ¤lde|dipinto|Das GemÃ¤lde hÃ¤ngt im Museum.|Cultura B1
        die Fotografie|fotografia|Die Fotografie ist schwarz-weiÃŸ.|Cultura B1
        das Design|design|Das Design ist schlicht.|Cultura B1
        die Mode|moda|Die Mode Ã¤ndert sich.|Cultura B1
        die BÃ¼hne|palco|Die BÃ¼hne ist groÃŸ.|Cultura B1
        die Leinwand|schermo|Die Leinwand ist hell.|Cultura B1
        der Professor|professore|Der Professor erklÃ¤rt die Regel.|Istruzione B1
        der Student|studente|Der Student lernt viel.|Istruzione B1
        die Studentin|studentessa|Die Studentin ist fleiÃŸig.|Istruzione B1
        das Diplom|diploma|Das Diplom ist anerkannt.|Istruzione B1
        der Bachelor|laurea triennale|Der Bachelor ist geschafft.|Istruzione B1
        der Master|laurea magistrale|Der Master beginnt im Herbst.|Istruzione B1
        der Doktor|dottore|Der Doktor forscht.|Istruzione B1
        die Wissenschaft|scienza|Die Wissenschaft sucht Antworten.|Istruzione B1
        die Theorie|teoria|Die Theorie ist schwierig.|Istruzione B1
        das Wissen|conoscenza|Das Wissen wÃ¤chst.|Istruzione B1
        das Lernziel|obiettivo di apprendimento|Das Lernziel ist klar.|Istruzione B1
        die FÃ¤higkeit|capacitÃ|Die FÃ¤higkeit ist angeboren.|Istruzione B1
        die Klinik|clinica|Die Klinik ist modern.|Salute B1
        der Pfleger|infermiere|Der Pfleger hilft den Patienten.|Salute B1
        das Symptom|sintomo|Das Symptom ist eindeutig.|Salute B1
        die Sucht|dipendenza|Die Sucht ist eine Krankheit.|Salute B1
        das Rauchen|fumo|Das Rauchen schadet.|Salute B1
        der Alkohol|alcol|Alkohol ist gefÃ¤hrlich.|Salute B1
        die Droge|droga|Die Droge ist verboten.|Salute B1
        das Immunsystem|sistema immunitario|Das Immunsystem ist stark.|Salute B1
        die ErnÃ¤hrung|alimentazione|Die ErnÃ¤hrung ist gesund.|Salute B1
        der Naturschutz|tutela della natura|Der Naturschutz beginnt hier.|Ambiente B1
        die Sonnenenergie|energia solare|Die Sonnenenergie ist sauber.|Ambiente B1
        die Windenergie|energia eolica|Die Windenergie wÃ¤chst.|Ambiente B1
        der MÃ¼ll|spazzatura|Der MÃ¼ll ist voll.|Ambiente B1
        der Treibhauseffekt|effetto serra|Der Treibhauseffekt ist gefÃ¤hrlich.|Ambiente B1
        die ErwÃ¤rmung|riscaldamento|Die ErwÃ¤rmung schreitet voran.|Ambiente B1
        die DÃ¼rre|siccitÃ|Die DÃ¼rre dauert an.|Ambiente B1
        die Ãœberschwemmung|allagamento|Die Ãœberschwemmung war schlimm.|Ambiente B1
        die Katastrophe|catastrofe|Die Katastrophe traf die Region.|Ambiente B1
        die Pflanze|pianta|Die Pflanze braucht Wasser.|Ambiente B1
        der Lebensraum|habitat|Der Lebensraum ist bedroht.|Ambiente B1
        die VerÃ¤nderung|cambiamento|Die VerÃ¤nderung braucht Zeit.|Astratti B1
        der RÃ¼ckgang|calo|Der RÃ¼ckgang ist deutlich.|Astratti B1
        die Steigerung|incremento|Die Steigerung ist geplant.|Astratti B1
        die ErhÃ¶hung|aumento|Die ErhÃ¶hung ist beschlossen.|Astratti B1
        die Verringerung|riduzione|Die Verringerung ist nÃ¶tig.|Astratti B1
        die Erweiterung|ampliamento|Die Erweiterung ist fertig.|Astratti B1
        die EinschrÃ¤nkung|limitazione|Die EinschrÃ¤nkung gilt weiter.|Astratti B1
        die Begrenzung|limitazione|Die Begrenzung ist sinnvoll.|Astratti B1
        die Umstellung|cambiamento|Die Umstellung ist gelungen.|Astratti B1
        die Verwirklichung|realizzazione|Die Verwirklichung ist nah.|Astratti B1
        die Umsetzung|attuazione|Die Umsetzung beginnt morgen.|Astratti B1
        die DurchfÃ¼hrung|esecuzione|Die DurchfÃ¼hrung ist geplant.|Astratti B1
        die Vorbereitung|preparazione|Die Vorbereitung dauert lange.|Astratti B1
        die Planung|pianificazione|Die Planung ist wichtig.|Astratti B1
        die Organisation|organizzazione|Die Organisation ist gut.|Astratti B1
        die Leitung|direzione|Die Leitung liegt bei ihr.|Astratti B1
        die FÃ¼hrung|guida|Die FÃ¼hrung ist schwach.|Astratti B1
        die Beurteilung|giudizio|Die Beurteilung ist positiv.|Astratti B1
        die EinschÃ¤tzung|valutazione|Die EinschÃ¤tzung war richtig.|Astratti B1
        die SchÃ¤tzung|stima|Die SchÃ¤tzung ist ungefÃ¤hr.|Astratti B1
        die Berechnung|calcolo|Die Berechnung ist falsch.|Astratti B1
        die Messung|misurazione|Die Messung ist genau.|Astratti B1
        die Beobachtung|osservazione|Die Beobachtung ist wichtig.|Astratti B1
        der Anlass|occasione|Der Anlass ist traurig.|Astratti B1
        die Alternative|alternativa|Die Alternative ist teuer.|Astratti B1
        die Gemeinsamkeit|somiglianza|Die Gemeinsamkeit ist Ã¼berraschend.|Astratti B1
        die Ã„hnlichkeit|somiglianza|Die Ã„hnlichkeit ist groÃŸ.|Astratti B1
        die Ansicht|opinione|Die Ansicht ist verbreitet.|Astratti B1
        der Standpunkt|punto di vista|Der Standpunkt ist klar.|Astratti B1
        die Zustimmung|consenso|Die Zustimmung war groÃŸ.|Astratti B1
        die BegrÃ¼ndung|motivazione|Die BegrÃ¼ndung fehlt.|Astratti B1
        die Beschreibung|descrizione|Die Beschreibung ist genau.|Astratti B1
        die Darstellung|rappresentazione|Die Darstellung ist klar.|Astratti B1
        die ErklÃ¤rung|spiegazione|Die ErklÃ¤rung ist einfach.|Astratti B1
        die PrÃ¤sentation|presentazione|Die PrÃ¤sentation ist fertig.|Astratti B1
        die Rede|discorso|Die Rede war kurz.|Astratti B1

        der Bundestag|parlamento federale|Der Bundestag tagt in Berlin.|Politica B1
        das Bundesland|land federale|Das Bundesland hat eigene Regeln.|Politica B1
        das Ministerium|ministero|Das Ministerium ist zuständig.|Politica B1
        der Minister|ministro|Der Minister tritt zurück.|Politica B1
        der Kanzler|cancelliere|Der Kanzler spricht im Fernsehen.|Politica B1
        der Abgeordnete|deputato|Der Abgeordnete stimmt zu.|Politica B1
        die Verfassung|costituzione|Die Verfassung schützt die Rechte.|Politica B1
        das Grundgesetz|legge fondamentale|Das Grundgesetz gilt seit 1949.|Politica B1
        die Menschenrechte|diritti umani|Die Menschenrechte sind unteilbar.|Politica B1
        die Meinungsfreiheit|libertà di opinione|Die Meinungsfreiheit ist geschützt.|Politica B1
        die Pressefreiheit|libertà di stampa|Die Pressefreiheit ist wichtig.|Politica B1
        die Gleichberechtigung|parità di diritti|Die Gleichberechtigung ist Gesetz.|Politica B1
        die Demonstration|manifestazione|Die Demonstration war friedlich.|Politica B1
        der Protest|protesta|Der Protest wächst.|Politica B1
        der Arbeitnehmer|dipendente|Der Arbeitnehmer hat Rechte.|Politica B1
        der Sozialstaat|stato sociale|Der Sozialstaat hilft den Schwachen.|Politica B1
        die Sozialhilfe|assistenza sociale|Die Sozialhilfe reicht kaum.|Politica B1
        das Sozialsystem|sistema sociale|Das Sozialsystem ist belastet.|Politica B1
        der Rentner|pensionato|Der Rentner lebt ruhig.|Politica B1
        der Steuersatz|aliquota fiscale|Der Steuersatz ist gestiegen.|Finanze B1
        die Mehrwertsteuer|IVA|Die Mehrwertsteuer ist enthalten.|Finanze B1
        die Einkommenssteuer|imposta sul reddito|Die Einkommenssteuer wird abgezogen.|Finanze B1
        die Sozialabgabe|contributo sociale|Die Sozialabgabe ist Pflicht.|Finanze B1
        der Wohlstand|benessere|Der Wohlstand ist gewachsen.|Finanze B1
        die Armutsgrenze|soglia di povertà|Die Armutsgrenze ist definiert.|Finanze B1
        die Einkommensverteilung|distribuzione del reddito|Die Einkommensverteilung ist ungleich.|Finanze B1
        das Defizit|disavanzo|Das Defizit ist zu hoch.|Finanze B1
        der Überschuss|avanzo|Der Überschuss ist klein.|Finanze B1
        die Staatsverschuldung|debito pubblico|Die Staatsverschuldung wächst.|Finanze B1
        die Geldpolitik|politica monetaria|Die Geldpolitik ist restriktiv.|Finanze B1
        die Zentralbank|banca centrale|Die Zentralbank senkt die Zinsen.|Finanze B1
        der Wechselkurs|tasso di cambio|Der Wechselkurs schwankt.|Finanze B1
        der Anleger|investitore|Der Anleger ist vorsichtig.|Finanze B1
        die Anlage|investimento|Die Anlage ist sicher.|Finanze B1
        die Rendite|rendimento|Die Rendite ist niedrig.|Finanze B1
        die Haftpflicht|responsabilità civile|Die Haftpflicht ist Pflicht.|Finanze B1
        die Hausratversicherung|assicurazione casa|Die Hausratversicherung ist günstig.|Finanze B1
        die Lebensversicherung|assicurazione vita|Die Lebensversicherung läuft lange.|Finanze B1
        die Krankenversicherung|assicurazione sanitaria|Die Krankenversicherung zahlt.|Finanze B1
        der Mietvertrag|contratto di affitto|Der Mietvertrag ist unterschrieben.|Abitare B1
        die Mieterhöhung|aumento dell'affitto|Die Mieterhöhung ist zulässig.|Abitare B1
        die Kaution|deposito cauzionale|Die Kaution beträgt drei Monate.|Abitare B1
        die Hausordnung|regolamento condominiale|Die Hausordnung hängt im Flur.|Abitare B1
        die Wohngemeinschaft|coabitazione|Die Wohngemeinschaft ist günstig.|Abitare B1
        der Mitbewohner|coinquilino|Der Mitbewohner ist neu.|Abitare B1
        der Mieter|inquilino|Der Mieter zahlt pünktlich.|Abitare B1
        die Ablöse|indennizzo|Die Ablöse ist verhandelbar.|Abitare B1
        die Renovierung|ristrutturazione|Die Renovierung dauert lange.|Abitare B1
        der Neubau|nuova costruzione|Der Neubau ist modern.|Abitare B1
        der Altbau|edificio storico|Der Altbau hat hohe Decken.|Abitare B1
        die Wohnfläche|superficie abitabile|Die Wohnfläche beträgt 80 Quadratmeter.|Abitare B1
        die Besichtigung|visita|Die Besichtigung ist am Samstag.|Abitare B1
        der Grundriss|pianta|Der Grundriss ist praktisch.|Abitare B1
        die Umgebung|dintorni|Die Umgebung ist grün.|Abitare B1
        die Infrastruktur|infrastruttura|Die Infrastruktur ist gut.|Abitare B1
        die Warmmiete|affitto incluse spese|Die Warmmiete ist hoch.|Abitare B1
        die Kaltmiete|affitto base|Die Kaltmiete ist niedriger.|Abitare B1
        die Mülltrennung|raccolta differenziata|Die Mülltrennung ist Pflicht.|Abitare B1
        der Sperrmüll|rifiuti ingombranti|Der Sperrmüll kommt im Mai.|Abitare B1
        das Reisebüro|agenzia di viaggi|Das Reisebüro ist geschlossen.|Viaggi B1
        der Reiseführer|guida turistica|Der Reiseführer ist informativ.|Viaggi B1
        die Buchung|prenotazione|Die Buchung ist bestätigt.|Viaggi B1
        die Stornierung|cancellazione|Die Stornierung kostet Geld.|Viaggi B1
        die Umbuchung|modifica della prenotazione|Die Umbuchung ist möglich.|Viaggi B1
        der Flug|volo|Der Flug ist verspätet.|Viaggi B1
        der Abflug|partenza|Der Abflug ist um sechs.|Viaggi B1
        die Landung|atterraggio|Die Landung war weich.|Viaggi B1
        der Zwischenstopp|scalo|Der Zwischenstopp dauert zwei Stunden.|Viaggi B1
        das Handgepäck|bagaglio a mano|Das Handgepäck ist klein.|Viaggi B1
        die Bordkarte|carta d'imbarco|Die Bordkarte ist digital.|Viaggi B1
        die Reiseversicherung|assicurazione di viaggio|Die Reiseversicherung ist sinnvoll.|Viaggi B1
        das Visum|visto|Das Visum ist gültig.|Viaggi B1
        die Grenzkontrolle|controllo di frontiera|Die Grenzkontrolle war schnell.|Viaggi B1
        die Jugendherberge|ostello della gioventù|Die Jugendherberge ist günstig.|Viaggi B1
        das Hotel|albergo|Das Hotel liegt zentral.|Viaggi B1
        die Rezeption|reception|Die Rezeption ist rund um die Uhr besetzt.|Viaggi B1
        die Führung|visita guidata|Die Führung dauert eine Stunde.|Viaggi B1
        der Eintritt|ingresso|Der Eintritt ist frei.|Viaggi B1
        die Insel|isola|Die Insel ist klein.|Viaggi B1
        der Wasserfall|cascata|Der Wasserfall ist hoch.|Viaggi B1
        der Gipfel|vetta|Der Gipfel ist schneebedeckt.|Viaggi B1
        das Tal|valle|Das Tal ist grün.|Viaggi B1
        die Wüste|deserto|Die Wüste ist heiß.|Viaggi B1
        der Dschungel|giungla|Der Dschungel ist dicht.|Viaggi B1
        die Jahreszeit|stagione|Die Jahreszeit ist ideal.|Viaggi B1
        der Tourismus|turismo|Der Tourismus wächst.|Viaggi B1
        der Tourist|turista|Der Tourist fragt nach dem Weg.|Viaggi B1
        das Lebensmittel|l'alimento|Das Lebensmittel ist frisch.|Cibo B1
        die Verpackung|confezione|Die Verpackung ist aus Plastik.|Cibo B1
        das Haltbarkeitsdatum|data di scadenza|Das Haltbarkeitsdatum ist abgelaufen.|Cibo B1
        die Zubereitung|preparazione|Die Zubereitung dauert 20 Minuten.|Cibo B1
        die Mahlzeit|pasto|Die Mahlzeit war gut.|Cibo B1
        die Hauptspeise|piatto principale|Die Hauptspeise ist fleischig.|Cibo B1
        die Nachspeise|dessert|Die Nachspeise ist süß.|Cibo B1
        die Bedienung|servizio|Die Bedienung ist freundlich.|Cibo B1
        das Trinkgeld|mancia|Das Trinkgeld ist freiwillig.|Cibo B1
        der Vegetarier|vegetariano|Der Vegetarier isst kein Fleisch.|Cibo B1
        der Veganer|vegano|Der Veganer isst keine Milchprodukte.|Cibo B1
        die Unverträglichkeit|intolleranza|Die Unverträglichkeit betrifft Milch.|Cibo B1
        das Bio-Produkt|prodotto biologico|Das Bio-Produkt ist teurer.|Cibo B1
        der Marktstand|bancarella|Der Marktstand ist voll.|Cibo B1
        die Ernte|raccolto|Die Ernte war gut.|Cibo B1
        der Anbau|coltivazione|Der Anbau ist biologisch.|Cibo B1
        der Bauer|contadino|Der Bauer arbeitet früh.|Cibo B1
        die Landwirtschaft|agricoltura|Die Landwirtschaft braucht Wasser.|Cibo B1
        das Gewächs|pianta coltivata|Das Gewächs wächst schnell.|Cibo B1
        das Geschirr|stoviglie|Das Geschirr ist gespült.|Cibo B1
        der Abwasch|piatti da lavare|Der Abwasch ist groß.|Cibo B1
        das Besteck|posate|Das Besteck liegt im Schrank.|Cibo B1
        der Gefrierschrank|congelatore|Der Gefrierschrank ist voll.|Cibo B1
        die Verpflegung|vitto|Die Verpflegung ist inklusive.|Cibo B1
        die Wäsche|bucato|Die Wäsche ist fertig.|Casa B1
        das Bügeleisen|ferro da stiro|Das Bügeleisen ist heiß.|Casa B1
        der Staubsauger|aspirapolvere|Der Staubsauger ist laut.|Casa B1
        der Besen|scopa|Der Besen steht in der Ecke.|Casa B1
        das Putzmittel|detergente|Das Putzmittel riecht stark.|Casa B1
        der Eimer|secchio|Der Eimer ist voll.|Casa B1
        der Lappen|panno|Der Lappen ist schmutzig.|Casa B1
        die Schublade|cassetto|Die Schublade klemmt.|Casa B1
        der Dachboden|soffitta|Der Dachboden ist voll.|Casa B1
        der Rasen|prato|Der Rasen ist grün.|Casa B1
        das Werkzeug|attrezzo|Das Werkzeug ist neu.|Casa B1
        der Hammer|martello|Der Hammer liegt dort.|Casa B1
        der Schraubenzieher|cacciavite|Der Schraubenzieher fehlt.|Casa B1
        die Schraube|vite|Die Schraube ist locker.|Casa B1
        der Nagel|chiodo|Der Nagel ist krumm.|Casa B1
        die Leiter|scala|Die Leiter ist stabil.|Casa B1
        das Kabel|cavo|Das Kabel ist zu kurz.|Casa B1
        die Sicherung|fusibile|Die Sicherung ist raus.|Casa B1

        sich entwickeln|svilupparsi|Die Stadt entwickelt sich schnell.|Verbi B1
        steigern|aumentare|Wir müssen den Umsatz steigern.|Verbi B1
        senken|ridurre|Die Firma will die Kosten senken.|Verbi B1
        erhöhen|aumentare|Sie wollen die Preise erhöhen.|Verbi B1
        verringern|diminuire|Wir müssen den Verbrauch verringern.|Verbi B1
        erweitern|ampliare|Sie wollen das Angebot erweitern.|Verbi B1
        einschränken|limitare|Wir müssen die Ausgaben einschränken.|Verbi B1
        begrenzen|limitare|Die Stadt will den Verkehr begrenzen.|Verbi B1
        ausbauen|ampliare|Sie wollen die Schule ausbauen.|Verbi B1
        umbauen|ristrutturare|Wir wollen das Haus umbauen.|Verbi B1
        anpassen|adattare|Man muss sich an das Klima anpassen.|Verbi B1
        umwandeln|trasformare|Sie wollen das Büro umwandeln.|Verbi B1
        verwirklichen|realizzare|Er will seinen Traum verwirklichen.|Verbi B1
        führen|condurre|Er will das Team führen.|Verbi B1
        verwalten|amministrare|Sie verwalten das Gebäude.|Verbi B1
        kontrollieren|controllare|Wir müssen die Ergebnisse kontrollieren.|Verbi B1
        prüfen|verificare|Bitte prüfen Sie die Angaben.|Verbi B1
        einschätzen|valutare|Ich kann die Lage schwer einschätzen.|Verbi B1
        berechnen|calcolare|Wir müssen die Kosten berechnen.|Verbi B1
        messen|misurare|Der Arzt will den Blutdruck messen.|Verbi B1
        beobachten|osservare|Wir können die Vögel beobachten.|Verbi B1
        untersuchen|esaminare|Der Arzt will den Patienten untersuchen.|Verbi B1
        forschen|fare ricerca|Die Universität will weiter forschen.|Verbi B1
        analysieren|analizzare|Wir müssen die Daten analysieren.|Verbi B1
        auswerten|valutare|Sie wollen die Umfrage auswerten.|Verbi B1
        vergleichen|confrontare|Man kann die Preise vergleichen.|Verbi B1
        unterscheiden|distinguere|Man muss die Fälle unterscheiden.|Verbi B1
        mitteilen|comunicare|Bitte teilen Sie uns das Ergebnis mit.|Verbi B1
        ankündigen|annunciare|Sie wollen die Änderung ankündigen.|Verbi B1
        debattieren|dibattere|Die Schüler wollen debattieren.|Verbi B1
        kritisieren|criticare|Man darf die Regierung kritisieren.|Verbi B1
        loben|elogiare|Der Chef will die Mitarbeiter loben.|Verbi B1
        zweifeln|dubitare|Ich beginne an der Lösung zu zweifeln.|Verbi B1
        wählen|scegliere|Sie können frei wählen.|Verbi B1
        auffordern|invitare|Die Polizei will die Leute auffordern.|Verbi B1
        bitten|chiedere|Ich möchte um Hilfe bitten.|Verbi B1
        begrüßen|salutare|Wir wollen die Gäste begrüßen.|Verbi B1
        verabschieden|congedare|Wir müssen uns verabschieden.|Verbi B1
        betreuen|seguire|Sie will die Kinder betreuen.|Verbi B1
        unterstützen|sostenere|Wir wollen das Projekt unterstützen.|Verbi B1
        retten|salvare|Die Feuerwehr will die Menschen retten.|Verbi B1
        schützen|proteggere|Wir müssen die Umwelt schützen.|Verbi B1
        sich ereignen|accadere|Solche Unfälle ereignen sich oft.|Verbi B1
        entstehen|sorgere|Es können hohe Kosten entstehen.|Verbi B1
        verschwinden|scomparire|Die Schmerzen wollen nicht verschwinden.|Verbi B1
        wachsen|crescere|Die Stadt will weiter wachsen.|Verbi B1
        schrumpfen|restringersi|Die Bevölkerung könnte schrumpfen.|Verbi B1
        zerstören|distruggere|Der Sturm kann Häuser zerstören.|Verbi B1
        produzieren|produrre|Die Firma will mehr produzieren.|Verbi B1
        herstellen|fabbricare|Sie wollen Papier herstellen.|Verbi B1
        investieren|investire|Wir wollen in Bildung investieren.|Verbi B1
        borgen|prendere in prestito|Ich möchte Geld borgen.|Verbi B1
        schulden|dovere|Ich schulde dir noch zehn Euro.|Verbi B1
        besitzen|possedere|Sie besitzen ein Haus am See.|Verbi B1
        entlassen|licenziare|Die Firma will Mitarbeiter entlassen.|Verbi B1
        ausbilden|formare|Die Firma will Lehrlinge ausbilden.|Verbi B1
        unterrichten|insegnare|Sie will Deutsch unterrichten.|Verbi B1
        lehren|insegnare|Die Erfahrung kann viel lehren.|Verbi B1
        üben|esercitarsi|Man muss jeden Tag üben.|Verbi B1
        bestehen|superare|Sie will die Prüfung bestehen.|Verbi B1
        durchfallen|essere bocciato|Er könnte durch die Prüfung durchfallen.|Verbi B1
        beantragen|richiedere|Sie wollen die Genehmigung beantragen.|Verbi B1
        genehmigen|approvare|Der Chef will den Urlaub genehmigen.|Verbi B1
        abstimmen|votare|Die Mitglieder wollen abstimmen.|Verbi B1
        demonstrieren|dimostrare|Die Studenten wollen demonstrieren.|Verbi B1
        protestieren|protestare|Die Bürger wollen protestieren.|Verbi B1
        sich engagieren|impegnarsi|Sie will sich für die Umwelt engagieren.|Verbi B1
        spenden|donare|Die Firma will Geld spenden.|Verbi B1
        pflegen|accudire|Sie will ihre Mutter pflegen.|Verbi B1
        behandeln|trattare|Der Arzt will den Patienten behandeln.|Verbi B1
        impfen|vaccinare|Der Arzt will die Kinder impfen.|Verbi B1
        sich entspannen|rilassarsi|Am Wochenende will ich mich entspannen.|Verbi B1
        einschlafen|addormentarsi|Ich kann abends schwer einschlafen.|Verbi B1
        träumen|sognare|Ich träume von einer Reise.|Verbi B1
        leiden|soffrire|Viele Menschen leiden unter Stress.|Verbi B1
        sich wundern|meravigliarsi|Ich muss mich über ihn wundern.|Verbi B1
        sich verlieben|innamorarsi|Die beiden wollen sich verlieben.|Verbi B1
        heiraten|sposarsi|Sie wollen im Sommer heiraten.|Verbi B1
        sich trennen|separarsi|Sie wollen sich trennen.|Verbi B1
        sterben|morire|Alle Menschen müssen einmal sterben.|Verbi B1
        einziehen|trasferirsi|Wir wollen im Mai einziehen.|Verbi B1
        bügeln|stirare|Ich muss noch die Hemden bügeln.|Verbi B1
        braten|arrostire|Wir wollen das Fleisch braten.|Verbi B1
        schneiden|tagliare|Bitte schneiden Sie das Brot.|Verbi B1
        mischen|mescolare|Sie müssen die Zutaten mischen.|Verbi B1
        servieren|servire|Wir wollen das Essen servieren.|Verbi B1
        einnehmen|assumere|Sie müssen die Tabletten einnehmen.|Verbi B1
        unzufrieden|insoddisfatto|Der Kunde ist unzufrieden.|Aggettivi B1
        erfolgreich|di successo|Das Projekt ist erfolgreich.|Aggettivi B1
        erfolglos|senza successo|Der Versuch war erfolglos.|Aggettivi B1
        sinnlos|senza senso|Die Diskussion ist sinnlos.|Aggettivi B1
        nutzlos|inutile|Das Gerät ist nutzlos.|Aggettivi B1
        nützlich|utile|Der Tipp ist nützlich.|Aggettivi B1
        wertvoll|prezioso|Die Erfahrung ist wertvoll.|Aggettivi B1
        wertlos|senza valore|Die Ware ist wertlos.|Aggettivi B1
        riskant|rischioso|Die Aktion ist riskant.|Aggettivi B1
        harmlos|innocuo|Das Medikament ist harmlos.|Aggettivi B1
        schädlich|dannoso|Rauchen ist schädlich.|Aggettivi B1
        ungesund|malsano|Diese Ernährung ist ungesund.|Aggettivi B1
        lecker|delizioso|Das Essen ist lecker.|Aggettivi B1
        köstlich|squisito|Der Kuchen ist köstlich.|Aggettivi B1
        bitter|amaro|Der Kaffee ist bitter.|Aggettivi B1
        fett|grasso|Das Essen ist zu fett.|Aggettivi B1
        mager|magro|Das Fleisch ist mager.|Aggettivi B1
        roh|crodo|Das Fleisch ist roh.|Aggettivi B1
        gekocht|cotto|Das Gemüse ist gekocht.|Aggettivi B1
        gebraten|arrosto|Das Hähnchen ist gebraten.|Aggettivi B1
        mild|mite|Der Käse ist mild.|Aggettivi B1
        würzig|speziato|Die Soße ist würzig.|Aggettivi B1
        verdorben|avariato|Die Milch ist verdorben.|Aggettivi B1
        haltbar|conservabile|Das Produkt ist lange haltbar.|Aggettivi B1
        biologisch|biologico|Das Obst ist biologisch.|Aggettivi B1
        regional|regionale|Das Gemüse ist regional.|Aggettivi B1
        saisonal|stagionale|Das Obst ist saisonal.|Aggettivi B1
        international|internazionale|Die Messe ist international.|Aggettivi B1
        national|nazionale|Die Mannschaft ist national.|Aggettivi B1
        lokal|locale|Der Markt ist lokal.|Aggettivi B1
        global|globale|Das Problem ist global.|Aggettivi B1
        persönlich|personale|Die Meinung ist persönlich.|Aggettivi B1
        privat|privato|Das Gespräch ist privat.|Aggettivi B1
        öffentlich|pubblico|Der Platz ist öffentlich.|Aggettivi B1
        geheim|segreto|Das Passwort ist geheim.|Aggettivi B1
        bekannt|noto|Der Schauspieler ist bekannt.|Aggettivi B1
        unbekannt|sconosciuto|Der Täter ist unbekannt.|Aggettivi B1
        berühmt|famoso|Die Stadt ist berühmt.|Aggettivi B1
        unbedeutend|irrilevante|Der Unterschied ist unbedeutend.|Aggettivi B1
        unzuverlässig|inaffidabile|Der Lieferant ist unzuverlässig.|Aggettivi B1
        unverantwortlich|irresponsabile|Das Verhalten ist unverantwortlich.|Aggettivi B1
        selbstständig|autonomo|Sie arbeitet selbstständig.|Aggettivi B1
        unabhängig|indipendente|Die Presse ist unabhängig.|Aggettivi B1
        abhängig|dipendente|Er ist von seinem Auto abhängig.|Aggettivi B1
        gültig|valido|Der Pass ist noch gültig.|Aggettivi B1
        ungültig|non valido|Das Ticket ist ungültig.|Aggettivi B1
        zuständig|competente|Die Abteilung ist zuständig.|Aggettivi B1
        regelmäßig|regolare|Der Unterricht ist regelmäßig.|Aggettivi B1
        unregelmäßig|irregolare|Der Bus fährt unregelmäßig.|Aggettivi B1
        häufig|frequente|Der Fehler ist häufig.|Aggettivi B1
        selten|raro|Der Fall ist selten.|Aggettivi B1
        gewöhnlich|abituale|Das ist ein gewöhnlicher Tag.|Aggettivi B1
        außergewöhnlich|eccezionale|Das Ereignis ist außergewöhnlich.|Aggettivi B1
        vollständig|completo|Die Liste ist vollständig.|Aggettivi B1
        folgend|seguente|Die folgende Frage ist leicht.|Aggettivi B1
        bisherig|precedente|Die bisherigen Ergebnisse sind gut.|Aggettivi B1
        zukünftig|futuro|Die zukünftige Regelung ist klar.|Aggettivi B1
        derzeitig|attuale|Die derzeitige Lage ist ruhig.|Aggettivi B1
        aktuell|attuale|Die Nachricht ist aktuell.|Aggettivi B1
        modern|moderno|Das Gebäude ist modern.|Aggettivi B1
        traditionell|tradizionale|Das Fest ist traditionell.|Aggettivi B1
        praktisch|pratico|Die Lösung ist praktisch.|Aggettivi B1
        theoretisch|teorico|Das ist nur theoretisch.|Aggettivi B1
        technisch|tecnico|Das Problem ist technisch.|Aggettivi B1
        wissenschaftlich|scientifico|Die Studie ist wissenschaftlich.|Aggettivi B1
        kulturell|kulturale|Das Angebot ist kulturell.|Aggettivi B1
        sozial|sociale|Das Problem ist sozial.|Aggettivi B1
        politisch|politico|Die Entscheidung ist politisch.|Aggettivi B1
        wirtschaftlich|economico|Das Land ist wirtschaftlich stark.|Aggettivi B1
        gesetzlich|legale|Die Frist ist gesetzlich.|Aggettivi B1
        illegal|illegale|Der Handel ist illegal.|Aggettivi B1
        legal|legale|Der Vertrag ist legal.|Aggettivi B1
        gerecht|giusto|Die Strafe ist gerecht.|Aggettivi B1
        ungerecht|ingiusto|Das Urteil ist ungerecht.|Aggettivi B1
        fair|corretto|Der Wettbewerb ist fair.|Aggettivi B1
        unfair|scorretto|Das war unfair.|Aggettivi B1
        unehrlich|disonesto|Die Antwort war unehrlich.|Aggettivi B1
        kompliziert|complicato|Die Regel ist kompliziert.|Aggettivi B1
        komplex|complesso|Das System ist komplex.|Aggettivi B1
        niedrig|basso|Der Preis ist niedrig.|Aggettivi B1
        flach|piatto|Das Land ist flach.|Aggettivi B1
        undeutlich|poco chiaro|Die Schrift ist undeutlich.|Aggettivi B1
        unklar|poco chiaro|Die Lage ist unklar.|Aggettivi B1
        genau|preciso|Die Zahl ist genau.|Aggettivi B1
        ungenau|impreciso|Die Angabe ist ungenau.|Aggettivi B1
        überflüssig|superfluo|Der Kommentar ist überflüssig.|Aggettivi B1
        unwichtig|poco importante|Das Detail ist unwichtig.|Aggettivi B1
        bedeutend|importante|Der Fund ist bedeutend.|Aggettivi B1
        normalerweise|normalmente|Normalerweise fahre ich mit dem Bus.|Avverbi B1
        meistens|per lo più|Meistens bin ich pünktlich.|Avverbi B1
        wiederholt|ripetutamente|Er hat wiederholt gefehlt.|Avverbi B1
        währenddessen|nel frattempo|Währenddessen kochte sie das Essen.|Avverbi B1
        inzwischen|nel frattempo|Inzwischen ist alles fertig.|Avverbi B1
        mittlerweile|ormai|Mittlerweile wohne ich hier.|Avverbi B1
        rechtzeitig|in tempo|Wir sind rechtzeitig angekommen.|Avverbi B1
        absichtlich|di proposito|Er hat es absichtlich gemacht.|Avverbi B1
        versehentlich|per sbaglio|Ich habe es versehentlich gelöscht.|Avverbi B1
        sicherlich|sicuramente|Das ist sicherlich richtig.|Avverbi B1
        außerdem|inoltre|Außerdem brauche ich Milch.|Avverbi B1
        insgesamt|in totale|Insgesamt sind es zehn Personen.|Avverbi B1
        zusammenfassend|riassumendo|Zusammenfassend ist die Lage gut.|Avverbi B1
        somit|pertanto|Somit ist die Frage geklärt.|Avverbi B1
        daher|perciò|Daher bleibe ich zu Hause.|Avverbi B1
        deswegen|per questo|Deswegen bin ich gekommen.|Avverbi B1
        die Rolle|ruolo|Die Rolle ist wichtig.|Astratti B1
        die Zuverlässigkeit|affidabilità|Die Zuverlässigkeit ist wichtig.|Astratti B1
        die Genauigkeit|precisione|Die Genauigkeit ist entscheidend.|Astratti B1
        die Würde|dignità|Die Würde des Menschen ist unantastbar.|Astratti B1
        die Toleranz|tolleranza|Die Toleranz ist wichtig.|Astratti B1
        die Solidarität|solidarietà|Die Solidarität ist groß.|Astratti B1
        die Information|informazione|Die Information ist korrekt.|Astratti B1
        die Schwäche|debolezza|Die Schwäche ist bekannt.|Astratti B1
        die Stärke|forza|Die Stärke liegt im Team.|Astratti B1
        die Wichtigkeit|importanza|Die Wichtigkeit ist offensichtlich.|Astratti B1

        der Igel|riccio|Der Igel ist im Garten.|Animali B1
        das Eichhörnchen|scoiattolo|Das Eichhörnchen klettert schnell.|Animali B1
        der Dachs|tasso|Der Dachs lebt im Wald.|Animali B1
        der Specht|picchio|Der Specht klopft am Baum.|Animali B1
        die Eule|civetta|Die Eule ist nachtaktiv.|Animali B1
        der Adler|aquila|Der Adler fliegt hoch.|Animali B1
        der Falke|falco|Der Falke ist schnell.|Animali B1
        die Möwe|gabbiano|Die Möwe fliegt über das Meer.|Animali B1
        die Robbe|foca|Die Robbe liegt am Strand.|Animali B1
        der Wal|balena|Der Wal ist riesig.|Animali B1
        der Delfin|delfino|Der Delfin springt aus dem Wasser.|Animali B1
        der Hai|squalo|Der Hai ist gefährlich.|Animali B1
        die Qualle|medusa|Die Qualle brennt.|Animali B1
        der Krake|polpo|Der Krake hat acht Arme.|Animali B1
        die Krabbe|granchio|Die Krabbe läuft seitwärts.|Animali B1
        die Muschel|conchiglia|Die Muschel ist leer.|Animali B1
        die Schnecke|lumaca|Die Schnecke ist langsam.|Animali B1
        der Wurm|verme|Der Wurm lebt in der Erde.|Animali B1
        die Raupe|bruco|Die Raupe frisst Blätter.|Animali B1
        die Libelle|libellula|Die Libelle fliegt am Teich.|Animali B1
        die Heuschrecke|cavalletta|Die Heuschrecke springt weit.|Animali B1
        der Käfer|scarafaggio|Der Käfer ist schwarz.|Animali B1
        die Wespe|vespa|Die Wespe sticht.|Animali B1
        die Mücke|zanzara|Die Mücke ist lästig.|Animali B1
        die Eiche|quercia|Die Eiche ist alt.|Natura B1
        die Buche|faggio|Die Buche ist ein Laubbaum.|Natura B1
        die Tanne|abete|Die Tanne ist immergrün.|Natura B1
        die Fichte|peccio|Die Fichte ist hoch.|Natura B1
        die Kiefer|pino|Die Kiefer wächst hier.|Natura B1
        die Linde|tiglio|Die Linde riecht gut.|Natura B1
        die Birke|betulla|Die Birke ist weiß.|Natura B1
        die Weide|salice|Die Weide steht am Wasser.|Natura B1
        der Ahorn|acero|Der Ahorn ist rot im Herbst.|Natura B1
        der Busch|cespuglio|Der Busch ist dicht.|Natura B1
        der Strauch|arbusto|Der Strauch trägt Beeren.|Natura B1
        die Hecke|siepe|Die Hecke ist geschnitten.|Natura B1
        das Kraut|erba aromatica|Das Kraut wächst im Garten.|Natura B1
        das Unkraut|erbaccia|Das Unkraut wächst schnell.|Natura B1
        die Wiese|prato|Die Wiese ist grün.|Natura B1
        das Feld|campo|Das Feld ist bestellt.|Natura B1
        der Acker|campo coltivato|Der Acker ist trocken.|Natura B1
        der Bauernhof|fattoria|Der Bauernhof ist groß.|Natura B1
        der Stall|stalla|Der Stall ist warm.|Natura B1
        die Scheune|fienile|Die Scheune ist voll.|Natura B1
        das Vieh|bestiame|Das Vieh ist auf der Weide.|Natura B1
        die Herde|gregge|Die Herde zieht weiter.|Natura B1
        der Jäger|cacciatore|Der Jäger geht früh los.|Natura B1
        der Förster|guardia forestale|Der Förster pflegt den Wald.|Natura B1
        das Naturschutzgebiet|riserva naturale|Das Naturschutzgebiet ist geschützt.|Natura B1
        der Bach|ruscello|Der Bach ist seicht.|Natura B1
        der Teich|stagno|Der Teich ist voller Fische.|Natura B1
        das Ufer|riva|Das Ufer ist steinig.|Natura B1
        die Schlucht|gola|Die Schlucht ist tief.|Natura B1
        die Höhle|grotta|Die Höhle ist dunkel.|Natura B1
        der Frost|gelo|Der Frost kommt nachts.|Natura B1
        das Glatteis|ghiaccio|Das Glatteis ist gefährlich.|Meteo B1
        der Hagel|grandine|Der Hagel hat die Ernte zerstört.|Meteo B1
        der Schneefall|nevicata|Der Schneefall hört nicht auf.|Meteo B1
        die Schneeflocke|fiocco di neve|Die Schneeflocke ist zart.|Meteo B1
        der Regenschirm|ombrello|Der Regenschirm ist kaputt.|Meteo B1
        die Regenjacke|giacca antipioggia|Die Regenjacke ist wasserdicht.|Meteo B1
        der Orkan|uragano|Der Orkan richtet Schäden an.|Meteo B1
        das Unwetter|temporale violento|Das Unwetter kommt heute Nacht.|Meteo B1
        die Wettervorhersage|previsioni del tempo|Die Wettervorhersage ist gut.|Meteo B1
        der Wetterbericht|bollettino meteo|Der Wetterbericht kommt um acht.|Meteo B1
        das Thermometer|termometro|Das Thermometer zeigt zehn Grad.|Meteo B1
        der Hausmeister|portiere|Der Hausmeister hat den Schlüssel.|Casa B1
        die Hausverwaltung|amministrazione condominiale|Die Hausverwaltung ist zuständig.|Casa B1
        die Etage|piano|Die Wohnung liegt in der dritten Etage.|Casa B1
        der Stock|piano|Wir wohnen im ersten Stock.|Casa B1
        das Erdgeschoss|piano terra|Das Erdgeschoss ist barrierefrei.|Casa B1
        das Obergeschoss|piano superiore|Der Obergeschoss ist leer.|Casa B1
        der Briefkasten|cassetta delle lettere|Der Briefkasten ist voll.|Casa B1
        die Gegensprechanlage|citofono|Die Gegensprechanlage funktioniert nicht.|Casa B1
        der Rauchmelder|rilevatore di fumo|Der Rauchmelder piept.|Casa B1
        die Alarmanlage|antifurto|Die Alarmanlage ist an.|Casa B1
        der Zaun|recinto|Der Zaun ist neu.|Casa B1
        das Treppenhaus|vano scale|Das Treppenhaus ist hell.|Casa B1
        die Mülltonne|bidone della spazzatura|Die Mülltonne ist voll.|Casa B1
        der Müllcontainer|container dei rifiuti|Der Müllcontainer steht hinten.|Casa B1
        die Innenstadt|centro storico|Die Innenstadt ist belebt.|Città B1
        die Altstadt|centro storico|Die Altstadt ist schön.|Città B1
        die Neustadt|quartiere nuovo|Die Neustadt ist modern.|Città B1
        der Stadtplan|mappa della città|Der Stadtplan ist kostenlos.|Città B1
        der Stadtpark|parco cittadino|Der Stadtpark ist groß.|Città B1
        der Spielplatz|parco giochi|Der Spielplatz ist voll.|Città B1
        die Grünanlage|area verde|Die Grünanlage ist gepflegt.|Città B1
        der Brunnen|fontana|Der Brunnen ist alt.|Città B1
        das Denkmal|monumento|Das Denkmal steht am Platz.|Città B1
        die Statue|statua|Die Statue ist aus Bronze.|Città B1
        die Galerie|galleria|Die Galerie ist klein.|Città B1
        das Kaufhaus|grande magazzino|Das Kaufhaus ist geschlossen.|Città B1
        das Einkaufszentrum|centro commerciale|Das Einkaufszentrum ist neu.|Città B1
        der Flohmarkt|mercato delle pulci|Der Flohmarkt ist am Sonntag.|Città B1
        die Messe|fiera|Die Messe ist international.|Città B1
        der Stand|stand|Der Stand ist leer.|Città B1
        der Kunde|cliente|Der Kunde hat immer recht.|Città B1
        die Kundin|cliente (f)|Die Kundin ist zufrieden.|Città B1
        der Bote|corriere|Der Bote klingelt.|Servizi B1
        der Kurier|corriere espresso|Der Kurier ist schnell.|Servizi B1
        die Rücksendung|reso|Die Rücksendung ist kostenlos.|Servizi B1
        die Mahnung|sollecito|Die Mahnung kam heute.|Servizi B1
        die Hotline|hotline|Die Hotline ist rund um die Uhr offen.|Servizi B1
        die Wartung|manutenzione|Die Wartung ist geplant.|Servizi B1
        die Bewerbungsunterlagen|documenti di candidatura|Die Bewerbungsunterlagen sind komplett.|Lavoro B1
        das Bewerbungsschreiben|lettera di candidatura|Das Bewerbungsschreiben ist kurz.|Lavoro B1
        der Berufswunsch|desiderio professionale|Der Berufswunsch ist Arzt.|Lavoro B1
        der Aufstieg|avanzamento|Der Aufstieg ist verdient.|Lavoro B1
        die Gleitzeit|orario flessibile|Die Gleitzeit ist beliebt.|Lavoro B1
        die Stellenbeschreibung|descrizione del posto|Die Stellenbeschreibung ist klar.|Lavoro B1
        das Einstellungsgespräch|colloquio di assunzione|Das Einstellungsgespräch war gut.|Lavoro B1
        die Einarbeitung|inserimento|Die Einarbeitung dauert zwei Wochen.|Lavoro B1
        die Einweisung|istruzione|Die Einweisung ist Pflicht.|Lavoro B1
        die Absprache|accordo|Die Absprache gilt.|Lavoro B1
        die Rücksprache|consultazione|Ich halte Rücksprache mit dem Chef.|Lavoro B1
        die Anwesenheit|presenza|Die Anwesenheit wird geprüft.|Lavoro B1
        die Fehlzeit|assenza|Die Fehlzeit ist kurz.|Lavoro B1
        die Überstunden|straordinari|Die Überstunden werden bezahlt.|Lavoro B1
        die Betriebsferien|chiusura aziendale|Die Betriebsferien sind im August.|Lavoro B1
        der Betriebsunfall|infortunio sul lavoro|Der Betriebsunfall ist gemeldet.|Lavoro B1
        die Berufsunfähigkeit|inabilità al lavoro|Die Berufsunfähigkeit ist versichert.|Lavoro B1
        der Ruhestand|pensione|Der Ruhestand beginnt mit 65.|Lavoro B1
        die Abfindung|indennità|Die Abfindung ist verhandelbar.|Lavoro B1
        die Zeitarbeit|lavoro interinale|Die Zeitarbeit ist befristet.|Lavoro B1
        der Zeitarbeiter|lavoratore interinale|Der Zeitarbeiter ist neu.|Lavoro B1
        die Leihfirma|agenzia interinale|Die Leihfirma vermittelt Jobs.|Lavoro B1
        der Personaldienstleister|fornitore di personale|Der Personaldienstleister ist groß.|Lavoro B1
        der Fachmann|esperto|Der Fachmann repariert das Gerät.|Lavoro B1
        der Ausbilder|formatore|Der Ausbilder ist geduldig.|Lavoro B1
        die Berufsschule|scuola professionale|Die Berufsschule ist zweimal pro Woche.|Lavoro B1
        das Berufskolleg|collegio professionale|Das Berufskolleg ist modern.|Lavoro B1
        der Schulabschluss|diploma scolastico|Der Schulabschluss ist wichtig.|Istruzione B1
        der Hochschulabschluss|laurea|Der Hochschulabschluss ist anerkannt.|Istruzione B1
        die Einschreibung|iscrizione|Die Einschreibung ist online.|Istruzione B1
        die Studiengebühr|tassa universitaria|Die Studiengebühr ist niedrig.|Istruzione B1
        das Stipendium|borsa di studio|Das Stipendium ist begehrt.|Istruzione B1
        der Stundenplan|orario scolastico|Der Stundenplan ist voll.|Istruzione B1
        das Fach|materia|Das Fach ist schwierig.|Istruzione B1
        die Prüfungsangst|ansia da esame|Die Prüfungsangst ist verbreitet.|Istruzione B1
        der Prüfer|esaminatore|Der Prüfer ist freundlich.|Istruzione B1
        der Aufsatz|tema scritto|Der Aufsatz ist gut gelungen.|Istruzione B1
        die Klassenarbeit|verifica in classe|Die Klassenarbeit ist morgen.|Istruzione B1
        das Diktat|dettato|Das Diktat war leicht.|Istruzione B1
        die Gruppenarbeit|lavoro di gruppo|Die Gruppenarbeit macht Spaß.|Istruzione B1
        das Referat|relazione|Das Referat ist nächste Woche.|Istruzione B1
        der Lesesaal|sala lettura|Der Lesesaal ist ruhig.|Istruzione B1
        das Lehrbuch|libro di testo|Das Lehrbuch ist teuer.|Istruzione B1
        das Arbeitsblatt|foglio di lavoro|Das Arbeitsblatt ist kopiert.|Istruzione B1
        die Mappe|carpetta|Die Mappe ist bunt.|Istruzione B1
        der Füller|penna stilografica|Der Füller schreibt gut.|Istruzione B1
        der Radiergummi|gomma|Der Radiergummi ist klein.|Istruzione B1
        das Lineal|righello|Das Lineal ist aus Plastik.|Istruzione B1
        die Schere|forbici|Die Schere ist scharf.|Istruzione B1
        der Kleber|colla|Der Kleber ist trocken.|Istruzione B1
        das Etui|astuccio|Das Etui ist voll.|Istruzione B1
        der Ranzen|zaino scolastico|Der Ranzen ist schwer.|Istruzione B1
        die Tafel|lavagna|Die Tafel ist voll.|Istruzione B1
        der Beamer|proiettore|Der Beamer funktioniert nicht.|Istruzione B1
        das Tablet|tablet|Das Tablet ist klein.|Istruzione B1
        das Smartphone|smartphone|Das Smartphone ist modern.|Istruzione B1
        die Plattform|piattaforma|Die Plattform ist online.|Istruzione B1
        die Cloud|cloud|Die Cloud speichert die Daten.|Istruzione B1
        der Roboter|robot|Der Roboter hilft im Haushalt.|Istruzione B1
        die künstliche Intelligenz|intelligenza artificiale|Die künstliche Intelligenz entwickelt sich.|Istruzione B1
        das soziale Netzwerk|rete sociale|Das soziale Netzwerk ist beliebt.|Istruzione B1
        der Virus|virus|Der Virus verbreitet sich.|Istruzione B1
        das Update|aggiornamento|Das Update ist verfügbar.|Istruzione B1
        die Funktion|funzione|Die Funktion ist neu.|Istruzione B1
        die Einstellung|impostazione|Die Einstellung ist falsch.|Istruzione B1
        die Benachrichtigung|notifica|Die Benachrichtigung ist an.|Istruzione B1
        der Scanner|scanner|Der Scanner ist schnell.|Istruzione B1
        der Lautsprecher|altoparlante|Der Lautsprecher ist laut.|Istruzione B1
        der Kopfhörer|cuffie|Der Kopfhörer ist kaputt.|Istruzione B1
        das Mikrofon|microfono|Das Mikrofon ist an.|Istruzione B1
        die Webcam|webcam|Die Webcam ist aus.|Istruzione B1
        der Router|router|Der Router ist neu.|Istruzione B1
        das WLAN|wifi|Das WLAN ist langsam.|Istruzione B1

        auf jeden Fall|in ogni caso|Auf jeden Fall komme ich morgen.|Espressioni B1
        auf keinen Fall|in nessun caso|Auf keinen Fall sage ich das.|Espressioni B1
        in der Regel|di regola|In der Regel beginne ich um acht.|Espressioni B1
        im Allgemeinen|in generale|Im Allgemeinen ist das richtig.|Espressioni B1
        im Gegenteil|al contrario|Im Gegenteil, es ist einfach.|Espressioni B1
        im Vergleich|in confronto|Im Vergleich ist das billiger.|Espressioni B1
        aus diesem Grund|per questo motivo|Aus diesem Grund bleibe ich hier.|Espressioni B1
        aus meiner Sicht|dal mio punto di vista|Aus meiner Sicht ist das falsch.|Espressioni B1
        meiner Meinung nach|secondo me|Meiner Meinung nach ist das gut.|Espressioni B1
        es handelt sich um|si tratta di|Es handelt sich um ein Problem.|Espressioni B1
        es geht um|si tratta di|Es geht um viel Geld.|Espressioni B1
        eine Rolle spielen|avere un ruolo|Die Erfahrung spielt eine Rolle.|Espressioni B1
        Bescheid geben|avvisare|Bitte geben Sie mir Bescheid.|Espressioni B1
        Bescheid sagen|avvisare|Sag mir bitte Bescheid.|Espressioni B1
        in Ordnung|va bene|Das ist in Ordnung.|Espressioni B1
        zu Fuß|a piedi|Ich gehe zu Fuß zur Arbeit.|Espressioni B1
        am Ende|alla fine|Am Ende waren alle zufrieden.|Espressioni B1
        am Anfang|all'inizio|Am Anfang war es schwer.|Espressioni B1
        auf dem Land|in campagna|Auf dem Land ist es ruhig.|Espressioni B1
        unterwegs sein|essere in giro|Ich bin den ganzen Tag unterwegs.|Espressioni B1
        auf dem Weg|in cammino|Ich bin auf dem Weg nach Hause.|Espressioni B1
        recht haben|avere ragione|Du hast recht.|Espressioni B1
        Unrecht haben|avere torto|Da hast du Unrecht.|Espressioni B1
        Spaß machen|divertire|Die Arbeit macht mir Spaß.|Espressioni B1
        Angst haben|avere paura|Ich habe Angst vor Hunden.|Espressioni B1
        Glück haben|avere fortuna|Wir haben Glück gehabt.|Espressioni B1
        Pech haben|avere sfortuna|Er hat immer Pech.|Espressioni B1
        Zeit haben|avere tempo|Hast du heute Zeit?|Espressioni B1
        keine Zeit haben|non avere tempo|Ich habe leider keine Zeit.|Espressioni B1
        Urlaub nehmen|prendere ferie|Ich möchte im Juli Urlaub nehmen.|Espressioni B1
        Abschied nehmen|congedarsi|Wir müssen Abschied nehmen.|Espressioni B1
        Platz nehmen|prendere posto|Bitte nehmen Sie Platz.|Espressioni B1
        Kontakt aufnehmen|prendere contatto|Ich möchte Kontakt aufnehmen.|Espressioni B1
        einen Antrag stellen|presentare domanda|Sie müssen einen Antrag stellen.|Espressioni B1
        eine Frage stellen|porre una domanda|Darf ich eine Frage stellen?|Espressioni B1
        eine Entscheidung treffen|prendere una decisione|Wir müssen eine Entscheidung treffen.|Espressioni B1
        eine Lösung finden|trovare una soluzione|Wir müssen eine Lösung finden.|Espressioni B1
        einen Termin vereinbaren|fissare un appuntamento|Ich möchte einen Termin vereinbaren.|Espressioni B1
        einen Vertrag abschließen|stipulare un contratto|Wir wollen einen Vertrag abschließen.|Espressioni B1
        eine Prüfung bestehen|superare un esame|Sie will eine Prüfung bestehen.|Espressioni B1
        einen Fehler machen|commettere un errore|Jeder kann einen Fehler machen.|Espressioni B1
        Sport treiben|fare sport|Ich möchte mehr Sport treiben.|Espressioni B1
        Rad fahren|andare in bicicletta|Wir wollen Rad fahren.|Espressioni B1
        spazieren gehen|fare una passeggiata|Wir wollen spazieren gehen.|Espressioni B1
        einkaufen gehen|andare a fare la spesa|Ich muss einkaufen gehen.|Espressioni B1
        essen gehen|andare a mangiare|Wir wollen essen gehen.|Espressioni B1
        nach Hause gehen|andare a casa|Ich will nach Hause gehen.|Espressioni B1
        zu Bett gehen|andare a letto|Die Kinder sollen zu Bett gehen.|Espressioni B1
        auf die Nerven gehen|dare sui nervi|Der Lärm geht mir auf die Nerven.|Espressioni B1
        in Ordnung bringen|mettere a posto|Ich will das in Ordnung bringen.|Espressioni B1
        zum Ausdruck bringen|esprimere|Er will seine Meinung zum Ausdruck bringen.|Espressioni B1
        zur Welt bringen|dare alla luce|Sie will ein Kind zur Welt bringen.|Espressioni B1
        im Moment|al momento|Im Moment habe ich keine Zeit.|Espressioni B1
        im Augenblick|in questo momento|Im Augenblick ist alles ruhig.|Espressioni B1
        im Voraus|in anticipo|Bitte zahlen Sie im Voraus.|Espressioni B1
        im Nachhinein|a posteriori|Im Nachhinein war es klar.|Espressioni B1
        nach und nach|poco a poco|Nach und nach wird es besser.|Espressioni B1
        von Zeit zu Zeit|di tanto in tanto|Von Zeit zu Zeit besuche ich sie.|Espressioni B1
        hin und wieder|ogni tanto|Hin und wieder gehe ich schwimmen.|Espressioni B1
        nach wie vor|come prima|Er ist nach wie vor krank.|Espressioni B1
        sowieso|comunque|Das weiß ich sowieso.|Espressioni B1
        ohnehin|comunque|Es ist ohnehin zu spät.|Espressioni B1
        jedenfalls|in ogni caso|Jedenfalls komme ich mit.|Espressioni B1
        unter anderem|tra l'altro|Unter anderem geht es um Geld.|Espressioni B1
        zum Teil|in parte|Das stimmt zum Teil.|Espressioni B1
        zum Glück|per fortuna|Zum Glück ist nichts passiert.|Espressioni B1
        zum Schluss|alla fine|Zum Schluss bedanke ich mich.|Espressioni B1
        mit Absicht|di proposito|Das war mit Absicht.|Espressioni B1
        ohne Absicht|senza intenzione|Das war ohne Absicht.|Espressioni B1

    """.trimIndent()
}
