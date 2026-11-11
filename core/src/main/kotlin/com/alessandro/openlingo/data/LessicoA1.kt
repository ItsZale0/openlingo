package com.alessandro.openlingo.data

/**
 * Lessico curato per livello (A1-B2), diviso per file per restare leggibile.
 *
 * Formato di ogni riga: `tedesco|italiano|esempio|tema`
 * - `tedesco` include SEMPRE l'articolo per i sostantivi (der/die/das)
 * - `esempio` contiene la parola (serve all'esercizio "completa la frase")
 * - nessuna riga duplicata all'interno dello stesso file
 *
 * A differenza di [WortlisteB1] (nato come lista B1 + sanità), questi elenchi
 * seguono il lessico ufficiale Goethe, che è CUMULATIVO: ~650 parole ad A1,
 * ~1.300 ad A2, ~2.400 a B1. Questo file è il lessico A1 (670 voci); i file
 * successivi contengono le parole NUOVE per livello, deduplicate contro i
 * livelli precedenti da [LessicoLivelli].
 *
 * È una selezione ad alta frequenza pensata per il ripasso attivo, non un
 * dizionario.
 */
object LessicoA1 {

    /** Voci grezze: tedesco|italiano|esempio|tema */
    val grezzo: String = """
        der Mann|uomo|Der Mann wohnt in Berlin.|Persone
        die Frau|donna|Die Frau arbeitet hier.|Persone
        das Kind|bambino|Das Kind spielt im Garten.|Persone
        der Junge|ragazzo|Der Junge lernt Deutsch.|Persone
        das Mädchen|ragazza|Das Mädchen ist zehn Jahre alt.|Persone
        der Freund|amico|Mein Freund heißt Lukas.|Persone
        die Freundin|amica|Meine Freundin kommt aus Wien.|Persone
        die Familie|famiglia|Meine Familie ist klein.|Famiglia
        die Mutter|madre|Meine Mutter kocht gut.|Famiglia
        der Vater|padre|Mein Vater arbeitet viel.|Famiglia
        die Eltern|genitori|Meine Eltern wohnen in Bozen.|Famiglia
        die Schwester|sorella|Meine Schwester ist Lehrerin.|Famiglia
        der Bruder|fratello|Mein Bruder spielt Fußball.|Famiglia
        die Oma|nonna|Meine Oma ist achtzig.|Famiglia
        der Opa|nonno|Mein Opa liest die Zeitung.|Famiglia
        das Baby|neonato|Das Baby schläft jetzt.|Famiglia
        der Sohn|figlio|Ihr Sohn geht zur Schule.|Famiglia
        die Tochter|figlia|Seine Tochter ist noch klein.|Famiglia
        der Name|nome|Mein Name ist Anna.|Presentarsi
        der Vorname|nome di battesimo|Mein Vorname ist Marco.|Presentarsi
        der Nachname|cognome|Wie ist Ihr Nachname?|Presentarsi
        das Alter|età|Wie ist dein Alter?|Presentarsi
        die Adresse|indirizzo|Meine Adresse ist Hauptstraße 5.|Presentarsi
        die Sprache|lingua|Deutsch ist eine schwere Sprache.|Presentarsi
        das Land|paese|Italien ist ein schönes Land.|Presentarsi
        die Stadt|città|Brixen ist eine kleine Stadt.|Presentarsi
        das Dorf|villaggio|Wir wohnen in einem Dorf.|Presentarsi
        die Straße|strada|Die Straße ist sehr lang.|Casa
        der Platz|piazza|Der Platz ist im Zentrum.|Città
        das Haus|casa|Unser Haus ist alt.|Casa
        die Wohnung|appartamento|Die Wohnung hat drei Zimmer.|Casa
        das Zimmer|stanza|Mein Zimmer ist klein und hell.|Casa
        die Küche|cucina|Die Küche ist modern.|Casa
        das Bad|bagno|Das Bad ist neben der Küche.|Casa
        das Schlafzimmer|camera da letto|Das Schlafzimmer ist oben.|Casa
        der Garten|giardino|Im Garten gibt es Blumen.|Casa
        der Balkon|balcone|Der Balkon ist groß.|Casa
        die Tür|porta|Die Tür ist offen.|Casa
        das Fenster|finestra|Das Fenster ist kaputt.|Casa
        der Tisch|tavolo|Der Tisch steht in der Küche.|Casa
        der Stuhl|sedia|Der Stuhl ist bequem.|Casa
        das Bett|letto|Das Bett ist neu.|Casa
        der Schrank|armadio|Der Schrank ist voll.|Casa
        das Sofa|divano|Wir sitzen auf dem Sofa.|Casa
        die Lampe|lampada|Die Lampe ist an.|Casa
        der Schlüssel|chiave|Wo ist mein Schlüssel?|Casa
        das Wasser|acqua|Ich trinke viel Wasser.|Cibo
        das Brot|pane|Ich kaufe frisches Brot.|Cibo
        die Milch|latte|Die Milch ist im Kühlschrank.|Cibo
        der Kaffee|caffè|Ich trinke einen Kaffee.|Cibo
        der Tee|tè|Möchtest du einen Tee?|Cibo
        der Apfel|mela|Der Apfel ist rot.|Cibo
        die Banane|banana|Die Banane ist reif.|Cibo
        das Ei|uovo|Ich esse ein Ei zum Frühstück.|Cibo
        der Käse|formaggio|Der Käse kommt aus Italien.|Cibo
        die Wurst|salsiccia|Die Wurst ist sehr lecker.|Cibo
        das Fleisch|carne|Ich esse kein Fleisch.|Cibo
        der Fisch|pesce|Der Fisch ist frisch.|Cibo
        der Reis|riso|Wir kochen Reis mit Gemüse.|Cibo
        die Kartoffel|patata|Die Kartoffel ist gekocht.|Cibo
        das Gemüse|verdura|Ich kaufe Gemüse auf dem Markt.|Cibo
        das Obst|frutta|Obst ist gesund.|Cibo
        die Suppe|zuppa|Die Suppe ist heiß.|Cibo
        der Salat|insalata|Der Salat ist frisch.|Cibo
        das Frühstück|colazione|Das Frühstück ist um sieben.|Cibo
        das Mittagessen|pranzo|Das Mittagessen ist fertig.|Cibo
        das Abendessen|cena|Wir essen um acht Abendessen.|Cibo
        das Restaurant|ristorante|Das Restaurant ist heute voll.|Cibo
        die Speisekarte|menu|Die Speisekarte liegt auf dem Tisch.|Cibo
        die Rechnung|conto|Die Rechnung bitte!|Cibo
        das Glas|bicchiere|Ein Glas Wasser, bitte.|Cibo
        die Flasche|bottiglia|Die Flasche ist leer.|Cibo
        der Teller|piatto|Der Teller ist sauber.|Cibo
        die Tasse|tazza|Die Tasse ist heiß.|Cibo
        der Supermarkt|supermercato|Der Supermarkt ist bis acht offen.|Negozi
        der Markt|mercato|Der Markt ist am Samstag.|Negozi
        das Geschäft|negozio|Das Geschäft ist geschlossen.|Negozi
        die Apotheke|farmacia|Die Apotheke ist in der Nähe.|Negozi
        die Bank|banca|Die Bank öffnet um neun.|Negozi
        die Post|posta|Die Post ist neben der Bank.|Negozi
        der Bahnhof|stazione|Der Bahnhof liegt im Zentrum.|Viaggi
        der Zug|treno|Der Zug fährt um zehn.|Viaggi
        der Bus|autobus|Der Bus kommt gleich.|Viaggi
        das Auto|auto|Mein Auto ist alt.|Viaggi
        das Fahrrad|bicicletta|Ich fahre mit dem Fahrrad.|Viaggi
        das Flugzeug|aereo|Das Flugzeug landet um sechs.|Viaggi
        die Fahrkarte|biglietto|Ich kaufe eine Fahrkarte.|Viaggi
        der Bahnsteig|binario|Der Zug steht auf Bahnsteig zwei.|Viaggi
        die Haltestelle|fermata|Die Haltestelle ist dort vorne.|Viaggi
        die Reise|viaggio|Die Reise dauert drei Stunden.|Viaggi
        die Straßenbahn|tram|Die Straßenbahn fährt alle zehn Minuten.|Viaggi
        der Flughafen|aeroporto|Der Flughafen ist weit weg.|Viaggi
        das Hotelzimmer|camera d'albergo|Das Hotelzimmer ist reserviert.|Viaggi
        die Arbeit|lavoro|Meine Arbeit beginnt um acht.|Lavoro
        der Beruf|professione|Was ist dein Beruf?|Lavoro
        der Lehrer|insegnante|Der Lehrer erklärt die Regel.|Lavoro
        die Lehrerin|insegnante (f)|Die Lehrerin ist sehr freundlich.|Lavoro
        der Arzt|medico|Der Arzt kommt gleich.|Lavoro
        die Ärztin|medica|Die Ärztin arbeitet im Krankenhaus.|Lavoro
        der Krankenpfleger|infermiere|Der Krankenpfleger hilft den Patienten.|Lavoro
        die Krankenschwester|infermiera|Die Krankenschwester misst den Blutdruck.|Lavoro
        der Verkäufer|commesso|Der Verkäufer ist freundlich.|Lavoro
        der Koch|cuoco|Der Koch arbeitet im Restaurant.|Lavoro
        der Kellner|cameriere|Der Kellner bringt das Essen.|Lavoro
        die Firma|azienda|Die Firma ist in Bozen.|Lavoro
        das Büro|ufficio|Ich arbeite im Büro.|Lavoro
        der Termin|appuntamento|Ich habe morgen einen Termin.|Lavoro
        das Geld|denaro|Ich habe kein Geld dabei.|Lavoro
        die Stunde|ora|Die Stunde ist zu Ende.|Tempo
        die Minute|minuto|Warte eine Minute, bitte.|Tempo
        der Tag|giorno|Heute ist ein schöner Tag.|Tempo
        die Woche|settimana|Die Woche ist lang.|Tempo
        der Monat|mese|Der Monat beginnt morgen.|Tempo
        das Jahr|anno|Das Jahr hat zwölf Monate.|Tempo
        die Uhr|orologio|Wie viel Uhr ist es?|Tempo
        der Morgen|mattina|Am Morgen trinke ich Kaffee.|Tempo
        der Vormittag|mattina (tarda)|Am Vormittag arbeite ich.|Tempo
        der Nachmittag|pomeriggio|Am Nachmittag lerne ich Deutsch.|Tempo
        der Abend|sera|Am Abend sehe ich Fernsehen.|Tempo
        die Nacht|notte|Die Nacht ist ruhig.|Tempo
        heute|oggi|Heute ist Montag.|Tempo
        morgen|domani|Morgen habe ich frei.|Tempo
        gestern|ieri|Gestern war ich zu Hause.|Tempo
        jetzt|adesso|Ich bin jetzt müde.|Tempo
        immer|sempre|Er ist immer freundlich.|Tempo
        oft|spesso|Wir gehen oft spazieren.|Tempo
        manchmal|a volte|Manchmal koche ich am Abend.|Tempo
        nie|mai|Ich trinke nie Alkohol.|Tempo
        der Montag|lunedì|Am Montag beginnt die Arbeit.|Giorni
        der Dienstag|martedì|Am Dienstag habe ich Deutsch.|Giorni
        der Mittwoch|mercoledì|Der Mittwoch ist frei.|Giorni
        der Donnerstag|giovedì|Am Donnerstag kommt der Arzt.|Giorni
        der Freitag|venerdì|Am Freitag ist Feierabend.|Giorni
        der Samstag|sabato|Am Samstag gehe ich einkaufen.|Giorni
        der Sonntag|domenica|Am Sonntag ruhen wir uns aus.|Giorni
        der Körper|corpo|Der Körper braucht Bewegung.|Salute
        der Kopf|testa|Mein Kopf tut weh.|Salute
        das Haar|capello|Ihre Haare sind lang.|Salute
        das Auge|occhio|Mein Auge ist rot.|Salute
        die Nase|naso|Die Nase ist verstopft.|Salute
        der Mund|bocca|Öffnen Sie den Mund, bitte.|Salute
        das Ohr|orecchio|Mein Ohr tut weh.|Salute
        der Hals|collo|Mein Hals ist entzündet.|Salute
        der Arm|braccio|Der Arm ist verletzt.|Salute
        die Hand|mano|Geben Sie mir die Hand.|Salute
        der Finger|dito|Der Finger blutet.|Salute
        der Bauch|pancia|Mir tut der Bauch weh.|Salute
        das Bein|gamba|Das Bein ist gebrochen.|Salute
        der Fuß|piede|Mein Fuß ist geschwollen.|Salute
        der Rücken|schiena|Mein Rücken tut weh.|Salute
        das Herz|cuore|Das Herz schlägt schnell.|Salute
        krank|malato|Ich bin heute krank.|Salute
        gesund|sano|Obst ist gesund.|Salute
        der Schmerz|dolore|Ich habe starke Schmerzen.|Salute
        das Fieber|febbre|Er hat hohes Fieber.|Salute
        die Erkältung|raffreddore|Ich habe eine Erkältung.|Salute
        die Grippe|influenza|Die Grippe kommt im Winter.|Salute
        der Husten|tosse|Der Husten ist schlimm.|Salute
        die Tablette|compressa|Nehmen Sie eine Tablette am Tag.|Salute
        das Medikament|medicinale|Das Medikament ist rezeptfrei.|Salute
        die Praxis|ambulatorio|Die Praxis ist heute geschlossen.|Salute
        das Krankenhaus|ospedale|Das Krankenhaus ist groß.|Salute
        der Patient|paziente|Der Patient ist stabil.|Salute
        die Hilfe|aiuto|Danke für Ihre Hilfe!|Salute
        der Notarzt|medico d'urgenza|Rufen Sie den Notarzt!|Salute
        die Kleidung|vestiti|Die Kleidung ist im Schrank.|Vestiti
        das Hemd|camicia|Das Hemd ist weiß.|Vestiti
        die Hose|pantaloni|Die Hose ist zu lang.|Vestiti
        der Rock|gonna|Der Rock ist blau.|Vestiti
        das Kleid|vestito|Das Kleid ist schön.|Vestiti
        die Jacke|giacca|Die Jacke ist warm.|Vestiti
        der Mantel|cappotto|Der Mantel ist teuer.|Vestiti
        der Schuh|scarpa|Der Schuh ist neu.|Vestiti
        die Socke|calzino|Die Socken sind sauber.|Vestiti
        der Hut|cappello|Der Hut ist groß.|Vestiti
        die Farbe|colore|Welche Farbe magst du?|Vestiti
        rot|rosso|Die Jacke ist rot.|Colori
        blau|blu|Der Himmel ist blau.|Colori
        grün|verde|Das Gras ist grün.|Colori
        gelb|giallo|Die Sonne ist gelb.|Colori
        schwarz|nero|Mein Hund ist schwarz.|Colori
        weiß|bianco|Das Hemd ist weiß.|Colori
        braun|marrone|Die Schuhe sind braun.|Colori
        grau|grigio|Die Wand ist grau.|Colori
        groß|grande|Unser Haus ist groß.|Aggettivi
        klein|piccolo|Das Zimmer ist klein.|Aggettivi
        neu|nuovo|Das Auto ist neu.|Aggettivi
        alt|vecchio|Mein Handy ist alt.|Aggettivi
        gut|buono|Das Essen ist gut.|Aggettivi
        schlecht|cattivo|Das Wetter ist schlecht.|Aggettivi
        schön|bello|Die Stadt ist schön.|Aggettivi
        teuer|caro|Das Hotel ist teuer.|Aggettivi
        billig|economico|Die Schuhe sind billig.|Aggettivi
        warm|caldo|Der Tee ist warm.|Aggettivi
        kalt|freddo|Das Bier ist kalt.|Aggettivi
        schnell|veloce|Der Zug ist schnell.|Aggettivi
        langsam|lento|Der Bus ist langsam.|Aggettivi
        leicht|facile|Die Aufgabe ist leicht.|Aggettivi
        schwer|difficile|Deutsch ist schwer.|Aggettivi
        voll|pieno|Der Bus ist voll.|Aggettivi
        leer|vuoto|Die Flasche ist leer.|Aggettivi
        müde|stanco|Ich bin sehr müde.|Aggettivi
        glücklich|felice|Sie ist heute glücklich.|Aggettivi
        traurig|triste|Warum bist du traurig?|Aggettivi
        jung|giovane|Er ist noch jung.|Aggettivi
        freundlich|gentile|Die Verkäuferin ist freundlich.|Aggettivi
        wichtig|importante|Das ist sehr wichtig.|Aggettivi
        richtig|giusto|Die Antwort ist richtig.|Aggettivi
        falsch|sbagliato|Diese Antwort ist falsch.|Aggettivi
        zu Hause|a casa|Ich bin heute zu Hause.|Luoghi
        hier|qui|Hier ist mein Platz.|Luoghi
        dort|là|Dort ist die Apotheke.|Luoghi
        links|a sinistra|Das Geschäft ist links.|Luoghi
        rechts|a destra|Die Bank ist rechts.|Luoghi
        oben|sopra|Das Bad ist oben.|Luoghi
        unten|sotto|Die Küche ist unten.|Luoghi
        neben|accanto|Die Post ist neben der Bank.|Luoghi
        zwischen|tra|Der Markt ist zwischen Kirche und Rathaus.|Luoghi
        vor|davanti|Das Auto steht vor dem Haus.|Luoghi
        hinter|dietro|Der Garten ist hinter dem Haus.|Luoghi
        auf|su|Das Buch liegt auf dem Tisch.|Luoghi
        in|in|Ich wohne in Bozen.|Luoghi
        mit|con|Ich gehe mit meiner Freundin ins Kino.|Luoghi
        für|per|Das Geschenk ist für dich.|Luoghi
        ohne|senza|Ich trinke Kaffee ohne Zucker.|Luoghi
        und|e|Ich trinke Tee und Kaffee.|Parole utili
        aber|ma|Ich bin müde, aber glücklich.|Parole utili
        oder|o|Tee oder Kaffee?|Parole utili
        auch|anche|Ich komme auch.|Parole utili
        sehr|molto|Das ist sehr gut.|Parole utili
        nicht|non|Ich verstehe nicht.|Parole utili
        kein|nessuno|Ich habe kein Geld.|Parole utili
        viel|molto|Ich habe viel Arbeit.|Parole utili
        wenig|poco|Ich habe wenig Zeit.|Parole utili
        mehr|più|Ich brauche mehr Zeit.|Parole utili
        bitte|per favore|Ein Wasser, bitte.|Parole utili
        danke|grazie|Danke für die Hilfe!|Parole utili
        entschuldigung|scusi|Entschuldigung, wo ist der Bahnhof?|Parole utili
        ja|sì|Ja, das ist richtig.|Parole utili
        nein|no|Nein, danke.|Parole utili
        vielleicht|forse|Vielleicht komme ich später.|Parole utili
        zusammen|insieme|Wir lernen zusammen.|Parole utili
        allein|da solo|Ich wohne allein.|Parole utili
        gehen|andare|Ich gehe nach Hause.|Verbi
        kommen|venire|Wann kommst du?|Verbi
        wohnen|abitare|Ich wohne in Bozen.|Verbi
        leben|vivere|Sie lebt in Deutschland.|Verbi
        arbeiten|lavorare|Ich arbeite im Krankenhaus.|Verbi
        lernen|imparare|Ich lerne Deutsch.|Verbi
        studieren|studiare|Er studiert Medizin.|Verbi
        sprechen|parlare|Sprechen Sie Deutsch?|Verbi
        verstehen|capire|Ich verstehe die Frage nicht.|Verbi
        hören|sentire|Ich höre Musik.|Verbi
        sehen|vedere|Ich sehe den Bus.|Verbi
        lesen|leggere|Ich lese ein Buch.|Verbi
        schreiben|scrivere|Ich schreibe eine E-Mail.|Verbi
        essen|mangiare|Wir essen um zwölf.|Verbi
        trinken|bere|Ich trinke Wasser.|Verbi
        kochen|cucinare|Meine Mutter kocht Suppe.|Verbi
        kaufen|comprare|Ich kaufe Brot.|Verbi
        bezahlen|pagare|Ich möchte bezahlen.|Verbi
        kosten|costare|Was kostet das?|Verbi
        brauchen|avere bisogno|Ich brauche Hilfe.|Verbi
        haben|avere|Ich habe einen Hund.|Verbi
        sein|essere|Ich bin Student.|Verbi
        werden|diventare|Es wird kalt.|Verbi
        machen|fare|Was machst du heute?|Verbi
        tun|fare|Ich habe viel zu tun.|Verbi
        geben|dare|Geben Sie mir bitte das Buch.|Verbi
        nehmen|prendere|Ich nehme den Bus.|Verbi
        finden|trovare|Ich finde den Schlüssel nicht.|Verbi
        suchen|cercare|Ich suche meine Brille.|Verbi
        fragen|chiedere|Ich frage den Lehrer.|Verbi
        antworten|rispondere|Bitte antworten Sie kurz.|Verbi
        helfen|aiutare|Kannst du mir helfen?|Verbi
        warten|aspettare|Ich warte auf den Bus.|Verbi
        fahren|andare (mezzo)|Wir fahren nach Innsbruck.|Verbi
        fliegen|volare|Wir fliegen nach Berlin.|Verbi
        reisen|viaggiare|Ich reise gern.|Verbi
        schlafen|dormire|Das Kind schläft schon.|Verbi
        aufstehen|alzarsi|Ich stehe um sechs auf.|Verbi
        aufwachen|svegliarsi|Ich wache früh auf.|Verbi
        duschen|farsi la doccia|Ich dusche am Morgen.|Verbi
        anfangen|iniziare|Der Kurs fängt um neun an.|Verbi
        aufhören|smettere|Bitte hör auf!|Verbi
        öffnen|aprire|Die Bank öffnet um acht.|Verbi
        schließen|chiudere|Wir schließen um acht.|Verbi
        spielen|giocare|Die Kinder spielen draußen.|Verbi
        singen|cantare|Sie singt sehr schön.|Verbi
        tanzen|ballare|Wir tanzen gern.|Verbi
        lachen|ridere|Warum lachst du?|Verbi
        weinen|piangere|Das Baby weint.|Verbi
        lieben|amare|Ich liebe meine Familie.|Verbi
        mögen|piacere|Ich mag Kaffee.|Verbi
        möchten|volere (cortesia)|Ich möchte einen Tee.|Verbi
        wollen|volere|Ich will nach Hause.|Verbi
        können|potere|Ich kann gut kochen.|Verbi
        müssen|dovere|Ich muss jetzt gehen.|Verbi
        dürfen|potere (permesso)|Darf ich hier parken?|Verbi
        sollen|dovere|Du sollst mehr schlafen.|Verbi
        wissen|sapere|Ich weiß es nicht.|Verbi
        kennen|conoscere|Kennst du den Arzt?|Verbi
        denken|pensare|Ich denke oft an dich.|Verbi
        glauben|credere|Ich glaube, er kommt später.|Verbi
        erklären|spiegare|Der Lehrer erklärt die Grammatik.|Verbi
        wiederholen|ripetere|Bitte wiederholen Sie das.|Verbi
        beginnen|cominciare|Der Unterricht beginnt jetzt.|Verbi
        enden|finire|Der Film endet um zehn.|Verbi
        feiern|festeggiare|Wir feiern heute Geburtstag.|Verbi
        besuchen|visitare|Ich besuche meine Oma.|Verbi
        treffen|incontrare|Ich treffe dich um fünf.|Verbi
        anrufen|telefonare|Ich rufe dich später an.|Verbi
        einkaufen|fare la spesa|Ich kaufe am Samstag ein.|Verbi
        putzen|pulire|Ich putze das Bad.|Verbi
        waschen|lavare|Ich wasche die Wäsche.|Verbi
        tragen|portare|Sie trägt eine rote Jacke.|Verbi
        zeigen|mostrare|Zeig mir das Foto!|Verbi
        sagen|dire|Was sagst du dazu?|Verbi
        das Wetter|tempo (meteo)|Das Wetter ist heute schön.|Tempo libero
        die Sonne|sole|Die Sonne scheint.|Tempo libero
        der Regen|pioggia|Der Regen ist stark.|Tempo libero
        der Schnee|neve|Der Schnee ist weiß.|Tempo libero
        der Wind|vento|Der Wind ist kalt.|Tempo libero
        die Wolke|nuvola|Am Himmel sind viele Wolken.|Tempo libero
        der Sport|sport|Ich mache Sport am Morgen.|Tempo libero
        der Fußball|calcio|Fußball ist sehr beliebt.|Tempo libero
        die Musik|musica|Ich höre gern Musik.|Tempo libero
        das Kino|cinema|Wir gehen ins Kino.|Tempo libero
        der Film|film|Der Film ist langweilig.|Tempo libero
        das Buch|libro|Das Buch ist interessant.|Tempo libero
        die Zeitung|giornale|Ich lese die Zeitung.|Tempo libero
        das Fernsehen|televisione|Im Fernsehen läuft nichts Gutes.|Tempo libero
        das Fest|festa|Das Fest ist am Samstag.|Tempo libero
        der Urlaub|vacanza|Im Sommer habe ich Urlaub.|Tempo libero
        der See|lago|Der See ist sehr kalt.|Tempo libero
        der Berg|montagna|Der Berg ist hoch.|Tempo libero
        der Strand|spiaggia|Der Strand ist voll.|Tempo libero
        das Meer|mare|Das Meer ist blau.|Tempo libero
        das Tier|animale|Das Tier braucht Wasser.|Tempo libero
        der Hund|cane|Der Hund ist braun.|Tempo libero
        die Katze|gatto|Die Katze schläft auf dem Sofa.|Tempo libero
        null|zero|Es sind null Grad.|Numeri
        eins|uno|Eins und eins ist zwei.|Numeri
        zwei|due|Ich habe zwei Brüder.|Numeri
        drei|tre|Das Kind ist drei Jahre alt.|Numeri
        vier|quattro|Der Tisch hat vier Stühle.|Numeri
        fünf|cinque|Ich arbeite fünf Tage pro Woche.|Numeri
        sechs|sei|Der Zug fährt um sechs.|Numeri
        sieben|sette|Die Woche hat sieben Tage.|Numeri
        acht|otto|Der Kurs beginnt um acht.|Numeri
        neun|nove|Das Geschäft öffnet um neun.|Numeri
        zehn|dieci|Ich brauche zehn Minuten.|Numeri
        elf|undici|Es ist elf Uhr.|Numeri
        zwölf|dodici|Das Jahr hat zwölf Monate.|Numeri
        zwanzig|venti|Der Bus kommt in zwanzig Minuten.|Numeri
        dreißig|trenta|Die Fahrt dauert dreißig Minuten.|Numeri
        hundert|cento|Das kostet hundert Euro.|Numeri
        tausend|mille|Die Stadt hat tausend Einwohner.|Numeri
        der Januar|gennaio|Im Januar ist es kalt.|Mesi
        der Februar|febbraio|Der Februar ist kurz.|Mesi
        der März|marzo|Im März kommt der Frühling.|Mesi
        der April|aprile|Im April regnet es oft.|Mesi
        der Mai|maggio|Im Mai sind die Blumen schön.|Mesi
        der Juni|giugno|Im Juni beginnt der Sommer.|Mesi
        der Juli|luglio|Im Juli fahren wir ans Meer.|Mesi
        der August|agosto|Der August ist heiß.|Mesi
        der September|settembre|Im September beginnt die Schule.|Mesi
        der Oktober|ottobre|Im Oktober fallen die Blätter.|Mesi
        der November|novembre|Der November ist grau.|Mesi
        der Dezember|dicembre|Im Dezember feiern wir Weihnachten.|Mesi
        der Frühling|primavera|Der Frühling ist meine Lieblingszeit.|Stagioni
        der Sommer|estate|Im Sommer ist es warm.|Stagioni
        der Herbst|autunno|Im Herbst regnet es viel.|Stagioni
        der Winter|inverno|Der Winter ist kalt.|Stagioni
        die Großmutter|nonna|Meine Großmutter wohnt bei uns.|Famiglia
        der Großvater|nonno|Mein Großvater ist Rentner.|Famiglia
        die Tante|zia|Meine Tante kommt aus Rom.|Famiglia
        der Onkel|zio|Mein Onkel hat ein Haus.|Famiglia
        der Cousin|cugino|Mein Cousin ist zehn.|Famiglia
        die Cousine|cugina|Meine Cousine studiert Medizin.|Famiglia
        das Ehepaar|coppia sposata|Das Ehepaar wohnt nebenan.|Famiglia
        der Ehemann|marito|Ihr Ehemann arbeitet viel.|Famiglia
        die Ehefrau|moglie|Seine Ehefrau ist Ärztin.|Famiglia
        die Geschwister|fratelli e sorelle|Wir sind drei Geschwister.|Famiglia
        der Enkel|nipote|Der Enkel spielt im Garten.|Famiglia
        die Enkelin|nipote|Die Enkelin ist zwei Jahre alt.|Famiglia
        die Wand|parete|Die Wand ist weiß.|Casa
        der Boden|pavimento|Der Boden ist aus Holz.|Casa
        die Decke|soffitto|Die Decke ist hoch.|Casa
        die Treppe|scala|Die Treppe ist steil.|Casa
        der Flur|corridoio|Der Flur ist lang.|Casa
        der Kühlschrank|frigorifero|Die Milch ist im Kühlschrank.|Casa
        der Herd|fornello|Der Herd ist heiß.|Casa
        der Ofen|forno|Der Ofen ist an.|Casa
        die Spülmaschine|lavastoviglie|Die Spülmaschine läuft.|Casa
        die Waschmaschine|lavatrice|Die Waschmaschine ist neu.|Casa
        der Sessel|poltrona|Der Sessel ist bequem.|Casa
        der Teppich|tappeto|Der Teppich ist rot.|Casa
        das Regal|scaffale|Das Regal ist voll.|Casa
        das Bild|quadro|Das Bild hängt an der Wand.|Casa
        der Wecker|sveglia|Der Wecker klingelt um sechs.|Casa
        das Kissen|cuscino|Das Kissen ist weich.|Casa
        die Bettdecke|coperta|Die Bettdecke ist warm.|Casa
        das Handtuch|asciugamano|Das Handtuch ist sauber.|Casa
        die Seife|sapone|Die Seife riecht gut.|Casa
        die Zahnbürste|spazzolino|Die Zahnbürste ist neu.|Casa
        der Spiegel|specchio|Der Spiegel ist im Bad.|Casa
        der Mülleimer|cestino|Der Mülleimer ist voll.|Casa
        das Dach|tetto|Das Dach ist alt.|Casa
        der Eingang|ingresso|Der Eingang ist hinten.|Casa
        das Brötchen|panino|Ich kaufe zwei Brötchen.|Cibo
        die Butter|burro|Die Butter ist im Kühlschrank.|Cibo
        der Zucker|zucchero|Ich trinke Kaffee ohne Zucker.|Cibo
        das Salz|sale|Die Suppe braucht mehr Salz.|Cibo
        der Pfeffer|pepe|Der Pfeffer ist scharf.|Cibo
        das Öl|olio|Das Öl ist aus Italien.|Cibo
        der Saft|succo|Ich trinke einen Saft.|Cibo
        die Limonade|limonata|Die Limonade ist kalt.|Cibo
        das Bier|birra|Er trinkt ein Bier.|Cibo
        der Wein|vino|Der Wein ist rot.|Cibo
        die Schokolade|cioccolata|Die Schokolade ist süß.|Cibo
        der Kuchen|torta|Der Kuchen ist fertig.|Cibo
        die Nudel|pasta|Die Nudeln kochen zehn Minuten.|Cibo
        die Pizza|pizza|Die Pizza ist heiß.|Cibo
        das Eis|gelato|Im Sommer esse ich Eis.|Cibo
        die Tomate|pomodoro|Die Tomate ist reif.|Cibo
        die Zwiebel|cipolla|Die Zwiebel macht mich weinen.|Cibo
        der Knoblauch|aglio|Der Knoblauch riecht stark.|Cibo
        die Gurke|cetriolo|Die Gurke ist frisch.|Cibo
        der Mais|mais|Der Mais ist gekocht.|Cibo
        die Bohne|fagiolo|Die Bohne ist grün.|Cibo
        die Birne|pera|Die Birne ist süß.|Cibo
        die Orange|arancia|Die Orange ist saftig.|Cibo
        die Traube|uva|Die Traube ist klein.|Cibo
        die Erdbeere|fragola|Die Erdbeere ist rot.|Cibo
        der Pfirsich|pesca|Der Pfirsich ist weich.|Cibo
        die Zitrone|limone|Die Zitrone ist sauer.|Cibo
        der Schinken|prosciutto|Der Schinken ist lecker.|Cibo
        das Huhn|pollo|Das Huhn ist im Ofen.|Cibo
        die Soße|salsa|Die Soße ist warm.|Cibo
        das Mehl|farina|Das Mehl ist im Schrank.|Cibo
        der Hunger|fame|Ich habe Hunger.|Cibo
        der Durst|sete|Ich habe Durst.|Cibo
        das Gericht|portata|Das Gericht ist typisch für die Region.|Cibo
        die Vorspeise|antipasto|Die Vorspeise war gut.|Cibo
        der Nachtisch|dessert|Der Nachtisch ist süß.|Cibo
        der Gast|ospite|Der Gast sitzt am Fenster.|Cibo
        das Café|bar|Wir treffen uns im Café.|Cibo
        die Bäckerei|panetteria|Die Bäckerei öffnet früh.|Cibo
        die Metzgerei|macelleria|Die Metzgerei ist geschlossen.|Cibo
        der Euro|euro|Das kostet zehn Euro.|Negozi
        der Cent|centesimo|Es fehlen fünfzig Cent.|Negozi
        die Kreditkarte|carta di credito|Ich zahle mit Kreditkarte.|Negozi
        das Sonderangebot|offerta speciale|Das ist ein Sonderangebot.|Negozi
        die Tüte|sacchetto|Die Tüte kostet zehn Cent.|Negozi
        der Einkaufswagen|carrello|Der Einkaufswagen ist voll.|Negozi
        die Buchhandlung|libreria|Die Buchhandlung ist klein.|Negozi
        das Geschenk|regalo|Das Geschenk ist für dich.|Negozi
        die Umkleidekabine|camerino|Die Umkleidekabine ist frei.|Negozi
        der Fahrplan|orario|Der Fahrplan hängt dort.|Viaggi
        der Sitzplatz|posto a sedere|Der Sitzplatz ist frei.|Viaggi
        das Ticket|biglietto|Das Ticket ist gültig.|Viaggi
        der Pass|passaporto|Der Pass ist abgelaufen.|Viaggi
        die Autobahn|autostrada|Die Autobahn ist frei.|Viaggi
        die Kreuzung|incrocio|An der Kreuzung gehen Sie links.|Viaggi
        die Ampel|semaforo|Die Ampel ist rot.|Viaggi
        die Tankstelle|distributore|Die Tankstelle ist geschlossen.|Viaggi
        der Führerschein|patente|Ich habe den Führerschein.|Viaggi
        das Taxi|taxi|Wir nehmen ein Taxi.|Viaggi
        der Hafen|porto|Der Hafen ist groß.|Viaggi
        die Fähre|traghetto|Die Fähre fährt um neun.|Viaggi
        die Kirche|chiesa|Die Kirche ist alt.|Città
        das Rathaus|municipio|Das Rathaus ist im Zentrum.|Città
        die Universität|università|Die Universität ist groß.|Città
        das Museum|museo|Das Museum ist heute offen.|Città
        das Theater|teatro|Das Theater ist ausverkauft.|Città
        das Schwimmbad|piscina|Das Schwimmbad ist neu.|Città
        der Park|parco|Der Park ist grün.|Città
        der Zoo|zoo|Der Zoo ist am Stadtrand.|Città
        das Stadion|stadio|Das Stadion ist voll.|Città
        die Polizei|polizia|Die Polizei ist schon dort.|Città
        das Amt|ufficio pubblico|Das Amt öffnet um neun.|Città
        der Friedhof|cimitero|Der Friedhof ist ruhig.|Città
        die Brücke|ponte|Die Brücke ist alt.|Città
        der Turm|torre|Der Turm ist hoch.|Città
        die Ecke|angolo|Das Café ist an der Ecke.|Città
        das Zentrum|centro|Das Zentrum ist laut.|Città
        der Stadtteil|quartiere|Der Stadtteil ist ruhig.|Città
        die Nachbarschaft|vicinato|Die Nachbarschaft ist freundlich.|Città
        geradeaus|dritto|Gehen Sie geradeaus.|Luoghi
        weit|lontano|Der Bahnhof ist weit.|Luoghi
        nah|vicino|Die Apotheke ist nah.|Luoghi
        über|sopra|Die Brücke geht über den Fluss.|Luoghi
        unter|sotto|Die Katze ist unter dem Tisch.|Luoghi
        gegenüber|di fronte|Die Bank ist gegenüber.|Luoghi
        entlang|lungo|Gehen Sie die Straße entlang.|Luoghi
        bis|fino a|Wir fahren bis München.|Luoghi
        nach|verso|Ich fahre nach Berlin.|Luoghi
        aus|da|Ich komme aus Italien.|Luoghi
        bei|presso|Ich wohne bei meinen Eltern.|Luoghi
        von|di|Ich komme gerade von der Arbeit.|Luoghi
        zum|dal|Ich gehe zum Arzt.|Luoghi
        seit|da|Ich lerne seit einem Jahr Deutsch.|Tempo
        durch|attraverso|Wir gehen durch den Park.|Luoghi
        um|alle|Der Kurs beginnt um neun.|Tempo
        gegen|verso|Ich komme gegen acht.|Tempo
        die Klasse|classe|Die Klasse ist klein.|Scuola
        das Heft|quaderno|Das Heft ist neu.|Scuola
        der Stift|penna|Der Stift schreibt gut.|Scuola
        der Bleistift|matita|Der Bleistift ist kaputt.|Scuola
        das Papier|carta|Das Papier ist weiß.|Scuola
        das Wort|parola|Das Wort ist neu.|Scuola
        die Pause|pausa|Die Pause ist um zehn.|Scuola
        der Schüler|alunno|Der Schüler lernt fleißig.|Scuola
        die Mittagspause|pausa pranzo|Die Mittagspause ist um zwölf.|Lavoro
        der Lohn|stipendio|Der Lohn kommt am Monatsende.|Lavoro
        das Praktikum|tirocinio|Das Praktikum dauert drei Monate.|Lavoro
        der Auftrag|incarico|Der Auftrag ist wichtig.|Lavoro
        der Zahn|dente|Mein Zahn tut weh.|Salute
        die Zahnschmerzen|mal di denti|Ich habe Zahnschmerzen.|Salute
        das Pflaster|cerotto|Ich brauche ein Pflaster.|Salute
        der Notruf|numero d'emergenza|Rufen Sie den Notruf!|Salute
        das T-Shirt|maglietta|Das T-Shirt ist blau.|Vestiti
        der Pullover|maglione|Der Pullover ist warm.|Vestiti
        die Bluse|camicetta|Die Bluse ist weiß.|Vestiti
        die Mütze|berretto|Die Mütze ist warm.|Vestiti
        der Schal|sciarpa|Der Schal ist lang.|Vestiti
        der Gürtel|cintura|Der Gürtel ist braun.|Vestiti
        die Tasche|borsa|Die Tasche ist schwer.|Vestiti
        die Brille|occhiali|Wo ist meine Brille?|Vestiti
        der Ring|anello|Der Ring ist aus Gold.|Vestiti
        der Stiefel|stivale|Der Stiefel ist nass.|Vestiti
        die Sandale|sandalo|Die Sandale ist bequem.|Vestiti
        orange|arancione|Die Jacke ist orange.|Colori
        rosa|rosa|Das Kleid ist rosa.|Colori
        lila|lilla|Die Blume ist lila.|Colori
        beige|beige|Der Mantel ist beige.|Colori
        dunkel|scuro|Die Farbe ist dunkel.|Colori
        hell|chiaro|Das Zimmer ist hell.|Colori
        sauber|pulito|Die Küche ist sauber.|Aggettivi
        schmutzig|sporco|Die Schuhe sind schmutzig.|Aggettivi
        laut|rumoroso|Die Straße ist laut.|Aggettivi
        leise|silenzioso|Das Radio ist leise.|Aggettivi
        stark|forte|Der Kaffee ist stark.|Aggettivi
        schwach|debole|Ich fühle mich schwach.|Aggettivi
        süß|dolce|Der Kuchen ist süß.|Aggettivi
        sauer|acido|Die Zitrone ist sauer.|Aggettivi
        salzig|salato|Die Suppe ist salzig.|Aggettivi
        scharf|piccante|Die Soße ist scharf.|Aggettivi
        frisch|fresco|Das Brot ist frisch.|Aggettivi
        reich|ricco|Er ist reich.|Aggettivi
        arm|povero|Die Familie ist arm.|Aggettivi
        frei|libero|Der Platz ist frei.|Aggettivi
        belegt|occupato|Der Stuhl ist belegt.|Aggettivi
        offen|aperto|Das Fenster ist offen.|Aggettivi
        geschlossen|chiuso|Das Geschäft ist geschlossen.|Aggettivi
        fertig|pronto|Das Essen ist fertig.|Aggettivi
        bereit|pronto|Ich bin bereit.|Aggettivi
        möglich|possibile|Das ist möglich.|Aggettivi
        unmöglich|impossibile|Das ist unmöglich.|Aggettivi
        nötig|necessario|Das ist nicht nötig.|Aggettivi
        einfach|semplice|Die Aufgabe ist einfach.|Aggettivi
        schwierig|difficile|Die Frage ist schwierig.|Aggettivi
        interessant|interessante|Das Buch ist interessant.|Aggettivi
        langweilig|noioso|Der Film ist langweilig.|Aggettivi
        lustig|divertente|Die Geschichte ist lustig.|Aggettivi
        ruhig|tranquillo|Die Straße ist ruhig.|Aggettivi
        pünktlich|puntuale|Der Zug ist pünktlich.|Aggettivi
        spät|tardi|Es ist schon spät.|Tempo
        früh|presto|Ich stehe früh auf.|Tempo
        gleich|subito|Ich komme gleich.|Tempo
        lange|a lungo|Ich warte lange.|Tempo
        kurz|breve|Die Pause ist kurz.|Aggettivi
        heißen|chiamarsi|Ich heiße Thomas.|Verbi
        buchstabieren|compitare|Buchstabieren Sie Ihren Namen.|Verbi
        probieren|provare|Probieren Sie den Kuchen.|Verbi
        schmecken|avere sapore|Die Suppe schmeckt gut.|Verbi
        riechen|odorare|Die Blumen riechen gut.|Verbi
        fühlen|sentire|Ich fühle mich gut.|Verbi
        bleiben|restare|Ich bleibe zu Hause.|Verbi
        gefallen|piacere|Die Wohnung gefällt mir.|Verbi
        danken|ringraziare|Ich danke dir.|Verbi
        gratulieren|fare gli auguri|Wir gratulieren dir.|Verbi
        entschuldigen|scusare|Entschuldigen Sie die Störung.|Verbi
        grüßen|salutare|Grüß deine Familie!|Verbi
        mitkommen|venire con|Willst du mitkommen?|Verbi
        umziehen|traslocare|Wir wollen im Mai umziehen.|Verbi
        bauen|costruire|Sie bauen ein Haus.|Verbi
        backen|cuocere al forno|Ich backe einen Kuchen.|Verbi
        grillen|grigliare|Wir grillen im Garten.|Verbi
        mieten|affittare|Wir mieten eine Wohnung.|Verbi
        vermieten|dare in affitto|Sie vermietet das Zimmer.|Verbi
        verkaufen|vendere|Er verkauft sein Auto.|Verbi
        leihen|prestare|Kannst du mir Geld leihen?|Verbi
        bringen|portare|Kannst du mir Wasser bringen?|Verbi
        holen|andare a prendere|Ich hole die Kinder.|Verbi
        versuchen|provare|Ich versuche es.|Verbi
        schaffen|farcela|Wir schaffen das.|Verbi
        hoffen|sperare|Ich hoffe, du kommst.|Verbi
        wünschen|desiderare|Ich wünsche dir Glück.|Verbi
        schenken|regalare|Ich schenke ihr Blumen.|Verbi
        gehören|appartenere|Das Buch gehört mir.|Verbi
        leiten|dirigere|Sie will das Team leiten.|Verbi
        mitmachen|partecipare|Willst du mitmachen?|Verbi
        funktionieren|funzionare|Der Aufzug funktioniert nicht.|Verbi
        klingeln|suonare|Es klingelt an der Tür.|Verbi
        lächeln|sorridere|Sie kann freundlich lächeln.|Verbi
        sich setzen|sedersi|Hier kann man sich setzen.|Verbi
        sich waschen|lavarsi|Ich wasche mich am Morgen.|Verbi
        wo|dove|Wo wohnst du?|Parole utili
        wohin|dove|Wohin gehst du?|Parole utili
        woher|da dove|Woher kommst du?|Parole utili
        wann|quando|Wann kommt der Bus?|Parole utili
        wie|come|Wie heißt du?|Parole utili
        warum|perché|Warum lernst du Deutsch?|Parole utili
        was|che cosa|Was machst du heute?|Parole utili
        wer|chi|Wer ist das?|Parole utili
        welche|quale|Welche Farbe magst du?|Parole utili
        wie viel|quanto|Wie viel kostet das?|Parole utili
        wie viele|quanti|Wie viele Kinder hast du?|Parole utili
        zu viel|troppo|Das ist zu viel.|Parole utili
        genug|abbastanza|Ich habe genug Zeit.|Parole utili
        nur|solo|Ich habe nur zehn Euro.|Parole utili
        schon|già|Ich bin schon fertig.|Parole utili
        noch|ancora|Ich warte noch.|Parole utili
        bald|presto|Der Bus kommt bald.|Parole utili
        dann|poi|Dann gehen wir nach Hause.|Parole utili
        denn|perché|Ich bleibe hier, denn ich bin müde.|Parole utili
        also|quindi|Also, fangen wir an.|Parole utili
        zum Beispiel|per esempio|Zum Beispiel Kaffee oder Tee.|Parole utili
        natürlich|naturalmente|Natürlich helfe ich dir.|Parole utili
        sicher|sicuro|Bist du sicher?|Parole utili
        leider|purtroppo|Leider kann ich nicht kommen.|Parole utili
        gern|volentieri|Ich trinke gern Tee.|Parole utili
        der Verein|associazione|Der Verein trifft sich am Montag.|Tempo libero
        das Hobby|hobby|Mein Hobby ist Lesen.|Tempo libero
        die Mannschaft|squadra|Die Mannschaft gewinnt.|Tempo libero
        das Spiel|partita|Das Spiel beginnt um acht.|Tempo libero
        das Konzert|concerto|Das Konzert ist ausverkauft.|Tempo libero
        die Party|festa|Die Party ist bei Anna.|Tempo libero
        der Geburtstag|compleanno|Mein Geburtstag ist im Mai.|Tempo libero
        die Wanderung|escursione|Die Wanderung ist lang.|Tempo libero
        die Radtour|giro in bici|Die Radtour macht Spaß.|Tempo libero
        das Schwimmen|nuoto|Schwimmen ist gesund.|Tempo libero
        das Laufen|corsa|Laufen macht müde.|Tempo libero
        das Gras|erba|Das Gras ist grün.|Natura
        der Wald|bosco|Der Wald ist dunkel.|Natura
        der Fluss|fiume|Der Fluss ist tief.|Natura
        der Himmel|cielo|Der Himmel ist blau.|Natura
        der Stern|stella|Der Stern ist hell.|Natura
        der Mond|luna|Der Mond ist voll.|Natura
        der Vogel|uccello|Der Vogel singt.|Animali
        das Pferd|cavallo|Das Pferd läuft schnell.|Animali
        die Kuh|mucca|Die Kuh ist auf der Wiese.|Animali
        das Schwein|maiale|Das Schwein ist dick.|Animali
        das Schaf|pecora|Das Schaf ist weiß.|Animali
        der Hase|lepre|Der Hase ist schnell.|Animali
        die Maus|topo|Die Maus ist klein.|Animali
        der Löwe|leone|Der Löwe ist stark.|Animali
        der Elefant|elefante|Der Elefant ist groß.|Animali
        die Spinne|ragno|Die Spinne ist im Bad.|Animali
        das Insekt|insetto|Das Insekt ist klein.|Animali
        der Schmetterling|farfalla|Der Schmetterling ist bunt.|Animali
        die Wärme|calore|Die Wärme ist angenehm.|Meteo
        das Telefon|telefono|Das Telefon klingelt.|Tecnologia
        der Computer|computer|Der Computer ist langsam.|Tecnologia
        der Fernseher|televisore|Der Fernseher ist an.|Tecnologia
        die Kamera|fotocamera|Die Kamera ist teuer.|Tecnologia
        der Anruf|chiamata|Der Anruf war wichtig.|Tecnologia
        die Nummer|numero|Wie ist deine Nummer?|Tecnologia
    """.trimIndent()
}
