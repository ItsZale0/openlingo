package com.alessandro.tedesco.data

// ============================================================
// Unità aggiunte (parte 3): B2
// ============================================================

internal fun unitaB2PassivErsatz(): UnitaGrammatica = unitaGrammatica(
    id = "b2-passiv-ersatz",
    livello = "B2",
    titolo = "Passivo sostitutivo",
    sottotitolo = "Sich lassen, sein + zu + Infinitiv",
    obiettivo = "Esprimere il passivo con forme sostitutive (sich lassen, sein + zu + Inf).",
    minuti = 12,
    prerequisiti = listOf("a2-passivo"),
    regola = listOf(
        "Il tedesco usa forme sostitutive del passivo per evitare il passivo vero e proprio.",
        "sich lassen + Infinitiv: esprime possibilità. Die Tür lässt sich öffnen. (= Die Tür kann geöffnet werden.)",
        "sein + zu + Infinitiv: esprime necessità o possibilità. Das ist zu machen. (= Das muss/kann gemacht werden.)",
        "man + verbo attivo: Man kann das machen.",
        "Passivo con modale (già trattato): Das muss gemacht werden.",
        "sich lassen = può essere fatto; sein + zu = deve/può essere fatto."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Forme sostitutive del passivo",
            intestazioni = listOf("Forma", "Significato", "Esempio"),
            righe = listOf(
                listOf("sich lassen + Inf", "può essere fatto", "Die Tür lässt sich öffnen."),
                listOf("sein + zu + Inf", "deve/può essere fatto", "Das ist zu machen."),
                listOf("man + verbo attivo", "si può fare", "Man kann das machen."),
                listOf("werden + Partizip II", "viene fatto (passivo vero)", "Die Tür wird geöffnet.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Die Tür lässt sich leicht öffnen.", "La porta si apre facilmente."),
        esempioUnita("Das Problem ist schwer zu lösen.", "Il problema è difficile da risolvere."),
        esempioUnita("Das ist nicht zu machen.", "Questo non si può fare."),
        esempioUnita("Der Text lässt sich gut lesen.", "Il testo si legge bene.")
    ),
    errori = listOf(
        erroreUnita("Die Tür kann sich öffnen lassen.", "Die Tür lässt sich öffnen.", "Struttura sbagliata: sich lassen + Infinitiv."),
        erroreUnita("Das ist zu machen nicht.", "Das ist nicht zu machen.", "La negazione va prima di 'zu machen'."),
        erroreUnita("Die Tür wird sich öffnen lassen.", "Die Tür lässt sich öffnen.", "Non si combina werden + sich lassen.")
    ),
    merksatz = "sich lassen = può essere fatto; sein + zu = deve/può essere fatto.",
    esercizi = listOf(
        sceltaUnita("passers-01", "Die Tür ___ sich leicht öffnen.", listOf("lässt", "wird", "ist", "kann"), 0, "sich lassen: lässt sich öffnen."),
        sceltaUnita("passers-02", "Das Problem ist schwer zu ___.", listOf("lösen", "gelöst", "lösend", "gelöst werden"), 0, "sein + zu + Infinitiv: zu lösen."),
        sceltaUnita("passers-03", "Das ___ nicht zu machen.", listOf("ist", "wird", "lässt", "kann"), 0, "sein + zu: ist nicht zu machen."),
        sceltaUnita("passers-04", "Der Text ___ sich gut lesen.", listOf("lässt", "wird", "ist", "kann"), 0, "sich lassen: lässt sich lesen."),
        sceltaUnita("passers-05", "Die Tür lässt sich ___.", listOf("öffnen", "geöffnet", "öffnend", "geöffnet werden"), 0, "sich lassen + Infinitiv."),
        produciUnita("b2-passersatz-prod", "„Il problema si può risolvere facilmente.“", "Das Problem lässt sich leicht lösen.", "Passivo sostitutivo: sich lassen + infinito."),
    )
)

internal fun unitaB2Zustandspassiv(): UnitaGrammatica = unitaGrammatica(
    id = "b2-zustandspassiv",
    livello = "B2",
    titolo = "Passivo stativo",
    sottotitolo = "Zustandspassiv: sein + Partizip II",
    obiettivo = "Distinguere il passivo d'azione (werden) dal passivo stativo (sein + Partizip II).",
    minuti = 12,
    prerequisiti = listOf("a2-passivo"),
    regola = listOf(
        "Vorgangspassiv (azione in corso): werden + Partizip II. Die Tür wird geöffnet. (la porta viene aperta)",
        "Zustandspassiv (stato risultante): sein + Partizip II. Die Tür ist geöffnet. (la porta è aperta — stato)",
        "Il Zustandspassiv descrive lo stato che resta dopo l'azione, spesso con aggettivi di stato.",
        "Si usa per descrivere condizioni, risultati, stati: Die Tür ist geschlossen. Das Auto ist repariert.",
        "Attenzione: sein + Partizip II può anche essere un aggettivo (Die geschlossene Tür = la porta chiusa)."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Vorgangspassiv vs Zustandspassiv",
            intestazioni = listOf("Forma", "Ausiliare", "Significato", "Esempio"),
            righe = listOf(
                listOf("Vorgangspassiv", "werden", "azione in corso", "Die Tür wird geöffnet."),
                listOf("Zustandspassiv", "sein", "stato risultante", "Die Tür ist geöffnet."),
                listOf("Vorgangspassiv", "werden", "azione in corso", "Das Fenster wird geschlossen."),
                listOf("Zustandspassiv", "sein", "stato risultante", "Die Tür ist geschlossen.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Die Tür wird geöffnet. (Vorgang)", "La porta viene aperta."),
        esempioUnita("Die Tür ist geöffnet. (Zustand)", "La porta è aperta."),
        esempioUnita("Die Tür ist geschlossen.", "La porta è chiusa."),
        esempioUnita("Das Auto ist repariert.", "L'auto è riparata.")
    ),
    errori = listOf(
        erroreUnita("Die Tür ist geöffnet worden.", "Die Tür ist geöffnet.", "Per lo stato risultante si usa sein + Partizip II, non il passivo perfetto."),
        erroreUnita("Die Tür wird geöffnet. (= stato)", "Die Tür ist geöffnet.", "werden = azione in corso; sein = stato risultante."),
        erroreUnita("Die Tür ist geschlossen worden.", "Die Tür ist geschlossen.", "Zustandspassiv: sein + Partizip II, senza worden.")
    ),
    merksatz = "werden + Partizip II = azione; sein + Partizip II = stato risultante.",
    esercizi = listOf(
        sceltaUnita("zust-01", "Die Tür ___ geöffnet. (stato risultante)", listOf("wird", "ist", "wurde", "war"), 1, "Zustandspassiv: ist geöffnet."),
        sceltaUnita("zust-02", "Die Tür ___ geöffnet. (azione in corso)", listOf("wird", "ist", "wurde", "war"), 0, "Vorgangspassiv: wird geöffnet."),
        sceltaUnita("zust-03", "Das Fenster ___ geschlossen.", listOf("wird", "ist", "wurde", "war"), 1, "Stato risultante: ist geschlossen."),
        sceltaUnita("zust-04", "Das Auto ___ repariert.", listOf("wird", "ist", "wurde", "war"), 1, "Stato risultante: ist repariert."),
        sceltaUnita("zust-05", "Die Tür wird geöffnet. → Die Tür ___ geöffnet.", listOf("wird", "ist", "wurde", "war"), 1, "Azione → stato: ist geöffnet."),
        produciUnita("b2-zustand-prod", "„Il negozio è aperto.“", "Der Laden ist geöffnet.", "Passivo stativo: sein + Partizip II (geöffnet) descrive lo stato."),
    )
)

internal fun unitaB2Modalpartikeln(): UnitaGrammatica = unitaGrammatica(
    id = "b2-modalpartikeln",
    livello = "B2",
    titolo = "Particelle modali",
    sottotitolo = "Doch, mal, ja, halt, eben: il tono della frase",
    obiettivo = "Usare le particelle modali per modificare il tono e l'atteggiamento del parlante.",
    minuti = 14,
    prerequisiti = listOf("b1-konnektoren"),
    regola = listOf(
        "Le particelle modali (doch, mal, ja, halt, eben, schon, noch, eigentlich, etwa, wohl, denn) non cambiano il significato letterale ma l'atteggiamento del parlante.",
        "doch: sorpresa, insistenza, esortazione. Komm doch! (Vieni su!)",
        "mal: attenuazione, richiesta cortese. Komm mal her! (Vieni un po' qui.)",
        "ja: ovvio, conosciuto. Das ist ja klar! (È ovvio!)",
        "halt/eben: rassegnazione. Das ist halt so. (È così, non ci si può fare nulla.)",
        "schon: certezza o dubbio. Das wird schon klappen. (Andrà bene.)",
        "eigentlich: vera domanda. Was machst du eigentlich? (Cosa fai veramente?)",
        "denn: curiosità. Was machst du denn? (Cosa fai? — tono curioso)",
        "wohl: probabilità. Er wird wohl krank sein. (Sarà probabilmente malato.)"
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Particelle modali comuni",
            intestazioni = listOf("Particella", "Valore", "Esempio"),
            righe = listOf(
                listOf("doch", "sorpresa, insistenza", "Komm doch!"),
                listOf("mal", "attenuazione, cortesia", "Komm mal her!"),
                listOf("ja", "ovvio, conosciuto", "Das ist ja klar!"),
                listOf("halt / eben", "rassegnazione", "Das ist halt so."),
                listOf("schon", "certezza, dubbio", "Das wird schon klappen."),
                listOf("eigentlich", "vera domanda", "Was machst du eigentlich?"),
                listOf("denn", "curiosità", "Was machst du denn?"),
                listOf("wohl", "probabilità", "Er wird wohl krank sein.")
            ),
            nota = "Le particelle modali non si traducono letteralmente: si sentono. Cambiano il tono, non il contenuto."
        )
    ),
    esempi = listOf(
        esempioUnita("Komm doch!", "Vieni su! (insistenza)"),
        esempioUnita("Schau mal!", "Guarda un po'! (attenuazione)"),
        esempioUnita("Das ist ja toll!", "È fantastico! (ovvio)"),
        esempioUnita("Das ist halt so.", "È così. (rassegnazione)"),
        esempioUnita("Was machst du denn?", "Cosa fai? (curiosità)")
    ),
    errori = listOf(
        erroreUnita("Komm!", "Komm doch!", "Senza particella il tono è più secco; doch aggiunge insistenza."),
        erroreUnita("Was machst du?", "Was machst du denn?", "denn rende la domanda più curiosa e naturale."),
        erroreUnita("Das ist ja eine Frage.", "Das ist eine Frage.", "ja si usa in affermazioni ovvie, non in domande.")
    ),
    merksatz = "Le particelle modali non si traducono: si sentono. Cambiano il tono, non il contenuto.",
    esercizi = listOf(
        sceltaUnita("modpart-01", "Komm ___, es ist wichtig!", listOf("doch", "mal", "ja", "halt"), 0, "Insistenza: doch."),
        sceltaUnita("modpart-02", "Schau ___, was ich gefunden habe.", listOf("doch", "mal", "ja", "halt"), 1, "Attenuazione: mal."),
        sceltaUnita("modpart-03", "Das ist ___ klar!", listOf("doch", "mal", "ja", "halt"), 2, "Ovvio: ja."),
        sceltaUnita("modpart-04", "Das ist ___ so, man kann nichts machen.", listOf("doch", "mal", "ja", "halt"), 3, "Rassegnazione: halt."),
        sceltaUnita("modpart-05", "Was machst du ___?", listOf("doch", "mal", "denn", "wohl"), 2, "Curiosità: denn."),
        produciUnita("b2-modalpart-prod", "„Vieni pure a trovarci!“", "Komm doch mal vorbei!", "doch e mal rendono il tono amichevole e insistente."),
    )
)

internal fun unitaB2Referenzwoerter(): UnitaGrammatica = unitaGrammatica(
    id = "b2-referenzwoerter",
    livello = "B2",
    titolo = "Referenzwörter im Text",
    sottotitolo = "Diese, dabei, dadurch: coesione testuale",
    obiettivo = "Riconoscere e usare le parole di riferimento per la coesione testuale (Goethe B2 Lesen).",
    minuti = 12,
    prerequisiti = listOf("b1-praepositionaladverbien"),
    regola = listOf(
        "Nei testi lunghi si usano parole di riferimento per evitare ripetizioni: diese/jene, dabei, dadurch, hierfür, dies, das, es.",
        "diese/jene: riferimento a un sostantivo precedente (questo/quello). Die Stadt ist schön. Diese Stadt liegt am Meer.",
        "dabei, dadurch, hierfür, dafür: riferimento a un'intera frase o situazione precedente.",
        "Relativpronomen (der/die/das, welcher) per riprendere il sostantivo: Der Mann, der dort steht, ...",
        "Le parole di riferimento sono fondamentali per la comprensione dei testi (Goethe B2 Lesen Teil 2).",
        "dies/das/es: riferimento a un'intera frase; dieser/diese/dieses: riferimento a un sostantivo (con accordo).",
        "derselbe/dieselbe/dasselbe = la stessa cosa/la stessa persona (identità): declinazione come l'articolo determinativo (derselbe, dieselbe, dasselbe, derselben, demselben).",
        "derjenige/diejenige/dasjenige = colui che/colui che... (relativo con antecedente generico)."
    ),
    tabelle = listOf(
        tabellaUnita(
            titolo = "Referenzwörter comuni",
            intestazioni = listOf("Parola", "Riferimento", "Esempio"),
            righe = listOf(
                listOf("diese/jene", "sostantivo precedente", "Die Stadt ist schön. Diese Stadt liegt am Meer."),
                listOf("dabei", "frase precedente", "Er lernte viel. Dabei bestand er die Prüfung."),
                listOf("dadurch", "frase precedente", "Er übte täglich. Dadurch wurde er besser."),
                listOf("hierfür / dafür", "frase precedente", "Er braucht Hilfe. Hierfür gibt es Beratung."),
                listOf("dies / das / es", "frase intera", "Er kam zu spät. Das war ein Problem."),
                listOf("der / die / das (rel.)", "sostantivo precedente", "Der Mann, der dort steht, ist mein Chef.")
            )
        )
    ),
    esempi = listOf(
        esempioUnita("Die Stadt ist schön. Diese Stadt liegt am Meer.", "La città è bella. Questa città si trova al mare."),
        esempioUnita("Er lernte viel. Dadurch bestand er die Prüfung.", "Studiò molto. Grazie a ciò superò l'esame."),
        esempioUnita("Er kam zu spät. Das war ein Problem.", "Arrivò in ritardo. Questo fu un problema."),
        esempioUnita("Der Mann, der dort steht, ist mein Chef.", "L'uomo che sta là è il mio capo.")
    ),
    errori = listOf(
        erroreUnita("Die Stadt ist schön. Dieses ist am Meer.", "Die Stadt ist schön. Diese Stadt liegt am Meer. / Diese liegt am Meer.", "dieses + sostantivo, oppure dies + verbo (senza sostantivo)."),
        erroreUnita("Er lernte viel. Dadurch er bestand die Prüfung.", "Er lernte viel. Dadurch bestand er die Prüfung.", "dadurch occupa la prima posizione: verbo in seconda."),
        erroreUnita("Das Auto ist rot. Dieses Auto ist schnell. → Dieses ist schnell.", "Das Auto ist rot. Dieses ist schnell.", "Se si omette il sostantivo, si usa dies/das (neutro).")
    ),
    merksatz = "diese/jene → sostantivo; dabei/dadurch/hierfür → frase intera; dies/das/es → frase intera (senza sostantivo).",
    esercizi = listOf(
        sceltaUnita("ref-01", "Die Stadt ist schön. ___ Stadt liegt am Meer.", listOf("Diese", "Dies", "Das", "Es"), 0, "Riferimento a sostantivo: Diese Stadt."),
        sceltaUnita("ref-02", "Er lernte viel. ___ bestand er die Prüfung.", listOf("Dadurch", "Diese", "Dies", "Das"), 0, "Riferimento a frase: Dadurch."),
        sceltaUnita("ref-03", "Er kam zu spät. ___ war ein Problem.", listOf("Dadurch", "Diese", "Dies", "Das"), 3, "Riferimento a frase intera: Das."),
        sceltaUnita("ref-04", "Der Mann, ___ ich gestern getroffen habe, ist mein Chef.", listOf("der", "den", "dem", "dessen"), 1, "Relativpronome all'accusativo: den."),
        sceltaUnita("ref-05", "Er übte täglich. ___ wurde er besser.", listOf("Dadurch", "Diese", "Dies", "Das"), 0, "Riferimento a frase: Dadurch."),
        produciUnita("b2-referenz-prod", "„Lo stesso problema si ripete ogni anno.“", "Dasselbe Problem wiederholt sich jedes Jahr.", "dasselbe = la stessa cosa (identica), non solo simile."),
    )
)
