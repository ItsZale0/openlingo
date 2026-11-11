package com.alessandro.openlingo.data

/**
 * Lessico curato A2: tedesco|italiano|esempio|tema.
 *
 * 708 parole nuove rispetto ad A1; con quelle di A1 la copertura arriva a
 * 1.378 parole (traguardo cumulativo A2: 1.300). Le voci già presenti in
 * [LessicoA1] sono state rimosse: qui restano solo le nuove.
 */
object LessicoA2 {

    val grezzo: String = """
        der Kollege|collega|Mein Kollege hat mir geholfen.|Lavoro
        die Kollegin|collega (f)|Die Kollegin ist neu im Team.|Lavoro
        der Chef|capo|Mein Chef ist heute nicht da.|Lavoro
        die Chefin|capa|Die Chefin hat einen Termin.|Lavoro
        die Besprechung|riunione|Die Besprechung dauert eine Stunde.|Lavoro
        das Protokoll|verbale|Ich schreibe das Protokoll.|Lavoro
        die Aufgabe|compito|Diese Aufgabe ist leicht.|Lavoro
        die Frist|scadenza|Die Frist ist am Freitag.|Lavoro
        der Vertrag|contratto|Der Vertrag ist unterschrieben.|Lavoro
        die Kündigung|disdetta|Die Kündigung muss schriftlich sein.|Lavoro
        das Gehalt|stipendio|Das Gehalt kommt am Monatsende.|Lavoro
        die Überstunde|ora straordinaria|Ich mache heute Überstunden.|Lavoro
        der Urlaubstag|giorno di ferie|Ich habe noch fünf Urlaubstage.|Lavoro
        die Bewerbung|candidatura|Ich habe meine Bewerbung geschickt.|Lavoro
        der Lebenslauf|curriculum|Mein Lebenslauf ist aktuell.|Lavoro
        das Vorstellungsgespräch|colloquio di lavoro|Das Vorstellungsgespräch war gut.|Lavoro
        die Ausbildung|formazione|Die Ausbildung dauert drei Jahre.|Lavoro
        die Erfahrung|esperienza|Ich habe zwei Jahre Erfahrung.|Lavoro
        die Verantwortung|responsabilità|Sie übernimmt viel Verantwortung.|Lavoro
        die Zusammenarbeit|collaborazione|Die Zusammenarbeit funktioniert gut.|Lavoro
        der Feierabend|fine lavoro|Endlich Feierabend!|Lavoro
        die Schicht|turno|Ich arbeite in der Nachtschicht.|Lavoro
        der Dienst|servizio|Der Dienst beginnt um sechs.|Lavoro
        die Krankmeldung|comunicazione di malattia|Ich habe die Krankmeldung geschickt.|Lavoro
        der Betrieb|azienda|Der Betrieb hat fünfzig Mitarbeiter.|Lavoro
        die Abteilung|reparto|Ich arbeite in der Chirurgie-Abteilung.|Lavoro
        der Mitarbeiter|collaboratore|Alle Mitarbeiter sind zufrieden.|Lavoro
        die Stelle|posto di lavoro|Ich suche eine neue Stelle.|Lavoro
        der Arbeitgeber|datore di lavoro|Der Arbeitgeber zahlt pünktlich.|Lavoro
        die Weiterbildung|formazione continua|Die Weiterbildung ist gratis.|Lavoro
        die Steuer|tassa|Die Steuer wird jedes Jahr berechnet.|Lavoro
        die Versicherung|assicurazione|Die Versicherung zahlt den Schaden.|Lavoro
        der Ausweis|documento|Bitte zeigen Sie Ihren Ausweis.|Ufficio
        das Formular|modulo|Füllen Sie das Formular aus.|Ufficio
        die Unterschrift|firma|Hier fehlt noch Ihre Unterschrift.|Ufficio
        der Antrag|domanda|Der Antrag ist noch nicht bearbeitet.|Ufficio
        die Anmeldung|iscrizione|Die Anmeldung ist online möglich.|Ufficio
        die Bestätigung|conferma|Ich warte auf die Bestätigung.|Ufficio
        die Rechnung|fattura|Die Rechnung ist bezahlt.|Ufficio
        der Betrag|importo|Der Betrag ist zu hoch.|Ufficio
        die Quittung|scontrino|Kann ich die Quittung haben?|Ufficio
        die Anzahlung|acconto|Die Anzahlung beträgt hundert Euro.|Ufficio
        der Rabatt|sconto|Gibt es einen Rabatt?|Acquisti
        die Garantie|garanzia|Die Garantie gilt zwei Jahre.|Acquisti
        die Größe|taglia|Welche Größe brauchen Sie?|Acquisti
        das Angebot|offerta|Das Angebot gilt bis Sonntag.|Acquisti
        der Preis|prezzo|Der Preis ist fair.|Acquisti
        die Kasse|cassa|Bitte zur Kasse gehen.|Acquisti
        der Umtausch|cambio merce|Der Umtausch ist kostenlos.|Acquisti
        die Lieferung|consegna|Die Lieferung kommt morgen.|Acquisti
        die Bestellung|ordine|Meine Bestellung ist angekommen.|Acquisti
        der Online-Shop|negozio online|Ich kaufe im Online-Shop.|Acquisti
        die Sendung|spedizione|Die Sendung ist unterwegs.|Acquisti
        die Postleitzahl|codice postale|Wie ist Ihre Postleitzahl?|Acquisti
        der Empfänger|destinatario|Der Empfänger wohnt in Meran.|Acquisti
        die Bankkarte|carta bancaria|Ich zahle mit Bankkarte.|Acquisti
        das Konto|conto|Mein Konto ist im Minus.|Acquisti
        die Überweisung|bonifico|Die Überweisung dauert zwei Tage.|Acquisti
        der Geldautomat|bancomat|Der Geldautomat ist außer Betrieb.|Acquisti
        die Verspätung|ritardo|Der Zug hat zwanzig Minuten Verspätung.|Viaggi
        der Anschluss|coincidenza|Ich habe den Anschluss verpasst.|Viaggi
        das Gleis|binario|Der Zug fährt auf Gleis drei.|Viaggi
        die Reservierung|prenotazione|Ich habe eine Reservierung.|Viaggi
        der Aufenthalt|soggiorno|Der Aufenthalt dauert eine Woche.|Viaggi
        die Unterkunft|alloggio|Die Unterkunft ist günstig.|Viaggi
        die Pension|pensione|Die Pension liegt im Zentrum.|Viaggi
        der Campingplatz|campeggio|Der Campingplatz ist am See.|Viaggi
        der Ausflug|gita|Wir machen einen Ausflug.|Viaggi
        die Sehenswürdigkeit|attrazione|Die Sehenswürdigkeit ist berühmt.|Viaggi
        der Führer|guida turistica|Der Führer spricht Italienisch.|Viaggi
        die Grenze|confine|Die Grenze ist nah.|Viaggi
        der Zoll|dogana|Am Zoll gab es eine Kontrolle.|Viaggi
        der Koffer|valigia|Mein Koffer ist zu schwer.|Viaggi
        das Gepäck|bagaglio|Das Gepäck ist verloren.|Viaggi
        der Rucksack|zaino|Der Rucksack ist praktisch.|Viaggi
        die Abfahrt|partenza|Die Abfahrt ist um acht.|Viaggi
        die Ankunft|arrivo|Die Ankunft war pünktlich.|Viaggi
        die Rückfahrt|viaggio di ritorno|Die Rückfahrt dauert lange.|Viaggi
        die Panne|guasto|Wir hatten eine Panne.|Viaggi
        tanken|fare rifornimento|Ich muss noch tanken.|Viaggi
        parken|parcheggiare|Hier darf man nicht parken.|Viaggi
        umsteigen|cambiare mezzo|In München müssen wir umsteigen.|Viaggi
        einsteigen|salire|Bitte einsteigen!|Viaggi
        aussteigen|scendere|Wir steigen am Bahnhof aus.|Viaggi
        der Blutdruck|pressione sanguigna|Ihr Blutdruck ist normal.|Salute
        die Atmung|respirazione|Die Atmung ist ruhig.|Salute
        die Temperatur|temperatura|Ich messe die Temperatur.|Salute
        die Untersuchung|visita medica|Die Untersuchung dauert zehn Minuten.|Salute
        die Behandlung|trattamento|Die Behandlung beginnt morgen.|Salute
        die Operation|operazione|Die Operation war erfolgreich.|Salute
        die Wunde|ferita|Die Wunde ist sauber.|Salute
        die Verletzung|lesione|Die Verletzung ist nicht schwer.|Salute
        die Entzündung|infiammazione|Die Entzündung ist stark.|Salute
        die Allergie|allergia|Ich habe eine Allergie gegen Penicillin.|Salute
        der Durchfall|diarrea|Der Patient hat Durchfall.|Salute
        die Übelkeit|nausea|Die Übelkeit ist besser geworden.|Salute
        der Schwindel|capogiro|Ich habe Schwindel.|Salute
        die Atemnot|difficoltà respiratorie|Atemnot ist ein Notfall.|Salute
        der Notfall|emergenza|Das ist ein Notfall.|Salute
        die Impfung|vaccinazione|Die Impfung ist empfohlen.|Salute
        das Rezept|ricetta medica|Hier ist Ihr Rezept.|Salute
        die Salbe|pomata|Die Salbe hilft gegen Schmerzen.|Salute
        die Spritze|iniezione|Die Spritze tut nicht weh.|Salute
        die Krankengymnastik|fisioterapia|Die Krankengymnastik beginnt nächste Woche.|Salute
        die Entlassung|dimissione|Die Entlassung ist am Freitag.|Salute
        die Aufnahme|ricovero|Die Aufnahme war gestern.|Salute
        die Station|reparto|Sie liegt auf Station drei.|Salute
        die Ambulanz|ambulanza|Die Ambulanz ist rund um die Uhr offen.|Salute
        der Befund|referto|Der Befund ist noch nicht da.|Salute
        die Vorsorge|prevenzione|Vorsorge ist wichtig.|Salute
        abnehmen|diminuire di peso|Ich habe zwei Kilo abgenommen.|Salute
        zunehmen|aumentare di peso|Im Winter nehme ich zu.|Salute
        sich fühlen|sentirsi|Ich fühle mich nicht gut.|Salute
        sich ausruhen|riposarsi|Du musst dich ausruhen.|Salute
        sich verletzen|farsi male|Er hat sich am Knie verletzt.|Salute
        die Beziehung|relazione|Die Beziehung ist stabil.|Relazioni
        der Partner|partner|Mein Partner wohnt in Meran.|Relazioni
        der Nachbar|vicino di casa|Der Nachbar ist sehr laut.|Relazioni
        die Nachbarin|vicina di casa|Die Nachbarin hat einen Hund.|Relazioni
        der Bekannte|conoscente|Ich habe einen Bekannten in Wien.|Relazioni
        die Freundschaft|amicizia|Unsere Freundschaft ist alt.|Relazioni
        die Hochzeit|matrimonio|Die Hochzeit ist im Juni.|Relazioni
        die Ehe|matrimonio|Ihre Ehe ist glücklich.|Relazioni
        die Trennung|separazione|Die Trennung war schwer.|Relazioni
        der Streit|litigio|Wir hatten einen kleinen Streit.|Relazioni
        das Gespräch|conversazione|Das Gespräch war hilfreich.|Relazioni
        die Meinung|opinione|Meine Meinung ist anders.|Relazioni
        das Verständnis|comprensione|Danke für Ihr Verständnis.|Relazioni
        die Sorge|preoccupazione|Ich habe große Sorgen.|Relazioni
        die Freude|gioia|Die Freude war groß.|Relazioni
        die Angst|paura|Ich habe Angst vor Spritzen.|Relazioni
        der Ärger|rabbia|Mein Ärger ist verflogen.|Relazioni
        die Geduld|pazienza|Du brauchst mehr Geduld.|Relazioni
        das Vertrauen|fiducia|Vertrauen muss man verdienen.|Relazioni
        einladen|invitare|Ich lade dich zum Essen ein.|Relazioni
        sich treffen|incontrarsi|Wir treffen uns um sechs.|Relazioni
        sich freuen|rallegrarsi|Ich freue mich auf dich.|Relazioni
        sich entschuldigen|scusarsi|Er hat sich entschuldigt.|Relazioni
        sich streiten|litigare|Die Kinder streiten oft.|Relazioni
        sich verstehen|capirsi|Wir verstehen uns gut.|Relazioni
        die Schule|scuola|Die Schule beginnt im September.|Istruzione
        der Unterricht|lezione|Der Unterricht ist langweilig.|Istruzione
        die Note|voto|Ich habe eine gute Note bekommen.|Istruzione
        die Prüfung|esame|Die Prüfung ist am Montag.|Istruzione
        die Hausaufgabe|compito a casa|Ich mache meine Hausaufgaben.|Istruzione
        das Zeugnis|pagella|Mein Zeugnis war gut.|Istruzione
        der Kurs|corso|Der Kurs kostet zweihundert Euro.|Istruzione
        die Bibliothek|biblioteca|Ich lerne in der Bibliothek.|Istruzione
        das Wörterbuch|dizionario|Ich brauche ein Wörterbuch.|Istruzione
        die Übung|esercizio|Diese Übung ist schwer.|Istruzione
        der Fehler|errore|Ich mache viele Fehler.|Istruzione
        die Regel|regola|Die Regel ist einfach.|Istruzione
        die Grammatik|grammatica|Die deutsche Grammatik ist schwer.|Istruzione
        die Aussprache|pronuncia|Deine Aussprache ist gut.|Istruzione
        der Fortschritt|progresso|Du machst große Fortschritte.|Istruzione
        die Wiederholung|ripetizione|Wiederholung ist wichtig.|Istruzione
        das Thema|argomento|Das Thema ist interessant.|Istruzione
        der Satz|frase|Der Satz ist zu lang.|Istruzione
        die Bedeutung|significato|Was ist die Bedeutung?|Istruzione
        die Übersetzung|traduzione|Die Übersetzung ist nicht leicht.|Istruzione
        das Studium|studi universitari|Mein Studium dauert drei Jahre.|Istruzione
        der Abschluss|diploma|Der Abschluss war schwer.|Istruzione
        die Wohnung|appartamento|Die Wohnung ist zu teuer.|Casa
        die Miete|affitto|Die Miete steigt jedes Jahr.|Casa
        der Vermieter|padrone di casa|Der Vermieter wohnt oben.|Casa
        die Nebenkosten|spese accessorie|Die Nebenkosten sind inklusive.|Casa
        der Umzug|trasloco|Der Umzug war anstrengend.|Casa
        die Klingel|campanello|Die Klingel funktioniert nicht.|Casa
        der Aufzug|ascensore|Der Aufzug ist kaputt.|Casa
        der Keller|cantina|Die Fahrräder sind im Keller.|Casa
        der Balkon|balcone|Wir frühstücken auf dem Balkon.|Casa
        die Heizung|riscaldamento|Die Heizung ist aus.|Casa
        der Strom|corrente|Der Strom ist ausgefallen.|Casa
        das Gerät|apparecchio|Das Gerät ist neu.|Casa
        die Reparatur|riparazione|Die Reparatur kostet viel.|Casa
        der Handwerker|artigiano|Der Handwerker kommt um neun.|Casa
        die Ordnung|ordine|Ich brauche mehr Ordnung.|Casa
        der Müll|rifiuti|Der Müll wird am Dienstag abgeholt.|Ambiente
        die Umwelt|ambiente|Wir müssen die Umwelt schützen.|Ambiente
        der Abfall|rifiuto|Bitte den Abfall trennen.|Ambiente
        das Recycling|riciclaggio|Recycling ist hier normal.|Ambiente
        die Energie|energia|Wir sparen Energie.|Ambiente
        das Klima|clima|Das Klima verändert sich.|Ambiente
        die Verschmutzung|inquinamento|Die Verschmutzung nimmt zu.|Ambiente
        der Verkehr|traffico|Der Verkehr ist morgens schlimm.|Ambiente
        die Luft|aria|Die Luft ist frisch.|Ambiente
        der Baum|albero|Der Baum ist alt.|Ambiente
        die Blume|fiore|Die Blumen sind schön.|Ambiente
        das Wetter|meteo|Das Wetter wird besser.|Meteo
        der Nebel|nebbia|Am Morgen gibt es Nebel.|Meteo
        der Sturm|tempesta|Der Sturm war stark.|Meteo
        das Gewitter|temporale|Ein Gewitter kommt.|Meteo
        der Regenbogen|arcobaleno|Der Regenbogen ist schön.|Meteo
        die Hitze|caldo intenso|Die Hitze ist unerträglich.|Meteo
        die Kälte|freddo|Die Kälte kommt im Dezember.|Meteo
        der Grad|grado|Es sind zwanzig Grad.|Meteo
        sonnig|soleggiato|Morgen wird es sonnig.|Meteo
        bewölkt|nuvoloso|Der Himmel ist bewölkt.|Meteo
        windig|ventoso|Heute ist es windig.|Meteo
        das Handy|cellulare|Mein Handy ist kaputt.|Media
        die Nachricht|messaggio|Ich habe eine Nachricht bekommen.|Media
        die E-Mail|email|Ich schreibe eine E-Mail.|Media
        das Internet|internet|Das Internet ist langsam.|Media
        das Passwort|password|Ich habe mein Passwort vergessen.|Media
        die Verbindung|connessione|Die Verbindung ist schlecht.|Media
        die App|app|Diese App ist nützlich.|Media
        das Programm|programma|Das Programm startet nicht.|Media
        der Bildschirm|schermo|Der Bildschirm ist groß.|Media
        das Foto|foto|Das Foto ist gelungen.|Media
        das Video|video|Das Video dauert fünf Minuten.|Media
        die Nachrichten|notizie|Ich sehe die Nachrichten.|Media
        die Werbung|pubblicità|Die Werbung ist zu lang.|Media
        der Artikel|articolo|Der Artikel ist interessant.|Media
        der Bericht|resoconto|Der Bericht war gut.|Media
        die Zeitung|giornale|Die Zeitung kommt morgens.|Media
        die Zeitschrift|rivista|Die Zeitschrift liegt im Warteraum.|Media
        das Radio|radio|Ich höre Radio im Auto.|Media
        der Sender|emittente|Der Sender ist neu.|Media
        die Fernsehsendung|trasmissione|Die Fernsehsendung beginnt um acht.|Media
        speichern|salvare|Ich speichere die Datei.|Media
        löschen|cancellare|Ich habe die Datei gelöscht.|Media
        herunterladen|scaricare|Ich habe das Programm heruntergeladen.|Media
        hochladen|caricare|Ich lade das Foto hoch.|Media
        klicken|cliccare|Klicken Sie hier.|Media
        teilen|condividere|Ich teile das Foto mit dir.|Media
        sonst|altrimenti|Beeil dich, sonst kommen wir zu spät.|Connettivi
        deshalb|per questo|Es regnet, deshalb bleiben wir hier.|Connettivi
        trotzdem|comunque|Ich bin müde, trotzdem lerne ich.|Connettivi
        weil|perché|Ich lerne Deutsch, weil ich in Deutschland arbeite.|Connettivi
        dass|che|Ich glaube, dass er kommt.|Connettivi
        wenn|se/quando|Wenn es regnet, bleiben wir zu Hause.|Connettivi
        als|quando (passato)|Als ich jung war, wohnte ich in Rom.|Connettivi
        ob|se|Ich weiß nicht, ob er kommt.|Connettivi
        während|mentre|Während ich koche, hört er Musik.|Connettivi
        bevor|prima che|Wasch dir die Hände, bevor du isst.|Connettivi
        nachdem|dopo che|Nachdem ich gegessen habe, gehe ich spazieren.|Connettivi
        damit|affinché|Ich lerne, damit ich die Prüfung bestehe.|Connettivi
        obwohl|benche|Obwohl es kalt ist, gehe ich raus.|Connettivi
        sowohl|sia|Sowohl der Arzt als auch die Schwester sind da.|Connettivi
        entweder|o|Entweder heute oder morgen.|Connettivi
        zuerst|prima|Zuerst frühstücke ich.|Connettivi
        danach|dopo|Danach gehe ich zur Arbeit.|Connettivi
        schließlich|infine|Schließlich sind wir angekommen.|Connettivi
        übrigens|a proposito|Übrigens, hast du Zeit?|Connettivi
        eigentlich|in realtà|Eigentlich wollte ich nicht kommen.|Connettivi
        der Grund|motivo|Was ist der Grund?|Connettivi
        die Lösung|soluzione|Wir brauchen eine Lösung.|Connettivi
        die Möglichkeit|possibilità|Es gibt eine andere Möglichkeit.|Connettivi
        der Vorschlag|proposta|Ich habe einen Vorschlag.|Connettivi
        die Entscheidung|decisione|Die Entscheidung ist schwer.|Connettivi
        das Problem|problema|Das Problem ist gelöst.|Connettivi
        die Frage|domanda|Ich habe eine Frage.|Connettivi
        die Antwort|risposta|Deine Antwort ist richtig.|Connettivi
        der Unterschied|differenza|Was ist der Unterschied?|Connettivi
        absagen|annullare|Ich muss den Termin absagen.|Verbi A2
        zusagen|confermare|Er hat für Samstag zugesagt.|Verbi A2
        verschieben|spostare|Wir müssen den Termin verschieben.|Verbi A2
        vereinbaren|concordare|Ich möchte einen Termin vereinbaren.|Verbi A2
        sich bewerben|candidarsi|Ich bewerbe mich um die Stelle.|Verbi A2
        einstellen|assumere|Die Firma stellt zehn Leute ein.|Verbi A2
        kündigen|licenziarsi|Ich habe gekündigt.|Verbi A2
        verdienen|guadagnare|Er verdient gut.|Verbi A2
        ausgeben|spendere|Ich gebe zu viel Geld aus.|Verbi A2
        sparen|risparmiare|Wir sparen für den Urlaub.|Verbi A2
        bestellen|ordinare|Ich bestelle eine Pizza.|Verbi A2
        liefern|consegnare|Die Firma liefert morgen.|Verbi A2
        reparieren|riparare|Kannst du das reparieren?|Verbi A2
        benutzen|usare|Ich benutze den Bus jeden Tag.|Verbi A2
        vergessen|dimenticare|Ich habe den Termin vergessen.|Verbi A2
        erinnern|ricordare|Ich erinnere mich an dich.|Verbi A2
        verstehen|capire|Ich habe alles verstanden.|Verbi A2
        erklären|spiegare|Erklär mir das noch einmal.|Verbi A2
        beschreiben|descrivere|Beschreiben Sie das Problem.|Verbi A2
        erzählen|raccontare|Erzähl mir von deiner Reise.|Verbi A2
        berichten|riferire|Die Zeitung berichtet über den Unfall.|Verbi A2
        sich beschweren|lamentarsi|Ich möchte mich beschweren.|Verbi A2
        sich entscheiden|decidere|Ich habe mich entschieden.|Verbi A2
        sich interessieren|interessarsi|Ich interessiere mich für Medizin.|Verbi A2
        sich verabreden|darsi appuntamento|Wir verabreden uns für Freitag.|Verbi A2
        sich abmelden|disdire|Ich melde mich vom Kurs ab.|Verbi A2
        sich anmelden|iscriversi|Ich melde mich für den Kurs an.|Verbi A2
        zurückgeben|restituire|Ich gebe dir das Buch zurück.|Verbi A2
        wegwerfen|buttare|Wirf den Müll nicht weg.|Verbi A2
        aufräumen|riordinare|Ich räume mein Zimmer auf.|Verbi A2
        anziehen|indossare|Ich ziehe die Jacke an.|Verbi A2
        ausziehen|togliere|Zieh die Schuhe aus.|Verbi A2
        frühstücken|fare colazione|Ich frühstücke um sieben.|Verbi A2
        mitbringen|portare con sé|Bring bitte Wasser mit.|Verbi A2
        mitnehmen|prendere con sé|Nimm den Regenschirm mit.|Verbi A2
        einkaufen|fare la spesa|Ich kaufe im Supermarkt ein.|Verbi A2
        anprobieren|provare (vestiti)|Ich möchte das Kleid anprobieren.|Verbi A2
        umtauschen|cambiare merce|Kann ich das umtauschen?|Verbi A2
        reklamieren|reclamare|Ich möchte das Gerät reklamieren.|Verbi A2
        sich erholen|riprendersi|Im Urlaub erhole ich mich.|Verbi A2
        sich beeilen|affrettarsi|Beeil dich, bitte!|Verbi A2
        sich langweilen|annoiarsi|Ich langweile mich zu Hause.|Verbi A2
        sich ärgern|arrabbiarsi|Ich ärgere mich über ihn.|Verbi A2
        sich Sorgen machen|preoccuparsi|Mach dir keine Sorgen.|Verbi A2
        aufpassen|fare attenzione|Pass auf die Kinder auf.|Verbi A2
        zuhören|ascoltare|Hör mir bitte zu.|Verbi A2
        mitspielen|partecipare al gioco|Die Kinder spielen mit.|Verbi A2
        stattfinden|avere luogo|Das Konzert findet am Samstag statt.|Verbi A2
        teilnehmen|partecipare|Ich nehme am Kurs teil.|Verbi A2
        besprechen|discutere|Wir besprechen das morgen.|Verbi A2
        entscheiden|decidere|Du entscheidest, wohin wir gehen.|Verbi A2
        bekommen|ricevere|Ich habe einen Brief bekommen.|Verbi A2
        schicken|inviare|Ich schicke dir die Adresse.|Verbi A2
        zeigen|mostrare|Zeig mir bitte den Weg.|Verbi A2
        folgen|seguire|Folgen Sie mir, bitte.|Verbi A2
        warten|aspettare|Wir warten schon zehn Minuten.|Verbi A2
        dauern|durare|Die Fahrt dauert eine Stunde.|Verbi A2
        passieren|succedere|Was ist passiert?|Verbi A2
        klappen|funzionare|Es hat gut geklappt.|Verbi A2
        fehlen|mancare|Mir fehlt die Zeit.|Verbi A2
        gelten|valere|Das Ticket gilt den ganzen Tag.|Verbi A2
        empfehlen|consigliare|Können Sie ein Hotel empfehlen?|Verbi A2
        vorschlagen|proporre|Ich schlage ein Café vor.|Verbi A2
        anbieten|offrire|Die Firma bietet Hilfe an.|Verbi A2
        ablehnen|rifiutare|Er hat das Angebot abgelehnt.|Verbi A2
        zustimmen|essere d'accordo|Ich stimme dir zu.|Verbi A2
        widersprechen|contraddire|Ich muss dir widersprechen.|Verbi A2
        bezahlen|pagare|Ich bezahle mit Karte.|Verbi A2
        der Erfolg|successo|Der Erfolg kommt langsam.|Astratti
        das Ziel|obiettivo|Mein Ziel ist B1.|Astratti
        die Fähigkeit|capacità|Er hat gute Fähigkeiten.|Astratti
        die Kenntnis|conoscenza|Meine Kenntnisse sind noch klein.|Astratti
        die Sicherheit|sicurezza|Sicherheit geht vor.|Astratti
        die Gefahr|pericolo|Die Gefahr ist nicht groß.|Astratti
        die Ruhe|calma|Ich brauche Ruhe.|Astratti
        der Stress|stress|Ich habe viel Stress.|Astratti
        die Freizeit|tempo libero|In meiner Freizeit lese ich.|Astratti
        die Gewohnheit|abitudine|Das ist eine gute Gewohnheit.|Astratti
        die Erinnerung|ricordo|Ich habe schöne Erinnerungen.|Astratti
        die Zukunft|futuro|Die Zukunft ist offen.|Astratti
        die Vergangenheit|passato|Die Vergangenheit ist vorbei.|Astratti
        die Gegenwart|presente|Wir leben in der Gegenwart.|Astratti
        der Anfang|inizio|Der Anfang war schwer.|Astratti
        das Ende|fine|Das Ende war überraschend.|Astratti
        die Mitte|metà|Die Mitte des Raumes ist frei.|Astratti
        der Teil|parte|Der erste Teil ist leicht.|Astratti
        das Stück|pezzo|Ein Stück Kuchen, bitte.|Astratti
        die Menge|quantità|Die Menge ist begrenzt.|Astratti
        die Zahl|numero|Die Zahl ist falsch.|Astratti
        die Liste|lista|Die Liste ist lang.|Astratti
        der Plan|piano|Der Plan ist gut.|Astratti
        die Idee|idea|Das ist eine gute Idee.|Astratti
        der Vorteil|vantaggio|Der Vorteil ist klar.|Astratti
        der Nachteil|svantaggio|Der Nachteil ist der Preis.|Astratti
        der Vergleich|confronto|Der Vergleich ist fair.|Astratti
        die Wahl|scelta|Die Wahl ist nicht leicht.|Astratti
        das Beispiel|esempio|Gib mir ein Beispiel.|Astratti
        der Arbeitsplatz|posto di lavoro|Mein Arbeitsplatz ist modern.|Lavoro
        die Arbeitszeit|orario di lavoro|Die Arbeitszeit beginnt um acht.|Lavoro
        die Teilzeit|tempo parziale|Sie arbeitet in Teilzeit.|Lavoro
        die Vollzeit|tempo pieno|Er arbeitet in Vollzeit.|Lavoro
        die Fortbildung|aggiornamento|Die Fortbildung ist freiwillig.|Lavoro
        die Karriere|carriera|Er macht Karriere in der Firma.|Lavoro
        der Arbeitsvertrag|contratto di lavoro|Der Arbeitsvertrag ist befristet.|Lavoro
        der Angestellte|impiegato|Der Angestellte arbeitet im Büro.|Lavoro
        der Arbeiter|operaio|Der Arbeiter trägt einen Helm.|Lavoro
        der Praktikant|tirocinante|Der Praktikant lernt viel.|Lavoro
        der Auszubildende|apprendista|Der Auszubildende ist im zweiten Jahr.|Lavoro
        der Ingenieur|ingegnere|Der Ingenieur arbeitet an einem Projekt.|Lavoro
        der Techniker|tecnico|Der Techniker prüft die Maschine.|Lavoro
        der Anwalt|avvocato|Der Anwalt hilft bei Verträgen.|Lavoro
        der Steuerberater|commercialista|Der Steuerberater macht die Steuer.|Lavoro
        der Unternehmer|imprenditore|Der Unternehmer hat eine kleine Firma.|Lavoro
        die Sekretärin|segretaria|Die Sekretärin macht die Termine.|Lavoro
        der Empfang|reception|Am Empfang bekommt man Besucherkarten.|Lavoro
        die Sitzung|seduta|Die Sitzung dauert eine Stunde.|Lavoro
        der Feiertag|giorno festivo|Der Feiertag ist am Montag.|Lavoro
        die Nachtschicht|turno di notte|Die Nachtschicht beginnt um zehn.|Lavoro
        der Job|lavoretto|Er hat einen Job im Café.|Lavoro
        die Bewerbungsfrist|termine per la candidatura|Die Bewerbungsfrist ist im März.|Lavoro
        die Empfehlung|raccomandazione|Ich habe eine Empfehlung bekommen.|Lavoro
        der Kontakt|contatto|Der Kontakt ist wichtig.|Lavoro
        die Erlaubnis|permesso|Ich brauche die Erlaubnis vom Chef.|Lavoro
        die Kantine|mensa aziendale|Wir essen in der Kantine.|Lavoro
        der Drucker|stampante|Der Drucker funktioniert nicht.|Ufficio
        der Kopierer|fotocopiatrice|Der Kopierer ist leer.|Ufficio
        die Tastatur|tastiera|Die Tastatur hat keine Batterie.|Ufficio
        der Laptop|portatile|Mein Laptop ist schwer.|Ufficio
        der Ordner|cartella|Der Ordner liegt auf dem Tisch.|Ufficio
        der Terminkalender|agenda|Der Terminkalender ist voll.|Ufficio
        die Notiz|nota|Ich schreibe eine Notiz.|Ufficio
        die Datei|file|Die Datei ist zu groß.|Ufficio
        der USB-Stick|chiavetta USB|Der USB-Stick ist im Computer.|Ufficio
        das Ladekabel|cavo di ricarica|Das Ladekabel fehlt.|Ufficio
        der Akku|batteria|Der Akku ist leer.|Ufficio
        der Anhang|allegato|Der Anhang fehlt.|Ufficio
        der Ausdruck|stampa|Der Ausdruck ist unscharf.|Ufficio
        die Software|software|Die Software ist neu.|Ufficio
        die Website|sito web|Die Website ist online.|Ufficio
        der Link|link|Der Link funktioniert nicht.|Ufficio
        der Automat|bancomat|Der Automat ist außer Betrieb.|Banca
        die Gebühr|commissione|Die Gebühr beträgt fünf Euro.|Banca
        der Kredit|credito|Der Kredit läuft über fünf Jahre.|Banca
        der Schaden|danno|Der Schaden ist groß.|Banca
        die Police|polizza|Die Police liegt zu Hause.|Banca
        die Rate|rata|Ich zahle die Rate monatlich.|Banca
        der Zins|interesse|Der Zins ist niedrig.|Banca
        das Sparbuch|libretto di risparmio|Das Sparbuch ist bei der Bank.|Banca
        die Filiale|filiale|Die Filiale schließt um fünf.|Banca
        der Schalter|sportello|Bitte gehen Sie zum Schalter drei.|Banca
        das Paket|pacco|Das Paket ist angekommen.|Posta
        der Brief|lettera|Der Brief ist für dich.|Posta
        die Briefmarke|francobollo|Die Briefmarke kostet eins zwanzig.|Posta
        der Umschlag|busta|Der Umschlag ist offen.|Posta
        der Absender|mittente|Der Absender fehlt.|Posta
        die Zustellung|recapito|Die Zustellung dauert drei Tage.|Posta
        das Einschreiben|raccomandata|Ich schicke ein Einschreiben.|Posta
        die Schulter|spalla|Die Schulter ist steif.|Corpo
        das Knie|ginocchio|Das Knie ist geschwollen.|Corpo
        die Zehe|dito del piede|Die Zehe ist kalt.|Corpo
        die Haut|pelle|Die Haut ist trocken.|Corpo
        die Lunge|polmone|Die Lunge arbeitet gut.|Corpo
        der Magen|stomaco|Der Magen ist leer.|Corpo
        das Blut|sangue|Das Blut ist rot.|Corpo
        der Knochen|osso|Der Knochen ist stark.|Corpo
        der Muskel|muscolo|Der Muskel ist verspannt.|Corpo
        der Zahnarzt|dentista|Der Zahnarzt arbeitet heute.|Corpo
        die Gesundheit|salute|Gesundheit ist wichtig.|Salute 2
        die Krankheit|malattia|Die Krankheit dauert eine Woche.|Salute 2
        der Schnupfen|raffreddore|Ich habe Schnupfen.|Salute 2
        die Halsschmerzen|mal di gola|Ich habe Halsschmerzen.|Salute 2
        die Kopfschmerzen|mal di testa|Die Kopfschmerzen sind stark.|Salute 2
        die Bauchschmerzen|mal di pancia|Das Kind hat Bauchschmerzen.|Salute 2
        die Patientin|paziente (f)|Die Patientin ist ruhig.|Salute 2
        die Schmerztablette|antidolorifico|Ich brauche eine Schmerztablette.|Salute 2
        die Narbe|cicatrice|Die Narbe ist klein.|Salute 2
        der Unfall|incidente|Der Unfall war leicht.|Salute 2
        der Krankenwagen|ambulanza|Der Krankenwagen wartet draußen.|Salute 2
        die Ernährung|alimentazione|Gesunde Ernährung hilft.|Salute 2
        die Diät|dieta|Der Arzt empfiehlt eine Diät.|Salute 2
        das Gewicht|peso|Das Gewicht ist normal.|Salute 2
        der Schlaf|sonno|Guter Schlaf ist wichtig.|Salute 2
        die Müdigkeit|stanchezza|Die Müdigkeit kommt am Abend.|Salute 2
        die Entspannung|rilassamento|Entspannung ist wichtig.|Salute 2
        dick|grasso|Der Hund ist dick.|Aspetto
        dünn|sottile|Das Buch ist dünn.|Aspetto
        schlank|snello|Sie ist schlank.|Aspetto
        hübsch|carino|Das Kleid ist hübsch.|Aspetto
        hässlich|brutto|Das Gebäude ist hässlich.|Aspetto
        blond|biondo|Das Kind ist blond.|Aspetto
        dunkelhaarig|dai capelli scuri|Der Mann ist dunkelhaarig.|Aspetto
        lockig|riccio|Sie hat lockige Haare.|Aspetto
        glatt|liscio|Seine Haare sind glatt.|Aspetto
        der Bart|barba|Der Bart ist neu.|Aspetto
        das Aussehen|aspetto|Das Aussehen ist nicht wichtig.|Aspetto
        die Figur|figura|Sie hat eine gute Figur.|Aspetto
        das Gesicht|viso|Sein Gesicht ist freundlich.|Aspetto
        die Stimme|voce|Ihre Stimme ist ruhig.|Aspetto
        das Lächeln|sorriso|Sein Lächeln ist nett.|Aspetto
        der Blick|sguardo|Ihr Blick ist ernst.|Aspetto
        nett|simpatico|Der Nachbar ist nett.|Carattere
        unfreundlich|scortese|Der Kellner ist unfreundlich.|Carattere
        höflich|educato|Sei höflich zu den Gästen.|Carattere
        unhöflich|maleducato|Das war unhöflich.|Carattere
        ehrlich|onesto|Er ist immer ehrlich.|Carattere
        hilfsbereit|disponibile|Meine Kollegin ist hilfsbereit.|Carattere
        geduldig|paziente|Der Lehrer ist geduldig.|Carattere
        fleißig|diligente|Die Schülerin ist fleißig.|Carattere
        faul|pigro|Am Sonntag bin ich faul.|Carattere
        mutig|coraggioso|Der Feuerwehrmann ist mutig.|Carattere
        schüchtern|timido|Das Kind ist schüchtern.|Carattere
        verschlossen|chiuso|Er ist eher verschlossen.|Carattere
        ernst|serio|Der Chef ist ernst.|Carattere
        nervös|nervoso|Vor der Prüfung bin ich nervös.|Carattere
        gestresst|stressato|Nach der Arbeit bin ich gestresst.|Carattere
        entspannt|rilassato|Im Urlaub bin ich entspannt.|Carattere
        klug|intelligente|Sie ist sehr klug.|Carattere
        dumm|stupido|Die Frage ist dumm.|Carattere
        gemein|malvagio|Das war eine gemeine Tat.|Carattere
        ordentlich|ordinato|Sein Zimmer ist ordentlich.|Carattere
        unordentlich|disordinato|Der Schreibtisch ist unordentlich.|Carattere
        neugierig|curioso|Die Katze ist neugierig.|Carattere
        großzügig|generoso|Er ist sehr großzügig.|Carattere
        egoistisch|egoista|Das war egoistisch.|Carattere
        sympathisch|simpatico|Der neue Lehrer ist sympathisch.|Carattere
        die Laune|umore|Heute habe ich gute Laune.|Carattere
        die Stimmung|atmosfera|Die Stimmung ist gut.|Carattere
        der Charakter|carattere|Er hat einen ruhigen Charakter.|Carattere
        wütend|arrabbiato|Der Kunde ist wütend.|Emozioni 2
        ängstlich|ansioso|Das Kind ist ängstlich.|Emozioni 2
        verliebt|innamorato|Die beiden sind verliebt.|Emozioni 2
        einsam|solo|Am Abend fühle ich mich einsam.|Emozioni 2
        der Hass|odio|Hass ist ein starkes Gefühl.|Emozioni 2
        der Neid|invidia|Neid hilft niemandem.|Emozioni 2
        die Erleichterung|sollievo|Nach der Prüfung kam die Erleichterung.|Emozioni 2
        die Aufregung|agitazione|Vor dem Auftritt ist die Aufregung groß.|Emozioni 2
        die Langeweile|noia|Am Regenabend kommt Langeweile auf.|Emozioni 2
        das Gefühl|sentimento|Das Gefühl ist schwer zu beschreiben.|Emozioni 2
        der Basketball|pallacanestro|Basketball ist schnell.|Sport
        das Tennis|tennis|Tennis ist mein Hobby.|Sport
        der Volleyball|pallavolo|Volleyball spielen wir am Strand.|Sport
        das Joggen|corsa leggera|Joggen am Morgen ist schön.|Sport
        die Fitness|forma fisica|Fitness ist wichtig.|Sport
        das Training|allenamento|Das Training dauert eine Stunde.|Sport
        der Trainer|allenatore|Der Trainer ist streng.|Sport
        der Spieler|giocatore|Der Spieler ist verletzt.|Sport
        das Tor|porta|Das Tor ist offen.|Sport
        der Wettkampf|gara|Der Wettkampf beginnt morgen.|Sport
        die Meisterschaft|campionato|Die Meisterschaft ist zu Ende.|Sport
        die Medaille|medaglia|Sie hat eine Medaille gewonnen.|Sport
        der Ball|palla|Der Ball ist rund.|Sport
        der Schläger|racchetta|Der Schläger ist neu.|Sport
        die Halle|palestra|Die Halle ist voll.|Sport
        der Sportplatz|campo sportivo|Der Sportplatz ist neu.|Sport
        das Fitnessstudio|palestra|Das Fitnessstudio ist teuer.|Sport
        der Helm|casco|Der Helm ist wichtig.|Sport
        der Sieg|vittoria|Der Sieg war knapp.|Sport
        die Niederlage|sconfitta|Die Niederlage tut weh.|Sport
        der Rekord|record|Der Rekord ist alt.|Sport
        der Schiedsrichter|arbitro|Der Schiedsrichter pfeift.|Sport
        die Serie|serie|Die Serie ist neu.|Tempo libero 2
        das Festival|festival|Das Festival dauert drei Tage.|Tempo libero 2
        die Oper|opera|Die Oper ist ausverkauft.|Tempo libero 2
        das Musical|musical|Das Musical ist lustig.|Tempo libero 2
        die Disco|discoteca|Die Disco ist laut.|Tempo libero 2
        der Club|club|Der Club ist neu.|Tempo libero 2
        das Picknick|picnic|Das Picknick ist am Sonntag.|Tempo libero 2
        der Grill|griglia|Der Grill ist heiß.|Tempo libero 2
        das Camping|campeggio|Camping ist im Sommer schön.|Tempo libero 2
        das Angeln|pesca|Angeln ist ruhig.|Tempo libero 2
        die Gitarre|chitarra|Die Gitarre ist alt.|Tempo libero 2
        das Klavier|pianoforte|Das Klavier ist gestimmt.|Tempo libero 2
        das Schlagzeug|batteria|Das Schlagzeug ist laut.|Tempo libero 2
        die Flöte|flauto|Die Flöte klingt hell.|Tempo libero 2
        das Lied|canzone|Das Lied ist bekannt.|Tempo libero 2
        der Tanz|danza|Der Tanz macht Spaß.|Tempo libero 2
        die Eintrittskarte|biglietto d'ingresso|Die Eintrittskarte kostet zehn Euro.|Tempo libero 2
        die Ferien|vacanze|Die Ferien sind vorbei.|Tempo libero 2
        das Wochenende|fine settimana|Das Wochenende war schön.|Tempo libero 2

        der Topf|pentola|Der Topf steht auf dem Herd.|Cucina
        die Pfanne|padella|Die Pfanne ist heiß.|Cucina
        der Löffel|cucchiaio|Der Löffel fehlt.|Cucina
        die Gabel|forchetta|Die Gabel ist aus Metall.|Cucina
        das Messer|coltello|Das Messer ist scharf.|Cucina
        die Schüssel|ciotola|Die Schüssel ist groß.|Cucina
        das Schneidebrett|tagliere|Das Schneidebrett ist nass.|Cucina
        der Kochlöffel|mestolo|Der Kochlöffel hängt dort.|Cucina
        der Backofen|forno|Der Backofen ist an.|Cucina
        die Mikrowelle|microonde|Die Mikrowelle ist neu.|Cucina
        die Spüle|lavello|Die Spüle ist voll.|Cucina
        der Wasserhahn|rubinetto|Der Wasserhahn tropft.|Cucina
        die Serviette|tovagliolo|Die Serviette ist weiß.|Cucina
        die Tischdecke|tovaglia|Die Tischdecke ist blau.|Cucina
        die Zutat|ingrediente|Jede Zutat ist frisch.|Cucina
        die Portion|porzione|Die Portion ist groß.|Cucina
        der Geschmack|sapore|Der Geschmack ist mild.|Cucina
        der Geruch|odore|Der Geruch ist stark.|Cucina
        das Rindfleisch|carne di manzo|Das Rindfleisch ist teuer.|Cibo 2
        das Schweinefleisch|carne di maiale|Schweinefleisch esse ich selten.|Cibo 2
        das Hähnchen|pollo|Das Hähnchen ist im Ofen.|Cibo 2
        der Lachs|salmone|Der Lachs ist lecker.|Cibo 2
        der Thunfisch|tonno|Der Thunfisch ist in der Dose.|Cibo 2
        der Joghurt|yogurt|Der Joghurt ist kalt.|Cibo 2
        die Sahne|panna|Die Sahne ist süß.|Cibo 2
        der Kohl|cavolo|Der Kohl ist gesund.|Cibo 2
        die Karotte|carota|Die Karotte ist süß.|Cibo 2
        die Paprika|peperone|Die Paprika ist rot.|Cibo 2
        der Pilz|fungo|Der Pilz ist frisch.|Cibo 2
        der Spinat|spinaci|Der Spinat ist gesund.|Cibo 2
        der Brokkoli|broccoli|Der Brokkoli ist grün.|Cibo 2
        der Blumenkohl|cavolfiore|Der Blumenkohl ist weiß.|Cibo 2
        die Kirsche|ciliegia|Die Kirsche ist süß.|Cibo 2
        die Pflaume|prugna|Die Pflaume ist saftig.|Cibo 2
        die Melone|melone|Die Melone ist kühl.|Cibo 2
        die Ananas|ananas|Die Ananas ist exotisch.|Cibo 2
        die Himbeere|lampone|Die Himbeere ist rot.|Cibo 2
        die Blaubeere|mirtillo|Die Blaubeere ist blau.|Cibo 2
        die Nuss|noce|Die Nuss ist hart.|Cibo 2
        die Mandel|mandorla|Die Mandel ist klein.|Cibo 2
        der Honig|miele|Der Honig ist süß.|Cibo 2
        die Marmelade|marmellata|Die Marmelade ist hausgemacht.|Cibo 2
        der Senf|senape|Der Senf ist scharf.|Cibo 2
        der Ketchup|ketchup|Der Ketchup ist rot.|Cibo 2
        der Essig|aceto|Der Essig ist sauer.|Cibo 2
        das Gewürz|spezia|Das Gewürz riecht gut.|Cibo 2
        der Zimt|cannella|Der Zimt passt zum Kuchen.|Cibo 2
        das Sandwich|panino imbottito|Das Sandwich ist schnell gemacht.|Cibo 2
        der Hamburger|hamburger|Der Hamburger ist fettig.|Cibo 2
        die Pommes|patatine fritte|Die Pommes sind knusprig.|Cibo 2
        die Spaghetti|spaghetti|Die Spaghetti sind al dente.|Cibo 2
        die Lasagne|lasagne|Die Lasagne ist im Ofen.|Cibo 2
        das Omelett|omelette|Das Omelett ist fertig.|Cibo 2
        der Pfannkuchen|pancake|Der Pfannkuchen ist süß.|Cibo 2
        das Müsli|muesli|Das Müsli ist gesund.|Cibo 2
        der Kakao|cacao|Der Kakao ist süß.|Cibo 2
        das Mineralwasser|acqua minerale|Das Mineralwasser ist kalt.|Cibo 2
        die Cola|cola|Die Cola ist süß.|Cibo 2
        der Wochenmarkt|mercato settimanale|Der Wochenmarkt ist klein.|Negozi 2
        die Drogerie|drogeria|Die Drogerie ist nebenan.|Negozi 2
        der Friseur|parrucchiere|Der Friseur ist besetzt.|Negozi 2
        die Feuerwehr|vigili del fuoco|Die Feuerwehr kommt sofort.|Negozi 2
        der Optiker|ottico|Der Optiker repariert die Brille.|Negozi 2
        der Kiosk|chiosco|Der Kiosk verkauft Zeitungen.|Negozi 2
        die Reinigung|lavanderia|Die Reinigung ist geschlossen.|Negozi 2
        das Schuhgeschäft|negozio di scarpe|Das Schuhgeschäft ist klein.|Negozi 2
        der Spielzeugladen|negozio di giocattoli|Der Spielzeugladen ist bunt.|Negozi 2
        das Möbelgeschäft|negozio di mobili|Das Möbelgeschäft ist groß.|Negozi 2
        das Elektrogeschäft|negozio di elettronica|Das Elektrogeschäft ist neu.|Negozi 2
        die Hauptstadt|capitale|Die Hauptstadt ist groß.|Città 2
        der Vorort|periferia|Der Vorort ist grün.|Città 2
        die Fußgängerzone|zona pedonale|Die Fußgängerzone ist voll.|Città 2
        der Marktplatz|piazza del mercato|Der Marktplatz ist alt.|Città 2
        der Parkplatz|parcheggio|Der Parkplatz ist frei.|Città 2
        das Parkhaus|parcheggio coperto|Das Parkhaus ist voll.|Città 2
        die Garage|garage|Die Garage ist offen.|Città 2
        die Werkstatt|officina|Die Werkstatt ist zu.|Città 2
        der Tunnel|tunnel|Der Tunnel ist lang.|Città 2
        der Kanal|canale|Der Kanal ist tief.|Città 2
        die Wartehalle|sala d'attesa|Die Wartehalle ist warm.|Città 2
        die U-Bahn|metropolitana|Die U-Bahn fährt oft.|Trasporti
        das Motorrad|motocicletta|Das Motorrad ist laut.|Trasporti
        der Roller|scooter|Der Roller ist klein.|Trasporti
        der Lkw|camion|Der Lkw ist schwer.|Trasporti
        der Transporter|furgone|Der Transporter ist weiß.|Trasporti
        das Schiff|nave|Das Schiff ist groß.|Trasporti
        das Boot|barca|Das Boot ist klein.|Trasporti
        die Bahn|ferrovia|Die Bahn ist pünktlich.|Trasporti
        das Verkehrsmittel|mezzo di trasporto|Das Fahrrad ist ein Verkehrsmittel.|Trasporti
        die Monatskarte|abbonamento mensile|Die Monatskarte ist günstig.|Trasporti
        die Verkehrsregel|regola stradale|Die Verkehrsregel ist wichtig.|Trasporti
        der Stau|ingorgo|Der Stau ist lang.|Trasporti
        die Geschwindigkeit|velocità|Die Geschwindigkeit ist zu hoch.|Trasporti
        die Umleitung|deviazione|Die Umleitung ist ausgeschildert.|Trasporti
        die Baustelle|cantiere|Die Baustelle ist gesperrt.|Trasporti
        das Wohnzimmer|soggiorno|Das Wohnzimmer ist groß.|Casa 2
        die Terrasse|terrazza|Die Terrasse ist sonnig.|Casa 2
        das Schloss|serratura|Das Schloss ist kaputt.|Casa 2
        das Licht|luce|Das Licht ist hell.|Casa 2
        die Steckdose|presa|Die Steckdose ist frei.|Casa 2
        der Vorhang|tenda|Der Vorhang ist blau.|Casa 2
        die Kommode|cassettiera|Die Kommode ist alt.|Casa 2
        der Schreibtisch|scrivania|Der Schreibtisch ist ordentlich.|Casa 2
        das Waschbecken|lavandino|Das Waschbecken ist sauber.|Casa 2
        die Dusche|doccia|Die Dusche ist warm.|Casa 2
        die Badewanne|vasca da bagno|Die Badewanne ist voll.|Casa 2
        die Toilette|toilette|Die Toilette ist besetzt.|Casa 2
        die Bürste|spazzola|Die Bürste ist neu.|Casa 2
        der Kamm|pettine|Der Kamm liegt im Bad.|Casa 2
        der Föhn|asciugacapelli|Der Föhn ist kaputt.|Casa 2
        die Zahnpasta|dentifricio|Die Zahnpasta ist leer.|Casa 2
        das Holz|legno|Das Holz ist trocken.|Materiali
        das Metall|metallo|Das Metall ist kalt.|Materiali
        das Plastik|plastica|Das Plastik ist leicht.|Materiali
        der Stoff|stoffa|Der Stoff ist weich.|Materiali
        das Leder|pelle|Das Leder ist braun.|Materiali
        die Wolle|lana|Die Wolle ist warm.|Materiali
        die Baumwolle|cotone|Die Baumwolle ist leicht.|Materiali
        der Stein|pietra|Der Stein ist schwer.|Materiali
        der Beton|cemento|Der Beton ist grau.|Materiali
        das Eisen|ferro|Das Eisen ist schwer.|Materiali
        das Gold|oro|Das Gold ist teuer.|Materiali
        das Silber|argento|Das Silber glänzt.|Materiali
        das Kupfer|rame|Das Kupfer ist rot.|Materiali
        rund|rotondo|Der Tisch ist rund.|Forme
        eckig|spigoloso|Das Fenster ist eckig.|Forme
        gerade|dritto|Die Straße ist gerade.|Forme
        krumm|storto|Der Weg ist krumm.|Forme
        breit|largo|Die Straße ist breit.|Forme
        schmal|stretto|Der Gang ist schmal.|Forme
        hoch|alto|Der Turm ist hoch.|Forme
        tief|profondo|Der See ist tief.|Forme
        der Meter|metro|Der Raum ist fünf Meter lang.|Misure
        der Zentimeter|centimetro|Das Blatt ist zehn Zentimeter breit.|Misure
        der Kilometer|chilometro|Der Ort ist zehn Kilometer entfernt.|Misure
        das Gramm|grammo|Ich brauche hundert Gramm Käse.|Misure
        das Kilogramm|chilogrammo|Ein Kilogramm ist schwer.|Misure
        der Liter|litro|Ich kaufe einen Liter Milch.|Misure
        der Milliliter|millilitro|Das sind zehn Milliliter Öl.|Misure
        die Sekunde|secondo|Eine Sekunde reicht.|Tempo 2
        der Mittag|mezzogiorno|Am Mittag essen wir.|Tempo 2
        die Mitternacht|mezzanotte|Um Mitternacht ist es still.|Tempo 2
        übermorgen|dopodomani|Übermorgen kommt Besuch.|Tempo 2
        vorgestern|l'altro ieri|Vorgestern war es kalt.|Tempo 2
        später|più tardi|Später rufe ich an.|Tempo 2
        früher|prima|Früher wohnte ich in Wien.|Tempo 2
        damals|allora|Damals war ich jung.|Tempo 2
        endlich|finalmente|Endlich ist der Sommer da.|Tempo 2
        das Weihnachtsfest|Natale|Das Weihnachtsfest ist im Dezember.|Feste
        das Osterfest|Pasqua|Das Osterfest ist im Frühling.|Feste
        der Silvesterabend|San Silvestro|Am Silvesterabend feiern wir zu Hause.|Feste
        der Neujahrstag|Capodanno|Der Neujahrstag ist ein Feiertag.|Feste
        der Karneval|carnevale|Der Karneval ist bunt.|Feste
        die Kerze|candela|Die Kerze brennt.|Feste
        die Torte|torta|Die Torte ist süß.|Feste
        der Sekt|spumante|Der Sekt ist kalt.|Feste
        die Einladung|invito|Die Einladung ist schön.|Feste
        der Gottesdienst|funzione religiosa|Der Gottesdienst beginnt um zehn.|Feste
        der Weihnachtsbaum|albero di Natale|Der Weihnachtsbaum ist geschmückt.|Feste
        der Osterhase|coniglietto pasquale|Der Osterhase bringt Eier.|Feste
        das Feuerwerk|fuochi d'artificio|Das Feuerwerk ist laut.|Feste
        die Erde|terra|Die Erde ist rund.|Natura 2
        das Feuer|fuoco|Das Feuer ist heiß.|Natura 2
        der Schatten|ombra|Der Schatten ist lang.|Natura 2
        das Geräusch|rumore|Das Geräusch ist leise.|Natura 2
        der Lärm|rumore|Der Lärm ist unangenehm.|Natura 2
        die Stille|silenzio|Die Stille ist angenehm.|Natura 2
        die Biene|ape|Die Biene summt.|Animali 2
        die Fliege|mosca|Die Fliege ist lästig.|Animali 2
        die Ameise|formica|Die Ameise ist fleißig.|Animali 2
        die Schlange|serpente|Die Schlange ist gefährlich.|Animali 2
        der Fuchs|volpe|Der Fuchs ist schlau.|Animali 2
        der Bär|orso|Der Bär ist groß.|Animali 2
        der Wolf|lupo|Der Wolf lebt im Wald.|Animali 2
        der Hirsch|cervo|Der Hirsch ist schnell.|Animali 2
        das Reh|capriolo|Das Reh steht im Wald.|Animali 2
        die Ziege|capra|Die Ziege klettert gut.|Animali 2
        der Hahn|gallo|Der Hahn kräht früh.|Animali 2
        die Ente|anatra|Die Ente schwimmt.|Animali 2
        der Schwan|cigno|Der Schwan ist weiß.|Animali 2

    """.trimIndent()
}
