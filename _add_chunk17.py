import re, sys

def esc(s):
    return s.replace('\\', '\\\\').replace('"', '\\"')

# Hand-authored translations for Dialoghi and Sudtirol user-visible Italian strings.
# Keys must match source exactly.
pairs = {}

# Dialoghi titles
pairs.update({
    "Alla reception": "At the reception desk",
    "Anamnesi: mal di schiena": "Anamnesis: back pain",
    "Telefonata: appuntamento": "Phone call: appointment",
    "In pausa: colleghi austriaci": "On break: Austrian colleagues",
    "Esercizi a casa: istruzioni": "Home exercises: instructions",
    "Primo giorno di corso": "First day of class",
    "In farmacia": "At the pharmacy",
    "Al supermercato": "At the supermarket",
    "Dal medico di base": "At the GP",
    "Dal dentista": "At the dentist",
    "Il primo giorno al lavoro": "First day at work",
    "Colloquio di lavoro": "Job interview",
})

# Dialoghi traduzioni/descriptions
pairs.update({
    "Un paziente si registra in fisioterapia.": "A patient registers at physiotherapy.",
    "Prima seduta: il fisioterapista fa l'anamnesi a un paziente con lombalgia.": "First session: the physiotherapist takes the medical history of a patient with lower back pain.",
    "Chiamata per fissare un appuntamento di fisioterapia.": "A call to book a physiotherapy appointment.",
    "Due colleghi in mensa usano varianti austriache (Jänner, Erdapfel, Topfen).": "Two colleagues in the cafeteria use Austrian variants (Jänner, Erdapfel, Topfen).",
    "Il fisioterapista spiega gli esercizi da fare a casa per il ginocchio.": "The physiotherapist explains home exercises for the knee.",
    "Due partecipanti al corso di tedesco si conoscono presentandosi.": "Two participants in the German course get to know each other by introducing themselves.",
    "Un cliente chiede alla farmacia un rimedio per il raffreddore.": "A customer asks the pharmacy for a remedy for a cold.",
    "Un cliente chiede indicazioni al commesso e fa la spesa.": "A customer asks the shop assistant for directions and does the shopping.",
    "Consultazione con il medico di base per un problema alla schiena.": "Consultation with the GP about a back problem.",
    "Visita dentistica: dolore al dente e cura discussa con la dottoressa.": "Dental visit: tooth pain and treatment discussed with the doctor.",
    "Un nuovo collega viene presentato in un ambulatorio di fisioterapia.": "A new colleague is introduced at a physiotherapy clinic.",
    "Selezione per la posizione di fisioterapista in una clinica sportiva.": "Selection for a physiotherapist position at a sports clinic.",
})

# Read actual battuta translations, questions, options from source to keep exact keys
text = open('core/src/main/kotlin/com/alessandro/tedesco/data/Dialoghi.kt', encoding='utf-8').read()

batt_pairs = {
    "Buongiorno, ho un appuntamento alle dieci.": "Good morning, I have an appointment at ten o'clock.",
    "Buongiorno. Come si chiama, per favore?": "Good morning. What's your name, please?",
    "Mi chiamo Rossi. Mario Rossi.": "My name is Rossi. Mario Rossi.",
    "Signor Rossi. Ha l'impegnativa con sé?": "Mr Rossi. Do you have the referral with you?",
    "Sì, eccola. E il referto dell'ortopedico.": "Yes, here it is. And the orthopedist's report.",
    "Perfetto. Si accomodi pure. La terapista arriva tra poco.": "Perfect. Please have a seat. The therapist will be here shortly.",
    "Grazie. Quanto dura la prima seduta?": "Thank you. How long does the first session last?",
    "Circa 45 minuti. Facciamo l'anamnesi e un esame.": "About 45 minutes. We'll do the medical history and an assessment.",
    "Buongiorno, sono il signor Müller, il suo fisioterapista. Cosa la porta da noi?": "Good morning, I'm Mr Müller, your physiotherapist. What brings you to us?",
    "Ho mal di schiena da tre settimane, soprattutto la mattina.": "I've had back pain for three weeks, especially in the morning.",
    "Mi può indicare dove fa male esattamente?": "Can you show me exactly where it hurts?",
    "Qui, in basso alla schiena. A volte irradia alla gamba.": "Here, in the lower back. Sometimes it radiates to the leg.",
    "Ha intorpidimento o formicolio alla gamba?": "Do you have numbness or tingling in your leg?",
    "No, solo dolore. Ma di notte mi sveglio a volte.": "No, just pain. But I sometimes wake up at night.",
    "Capisco. Fa sport? Com'è la sua giornata lavorativa?": "I understand. Do you do sports? What is your working day like?",
    "Sto molto alla scrivania. Ma corro due volte a settimana.": "I sit at a desk a lot. But I run twice a week.",
    "Bene. Allora controlliamo la mobilità. Si pieghi in avanti per favore.": "Good. Then let's check your mobility. Please bend forward.",
}
pairs.update(batt_pairs)

# Questions/options - common ones
pairs.update({
    "A che ora è l'appuntamento?": "What time is the appointment?",
    "Cosa porta il paziente?": "What does the patient bring?",
    "Quanto dura la prima seduta?": "How long does the first session last?",
    "Solo l'impegnativa": "Only the referral",
    "Impegnativa e referto": "Referral and report",
    "Solo il referto": "Only the report",
    "Niente": "Nothing",
    "30 minuti": "30 minutes",
    "45 minuti": "45 minutes",
    "60 minuti": "60 minutes",
    "90 minuti": "90 minutes",
    "Da quanto tempo ha dolore il paziente?": "How long has the patient had pain?",
    "Una settimana": "One week",
    "Due settimane": "Two weeks",
    "Tre settimane": "Three weeks",
    "Un mese": "One month",
    "Dove irradia il dolore?": "Where does the pain radiate?",
    "Al braccio": "To the arm",
    "Alla gamba": "To the leg",
    "Al collo": "To the neck",
    "Non irradia": "It doesn't radiate",
    "Cosa fa il paziente di lavoro?": "What does the patient do for work?",
    "Lavoro fisico": "Physical work",
    "Lavoro alla scrivania": "Desk work",
    "Insegnante": "Teacher",
    "In pensione": "Retired",
})

# Build chunk
lines = []
lines.append('    private fun esattoChunk17(): Map<String, String> = mapOf(')
for k,v in pairs.items():
    lines.append(f'            "{esc(k)}" to "{esc(v)}",')
lines.append('    )')

chunk = '\n'.join(lines)

path = 'core/src/main/kotlin/com/alessandro/tedesco/i18n/Traduttore.kt'
s = open(path, encoding='utf-8').read()

marker = '\n    private val esatto: Map<String, String> by lazy {'
if marker not in s:
    print('marker missing'); sys.exit(1)
s = s.replace(marker, '\n' + chunk + '\n' + marker)

# wire chunk17
s = s.replace('        r.putAll(esattoChunk16())\n        r\n    }',
              '        r.putAll(esattoChunk16())\n        r.putAll(esattoChunk17())\n        r\n    }')

open(path, 'w', encoding='utf-8').write(s)
print('added', len(pairs))
