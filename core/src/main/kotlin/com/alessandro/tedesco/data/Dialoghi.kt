package com.alessandro.tedesco.data

/** Dialoghi per l'ascolto: conversazioni realistiche a 2 voci. */
object Dialoghi {

    data class BattutaDialogo(
        val parlante: String,      // "A" o "B"
        val testo: String,
        val traduzione: String
    )

    data class DomandaDialogo(
        val domanda: String,
        val opzioni: List<String>,
        val corretta: Int
    )

    data class DialogoAscolto(
        val id: String,
        val titolo: String,
        val livello: String,
        val battute: List<BattutaDialogo>,
        val domande: List<DomandaDialogo>,
        val traduzione: String // descrizione generale
    )

    val dialoghi: List<DialogoAscolto> = listOf(
        DialogoAscolto(
            id = "dial_01",
            titolo = "Alla reception",
            livello = "A2",
            traduzione = "Un paziente si registra in fisioterapia.",
            battute = listOf(
                BattutaDialogo("A", "Guten Tag, ich habe einen Termin um zehn Uhr.", "Buongiorno, ho un appuntamento alle dieci."),
                BattutaDialogo("B", "Guten Tag. Wie ist Ihr Name, bitte?", "Buongiorno. Come si chiama, per favore?"),
                BattutaDialogo("A", "Ich heiße Rossi. Mario Rossi.", "Mi chiamo Rossi. Mario Rossi."),
                BattutaDialogo("B", "Herr Rossi. Haben Sie die Überweisung dabei?", "Signor Rossi. Ha l'impegnativa con sé?"),
                BattutaDialogo("A", "Ja, hier ist sie. Und den Befund vom Orthopäden.", "Sì, eccola. E il referto dell'ortopedico."),
                BattutaDialogo("B", "Perfekt. Bitte nehmen Sie Platz. Die Therapeutin kommt gleich.", "Perfetto. Si accomodi pure. La terapista arriva tra poco."),
                BattutaDialogo("A", "Danke schön. Wie lange dauert die erste Sitzung?", "Grazie. Quanto dura la prima seduta?"),
                BattutaDialogo("B", "Etwa 45 Minuten. Wir machen die Anamnese und einen Befund.", "Circa 45 minuti. Facciamo l'anamnesi e un esame.")
            ),
            domande = listOf(
                DomandaDialogo("A che ora è l'appuntamento?", listOf("Alle 9", "Alle 10", "Alle 11", "Alle 12"), 1),
                DomandaDialogo("Cosa porta il paziente?", listOf("Solo l'impegnativa", "Impegnativa e referto", "Solo il referto", "Niente"), 1),
                DomandaDialogo("Quanto dura la prima seduta?", listOf("30 minuti", "45 minuti", "60 minuti", "90 minuti"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_02",
            titolo = "Anamnesi: mal di schiena",
            livello = "B1",
            traduzione = "Prima seduta: il fisioterapista fa l'anamnesi a un paziente con lombalgia.",
            battute = listOf(
                BattutaDialogo("A", "Guten Morgen, ich bin Herr Müller, Ihr Physiotherapeut. Was führt Sie zu uns?", "Buongiorno, sono il signor Müller, il suo fisioterapista. Cosa la porta da noi?"),
                BattutaDialogo("B", "Ich habe seit drei Wochen Rückenschmerzen, vor allem morgens.", "Ho mal di schiena da tre settimane, soprattutto la mattina."),
                BattutaDialogo("A", "Können Sie mir zeigen, wo genau es wehtut?", "Mi può indicare dove fa male esattamente?"),
                BattutaDialogo("B", "Hier, im unteren Rücken. Manchmal strahlt es ins Bein aus.", "Qui, in basso alla schiena. A volte irradia alla gamba."),
                BattutaDialogo("A", "Haben Sie Taubheitsgefühle oder Kribbeln im Bein?", "Ha intorpidimento o formicolio alla gamba?"),
                BattutaDialogo("B", "Nein, nur Schmerzen. Aber nachts wache ich manchmal auf.", "No, solo dolore. Ma di notte mi sveglio a volte."),
                BattutaDialogo("A", "Verstehen Sie. Machen Sie Sport? Wie ist Ihr Arbeitsalltag?", "Capisco. Fa sport? Com'è la sua giornata lavorativa?"),
                BattutaDialogo("B", "Ich sitze viel am Schreibtisch. Jogge aber zweimal die Woche.", "Sto molto alla scrivania. Ma corro due volte a settimana."),
                BattutaDialogo("A", "Gut. Dann schauen wir uns die Beweglichkeit an. Bitte beugen Sie sich vor.", "Bene. Allora controlliamo la mobilità. Si pieghi in avanti per favore.")
            ),
            domande = listOf(
                DomandaDialogo("Da quanto tempo ha dolore il paziente?", listOf("Una settimana", "Due settimane", "Tre settimane", "Un mese"), 2),
                DomandaDialogo("Dove irradia il dolore?", listOf("Al braccio", "Alla gamba", "Al collo", "Non irradia"), 1),
                DomandaDialogo("Cosa fa il paziente di lavoro?", listOf("Lavoro fisico", "Lavoro alla scrivania", "Insegnante", "In pensione"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_03",
            titolo = "Telefonata: appuntamento",
            livello = "A2",
            traduzione = "Chiamata per fissare un appuntamento di fisioterapia.",
            battute = listOf(
                BattutaDialogo("A", "Physiotherapie Huber, guten Tag.", "Fisioterapia Huber, buongiorno."),
                BattutaDialogo("B", "Guten Tag, ich möchte gerne einen Termin vereinbaren.", "Buongiorno, vorrei fissare un appuntamento."),
                BattutaDialogo("A", "Gerne. Haben Sie eine Überweisung vom Arzt?", "Volentieri. Ha un'impegnativa del medico?"),
                BattutaDialogo("B", "Ja, die habe ich. Ich habe Knieschmerzen seit einer Woche.", "Sì, ce l'ho. Ho dolore al ginocchio da una settimana."),
                BattutaDialogo("A", "Verstehe. Haben Sie morgens oder nachmittags Zeit?", "Capisco. Ha tempo la mattina o il pomeriggio?"),
                BattutaDialogo("B", "Nachmittags wäre besser. Ab 14 Uhr.", "Il pomeriggio sarebbe meglio. Dalle 14."),
                BattutaDialogo("A", "Morgen um 14:30 wäre frei. Passt das?", "Domani alle 14:30 sarebbe libero. Va bene?"),
                BattutaDialogo("B", "Ja, perfekt. Danke schön!", "Sì, perfetto. Grazie mille!"),
                BattutaDialogo("A", "Bitte bringen Sie die Überweisung und bequeme Kleidung mit.", "Porti l'impegnativa e abbigliamento comodo.")
            ),
            domande = listOf(
                DomandaDialogo("Quando ha tempo il paziente?", listOf("Mattina", "Pomeriggio dalle 14", "Sera", "Weekend"), 1),
                DomandaDialogo("Quando è l'appuntamento?", listOf("Oggi 14:30", "Domani 14:30", "Domani 10:00", "Dopodomani 14:30"), 1),
                DomandaDialogo("Cosa deve portare?", listOf("Solo documento", "Impegnativa e abbigliamento comodo", "Referto RM", "Niente"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_04",
            titolo = "In pausa: colleghi austriaci",
            livello = "B1",
            traduzione = "Due colleghi in mensa usano varianti austriache (Jänner, Erdapfel, Topfen).",
            battute = listOf(
                BattutaDialogo("A", "Na, hast du heuer schon Urlaub gemacht?", "Allora, hai fatto le ferie quest'anno?"),
                BattutaDialogo("B", "Ja, im Jänner war ich in Südtirol. Die Berge sind traumhaft.", "Sì, a gennaio ero in Südtirol. Le montagne sono da sogno."),
                BattutaDialogo("A", "Und? Gab's dort gute Erdäpfel? Die Knödel sind der Hammer.", "E? Ci sono buone patate lì? I canederli sono spettacolari."),
                BattutaDialogo("B", "Absolut. Und einen Topfenstrudel muss man probieren. Nicht Quark, Topfen!", "Assolutamente. E devi provare lo strudel di topfen. Non quark, topfen!"),
                BattutaDialogo("A", "Stimmt. Bei uns sagt man Semmel, nicht Brötchen. Und Paradeiser für Tomaten.", "Vero. Da noi si dice Semmel, non Brötchen. E Paradeiser per pomodori."),
                BattutaDialogo("B", "Genau. In der Ordination sagen die Patienten oft 'Jänner' statt Januar.", "Esatto. In ambulatorio i pazienti dicono spesso 'Jänner' invece di Januar."),
                BattutaDialogo("A", "Macht nichts. Hauptsache, wir verstehen uns. Prost!", "Non importa. L'importante è capirsi. Cin cin!"),
                BattutaDialogo("B", "Prost! Auf die Erdäpfel!", "Cin cin! Alle patate!")
            ),
            domande = listOf(
                DomandaDialogo("Quando è andato in vacanza il collega B?", listOf("A luglio", "A gennaio", "Ad agosto", "A Natale"), 1),
                DomandaDialogo("Quale dolce austriaco menzionano?", listOf("Apfelstrudel", "Topfenstrudel", "Sachertorte", "Kaiserschmarrn"), 1),
                DomandaDialogo("Come dicono 'panino' in Austria?", listOf("Brötchen", "Semmel", "Weck", "Schrippe"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_05",
            titolo = "Esercizi a casa: istruzioni",
            livello = "B1",
            traduzione = "Il fisioterapista spiega gli esercizi da fare a casa per il ginocchio.",
            battute = listOf(
                BattutaDialogo("A", "So, jetzt zeige ich Ihnen die Hausübungen für Ihr Knie.", "Allora, le mostro gli esercizi da fare a casa per il ginocchio."),
                BattutaDialogo("B", "Wie oft soll ich die machen?", "Quanto spesso devo farli?"),
                BattutaDialogo("A", "Dreimal am Tag. Morgens, mittags, abends. Je zehn Wiederholungen.", "Tre volte al giorno. Mattina, mezzogiorno, sera. Dieci ripetizioni ciascuna."),
                BattutaDialogo("B", "Und wenn es wehtut? Soll ich aufhören?", "E se fa male? Devo smettere?"),
                BattutaDialogo("A", "Leichter Schmerz ist okay. Aber scharfer Schmerz: sofort aufhören.", "Un leggero dolore va bene. Ma dolore acuto: smetta subito."),
                BattutaDialogo("B", "Verstanden. Wann kommt die Kontrolle?", "Capito. Quando è il controllo?"),
                BattutaDialogo("A", "In zwei Wochen. Bringen Sie das Tagebuch mit, wo Sie die Übungen eintragen.", "Tra due settimane. Porti il diario dove segna gli esercizi."),
                BattutaDialogo("B", "Alles klar. Danke, Herr Müller.", "Tutto chiaro. Grazie, signor Müller."),
                BattutaDialogo("A", "Gute Besserung! Und denken Sie dran: Bewegung ist die beste Medizin.", "Buona guarigione! E ricordi: il movimento è la migliore medicina.")
            ),
            domande = listOf(
                DomandaDialogo("Quante volte al giorno gli esercizi?", listOf("Una", "Due", "Tre", "Quattro"), 2),
                DomandaDialogo("Quando è il prossimo controllo?", listOf("Tra una settimana", "Tra due settimane", "Tra un mese", "Non c'è"), 1),
                DomandaDialogo("Cosa portare al controllo?", listOf("Il referto", "Il diario degli esercizi", "La ricetta", "L'impegnativa"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_06",
            titolo = "Primo giorno di corso",
            livello = "A1",
            traduzione = "Due partecipanti al corso di tedesco si conoscono presentandosi.",
            battute = listOf(
                BattutaDialogo("A", "Hallo! Sind Sie auch hier im Deutschkurs?", "Ciao! È anche lei qui nel corso di tedesco?"),
                BattutaDialogo("B", "Ja, ich bin hier neu. Ich heiße Emma. Und du?", "Sì, sono nuovo qui. Mi chiamo Emma. E tu?"),
                BattutaDialogo("A", "Ich bin Alessandro, sehr schön. Woher kommst du?", "Sono Alessandro, molto piacere. Da dove vieni?"),
                BattutaDialogo("B", "Ich komme aus Brixen. Und du?", "Vengo da Bressanone. E tu?"),
                BattutaDialogo("A", "Auch aus Brixen! Warum lernst du Deutsch?", "Anche io da Bressanone! Perché studi tedesco?"),
                BattutaDialogo("B", "Für die Arbeit und die Prüfung. Und du?", "Per il lavoro e per l'esame. E tu?"),
                BattutaDialogo("A", "Für meine Arbeit im Krankenhaus. Der Kurs ist jeden Dienstag.", "Per il mio lavoro in ospedale. Il corso è ogni martedì.")
            ),
            domande = listOf(
                DomandaDialogo("Da dove viene Emma?", listOf("Da Bolzano", "Da Bressanone", "Da Merano", "Da Trento"), 1),
                DomandaDialogo("Perché studia Alessandro?", listOf("Per esame", "Per il lavoro all'ospedale", "Per hobby", "Per un trasloco"), 1),
                DomandaDialogo("Quando è il corso?", listOf("Lunedì", "Mercoledì", "Martedì", "Giovedì"), 2)
            )
        ),
        DialogoAscolto(
            id = "dial_07",
            titolo = "In farmacia",
            livello = "A2",
            traduzione = "Un cliente chiede alla farmacia un rimedio per il raffreddore.",
            battute = listOf(
                BattutaDialogo("A", "Guten Tag, was kann ich für Sie tun?", "Buongiorno, cosa posso fare per lei?"),
                BattutaDialogo("B", "Guten Tag. Ich habe Schnuppen und Kopfschmerzen.", "Buongiorno. Ho il raffreddore e mal di testa."),
                BattutaDialogo("A", "Seit wann haben Sie die Symptome?", "Da quando ha i sintomi?"),
                BattutaDialogo("B", "Seit drei Tagen. Nachts kann ich schlecht schlafen.", "Da tre giorni. Di notte dormo male."),
                BattutaDialogo("A", "Ich empfehle diese Tropfen und eine Salbe gegen Schmerzen.", "Le consiglio queste gocce e una crema contro il dolore."),
                BattutaDialogo("B", "Wie oft soll ich die Tropfen einnehmen?", "Ogni quanto devo prendere le gocce?"),
                BattutaDialogo("A", "Dreimal täglich, nach dem Essen. Gute Besserung!", "Tre volte al giorno, dopo i pasti. Migliorie!")
            ),
            domande = listOf(
                DomandaDialogo("Che cosa ha il cliente?", listOf("Raffreddore e mal di testa", "Solo febbre", "Mal di schiena", "Vertigini"), 0),
                DomandaDialogo("Da quanto ha i sintomi?", listOf("Da un giorno", "Da tre giorni", "Da una settimana", "Da un mese"), 1),
                DomandaDialogo("Come si prendono le gocce?", listOf("Prima dei pasti", "Tre volte al giorno dopo i pasti", "Solo la sera", "Due volte nel weekend"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_08",
            titolo = "Al supermercato",
            livello = "A2",
            traduzione = "Un cliente chiede indicazioni al commesso e fa la spesa.",
            battute = listOf(
                BattutaDialogo("A", "Entschuldigung, wo finde ich Brot und Milch?", "Scusi, dove trovo il pane e il latte?"),
                BattutaDialogo("B", "Das Brot ist am Backregal hinten links. Die Milch bei den Kühlregalen.", "Il pane è nel reparto panetteria, in fondo a sinistra. Il latte nei frigo dei latticini."),
                BattutaDialogo("A", "Vielen Dank. Und wie viel kostet der Semmel?", "Grazie mille. E quanto costa la ciabatta?"),
                BattutaDialogo("B", "40 Cent das Stück. Heute haben wir eine Aktion: drei für ein Euro.", "40 centesimi l'uno. Oggi c'è una promozione: tre per un euro."),
                BattutaDialogo("A", "Super, dann nehme ich drei. Woher ist der Käse?", "Bene, allora ne prendo tre. Da dove viene il formaggio?"),
                BattutaDialogo("B", "Von einer Käserei in Südtirol — sehr gutes Produkt!", "Da una latteria in Alto Adige — un ottimo prodotto!"),
                BattutaDialogo("A", "Dann nehme ich auch davon 200 Gramm. Danke schön.", "Allora ne prendo anche 200 grammi. Grazie mille.")
            ),
            domande = listOf(
                DomandaDialogo("Quanto costa la ciabatta?", listOf("30 centesimi", "40 centesimi", "Un euro", "Un euro e venti"), 1),
                DomandaDialogo("Che promozione c'è oggi?", listOf("Tre ciabatte per un euro", "Due latte per uno", "Sconto del 20%", "Formaggio gratis"), 0),
                DomandaDialogo("Da dove viene il formaggio?", listOf("Da una latteria altoatesina", "Dall'Austria", "Dall'Olanda", "Da una fabbrica locale"), 0)
            )
        ),
        DialogoAscolto(
            id = "dial_09",
            titolo = "Dal medico di base",
            livello = "A2",
            traduzione = "Consultazione con il medico di base per un problema alla schiena.",
            battute = listOf(
                BattutaDialogo("A", "Guten Tag, Frau Huber. Was führt Sie heute zu mir?", "Buongiorno, signora Huber. Cosa la porta oggi da me?"),
                BattutaDialogo("B", "Ich habe seit einer Woche Rückenschmerzen, links unterhalb der Schulter.", "Ho mal di schiena da una settimana, a sinistra sotto la spalla."),
                BattutaDialogo("A", "Fieber? Husten? Gewichtsverlust?", "Febbre? Tosse? Perdita di peso?"),
                BattutaDialogo("B", "Nein, keine. Aber nachts wache ich auf.", "No, niente. Ma di notte mi sveglio."),
                BattutaDialogo("A", "Ich verschreibe Ihnen ein Schmerzmittel und eine Überweisung zur Physiotherapie.", "Le prescrivo un antidolorifico e un'impegnativa per la fisioterapia."),
                BattutaDialogo("B", "Wie lange muss ich die Tabletten nehmen?", "Per quanto tempo devo prendere le compresse?"),
                BattutaDialogo("A", "Vier Tage, maximal zweimal täglich. Wenn es nicht besser wird: bitte zurück.", "Quattro giorni, al massimo due volte al giorno. Se non migliora, torni da me.")
            ),
            domande = listOf(
                DomandaDialogo("Dove fa male?", listOf("Sotto la spalla sinistra", "Sotto la spalla destra", "Sul collo", "Alla gamba"), 0),
                DomandaDialogo("Cosa riceve la paziente?", listOf("Antidolorifico e impegnativa", "Solo la impegnativa", "Una radiografia", "Iniezioni"), 0),
                DomandaDialogo("Per quanto tempo le compresse?", listOf("Due giorni", "Quattro giorni", "Una settimana", "Un mese"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_10",
            titolo = "Dal dentista",
            livello = "B1",
            traduzione = "Visita dentistica: dolore al dente e cura discussa con la dottoressa.",
            battute = listOf(
                BattutaDialogo("A", "Guten Tag, setzen Sie sich bitte. Welcher Zahn tut weh?", "Buongiorno, si sieda per favore. Quale dente fa male?"),
                BattutaDialogo("B", "Unten rechts, der Backenzahn. Beim Kauen tut er besonders weh.", "In basso a destra, il molare. Quando mastico fa male, soprattutto."),
                BattutaDialogo("A", "Öffnen Sie bitte den Mund. ... Ja, ich sehe eine kleine Karies.", "Apra la bocca per favore. ... Sì, vedo una piccola carie."),
                BattutaDialogo("B", "Muss es eine Wurzelbehandlung sein? Ich habe Angst davor.", "Deve fare la devitalizzazione? Ho paura."),
                BattutaDialogo("A", "Nein, glaube ich nicht. Eine Füllung reicht. Mit Betäubung fühlen Sie nichts.", "No, credo di no. Basta un'otturazione. Con l'anestesia non sentirà nulla."),
                BattutaDialogo("B", "Darf ich nachher essen? Und wie oft die Zahnseide?", "Posso mangiare dopo? E ogni quanto il filo interdentale?"),
                BattutaDialogo("A", "Zwei Stunden warten. Und die Zahnseide bitte jeden Abend.", "Attenda due ore. E il filo interdentale ogni sera, per favore.")
            ),
            domande = listOf(
                DomandaDialogo("Quale dente fa male?", listOf("La molare in basso a destra", "L'incisivo in alto", "Il dente del giudizio", "Nessuno"), 0),
                DomandaDialogo("Che trattamento servirà?", listOf("Una devitalizzazione", "Un'otturazione", "L'estrazione", "Una pulizia"), 1),
                DomandaDialogo("Quando si può mangiare?", listOf("Subito", "Dopo due ore", "Il giorno dopo", "Mai con anestesia"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_11",
            titolo = "Il primo giorno al lavoro",
            livello = "B1",
            traduzione = "Un nuovo collega viene presentato in un ambulatorio di fisioterapia.",
            battute = listOf(
                BattutaDialogo("A", "Frau Rossi, das ist Alessandro, unser neuer Kollege.", "Signora Rossi, questo è Alessandro, il nostro nuovo collega."),
                BattutaDialogo("B", "Schön, dich kennenzulernen! Wo hast du vorher gearbeitet?", "Piacere di conoscerti! Dove hai lavorato prima?"),
                BattutaDialogo("C", "In einem Rehabilitationszentrum in Bozen. Zwei Jahre lang.", "In un centro di riabilitazione a Bolzano. Per due anni."),
                BattutaDialogo("B", "Und welche Bereiche kennst du gut?", "E quali settori conosci bene?"),
                BattutaDialogo("C", "Rückenprobleme, vor allem. Und auch Sportverletzungen.", "Problemi alla schiena, soprattutto. E anche lesioni sportive."),
                BattutaDialogo("B", "Perfekt. Wir bekommen viele Patienten mit Rückenschmerzen.", "Perfetto. Abbiamo molti pazienti con mal di schiena."),
                BattutaDialogo("A", "Am Montag kommt die erste Patientin um neun Uhr. Du machst die Anamnese.", "Lunedì arriva la prima paziente alle nove. Tu fai l'anamnesi."),
                BattutaDialogo("C", "Kein Problem. Wo finde ich die Unterlagen?", "Nessun problema. Dove trovo i documenti?")
            ),
            domande = listOf(
                DomandaDialogo("Dove ha lavorato prima Alessandro?", listOf("In un ospedale a Merano", "In un centro di riabilitazione a Bolzano", "In una palestra", "In un ambulatorio lombardo"), 1),
                DomandaDialogo("Quali pazienti sono più frequenti?", listOf("Con mal di schiena", "Con problemi cardiaci", "Bambini", "Post-operatori del ginocchio"), 0),
                DomandaDialogo("Quando arriva la prima paziente per Alessandro?", listOf("Giovedì alle 9", "Lunedì alle 9", "Mercoledì alle 11", "Sabato alle 8"), 1)
            )
        ),
        DialogoAscolto(
            id = "dial_12",
            titolo = "Colloquio di lavoro",
            livello = "B2",
            traduzione = "Selezione per la posizione di fisioterapista in una clinica sportiva.",
            battute = listOf(
                BattutaDialogo("A", "Herr Renato, danke für das Gespräch. Erzählen Sie kurz von sich.", "Signor Renato, grazie per il colloquio. Si presenti brevemente."),
                BattutaDialogo("B", "Gerne. Ich bin Physiotherapeut mit fünf Jahren Erfahrung, davon zwei in der Sportmedizin.", "Con piacere. Sono fisioterapista con cinque anni di esperienza, di cui due in medicina sportiva."),
                BattutaDialogo("A", "Was empfinden Sie als Ihre größte Stärke?", "Cosa considera la sua maggiore forza?"),
                BattutaDialogo("B", "Geduld und Flexibilität bei komplexen Fällen. Außerdem kommuniziere ich klar mit dem Team.", "Pazienza e flessibilità nei casi complessi. Inoltre comunico in modo chiaro con il team."),
                BattutaDialogo("A", "Wie stehen Sie zu Fortbildungen in diesem Jahr?", "Cosa pensa della formazione continua quest'anno?"),
                BattutaDialogo("B", "Sehr positiv. Ich plane eine Zusatzausbildung in Manueller Therapie.", "Molto positivamente. Ho in programma un percorso aggiuntivo in terapia manuale."),
                BattutaDialogo("A", "Wir melden uns bis Freitag. Möchten Sie noch etwas fragen?", "Ci faremo sentire entro venerdì. Vuole farci altre domande?"),
                BattutaDialogo("B", "Ja: wie groß ist das Team und wie viele Behandlungen macht man pro Tag?", "Sì: quanto è grande il team e quante sedute si fanno al giorno?")
            ),
            domande = listOf(
                DomandaDialogo("Da quanti anni lavora il candidato?", listOf("Due", "Cinque", "Dieci", "Venti"), 1),
                DomandaDialogo("Cosa pianifica come formazione aggiuntiva?", listOf("Terapia manuale", "Osteopatia", "Yoga terapeutico", "Bioingegneria"), 0),
                DomandaDialogo("Quando riceverà la risposta?", listOf("Entro lunedì", "Entro giovedì", "Entro venerdì", "Il mese prossimo"), 2)
            )
        )
    )
}