package com.alessandro.tedesco.data

// =====================================================================
// PERCORSO A0 -> A1
// Contenuti curati: suoni, prime strutture, casi e verbi di base.
// =====================================================================

internal fun unitaA0Alfabeto(): UnitaGrammatica = unitaGrammatica(
    id = "a0-alfabeto",
    livello = "A0",
    titolo = "Alfabeto, suoni e pronuncia",
    sottotitolo = "Leggere e scrivere senza partire dalle regole sbagliate",
    obiettivo = "Riconoscere i suoni tedeschi più frequenti e leggere le prime parole.",
    minuti = 14,
    regola = listOf(
        "L'alfabeto tedesco ha 26 lettere più ä, ö, ü e la lettera ß (che vale come ss).",
        "Le combinazioni ei e ie non si leggono allo stesso modo: ei suona [ai], ie suona [i:] lungo.",
        "sch si pronuncia come la sc di «scena»; z suona [ts]; v spesso suona [f]; w suona [v].",
        "La s iniziale davanti a vocale è sonora [z]; sp e st all'inizio di parola suonano [ʃp] e [ʃt].",
        "Le vocali con umlaut cambiano suono: ä è più aperta, ö e ü non esistono in italiano e vanno ascoltate e imitate."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Suoni da riconoscere",
            intestazioni = listOf("Scrittura", "Suono", "Esempio"),
            righe = listOf(
                listOf("ei", "[ai]", "mein"),
                listOf("ie", "[i:]", "sie"),
                listOf("eu / äu", "[oi]", "neu / Häuser"),
                listOf("au", "[au]", "Haus"),
                listOf("sch", "[ʃ]", "Schule"),
                listOf("ch dopo e/i", "[ç]", "ich"),
                listOf("ch dopo a/o/u", "[x]", "Buch"),
                listOf("z", "[ts]", "Zeit"),
                listOf("v", "[f]", "Vater"),
                listOf("w", "[v]", "Wasser"),
                listOf("s iniziale", "[z]", "Sonne"),
                listOf("ß", "[s]", "Straße")
            ),
            nota = "Non serve pronunciare tutto perfettamente: l'importante è non confondere i suoni che cambiano il significato."
        ),
        tabellaUnita(
            titolo = "Vocali lunghe e brevi",
            intestazioni = listOf("Tipo", "Indizio", "Esempio"),
            righe = listOf(
                listOf("Lunga", "vocale + h, vocale doppia o ie", "fahren, See, sie"),
                listOf("Breve", "vocale seguita da due consonanti", "Stadt, kommen"),
                listOf("Umlaut", "ä, ö, ü cambiano il suono", "spät, schön, Tür")
            ),
            nota = "La durata della vocale aiuta l'orecchio, ma non è necessario misurarla: imita la parola intera."
        )
    ),
    esempi = listOf(
        esempioUnita("Ich heiße Anna.", "Mi chiamo Anna."),
        esempioUnita("Wo wohnst du?", "Dove abiti?"),
        esempioUnita("Die Schule ist schön.", "La scuola è bella."),
        esempioUnita("Ich möchte Deutsch lernen.", "Vorrei imparare il tedesco."),
        esempioUnita("Das ist mein Vater.", "Questo è mio padre.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich heise Anna.",
            "Ich heiße Anna.",
            "Il verbo heißen si scrive con ß. La ß non è una B: vale come una s sorda."
        ),
        erroreUnita(
            "sie come «sai»",
            "sie come [zi:]",
            "La combinazione ie si legge [i:] lungo; per il suono [ai] si scrive ei, come in mein."
        ),
        erroreUnita(
            "Schule come «s-cu-le»",
            "Schule come «scu-le»",
            "sch è un unico suono [ʃ], non una s seguita da ch."
        )
    ),
    merksatz = "ei = ai, ie = i lungo, sch = sc di «scena». La ß vale ss.",
    esercizi = listOf(
        sceltaUnita(
            "alf-01",
            "Come si pronuncia ei in mein?",
            listOf("[ai]", "[i:]", "[oi]", "[au]"),
            0,
            "ei si pronuncia [ai], come nell'italiano «mai»."
        ),
        sceltaUnita(
            "alf-02",
            "Come si pronuncia ie in sie?",
            listOf("[ai]", "[i:]", "[je]", "[e]"),
            1,
            "ie indica una i lunga: [zi:]."
        ),
        sceltaUnita(
            "alf-03",
            "Quale parola contiene il suono [ʃ]?",
            listOf("Sonne", "Schule", "Zeit", "Vater"),
            1,
            "Schule inizia con sch, che si pronuncia [ʃ]."
        ),
        sceltaUnita(
            "alf-04",
            "Come si pronuncia la z in Zeit?",
            listOf("[s]", "[ts]", "[z]", "[k]"),
            1,
            "La z tedesca è un'affricata: [ts]."
        ),
        sceltaUnita(
            "alf-05",
            "Come si pronuncia la v in Vater?",
            listOf("[v]", "[f]", "[b]", "[p]"),
            1,
            "In molte parole tedesche v suona [f], come in Vater."
        ),
        sceltaUnita(
            "alf-06",
            "Come si pronuncia la w in Wasser?",
            listOf("[w]", "[v]", "[f]", "[u]"),
            1,
            "La w tedesca suona [v], come la v italiana in «vino»."
        ),
        completaUnita(
            "alf-07",
            "Completa: Stra___e (strada)",
            "ß",
            "Si scrive Straße con la ß, che vale come ss.",
            "Scrivi solo la lettera mancante.",
            listOf("ss")
        ),
        completaUnita(
            "alf-08",
            "Come si scrive «scuola» in tedesco?",
            "Schule",
            "Schule inizia con sch e si scrive con la u finale.",
            "Scrivi solo il sostantivo."
        ),
        completaUnita(
            "alf-09",
            "Come si scrive «amico» in tedesco?",
            "Freund",
            "Freund è l'amico; l'amica è Freundin.",
            "Scrivi solo il sostantivo maschile."
        ),
        trasformaUnita(
            "alf-10",
            "Scrivi in tedesco: «Mi chiamo Luca.»",
            "Ich heiße Luca.",
            "Si usa heißen: ich heiße. La ß può essere scritta anche ss in alternativa.",
            "Inizia con Ich.",
            listOf("Ich heisse Luca.")
        ),
        completaUnita(
            "alf-11",
            "Come si scrive «ospedale» in tedesco?",
            "Krankenhaus",
            "Krankenhaus è un composto: Kranken + Haus, con la h di Haus.",
            "Scrivi il sostantivo, con l'iniziale maiuscola."
        ),
        sceltaUnita(
            "alf-12",
            "In quale parola la s iniziale è sonora [z]?",
            listOf("Schule", "Straße", "Sonne", "Zeit"),
            2,
            "Sonne inizia con s davanti a vocale: [z]. Schule e Straße iniziano con [ʃ], Zeit con [ts]."
        ),
        produciUnita(
            "alf-13",
            "Scrivi in tedesco: «strada».",
            "Straße",
            "Straße si scrive con sch, a e ß: la ß vale come ss.",
            "Una parola sola, con l'iniziale maiuscola."
        )
    )
)

internal fun unitaA0PronomiSein(): UnitaGrammatica = unitaGrammatica(
    id = "a0-pronomi-sein",
    livello = "A0",
    titolo = "Pronomi personali e sein",
    sottotitolo = "Presentarsi e descrivere con il verbo più importante",
    obiettivo = "Usare i pronomi al nominativo e coniugare sein al presente.",
    minuti = 12,
    prerequisiti = listOf("a0-alfabeto"),
    regola = listOf(
        "I pronomi al nominativo sono: ich, du, er/sie/es, wir, ihr, sie. La forma di cortesia Sie si scrive con la maiuscola.",
        "Il verbo sein (essere) è irregolare: ich bin, du bist, er/sie/es ist, wir sind, ihr seid, sie/Sie sind.",
        "Si usa sein per identità, origine, nazionalità, professione, età, stato e luogo: Ich bin Italiener. Sie ist Lehrerin.",
        "Nella frase principale il verbo è in seconda posizione. Nella domanda sì/no il verbo va in prima posizione: Bist du müde?",
        "La forma di cortesia Sie richiede il verbo alla terza persona plurale: Sind Sie Herr Müller?"
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Pronomi e sein",
            intestazioni = listOf("Pronome", "sein", "Esempio"),
            righe = listOf(
                listOf("ich", "bin", "Ich bin Student."),
                listOf("du", "bist", "Du bist müde."),
                listOf("er / sie / es", "ist", "Er ist Lehrer."),
                listOf("wir", "sind", "Wir sind Freunde."),
                listOf("ihr", "seid", "Ihr seid pünktlich."),
                listOf("sie / Sie", "sind", "Sie sind aus Italien.")
            ),
            nota = "Attenzione: sie (loro) e Sie (cortesia) hanno la stessa forma verbale, ma significati diversi."
        ),
        tabellaUnita(
            titolo = "Domanda sì/no",
            intestazioni = listOf("Affermazione", "Domanda"),
            righe = listOf(
                listOf("Du bist Lehrer.", "Bist du Lehrer?"),
                listOf("Er ist zu Hause.", "Ist er zu Hause?"),
                listOf("Sie sind Frau Rossi.", "Sind Sie Frau Rossi?")
            ),
            nota = "Il verbo passa dalla seconda alla prima posizione; il resto della frase non cambia."
        )
    ),
    esempi = listOf(
        esempioUnita("Ich bin Italiener.", "Sono italiano."),
        esempioUnita("Woher kommst du?", "Di dove sei?"),
        esempioUnita("Sie ist Lehrerin.", "Lei è insegnante."),
        esempioUnita("Wir sind müde.", "Siamo stanchi."),
        esempioUnita("Sind Sie Herr Müller?", "Lei è il signor Müller?")
    ),
    errori = listOf(
        erroreUnita(
            "Ich habe 25 Jahre.",
            "Ich bin 25 Jahre alt.",
            "L'età si esprime con sein, non con haben: ich bin ... Jahre alt."
        ),
        erroreUnita(
            "Er bist mein Bruder.",
            "Er ist mein Bruder.",
            "La terza persona singolare di sein è ist."
        ),
        erroreUnita(
            "Wir sind Lehrerin.",
            "Wir sind Lehrerinnen.",
            "Se il soggetto è plurale, anche il nome predicativo va al plurale quando possibile."
        )
    ),
    merksatz = "sein: bin, bist, ist, sind, seid, sind. L'età si dice con sein.",
    esercizi = listOf(
        sceltaUnita(
            "sein-01",
            "Ich ___ Student.",
            listOf("bin", "bist", "ist", "sind"),
            0,
            "Con ich si usa bin."
        ),
        sceltaUnita(
            "sein-02",
            "Du ___ müde.",
            listOf("bin", "bist", "ist", "seid"),
            1,
            "Con du si usa bist."
        ),
        sceltaUnita(
            "sein-03",
            "Er ___ aus Berlin.",
            listOf("bin", "bist", "ist", "sind"),
            2,
            "Con er/sie/es si usa ist."
        ),
        sceltaUnita(
            "sein-04",
            "Wir ___ Freunde.",
            listOf("seid", "sind", "ist", "bin"),
            1,
            "Con wir si usa sind."
        ),
        sceltaUnita(
            "sein-05",
            "___ ihr aus Italien?",
            listOf("Seid", "Sind", "Ist", "Bist"),
            0,
            "Con ihr si usa seid: Seid ihr ...?"
        ),
        completaUnita(
            "sein-06",
            "Sie (forma di cortesia) ___ Frau Rossi.",
            "sind",
            "La forma di cortesia Sie usa sind, come la terza persona plurale.",
            "Scrivi solo la forma di sein."
        ),
        completaUnita(
            "sein-07",
            "Ich ___ 30 Jahre alt.",
            "bin",
            "L'età si esprime con sein: ich bin ... Jahre alt.",
            "Scrivi solo la forma di sein."
        ),
        completaUnita(
            "sein-08",
            "Das ___ mein Bruder.",
            "ist",
            "Il soggetto das è neutro singolare: ist.",
            "Scrivi solo la forma di sein."
        ),
        trasformaUnita(
            "sein-09",
            "Trasforma in domanda sì/no: Du bist Lehrer.",
            "Bist du Lehrer?",
            "Il verbo va in prima posizione: Bist du ...?",
            "Scrivi la domanda completa.",
            listOf("Bist du Lehrer")
        ),
        trasformaUnita(
            "sein-10",
            "Trasforma in domanda sì/no: Er ist zu Hause.",
            "Ist er zu Hause?",
            "Il verbo ist passa in prima posizione.",
            "Scrivi la domanda completa.",
            listOf("Ist er zu Hause")
        ),
        completaUnita(
            "sein-11",
            "Meine Schwester ___ Ärztin.",
            "ist",
            "Il soggetto è singolare: ist.",
            "Scrivi solo la forma di sein."
        ),
        produciUnita(
            "sein-12",
            "Traduci: «Noi siamo stanchi.»",
            "Wir sind müde.",
            "Wir sind + aggettivo müde.",
            "Usa il pronome wir e il verbo sein."
        )
    )
)

internal fun unitaA0Articoli(): UnitaGrammatica = unitaGrammatica(
    id = "a0-articoli",
    livello = "A0",
    titolo = "Articoli determinativi e genere",
    sottotitolo = "Der, die, das: il genere si impara con il sostantivo",
    obiettivo = "Scegliere l'articolo determinativo giusto e riconoscere i principali indizi di genere.",
    minuti = 12,
    prerequisiti = listOf("a0-pronomi-sein"),
    regola = listOf(
        "Il tedesco ha tre generi: maschile (der), femminile (die), neutro (das). Il plurale usa sempre die.",
        "Il genere non si deduce sempre dal significato: das Mädchen (la ragazza) è neutro, der Name è maschile, die Tür è femminile.",
        "Alcuni suffissi aiutano: -ung, -heit, -keit, -schaft, -tion sono normalmente femminili; -er, -ling, -ismus spesso maschili; -chen, -lein, -ment spesso neutri.",
        "L'articolo determinativo al nominativo è der/die/das; al plurale è die per tutti e tre i generi.",
        "Impara sempre il sostantivo insieme al suo articolo: non memorizzare solo la parola italiana."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Articoli determinativi al nominativo",
            intestazioni = listOf("Genere", "Singolare", "Plurale"),
            righe = listOf(
                listOf("Maschile", "der Mann", "die Männer"),
                listOf("Femminile", "die Frau", "die Frauen"),
                listOf("Neutro", "das Kind", "die Kinder")
            ),
            nota = "Il plurale non distingue il genere: l'articolo è sempre die."
        ),
        tabellaUnita(
            titolo = "Indizi di genere",
            intestazioni = listOf("Suffisso", "Genere", "Esempio"),
            righe = listOf(
                listOf("-ung", "femminile", "die Zeitung"),
                listOf("-heit / -keit", "femminile", "die Freiheit / die Möglichkeit"),
                listOf("-schaft", "femminile", "die Freundschaft"),
                listOf("-tion", "femminile", "die Nation"),
                listOf("-er", "spesso maschile", "der Lehrer"),
                listOf("-chen / -lein", "neutro", "das Mädchen / das Fräulein"),
                listOf("-ment", "spesso neutro", "das Dokument")
            ),
            nota = "Sono indizi, non leggi assolute: impara sempre l'articolo insieme alla parola."
        )
    ),
    esempi = listOf(
        esempioUnita("Der Mann ist Lehrer.", "L'uomo è insegnante."),
        esempioUnita("Die Frau liest ein Buch.", "La donna legge un libro."),
        esempioUnita("Das Kind spielt im Garten.", "Il bambino gioca in giardino."),
        esempioUnita("Die Bücher sind neu.", "I libri sono nuovi."),
        esempioUnita("Das Mädchen ist meine Schwester.", "La ragazza è mia sorella.")
    ),
    errori = listOf(
        erroreUnita(
            "Die Mädchen ist klein.",
            "Das Mädchen ist klein.",
            "Mädchen termina in -chen: è neutro, quindi das."
        ),
        erroreUnita(
            "Der Zeitung ist neu.",
            "Die Zeitung ist neu.",
            "I sostantivi in -ung sono femminili: die Zeitung."
        ),
        erroreUnita(
            "Die Buch ist interessant.",
            "Das Buch ist interessant.",
            "Buch è neutro: das Buch."
        )
    ),
    merksatz = "Der, die, das: il genere va imparato con il sostantivo. Al plurale l'articolo è sempre die.",
    esercizi = listOf(
        sceltaUnita(
            "art-01",
            "___ Mann kommt aus Berlin.",
            listOf("Der", "Die", "Das", "Den"),
            0,
            "Mann è maschile: der Mann."
        ),
        sceltaUnita(
            "art-02",
            "___ Frau arbeitet hier.",
            listOf("Der", "Die", "Das", "Den"),
            1,
            "Frau è femminile: die Frau."
        ),
        sceltaUnita(
            "art-03",
            "___ Kind spielt im Garten.",
            listOf("Der", "Die", "Das", "Den"),
            2,
            "Kind è neutro: das Kind."
        ),
        sceltaUnita(
            "art-04",
            "___ Bücher sind neu.",
            listOf("Der", "Die", "Das", "Den"),
            1,
            "Al plurale l'articolo è sempre die: die Bücher."
        ),
        sceltaUnita(
            "art-05",
            "___ Zeitung liegt auf dem Tisch.",
            listOf("Der", "Die", "Das", "Den"),
            1,
            "Il suffisso -ung è femminile: die Zeitung."
        ),
        sceltaUnita(
            "art-06",
            "___ Mädchen ist meine Schwester.",
            listOf("Der", "Die", "Das", "Den"),
            2,
            "Il suffisso -chen rende il sostantivo neutro: das Mädchen."
        ),
        completaUnita(
            "art-07",
            "___ Schule ist groß.",
            "Die",
            "Schule è femminile: die Schule.",
            "Scrivi solo l'articolo."
        ),
        completaUnita(
            "art-08",
            "___ Lehrer kommt gleich.",
            "Der",
            "Lehrer è maschile: der Lehrer.",
            "Scrivi solo l'articolo."
        ),
        completaUnita(
            "art-09",
            "___ Auto ist neu.",
            "Das",
            "Auto è neutro: das Auto.",
            "Scrivi solo l'articolo."
        ),
        trasformaUnita(
            "art-10",
            "Aggiungi l'articolo determinativo: Hund",
            "der Hund",
            "Hund è maschile: der Hund.",
            "Scrivi articolo e sostantivo."
        ),
        trasformaUnita(
            "art-11",
            "Aggiungi l'articolo determinativo: Wohnung",
            "die Wohnung",
            "Wohnung termina in -ung ed è femminile.",
            "Scrivi articolo e sostantivo."
        ),
        produciUnita(
            "art-12",
            "Traduci: «Il libro è interessante.»",
            "Das Buch ist interessant.",
            "Buch è neutro: das Buch.",
            "Usa l'articolo das e l'aggettivo interessant."
        )
    )
)

internal fun unitaA0Plurale(): UnitaGrammatica = unitaGrammatica(
    id = "a0-plurale",
    livello = "A0",
    titolo = "Il plurale dei sostantivi",
    sottotitolo = "Die Kinder, die Autos, die Frauen",
    obiettivo = "Formare i plurali più frequenti e usare correttamente l'articolo die.",
    minuti = 12,
    prerequisiti = listOf("a0-articoli"),
    regola = listOf(
        "Al plurale l'articolo è sempre die, ma il sostantivo cambia in modi diversi.",
        "Le desinenze più comuni sono -e (Tisch -> Tische), -en (Frau -> Frauen), -er (Kind -> Kinder), -s (Auto -> Autos) e nessuna desinenza (Lehrer -> Lehrer).",
        "Molti maschili e neutri prendono -e o -er; molti femminili prendono -en.",
        "Alcuni plurali aggiungono l'umlaut: Stadt -> Städte, Mann -> Männer, Buch -> Bücher.",
        "Alcuni sostantivi si usano solo al plurale: die Eltern (i genitori), die Leute (la gente)."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Desinenze frequenti",
            intestazioni = listOf("Tipo", "Singolare", "Plurale"),
            righe = listOf(
                listOf("-e", "der Tisch", "die Tische"),
                listOf("-en", "die Frau", "die Frauen"),
                listOf("-er", "das Kind", "die Kinder"),
                listOf("-s", "das Auto", "die Autos"),
                listOf("nessuna", "der Lehrer", "die Lehrer"),
                listOf("umlaut + -e", "die Stadt", "die Städte"),
                listOf("umlaut + -er", "der Mann", "die Männer"),
                listOf("-er + umlaut", "das Buch", "die Bücher")
            ),
            nota = "Non esiste una regola unica: il plurale si impara insieme al singolare."
        ),
        tabellaUnita(
            titolo = "Plurali da memorizzare",
            intestazioni = listOf("Singolare", "Plurale", "Italiano"),
            righe = listOf(
                listOf("das Kind", "die Kinder", "i bambini"),
                listOf("die Frau", "die Frauen", "le donne"),
                listOf("der Mann", "die Männer", "gli uomini"),
                listOf("das Buch", "die Bücher", "i libri"),
                listOf("die Stadt", "die Städte", "le città"),
                listOf("das Auto", "die Autos", "le auto")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Die Kinder spielen im Park.", "I bambini giocano nel parco."),
        esempioUnita("Die Frauen arbeiten hier.", "Le donne lavorano qui."),
        esempioUnita("Die Bücher sind interessant.", "I libri sono interessanti."),
        esempioUnita("Die Autos sind neu.", "Le auto sono nuove."),
        esempioUnita("Die Lehrer kommen morgen.", "Gli insegnanti arrivano domani.")
    ),
    errori = listOf(
        erroreUnita(
            "die Kinders",
            "die Kinder",
            "Kind forma il plurale con -er, non con -s."
        ),
        erroreUnita(
            "die Auten",
            "die Autos",
            "Auto è un prestito e forma il plurale con -s."
        ),
        erroreUnita(
            "die Frau",
            "die Frauen",
            "Frau aggiunge -en: die Frauen."
        )
    ),
    merksatz = "Plurale: articolo die + desinenza. Impara il plurale insieme al singolare.",
    esercizi = listOf(
        sceltaUnita(
            "plur-01",
            "Plurale di Kind:",
            listOf("die Kind", "die Kinder", "die Kindes", "die Kindern"),
            1,
            "Kind -> Kinder: die Kinder."
        ),
        sceltaUnita(
            "plur-02",
            "Plurale di Auto:",
            listOf("die Auten", "die Autos", "die Autoe", "die Auto"),
            1,
            "Auto è un prestito: die Autos."
        ),
        sceltaUnita(
            "plur-03",
            "Plurale di Frau:",
            listOf("die Fraus", "die Frauen", "die Fräue", "die Frau"),
            1,
            "Frau -> Frauen."
        ),
        sceltaUnita(
            "plur-04",
            "Plurale di Buch:",
            listOf("die Buchs", "die Bucher", "die Bücher", "die Büchen"),
            2,
            "Buch -> Bücher, con umlaut e -er."
        ),
        sceltaUnita(
            "plur-05",
            "Plurale di Tisch:",
            listOf("die Tische", "die Tischer", "die Tischs", "die Tischen"),
            0,
            "Tisch -> Tische."
        ),
        sceltaUnita(
            "plur-06",
            "Plurale di Stadt:",
            listOf("die Stadts", "die Städte", "die Stadten", "die Städter"),
            1,
            "Stadt -> Städte, con umlaut e -e."
        ),
        completaUnita(
            "plur-07",
            "Scrivi il plurale: der Lehrer -> die ___",
            "Lehrer",
            "Lehrer resta invariato: die Lehrer.",
            "Scrivi solo il sostantivo al plurale."
        ),
        completaUnita(
            "plur-08",
            "Scrivi il plurale: der Mann -> die ___",
            "Männer",
            "Mann -> Männer, con umlaut e -er.",
            "Scrivi solo il sostantivo al plurale."
        ),
        completaUnita(
            "plur-09",
            "Scrivi il plurale: die Zeitung -> die ___",
            "Zeitungen",
            "Zeitung aggiunge -en: die Zeitungen.",
            "Scrivi solo il sostantivo al plurale."
        ),
        trasformaUnita(
            "plur-10",
            "Trasforma al plurale: Das Kind spielt.",
            "Die Kinder spielen.",
            "Il soggetto diventa plurale e il verbo prende la forma plurale.",
            "Scrivi la frase completa al plurale."
        ),
        trasformaUnita(
            "plur-11",
            "Trasforma al plurale: Der Freund kommt.",
            "Die Freunde kommen.",
            "Freund -> Freunde; kommt -> kommen.",
            "Scrivi la frase completa al plurale."
        ),
        produciUnita(
            "plur-12",
            "Traduci: «I libri sono nuovi.»",
            "Die Bücher sind neu.",
            "Buch -> Bücher; il verbo sein al plurale è sind.",
            "Usa die Bücher e sind."
        )
    )
)

internal fun unitaA0Frase(): UnitaGrammatica = unitaGrammatica(
    id = "a0-frase",
    livello = "A0",
    titolo = "Ordine della frase e domande",
    sottotitolo = "Il verbo in seconda posizione",
    obiettivo = "Costruire frasi principali corrette, domande sì/no e domande con W-Wort.",
    minuti = 13,
    prerequisiti = listOf("a0-articoli"),
    regola = listOf(
        "Nella frase principale il verbo coniugato occupa la seconda posizione: Ich gehe heute ins Kino.",
        "Se la frase inizia con un'espressione di tempo o luogo, il soggetto passa dopo il verbo: Heute gehe ich ins Kino.",
        "La domanda sì/no mette il verbo in prima posizione: Gehst du ins Kino?",
        "La domanda con W-Wort segue lo schema W-Wort + verbo + soggetto: Wo wohnst du?",
        "La negazione nicht sta dopo il verbo o l'aggettivo; kein sta davanti al sostantivo: Ich habe keine Zeit."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Tre tipi di frase",
            intestazioni = listOf("Tipo", "Schema", "Esempio"),
            righe = listOf(
                listOf("Affermazione", "Soggetto + verbo + ...", "Ich lerne Deutsch."),
                listOf("Tempo all'inizio", "Tempo + verbo + soggetto + ...", "Heute lerne ich Deutsch."),
                listOf("Domanda sì/no", "Verbo + soggetto + ...?", "Lernst du Deutsch?"),
                listOf("Domanda con W-Wort", "W-Wort + verbo + soggetto + ...?", "Wo lernst du Deutsch?")
            ),
            nota = "Il verbo resta sempre nella sua posizione: cambia solo ciò che lo precede."
        ),
        tabellaUnita(
            titolo = "Negazione",
            intestazioni = listOf("Uso", "Posizione", "Esempio"),
            righe = listOf(
                listOf("nicht", "dopo verbo o aggettivo", "Ich arbeite nicht."),
                listOf("kein", "davanti al sostantivo", "Ich habe keine Zeit."),
                listOf("nicht", "davanti a un avverbio o preposizione", "Ich fahre nicht nach Berlin.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich lerne jeden Tag Deutsch.", "Imparo il tedesco ogni giorno."),
        esempioUnita("Heute gehe ich ins Kino.", "Oggi vado al cinema."),
        esempioUnita("Wo wohnst du?", "Dove abiti?"),
        esempioUnita("Hast du Zeit?", "Hai tempo?"),
        esempioUnita("Ich habe keine Zeit.", "Non ho tempo.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich heute gehe ins Kino.",
            "Ich gehe heute ins Kino. / Heute gehe ich ins Kino.",
            "Il verbo deve restare in seconda posizione: se heute è all'inizio, il soggetto va dopo il verbo."
        ),
        erroreUnita(
            "Wo du wohnst?",
            "Wo wohnst du?",
            "Nella domanda con W-Wort il verbo viene subito dopo il W-Wort."
        ),
        erroreUnita(
            "Ich nicht habe Zeit.",
            "Ich habe keine Zeit.",
            "La negazione di un sostantivo con articolo indeterminativo si fa con kein: keine Zeit."
        )
    ),
    merksatz = "Verbo in seconda posizione. Domanda sì/no: verbo in prima. W-Wort + verbo + soggetto.",
    esercizi = listOf(
        sceltaUnita(
            "frase-01",
            "Quale frase è corretta?",
            listOf("Heute ich gehe zur Arbeit.", "Heute gehe ich zur Arbeit.", "Ich heute gehe zur Arbeit.", "Gehe heute ich zur Arbeit."),
            1,
            "Con heute all'inizio il verbo va in seconda posizione e il soggetto dopo: Heute gehe ich ..."
        ),
        sceltaUnita(
            "frase-02",
            "Wo ___ du?",
            listOf("wohnst", "wohne", "wohnen", "wohnt"),
            0,
            "Con du il verbo wohnen diventa wohnst: Wo wohnst du?"
        ),
        sceltaUnita(
            "frase-03",
            "___ du Zeit?",
            listOf("Hast", "Habe", "Hat", "Habt"),
            0,
            "Domanda sì/no con du: Hast du Zeit?"
        ),
        sceltaUnita(
            "frase-04",
            "Ich ___ heute keine Zeit.",
            listOf("habe", "hast", "hat", "haben"),
            0,
            "Con ich si usa habe."
        ),
        sceltaUnita(
            "frase-05",
            "Am Montag ___ wir Deutsch.",
            listOf("lerne", "lernst", "lernen", "lernt"),
            2,
            "Con wir si usa lernen; il verbo resta in seconda posizione."
        ),
        completaUnita(
            "frase-06",
            "Trasforma in domanda sì/no: Du kommst aus Italien. -> ___?",
            "Kommst du aus Italien?",
            "Il verbo passa in prima posizione: Kommst du ...?",
            "Scrivi la domanda completa.",
            listOf("Kommst du aus Italien")
        ),
        trasformaUnita(
            "frase-07",
            "Metti «Morgen» all'inizio: Ich fahre nach Berlin.",
            "Morgen fahre ich nach Berlin.",
            "Con Morgen all'inizio il verbo fahre resta in seconda posizione e ich passa dopo.",
            "Scrivi la frase completa.",
            listOf("Morgen fahre ich nach Berlin")
        ),
        trasformaUnita(
            "frase-08",
            "Trasforma in domanda con «Wo»: Du wohnst in Rom.",
            "Wo wohnst du?",
            "Schema: Wo + wohnst + du?",
            "Scrivi la domanda completa.",
            listOf("Wo wohnst du")
        ),
        completaUnita(
            "frase-09",
            "Negazione: Ich habe ___ Zeit.",
            "keine",
            "Zeit è femminile e il sostantivo è negato: keine Zeit.",
            "Scrivi solo la parola negativa."
        ),
        completaUnita(
            "frase-10",
            "Negazione: Das ist ___ richtig.",
            "nicht",
            "richtig è un aggettivo: si nega con nicht.",
            "Scrivi solo la parola negativa."
        ),
        produciUnita(
            "frase-11",
            "Traduci: «Oggi lavoro a casa.»",
            "Heute arbeite ich zu Hause.",
            "Con Heute all'inizio il verbo arbeite è in seconda posizione: Heute arbeite ich ...",
            "Puoi anche scrivere Ich arbeite heute zu Hause.",
            listOf("Ich arbeite heute zu Hause.")
        ),
        produciUnita(
            "frase-12",
            "Traduci: «Dove abiti?»",
            "Wo wohnst du?",
            "Domanda con W-Wort: Wo + wohnst + du?",
            "Usa il verbo wohnen."
        )
    )
)

internal fun unitaA1Modali(): UnitaGrammatica = unitaGrammatica(
    id = "a1-modali",
    livello = "A1",
    titolo = "I verbi modali",
    sottotitolo = "Können, müssen, dürfen, wollen, sollen, mögen",
    obiettivo = "Esprimere possibilità, dovere, permesso e volontà con i verbi modali.",
    minuti = 14,
    prerequisiti = listOf("a1-praesens"),
    regola = listOf(
        "I verbi modali più importanti sono können (potere), müssen (dovere), dürfen (avere il permesso), wollen (volere), sollen (dovere/consiglio), mögen (piacere) e möchten (vorrei).",
        "Alla prima e alla terza persona singolare i modali non prendono la desinenza -t: ich kann, du kannst, er kann; ich muss, du musst, er muss.",
        "Il modale coniugato sta in seconda posizione; l'infinito del verbo principale va alla fine: Ich kann heute nicht kommen.",
        "La negazione si fa con nicht davanti all'infinito o con kein davanti a un sostantivo: Ich kann nicht kommen. Ich habe keine Zeit.",
        "möchten è la forma cortese di mögen e si usa per ordinare o chiedere: Ich möchte einen Kaffee."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Coniugazione di können, müssen, wollen",
            intestazioni = listOf("Pronome", "können", "müssen", "wollen"),
            righe = listOf(
                listOf("ich", "kann", "muss", "will"),
                listOf("du", "kannst", "musst", "willst"),
                listOf("er / sie / es", "kann", "muss", "will"),
                listOf("wir", "können", "müssen", "wollen"),
                listOf("ihr", "könnt", "müsst", "wollt"),
                listOf("sie / Sie", "können", "müssen", "wollen")
            ),
            nota = "Osserva: ich e er/sie/es hanno la stessa forma, senza -t finale."
        ),
        tabellaUnita(
            titolo = "Significati",
            intestazioni = listOf("Modale", "Significato", "Esempio"),
            righe = listOf(
                listOf("können", "potere, essere capace", "Ich kann schwimmen."),
                listOf("müssen", "dovere, necessità", "Ich muss lernen."),
                listOf("dürfen", "avere il permesso", "Du darfst hier parken."),
                listOf("wollen", "volere, intenzione forte", "Wir wollen nach Berlin."),
                listOf("sollen", "dovere, consiglio", "Du sollst mehr schlafen."),
                listOf("mögen", "piacere", "Ich mag Kaffee."),
                listOf("möchten", "vorrei, forma cortese", "Ich möchte einen Tee.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich kann gut schwimmen.", "So nuotare bene."),
        esempioUnita("Du musst heute arbeiten.", "Oggi devi lavorare."),
        esempioUnita("Er darf nicht rauchen.", "Non può fumare."),
        esempioUnita("Wir wollen nach Berlin fahren.", "Vogliamo andare a Berlino."),
        esempioUnita("Ich möchte einen Kaffee.", "Vorrei un caffè.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich kann sprechen Deutsch.",
            "Ich kann Deutsch sprechen.",
            "L'infinito del verbo principale va alla fine: ... Deutsch sprechen."
        ),
        erroreUnita(
            "Er kannst gut kochen.",
            "Er kann gut kochen.",
            "Alla terza persona singolare il modale non prende -t: er kann."
        ),
        erroreUnita(
            "Ich will nicht keine Zeit.",
            "Ich habe keine Zeit.",
            "Non si accumulano not e kein: per negare un sostantivo basta kein."
        )
    ),
    merksatz = "Modale in seconda posizione, infinito alla fine. ich/er: kann, muss, will (senza -t).",
    esercizi = listOf(
        sceltaUnita(
            "mod-01",
            "Ich ___ gut schwimmen.",
            listOf("kann", "kannst", "können", "könnt"),
            0,
            "Con ich si usa kann."
        ),
        sceltaUnita(
            "mod-02",
            "Du ___ heute arbeiten.",
            listOf("muss", "musst", "müssen", "müsst"),
            1,
            "Con du si usa musst."
        ),
        sceltaUnita(
            "mod-03",
            "Er ___ nicht rauchen. (non ha il permesso)",
            listOf("darf", "darfst", "dürfen", "dürft"),
            0,
            "dürfen esprime permesso; con er si usa darf."
        ),
        sceltaUnita(
            "mod-04",
            "Wir ___ nach Berlin fahren.",
            listOf("wollt", "wollen", "will", "wollte"),
            1,
            "Con wir si usa wollen."
        ),
        sceltaUnita(
            "mod-05",
            "___ ihr Deutsch?",
            listOf("Kann", "Kannst", "Könnt", "Können"),
            2,
            "Con ihr si usa könnt: Könnt ihr ...?"
        ),
        completaUnita(
            "mod-06",
            "Ich ___ (mögen) Kaffee.",
            "mag",
            "mögen è irregolare: ich mag."
        ),
        completaUnita(
            "mod-07",
            "Sie (forma di cortesia) ___ hier parken. (avere il permesso)",
            "dürfen",
            "Con Sie si usa dürfen."
        ),
        completaUnita(
            "mod-08",
            "Er ___ (müssen) lernen.",
            "muss",
            "Con er si usa muss, senza -t."
        ),
        trasformaUnita(
            "mod-09",
            "Metti l'infinito alla fine: Ich kann / gut / kochen.",
            "Ich kann gut kochen.",
            "Il modale kann è in seconda posizione, l'infinito kochen alla fine.",
            "Scrivi la frase completa."
        ),
        trasformaUnita(
            "mod-10",
            "Trasforma in domanda sì/no: Du kannst mir helfen.",
            "Kannst du mir helfen?",
            "Il modale va in prima posizione: Kannst du ...?",
            "Scrivi la domanda completa."
        ),
        completaUnita(
            "mod-11",
            "Ich ___ (möchten) einen Kaffee.",
            "möchte",
            "möchten: ich möchte."
        ),
        produciUnita(
            "mod-12",
            "Traduci: «Devo andare a casa.»",
            "Ich muss nach Hause gehen.",
            "müssen + infinito alla fine: muss ... gehen.",
            "Usa nach Hause per «a casa»."
        )
    )
)

internal fun unitaA1Separabili(): UnitaGrammatica = unitaGrammatica(
    id = "a1-separabili",
    livello = "A1",
    titolo = "I verbi separabili",
    sottotitolo = "Il prefisso che va in fondo alla frase",
    obiettivo = "Riconoscere e usare i verbi con prefisso separabile nelle frasi principali.",
    minuti = 13,
    prerequisiti = listOf("a1-praesens"),
    regola = listOf(
        "Molti verbi tedeschi hanno un prefisso separabile: auf-, an-, aus-, ein-, mit-, zu-, ab-, vor-, zurück-, fern-.",
        "Nella frase principale il verbo coniugato resta in seconda posizione e il prefisso va alla fine: Ich stehe um 7 Uhr auf.",
        "All'infinito e nel participio passato il prefisso resta unito al verbo: aufstehen, aufgestanden.",
        "I prefissi be-, ge-, er-, ver-, ent-, emp-, miss- non si separano: verstehen, bekommen, erklären.",
        "Con i verbi separabili il participio passato mette ge tra prefisso e verbo: aufstehen -> aufgestanden."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Verbi separabili frequenti",
            intestazioni = listOf("Infinito", "Significato", "Frase principale"),
            righe = listOf(
                listOf("aufstehen", "alzarsi", "Ich stehe um 7 Uhr auf."),
                listOf("anfangen", "iniziare", "Der Kurs fängt um 9 Uhr an."),
                listOf("einkaufen", "fare la spesa", "Wir kaufen im Supermarkt ein."),
                listOf("mitkommen", "venire con", "Kommst du mit?"),
                listOf("fernsehen", "guardare la TV", "Er sieht abends fern."),
                listOf("zumachen", "chiudere", "Ich mache die Tür zu."),
                listOf("abholen", "andare a prendere", "Ich hole dich ab."),
                listOf("vorbereiten", "preparare", "Sie bereitet das Essen vor.")
            ),
            nota = "Il prefisso separabile si riconosce perché ha un significato proprio e resta in fondo alla frase."
        ),
        tabellaUnita(
            titolo = "Prefissi inseparabili",
            intestazioni = listOf("Prefisso", "Esempio", "Nota"),
            righe = listOf(
                listOf("be-", "bekommen", "non si separa"),
                listOf("ge-", "gefallen", "non si separa"),
                listOf("er-", "erklären", "non si separa"),
                listOf("ver-", "verstehen", "non si separa"),
                listOf("ent-", "entscheiden", "non si separa")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich stehe um 7 Uhr auf.", "Mi alzo alle 7."),
        esempioUnita("Der Kurs fängt um 9 Uhr an.", "Il corso inizia alle 9."),
        esempioUnita("Wir kaufen im Supermarkt ein.", "Facciamo la spesa al supermercato."),
        esempioUnita("Kommst du mit?", "Vieni con noi?"),
        esempioUnita("Er sieht abends fern.", "La sera guarda la TV.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich aufstehe um 7 Uhr.",
            "Ich stehe um 7 Uhr auf.",
            "Il verbo coniugato va in seconda posizione, il prefisso alla fine."
        ),
        erroreUnita(
            "Ich beginne an.",
            "Ich fange an.",
            "anfangen è irregolare: fange ... an."
        ),
        erroreUnita(
            "Ich habe gestanden auf.",
            "Ich habe aufgestanden.",
            "Nel participio passato il prefisso resta unito: aufgestanden."
        )
    ),
    merksatz = "Prefisso separabile: verbo in seconda posizione, prefisso in fondo. Nel participio: aufgestanden.",
    esercizi = listOf(
        sceltaUnita(
            "sep-01",
            "Ich ___ um 7 Uhr ___.",
            listOf("stehe ... auf", "auf ... stehe", "stehe ... an", "aufstehe ... -"),
            0,
            "aufstehen: Ich stehe um 7 Uhr auf."
        ),
        sceltaUnita(
            "sep-02",
            "Wir ___ heute im Supermarkt ___.",
            listOf("kaufen ... ein", "ein ... kaufen", "kaufe ... ein", "einkaufen ... -"),
            0,
            "einkaufen: Wir kaufen ... ein."
        ),
        sceltaUnita(
            "sep-03",
            "Er ___ abends ___.",
            listOf("sieht ... fern", "fern ... sieht", "sehe ... fern", "sieht ... an"),
            0,
            "fernsehen: Er sieht abends fern."
        ),
        sceltaUnita(
            "sep-04",
            "___ du ___? (mitkommen)",
            listOf("Kommst ... mit", "Mit ... kommst", "Kommt ... mit", "Kommst ... an"),
            0,
            "mitkommen: Kommst du mit?"
        ),
        sceltaUnita(
            "sep-05",
            "Ich ___ die Tür ___.",
            listOf("mache ... zu", "zu ... mache", "machst ... zu", "mache ... an"),
            0,
            "zumachen: Ich mache die Tür zu."
        ),
        completaUnita(
            "sep-06",
            "Ich stehe um 6 Uhr ___.",
            "auf",
            "Il prefisso di aufstehen è auf."
        ),
        completaUnita(
            "sep-07",
            "Wir kaufen im Supermarkt ___.",
            "ein",
            "Il prefisso di einkaufen è ein."
        ),
        completaUnita(
            "sep-08",
            "Er sieht abends ___.",
            "fern",
            "Il prefisso di fernsehen è fern."
        ),
        trasformaUnita(
            "sep-09",
            "Fai una frase principale: aufstehen / ich / um 7 Uhr.",
            "Ich stehe um 7 Uhr auf.",
            "Il verbo coniugato stehe è in seconda posizione, auf alla fine.",
            "Scrivi la frase completa."
        ),
        trasformaUnita(
            "sep-10",
            "Fai una frase principale: anfangen / der Kurs / um 9 Uhr.",
            "Der Kurs fängt um 9 Uhr an.",
            "anfangen è irregolare: fängt ... an.",
            "Scrivi la frase completa.",
            listOf("Der Kurs faengt um 9 Uhr an.")
        ),
        completaUnita(
            "sep-11",
            "Participio passato di aufstehen.",
            "aufgestanden",
            "Il participio è aufgestanden: ge tra prefisso e verbo."
        ),
        produciUnita(
            "sep-12",
            "Traduci: «Vengo volentieri con voi.»",
            "Ich komme gerne mit.",
            "mitkommen: Ich komme ... mit; gerne = volentieri.",
            "Usa mitkommen e gerne."
        )
    )
)

internal fun unitaA1PreposizioniLuogo(): UnitaGrammatica = unitaGrammatica(
    id = "a1-preposizioni-luogo",
    livello = "A1",
    titolo = "Preposizioni di luogo",
    sottotitolo = "Wo? e Wohin? tra dativo e accusativo",
    obiettivo = "Scegliere la preposizione giusta e distinguere stato in luogo e moto a luogo.",
    minuti = 15,
    prerequisiti = listOf("a1-akkusativ"),
    regola = listOf(
        "Le preposizioni in, an, auf, unter, über, vor, hinter, neben, zwischen possono reggere dativo o accusativo.",
        "Con Wo? (dove? stato in luogo) si usa il dativo: Ich bin in der Schule.",
        "Con Wohin? (dove? moto a luogo) si usa l'accusativo: Ich gehe in die Schule.",
        "Le contrazioni più comuni sono im, am, ins, ans, zum, zur, beim, vom: ins Kino, am Tisch, zur Schule.",
        "Con città e paesi si usa spesso nach: nach Berlin, nach Italien. Con le persone e alcuni luoghi si usa zu: zum Arzt, zur Lehrerin."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Wo? e Wohin?",
            intestazioni = listOf("Preposizione", "Wo? + dativo", "Wohin? + accusativo"),
            righe = listOf(
                listOf("in", "in der Schule", "in die Schule"),
                listOf("an", "an der Wand", "an die Wand"),
                listOf("auf", "auf dem Tisch", "auf den Tisch"),
                listOf("unter", "unter dem Bett", "unter das Bett"),
                listOf("über", "über dem Sofa", "über das Sofa"),
                listOf("vor", "vor dem Haus", "vor das Haus"),
                listOf("hinter", "hinter dem Haus", "hinter das Haus"),
                listOf("neben", "neben dem Bett", "neben das Bett"),
                listOf("zwischen", "zwischen den Stühlen", "zwischen die Stühle")
            ),
            nota = "La preposizione è la stessa: cambia il caso e quindi l'articolo."
        ),
        tabellaUnita(
            titolo = "Articoli nei due casi",
            intestazioni = listOf("Genere", "Dativo", "Accusativo"),
            righe = listOf(
                listOf("Maschile", "dem", "den"),
                listOf("Femminile", "der", "die"),
                listOf("Neutro", "dem", "das"),
                listOf("Plurale", "den + -n", "die")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Ich bin in der Schule.", "Sono a scuola."),
        esempioUnita("Ich gehe in die Schule.", "Vado a scuola."),
        esempioUnita("Das Buch liegt auf dem Tisch.", "Il libro è sul tavolo."),
        esempioUnita("Ich lege das Buch auf den Tisch.", "Metto il libro sul tavolo."),
        esempioUnita("Wir fahren nach Berlin.", "Andiamo a Berlino.")
    ),
    errori = listOf(
        erroreUnita(
            "Ich gehe in der Schule.",
            "Ich gehe in die Schule.",
            "Wohin? richiede l'accusativo: in die Schule."
        ),
        erroreUnita(
            "Ich bin in die Schule.",
            "Ich bin in der Schule.",
            "Wo? richiede il dativo: in der Schule."
        ),
        erroreUnita(
            "Ich gehe zu Hause.",
            "Ich gehe nach Hause.",
            "«A casa» si dice nach Hause; «a casa» come stato si dice zu Hause."
        )
    ),
    merksatz = "Wo? -> dativo. Wohin? -> accusativo. La preposizione non cambia, cambia l'articolo.",
    esercizi = listOf(
        sceltaUnita(
            "luogo-01",
            "Ich gehe ___ Schule.",
            listOf("in die", "in der", "im", "zur"),
            0,
            "Wohin? -> accusativo: in die Schule."
        ),
        sceltaUnita(
            "luogo-02",
            "Ich bin ___ Schule.",
            listOf("in die", "in der", "ins", "zur"),
            1,
            "Wo? -> dativo: in der Schule."
        ),
        sceltaUnita(
            "luogo-03",
            "Das Buch liegt ___ Tisch.",
            listOf("auf den", "auf dem", "auf der", "auf das"),
            1,
            "Wo? -> dativo: auf dem Tisch."
        ),
        sceltaUnita(
            "luogo-04",
            "Ich lege das Buch ___ Tisch.",
            listOf("auf den", "auf dem", "auf der", "auf das"),
            0,
            "Wohin? -> accusativo: auf den Tisch."
        ),
        sceltaUnita(
            "luogo-05",
            "Wir wohnen ___ Berlin.",
            listOf("in", "nach", "zu", "bei"),
            0,
            "Con le città si usa in: in Berlin."
        ),
        completaUnita(
            "luogo-06",
            "Ich gehe ___ Kino.",
            "ins",
            "in + das = ins: ins Kino."
        ),
        completaUnita(
            "luogo-07",
            "Sie geht ___ Lehrerin.",
            "zur",
            "zu + der = zur: zur Lehrerin gehen."
        ),
        completaUnita(
            "luogo-08",
            "Wir fahren ___ Italien.",
            "nach",
            "Con i paesi si usa nach: nach Italien."
        ),
        trasformaUnita(
            "luogo-09",
            "Wo? Forma la frase: Ich bin / Schule.",
            "Ich bin in der Schule.",
            "Stato in luogo: in + dativo -> in der Schule.",
            "Scrivi la frase completa."
        ),
        trasformaUnita(
            "luogo-10",
            "Wohin? Forma la frase: Ich gehe / Kino.",
            "Ich gehe ins Kino.",
            "Moto a luogo: in + das = ins.",
            "Scrivi la frase completa.",
            listOf("Ich gehe in das Kino.")
        ),
        completaUnita(
            "luogo-11",
            "Das Bild hängt ___ Wand. (an + dativo)",
            "an der",
            "Wand è femminile; con Wo? si usa il dativo: an der Wand.",
            "Scrivi preposizione e articolo."
        ),
        produciUnita(
            "luogo-12",
            "Traduci: «Vado a casa.»",
            "Ich gehe nach Hause.",
            "«A casa» come moto si dice nach Hause.",
            "Usa nach Hause."
        )
    )
)

internal fun unitaA1PreposizioniTempo(): UnitaGrammatica = unitaGrammatica(
    id = "a1-preposizioni-tempo",
    livello = "A1",
    titolo = "Preposizioni di tempo",
    sottotitolo = "Um, am, im, seit, vor e nach",
    obiettivo = "Collocare azioni nel tempo con le preposizioni più frequenti.",
    minuti = 13,
    prerequisiti = listOf("a1-praesens"),
    regola = listOf(
        "um si usa con l'ora: um 8 Uhr.",
        "am si usa con i giorni e le date: am Montag, am 1. Mai.",
        "im si usa con mesi e stagioni: im Januar, im Sommer.",
        "in si usa per un periodo futuro: in einer Woche, in zwei Tagen.",
        "seit + dativo indica «da/per» un periodo che continua; vor + dativo indica «fa»; nach + dativo indica «dopo»."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Preposizioni di tempo",
            intestazioni = listOf("Preposizione", "Uso", "Esempio"),
            righe = listOf(
                listOf("um", "ora", "um 8 Uhr"),
                listOf("am", "giorno, data", "am Montag, am 1. Mai"),
                listOf("im", "mese, stagione", "im Januar, im Sommer"),
                listOf("in", "periodo futuro", "in einer Woche"),
                listOf("seit", "da/per, con dativo", "seit einem Jahr"),
                listOf("vor", "fa, con dativo", "vor einer Woche"),
                listOf("nach", "dopo, con dativo", "nach dem Essen"),
                listOf("bis", "fino a", "bis morgen"),
                listOf("ab", "a partire da", "ab Montag")
            ),
            nota = "am e im sono contrazioni di an + dem e in + dem."
        ),
        tabellaUnita(
            titolo = "Domande sul tempo",
            intestazioni = listOf("Domanda", "Risposta"),
            righe = listOf(
                listOf("Wann?", "Am Montag."),
                listOf("Um wie viel Uhr?", "Um 8 Uhr."),
                listOf("Wie lange?", "Seit einem Jahr."),
                listOf("Seit wann?", "Seit Januar.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Am Montag habe ich frei.", "Lunedì sono libero."),
        esempioUnita("Im Januar ist es kalt.", "In gennaio fa freddo."),
        esempioUnita("Der Kurs beginnt um 9 Uhr.", "Il corso inizia alle 9."),
        esempioUnita("Ich wohne seit einem Jahr in Berlin.", "Vivo a Berlino da un anno."),
        esempioUnita("Nach dem Essen gehen wir spazieren.", "Dopo mangiato andiamo a passeggiare.")
    ),
    errori = listOf(
        erroreUnita(
            "in Montag",
            "am Montag",
            "I giorni della settimana vogliono am: am Montag."
        ),
        erroreUnita(
            "um Januar",
            "im Januar",
            "I mesi vogliono im: im Januar."
        ),
        erroreUnita(
            "seit ein Jahr",
            "seit einem Jahr",
            "seit regge il dativo: seit einem Jahr."
        )
    ),
    merksatz = "um + ora, am + giorno, im + mese/stagione, in + futuro, seit/vor + dativo.",
    esercizi = listOf(
        sceltaUnita(
            "tempo-01",
            "___ Montag habe ich frei.",
            listOf("Am", "Im", "Um", "In"),
            0,
            "I giorni vogliono am: am Montag."
        ),
        sceltaUnita(
            "tempo-02",
            "___ Januar ist es kalt.",
            listOf("Am", "Im", "Um", "In"),
            1,
            "I mesi vogliono im: im Januar."
        ),
        sceltaUnita(
            "tempo-03",
            "Der Kurs beginnt ___ 9 Uhr.",
            listOf("am", "im", "um", "in"),
            2,
            "L'ora vuole um: um 9 Uhr."
        ),
        sceltaUnita(
            "tempo-04",
            "Ich wohne ___ einem Jahr in Berlin.",
            listOf("seit", "vor", "nach", "in"),
            0,
            "Un periodo che continua si esprime con seit + dativo."
        ),
        sceltaUnita(
            "tempo-05",
            "___ zwei Tagen fahre ich nach Rom.",
            listOf("Seit", "Vor", "In", "Am"),
            2,
            "Un periodo futuro si esprime con in: in zwei Tagen."
        ),
        completaUnita(
            "tempo-06",
            "___ Sommer fahren wir ans Meer.",
            "Im",
            "Le stagioni vogliono im: im Sommer."
        ),
        completaUnita(
            "tempo-07",
            "___ 1. Mai ist Feiertag.",
            "Am",
            "Le date vogliono am: am 1. Mai."
        ),
        completaUnita(
            "tempo-08",
            "Ich bin ___ einer Woche zurück.",
            "in",
            "Un periodo futuro: in einer Woche."
        ),
        trasformaUnita(
            "tempo-09",
            "Scrivi: «Lunedì lavoro.»",
            "Am Montag arbeite ich.",
            "Am Montag + verbo in seconda posizione + soggetto.",
            "Scrivi la frase completa."
        ),
        trasformaUnita(
            "tempo-10",
            "Scrivi: «In estate viaggio.»",
            "Im Sommer reise ich.",
            "Im Sommer + verbo in seconda posizione + soggetto.",
            "Scrivi la frase completa."
        ),
        completaUnita(
            "tempo-11",
            "___ dem Essen gehen wir spazieren.",
            "Nach",
            "«Dopo il pasto» si dice nach dem Essen."
        ),
        produciUnita(
            "tempo-12",
            "Traduci: «Aspetto da un'ora.»",
            "Ich warte seit einer Stunde.",
            "seit + dativo: seit einer Stunde.",
            "Usa warten e seit."
        )
    )
)

internal fun unitaA1Imperativo(): UnitaGrammatica = unitaGrammatica(
    id = "a1-imperativo",
    livello = "A1",
    titolo = "L'imperativo",
    sottotitolo = "Dare ordini, consigli e istruzioni",
    obiettivo = "Formare l'imperativo nelle forme du, ihr, Sie e wir.",
    minuti = 12,
    prerequisiti = listOf("a1-praesens"),
    regola = listOf(
        "La forma du si costruisce dalla radice del verbo senza -st: machen -> Mach!; la -e finale è facoltativa.",
        "La forma ihr usa la radice + -t: Macht das!",
        "La forma Sie usa l'infinito + Sie: Machen Sie das!",
        "La forma wir usa l'infinito + wir: Gehen wir!",
        "I verbi con prefisso separabile mettono il prefisso in fondo: Steh auf!; le forme irregolari più importanti sono sein -> sei, haben -> hab, werden -> werde."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Forme dell'imperativo",
            intestazioni = listOf("Persona", "Forma", "Esempio"),
            righe = listOf(
                listOf("du", "radice (senza -st)", "Mach das!"),
                listOf("ihr", "radice + -t", "Macht das!"),
                listOf("Sie", "infinito + Sie", "Machen Sie das!"),
                listOf("wir", "infinito + wir", "Gehen wir!")
            ),
            nota = "Il pronome du, ihr e wir di solito non si scrive; con Sie invece si scrive sempre."
        ),
        tabellaUnita(
            titolo = "Forme irregolari",
            intestazioni = listOf("Infinito", "du", "ihr", "Sie"),
            righe = listOf(
                listOf("sein", "sei", "seid", "seien Sie"),
                listOf("haben", "hab", "habt", "haben Sie"),
                listOf("werden", "werde", "werdet", "werden Sie"),
                listOf("nehmen", "nimm", "nehmt", "nehmen Sie"),
                listOf("geben", "gib", "gebt", "geben Sie")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Mach bitte die Tür zu!", "Per favore, chiudi la porta!"),
        esempioUnita("Kommt mit!", "Venite con noi!"),
        esempioUnita("Warten Sie bitte hier!", "Aspetti qui, per favore!"),
        esempioUnita("Sei ruhig!", "Stai zitto!"),
        esempioUnita("Steh um 7 Uhr auf!", "Alzati alle 7!")
    ),
    errori = listOf(
        erroreUnita(
            "Du machst das!",
            "Mach das!",
            "Per dare un ordine non si usa il pronome du né la forma -st."
        ),
        erroreUnita(
            "Sein ruhig!",
            "Sei ruhig!",
            "L'imperativo di sein alla seconda persona singolare è sei."
        ),
        erroreUnita(
            "Stehst du auf!",
            "Steh auf!",
            "Nell'imperativo il verbo non prende -st e il prefisso va in fondo."
        )
    ),
    merksatz = "du: radice; ihr: radice + t; Sie: infinito + Sie; wir: infinito + wir. sein -> sei.",
    esercizi = listOf(
        sceltaUnita(
            "imp-01",
            "Imperativo du di machen:",
            listOf("Mach!", "Machst!", "Mache du!", "Machen!"),
            0,
            "La forma du di machen è Mach!"
        ),
        sceltaUnita(
            "imp-02",
            "Imperativo du di kommen:",
            listOf("Komm!", "Kommst!", "Komme du!", "Kommen!"),
            0,
            "La forma du di kommen è Komm!"
        ),
        sceltaUnita(
            "imp-03",
            "Imperativo ihr di gehen:",
            listOf("Geh!", "Geht!", "Gehen!", "Gehst!"),
            1,
            "La forma ihr aggiunge -t: Geht!"
        ),
        sceltaUnita(
            "imp-04",
            "Imperativo Sie di warten:",
            listOf("Warte!", "Wartet!", "Warten Sie!", "Wartest du!"),
            2,
            "Con Sie si usa l'infinito + Sie: Warten Sie!"
        ),
        sceltaUnita(
            "imp-05",
            "Imperativo du di sein:",
            listOf("Sei!", "Bist!", "Sein!", "Seid!"),
            0,
            "L'imperativo di sein è sei."
        ),
        completaUnita(
            "imp-06",
            "___ bitte leise! (sein, du)",
            "Sei",
            "La forma du di sein all'imperativo è Sei."
        ),
        completaUnita(
            "imp-07",
            "___ Sie bitte hier! (warten)",
            "Warten",
            "Con Sie: Warten Sie bitte hier!"
        ),
        completaUnita(
            "imp-08",
            "___ auf! (aufstehen, du)",
            "Steh",
            "aufstehen: Steh auf!"
        ),
        trasformaUnita(
            "imp-09",
            "Trasforma in imperativo du: Du gehst nach Hause.",
            "Geh nach Hause!",
            "La forma du di gehen è Geh; il resto della frase resta uguale.",
            "Scrivi l'ordine completo."
        ),
        trasformaUnita(
            "imp-10",
            "Trasforma in imperativo ihr: Ihr kommt mit.",
            "Kommt mit!",
            "La forma ihr di kommen è Kommt; il prefisso mit va in fondo.",
            "Scrivi l'ordine completo."
        ),
        trasformaUnita(
            "imp-11",
            "Trasforma in imperativo Sie: Sie machen die Tür zu.",
            "Machen Sie die Tür zu!",
            "Con Sie: Machen Sie ...; il prefisso zu va in fondo.",
            "Scrivi l'ordine completo."
        ),
        produciUnita(
            "imp-12",
            "Traduci: «Per favore, siediti!»",
            "Bitte setz dich!",
            "sich setzen: Setz dich! La forma du di setzen è setz.",
            "Usa bitte e il riflessivo dich.",
            listOf("Bitte setze dich!")
        )
    )
)
