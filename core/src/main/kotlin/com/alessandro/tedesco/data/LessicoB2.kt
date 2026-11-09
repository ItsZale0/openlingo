package com.alessandro.tedesco.data

/**
 * Lessico curato B2: tedesco|italiano|esempio|tema.
 *
 * 605 parole nuove rispetto ad A1+A2+B1; con i livelli precedenti la copertura
 * arriva a 3.108 parole. Per B2 non esiste una Wortliste ufficiale Goethe: il
 * traguardo di 3.000 è una soglia prudente scelta dall'app per leggere testi
 * complessi. Le voci già presenti nei livelli precedenti sono state rimosse.
 */
object LessicoB2 {

    val grezzo: String = """
        die Zulassung|ammissione|Die Zulassung setzt einen Abschluss voraus.|Università
        die Immatrikulation|immatricolazione|Die Immatrikulation erfolgt online.|Università
        der Studiengang|corso di laurea|Der Studiengang dauert fünf Jahre.|Università
        die Vorlesung|lezione|Die Vorlesung wird aufgezeichnet.|Università
        die Hausarbeit|tesina|Die Hausarbeit umfasst zwanzig Seiten.|Università
        die Bachelorarbeit|tesi triennale|Die Bachelorarbeit ist abgegeben.|Università
        die Promotion|dottorato|Die Promotion dauert drei Jahre.|Università
        der Lehrstuhl|cattedra|Der Lehrstuhl ist neu besetzt.|Università
        der Dozent|docente|Der Dozent erklärt die Methode.|Università
        die Prüfungsordnung|regolamento d'esame|Die Prüfungsordnung ist streng.|Università
        die Leistung|prestazione|Die Leistung ist gestiegen.|Università
        die Bewertung|valutazione|Die Bewertung erfolgt anonym.|Università
        die Quelle|fonte|Die Quelle muss zitiert werden.|Università
        die These|tesi|Die These ist umstritten.|Università
        die Analyse|analisi|Die Analyse bestätigt den Verdacht.|Università
        die Methode|metodo|Die Methode ist etabliert.|Università
        die Stichprobe|campione|Die Stichprobe war zu klein.|Università
        das Ergebnis|risultato|Das Ergebnis ist signifikant.|Università
        der Beweis|prova|Der Beweis fehlt noch.|Università
        die Schlussfolgerung|conclusione|Die Schlussfolgerung ist vorsichtig formuliert.|Università
        die Hypothese|ipotesi|Die Hypothese wurde widerlegt.|Università
        der Ansatz|approccio|Der Ansatz ist vielversprechend.|Università
        die Forschung|ricerca|Die Forschung braucht Geld.|Università
        der Forscher|ricercatore|Der Forscher arbeitet im Labor.|Università
        die Erkenntnis|conoscenza|Die Erkenntnis ist neu.|Università
        der Fortschritt|progresso|Der Fortschritt ist messbar.|Università
        die Herausforderung|sfida|Die Herausforderung bleibt groß.|Università
        die Verantwortung|responsabilità|Die Verantwortung liegt beim Arzt.|Università
        die Ethik|etica|Die Ethik spielt eine zentrale Rolle.|Università
        die Richtlinie|linea guida|Die Richtlinie gilt europaweit.|Università
        die Vorschrift|norma|Die Vorschrift ist verbindlich.|Diritto
        das Gesetz|legge|Das Gesetz tritt im Januar in Kraft.|Diritto
        die Verordnung|regolamento|Die Verordnung wurde geändert.|Diritto
        der Paragraph|paragrafo|Der Paragraph regelt den Fall.|Diritto
        die Klage|denuncia|Die Klage wurde abgewiesen.|Diritto
        der Anwalt|avvocato|Der Anwalt prüft den Vertrag.|Diritto
        das Gericht|tribunale|Das Gericht entscheidet im Mai.|Diritto
        der Prozess|processo|Der Prozess dauert Monate.|Diritto
        die Strafe|pena|Die Strafe ist mild.|Diritto
        das Urteil|sentenza|Das Urteil ist rechtskräftig.|Diritto
        die Pflicht|dovere|Die Pflicht zur Dokumentation besteht.|Diritto
        das Recht|diritto|Jeder hat das Recht auf Information.|Diritto
        die Haftung|responsabilità legale|Die Haftung liegt beim Betreiber.|Diritto
        der Schaden|danno|Der Schaden wird ersetzt.|Diritto
        die Entschädigung|risarcimento|Die Entschädigung ist angemessen.|Diritto
        der Anspruch|diritto (pretesa)|Sie hat Anspruch auf Urlaub.|Diritto
        die Frist|termine|Die Frist läuft am Freitag ab.|Diritto
        der Antrag|istanza|Der Antrag wurde bewilligt.|Diritto
        die Genehmigung|autorizzazione|Die Genehmigung liegt vor.|Diritto
        die Auflage|condizione|Die Auflage ist streng.|Diritto
        die Aufsicht|vigilanza|Die Aufsicht kontrolliert die Station.|Diritto
        die Kontrolle|controllo|Die Kontrolle war angekündigt.|Diritto
        die Prüfung|verifica|Die Prüfung der Geräte ist jährlich.|Diritto
        der Mangel|difetto|Der Mangel wurde behoben.|Diritto
        die Reklamation|reclamo|Die Reklamation ist berechtigt.|Diritto
        der Widerruf|revoca|Der Widerruf ist innerhalb von 14 Tagen möglich.|Diritto
        die Klausel|clausola|Die Klausel ist ungültig.|Diritto
        der Vertragspartner|controparte contrattuale|Der Vertragspartner haftet mit.|Diritto
        die Vereinbarung|accordo|Die Vereinbarung ist schriftlich.|Diritto
        die Verpflichtung|obbligo|Die Verpflichtung endet im Dezember.|Diritto
        die Befugnis|competenza|Die Befugnis liegt beim Chefarzt.|Diritto
        die Zuständigkeit|competenza|Die Zuständigkeit ist geklärt.|Diritto
        die Ursache|causa|Die Ursache ist multifaktoriell.|Scienza
        der Auslöser|fattore scatenante|Der Auslöser war eine Infektion.|Scienza
        der Verlauf|decorso|Der Verlauf ist unauffällig.|Scienza
        die Prognose|prognosi|Die Prognose ist günstig.|Scienza
        die Symptomatik|sintomatologia|Die Symptomatik ist unspezifisch.|Scienza
        die Begleiterkrankung|comorbilità|Die Begleiterkrankung erschwert die Therapie.|Scienza
        die Ansteckung|contagio|Die Ansteckung erfolgt über Tröpfchen.|Scienza
        die Immunität|immunità|Die Immunität hält Jahre an.|Scienza
        der Erreger|agente patogeno|Der Erreger ist bekannt.|Scienza
        die Resistenz|resistenza|Die Resistenz nimmt zu.|Scienza
        die Übertragung|trasmissione|Die Übertragung ist selten.|Scienza
        die Vorbeugung|prevenzione|Vorbeugung ist besser als Therapie.|Scienza
        die Früherkennung|diagnosi precoce|Die Früherkennung rettet Leben.|Scienza
        die Zunahme|aumento|Die Zunahme der Fälle ist besorgniserregend.|Scienza
        die Abnahme|diminuzione|Die Abnahme der Zahlen ist erfreulich.|Scienza
        die Häufigkeit|frequenza|Die Häufigkeit liegt bei fünf Prozent.|Scienza
        der Anteil|quota|Der Anteil der Älteren steigt.|Scienza
        das Verhältnis|rapporto|Das Verhältnis von Kosten und Nutzen stimmt.|Scienza
        die Schätzung|stima|Die Schätzung ist vorsichtig.|Scienza
        der Durchschnitt|media|Der Durchschnitt liegt bei 30.|Scienza
        die Spanne|intervallo|Die Spanne ist groß.|Scienza
        der Trend|tendenza|Der Trend hält an.|Scienza
        der Rückgang|calo|Der Rückgang ist deutlich.|Scienza
        der Anstieg|aumento|Der Anstieg war erwartet.|Scienza
        die Folge|effetto|Die Folge ist eine längere Behandlung.|Scienza
        die Wechselwirkung|interazione|Die Wechselwirkung ist bekannt.|Scienza
        die Verträglichkeit|tollerabilità|Die Verträglichkeit ist gut.|Scienza
        die Wirksamkeit|efficacia|Die Wirksamkeit ist belegt.|Scienza
        die Sicherheit|affidabilità|Die Sicherheit des Systems ist geprüft.|Scienza
        die Qualitätssicherung|garanzia di qualità|Die Qualitätssicherung ist dokumentiert.|Scienza
        die Dokumentation|documentazione|Die Dokumentation ist vollständig.|Scienza
        die Aufzeichnung|registrazione|Die Aufzeichnung wird archiviert.|Scienza
        die Auswertung|valutazione|Die Auswertung läuft noch.|Scienza
        die Erhebung|rilevazione|Die Erhebung der Daten dauerte ein Jahr.|Scienza
        die Umfrage|sondaggio|Die Umfrage ergab ein klares Bild.|Scienza
        die Teilnahme|partecipazione|Die Teilnahme ist freiwillig.|Scienza
        die Rückmeldung|feedback|Die Rückmeldung war hilfreich.|Scienza
        die Auswirkung|ripercussione|Die Auswirkung auf die Kosten ist groß.|Scienza
        der Zusammenhang|correlazione|Der Zusammenhang ist statistisch schwach.|Scienza
        die Wahrscheinlichkeit|probabilità|Die Wahrscheinlichkeit ist hoch.|Scienza
        die Maßnahme|provvedimento|Die Maßnahme trat sofort in Kraft.|Società
        die Reform|riforma|Die Reform ist umstritten.|Società
        die Debatte|dibattito|Die Debatte wird emotional geführt.|Società
        die Mehrheit|maggioranza|Die Mehrheit stimmte zu.|Società
        die Minderheit|minoranza|Die Minderheit wurde gehört.|Società
        der Kompromiss|compromesso|Der Kompromiss war mühsam.|Società
        die Opposition|opposizione|Die Opposition fordert Änderungen.|Società
        die Koalition|coalizione|Die Koalition ist brüchig.|Società
        die Wahlbeteiligung|affluenza elettorale|Die Wahlbeteiligung war niedrig.|Società
        der Haushalt|bilancio|Der Haushalt ist ausgeglichen.|Società
        die Verschuldung|indebitamento|Die Verschuldung steigt.|Società
        die Subvention|sovvenzione|Die Subvention wird gestrichen.|Società
        die Förderung|incentivo|Die Förderung gilt für Familien.|Società
        die Gebühr|tassa|Die Gebühr wird jährlich erhoben.|Società
        die Verteilung|distribuzione|Die Verteilung ist ungerecht.|Società
        die Gerechtigkeit|giustizia|Gerechtigkeit ist ein hohes Gut.|Società
        die Gleichstellung|parità|Die Gleichstellung ist noch nicht erreicht.|Società
        die Integration|integrazione|Die Integration braucht Zeit.|Società
        die Migration|migrazione|Die Migration prägt die Gesellschaft.|Società
        die Vielfalt|diversità|Die Vielfalt ist eine Stärke.|Società
        die Zuwanderung|immigrazione|Die Zuwanderung nimmt zu.|Società
        der Zusammenhalt|coesione|Der Zusammenhalt ist wichtig.|Società
        die Krise|crisi|Die Krise hat viele getroffen.|Economia
        die Konjunktur|congiuntura|Die Konjunktur kühlt ab.|Economia
        die Rezession|recessione|Die Rezession dauert an.|Economia
        das Wachstum|crescita|Das Wachstum bleibt schwach.|Economia
        die Produktivität|produttività|Die Produktivität ist gestiegen.|Economia
        der Wettbewerb|concorrenza|Der Wettbewerb ist global.|Economia
        der Standort|sede|Der Standort ist attraktiv.|Economia
        die Investition|investimento|Die Investition wurde genehmigt.|Economia
        die Rentabilität|redditività|Die Rentabilität ist gering.|Economia
        das Kapital|capitale|Das Kapital fehlt.|Economia
        die Aktie|azione|Die Aktie ist gefallen.|Economia
        die Börse|borsa|Die Börse reagiert nervös.|Economia
        der Zinssatz|tasso d'interesse|Der Zinssatz bleibt stabil.|Economia
        die Inflation|inflazione|Die Inflation belastet die Haushalte.|Economia
        die Kaufkraft|potere d'acquisto|Die Kaufkraft sinkt.|Economia
        die Steuerlast|carico fiscale|Die Steuerlast ist hoch.|Economia
        der Umsatz|fatturato|Der Umsatz ist gestiegen.|Economia
        die Bilanz|bilancio|Die Bilanz fällt positiv aus.|Economia
        der Gewinn|profitto|Der Gewinn ist gesunken.|Economia
        der Verlust|perdita|Der Verlust ist verkraftbar.|Economia
        die Effizienz|efficienza|Die Effizienz muss steigen.|Economia
        die Nachhaltigkeit|sostenibilità|Die Nachhaltigkeit ist Teil der Strategie.|Economia
        die gesellschaftliche Verantwortung|responsabilità sociale|Die Firma übernimmt gesellschaftliche Verantwortung.|Economia
        die Transparenz|trasparenza|Transparenz schafft Vertrauen.|Economia
        die Korruption|corruzione|Korruption wird härter bestraft.|Economia
        der Interessenkonflikt|conflitto d'interessi|Der Interessenkonflikt ist offensichtlich.|Economia
        die Fusion|fusione|Die Fusion wurde genehmigt.|Economia
        die Übernahme|acquisizione|Die Übernahme kostet Millionen.|Economia
        die Strategie|strategia|Die Strategie muss überzeugend sein.|Economia
        das Ziel|obiettivo|Das Ziel ist ehrgeizig.|Economia
        die Priorität|priorità|Die Priorität liegt auf Sicherheit.|Economia
        die Ressource|risorsa|Die Ressourcen sind begrenzt.|Economia
        die Kapazität|capacità|Die Kapazität ist ausgeschöpft.|Economia
        der Engpass|collo di bottiglia|Der Engpass liegt in der Logistik.|Economia
        die Lieferkette|catena di approvvigionamento|Die Lieferkette ist unterbrochen.|Economia
        der Verbrauch|consumo|Der Verbrauch ist gesunken.|Ambiente B2
        der Ausstoß|emissione|Der Ausstoß muss reduziert werden.|Ambiente B2
        die Emission|emissione|Die Emissionen sind messbar.|Ambiente B2
        die Erwärmung|riscaldamento|Die Erwärmung schreitet voran.|Ambiente B2
        der Meeresspiegel|livello del mare|Der Meeresspiegel steigt.|Ambiente B2
        die Artenvielfalt|biodiversità|Die Artenvielfalt nimmt ab.|Ambiente B2
        das Ökosystem|ecosistema|Das Ökosystem ist empfindlich.|Ambiente B2
        die Belastung|carico|Die Belastung der Böden ist hoch.|Ambiente B2
        die Sanierung|risanamento|Die Sanierung ist teuer.|Ambiente B2
        die Entsorgung|smaltimento|Die Entsorgung ist geregelt.|Ambiente B2
        die Wiedergewinnung|recupero|Die Wiedergewinnung von Rohstoffen lohnt sich.|Ambiente B2
        die Energieeffizienz|efficienza energetica|Die Energieeffizienz ist entscheidend.|Ambiente B2
        die Anpassung|adattamento|Die Anpassung an das Klima ist nötig.|Ambiente B2
        der Schutz|protezione|Der Schutz der Daten ist zentral.|Ambiente B2
        die Gefährdung|pericolo|Die Gefährdung ist nicht ausgeschlossen.|Ambiente B2
        der Ausgleich|compensazione|Der Ausgleich der Emissionen ist umstritten.|Ambiente B2
        die Verbindlichkeit|vincolatività|Die Verbindlichkeit der Ziele ist unklar.|Ambiente B2
        die Verhandlung|negoziato|Die Verhandlung stockt.|Ambiente B2
        die Zusammenarbeit|cooperazione|Die Zusammenarbeit ist international.|Ambiente B2
        die Forderung|richiesta|Die Forderung ist berechtigt.|Ambiente B2
        die Aussicht|prospettiva|Die Aussicht auf Erfolg ist gering.|Ambiente B2
        die Grundlage|base|Die Grundlage ist eine solide Ausbildung.|Astratti B2
        der Rahmen|quadro|Der Rahmen ist gesetzlich vorgegeben.|Astratti B2
        der Bereich|settore|Der Bereich wächst.|Astratti B2
        der Umfang|portata|Der Umfang des Projekts ist groß.|Astratti B2
        das Ausmaß|entità|Das Ausmaß des Schadens ist unbekannt.|Astratti B2
        das Merkmal|caratteristica|Das Merkmal ist typisch.|Astratti B2
        die Eigenschaft|proprietà|Die Eigenschaft ist entscheidend.|Astratti B2
        die Voraussetzung|requisito|Die Voraussetzungen sind erfüllt.|Astratti B2
        die Bedingung|condizione|Die Bedingungen sind hart.|Astratti B2
        der Bestandteil|componente|Der Bestandteil ist unverzichtbar.|Astratti B2
        der Widerspruch|contraddizione|Der Widerspruch ist offensichtlich.|Astratti B2
        der Zweifel|dubbio|Der Zweifel bleibt.|Astratti B2
        die Gewissheit|certezza|Die Gewissheit fehlt noch.|Astratti B2
        die Annahme|ipotesi|Die Annahme ist plausibel.|Astratti B2
        die Vermutung|congettura|Die Vermutung bestätigt sich.|Astratti B2
        die Abschätzung|stima|Die Abschätzung ist schwierig.|Astratti B2
        die Tendenz|tendenza|Die Tendenz ist steigend.|Astratti B2
        die Entwicklung|sviluppo|Die Entwicklung ist rasant.|Astratti B2
        die Veränderung|cambiamento|Die Veränderung ist unvermeidlich.|Astratti B2
        der Übergang|passaggio|Der Übergang war schwierig.|Astratti B2
        der Umbruch|rottura|Der Umbruch veränderte alles.|Astratti B2
        die Wende|svolta|Die Wende kam 1989.|Astratti B2
        der Wandel|trasformazione|Der Wandel ist sichtbar.|Astratti B2
        die Bewältigung|gestione|Die Bewältigung der Krise dauerte Jahre.|Astratti B2
        die Auseinandersetzung|confronto|Die Auseinandersetzung war heftig.|Astratti B2
        die Bewegung|movimento|Die Bewegung wächst.|Astratti B2
        der Einfluss|influenza|Der Einfluss ist groß.|Astratti B2
        die Wirkung|effetto|Die Wirkung tritt schnell ein.|Astratti B2
        der Nutzen|utilità|Der Nutzen überwiegt.|Astratti B2
        der Aufwand|impegno|Der Aufwand ist hoch.|Astratti B2
        der Vorteil|vantaggio|Der Vorteil liegt im Preis.|Astratti B2
        der Nachteil|svantaggio|Der Nachteil ist die Zeit.|Astratti B2
        die Grenze|limite|Die Grenzen sind erreicht.|Astratti B2
        die Einschränkung|limitazione|Die Einschränkung gilt weiterhin.|Astratti B2
        der Spielraum|margine|Der Spielraum ist klein.|Astratti B2
        die Rechenschaft|rendiconto|Er wurde zur Rechenschaft gezogen.|Astratti B2
        die Glaubwürdigkeit|credibilità|Die Glaubwürdigkeit hat gelitten.|Astratti B2
        das Ansehen|reputazione|Das Ansehen der Institution ist hoch.|Astratti B2
        der Ruf|reputazione|Der Ruf ist ruiniert.|Astratti B2
        die Erwartung|aspettativa|Die Erwartungen wurden übertroffen.|Astratti B2
        die Enttäuschung|delusione|Die Enttäuschung war groß.|Astratti B2
        die Zufriedenheit|soddisfazione|Die Zufriedenheit der Kunden steigt.|Astratti B2
        die Bereitschaft|disponibilità|Die Bereitschaft zu helfen ist da.|Astratti B2
        die Nachfrage|domanda|Die Nachfrage übersteigt das Angebot.|Astratti B2
        die Bereitstellung|fornitura|Die Bereitstellung der Daten dauert.|Astratti B2
        die Inanspruchnahme|utilizzo|Die Inanspruchnahme ist kostenpflichtig.|Astratti B2
        die Verzögerung|ritardo|Die Verzögerung ist ärgerlich.|Astratti B2
        die Beschleunigung|accelerazione|Die Beschleunigung des Verfahrens ist nötig.|Astratti B2
        die Verlängerung|proroga|Die Verlängerung wurde beantragt.|Astratti B2
        die Kürzung|taglio|Die Kürzung trifft die Ärmsten.|Astratti B2
        die Erhöhung|aumento|Die Erhöhung der Preise ist spürbar.|Astratti B2
        die Senkung|riduzione|Die Senkung der Kosten ist das Ziel.|Astratti B2
        berücksichtigen|tenere conto|Wir müssen die Kosten berücksichtigen.|Verbi B2
        voraussetzen|presupporre|Die Stelle setzt Erfahrung voraus.|Verbi B2
        gewährleisten|garantire|Die Qualität muss gewährleistet sein.|Verbi B2
        sicherstellen|assicurare|Bitte stellen Sie die Dokumentation sicher.|Verbi B2
        beeinträchtigen|compromettere|Der Lärm beeinträchtigt die Gesundheit.|Verbi B2
        gefährden|mettere in pericolo|Rauchen gefährdet die Gesundheit.|Verbi B2
        vermeiden|evitare|Fehler lassen sich vermeiden.|Verbi B2
        verhindern|impedire|Das wurde erfolgreich verhindert.|Verbi B2
        ermöglichen|permettere|Die Technik ermöglicht neue Lösungen.|Verbi B2
        erfordern|richiedere|Der Fall erfordert schnelles Handeln.|Verbi B2
        erfüllen|soddisfare|Die Bedingungen sind erfüllt.|Verbi B2
        übertreffen|superare|Die Ergebnisse übertreffen die Erwartungen.|Verbi B2
        unterschreiten|restare sotto|Die Werte unterschreiten die Grenze.|Verbi B2
        überschreiten|superare|Die Kosten überschreiten das Budget.|Verbi B2
        zunehmen|crescere|Der Druck nimmt zu.|Verbi B2
        abnehmen|calare|Die Akzeptanz nimmt ab.|Verbi B2
        zurückgehen|diminuire|Die Zahl der Fälle geht zurück.|Verbi B2
        zunehmen an|aumentare di|Die Nachfrage nimmt an Bedeutung zu.|Verbi B2
        beitragen|contribuire|Jeder kann zum Erfolg beitragen.|Verbi B2
        sich beteiligen|partecipare|Die Bürger beteiligen sich an der Planung.|Verbi B2
        einbeziehen|coinvolgere|Wir beziehen die Familie ein.|Verbi B2
        vorsehen|prevedere|Der Plan sieht eine Pause vor.|Verbi B2
        umsetzen|attuare|Wir setzen die Maßnahmen um.|Verbi B2
        durchsetzen|imporre|Er konnte sich durchsetzen.|Verbi B2
        verzichten|rinunciare|Wir verzichten auf Plastik.|Verbi B2
        aufrechterhalten|mantenere|Die Ordnung muss aufrechterhalten werden.|Verbi B2
        erhalten|conservare|Die Frische bleibt erhalten.|Verbi B2
        sichern|mettere in sicurezza|Wir sichern die Daten.|Verbi B2
        gewinnen|guadagnare|Wir haben Zeit gewonnen.|Verbi B2
        verlieren|perdere|Er hat das Vertrauen verloren.|Verbi B2
        auslösen|scatenare|Der Bericht löste eine Debatte aus.|Verbi B2
        bewirken|provocare|Die Reform bewirkte wenig.|Verbi B2
        erzielen|ottenere|Wir erzielten gute Ergebnisse.|Verbi B2
        aufweisen|presentare|Die Studie weist Schwächen auf.|Verbi B2
        feststellen|constatare|Wir stellten einen Fehler fest.|Verbi B2
        annehmen|presumere|Ich nehme an, dass er kommt.|Verbi B2
        vermuten|supporre|Wir vermuten einen Zusammenhang.|Verbi B2
        schätzen|stimare|Die Kosten werden auf 500 Euro geschätzt.|Verbi B2
        abschätzen|valutare|Die Folgen sind schwer abzuschätzen.|Verbi B2
        belegen|documentare|Die Zahlen belegen den Trend.|Verbi B2
        bestätigen|confermare|Die Ergebnisse bestätigen die Annahme.|Verbi B2
        widerlegen|confutare|Die Studie widerlegt die These.|Verbi B2
        zustande kommen|realizzarsi|Der Vertrag kam nicht zustande.|Verbi B2
        in Frage stellen|mettere in discussione|Die Kritik stellt das Konzept in Frage.|Verbi B2
        zur Verfügung stellen|mettere a disposizione|Wir stellen die Daten zur Verfügung.|Verbi B2
        in Anspruch nehmen|usufruire|Er nimmt die Beratung in Anspruch.|Verbi B2
        Rücksicht nehmen|avere riguardo|Nehmen Sie Rücksicht auf die Patienten.|Verbi B2
        in Betracht ziehen|prendere in considerazione|Wir ziehen eine Alternative in Betracht.|Verbi B2
        entscheidend|decisivo|Die Zeit ist entscheidend.|Aggettivi B2
        wesentlich|essenziale|Der Unterschied ist wesentlich.|Aggettivi B2
        erheblich|considerevole|Die Kosten sind erheblich.|Aggettivi B2
        gering|scarso|Das Risiko ist gering.|Aggettivi B2
        umfangreich|ampio|Die Studie ist umfangreich.|Aggettivi B2
        zuverlässig|affidabile|Die Quelle ist zuverlässig.|Aggettivi B2
        nachvollziehbar|comprensibile|Die Entscheidung ist nachvollziehbar.|Aggettivi B2
        vertretbar|sostenibile|Die Lösung ist vertretbar.|Aggettivi B2
        angemessen|adeguato|Die Bezahlung ist angemessen.|Aggettivi B2
        unangemessen|inadeguato|Das Verhalten war unangemessen.|Aggettivi B2
        vorläufig|provvisorio|Das Ergebnis ist vorläufig.|Aggettivi B2
        endgültig|definitivo|Die Entscheidung ist endgültig.|Aggettivi B2
        langfristig|a lungo termine|Die Strategie ist langfristig.|Aggettivi B2
        kurzfristig|a breve termine|Die Lösung ist kurzfristig.|Aggettivi B2
        nachhaltig|sostenibile|Die Investition ist nachhaltig.|Aggettivi B2
        umstritten|controverso|Die Reform ist umstritten.|Aggettivi B2
        unumstritten|indiscusso|Der Nutzen ist unumstritten.|Aggettivi B2
        offensichtlich|evidente|Der Fehler ist offensichtlich.|Aggettivi B2
        vermutlich|presumibilmente|Der Wert ist vermutlich zu hoch.|Aggettivi B2
        überwiegend|prevalentemente|Die Reaktionen sind überwiegend positiv.|Aggettivi B2
        zusätzlich|aggiuntivo|Es gibt zusätzliche Kosten.|Aggettivi B2
        maßgeblich|determinante|Die Forschung ist maßgeblich beteiligt.|Aggettivi B2
        verpflichtend|obbligatorio|Die Teilnahme ist verpflichtend.|Aggettivi B2
        freiwillig|volontario|Die Teilnahme ist freiwillig.|Aggettivi B2
        zulässig|ammissibile|Der Antrag ist zulässig.|Aggettivi B2
        unzulässig|inammissibile|Die Klage ist unzulässig.|Aggettivi B2
        verbindlich|vincolante|Die Zusage ist verbindlich.|Aggettivi B2
        unverbindlich|non vincolante|Das Angebot ist unverbindlich.|Aggettivi B2
        vorbehaltlich|fatto salvo|Die Genehmigung gilt vorbehaltlich der Prüfung.|Aggettivi B2
        im Hinblick auf|in vista di|Im Hinblick auf die Kosten ist es sinnvoll.|Connettivi B2
        hinsichtlich|per quanto riguarda|Hinsichtlich der Termine gibt es Probleme.|Connettivi B2
        bezüglich|riguardo a|Bezüglich Ihrer Anfrage antworten wir heute.|Connettivi B2
        angesichts|in considerazione di|Angesichts der Lage ist Eile geboten.|Connettivi B2
        infolgedessen|di conseguenza|Es regnete, infolgedessen fiel das Fest aus.|Connettivi B2
        demzufolge|pertanto|Die Zahlen sanken, demzufolge sparen wir.|Connettivi B2
        dennoch|nondimeno|Es ist teuer, dennoch lohnt es sich.|Connettivi B2
        allerdings|tuttavia|Das ist richtig, allerdings teuer.|Connettivi B2
        zwar|è vero che|Zwar ist es teuer, aber gut.|Connettivi B2
        einerseits|da un lato|Einerseits ist es sicher, andererseits langsam.|Connettivi B2
        andererseits|dall'altro|Andererseits fehlt das Personal.|Connettivi B2
        folglich|quindi|Folglich müssen wir handeln.|Connettivi B2
        sofern|purché|Sofern es möglich ist, kommen wir.|Connettivi B2
        soweit|per quanto|Soweit ich weiß, ist er krank.|Connettivi B2
        sobald|non appena|Sobald die Ergebnisse da sind, rufe ich an.|Connettivi B2
        solange|finché|Solange es regnet, bleiben wir drinnen.|Connettivi B2
        falls|nel caso|Falls du kommst, ruf vorher an.|Connettivi B2
        ansonsten|altrimenti|Beeil dich, ansonsten verpassen wir den Zug.|Connettivi B2
        vielmehr|piuttosto|Es ist nicht teuer, vielmehr günstig.|Connettivi B2
        hingegen|invece|Die Kosten sinken, die Qualität hingegen steigt.|Connettivi B2
        das Parlament|parlamento|Das Parlament debattiert heute.|Politica B2
        der Bundesrat|consiglio federale|Der Bundesrat stimmt zu.|Politica B2
        der Koalitionsvertrag|contratto di coalizione|Der Koalitionsvertrag ist unterschrieben.|Politica B2
        das Mandat|mandato|Das Mandat dauert vier Jahre.|Politica B2
        die Fraktion|gruppo parlamentare|Die Fraktion ist gespalten.|Politica B2
        der Ausschuss|commissione|Der Ausschuss tagt morgen.|Politica B2
        die Resolution|risoluzione|Die Resolution wurde angenommen.|Politica B2
        die Sanktion|sanzione|Die Sanktion trifft das Land.|Politica B2
        die Diplomatie|diplomazia|Die Diplomatie sucht eine Lösung.|Politica B2
        der Diplomat|diplomatico|Der Diplomat wurde abberufen.|Politica B2
        die Botschaft|ambasciata|Die Botschaft ist geschlossen.|Politica B2
        das Konsulat|consolato|Das Konsulat öffnet um neun.|Politica B2
        die Oberschicht|classe alta|Die Oberschicht ist klein.|Società B2
        die Mittelschicht|classe media|Die Mittelschicht trägt die Last.|Società B2
        die Unterschicht|classe bassa|Die Unterschicht ist benachteiligt.|Società B2
        die Randgruppe|gruppo emarginato|Die Randgruppe wird übersehen.|Società B2
        die Inklusion|inclusione|Die Inklusion ist ein Ziel.|Società B2
        die Exklusion|esclusione|Die Exklusion nimmt zu.|Società B2
        die Diskriminierung|discriminazione|Die Diskriminierung ist verboten.|Società B2
        der Rassismus|razzismo|Der Rassismus ist ein Problem.|Società B2
        der Sexismus|sessismo|Der Sexismus ist noch verbreitet.|Società B2
        das Vorurteil|pregiudizio|Das Vorurteil hält sich hartnäckig.|Società B2
        das Stereotyp|stereotipo|Das Stereotyp stimmt nicht.|Società B2
        die Identität|identità|Die Identität ist vielfältig.|Società B2
        die Diversität|diversità|Die Diversität bereichert die Firma.|Società B2
        die Pluralität|pluralità|Die Pluralität ist normal.|Società B2
        die Segregation|segregazione|Die Segregation wächst.|Società B2
        die Assimilation|assimilazione|Die Assimilation dauert Generationen.|Società B2
        die Sozialisation|socializzazione|Die Sozialisation beginnt in der Familie.|Società B2
        die Generation|generazione|Die Generation ist optimistisch.|Società B2
        der Generationenkonflikt|conflitto generazionale|Der Generationenkonflikt ist sichtbar.|Società B2
        die Demografie|demografia|Die Demografie verändert das Land.|Società B2
        die Überalterung|invecchiamento|Die Überalterung ist eine Herausforderung.|Società B2
        der Fachkräftemangel|penuria di personale qualificato|Der Fachkräftemangel ist groß.|Società B2
        die Abwanderung|emigrazione|Die Abwanderung schwächt die Region.|Società B2
        der Migrationshintergrund|background migratorio|Er hat einen Migrationshintergrund.|Società B2
        die Einbürgerung|naturalizzazione|Die Einbürgerung ist beantragt.|Società B2
        das Asyl|asilo|Das Asyl ist ein Grundrecht.|Società B2
        der Asylantrag|domanda d'asilo|Der Asylantrag wird geprüft.|Società B2
        der Flüchtling|rifugiato|Der Flüchtling lernt Deutsch.|Società B2
        die Unterbringung|alloggio|Die Unterbringung ist provisorisch.|Società B2
        die Abschiebung|espulsione|Die Abschiebung ist umstritten.|Società B2
        das Bleiberecht|diritto di restare|Das Bleiberecht ist befristet.|Società B2
        die Globalisierung|globalizzazione|Die Globalisierung verändert den Handel.|Economia B2
        der Welthandel|commercio mondiale|Der Welthandel wächst langsam.|Economia B2
        der Freihandel|libero scambio|Der Freihandel ist umstritten.|Economia B2
        der Protektionismus|protezionismo|Der Protektionismus nimmt zu.|Economia B2
        die Währungsunion|unione monetaria|Die Währungsunion ist stabil.|Economia B2
        der Binnenmarkt|mercato interno|Der Binnenmarkt ist groß.|Economia B2
        die Deregulierung|deregolamentazione|Die Deregulierung ist geplant.|Economia B2
        die Privatisierung|privatizzazione|Die Privatisierung war umstritten.|Economia B2
        die Liberalisierung|liberalizzazione|Die Liberalisierung öffnet den Markt.|Economia B2
        die Regulierung|regolamentazione|Die Regulierung ist nötig.|Economia B2
        das Monopol|monopolio|Das Monopol ist verboten.|Economia B2
        das Kartell|cartello|Das Kartell wurde zerschlagen.|Economia B2
        die Insolvenz|insolvenza|Die Insolvenz ist angemeldet.|Economia B2
        die Kooperation|cooperazione|Die Kooperation ist erfolgreich.|Economia B2
        die Wertschöpfung|creazione di valore|Die Wertschöpfung steigt.|Economia B2
        die Logistik|logistica|Die Logistik ist effizient.|Economia B2
        die Automatisierung|automazione|Die Automatisierung kostet Arbeitsplätze.|Economia B2
        die Innovation|innovazione|Die Innovation braucht Zeit.|Economia B2
        die Wettbewerbsfähigkeit|competitività|Die Wettbewerbsfähigkeit ist gefährdet.|Economia B2
        die Deflation|deflazione|Die Deflation ist gefährlich.|Economia B2
        der Aufschwung|ripresa|Der Aufschwung kommt langsam.|Economia B2
        der Abschwung|rallentamento|Der Abschwung ist spürbar.|Economia B2
        die Spekulation|speculazione|Die Spekulation trieb die Preise hoch.|Economia B2
        die Dividende|dividendo|Die Dividende ist niedrig.|Economia B2
        die Anleihe|obbligazione|Die Anleihe ist sicher.|Economia B2
        der Fonds|fondo|Der Fonds ist breit gestreut.|Economia B2
        die Hypothek|ipoteca|Die Hypothek läuft 20 Jahre.|Economia B2
        die Finanzkrise|crisi finanziaria|Die Finanzkrise traf alle.|Economia B2
        die Rechtsprechung|giurisprudenza|Die Rechtsprechung ist einheitlich.|Diritto B2
        die Gesetzgebung|legislazione|Die Gesetzgebung ist langsam.|Diritto B2
        der Gesetzgeber|legislatore|Der Gesetzgeber muss handeln.|Diritto B2
        der Entwurf|bozza|Der Entwurf liegt vor.|Diritto B2
        die Anhörung|audizione|Die Anhörung beginnt um zehn.|Diritto B2
        die Stellungnahme|parere|Die Stellungnahme ist kritisch.|Diritto B2
        der Kläger|attore|Der Kläger hat recht.|Diritto B2
        der Beklagte|convenuto|Der Beklagte schweigt.|Diritto B2
        die Instanz|istanza|Die Instanz hat entschieden.|Diritto B2
        die Berufung|appello|Die Berufung ist möglich.|Diritto B2
        die Revision|revisione|Die Revision wurde abgelehnt.|Diritto B2
        die Vollstreckung|esecuzione|Die Vollstreckung beginnt morgen.|Diritto B2
        die Verjährung|prescrizione|Die Verjährung ist eingetreten.|Diritto B2
        die Freiheitsstrafe|pena detentiva|Die Freiheitsstrafe beträgt drei Jahre.|Diritto B2
        die Geldstrafe|multa|Die Geldstrafe ist hoch.|Diritto B2
        die Bewährung|sospensione condizionale|Er bekam Bewährung.|Diritto B2
        die Unschuldsvermutung|presunzione di innocenza|Die Unschuldsvermutung gilt.|Diritto B2
        das Grundrecht|diritto fondamentale|Das Grundrecht ist geschützt.|Diritto B2
        die Menschenwürde|dignità umana|Die Menschenwürde ist unantastbar.|Diritto B2
        die Verhältnismäßigkeit|proporzionalità|Die Verhältnismäßigkeit wird geprüft.|Diritto B2
        die Rechtsstaatlichkeit|stato di diritto|Die Rechtsstaatlichkeit ist gefährdet.|Diritto B2
        die Gewaltenteilung|separazione dei poteri|Die Gewaltenteilung ist wichtig.|Diritto B2
        die Empirie|empiria|Die Empirie stützt die These.|Scienza B2
        die Antithese|antitesi|Die Antithese ist schwach.|Scienza B2
        die Synthese|sintesi|Die Synthese fehlt noch.|Scienza B2
        das Paradigma|paradigma|Das Paradigma hat sich verändert.|Scienza B2
        die Methodik|metodologia|Die Methodik ist umstritten.|Scienza B2
        die Validität|validità|Die Validität ist fraglich.|Scienza B2
        die Reliabilität|affidabilità|Die Reliabilität ist hoch.|Scienza B2
        die Signifikanz|significatività|Die Signifikanz ist gering.|Scienza B2
        die Korrelation|correlazione|Die Korrelation ist positiv.|Scienza B2
        die Kausalität|causalità|Die Kausalität ist nicht bewiesen.|Scienza B2
        die Kontrollgruppe|gruppo di controllo|Die Kontrollgruppe erhielt kein Medikament.|Scienza B2
        das Experiment|esperimento|Das Experiment war erfolgreich.|Scienza B2
        die Interpretation|interpretazione|Die Interpretation ist subjektiv.|Scienza B2
        der Kenntnisstand|stato delle conoscenze|Der Kenntnisstand ist begrenzt.|Scienza B2
        der Forschungsstand|stato della ricerca|Der Forschungsstand ist lückenhaft.|Scienza B2
        die Zitation|cittazione|Die Zitation fehlt.|Scienza B2
        das Plagiat|plagio|Das Plagiat wurde entdeckt.|Scienza B2
        die Publikation|pubblicazione|Die Publikation ist frei zugänglich.|Scienza B2
        die Fachzeitschrift|rivista scientifica|Die Fachzeitschrift ist renommiert.|Scienza B2
        die Reproduzierbarkeit|riproducibilità|Die Reproduzierbarkeit ist gering.|Scienza B2
        der Klimaschutz|tutela del clima|Der Klimaschutz ist dringend.|Ambiente B2
        die Klimapolitik|politica climatica|Die Klimapolitik ist umstritten.|Ambiente B2
        das Klimaabkommen|accordo sul clima|Das Klimaabkommen ist verbindlich.|Ambiente B2
        die CO2-Bilanz|bilancio di CO2|Die CO2-Bilanz ist schlecht.|Ambiente B2
        die Dekarbonisierung|decarbonizzazione|Die Dekarbonisierung dauert Jahrzehnte.|Ambiente B2
        die Energiewende|transizione energetica|Die Energiewende ist beschlossen.|Ambiente B2
        die Atomkraft|energia nucleare|Die Atomkraft ist umstritten.|Ambiente B2
        das Atomkraftwerk|centrale nucleare|Das Atomkraftwerk wird abgeschaltet.|Ambiente B2
        die Kohle|carbone|Die Kohle ist billig.|Ambiente B2
        das Erdgas|gas naturale|Das Erdgas ist knapp.|Ambiente B2
        das Erdöl|petrolio|Das Erdöl ist teuer.|Ambiente B2
        die Solarzelle|cella solare|Die Solarzelle ist effizienter geworden.|Ambiente B2
        das Windrad|turbina eolica|Das Windrad dreht sich.|Ambiente B2
        die Wasserkraft|energia idrica|Die Wasserkraft ist erneuerbar.|Ambiente B2
        die Biomasse|biomassa|Die Biomasse ist begrenzt.|Ambiente B2
        die Geothermie|geotermia|Die Geothermie wird ausgebaut.|Ambiente B2
        die Elektromobilität|mobilità elettrica|Die Elektromobilität wächst.|Ambiente B2
        das Elektroauto|auto elettrica|Das Elektroauto ist leise.|Ambiente B2
        der Wasserstoff|idrogeno|Der Wasserstoff ist teuer.|Ambiente B2
        die Brennstoffzelle|cella a combustibile|Die Brennstoffzelle ist effizient.|Ambiente B2
        die Kreislaufwirtschaft|economia circolare|Die Kreislaufwirtschaft ist ein Ziel.|Ambiente B2
        die Wegwerfgesellschaft|società dell'usa e getta|Die Wegwerfgesellschaft produziert Müll.|Ambiente B2
        die Müllvermeidung|riduzione dei rifiuti|Die Müllvermeidung beginnt beim Einkauf.|Ambiente B2
        das Pfandsystem|sistema di deposito|Das Pfandsystem funktioniert gut.|Ambiente B2
        das Mikroplastik|microplastica|Das Mikroplastik ist überall.|Ambiente B2
        der Feinstaub|polveri sottili|Der Feinstaub belastet die Luft.|Ambiente B2
        die Luftqualität|qualità dell'aria|Die Luftqualität ist schlecht.|Ambiente B2
        die Medienlandschaft|panorama mediatico|Die Medienlandschaft ist vielfältig.|Media B2
        die Medienkompetenz|competenza mediatica|Die Medienkompetenz ist wichtig.|Media B2
        die Meinungsbildung|formazione dell'opinione|Die Meinungsbildung ist ein Prozess.|Media B2
        die Desinformation|disinformazione|Die Desinformation verbreitet sich schnell.|Media B2
        die Propaganda|propaganda|Die Propaganda ist offensichtlich.|Media B2
        die Zensur|censura|Die Zensur ist verboten.|Media B2
        der Journalismus|giornalismo|Der Journalismus ist unabhängig.|Media B2
        der Journalist|giornalista|Der Journalist recherchiert.|Media B2
        die Redaktion|redazione|Die Redaktion entscheidet.|Media B2
        der Chefredakteur|caporedattore|Der Chefredakteur tritt zurück.|Media B2
        die Schlagzeile|titolo di giornale|Die Schlagzeile ist reißerisch.|Media B2
        die Berichterstattung|copertura mediatica|Die Berichterstattung war einseitig.|Media B2
        die Nachrichtenagentur|agenzia di stampa|Die Nachrichtenagentur meldet den Unfall.|Media B2
        die Objektivität|obiettività|Die Objektivität ist gefragt.|Media B2
        die Subjektivität|soggettività|Die Subjektivität ist unvermeidlich.|Media B2
        die Manipulation|manipolazione|Die Manipulation ist schwer zu erkennen.|Media B2
        der Algorithmus|algoritmo|Der Algorithmus empfiehlt Videos.|Media B2
        die Filterblase|bolla di filtraggio|Die Filterblase verengt die Sicht.|Media B2
        die Echokammer|camera d'eco|Die Echokammer verstärkt Meinungen.|Media B2
        die Reichweite|portata|Die Reichweite ist enorm.|Media B2
        die Zielgruppe|pubblico target|Die Zielgruppe ist jung.|Media B2
        der Influencer|influencer|Der Influencer wirbt für Produkte.|Media B2
        der Podcast|podcast|Der Podcast ist beliebt.|Media B2
        das Streaming|streaming|Das Streaming ist bequem.|Media B2
        das Abonnement|abbonamento|Das Abonnement ist teuer.|Media B2
        die Paywall|paywall|Die Paywall sperrt den Artikel.|Media B2
        die Hochkultur|alta cultura|Die Hochkultur ist gefördert.|Cultura B2
        die Popkultur|cultura pop|Die Popkultur prägt die Jugend.|Cultura B2
        die Subkultur|sottocultura|Die Subkultur ist klein.|Cultura B2
        die Performance|performance|Die Performance war beeindruckend.|Cultura B2
        die Installation|installazione|Die Installation ist interaktiv.|Cultura B2
        die Vernissage|vernissage|Die Vernissage ist am Freitag.|Cultura B2
        die Premiere|prima|Die Premiere war ausverkauft.|Cultura B2
        die Inszenierung|messa in scena|Die Inszenierung ist modern.|Cultura B2
        die Rezension|recensione|Die Rezension ist positiv.|Cultura B2
        der Kanon|canone|Der Kanon ist umstritten.|Cultura B2
        die Epoche|epoca|Die Epoche dauerte hundert Jahre.|Cultura B2
        die Avantgarde|avanguardia|Die Avantgarde war radikal.|Cultura B2
        das Kulturerbe|patrimonio culturale|Das Kulturerbe ist geschützt.|Cultura B2
        die Ausgrabung|scavo archeologico|Die Ausgrabung ist abgeschlossen.|Cultura B2
        das Artefakt|artefatto|Das Artefakt ist wertvoll.|Cultura B2
        der Kurator|curatore|Der Kurator führt durch die Ausstellung.|Cultura B2
        die Psyche|psiche|Die Psyche ist verletzlich.|Psicologia B2
        die Kognition|cognizione|Die Kognition nimmt im Alter ab.|Psicologia B2
        die Wahrnehmung|percezione|Die Wahrnehmung ist subjektiv.|Psicologia B2
        die Aufmerksamkeit|attenzione|Die Aufmerksamkeit ist begrenzt.|Psicologia B2
        das Gedächtnis|memoria|Das Gedächtnis lässt nach.|Psicologia B2
        das Unterbewusstsein|subconscio|Das Unterbewusstsein beeinflusst uns.|Psicologia B2
        das Bewusstsein|coscienza|Das Bewusstsein ist ein Rätsel.|Psicologia B2
        die Persönlichkeit|personalità|Die Persönlichkeit ist stabil.|Psicologia B2
        das Selbstbewusstsein|autostima|Das Selbstbewusstsein ist gewachsen.|Psicologia B2
        das Selbstwertgefühl|senso di autostima|Das Selbstwertgefühl ist niedrig.|Psicologia B2
        die Resilienz|resilienza|Die Resilienz hilft in Krisen.|Psicologia B2
        der Burnout|burnout|Der Burnout ist weit verbreitet.|Psicologia B2
        die Depression|depressione|Die Depression ist behandelbar.|Psicologia B2
        die Angststörung|disturbo d'ansia|Die Angststörung ist häufig.|Psicologia B2
        die Psychotherapie|psicoterapia|Die Psychotherapie dauert lange.|Psicologia B2
        der Psychologe|psicologo|Der Psychologe hört zu.|Psicologia B2
        der Psychiater|psichiatra|Der Psychiater verschreibt Medikamente.|Psicologia B2
        die Arbeitswelt|mondo del lavoro|Die Arbeitswelt verändert sich.|Lavoro B2
        die Work-Life-Balance|equilibrio lavoro-vita|Die Work-Life-Balance ist wichtig.|Lavoro B2
        die Vereinbarkeit|conciliazione|Die Vereinbarkeit von Familie und Beruf ist schwer.|Lavoro B2
        die Flexibilisierung|flessibilizzazione|Die Flexibilisierung nimmt zu.|Lavoro B2
        die Mobilität|mobilità|Die Mobilität wird erwartet.|Lavoro B2
        das Homeoffice|telelavoro|Das Homeoffice ist beliebt.|Lavoro B2
        die Telearbeit|telelavoro|Die Telearbeit ist geregelt.|Lavoro B2
        die Präsenzpflicht|obbligo di presenza|Die Präsenzpflicht wurde gelockert.|Lavoro B2
        die Zeiterfassung|registrazione del tempo|Die Zeiterfassung ist Pflicht.|Lavoro B2
        die Vertrauensarbeitszeit|orario fiduciario|Die Vertrauensarbeitszeit ist flexibel.|Lavoro B2
        die Leistungsgesellschaft|società della prestazione|Die Leistungsgesellschaft fordert viel.|Lavoro B2
        der Leistungsdruck|pressione alla prestazione|Der Leistungsdruck ist hoch.|Lavoro B2
        die Überforderung|sovraccarico|Die Überforderung führt zu Fehlern.|Lavoro B2
        die Unterforderung|sottocarico|Die Unterforderung macht unzufrieden.|Lavoro B2
        die Aufstiegschancen|possibilità di carriera|Die Aufstiegschancen sind gut.|Lavoro B2
        die Personalentwicklung|sviluppo del personale|Die Personalentwicklung ist Aufgabe der Firma.|Lavoro B2
        die Unternehmenskultur|cultura aziendale|Die Unternehmenskultur ist offen.|Lavoro B2
        das Betriebsklima|clima aziendale|Das Betriebsklima ist gut.|Lavoro B2
        die Mitarbeiterzufriedenheit|soddisfazione dei dipendenti|Die Mitarbeiterzufriedenheit ist gestiegen.|Lavoro B2
        die Fluktuation|turnover|Die Fluktuation ist hoch.|Lavoro B2
        sich etablieren|affermarsi|Die Firma will sich etablieren.|Verbi B2
        sich manifestieren|manifestarsi|Die Krise manifestiert sich deutlich.|Verbi B2
        sich differenzieren|differenziarsi|Die Angebote differenzieren sich kaum.|Verbi B2
        sich integrieren|integrarsi|Die Kinder integrieren sich schnell.|Verbi B2
        sich assimilieren|assimilarsi|Die Gruppe assimilierte sich langsam.|Verbi B2
        sich polarisieren|polarizzarsi|Die Gesellschaft polarisiert sich.|Verbi B2
        sich zuspitzen|aggravarsi|Der Konflikt spitzt sich zu.|Verbi B2
        sich verfestigen|consolidarsi|Die Strukturen verfestigen sich.|Verbi B2
        sich lockern|allentarsi|Die Regeln lockern sich.|Verbi B2
        sich wandeln|trasformarsi|Die Stadt wandelt sich.|Verbi B2
        sich vollziehen|compirsi|Der Wandel vollzieht sich langsam.|Verbi B2
        sich abzeichnen|profilarsi|Ein Kompromiss zeichnet sich ab.|Verbi B2
        sich herausstellen|rivelarsi|Der Fehler stellte sich heraus.|Verbi B2
        sich erweisen|dimostrarsi|Die Maßnahme erwies sich als nützlich.|Verbi B2
        sich bewähren|dimostrare il proprio valore|Das System hat sich bewährt.|Verbi B2
        sich verständigen|intendersi|Wir müssen uns verständigen.|Verbi B2
        sich auseinandersetzen|confrontarsi|Wir müssen uns mit dem Thema auseinandersetzen.|Verbi B2
        sich distanzieren|prendere le distanze|Er distanzierte sich von der Aussage.|Verbi B2
        sich solidarisieren|solidarizzare|Die Kollegen solidarisierten sich.|Verbi B2
        sich einbringen|contribuire|Sie will sich einbringen.|Verbi B2
        sich verpflichten|impegnarsi|Die Firma verpflichtet sich zu mehr Umweltschutz.|Verbi B2
        sich weigern|rifiutarsi|Er weigert sich zu zahlen.|Verbi B2
        sich erkundigen|informarsi|Ich erkundige mich nach dem Termin.|Verbi B2
        sich orientieren|orientarsi|Die Schüler sollen sich orientieren.|Verbi B2
        sich spezialisieren|specializzarsi|Die Klinik spezialisiert sich auf Kinder.|Verbi B2
        sich qualifizieren|qualificarsi|Sie will sich weiter qualifizieren.|Verbi B2
        sich fortbilden|aggiornarsi|Die Mitarbeiter sollen sich fortbilden.|Verbi B2
        sich weiterbilden|proseguire la formazione|Er will sich weiterbilden.|Verbi B2
        sich niederlassen|stabilirsi|Die Familie will sich hier niederlassen.|Verbi B2
        sich ausbreiten|diffondersi|Die Krankheit breitet sich aus.|Verbi B2
        sich verbreiten|diffondersi|Die Nachricht verbreitet sich schnell.|Verbi B2
        sich zurückziehen|ritirarsi|Der Politiker zog sich zurück.|Verbi B2
        sich abgrenzen|distinguersi|Sie will sich von der Gruppe abgrenzen.|Verbi B2
        sich abfinden|rassegnarsi|Er hat sich mit der Lage abgefunden.|Verbi B2
        sich arrangieren|arrangiarsi|Wir werden uns arrangieren.|Verbi B2
        sich überfordern|sovraccaricarsi|Man soll sich nicht überfordern.|Verbi B2
        sich unterschätzen|sottovalutarsi|Sie unterschätzt sich oft.|Verbi B2
        resilient|resiliente|Die Gesellschaft ist resilient.|Aggettivi B2
        vulnerabel|vulnerabile|Die Gruppe ist besonders vulnerabel.|Aggettivi B2
        prekär|precario|Die Lage ist prekär.|Aggettivi B2
        marginal|marginale|Das Problem ist marginal.|Aggettivi B2
        relevant|rilevante|Der Unterschied ist relevant.|Aggettivi B2
        irrelevant|irrilevante|Die Frage ist irrelevant.|Aggettivi B2
        signifikant|significativo|Der Effekt ist signifikant.|Aggettivi B2
        valide|valido|Das Ergebnis ist valide.|Aggettivi B2
        reliabel|affidabile|Die Messung ist reliabel.|Aggettivi B2
        objektiv|oggettivo|Die Bewertung ist objektiv.|Aggettivi B2
        subjektiv|soggettivo|Die Wahrnehmung ist subjektiv.|Aggettivi B2
        transparent|trasparente|Der Prozess ist transparent.|Aggettivi B2
        intransparent|opaco|Die Entscheidung ist intransparent.|Aggettivi B2
        effizient|efficiente|Die Methode ist effizient.|Aggettivi B2
        ineffizient|inefficiente|Das System ist ineffizient.|Aggettivi B2
        effektiv|efficace|Die Maßnahme ist effektiv.|Aggettivi B2
        ineffektiv|inefficace|Der Ansatz ist ineffektiv.|Aggettivi B2
        inflexibel|inflessibile|Der Plan ist inflexibel.|Aggettivi B2
        mobil|mobile|Die Gesellschaft ist mobil.|Aggettivi B2
        immobil|immobile|Der Markt ist immobil.|Aggettivi B2
        stabil|stabile|Die Währung ist stabil.|Aggettivi B2
        instabil|instabile|Die Lage ist instabil.|Aggettivi B2
        volatil|volatile|Der Markt ist volatil.|Aggettivi B2
        konstant|costante|Die Temperatur ist konstant.|Aggettivi B2
        variabel|variabile|Die Kosten sind variabel.|Aggettivi B2
        dynamisch|dinamico|Die Entwicklung ist dynamisch.|Aggettivi B2
        statisch|statico|Das Modell ist statisch.|Aggettivi B2
        progressiv|progressista|Die Partei ist progressiv.|Aggettivi B2
        konservativ|conservatore|Die Wähler sind konservativ.|Aggettivi B2
        liberal|liberale|Die Wirtschaft ist liberal.|Aggettivi B2
        extremistisch|estremista|Die Gruppe ist extremistisch.|Aggettivi B2
        radikal|radicale|Die Reform ist radikal.|Aggettivi B2
        gemäßigt|moderato|Die Opposition ist gemäßigt.|Aggettivi B2
        bürokratisch|burocratico|Der Weg ist bürokratisch.|Aggettivi B2
        hierarchisch|gerarchico|Die Struktur ist hierarchisch.|Aggettivi B2
        partizipativ|partecipativo|Der Prozess ist partizipativ.|Aggettivi B2
        inklusiv|inclusivo|Das Angebot ist inklusiv.|Aggettivi B2
        exklusiv|esclusivo|Der Club ist exklusiv.|Aggettivi B2
        diskriminierend|discriminatorio|Die Regel ist diskriminierend.|Aggettivi B2
        gleichberechtigt|paritario|Die Partner sind gleichberechtigt.|Aggettivi B2
        benachteiligt|svantaggiato|Die Gruppe ist benachteiligt.|Aggettivi B2
        privilegiert|privilegiato|Er ist privilegiert aufgewachsen.|Aggettivi B2
        marginalisiert|emarginato|Die Minderheit ist marginalisiert.|Aggettivi B2
        integriert|integrato|Die Migranten sind gut integriert.|Aggettivi B2
        multinational|multinazionale|Der Konzern ist multinational.|Aggettivi B2
        supranational|sovranazionale|Die EU ist supranational.|Aggettivi B2
        virtuell|virtuale|Die Welt ist virtuell.|Aggettivi B2
        analog|analogico|Das Gerät ist analog.|Aggettivi B2
        der Aspekt|aspetto|Der Aspekt ist wichtig.|Astratti B2
        der Faktor|fattore|Der Faktor wird unterschätzt.|Astratti B2
        das Kriterium|criterio|Das Kriterium ist klar.|Astratti B2
        der Indikator|indicatore|Der Indikator ist aussagekräftig.|Astratti B2
        der Parameter|parametro|Der Parameter ist festgelegt.|Astratti B2
        die Variable|variabile|Die Variable ist unbekannt.|Astratti B2
        die Konstante|costante|Die Konstante bleibt gleich.|Astratti B2
        das Szenario|scenario|Das Szenario ist realistisch.|Astratti B2
        die Perspektive|prospettiva|Die Perspektive ist neu.|Astratti B2
        der Horizont|orizzonte|Der Horizont ist weit.|Astratti B2
        die Dimension|dimensione|Die Dimension ist unbekannt.|Astratti B2
        die Struktur|struttura|Die Struktur ist klar.|Astratti B2
        das System|sistema|Das System ist stabil.|Astratti B2
        das Modell|modello|Das Modell ist vereinfacht.|Astratti B2
        das Konzept|concetto|Das Konzept überzeugt.|Astratti B2
        die Taktik|tattica|Die Taktik ist riskant.|Astratti B2
        die Initiative|iniziativa|Die Initiative ist erfolgreich.|Astratti B2
        das Engagement|impegno|Das Engagement ist groß.|Astratti B2
        die Legitimität|legittimità|Die Legitimität ist umstritten.|Astratti B2
        die Akzeptanz|accettazione|Die Akzeptanz ist gestiegen.|Astratti B2
        die Compliance|compliance|Die Compliance ist Pflicht.|Astratti B2
        die Governance|governance|Die Governance ist schwach.|Astratti B2
        die Lobby|lobby|Die Lobby ist mächtig.|Astratti B2
        der Lobbyismus|lobbismo|Der Lobbyismus ist umstritten.|Astratti B2
        die Vetternwirtschaft|nepotismo|Die Vetternwirtschaft ist bekannt.|Astratti B2
        der Skandal|scandalo|Der Skandal ist groß.|Astratti B2
        die Affäre|affare|Die Affäre ist noch nicht geklärt.|Astratti B2
        die Eskalation|escalation|Die Eskalation ist gefährlich.|Astratti B2
        die Deeskalation|de-escalation|Die Deeskalation ist nötig.|Astratti B2
        der Konsens|consenso|Der Konsens ist brüchig.|Astratti B2
        der Dissens|dissenso|Der Dissens bleibt.|Astratti B2
        der Diskurs|discorso|Der Diskurs ist hitzig.|Astratti B2
        die Rhetorik|retorica|Die Rhetorik ist überzeugend.|Astratti B2
        die Polemik|polemica|Die Polemik schadet.|Astratti B2
        die Ironie|ironia|Die Ironie ist fein.|Astratti B2
        die Satire|satira|Die Satire ist scharf.|Astratti B2

    """.trimIndent()
}
