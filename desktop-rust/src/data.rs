use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::collections::{BTreeMap, HashMap, HashSet};
use std::fs;
use std::path::PathBuf;
use chrono::{Duration, Local, NaiveDate, Utc};

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct Preferenze {
    #[serde(alias = "temaScuro")]
    pub tema_scuro: bool,
    #[serde(alias = "temaModalita")]
    pub tema_modalita: String,
    #[serde(alias = "temaColore")]
    pub tema_colore: String,
    #[serde(alias = "profiloAttivoId")]
    pub profilo_attivo_id: Option<String>,
    #[serde(alias = "githubToken")]
    pub github_token: String,
    #[serde(alias = "githubRepo")]
    pub github_repo: String,
    #[serde(alias = "ultimoBackup")]
    pub ultimo_backup: i64,
    #[serde(alias = "ultimoHashCloud")]
    pub ultimo_hash_cloud: String,
    #[serde(alias = "ultimaSincronizzazioneCloud")]
    pub ultima_sincronizzazione_cloud: i64,
    pub lingua: String,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiliRepository {
    #[serde(alias = "profiloAttivoId")]
    pub profilo_attivo_id: Option<String>,
    pub profili: HashMap<String, ProfiloUtente>,
    #[serde(alias = "profiliEliminati")]
    pub profili_eliminati: Vec<String>,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiloUtente {
    pub id: String,
    pub config: ProfiloConfig,
    pub stato: ProfiloStato,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiloConfig {
    #[serde(alias = "nomeVisualizzato")]
    pub nome_visualizzato: String,
    #[serde(alias = "livelloIniziale")]
    pub livello_iniziale: String,
    #[serde(alias = "contestoUtente")]
    pub contesto_utente: String,
    #[serde(alias = "mostraContestoMedico")]
    pub mostra_contesto_medico: bool,
    #[serde(alias = "feedUrl")]
    pub feed_url: String,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiloStato {
    pub parole: Vec<WordEntity>,
    pub reviews: HashMap<String, ReviewEntity>,
    #[serde(alias = "ripassiLog")]
    pub ripassi_log: Vec<RipassoLog>,
    #[serde(alias = "padronanzaGrammatica")]
    pub padronanza_grammatica: HashMap<String, Value>,
    #[serde(alias = "streakGiorni")]
    pub streak_giorni: i32,
    #[serde(alias = "streakRecord")]
    pub streak_record: i32,
    #[serde(alias = "ultimoGiornoStudio")]
    pub ultimo_giorno_studio: Option<String>,
    pub progresso: Progresso,
    /// Quaderno degli errori: SRS separato dal lessico (portato dall'app mobile).
    #[serde(default, alias = "erroriQuaderno")]
    pub errori_quaderno: Vec<ErroreEntity>,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

// ── Quaderno degli errori (error notebook, SRS separato) ──

/// Un errore reale fatto dallo studente (articolo, caso, ordine parole, ...).
#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ErroreEntity {
    pub id: String,
    pub tipo: String,
    pub domanda: String,
    #[serde(alias = "rispostaCorretta")]
    pub risposta_corretta: String,
    #[serde(alias = "rispostaData")]
    pub risposta_data: String,
    pub spiegazione: String,
    pub livello: String,
    #[serde(default = "now_millis")]
    pub creato: i64,
    #[serde(alias = "ultimaVolta", default = "now_millis")]
    pub ultima_volta: i64,
    #[serde(default)]
    pub ripetizioni: i32,
    #[serde(alias = "intervalloGiorni", default)]
    pub intervallo_giorni: i32,
    #[serde(alias = "prossimaRipasso", default)]
    pub prossima_ripasso: i64,
    #[serde(default)]
    pub risolto: bool,
}

fn now_millis() -> i64 {
    Utc::now().timestamp_millis()
}

impl ErroreEntity {
    /// Chiave stabile per non duplicare lo stesso errore.
    pub fn chiave(&self) -> String {
        format!(
            "{}|{}|{}",
            self.tipo.trim().to_lowercase(),
            normalizza_risposta(&self.domanda),
            normalizza_risposta(&self.risposta_corretta)
        )
    }
}

fn normalizza_risposta(s: &str) -> String {
    s.trim().to_lowercase().split_whitespace().collect::<Vec<_>>().join(" ")
}

/// Ripetizione spaziata degli errori (piu' semplice dell'SM-2 del lessico).
pub struct ErroriSRS;

impl ErroriSRS {
    pub const INTERVALLI: [i32; 7] = [1, 3, 7, 16, 35, 75, 150];
    const GIORNO_MS: i64 = 24 * 60 * 60 * 1000;
    const SOGLIA_RISOLTO: i32 = 5;

    pub fn nuovo(
        tipo: &str,
        domanda: &str,
        risposta_corretta: &str,
        risposta_data: &str,
        spiegazione: &str,
        livello: &str,
    ) -> ErroreEntity {
        let now = now_millis();
        ErroreEntity {
            id: format!("err_{}_{}_{}", now, hash_u32(domanda), hash_u32(risposta_corretta)),
            tipo: tipo.to_string(),
            domanda: domanda.to_string(),
            risposta_corretta: risposta_corretta.to_string(),
            risposta_data: risposta_data.to_string(),
            spiegazione: spiegazione.to_string(),
            livello: livello.to_string(),
            creato: now,
            ultima_volta: now,
            ripetizioni: 0,
            intervallo_giorni: 1,
            prossima_ripasso: now + Self::GIORNO_MS,
            risolto: false,
        }
    }

    /// Aggiorna l'errore dopo un ripasso: saputo = richiamo riuscito.
    pub fn rispondi(mut errore: ErroreEntity, saputo: bool) -> ErroreEntity {
        let now = now_millis();
        errore.ultima_volta = now;
        if saputo {
            errore.ripetizioni += 1;
            let giorni = Self::INTERVALLI
                .get((errore.ripetizioni - 1) as usize)
                .copied()
                .unwrap_or(*Self::INTERVALLI.last().unwrap());
            errore.intervallo_giorni = giorni;
            errore.prossima_ripasso = now + giorni as i64 * Self::GIORNO_MS;
            errore.risolto = errore.ripetizioni >= Self::SOGLIA_RISOLTO;
        } else {
            errore.ripetizioni = 0;
            errore.intervallo_giorni = 1;
            errore.prossima_ripasso = now + Self::GIORNO_MS;
            errore.risolto = false;
        }
        errore
    }

    pub fn da_ripassare(errore: &ErroreEntity) -> bool {
        !errore.risolto && errore.prossima_ripasso <= now_millis()
    }

    pub fn giorni_alla_scadenza(errore: &ErroreEntity) -> i32 {
        if errore.risolto { return -1; }
        let diff = errore.prossima_ripasso - now_millis();
        if diff <= 0 { return 0; }
        ((diff + Self::GIORNO_MS - 1) / Self::GIORNO_MS) as i32
    }
}

fn hash_u32(s: &str) -> u32 {
    use std::collections::hash_map::DefaultHasher;
    use std::hash::{Hash, Hasher};
    let mut h = DefaultHasher::new();
    s.hash(&mut h);
    h.finish() as u32
}

impl Store {
    /// Aggiunge un errore al quaderno, evitando duplicati per chiave stabile.
    pub fn aggiungi_errore(&mut self, errore: ErroreEntity) -> Result<(), String> {
        let profilo = self.active_profile_mut().ok_or("Nessun profilo attivo")?;
        let chiave = errore.chiave();
        if profilo.stato.errori_quaderno.iter().any(|e| e.chiave() == chiave) {
            return Ok(());
        }
        profilo.stato.errori_quaderno.push(errore);
        Ok(())
    }

    /// Errori da ripassare (non risolti e in scadenza).
    pub fn errori_da_ripassare(&self) -> Vec<ErroreEntity> {
        self.active_profile()
            .map(|p| p.stato.errori_quaderno.iter().filter(|e| ErroriSRS::da_ripassare(e)).cloned().collect())
            .unwrap_or_default()
    }

    /// Registra la risposta a un errore aggiornando l'SRS.
    pub fn rispondi_errore(&mut self, id: &str, saputo: bool) -> Result<(), String> {
        let profilo = self.active_profile_mut().ok_or("Nessun profilo attivo")?;
        if let Some(e) = profilo.stato.errori_quaderno.iter_mut().find(|e| e.id == id) {
            *e = ErroriSRS::rispondi(e.clone(), saputo);
        }
        Ok(())
    }

    /// Rimuove un errore dal quaderno.
    pub fn rimuovi_errore(&mut self, id: &str) {
        if let Some(profilo) = self.active_profile_mut() {
            profilo.stato.errori_quaderno.retain(|e| e.id != id);
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct WordEntity {
    pub id: String,
    pub german: String,
    pub italian: String,
    #[serde(default)]
    pub example: String,
    pub article: Option<String>,
    #[serde(default)]
    pub level: String,
    #[serde(default)]
    pub tags: String,
    #[serde(default)]
    pub archived: bool,
    #[serde(alias = "createdAt", default)]
    pub created_at: i64,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

impl WordEntity {
    pub fn label(&self) -> String {
        match &self.article {
            Some(a) if !a.trim().is_empty() => format!("{} {}", a.trim(), self.german),
            _ => self.german.clone(),
        }
    }

    pub fn matches_query(&self, q: &str) -> bool {
        let q = q.trim().to_lowercase();
        if q.is_empty() { return true; }
        self.german.to_lowercase().contains(&q)
            || self.italian.to_lowercase().contains(&q)
            || self.example.to_lowercase().contains(&q)
            || self.tags.to_lowercase().contains(&q)
            || self.article.as_deref().unwrap_or("").to_lowercase().contains(&q)
    }

    pub fn normalized_article(&self) -> Option<String> {
        self.article.as_ref().map(|a| a.trim().to_lowercase()).filter(|a| !a.is_empty())
    }

    pub fn has_gender_article(&self) -> bool {
        matches!(self.normalized_article().as_deref(), Some("der" | "die" | "das"))
    }
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub enum ExerciseKind {
    MeaningChoice,
    GermanChoice,
    ArticleQuiz,
    Typing,
}

#[derive(Debug, Clone)]
pub struct ExerciseQuestion {
    pub word: WordEntity,
    pub kind: ExerciseKind,
    pub prompt: String,
    pub options: Vec<String>,
    pub answer: String,
}

fn simple_shuffle<T: Clone>(items: &mut [T], seed: u64) {
    if items.len() < 2 { return; }
    let mut x = if seed == 0 { 0x9E3779B97F4A7C15 } else { seed };
    for i in (1..items.len()).rev() {
        x ^= x << 13;
        x ^= x >> 7;
        x ^= x << 17;
        let j = (x as usize) % (i + 1);
        items.swap(i, j);
    }
}

fn level_rank(level: &str) -> i32 {
    match level.trim().to_uppercase().as_str() {
        "A1" => 1, "A2" => 2, "B1" => 3, "B2" => 4, "C1" => 5, "C2" => 6, _ => 0,
    }
}

pub fn words_up_to_level(words: &[WordEntity], max_level: &str, require_article: bool) -> Vec<WordEntity> {
    let cap = level_rank(max_level);
    let mut seen: HashSet<String> = HashSet::new();
    let mut out = Vec::new();
    for w in words.iter().filter(|w| !w.archived) {
        if require_article && !w.has_gender_article() { continue; }
        let lr = level_rank(&w.level);
        if cap > 0 && (lr == 0 || lr > cap) { continue; }
        let key = format!("{}|{}", w.normalized_article().unwrap_or_default(), w.german.trim().to_lowercase());
        if seen.insert(key) {
            out.push(w.clone());
        }
    }
    out
}

pub fn build_article_quiz(words: &[WordEntity], max_level: &str, seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    let mut pool = words_up_to_level(words, max_level, true);
    simple_shuffle(&mut pool, seed);
    pool.truncate(limit);
    pool.into_iter().map(|w| {
        let ans = w.normalized_article().unwrap_or_default();
        ExerciseQuestion {
            prompt: format!("Choose the article: {}", w.german),
            options: vec!["der".into(), "die".into(), "das".into()],
            answer: ans,
            word: w,
            kind: ExerciseKind::ArticleQuiz,
        }
    }).collect()
}

pub fn build_meaning_quiz(words: &[WordEntity], seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    let mut pool: Vec<_> = words.iter().filter(|w| !w.archived && !w.italian.trim().is_empty()).cloned().collect();
    simple_shuffle(&mut pool, seed ^ 0xA5A5A5A5);
    pool.truncate(limit.max(4));
    let all = pool.clone();
    if all.len() < 2 { return vec![]; }
    pool.into_iter().take(limit).map(|w| {
        let mut opts = vec![w.italian.clone()];
        let mut i = 0u64;
        while opts.len() < 4 && opts.len() < all.len() {
            let pick = &all[((seed.wrapping_add(i).wrapping_mul(1103515245) as usize) + w.id.len() + opts.len()) % all.len()];
            if !opts.iter().any(|o| o.eq_ignore_ascii_case(&pick.italian)) {
                opts.push(pick.italian.clone());
            }
            i += 1;
            if i > 50 { break; }
        }
        simple_shuffle(&mut opts, seed ^ (w.german.len() as u64));
        ExerciseQuestion {
            prompt: format!("What does '{}' mean?", w.label()),
            options: opts,
            answer: w.italian.clone(),
            word: w,
            kind: ExerciseKind::MeaningChoice,
        }
    }).collect()
}

pub fn build_german_choice_quiz(words: &[WordEntity], seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    let mut pool: Vec<_> = words.iter().filter(|w| !w.archived && !w.italian.trim().is_empty() && !w.german.trim().is_empty()).cloned().collect();
    simple_shuffle(&mut pool, seed ^ 0x6E7243);
    pool.truncate(limit.max(4));
    let all = pool.clone();
    if all.len() < 2 { return vec![]; }
    pool.into_iter().take(limit).map(|w| {
        let correct = w.label();
        let mut opts = vec![correct.clone()];
        let mut i = 0u64;
        while opts.len() < 4 && opts.len() < all.len() {
            let pick = &all[((seed.wrapping_add(i).wrapping_mul(2654435761) as usize) + w.id.len() + opts.len()) % all.len()];
            let cand = pick.label();
            if !opts.iter().any(|o| o.eq_ignore_ascii_case(&cand)) {
                opts.push(cand);
            }
            i += 1;
            if i > 50 { break; }
        }
        simple_shuffle(&mut opts, seed ^ (w.italian.len() as u64));
        ExerciseQuestion {
            prompt: format!("Choose the German for: {}", w.italian),
            options: opts,
            answer: correct,
            word: w,
            kind: ExerciseKind::GermanChoice,
        }
    }).collect()
}

pub fn build_typing_quiz(words: &[WordEntity], seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    let mut pool: Vec<_> = words.iter().filter(|w| !w.archived && !w.italian.trim().is_empty()).cloned().collect();
    simple_shuffle(&mut pool, seed ^ 0xC0FFEE);
    pool.truncate(limit);
    pool.into_iter().map(|w| ExerciseQuestion {
        prompt: format!("Type the German for: {}", w.italian),
        options: vec![],
        answer: w.label(),
        word: w,
        kind: ExerciseKind::Typing,
    }).collect()
}

pub fn check_typing_answer(expected: &str, given: &str) -> bool {
    let norm = |s: &str| s.trim().to_lowercase().split_whitespace().collect::<Vec<_>>().join(" ");
    norm(expected) == norm(given)
}

fn cloze_blank(example: &str, german: &str) -> Option<String> {
    let ex = example.trim();
    let g = german.trim();
    if ex.is_empty() || g.is_empty() { return None; }
    let ex_lower = ex.to_lowercase();
    let g_lower = g.to_lowercase();
    if let Some(idx) = ex_lower.find(&g_lower) {
        let mut out = ex.to_string();
        out.replace_range(idx..idx + g.len(), "_____");
        return Some(out);
    }
    // try without article prefix in german label
    let parts: Vec<&str> = g.split_whitespace().collect();
    if parts.len() >= 2 {
        let bare = parts[1..].join(" ");
        let b_lower = bare.to_lowercase();
        if let Some(idx) = ex_lower.find(&b_lower) {
            let mut out = ex.to_string();
            out.replace_range(idx..idx + bare.len(), "_____");
            return Some(out);
        }
    }
    None
}

pub fn build_cloze_quiz(words: &[WordEntity], seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    let mut pool: Vec<_> = words.iter()
        .filter(|w| !w.archived && !w.example.trim().is_empty() && !w.italian.trim().is_empty())
        .cloned()
        .collect();
    simple_shuffle(&mut pool, seed ^ 0xC102E);
    let mut out = Vec::new();
    for w in pool {
        if out.len() >= limit { break; }
        if let Some(sentence) = cloze_blank(&w.example, &w.german) {
            out.push(ExerciseQuestion {
                prompt: format!("Complete: {sentence}"),
                options: vec![],
                answer: w.german.clone(),
                word: w,
                kind: ExerciseKind::Typing,
            });
        }
    }
    out
}

pub fn build_mixed_quiz(words: &[WordEntity], max_level: &str, seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    let mut qs = Vec::new();
    let n = (limit / 4).max(1);
    qs.extend(build_article_quiz(words, max_level, seed, n + 1));
    qs.extend(build_meaning_quiz(words, seed ^ 1, n + 1));
    qs.extend(build_german_choice_quiz(words, seed ^ 4, n + 1));
    qs.extend(build_cloze_quiz(words, seed ^ 2, n + 1));
    qs.extend(build_typing_quiz(words, seed ^ 3, n + 1));
    simple_shuffle(&mut qs, seed ^ 0x51EED);
    qs.truncate(limit);
    qs
}

/// Guided session: prioritizes due/new words (from provided selection) and builds a mixed
/// set of recognition + production exercises (meaning, German choice, typing, cloze; plus
/// article quiz when applicable). Profile-compatible: callers pass words derived from
/// due reviews/new words/active pool.
pub fn build_guided_session(words: &[WordEntity], max_level: &str, seed: u64, limit: usize) -> Vec<ExerciseQuestion> {
    if words.is_empty() || limit == 0 { return vec![]; }
    let mut qs = Vec::new();
    let n = (limit / 5).max(1);
    qs.extend(build_meaning_quiz(words, seed ^ 0x11, (n + 2).min(limit)));
    qs.extend(build_german_choice_quiz(words, seed ^ 0x22, (n + 1).min(limit)));
    qs.extend(build_typing_quiz(words, seed ^ 0x33, (n + 1).min(limit)));
    qs.extend(build_cloze_quiz(words, seed ^ 0x44, (n + 1).min(limit)));
    qs.extend(build_article_quiz(words, max_level, seed ^ 0x55, (n + 1).min(limit)));
    // stable-ish dedupe by (prompt, answer)
    let mut seen = std::collections::HashSet::new();
    qs.retain(|q| seen.insert(format!("{}||{}", q.prompt, q.answer)));
    simple_shuffle(&mut qs, seed ^ 0xA11CE);
    qs.truncate(limit);
    qs
}

#[derive(Debug, Clone, Serialize, Deserialize)]
#[serde(default)]
pub struct ReviewEntity {
    #[serde(alias = "wordId")]
    pub word_id: String,
    #[serde(alias = "easeFactor")]
    pub ease_factor: f32,
    #[serde(alias = "intervalDays")]
    pub interval_days: i32,
    pub repetitions: i32,
    pub lapses: i32,
    #[serde(alias = "dueAt")]
    pub due_at: i64,
    #[serde(alias = "lastReviewedAt")]
    pub last_reviewed_at: Option<i64>,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

impl Default for ReviewEntity {
    fn default() -> Self {
        Self {
            word_id: String::new(),
            ease_factor: 2.5,
            interval_days: 0,
            repetitions: 0,
            lapses: 0,
            due_at: 0,
            last_reviewed_at: None,
            extra: HashMap::new(),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct RipassoLog {
    #[serde(alias = "wordId")]
    pub word_id: String,
    pub timestamp: i64,
    #[serde(alias = "corretta")]
    pub corretta: bool,
    #[serde(default)]
    pub verifica: bool,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct Progresso {
    #[serde(alias = "livelloCorrente", default = "default_livello")]
    pub livello_corrente: String,
    #[serde(alias = "obiettivoLivello", default = "default_obiettivo")]
    pub obiettivo_livello: String,
    #[serde(alias = "lezioneCorrente", default)]
    pub lezione_corrente: i32,
    #[serde(alias = "ultimaAttivita", default)]
    pub ultima_attivita: i64,
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
}

fn default_livello() -> String { "A1".into() }
fn default_obiettivo() -> String { "B2".into() }

pub const SRS_INTERVALS: [i32; 8] = [1, 3, 7, 16, 35, 75, 150, 300];
pub const DAY_MS: i64 = 24 * 60 * 60 * 1000;

pub fn nuova_review(word_id: &str, now: i64) -> ReviewEntity {
    ReviewEntity { word_id: word_id.to_string(), due_at: now, ..Default::default() }
}

pub fn review_con_risposta(mut review: ReviewEntity, saputa: bool, now: i64) -> ReviewEntity {
    if saputa {
        let idx = review.repetitions.max(0) as usize;
        let next = *SRS_INTERVALS.get(idx).unwrap_or(SRS_INTERVALS.last().unwrap());
        review.repetitions += 1;
        review.interval_days = next;
        review.ease_factor = (review.ease_factor + 0.05).min(3.0);
        review.due_at = now + next as i64 * DAY_MS;
        review.last_reviewed_at = Some(now);
    } else {
        review.repetitions = 0;
        review.interval_days = 1;
        review.lapses += 1;
        review.ease_factor = (review.ease_factor - 0.2).max(1.3);
        review.due_at = now + DAY_MS;
        review.last_reviewed_at = Some(now);
    }
    review
}

fn today_str() -> String {
    chrono::Local::now().format("%Y-%m-%d").to_string()
}

pub fn registra_studio(stato: &mut ProfiloStato, now: i64) {
    let oggi = today_str();
    if stato.ultimo_giorno_studio.as_deref() == Some(&oggi) {
        stato.progresso.ultima_attivita = now;
        return;
    }
    let ieri = (chrono::Local::now() - chrono::Duration::days(1)).format("%Y-%m-%d").to_string();
    let nuovo = if stato.ultimo_giorno_studio.as_deref() == Some(&ieri) {
        stato.streak_giorni + 1
    } else {
        1
    };
    stato.streak_giorni = nuovo;
    if nuovo > stato.streak_record { stato.streak_record = nuovo; }
    stato.ultimo_giorno_studio = Some(oggi);
    stato.progresso.ultima_attivita = now;
}

#[derive(Debug, Clone)]
pub struct Store {
    pub base_dir: PathBuf,
    pub profili_path: PathBuf,
    pub preferenze_path: PathBuf,
    pub repo: ProfiliRepository,
    pub prefs: Preferenze,
}

impl Store {
    pub fn load_default() -> Self {
        let base = dirs::home_dir()
            .unwrap_or_else(|| PathBuf::from("."))
            .join(".tedesco-desktop");
        Self::load_from(base)
    }

    pub fn load_from(base_dir: PathBuf) -> Self {
        let profili_path = base_dir.join("profili.json");
        let preferenze_path = base_dir.join("preferenze.json");
        let repo = read_json(&profili_path).unwrap_or_default();
        let prefs = read_json(&preferenze_path).unwrap_or_default();
        Self { base_dir, profili_path, preferenze_path, repo, prefs }
    }

    pub fn save(&self) -> Result<(), String> {
        fs::create_dir_all(&self.base_dir).map_err(|e| e.to_string())?;
        write_json(&self.profili_path, &self.repo)?;
        write_json(&self.preferenze_path, &self.prefs)?;
        Ok(())
    }

    pub fn active_profile(&self) -> Option<&ProfiloUtente> {
        let id = self.repo.profilo_attivo_id.as_ref()?;
        self.repo.profili.get(id)
    }

    pub fn active_profile_mut(&mut self) -> Option<&mut ProfiloUtente> {
        let id = self.repo.profilo_attivo_id.clone()?;
        self.repo.profili.get_mut(&id)
    }

    pub fn set_active_profile(&mut self, id: &str) {
        if self.repo.profili.contains_key(id) {
            self.repo.profilo_attivo_id = Some(id.to_string());
            self.prefs.profilo_attivo_id = Some(id.to_string());
        }
    }

    pub fn create_profile(&mut self, nome: &str) -> String {
        let id = format!("profilo_{}", chrono::Utc::now().timestamp_millis());
        let p = ProfiloUtente {
            id: id.clone(),
            config: ProfiloConfig {
                nome_visualizzato: if nome.trim().is_empty() { "Profilo".into() } else { nome.trim().into() },
                livello_iniziale: "A1".into(),
                contesto_utente: String::new(),
                mostra_contesto_medico: false,
                feed_url: String::new(),
                extra: HashMap::new(),
            },
            stato: ProfiloStato::default(),
            extra: HashMap::new(),
        };
        self.repo.profili.insert(id.clone(), p);
        self.set_active_profile(&id);
        let _ = self.save();
        id
    }

    pub fn due_reviews(&self, now_ms: i64, limit: usize) -> Vec<WordEntity> {
        let Some(p) = self.active_profile() else { return vec![]; };
        let active: HashMap<&str, &WordEntity> = p.stato.parole.iter()
            .filter(|w| !w.archived)
            .map(|w| (w.id.as_str(), w))
            .collect();
        let mut items: Vec<_> = p.stato.reviews.values()
            .filter(|r| r.due_at <= now_ms)
            .filter_map(|r| active.get(r.word_id.as_str()).copied().cloned().map(|w| (r.due_at, w)))
            .collect();
        items.sort_by_key(|(due, _)| *due);
        items.into_iter().take(limit).map(|(_, w)| w).collect()
    }

    pub fn new_words(&self, limit: usize) -> Vec<WordEntity> {
        let Some(p) = self.active_profile() else { return vec![]; };
        let reviewed: HashSet<_> = p.stato.reviews.iter()
            .filter(|(_, r)| r.repetitions > 0)
            .map(|(id, _)| id.as_str())
            .collect();
        let mut words: Vec<_> = p.stato.parole.iter()
            .filter(|w| !w.archived && !reviewed.contains(w.id.as_str()))
            .cloned()
            .collect();
        words.sort_by_key(|w| w.created_at);
        words.truncate(limit);
        words
    }

    /// Words that need extra practice: reviewed words with lapses/low ease and/or recent incorrect answers.
    /// Profile-compatible (derived from saved reviews and ripassi_log).
    pub fn difficult_words(&self, limit: usize) -> Vec<WordEntity> {
        let Some(p) = self.active_profile() else { return vec![]; };
        let mut score: HashMap<String, i64> = HashMap::new();
        for (id, r) in &p.stato.reviews {
            let mut s = 0i64;
            if r.lapses > 0 { s += (r.lapses as i64) * 3; }
            if r.ease_factor < 2.3 { s += 2; }
            if r.repetitions == 0 { s += 1; }
            if r.interval_days <= 1 { s += 1; }
            if s > 0 { score.insert(id.clone(), s); }
        }
        let start = p.stato.ripassi_log.len().saturating_sub(200);
        for e in &p.stato.ripassi_log[start..] {
            if !e.corretta {
                *score.entry(e.word_id.clone()).or_insert(0) += 2;
            }
        }
        let mut ids: Vec<(String, i64)> = score.into_iter().collect();
        ids.sort_by(|a, b| b.1.cmp(&a.1).then_with(|| a.0.cmp(&b.0)));
        let wanted: HashSet<String> = ids.into_iter().take(limit).map(|(id, _)| id).collect();
        let mut out: Vec<WordEntity> = p.stato.parole.iter()
            .filter(|w| !w.archived && wanted.contains(&w.id))
            .cloned()
            .collect();
        out.sort_by_key(|w| -(p.stato.reviews.get(&w.id).map(|r| r.lapses as i64).unwrap_or(0)));
        out.truncate(limit);
        out
    }

    pub fn stats(&self) -> (usize, usize, usize, i32) {
        let Some(p) = self.active_profile() else { return (0,0,0,0); };
        let total = p.stato.parole.iter().filter(|w| !w.archived).count();
        let reviewed = p.stato.reviews.values().filter(|r| r.repetitions > 0).count();
        let now = chrono::Utc::now().timestamp_millis();
        let due = p.stato.reviews.values().filter(|r| r.due_at <= now).count();
        (total, reviewed, due, p.stato.streak_giorni)
    }

    pub fn mature_count(&self) -> usize {
        let Some(p) = self.active_profile() else { return 0; };
        p.stato.reviews.values().filter(|r| r.interval_days >= 21 || r.repetitions >= 5).count()
    }

    pub fn word_level_counts(&self) -> BTreeMap<String, usize> {
        let mut out: BTreeMap<String, usize> = BTreeMap::new();
        let Some(p) = self.active_profile() else { return out; };
        for w in p.stato.parole.iter().filter(|w| !w.archived) {
            let lvl = if w.level.trim().is_empty() { "â€”".to_string() } else { w.level.trim().to_uppercase() };
            *out.entry(lvl).or_insert(0) += 1;
        }
        out
    }

    pub fn search_words(&self, query: &str, level: Option<&str>, include_archived: bool, limit: usize) -> Vec<WordEntity> {
        let Some(p) = self.active_profile() else { return vec![]; };
        let lvl = level.map(|s| s.trim().to_uppercase()).filter(|s| !s.is_empty() && s != "ALL" && s != "â€”");
        let mut words: Vec<_> = p.stato.parole.iter()
            .filter(|w| include_archived || !w.archived)
            .filter(|w| lvl.as_ref().map(|l| w.level.trim().to_uppercase() == *l).unwrap_or(true))
            .filter(|w| w.matches_query(query))
            .cloned()
            .collect();
        words.sort_by(|a, b| a.german.to_lowercase().cmp(&b.german.to_lowercase()));
        words.truncate(limit);
        words
    }

    pub fn add_word(&mut self, german: &str, italian: &str, article: Option<String>, level: &str, example: &str) -> Result<String, String> {
        let german = german.trim();
        let italian = italian.trim();
        if german.is_empty() { return Err("German word is required".into()); }
        if italian.is_empty() { return Err("Translation is required".into()); }
        let now = chrono::Utc::now().timestamp_millis();
        let id = format!("custom_{}_{}", now, german.chars().filter(|c| c.is_alphanumeric()).take(8).collect::<String>().to_lowercase());
        let mut extra = HashMap::new();
        extra.insert("source".to_string(), Value::String("CUSTOM".into()));
        let w = WordEntity {
            id: id.clone(),
            german: german.to_string(),
            italian: italian.to_string(),
            example: example.trim().to_string(),
            article: article.map(|a| a.trim().to_string()).filter(|a| !a.is_empty()),
            level: level.trim().to_uppercase(),
            tags: "custom".to_string(),
            archived: false,
            created_at: now,
            extra,
        };
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        if p.stato.parole.iter().any(|x| x.german.eq_ignore_ascii_case(german) && x.article.as_deref().unwrap_or("") == w.article.as_deref().unwrap_or("")) {
            return Err("Word already exists".into());
        }
        p.stato.parole.push(w);
        self.save()?;
        Ok(id)
    }

    pub fn set_word_archived(&mut self, word_id: &str, archived: bool) -> Result<(), String> {
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let Some(w) = p.stato.parole.iter_mut().find(|w| w.id == word_id) else {
            return Err("Word not found".into());
        };
        w.archived = archived;
        self.save()
    }

    pub fn answer_review(&mut self, word_id: &str, saputa: bool) -> Result<(), String> {
        let now = chrono::Utc::now().timestamp_millis();
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let existing = p.stato.reviews.get(word_id).cloned().unwrap_or_else(|| nuova_review(word_id, now));
        let updated = review_con_risposta(existing, saputa, now);
        p.stato.reviews.insert(word_id.to_string(), updated);
        p.stato.ripassi_log.push(RipassoLog {
            word_id: word_id.to_string(),
            timestamp: now,
            corretta: saputa,
            verifica: false,
            extra: HashMap::new(),
        });
        if p.stato.ripassi_log.len() > 5000 {
            let drop_n = p.stato.ripassi_log.len() - 5000;
            p.stato.ripassi_log.drain(0..drop_n);
        }
        registra_studio(&mut p.stato, now);
        self.save()
    }

    /// Adds missing SRS review entries for active words in the selected level (or all levels).
    /// Returns how many words were added to review.
    pub fn add_words_to_review(&mut self, level: Option<&str>) -> Result<usize, String> {
        let now = chrono::Utc::now().timestamp_millis();
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let mut added = 0usize;
        let ids: Vec<String> = p.stato.parole.iter()
            .filter(|w| !w.archived)
            .filter(|w| match level {
                Some(lv) if !lv.is_empty() && lv != "ALL" && lv != "â€”" => w.level.trim().eq_ignore_ascii_case(lv.trim()),
                _ => true,
            })
            .map(|w| w.id.clone())
            .collect();
        for id in ids {
            if !p.stato.reviews.contains_key(&id) {
                p.stato.reviews.insert(id.clone(), nuova_review(&id, now));
                added += 1;
            }
        }
        if added > 0 {
            self.save()?;
        }
        Ok(added)
    }

    /// Adds missing SRS review entries for the given word IDs (active profile). Returns how many were added.
    pub fn add_word_ids_to_review(&mut self, ids: &[String]) -> Result<usize, String> {
        let now = chrono::Utc::now().timestamp_millis();
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let mut added = 0usize;
        let valid: std::collections::HashSet<String> = p.stato.parole.iter()
            .filter(|w| !w.archived)
            .map(|w| w.id.clone())
            .collect();
        for id in ids {
            if valid.contains(id) && !p.stato.reviews.contains_key(id) {
                p.stato.reviews.insert(id.clone(), nuova_review(id, now));
                added += 1;
            }
        }
        if added > 0 {
            self.save()?;
        }
        Ok(added)
    }

    pub fn grammar_mastery_summaries(&self) -> Vec<GrammarMasterySummary> {
        let Some(p) = self.active_profile() else { return vec![]; };
        let mut out: Vec<_> = p.stato.padronanza_grammatica.iter().map(|(id, v)| {
            GrammarMasterySummary::from_value(id, v)
        }).collect();
        out.sort_by(|a, b| b.percentuale.cmp(&a.percentuale).then_with(|| b.tentativi.cmp(&a.tentativi)).then_with(|| a.id.cmp(&b.id)));
        out
    }

    pub fn export_bundle_to(&self, path: &PathBuf) -> Result<(), String> {
        let bundle = serde_json::json!({
            "format": "openlingo-rust-backup",
            "version": 1,
            "profili": self.repo,
            "preferenze": self.prefs,
        });
        write_json(path, &bundle)
    }

    pub fn import_bundle_from(&mut self, path: &PathBuf) -> Result<(), String> {
        let data = fs::read_to_string(path).map_err(|e| e.to_string())?;
        let v: Value = serde_json::from_str(&data).map_err(|e| e.to_string())?;
        if let Some(p) = v.get("profili") {
            self.repo = serde_json::from_value(p.clone()).map_err(|e| e.to_string())?;
        } else if v.get("profiliAttivoId").is_some() || v.get("profiloAttivoId").is_some() || v.get("profili").is_some() {
            self.repo = serde_json::from_value(v.clone()).map_err(|e| e.to_string())?;
        }
        if let Some(pref) = v.get("preferenze") {
            self.prefs = serde_json::from_value(pref.clone()).map_err(|e| e.to_string())?;
        }
        self.save()
    }

    pub fn export_profiles_json_to(&self, path: &PathBuf) -> Result<(), String> {
        write_json(path, &self.repo)
    }
}

#[derive(Debug, Clone, Default)]
pub struct GrammarMasterySummary {
    pub id: String,
    pub percentuale: i64,
    pub tentativi: i64,
    pub corrette: i64,
    pub completata: bool,
    pub ultima_volta: i64,
    pub prossima_ripasso: i64,
    pub ripassi: i64,
}

impl GrammarMasterySummary {
    pub fn from_value(id: &str, v: &Value) -> Self {
        let obj = v.as_object();
        let num = |k: &str| obj.and_then(|o| o.get(k)).and_then(|x| x.as_i64().or_else(|| x.as_f64().map(|f| f as i64))).unwrap_or(0);
        let boolv = |k: &str| obj.and_then(|o| o.get(k)).and_then(|x| x.as_bool()).unwrap_or(false);
        Self {
            id: id.to_string(),
            percentuale: num("percentuale"),
            tentativi: num("tentativi").max(num("totali")),
            corrette: num("corrette"),
            completata: boolv("completata"),
            ultima_volta: num("ultimaVolta").max(num("ultimoUso")).max(num("ultimaPratica")),
            prossima_ripasso: num("prossimaRipasso"),
            ripassi: num("ripassi"),
        }
    }

    pub fn is_due(&self, now_ms: i64) -> bool {
        self.prossima_ripasso > 0 && self.prossima_ripasso <= now_ms && self.tentativi > 0
    }
}

#[derive(Debug, Clone, Default)]
pub struct StudyPlanSummary {
    pub livello_corrente: String,
    pub obiettivo_livello: String,
    pub lezione_corrente: i32,
    pub due_words: usize,
    pub new_words: usize,
    pub grammar_tracked: usize,
    pub grammar_completed: usize,
    pub grammar_due: usize,
    pub next_actions: Vec<String>,
}

#[derive(Debug, Clone, PartialEq, Eq)]
pub struct CurriculumLesson {
    pub number: i32,
    pub topic_it: String,
    pub topic_en: String,
    pub is_current: bool,
    pub is_completed: bool,
}

pub fn curriculum_topics_for_level(level: &str) -> &'static [(&'static str, &'static str)] {
    let l = level.trim().to_uppercase();
    let base = l.split_whitespace().next().unwrap_or(&l);
    match base {
        "A0" | "A1" => &A1_TOPICS,
        "A2" => &A2_TOPICS,
        "B1" | "B2" | "C1" | "C2" => &B1_TOPICS,
        _ => &A1_TOPICS,
    }
}

const A1_TOPICS: [(&str, &str); 20] = [
    ("Saluti e presentazioni", "Greetings and introductions"),
    ("Numeri 0-100, etÃ  e date", "Numbers 0-100, age and dates"),
    ("La famiglia", "Family"),
    ("Pronomi personali e verbi sein/haben", "Personal pronouns and sein/haben"),
    ("Articoli der/die/das e plurali", "Articles der/die/das and plurals"),
    ("CittÃ  e paese", "City and country"),
    ("Cibo e bevande", "Food and drinks"),
    ("La giornata e le ore", "Daily routine and time"),
    ("Tempo libero e hobby", "Free time and hobbies"),
    ("Casa e stanze", "Home and rooms"),
    ("Vestiti e colori", "Clothes and colors"),
    ("Salute e corpo", "Health and body"),
    ("Viaggi e mezzi", "Travel and transport"),
    ("Lavoro e professioni", "Work and professions"),
    ("Tempo atmosferico", "Weather"),
    ("Animali", "Animals"),
    ("Soldi, negozi e prezzi", "Money, shops and prices"),
    ("Al telefono", "On the phone"),
    ("Al ristorante", "At the restaurant"),
    ("In farmacia", "At the pharmacy"),
];

const A2_TOPICS: [(&str, &str); 20] = [
    ("Perfetto (haben/sein + Partizip II)", "Perfect tense (haben/sein + Partizip II)"),
    ("Accusativo", "Accusative case"),
    ("Dativo", "Dative case"),
    ("Verbi modali", "Modal verbs"),
    ("Frasi subordinate con weil e dass", "Subordinate clauses with weil and dass"),
    ("Comparativi e superlativi", "Comparatives and superlatives"),
    ("Preposizioni con dativo", "Prepositions with dative"),
    ("Preposizioni con accusativo", "Prepositions with accusative"),
    ("Imperativo", "Imperative"),
    ("Pronomi relativi", "Relative pronouns"),
    ("Verbi separabili", "Separable verbs"),
    ("PrÃ¤teritum di sein e haben", "PrÃ¤teritum of sein and haben"),
    ("Lessico: lavoro e carriera", "Vocabulary: work and career"),
    ("Lessico: viaggi e turismo", "Vocabulary: travel and tourism"),
    ("Lessico: salute e medicina", "Vocabulary: health and medicine"),
    ("Lessico: tempo libero e sport", "Vocabulary: leisure and sport"),
    ("Lessico: cibo e ristorante", "Vocabulary: food and restaurant"),
    ("Lessico: casa e arredamento", "Vocabulary: home and furniture"),
    ("Lessico: tecnologia e media", "Vocabulary: technology and media"),
    ("Lessico: ambiente e natura", "Vocabulary: environment and nature"),
];

const B1_TOPICS: [(&str, &str); 20] = [
    ("Adjektivdeklination", "Adjective declension"),
    ("Genitivo", "Genitive case"),
    ("Frasi relative", "Relative clauses"),
    ("Connettori", "Connectors"),
    ("Verbi con preposizione", "Verbs with prepositions"),
    ("Preposizioni a doppio caso", "Two-way prepositions"),
    ("Verbi riflessivi", "Reflexive verbs"),
    ("Domande indirette", "Indirect questions"),
    ("Passivo con verbi modali", "Passive with modal verbs"),
    ("PrÃ¤teritum vs Perfetto", "PrÃ¤teritum vs Perfekt"),
    ("Konjunktiv II", "Konjunktiv II"),
    ("Lessico: lavoro e professione sanitaria", "Vocabulary: healthcare work"),
    ("Lessico: salute, corpo e sintomi", "Vocabulary: health, body and symptoms"),
    ("Lessico: relazioni e conflitti", "Vocabulary: relationships and conflicts"),
    ("Lessico: istruzione e formazione", "Vocabulary: education and training"),
    ("Lessico: ambiente e sostenibilitÃ ", "Vocabulary: environment and sustainability"),
    ("Lessico: tecnologia e innovazione", "Vocabulary: technology and innovation"),
    ("Lessico: cultura e tradizione", "Vocabulary: culture and tradition"),
    ("Lessico: economia e business", "Vocabulary: economy and business"),
    ("Lessico: emozioni e sentimenti", "Vocabulary: emotions and feelings"),
];

pub fn lesson_topic(level: &str, number: i32) -> (String, String) {
    let topics = curriculum_topics_for_level(level);
    if topics.is_empty() { return ("-".into(), "-".into()); }
    let n = number.max(1);
    let idx = ((n - 1) as usize) % topics.len();
    let giro = ((n - 1) as usize) / topics.len();
    let (it, en) = topics[idx];
    if giro == 0 {
        (it.to_string(), en.to_string())
    } else {
        (
            format!("{it} â€” approfondimento {}: nuove parole e strutture", giro + 1),
            format!("{en} â€” deepening {}: new words and structures", giro + 1),
        )
    }
}

pub fn curriculum_lessons(level: &str, current_lesson: i32, count: i32) -> Vec<CurriculumLesson> {
    let max_n = count.max(1);
    (1..=max_n).map(|n| {
        let (topic_it, topic_en) = lesson_topic(level, n);
        CurriculumLesson {
            number: n,
            topic_it,
            topic_en,
            is_current: n == current_lesson,
            is_completed: n < current_lesson,
        }
    }).collect()
}

impl Store {
    pub fn study_plan_summary(&self) -> StudyPlanSummary {
        let mut out = StudyPlanSummary::default();
        let Some(p) = self.active_profile() else { return out; };
        let now = chrono::Utc::now().timestamp_millis();
        out.livello_corrente = p.stato.progresso.livello_corrente.clone();
        out.obiettivo_livello = p.stato.progresso.obiettivo_livello.clone();
        out.lezione_corrente = p.stato.progresso.lezione_corrente;
        out.due_words = self.due_reviews(now, usize::MAX).len();
        out.new_words = self.new_words(usize::MAX).len();
        let summaries = self.grammar_mastery_summaries();
        out.grammar_tracked = summaries.len();
        out.grammar_completed = summaries.iter().filter(|s| s.completata || s.percentuale >= 80).count();
        out.grammar_due = summaries.iter().filter(|s| s.is_due(now)).count();

        if out.due_words > 0 {
            out.next_actions.push(format!("Review {} due word(s) (SRS)", out.due_words));
        }
        if out.grammar_due > 0 {
            out.next_actions.push(format!("Review {} grammar unit(s) due", out.grammar_due));
        }
        if out.new_words > 0 {
            out.next_actions.push(format!("Learn {} new word(s)", out.new_words.min(20)));
        }
        if out.lezione_corrente > 0 {
            out.next_actions.push(format!("Continue lesson/unit progress (lesson {})", out.lezione_corrente));
        }
        if out.next_actions.is_empty() {
            out.next_actions.push("Keep practicing: start a mixed exercise session".to_string());
        }
        out
    }

    pub fn set_lesson_progress(&mut self, lesson: i32) -> Result<(), String> {
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        p.stato.progresso.lezione_corrente = lesson.max(1);
        self.save()
    }

    pub fn advance_lesson_progress(&mut self) -> Result<i32, String> {
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let next = p.stato.progresso.lezione_corrente.max(0) + 1;
        p.stato.progresso.lezione_corrente = next;
        self.save()?;
        Ok(next)
    }
}

fn read_json<T: for<'de> Deserialize<'de> + Default>(path: &PathBuf) -> Option<T> {
    let data = fs::read_to_string(path).ok()?;
    serde_json::from_str(&data).ok()
}

fn write_json<T: Serialize>(path: &PathBuf, value: &T) -> Result<(), String> {
    let data = serde_json::to_string_pretty(value).map_err(|e| e.to_string())?;
    fs::write(path, data).map_err(|e| e.to_string())
}

// -----------------------------------------------------------------------------
// Embedded grammar units (subset, profile-compatible practice/mastery flow)
// -----------------------------------------------------------------------------

#[derive(Debug, Clone)]
pub struct GrammarUnit {
    pub id: &'static str,
    pub level: &'static str,
    pub title_it: &'static str,
    pub title_en: &'static str,
    pub subtitle_it: &'static str,
    pub subtitle_en: &'static str,
    pub objective_it: &'static str,
    pub objective_en: &'static str,
    pub theory_it: &'static [&'static str],
    pub theory_en: &'static [&'static str],
    pub examples: &'static [(&'static str, &'static str)], // (de, translation it/en-neutral)
    pub exercises: &'static [GrammarUnitExercise],
}

#[derive(Debug, Clone)]
pub struct GrammarUnitExercise {
    pub prompt_it: &'static str,
    pub prompt_en: &'static str,
    pub options: &'static [&'static str], // empty => typing/completion answer
    pub correct_index: usize,
    pub answer_text: &'static str, // expected text for typing exercises (case-insensitive trim)
    pub explanation_it: &'static str,
    pub explanation_en: &'static str,
}

pub fn embedded_grammar_units() -> Vec<GrammarUnit> {
    vec![
        unit_a0_alfabeto(),
        unit_a0_articoli(),
        unit_a0_plurale(),
        unit_a0_pronomi_sein(),
        unit_a0_frase(),
        unit_a1_akkusativ(),
        unit_a1_sein_haben(),
        unit_a1_possessivi_negazione(),
        unit_a1_imperativo(),
        unit_a1_preposizioni_luogo(),
        unit_a1_preposizioni_tempo(),
        unit_a1_separabili(),
        unit_a1_modali(),
        unit_a1_modalverben(),
        unit_a2_trennbare_verben(),
        unit_a2_perfekt_intro(),
        unit_a2_dativ(),
        unit_a2_praeteritum(),
        unit_a2_riflessivi(),
        unit_a2_weil_dass(),
        unit_a2_subordinate(),
        unit_a2_komparativ(),
        unit_a2_praepositionen_dativ(),
        unit_a2_praepositionen_akkusativ(),
        unit_a2_wechselpraepositionen(),
        unit_a2_relative(),
        unit_a2_passiv_intro(),
        unit_b1_adjektivdeklination_intro(),
        unit_b1_relativsaetze_intro(),
        unit_b1_konnektoren(),
        unit_b1_doppelkonnektoren(),
        unit_b1_reflexive_verben(),
        unit_b1_praeteritum_perfekt(),
        unit_b1_genitiv_intro(),
        unit_b1_konjunktiv2_intro(),
        unit_b1_futur_intro(),
        unit_b1_indirekte_rede(),
        unit_b1_nominalisierung(),
        unit_b1_partizipien(),
        unit_b1_passiv_modalverben_intro(),
    ]
}

pub fn embedded_grammar_unit_by_id(id: &str) -> Option<GrammarUnit> {
    embedded_grammar_units().into_iter().find(|u| u.id == id)
}

fn unit_a0_articoli() -> GrammarUnit {
    GrammarUnit {
        id: "a0-articoli",
        level: "A0",
        title_it: "Articoli determinativi: der, die, das",
        title_en: "Definite articles: der, die, das",
        subtitle_it: "Il genere dei sostantivi al nominativo",
        subtitle_en: "Noun gender in the nominative",
        objective_it: "Riconoscere e usare der/die/das con i sostantivi base.",
        objective_en: "Recognize and use der/die/das with basic nouns.",
        theory_it: &[
            "In tedesco ogni sostantivo ha un genere: maschile (der), femminile (die), neutro (das).",
            "Il plurale usa sempre die al nominativo (die Tische, die Frauen, die Kinder).",
            "Il genere non segue sempre l'italiano: va imparato con la parola.",
        ],
        theory_en: &[
            "In German every noun has a gender: masculine (der), feminine (die), neuter (das).",
            "The plural always uses die in the nominative (die Tische, die Frauen, die Kinder).",
            "Gender does not always match Italian/English: learn it with the word.",
        ],
        examples: &[
            ("der Mann", "l'uomo / the man"),
            ("die Frau", "la donna / the woman"),
            ("das Kind", "il bambino / the child"),
            ("die BÃ¼cher", "i libri / the books"),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Mann", prompt_en: "Choose the article: ___ Mann", options: &["der", "die", "das"], correct_index: 0, answer_text: "", explanation_it: "Mann Ã¨ maschile: der Mann.", explanation_en: "Mann is masculine: der Mann." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Frau", prompt_en: "Choose the article: ___ Frau", options: &["der", "die", "das"], correct_index: 1, answer_text: "", explanation_it: "Frau Ã¨ femminile: die Frau.", explanation_en: "Frau is feminine: die Frau." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Kind", prompt_en: "Choose the article: ___ Kind", options: &["der", "die", "das"], correct_index: 2, answer_text: "", explanation_it: "Kind Ã¨ neutro: das Kind.", explanation_en: "Kind is neuter: das Kind." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo (plurale): ___ Kinder", prompt_en: "Choose the article (plural): ___ Kinder", options: &["der", "die", "das"], correct_index: 1, answer_text: "", explanation_it: "Il plurale al nominativo usa die.", explanation_en: "Plural nominative uses die." },
        ],
    }
}

fn unit_a0_pronomi_sein() -> GrammarUnit {
    GrammarUnit {
        id: "a0-pronomi-sein",
        level: "A0",
        title_it: "Pronomi personali e sein",
        title_en: "Personal pronouns and sein",
        subtitle_it: "Presentarsi e descrivere",
        subtitle_en: "Introduce and describe yourself",
        objective_it: "Usare i pronomi e coniugare sein al presente.",
        objective_en: "Use pronouns and conjugate sein in the present.",
        theory_it: &[
            "Pronomi: ich, du, er/sie/es, wir, ihr, sie/Sie.",
            "sein (essere): ich bin, du bist, er/sie/es ist, wir sind, ihr seid, sie/Sie sind.",
            "Il verbo sta in seconda posizione nella frase principale.",
        ],
        theory_en: &[
            "Pronouns: ich, du, er/sie/es, wir, ihr, sie/Sie.",
            "sein (to be): ich bin, du bist, er/sie/es ist, wir sind, ihr seid, sie/Sie sind.",
            "The verb stays in second position in main clauses.",
        ],
        examples: &[
            ("Ich bin Anna.", "Io sono Anna."),
            ("Du bist mÃ¼de.", "Tu sei stanco/a."),
            ("Wir sind in Berlin.", "Siamo a Berlino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ Student.", prompt_en: "Complete: Ich ___ Student.", options: &["bin", "bist", "ist"], correct_index: 0, answer_text: "", explanation_it: "Con ich si usa bin.", explanation_en: "With ich use bin." },
            GrammarUnitExercise { prompt_it: "Completa: Du ___ nett.", prompt_en: "Complete: Du ___ nett.", options: &["bin", "bist", "sind"], correct_index: 1, answer_text: "", explanation_it: "Con du si usa bist.", explanation_en: "With du use bist." },
            GrammarUnitExercise { prompt_it: "Completa: Er ___ Arzt.", prompt_en: "Complete: Er ___ Arzt.", options: &["ist", "bist", "seid"], correct_index: 0, answer_text: "", explanation_it: "Con er/sie/es si usa ist.", explanation_en: "With er/sie/es use ist." },
            GrammarUnitExercise { prompt_it: "Completa: Wir ___ Freunde.", prompt_en: "Complete: Wir ___ Freunde.", options: &["seid", "sind", "ist"], correct_index: 1, answer_text: "", explanation_it: "Con wir si usa sind.", explanation_en: "With wir use sind." },
        ],
    }
}

fn unit_a0_frase() -> GrammarUnit {
    GrammarUnit {
        id: "a0-frase",
        level: "A0",
        title_it: "Ordine delle parole (frase principale)",
        title_en: "Word order (main clause)",
        subtitle_it: "Il verbo in seconda posizione",
        subtitle_en: "Verb in second position",
        objective_it: "Costruire frasi semplici con il verbo al secondo posto.",
        objective_en: "Build simple sentences with the verb in second position.",
        theory_it: &[
            "Nella frase principale il verbo coniugato Ã¨ sempre in posizione 2.",
            "Il soggetto puÃ² stare in posizione 1 o 3: Ich lerne Deutsch. / Heute lerne ich Deutsch.",
            "Le domande sÃ¬/no iniziano con il verbo: Lernst du Deutsch?",
        ],
        theory_en: &[
            "In a main clause the conjugated verb is always in position 2.",
            "The subject can be in position 1 or 3: Ich lerne Deutsch. / Heute lerne ich Deutsch.",
            "Yes/no questions start with the verb: Lernst du Deutsch?",
        ],
        examples: &[
            ("Ich komme aus Italien.", "Vengo dall'Italia."),
            ("Heute arbeite ich.", "Oggi lavoro."),
            ("Wohnst du in Wien?", "Abiti a Vienna?"),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Quale frase Ã¨ corretta?", prompt_en: "Which sentence is correct?", options: &["Ich heute lerne Deutsch.", "Heute lerne ich Deutsch.", "Heute ich lerne Deutsch."], correct_index: 1, answer_text: "", explanation_it: "Il verbo resta in seconda posizione: Heute (1) lerne (2) ich (3)...", explanation_en: "Verb stays in position 2: Heute (1) lerne (2) ich (3)..." },
            GrammarUnitExercise { prompt_it: "Completa l'ordine corretto: ___ du Deutsch? (lernen)", prompt_en: "Complete the correct order: ___ du Deutsch? (lernen)", options: &["Du lernst", "Lernst", "Deutsch lernst"], correct_index: 1, answer_text: "", explanation_it: "Nelle domande sÃ¬/no il verbo va all'inizio.", explanation_en: "In yes/no questions the verb comes first." },
            GrammarUnitExercise { prompt_it: "Quale frase Ã¨ corretta?", prompt_en: "Which sentence is correct?", options: &["Wir spielen heute FuÃŸball.", "Wir heute spielen FuÃŸball.", "Spielen wir heute FuÃŸball."], correct_index: 0, answer_text: "", explanation_it: "Frase affermativa: soggetto (1), verbo (2), resto.", explanation_en: "Statement: subject (1), verb (2), rest." },
        ],
    }
}

fn unit_a1_akkusativ() -> GrammarUnit {
    GrammarUnit {
        id: "a1-akkusativ",
        level: "A1",
        title_it: "Accusativo (introduzione)",
        title_en: "Accusative case (introduction)",
        subtitle_it: "L'oggetto diretto: den/die/das/einen/eine/ein",
        subtitle_en: "Direct object: den/die/das/einen/eine/ein",
        objective_it: "Riconoscere l'accusativo con articoli determinativi e indeterminativi.",
        objective_en: "Recognize accusative with definite and indefinite articles.",
        theory_it: &[
            "L'accusativo indica l'oggetto diretto (chi/che cosa?).",
            "Solo il maschile cambia: der -> den, ein -> einen. Femminile e neutro restano uguali al nominativo.",
            "Esempio: Ich sehe den Mann. / Ich habe einen Hund.",
        ],
        theory_en: &[
            "The accusative marks the direct object (whom/what?).",
            "Only masculine changes: der -> den, ein -> einen. Feminine and neuter stay the same as nominative.",
            "Example: Ich sehe den Mann. / Ich habe einen Hund.",
        ],
        examples: &[
            ("Ich habe den Stift.", "Ho la penna."),
            ("Sie kauft eine Tasche.", "Lei compra una borsa."),
            ("Wir sehen das Haus.", "Vediamo la casa."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich sehe ___ Mann. (der)", prompt_en: "Complete: Ich sehe ___ Mann. (der)", options: &["der", "den", "dem"], correct_index: 1, answer_text: "", explanation_it: "Maschile all'accusativo: der -> den.", explanation_en: "Masculine accusative: der -> den." },
            GrammarUnitExercise { prompt_it: "Completa: Ich habe ___ Katze. (eine)", prompt_en: "Complete: Ich habe ___ Katze. (eine)", options: &["eine", "einen", "einer"], correct_index: 0, answer_text: "", explanation_it: "Femminile non cambia all'accusativo: eine.", explanation_en: "Feminine does not change in accusative: eine." },
            GrammarUnitExercise { prompt_it: "Completa: Er kauft ___ Buch. (ein)", prompt_en: "Complete: Er kauft ___ Buch. (ein)", options: &["ein", "einen", "einem"], correct_index: 0, answer_text: "", explanation_it: "Neutro non cambia all'accusativo: ein.", explanation_en: "Neuter does not change in accusative: ein." },
            GrammarUnitExercise { prompt_it: "Completa: Wir brauchen ___ Tisch. (der)", prompt_en: "Complete: Wir brauchen ___ Tisch. (der)", options: &["der", "den", "des"], correct_index: 1, answer_text: "", explanation_it: "Oggetto diretto maschile: den Tisch.", explanation_en: "Masculine direct object: den Tisch." },
        ],
    }
}

fn unit_a1_sein_haben() -> GrammarUnit {
    GrammarUnit {
        id: "a1-sein-haben",
        level: "A1",
        title_it: "sein e haben al presente",
        title_en: "sein and haben in the present",
        subtitle_it: "Due verbi fondamentali: essere e avere",
        subtitle_en: "Two essential verbs: to be and to have",
        objective_it: "Coniugare e usare sein/haben in frasi semplici.",
        objective_en: "Conjugate and use sein/haben in simple sentences.",
        theory_it: &[
            "sein: ich bin, du bist, er/sie/es ist, wir sind, ihr seid, sie/Sie sind.",
            "haben: ich habe, du hast, er/sie/es hat, wir haben, ihr habt, sie/Sie haben.",
            "haben si usa spesso per possesso ed espressioni (Ich habe Hunger).",
        ],
        theory_en: &[
            "sein: ich bin, du bist, er/sie/es ist, wir sind, ihr seid, sie/Sie sind.",
            "haben: ich habe, du hast, er/sie/es hat, wir haben, ihr habt, sie/Sie haben.",
            "haben is often used for possession and expressions (Ich habe Hunger).",
        ],
        examples: &[
            ("Ich habe einen Bruder.", "Ho un fratello."),
            ("Sie hat Zeit.", "Lei ha tempo."),
            ("Wir sind zu Hause.", "Siamo a casa."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ eine Schwester. (haben)", prompt_en: "Complete: Ich ___ eine Schwester. (haben)", options: &["habe", "hast", "hat"], correct_index: 0, answer_text: "", explanation_it: "Con ich: ich habe.", explanation_en: "With ich: ich habe." },
            GrammarUnitExercise { prompt_it: "Completa: Du ___ mÃ¼de. (sein)", prompt_en: "Complete: Du ___ mÃ¼de. (sein)", options: &["bin", "bist", "ist"], correct_index: 1, answer_text: "", explanation_it: "Con du: du bist.", explanation_en: "With du: du bist." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma corretta: Er ___ (haben) ein Auto.", prompt_en: "Type the correct form: Er ___ (haben) ein Auto.", options: &[], correct_index: 0, answer_text: "hat", explanation_it: "Con er/sie/es: er hat.", explanation_en: "With er/sie/es: er hat." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma corretta: Wir ___ (sein) bereit.", prompt_en: "Type the correct form: Wir ___ (sein) bereit.", options: &[], correct_index: 0, answer_text: "sind", explanation_it: "Con wir: wir sind.", explanation_en: "With wir: wir sind." },
        ],
    }
}

fn unit_a1_modalverben() -> GrammarUnit {
    GrammarUnit {
        id: "a1-modalverben",
        level: "A1",
        title_it: "Verbi modali: kÃ¶nnen, mÃ¼ssen, wollen",
        title_en: "Modal verbs: kÃ¶nnen, mÃ¼ssen, wollen",
        subtitle_it: "Potere, dovere, volere",
        subtitle_en: "Can, must, want",
        objective_it: "Usare i modali principali con l'infinito in fondo.",
        objective_en: "Use common modals with the infinitive at the end.",
        theory_it: &[
            "I modali cambiano la vocale/radice in alcune persone: ich kann, du kannst, er kann; ich muss, du musst; ich will, du willst.",
            "Nella frase principale il modale Ã¨ coniugato in posizione 2 e l'infinito va alla fine.",
            "Esempio: Ich kann Deutsch sprechen. / Wir mÃ¼ssen heute lernen.",
        ],
        theory_en: &[
            "Modals change stem in some forms: ich kann, du kannst, er kann; ich muss, du musst; ich will, du willst.",
            "In a main clause the conjugated modal is in position 2 and the infinitive goes to the end.",
            "Example: Ich kann Deutsch sprechen. / Wir mÃ¼ssen heute lernen.",
        ],
        examples: &[
            ("Ich kann schwimmen.", "So nuotare."),
            ("Du musst arbeiten.", "Devi lavorare."),
            ("Wir wollen nach Hause gehen.", "Vogliamo andare a casa."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ gut kochen. (kÃ¶nnen)", prompt_en: "Complete: Ich ___ gut kochen. (kÃ¶nnen)", options: &["kann", "kannst", "kÃ¶nnen"], correct_index: 0, answer_text: "", explanation_it: "Con ich: ich kann.", explanation_en: "With ich: ich kann." },
            GrammarUnitExercise { prompt_it: "Completa: Du ___ heute lernen. (mÃ¼ssen)", prompt_en: "Complete: Du ___ heute lernen. (mÃ¼ssen)", options: &["muss", "musst", "mÃ¼ssen"], correct_index: 1, answer_text: "", explanation_it: "Con du: du musst.", explanation_en: "With du: du musst." },
            GrammarUnitExercise { prompt_it: "Scegli la frase corretta.", prompt_en: "Choose the correct sentence.", options: &["Ich will spielen FuÃŸball.", "Ich will FuÃŸball spielen.", "Will ich FuÃŸball spielen."], correct_index: 1, answer_text: "", explanation_it: "Il modale va in posizione 2 e l'infinito alla fine.", explanation_en: "Modal in position 2, infinitive at the end." },
            GrammarUnitExercise { prompt_it: "Scrivi il modale corretto: Er ___ (kÃ¶nnen) helfen.", prompt_en: "Type the correct modal: Er ___ (kÃ¶nnen) helfen.", options: &[], correct_index: 0, answer_text: "kann", explanation_it: "Con er/sie/es: er kann.", explanation_en: "With er/sie/es: er kann." },
        ],
    }
}

fn unit_a2_perfekt_intro() -> GrammarUnit {
    GrammarUnit {
        id: "a2-perfekt-intro",
        level: "A2",
        title_it: "Perfekt (introduzione)",
        title_en: "Perfekt tense (introduction)",
        subtitle_it: "Passato parlato con haben/sein + Partizip II",
        subtitle_en: "Spoken past with haben/sein + Partizip II",
        objective_it: "Riconoscere e formare frasi base al Perfekt.",
        objective_en: "Recognize and form basic Perfekt sentences.",
        theory_it: &[
            "Il Perfekt usa haben o sein come ausiliare + participio passato (Partizip II) alla fine.",
            "Molti verbi usano haben (ich habe gemacht). Verbi di movimento/cambiamento di stato spesso usano sein (ich bin gegangen).",
            "Partizip II regolare tipico: ge- + radice + -t (machen -> gemacht).",
        ],
        theory_en: &[
            "Perfekt uses haben or sein as auxiliary + past participle (Partizip II) at the end.",
            "Many verbs use haben (ich habe gemacht). Movement/state-change verbs often use sein (ich bin gegangen).",
            "Typical regular Partizip II: ge- + stem + -t (machen -> gemacht).",
        ],
        examples: &[
            ("Ich habe gelernt.", "Ho studiato / I studied."),
            ("Sie hat gekocht.", "Lei ha cucinato."),
            ("Wir sind nach Hause gegangen.", "Siamo andati a casa."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Quale ausiliare Ã¨ tipico per 'machen'?", prompt_en: "Which auxiliary is typical for 'machen'?", options: &["sein", "haben", "werden"], correct_index: 1, answer_text: "", explanation_it: "machen forma il Perfekt con haben: ich habe gemacht.", explanation_en: "machen forms Perfekt with haben: ich habe gemacht." },
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ gestern gelernt. (haben)", prompt_en: "Complete: Ich ___ gestern gelernt. (haben)", options: &["habe", "bin", "hat"], correct_index: 0, answer_text: "", explanation_it: "Con ich + haben: ich habe gelernt.", explanation_en: "With ich + haben: ich habe gelernt." },
            GrammarUnitExercise { prompt_it: "Scegli il participio corretto di machen.", prompt_en: "Choose the correct participle of machen.", options: &["gemacht", "gemachen", "gemachte"], correct_index: 0, answer_text: "", explanation_it: "Regolare: ge- + mach + -t = gemacht.", explanation_en: "Regular: ge- + mach + -t = gemacht." },
            GrammarUnitExercise { prompt_it: "Scrivi il participio: lernen -> ___", prompt_en: "Type the participle: lernen -> ___", options: &[], correct_index: 0, answer_text: "gelernt", explanation_it: "lernen -> gelernt (ge- + lern + -t).", explanation_en: "lernen -> gelernt (ge- + lern + -t)." },
        ],
    }
}

fn unit_a2_dativ() -> GrammarUnit {
    GrammarUnit {
        id: "a2-dativ",
        level: "A2",
        title_it: "Dativo (introduzione)",
        title_en: "Dative case (introduction)",
        subtitle_it: "Oggetto indiretto e articoli al dativo",
        subtitle_en: "Indirect object and dative articles",
        objective_it: "Riconoscere e usare le forme base del dativo.",
        objective_en: "Recognize and use basic dative forms.",
        theory_it: &[
            "Il dativo risponde a 'a chi?' (wem?). Spesso indica il destinatario.",
            "Articoli determinativi: dem (m/n), der (f), den (+n al plurale in molti nomi). Indeterminativi: einem (m/n), einer (f).",
            "Verbi/preposizioni comuni con dativo: helfen, danken, mit, von, zu, bei.",
        ],
        theory_en: &[
            "The dative answers 'to whom?' (wem?). It often marks the recipient.",
            "Definite articles: dem (m/n), der (f), den (plural, often +n on noun). Indefinite: einem (m/n), einer (f).",
            "Common dative verbs/prepositions: helfen, danken, mit, von, zu, bei.",
        ],
        examples: &[
            ("Ich helfe dem Mann.", "Aiuto l'uomo."),
            ("Sie gibt der Frau ein Buch.", "DÃ  un libro alla donna."),
            ("Wir fahren mit dem Bus.", "Andiamo in autobus."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich helfe ___ Mann. (der)", prompt_en: "Complete: Ich helfe ___ Mann. (der)", options: &["den", "dem", "der"], correct_index: 1, answer_text: "", explanation_it: "Dativo maschile: der -> dem.", explanation_en: "Masculine dative: der -> dem." },
            GrammarUnitExercise { prompt_it: "Completa: Sie dankt ___ Frau. (die)", prompt_en: "Complete: Sie dankt ___ Frau. (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "Dativo femminile: die -> der.", explanation_en: "Feminine dative: die -> der." },
            GrammarUnitExercise { prompt_it: "Scegli la preposizione che regge il dativo.", prompt_en: "Choose the preposition that takes dative.", options: &["fÃ¼r", "ohne", "mit"], correct_index: 2, answer_text: "", explanation_it: "mit richiede il dativo (es. mit dem Bus).", explanation_en: "mit takes the dative (e.g. mit dem Bus)." },
            GrammarUnitExercise { prompt_it: "Scrivi l'articolo: Ich spreche mit ___ Kind. (das)", prompt_en: "Type the article: Ich spreche mit ___ Kind. (das)", options: &[], correct_index: 0, answer_text: "dem", explanation_it: "Neutro al dativo: das -> dem.", explanation_en: "Neuter dative: das -> dem." },
        ],
    }
}

fn unit_a2_weil_dass() -> GrammarUnit {
    GrammarUnit {
        id: "a2-weil-dass",
        level: "A2",
        title_it: "Subordinate con weil e dass",
        title_en: "Subordinate clauses with weil and dass",
        subtitle_it: "Il verbo va alla fine della subordinata",
        subtitle_en: "Verb goes to the end in subordinate clauses",
        objective_it: "Costruire frasi con weil (perchÃ©) e dass (che).",
        objective_en: "Build sentences with weil (because) and dass (that).",
        theory_it: &[
            "Nelle subordinate introdotte da weil/dass il verbo coniugato va alla fine.",
            "La subordinata puÃ² stare dopo o prima della principale: Ich bleibe zu Hause, weil ich krank bin. / Weil ich krank bin, bleibe ich zu Hause.",
            "Con modale: ... weil ich heute arbeiten muss.",
        ],
        theory_en: &[
            "In subordinate clauses introduced by weil/dass the conjugated verb goes to the end.",
            "The clause can come after or before the main clause: Ich bleibe zu Hause, weil ich krank bin. / Weil ich krank bin, bleibe ich zu Hause.",
            "With modal: ... weil ich heute arbeiten muss.",
        ],
        examples: &[
            ("Ich lerne Deutsch, weil ich in Wien arbeite.", "Studio tedesco perchÃ© lavoro a Vienna."),
            ("Ich weiÃŸ, dass du mÃ¼de bist.", "So che sei stanco/a."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Quale frase Ã¨ corretta?", prompt_en: "Which sentence is correct?", options: &["Ich bleibe, weil ich bin krank.", "Ich bleibe, weil ich krank bin.", "Weil ich krank bin, ich bleibe."], correct_index: 1, answer_text: "", explanation_it: "In subordinata il verbo va alla fine: weil ich krank bin.", explanation_en: "In subordinate clause the verb goes to the end: weil ich krank bin." },
            GrammarUnitExercise { prompt_it: "Completa: Ich denke, dass er heute ___ . (kommen)", prompt_en: "Complete: Ich denke, dass er heute ___ . (kommen)", options: &["kommt", "kommen", "kam"], correct_index: 0, answer_text: "", explanation_it: "Verbo coniugato alla fine della subordinata: dass er heute kommt.", explanation_en: "Conjugated verb at the end: dass er heute kommt." },
            GrammarUnitExercise { prompt_it: "Scegli la congiunzione: Ich lerne, ___ ich die PrÃ¼fung habe.", prompt_en: "Choose the conjunction: Ich lerne, ___ ich die PrÃ¼fung habe.", options: &["dass", "weil", "ob"], correct_index: 1, answer_text: "", explanation_it: "weil esprime causa: perchÃ© ho l'esame.", explanation_en: "weil expresses reason/cause." },
            GrammarUnitExercise { prompt_it: "Scrivi l'ultima parola: Ich weiÃŸ, dass du Recht ___ . (haben)", prompt_en: "Type the last word: Ich weiÃŸ, dass du Recht ___ . (haben)", options: &[], correct_index: 0, answer_text: "hast", explanation_it: "In subordinata il verbo coniugato va alla fine: dass du Recht hast.", explanation_en: "Conjugated verb at the end of subordinate clause: dass du Recht hast." },
        ],
    }
}

fn unit_a2_komparativ() -> GrammarUnit {
    GrammarUnit {
        id: "a2-komparativ",
        level: "A2",
        title_it: "Comparativi e superlativi",
        title_en: "Comparatives and superlatives",
        subtitle_it: "Confrontare persone, cose e qualitÃ ",
        subtitle_en: "Compare people, things, and qualities",
        objective_it: "Formare e usare comparativo e superlativo di base.",
        objective_en: "Form and use basic comparative and superlative forms.",
        theory_it: &[
            "Comparativo: aggettivo + -er (+ spesso umlaut: alt -> Ã¤lter), confronto con 'als'.",
            "Superlativo: am + aggettivo + -sten (am schnellsten) oppure der/die/das + -ste in attributo.",
            "Irregolari comuni: gut -> besser -> am besten; viel -> mehr -> am meisten.",
        ],
        theory_en: &[
            "Comparative: adjective + -er (often umlaut: alt -> Ã¤lter), comparison with 'als'.",
            "Superlative: am + adjective + -sten (am schnellsten) or der/die/das + -ste as attribute.",
            "Common irregulars: gut -> besser -> am besten; viel -> mehr -> am meisten.",
        ],
        examples: &[
            ("Mein Auto ist schneller als dein Auto.", "La mia auto Ã¨ piÃ¹ veloce della tua."),
            ("Sie ist die beste Spielerin.", "Lei Ã¨ la migliore giocatrice."),
            ("Im Sommer ist es am wÃ¤rmsten.", "In estate fa piÃ¹ caldo (Ã¨ il periodo piÃ¹ caldo)."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa il comparativo: schnell -> ___", prompt_en: "Complete the comparative: schnell -> ___", options: &["schneller", "am schnellsten", "schnellste"], correct_index: 0, answer_text: "", explanation_it: "Comparativo regolare: schnell + -er = schneller.", explanation_en: "Regular comparative: schnell + -er = schneller." },
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: gut -> ___ -> am besten", prompt_en: "Choose the correct form: gut -> ___ -> am besten", options: &["guter", "besser", "mehr"], correct_index: 1, answer_text: "", explanation_it: "gut Ã¨ irregolare: besser.", explanation_en: "gut is irregular: besser." },
            GrammarUnitExercise { prompt_it: "Completa: Dieses Buch ist ___ als jenes. (interessant)", prompt_en: "Complete: Dieses Buch ist ___ als jenes. (interessant)", options: &[], correct_index: 0, answer_text: "interessanter", explanation_it: "Comparativo: interessanter als ...", explanation_en: "Comparative: interessanter als ..." },
            GrammarUnitExercise { prompt_it: "Scegli il superlativo predicativo di schnell.", prompt_en: "Choose the predicative superlative of schnell.", options: &["am schnellsten", "schneller", "der schnellste"], correct_index: 0, answer_text: "", explanation_it: "Predicativo: am + -sten = am schnellsten.", explanation_en: "Predicative: am + -sten = am schnellsten." },
        ],
    }
}

fn unit_b1_adjektivdeklination_intro() -> GrammarUnit {
    GrammarUnit {
        id: "b1-adjektivdeklination-intro",
        level: "B1",
        title_it: "Declinazione dell'aggettivo (introduzione)",
        title_en: "Adjective declension (introduction)",
        subtitle_it: "Desinenze con articolo determinativo e indeterminativo",
        subtitle_en: "Endings with definite and indefinite articles",
        objective_it: "Riconoscere le desinenze piÃ¹ comuni degli aggettivi attributivi.",
        objective_en: "Recognize the most common attributive adjective endings.",
        theory_it: &[
            "L'aggettivo davanti al nome prende desinenze che dipendono da genere, caso e articolo.",
            "Con articolo determinativo al nominativo: der gute Mann, die gute Frau, das gute Kind, die guten Kinder.",
            "Con articolo indeterminativo al nominativo: ein guter Mann, eine gute Frau, ein gutes Kind.",
        ],
        theory_en: &[
            "An adjective before a noun takes endings depending on gender, case, and article type.",
            "With definite article in nominative: der gute Mann, die gute Frau, das gute Kind, die guten Kinder.",
            "With indefinite article in nominative: ein guter Mann, eine gute Frau, ein gutes Kind.",
        ],
        examples: &[
            ("Der kleine Hund schlÃ¤ft.", "Il cagnolino dorme."),
            ("Ich habe eine neue Tasche.", "Ho una borsa nuova."),
            ("Das ist ein gutes Buch.", "Questo Ã¨ un buon libro."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: der ___ Mann (gut)", prompt_en: "Complete: der ___ Mann (gut)", options: &["gute", "guter", "gutes"], correct_index: 0, answer_text: "", explanation_it: "Maschile nominativo con articolo determinativo: der gute Mann.", explanation_en: "Masculine nominative with definite article: der gute Mann." },
            GrammarUnitExercise { prompt_it: "Completa: ein ___ Mann (gut)", prompt_en: "Complete: ein ___ Mann (gut)", options: &["gute", "guter", "guten"], correct_index: 1, answer_text: "", explanation_it: "Maschile nominativo con articolo indeterminativo: ein guter Mann.", explanation_en: "Masculine nominative with indefinite article: ein guter Mann." },
            GrammarUnitExercise { prompt_it: "Completa: eine ___ Frau (neu)", prompt_en: "Complete: eine ___ Frau (neu)", options: &["neue", "neuer", "neuen"], correct_index: 0, answer_text: "", explanation_it: "Femminile nominativo con eine: eine neue Frau.", explanation_en: "Feminine nominative with eine: eine neue Frau." },
            GrammarUnitExercise { prompt_it: "Scrivi la desinenza: das ___ Kind (klein)", prompt_en: "Type the ending/form: das ___ Kind (klein)", options: &[], correct_index: 0, answer_text: "kleine", explanation_it: "Neutro nominativo con articolo determinativo: das kleine Kind.", explanation_en: "Neuter nominative with definite article: das kleine Kind." },
        ],
    }
}

fn unit_a2_praepositionen_dativ() -> GrammarUnit {
    GrammarUnit {
        id: "a2-praepositionen-dativ",
        level: "A2",
        title_it: "Preposizioni con dativo",
        title_en: "Prepositions with dative",
        subtitle_it: "mit, nach, bei, von, zu, aus, seit, gegenÃ¼ber",
        subtitle_en: "mit, nach, bei, von, zu, aus, seit, gegenÃ¼ber",
        objective_it: "Usare le preposizioni piÃ¹ comuni che richiedono il dativo.",
        objective_en: "Use common prepositions that take the dative.",
        theory_it: &[
            "Alcune preposizioni reggono sempre il dativo: aus, bei, mit, nach, seit, von, zu, gegenÃ¼ber.",
            "Dopo queste preposizioni l'articolo maschile/neutro diventa dem, il femminile der.",
            "Esempio: mit dem Freund, bei der Ã„rztin, zu dem -> zum, zu der -> zur.",
        ],
        theory_en: &[
            "Some prepositions always take the dative: aus, bei, mit, nach, seit, von, zu, gegenÃ¼ber.",
            "After these, masculine/neuter article becomes dem, feminine becomes der.",
            "Example: mit dem Freund, bei der Ã„rztin, zu dem -> zum, zu der -> zur.",
        ],
        examples: &[
            ("Ich fahre mit dem Bus.", "Vado in autobus."),
            ("Sie kommt aus der Schweiz.", "Lei viene dalla Svizzera."),
            ("Wir gehen zu dem/zum Arzt.", "Andiamo dal medico."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich fahre mit ___ Auto. (das)", prompt_en: "Complete: Ich fahre mit ___ Auto. (das)", options: &["das", "dem", "den"], correct_index: 1, answer_text: "", explanation_it: "mit + dativo neutro: dem Auto.", explanation_en: "mit + dative neuter: dem Auto." },
            GrammarUnitExercise { prompt_it: "Completa: Er wohnt bei ___ Tante. (die)", prompt_en: "Complete: Er wohnt bei ___ Tante. (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "bei + dativo femminile: der Tante.", explanation_en: "bei + dative feminine: der Tante." },
            GrammarUnitExercise { prompt_it: "Scegli la preposizione che regge il dativo.", prompt_en: "Choose the preposition that takes dative.", options: &["durch", "fÃ¼r", "von"], correct_index: 2, answer_text: "", explanation_it: "von richiede il dativo; durch/fÃ¼r l'accusativo.", explanation_en: "von takes dative; durch/fÃ¼r take accusative." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma contratta: zu + dem = ___", prompt_en: "Type the contraction: zu + dem = ___", options: &[], correct_index: 0, answer_text: "zum", explanation_it: "zu + dem si contrae in zum.", explanation_en: "zu + dem contracts to zum." },
        ],
    }
}

fn unit_b1_relativsaetze_intro() -> GrammarUnit {
    GrammarUnit {
        id: "b1-relativsaetze-intro",
        level: "B1",
        title_it: "Frasi relative (introduzione)",
        title_en: "Relative clauses (introduction)",
        subtitle_it: "Pronomi relativi al nominativo e accusativo",
        subtitle_en: "Relative pronouns in nominative and accusative",
        objective_it: "Collegare frasi con pronomi relativi di base (der/die/das/den).",
        objective_en: "Connect clauses with basic relative pronouns (der/die/das/den).",
        theory_it: &[
            "Il pronome relativo concorda in genere e numero con il nome a cui si riferisce.",
            "Nominativo: der (m), die (f/pl), das (n). Accusativo maschile: den; femm/neutro/plurale come nominativo (die/das/die).",
            "Nella frase relativa il verbo coniugato va alla fine.",
        ],
        theory_en: &[
            "The relative pronoun agrees in gender and number with the noun it refers to.",
            "Nominative: der (m), die (f/pl), das (n). Accusative masculine: den; fem/neut/pl same as nominative forms in these cases (die/das/die).",
            "In a relative clause the conjugated verb goes to the end.",
        ],
        examples: &[
            ("Das ist der Mann, der hier wohnt.", "Questo Ã¨ l'uomo che abita qui."),
            ("Ich kenne die Frau, die du meinst.", "Conosco la donna che intendi."),
            ("Das ist das Buch, das ich lese.", "Questo Ã¨ il libro che leggo."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Der Mann, ___ dort steht, ist mein Lehrer.", prompt_en: "Complete: Der Mann, ___ dort steht, ist mein Lehrer.", options: &["der", "den", "dem"], correct_index: 0, answer_text: "", explanation_it: "Soggetto maschile nella relativa: der.", explanation_en: "Masculine subject in relative clause: der." },
            GrammarUnitExercise { prompt_it: "Completa: Das Kind, ___ ich sehe, spielt.", prompt_en: "Complete: Das Kind, ___ ich sehe, spielt.", options: &["der", "das", "dem"], correct_index: 1, answer_text: "", explanation_it: "Neutro, oggetto in relativa (accusativo = nominativo qui): das.", explanation_en: "Neuter object in relative clause: das." },
            GrammarUnitExercise { prompt_it: "Completa: Die Frau, ___ du kennst, ist Ã„rztin. (accusativo)", prompt_en: "Complete: Die Frau, ___ du kennst, ist Ã„rztin. (accusative)", options: &["der", "die", "den"], correct_index: 1, answer_text: "", explanation_it: "Femminile accusativo nella relativa: die.", explanation_en: "Feminine accusative in relative clause: die." },
            GrammarUnitExercise { prompt_it: "Scrivi il pronome: Der Hund, ___ ich habe, ist klein. (accusativo maschile)", prompt_en: "Type the pronoun: Der Hund, ___ ich habe, ist klein. (masc. accusative)", options: &[], correct_index: 0, answer_text: "den", explanation_it: "Maschile accusativo nella relativa: den.", explanation_en: "Masculine accusative relative pronoun: den." },
        ],
    }
}

fn unit_b1_konnektoren() -> GrammarUnit {
    GrammarUnit {
        id: "b1-konnektoren",
        level: "B1",
        title_it: "Connettori: deshalb, trotzdem, obwohl",
        title_en: "Connectors: deshalb, trotzdem, obwohl",
        subtitle_it: "Collegare idee: causa, conseguenza e contrasto",
        subtitle_en: "Link ideas: cause, consequence, contrast",
        objective_it: "Usare connettori comuni per strutturare frasi e argomenti.",
        objective_en: "Use common connectors to structure sentences and arguments.",
        theory_it: &[
            "deshalb (perciÃ²) introduce una conseguenza: spesso il verbo segue subito (posizione 2) nella frase principale.",
            "trotzdem (nonostante ciÃ²) esprime contrasto rispetto a quanto detto prima.",
            "obwohl (sebbene) introduce una subordinata: il verbo coniugato va alla fine.",
        ],
        theory_en: &[
            "deshalb (therefore) introduces a consequence; the verb often follows in position 2 in the main clause.",
            "trotzdem (nevertheless) expresses contrast with what was said before.",
            "obwohl (although) introduces a subordinate clause: the conjugated verb goes to the end.",
        ],
        examples: &[
            ("Es regnet, deshalb bleibe ich zu Hause.", "Piove, perciÃ² resto a casa."),
            ("Ich bin mÃ¼de, trotzdem arbeite ich.", "Sono stanco, eppure lavoro."),
            ("Ich gehe spazieren, obwohl es regnet.", "Vado a passeggiare sebbene piova."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli il connettore: Es ist kalt, ___ ziehe ich eine Jacke an.", prompt_en: "Choose the connector: Es ist kalt, ___ ziehe ich eine Jacke an.", options: &["obwohl", "deshalb", "weil"], correct_index: 1, answer_text: "", explanation_it: "Conseguenza: fa freddo, perciÃ² metto la giacca.", explanation_en: "Consequence: it's cold, therefore I put on a jacket." },
            GrammarUnitExercise { prompt_it: "Scegli il connettore: ___ ich krank bin, gehe ich zur Arbeit.", prompt_en: "Choose the connector: ___ ich krank bin, gehe ich zur Arbeit.", options: &["Deshalb", "Trotzdem", "Obwohl"], correct_index: 2, answer_text: "", explanation_it: "obwohl introduce una concessione/contrasto (verbo alla fine nella subordinata).", explanation_en: "obwohl introduces concession/contrast (verb at the end in the clause)." },
            GrammarUnitExercise { prompt_it: "Completa: Ich habe wenig Zeit, ___ helfe ich dir.", prompt_en: "Complete: Ich habe wenig Zeit, ___ helfe ich dir.", options: &["trotzdem", "obwohl", "weil"], correct_index: 0, answer_text: "", explanation_it: "Contrasto: poco tempo, eppure ti aiuto.", explanation_en: "Contrast: little time, nevertheless I help you." },
            GrammarUnitExercise { prompt_it: "Scrivi il connettore: Ich lerne viel, ___ mÃ¶chte ich die PrÃ¼fung bestehen.", prompt_en: "Type the connector: Ich lerne viel, ___ mÃ¶chte ich die PrÃ¼fung bestehen.", options: &[], correct_index: 0, answer_text: "deshalb", explanation_it: "Conseguenza: studio molto, perciÃ² voglio superare l'esame.", explanation_en: "Consequence: I study a lot, therefore I want to pass the exam." },
        ],
    }
}

fn unit_b1_reflexive_verben() -> GrammarUnit {
    GrammarUnit {
        id: "b1-reflexive-verben",
        level: "B1",
        title_it: "Verbi riflessivi (introduzione)",
        title_en: "Reflexive verbs (introduction)",
        subtitle_it: "Pronomi riflessivi all'accusativo",
        subtitle_en: "Reflexive pronouns in the accusative",
        objective_it: "Usare i verbi riflessivi comuni con il pronome corretto.",
        objective_en: "Use common reflexive verbs with the correct pronoun.",
        theory_it: &[
            "I verbi riflessivi usano un pronome che si riferisce al soggetto: mich, dich, sich, uns, euch, sich.",
            "Accusativo: ich freue mich, du freust dich, er/sie/es freut sich, wir freuen uns, ihr freut euch, sie/Sie freuen sich.",
            "Molti verbi riflessivi in tedesco non lo sono in italiano (es. sich freuen = essere contento).",
        ],
        theory_en: &[
            "Reflexive verbs use a pronoun referring back to the subject: mich, dich, sich, uns, euch, sich.",
            "Accusative: ich freue mich, du freust dich, er/sie/es freut sich, wir freuen uns, ihr freut euch, sie/Sie freuen sich.",
            "Many German reflexive verbs are not reflexive in English/Italian (e.g. sich freuen = to be happy).",
        ],
        examples: &[
            ("Ich freue mich.", "Sono contento/a."),
            ("Er beeilt sich.", "Lui si sbrig/a."),
            ("Wir interessieren uns fÃ¼r Musik.", "Ci interessiamo di musica."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich freue ___ .", prompt_en: "Complete: Ich freue ___ .", options: &["mich", "dich", "sich"], correct_index: 0, answer_text: "", explanation_it: "Con ich si usa mich.", explanation_en: "With ich use mich." },
            GrammarUnitExercise { prompt_it: "Completa: Du wÃ¤schst ___ .", prompt_en: "Complete: Du wÃ¤schst ___ .", options: &["mich", "dich", "sich"], correct_index: 1, answer_text: "", explanation_it: "Con du si usa dich.", explanation_en: "With du use dich." },
            GrammarUnitExercise { prompt_it: "Completa: Sie (lei) beeilt ___ .", prompt_en: "Complete: Sie (she) beeilt ___ .", options: &["mich", "dich", "sich"], correct_index: 2, answer_text: "", explanation_it: "Con er/sie/es si usa sich.", explanation_en: "With er/sie/es use sich." },
            GrammarUnitExercise { prompt_it: "Scrivi il pronome: Wir freuen ___ auf das Wochenende.", prompt_en: "Type the pronoun: Wir freuen ___ auf das Wochenende.", options: &[], correct_index: 0, answer_text: "uns", explanation_it: "Con wir si usa uns.", explanation_en: "With wir use uns." },
        ],
    }
}

fn unit_a2_trennbare_verben() -> GrammarUnit {
    GrammarUnit {
        id: "a2-trennbare-verben",
        level: "A2",
        title_it: "Verbi separabili",
        title_en: "Separable verbs",
        subtitle_it: "Prefisso alla fine nella frase principale",
        subtitle_en: "Prefix moves to the end in main clauses",
        objective_it: "Riconoscere e usare i verbi separabili al presente e nel Perfekt.",
        objective_en: "Recognize and use separable verbs in the present and Perfekt.",
        theory_it: &[
            "Nei verbi separabili il prefisso si stacca e va alla fine della frase principale: aufstehen -> Ich stehe frueh auf.",
            "Nelle subordinate (weil, dass...) e con modali il verbo resta unito all'infinito: weil ich frueh aufstehe / ich muss frueh aufstehen.",
            "Nel Perfekt il ge- si inserisce tra prefisso e radice: aufgestanden, eingekauft.",
        ],
        theory_en: &[
            "In separable verbs the prefix separates and moves to the end of the main clause: aufstehen -> Ich stehe frueh auf.",
            "In subordinate clauses (weil, dass...) and with modals the verb stays together: weil ich frueh aufstehe / ich muss frueh aufstehen.",
            "In the Perfekt, ge- is inserted between prefix and stem: aufgestanden, eingekauft.",
        ],
        examples: &[
            ("Ich stehe jeden Tag um 7 Uhr auf.", "Mi alzo ogni giorno alle 7."),
            ("Er ruft seine Mutter an.", "Lui chiama sua madre."),
            ("Wir haben gestern eingekauft.", "Ieri abbiamo fatto la spesa."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli la frase corretta (aufstehen).", prompt_en: "Choose the correct sentence (aufstehen).", options: &["Ich aufstehe frueh.", "Ich stehe frueh auf.", "Ich stehe auf frueh."], correct_index: 1, answer_text: "", explanation_it: "Nella frase principale il prefisso va alla fine: Ich stehe frueh auf.", explanation_en: "In a main clause the prefix goes to the end." },
            GrammarUnitExercise { prompt_it: "Completa: Er ___ seine Freundin ___ . (anrufen)", prompt_en: "Complete: Er ___ seine Freundin ___ . (anrufen)", options: &["ruft ... an", "anruft ...", "ruft ... auf"], correct_index: 0, answer_text: "", explanation_it: "anrufen si separa: Er ruft ... an.", explanation_en: "anrufen separates: Er ruft ... an." },
            GrammarUnitExercise { prompt_it: "Scrivi il participio di einkaufen.", prompt_en: "Type the participle of einkaufen.", options: &[], correct_index: 0, answer_text: "eingekauft", explanation_it: "Verbi separabili: prefisso + ge + radice + t -> eingekauft.", explanation_en: "Separable verbs: prefix + ge + stem + t -> eingekauft." },
            GrammarUnitExercise { prompt_it: "Quale e' corretta con weil? (aufstehen)", prompt_en: "Which is correct with weil? (aufstehen)", options: &["..., weil ich stehe frueh auf.", "..., weil ich frueh aufstehe.", "..., weil ich auf frueh stehe."], correct_index: 1, answer_text: "", explanation_it: "In subordinata il verbo separabile resta unito alla fine: weil ich frueh aufstehe.", explanation_en: "In a subordinate clause the separable verb stays together at the end." },
        ],
    }
}

fn unit_b1_praeteritum_perfekt() -> GrammarUnit {
    GrammarUnit {
        id: "b1-praeteritum-vs-perfekt",
        level: "B1",
        title_it: "PrÃ¤teritum vs Perfekt (uso)",
        title_en: "PrÃ¤teritum vs Perfekt (usage)",
        subtitle_it: "Quando usare il passato scritto o parlato",
        subtitle_en: "When to use written vs spoken past",
        objective_it: "Distinguere l'uso tipico di Praeteritum e Perfekt in tedesco.",
        objective_en: "Distinguish typical usage of PrÃ¤teritum and Perfekt in German.",
        theory_it: &[
            "Nel parlato quotidiano il Perfekt e' il tempo passato piu' comune per raccontare eventi.",
            "Il Praeteritum e' frequente nella lingua scritta, nei racconti e con alcuni verbi (sein, haben, modali) anche nel parlato.",
            "Esempio parlato: Ich habe gestern gearbeitet. / Scritto/narrativo: Er ging nach Hause.",
        ],
        theory_en: &[
            "In everyday speech, Perfekt is the most common past tense for narrating events.",
            "PrÃ¤teritum is common in writing, narratives, and with some verbs (sein, haben, modals) even in speech.",
            "Spoken example: Ich habe gestern gearbeitet. / Written/narrative: Er ging nach Hause.",
        ],
        examples: &[
            ("Ich war krank.", "Ero malato."),
            ("Wir haben einen Film gesehen.", "Abbiamo visto un film."),
            ("Sie hatte keine Zeit.", "Lei non aveva tempo."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Quale forma e' tipicamente piu' comune nel parlato per 'lavorare ieri'?", prompt_en: "Which form is typically more common in speech for 'worked yesterday'?", options: &["Ich arbeitete gestern.", "Ich habe gestern gearbeitet.", "Ich hatte gestern gearbeitet."], correct_index: 1, answer_text: "", explanation_it: "Nel parlato quotidiano si preferisce spesso il Perfekt.", explanation_en: "In everyday speech Perfekt is often preferred." },
            GrammarUnitExercise { prompt_it: "Quale verbo e' spesso usato al Praeteritum anche nel parlato?", prompt_en: "Which verb is often used in PrÃ¤teritum even in speech?", options: &["kaufen", "sein", "spielen"], correct_index: 1, answer_text: "", explanation_it: "sein, haben e i modali compaiono spesso al Praeteritum anche nel parlato.", explanation_en: "sein, haben and modals often appear in PrÃ¤teritum even in speech." },
            GrammarUnitExercise { prompt_it: "Completa (Perfekt): Wir ___ einen Film ___ . (sehen)", prompt_en: "Complete (Perfekt): Wir ___ einen Film ___ . (sehen)", options: &["haben ... gesehen", "sind ... gesehen", "haben ... sahen"], correct_index: 0, answer_text: "", explanation_it: "Perfekt di sehen: haben + gesehen.", explanation_en: "Perfekt of sehen: haben + gesehen." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma Praeteritum di 'haben' (ich).", prompt_en: "Type the PrÃ¤teritum form of 'haben' (ich).", options: &[], correct_index: 0, answer_text: "hatte", explanation_it: "Praeteritum: ich hatte.", explanation_en: "PrÃ¤teritum: ich hatte." },
        ],
    }
}

fn unit_a2_passiv_intro() -> GrammarUnit {
    GrammarUnit {
        id: "a2-passiv-intro",
        level: "A2",
        title_it: "Passivo (introduzione)",
        title_en: "Passive voice (introduction)",
        subtitle_it: "werden + Partizip II al presente",
        subtitle_en: "werden + Partizip II in the present",
        objective_it: "Riconoscere e formare frasi passive semplici al presente.",
        objective_en: "Recognize and form simple passive sentences in the present.",
        theory_it: &[
            "Il passivo al presente si forma con werden (coniugato) + Partizip II alla fine: Das Haus wird gebaut.",
            "Il soggetto dell'attivo diventa spesso complemento con von + dativo (facoltativo): Das Haus wird von den Arbeitern gebaut.",
            "Il verbo werden al presente: ich werde, du wirst, er/sie/es wird, wir werden, ihr werdet, sie/Sie werden.",
        ],
        theory_en: &[
            "The present passive is formed with werden (conjugated) + Partizip II at the end: Das Haus wird gebaut.",
            "The active subject often becomes a von + dative phrase (optional): Das Haus wird von den Arbeitern gebaut.",
            "werden in present: ich werde, du wirst, er/sie/es wird, wir werden, ihr werdet, sie/Sie werden.",
        ],
        examples: &[
            ("Die Tuer wird geoeffnet.", "La porta viene aperta."),
            ("Das Auto wird repariert.", "L'auto viene riparata."),
            ("Die Briefe werden geschrieben.", "Le lettere vengono scritte."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa il passivo: Das Haus ___ gebaut. (werden)", prompt_en: "Complete the passive: Das Haus ___ gebaut. (werden)", options: &["wird", "werde", "werden"], correct_index: 0, answer_text: "", explanation_it: "Soggetto singolare (das Haus): wird.", explanation_en: "Singular subject (das Haus): wird." },
            GrammarUnitExercise { prompt_it: "Scegli la frase passiva corretta.", prompt_en: "Choose the correct passive sentence.", options: &["Der Brief schreibt.", "Der Brief wird geschrieben.", "Der Brief ist schreiben."], correct_index: 1, answer_text: "", explanation_it: "Passivo presente: wird + Partizip II (geschrieben).", explanation_en: "Present passive: wird + Partizip II (geschrieben)." },
            GrammarUnitExercise { prompt_it: "Scrivi il participio: reparieren -> ___ (nel passivo: Das Auto wird ___)", prompt_en: "Type the participle: reparieren -> ___ (in passive: Das Auto wird ___)", options: &[], correct_index: 0, answer_text: "repariert", explanation_it: "Partizip II di reparieren: repariert.", explanation_en: "Partizip II of reparieren: repariert." },
            GrammarUnitExercise { prompt_it: "Quale ausiliare forma il passivo di processo?", prompt_en: "Which auxiliary forms the process passive?", options: &["sein", "haben", "werden"], correct_index: 2, answer_text: "", explanation_it: "Il passivo di processo (Vorgangspassiv) usa werden + Partizip II.", explanation_en: "The process passive (Vorgangspassiv) uses werden + Partizip II." },
        ],
    }
}

fn unit_b1_genitiv_intro() -> GrammarUnit {
    GrammarUnit {
        id: "b1-genitiv-intro",
        level: "B1",
        title_it: "Genitivo (introduzione)",
        title_en: "Genitive case (introduction)",
        subtitle_it: "Possesso e relazioni tra nomi",
        subtitle_en: "Possession and relations between nouns",
        objective_it: "Riconoscere le forme base del genitivo e il suo uso.",
        objective_en: "Recognize basic genitive forms and usage.",
        theory_it: &[
            "Il genitivo esprime appartenenza/relazione: das Auto des Mannes (l'auto dell'uomo).",
            "Articoli: des (m/n), der (f/pl). I nomi maschili/neutri spesso aggiungono -s/-es (des Mannes, des Kindes).",
            "Preposizioni comuni con genitivo: wegen, trotz, waehrend, statt (uso varia nel parlato).",
        ],
        theory_en: &[
            "The genitive expresses possession/relation: das Auto des Mannes (the man's car).",
            "Articles: des (m/n), der (f/pl). Masculine/neuter nouns often add -s/-es (des Mannes, des Kindes).",
            "Common genitive prepositions: wegen, trotz, waehrend, statt (usage varies in speech).",
        ],
        examples: &[
            ("Das ist das Haus meiner Schwester.", "Questa e' la casa di mia sorella."),
            ("Wegen des Wetters bleiben wir zu Hause.", "A causa del tempo restiamo a casa."),
            ("Trotz des Regens gehen wir spazieren.", "Nonostante la pioggia andiamo a passeggiare."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: das Auto ___ Mannes (der)", prompt_en: "Complete: das Auto ___ Mannes (der)", options: &["der", "des", "dem"], correct_index: 1, answer_text: "", explanation_it: "Genitivo maschile: der -> des (des Mannes).", explanation_en: "Masculine genitive: der -> des (des Mannes)." },
            GrammarUnitExercise { prompt_it: "Completa: die Tasche ___ Frau (die)", prompt_en: "Complete: die Tasche ___ Frau (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "Genitivo femminile: die -> der (der Frau).", explanation_en: "Feminine genitive: die -> der (der Frau)." },
            GrammarUnitExercise { prompt_it: "Quale preposizione richiede tipicamente il genitivo (standard)?", prompt_en: "Which preposition typically takes the genitive (standard)?", options: &["mit", "wegen", "fuer"], correct_index: 1, answer_text: "", explanation_it: "wegen regge il genitivo nello standard (wegen des Wetters).", explanation_en: "wegen takes the genitive in standard usage (wegen des Wetters)." },
            GrammarUnitExercise { prompt_it: "Scrivi l'articolo: das Zimmer ___ Kindes (das)", prompt_en: "Type the article: das Zimmer ___ Kindes (das)", options: &[], correct_index: 0, answer_text: "des", explanation_it: "Genitivo neutro: das -> des (des Kindes).", explanation_en: "Neuter genitive: das -> des (des Kindes)." },
        ],
    }
}

fn unit_a2_wechselpraepositionen() -> GrammarUnit {
    GrammarUnit {
        id: "a2-wechselpraepositionen",
        level: "A2",
        title_it: "Preposizioni a doppio caso (WechselprÃ¤positionen)",
        title_en: "Two-way prepositions (WechselprÃ¤positionen)",
        subtitle_it: "Dativo (dove?) o accusativo (verso dove?)",
        subtitle_en: "Dative (where?) or accusative (where to?)",
        objective_it: "Scegliere dativo o accusativo con an, auf, in, neben, Ã¼ber, unter, vor, zwischen, hinter.",
        objective_en: "Choose dative or accusative with an, auf, in, neben, Ã¼ber, unter, vor, zwischen, hinter.",
        theory_it: &[
            "Le WechselprÃ¤positionen reggono il dativo per indicare posizione/stato (Wo?) e l'accusativo per direzione/movimento (Wohin?).",
            "Preposizioni: an, auf, hinter, in, neben, Ã¼ber, unter, vor, zwischen.",
            "Esempio: Ich bin in der KÃ¼che. (Wo? dativo) / Ich gehe in die KÃ¼che. (Wohin? accusativo).",
        ],
        theory_en: &[
            "Two-way prepositions take the dative for location/state (Wo?) and the accusative for direction/movement (Wohin?).",
            "Prepositions: an, auf, hinter, in, neben, Ã¼ber, unter, vor, zwischen.",
            "Example: Ich bin in der KÃ¼che. (Where? dative) / Ich gehe in die KÃ¼che. (Where to? accusative).",
        ],
        examples: &[
            ("Das Buch liegt auf dem Tisch.", "Il libro Ã¨ sul tavolo. (posizione)"),
            ("Ich lege das Buch auf den Tisch.", "Metto il libro sul tavolo. (direzione)"),
            ("Wir sitzen im (in dem) Garten.", "Siamo seduti in giardino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (posizione, Wo?): Ich bin in ___ KÃ¼che. (die)", prompt_en: "Complete (location, Wo?): Ich bin in ___ KÃ¼che. (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "Posizione -> dativo femminile: in der KÃ¼che.", explanation_en: "Location -> dative feminine: in der KÃ¼che." },
            GrammarUnitExercise { prompt_it: "Completa (direzione, Wohin?): Ich gehe in ___ KÃ¼che. (die)", prompt_en: "Complete (direction, Wohin?): Ich gehe in ___ KÃ¼che. (die)", options: &["der", "die", "den"], correct_index: 1, answer_text: "", explanation_it: "Direzione -> accusativo femminile (uguale forma): in die KÃ¼che.", explanation_en: "Direction -> accusative feminine: in die KÃ¼che." },
            GrammarUnitExercise { prompt_it: "Scegli: Das Buch liegt auf ___ Tisch. (der, posizione)", prompt_en: "Choose: Das Buch liegt auf ___ Tisch. (der, location)", options: &["der", "den", "dem"], correct_index: 2, answer_text: "", explanation_it: "liegen = posizione -> dativo maschile: auf dem Tisch.", explanation_en: "liegen = location -> dative masculine: auf dem Tisch." },
            GrammarUnitExercise { prompt_it: "Scrivi l'articolo: Ich stelle die Lampe auf ___ Tisch. (der, direzione)", prompt_en: "Type the article: Ich stelle die Lampe auf ___ Tisch. (der, direction)", options: &[], correct_index: 0, answer_text: "den", explanation_it: "stellen = direzione -> accusativo maschile: auf den Tisch.", explanation_en: "stellen = direction -> accusative masculine: auf den Tisch." },
        ],
    }
}

fn unit_b1_konjunktiv2_intro() -> GrammarUnit {
    GrammarUnit {
        id: "b1-konjunktiv2-intro",
        level: "B1",
        title_it: "Konjunktiv II (introduzione)",
        title_en: "Konjunktiv II (introduction)",
        subtitle_it: "Desideri, richieste cortesi e situazioni irreali",
        subtitle_en: "Wishes, polite requests, and unreal situations",
        objective_it: "Usare forme base del Konjunktiv II (wÃ¼rde, hÃ¤tte, wÃ¤re, kÃ¶nnte).",
        objective_en: "Use basic Konjunktiv II forms (wÃ¼rde, hÃ¤tte, wÃ¤re, kÃ¶nnte).",
        theory_it: &[
            "Il Konjunktiv II esprime desideri, ipotesi irreali e richieste cortesi.",
            "Forme comuni: ich wÃ¤re (sarei), ich hÃ¤tte (avrei), ich kÃ¶nnte (potrei), ich wÃ¼rde + infinito (farei).",
            "Esempi: Ich hÃ¤tte gern einen Kaffee. / KÃ¶nntest du mir helfen? / Wenn ich Zeit hÃ¤tte, wÃ¼rde ich reisen.",
        ],
        theory_en: &[
            "Konjunktiv II expresses wishes, unreal hypotheses, and polite requests.",
            "Common forms: ich wÃ¤re (would be), ich hÃ¤tte (would have), ich kÃ¶nnte (could), ich wÃ¼rde + infinitive (would).",
            "Examples: Ich hÃ¤tte gern einen Kaffee. / KÃ¶nntest du mir helfen? / Wenn ich Zeit hÃ¤tte, wÃ¼rde ich reisen.",
        ],
        examples: &[
            ("Ich hÃ¤tte gern ein Wasser.", "Vorrei un'acqua."),
            ("KÃ¶nnten Sie das bitte wiederholen?", "Potrebbe ripeterlo, per favore?"),
            ("Wenn ich reich wÃ¤re, wÃ¼rde ich viel reisen.", "Se fossi ricco, viaggerei molto."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (richiesta cortese): ___ Sie mir helfen?", prompt_en: "Complete (polite request): ___ Sie mir helfen?", options: &["KÃ¶nnen", "KÃ¶nnten", "Konnten"], correct_index: 1, answer_text: "", explanation_it: "Richiesta cortese: Konjunktiv II -> KÃ¶nnten Sie...?", explanation_en: "Polite request: Konjunktiv II -> KÃ¶nnten Sie...?" },
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: Ich ___ gern einen Tee. (haben)", prompt_en: "Choose the correct form: Ich ___ gern einen Tee. (haben)", options: &["habe", "hatte", "hÃ¤tte"], correct_index: 2, answer_text: "", explanation_it: "Desiderio cortese: ich hÃ¤tte gern...", explanation_en: "Polite wish: ich hÃ¤tte gern..." },
            GrammarUnitExercise { prompt_it: "Completa: Wenn ich frei ___, wÃ¼rde ich kommen. (sein)", prompt_en: "Complete: Wenn ich frei ___, wÃ¼rde ich kommen. (sein)", options: &["bin", "war", "wÃ¤re"], correct_index: 2, answer_text: "", explanation_it: "Irreale/condizionale con sein: ich wÃ¤re.", explanation_en: "Unreal/conditional with sein: ich wÃ¤re." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: ich ___ (sein, Konjunktiv II)", prompt_en: "Type the form: ich ___ (sein, Konjunktiv II)", options: &[], correct_index: 0, answer_text: "wÃ¤re", explanation_it: "Konjunktiv II di sein: ich wÃ¤re.", explanation_en: "Konjunktiv II of sein: ich wÃ¤re." },
        ],
    }
}

fn unit_a2_praepositionen_akkusativ() -> GrammarUnit {
    GrammarUnit {
        id: "a2-praepositionen-akkusativ",
        level: "A2",
        title_it: "Preposizioni con accusativo",
        title_en: "Prepositions with accusative",
        subtitle_it: "durch, fÃ¼r, gegen, ohne, um",
        subtitle_en: "durch, fÃ¼r, gegen, ohne, um",
        objective_it: "Usare le preposizioni piÃ¹ comuni che richiedono l'accusativo.",
        objective_en: "Use common prepositions that take the accusative.",
        theory_it: &[
            "Alcune preposizioni reggono sempre l'accusativo: durch, fÃ¼r, gegen, ohne, um.",
            "Dopo queste preposizioni l'articolo maschile diventa den; femminile/neutro restano die/das.",
            "Esempio: fÃ¼r den Mann, ohne die Tasche, durch das Fenster.",
        ],
        theory_en: &[
            "Some prepositions always take the accusative: durch, fÃ¼r, gegen, ohne, um.",
            "After these, the masculine article becomes den; feminine/neuter remain die/das.",
            "Example: fÃ¼r den Mann, ohne die Tasche, durch das Fenster.",
        ],
        examples: &[
            ("Das Geschenk ist fÃ¼r dich.", "Il regalo Ã¨ per te."),
            ("Wir gehen durch den Park.", "Attraversiamo il parco."),
            ("Ich gehe ohne meinen Bruder.", "Vado senza mio fratello."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Das ist fÃ¼r ___ Mann. (der)", prompt_en: "Complete: Das ist fÃ¼r ___ Mann. (der)", options: &["der", "den", "dem"], correct_index: 1, answer_text: "", explanation_it: "fÃ¼r + accusativo maschile: den Mann.", explanation_en: "fÃ¼r + accusative masculine: den Mann." },
            GrammarUnitExercise { prompt_it: "Completa: Wir fahren ohne ___ Auto. (das)", prompt_en: "Complete: Wir fahren ohne ___ Auto. (das)", options: &["dem", "das", "den"], correct_index: 1, answer_text: "", explanation_it: "Neutro all'accusativo resta das: ohne das Auto.", explanation_en: "Neuter accusative stays das: ohne das Auto." },
            GrammarUnitExercise { prompt_it: "Quale preposizione richiede l'accusativo?", prompt_en: "Which preposition takes the accusative?", options: &["mit", "bei", "gegen"], correct_index: 2, answer_text: "", explanation_it: "gegen regge l'accusativo; mit/bei il dativo.", explanation_en: "gegen takes accusative; mit/bei take dative." },
            GrammarUnitExercise { prompt_it: "Scrivi l'articolo: durch ___ Park (der)", prompt_en: "Type the article: durch ___ Park (der)", options: &[], correct_index: 0, answer_text: "den", explanation_it: "durch + accusativo maschile: den Park.", explanation_en: "durch + accusative masculine: den Park." },
        ],
    }
}

fn unit_b1_passiv_modalverben_intro() -> GrammarUnit {
    GrammarUnit {
        id: "b1-passiv-modalverben-intro",
        level: "B1",
        title_it: "Passivo con verbi modali (introduzione)",
        title_en: "Passive with modal verbs (introduction)",
        subtitle_it: "Modalverb + Partizip II + werden",
        subtitle_en: "Modalverb + Partizip II + werden",
        objective_it: "Riconoscere e formare frasi passive con modali (muss/kann/soll).",
        objective_en: "Recognize and form passive sentences with modals (muss/kann/soll).",
        theory_it: &[
            "Con i verbi modali il passivo si forma: modale coniugato + Partizip II + werden (infinito finale).",
            "Esempio: Das muss gemacht werden. / Die TÃ¼r kann geÃ¶ffnet werden.",
            "Il modale va in posizione 2 nella frase principale; 'werden' resta alla fine.",
        ],
        theory_en: &[
            "With modal verbs the passive is formed: conjugated modal + Partizip II + werden (infinitive at the end).",
            "Example: Das muss gemacht werden. / Die TÃ¼r kann geÃ¶ffnet werden.",
            "The modal is in position 2 in the main clause; 'werden' stays at the end.",
        ],
        examples: &[
            ("Die Aufgabe muss heute gemacht werden.", "Il compito deve essere fatto oggi."),
            ("Das Fenster kann geÃ¶ffnet werden.", "La finestra puÃ² essere aperta."),
            ("Die Regeln sollen beachtet werden.", "Le regole devono essere rispettate."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: Die Arbeit ___ erledigt werden. (mÃ¼ssen)", prompt_en: "Choose the correct form: Die Arbeit ___ erledigt werden. (mÃ¼ssen)", options: &["muss", "musst", "mÃ¼ssen"], correct_index: 0, answer_text: "", explanation_it: "Soggetto singolare (die Arbeit): muss erledigt werden.", explanation_en: "Singular subject (die Arbeit): muss erledigt werden." },
            GrammarUnitExercise { prompt_it: "Completa: Das ___ gemacht werden. (kÃ¶nnen)", prompt_en: "Complete: Das ___ gemacht werden. (kÃ¶nnen)", options: &["kann", "kannst", "kÃ¶nnen"], correct_index: 0, answer_text: "", explanation_it: "Das -> kann gemacht werden.", explanation_en: "Das -> kann gemacht werden." },
            GrammarUnitExercise { prompt_it: "Qual Ã¨ l'ordine corretto?", prompt_en: "Which order is correct?", options: &["Das muss werden gemacht.", "Das muss gemacht werden.", "Das gemacht muss werden."], correct_index: 1, answer_text: "", explanation_it: "Modale + Partizip II + werden: muss gemacht werden.", explanation_en: "Modal + Partizip II + werden: muss gemacht werden." },
            GrammarUnitExercise { prompt_it: "Scrivi il modale: Die TÃ¼r ___ geschlossen werden. (sollen, singolare)", prompt_en: "Type the modal: Die TÃ¼r ___ geschlossen werden. (sollen, singular)", options: &[], correct_index: 0, answer_text: "soll", explanation_it: "Singolare: die TÃ¼r soll geschlossen werden.", explanation_en: "Singular: die TÃ¼r soll geschlossen werden." },
        ],
    }
}


fn unit_a2_praeteritum() -> GrammarUnit {
    GrammarUnit {
        id: "a2-praeteritum",
        level: "A2",
        title_it: "Il PrÃ¤teritum",
        title_en: "PrÃ¤teritum (simple past)",
        subtitle_it: "Il passato della narrazione",
        subtitle_en: "The narrative past tense",
        objective_it: "Usare il PrÃ¤teritum con verbi regolari, irregolari e modali comuni.",
        objective_en: "Use PrÃ¤teritum with regular, irregular, and common modal verbs.",
        theory_it: &[
            "Il PrÃ¤teritum Ã¨ il passato semplice, frequente nello scritto e nei racconti.",
            "Verbi regolari: radice + -te (machen -> machte). Irregolari: cambio di radice (gehen -> ging).",
            "Forme importanti: sein -> war, haben -> hatte, modali -> konnte, musste, wollte, durfte, sollte.",
        ],
        theory_en: &[
            "PrÃ¤teritum is the simple past, common in writing and narration.",
            "Regular verbs: stem + -te (machen -> machte). Irregular verbs change the stem (gehen -> ging).",
            "Important forms: sein -> war, haben -> hatte, modals -> konnte, musste, wollte, durfte, sollte.",
        ],
        examples: &[
            ("Ich war gestern im Kino.", "Ieri ero al cinema."),
            ("Wir hatten keine Zeit.", "Non avevamo tempo."),
            ("Er ging nach Berlin.", "AndÃ² a Berlino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ gestern im Kino. (sein)", prompt_en: "Complete: Ich ___ gestern im Kino. (sein)", options: &["war", "warst", "bin"], correct_index: 0, answer_text: "", explanation_it: "sein al PrÃ¤teritum: ich war.", explanation_en: "sein in PrÃ¤teritum: ich war." },
            GrammarUnitExercise { prompt_it: "Completa: Wir ___ keine Zeit. (haben)", prompt_en: "Complete: Wir ___ keine Zeit. (haben)", options: &["hatte", "hatten", "haben"], correct_index: 1, answer_text: "", explanation_it: "haben al PrÃ¤teritum: wir hatten.", explanation_en: "haben in PrÃ¤teritum: wir hatten." },
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: Er ___ nach Berlin. (gehen)", prompt_en: "Choose the correct form: Er ___ nach Berlin. (gehen)", options: &["gehte", "ging", "gegangen"], correct_index: 1, answer_text: "", explanation_it: "gehen Ã¨ irregolare: er ging.", explanation_en: "gehen is irregular: er ging." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: ich ___ (machen, PrÃ¤teritum)", prompt_en: "Type the form: ich ___ (machen, PrÃ¤teritum)", options: &[], correct_index: 0, answer_text: "machte", explanation_it: "Verbo regolare: machen -> machte.", explanation_en: "Regular verb: machen -> machte." },
        ],
    }
}

fn unit_b1_indirekte_rede() -> GrammarUnit {
    GrammarUnit {
        id: "b1-indirekte-rede",
        level: "B1",
        title_it: "Il discorso indiretto",
        title_en: "Indirect speech",
        subtitle_it: "Konjunktiv I e domande riportate",
        subtitle_en: "Konjunktiv I and reported questions",
        objective_it: "Riferire affermazioni e domande senza virgolette.",
        objective_en: "Report statements and questions without quotation marks.",
        theory_it: &[
            "Nel discorso indiretto si riporta ciÃ² che dice qualcuno; spesso si usa il Konjunktiv I (sei, habe, komme).",
            "Se il Konjunktiv I coincide con l'indicativo, si puÃ² usare il Konjunktiv II per chiarezza.",
            "Domande indirette: ob per sÃ¬/no, W-Wort per le altre; il verbo va alla fine.",
        ],
        theory_en: &[
            "In indirect speech you report what someone said; Konjunktiv I is often used (sei, habe, komme).",
            "If Konjunktiv I matches the indicative, Konjunktiv II can be used for clarity.",
            "Indirect questions: ob for yes/no, a W-word for others; the verb goes to the end.",
        ],
        examples: &[
            ("Er sagt, er sei mÃ¼de.", "Dice che Ã¨ stanco."),
            ("Sie fragt, ob ich Zeit habe.", "Chiede se ho tempo."),
            ("Er fragt, wo ich wohne.", "Chiede dove abito."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Er sagt, er ___ krank. (sein)", prompt_en: "Complete: Er sagt, er ___ krank. (sein)", options: &["ist", "sei", "war"], correct_index: 1, answer_text: "", explanation_it: "Konjunktiv I di sein: er sei.", explanation_en: "Konjunktiv I of sein: er sei." },
            GrammarUnitExercise { prompt_it: "Completa: Sie sagt, sie ___ keine Zeit. (haben)", prompt_en: "Complete: Sie sagt, sie ___ keine Zeit. (haben)", options: &["hat", "habe", "hatte"], correct_index: 1, answer_text: "", explanation_it: "Konjunktiv I di haben: sie habe.", explanation_en: "Konjunktiv I of haben: sie habe." },
            GrammarUnitExercise { prompt_it: "Domanda indiretta sÃ¬/no: Er fragt, ___ ich komme.", prompt_en: "Indirect yes/no question: Er fragt, ___ ich komme.", options: &["dass", "ob", "weil"], correct_index: 1, answer_text: "", explanation_it: "Le domande indirette sÃ¬/no usano ob.", explanation_en: "Indirect yes/no questions use ob." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: sie ___ (kommen, Konjunktiv I)", prompt_en: "Type the form: sie ___ (kommen, Konjunktiv I)", options: &[], correct_index: 0, answer_text: "komme", explanation_it: "Konjunktiv I: sie komme.", explanation_en: "Konjunktiv I: sie komme." },
        ],
    }
}

fn unit_b1_nominalisierung() -> GrammarUnit {
    GrammarUnit {
        id: "b1-nominalisierung",
        level: "B1",
        title_it: "La nominalizzazione",
        title_en: "Nominalization",
        subtitle_it: "Trasformare verbi e aggettivi in nomi",
        subtitle_en: "Turning verbs and adjectives into nouns",
        objective_it: "Riconoscere e usare nomi derivati da verbi e aggettivi.",
        objective_en: "Recognize and use nouns derived from verbs and adjectives.",
        theory_it: &[
            "In tedesco verbi e aggettivi possono diventare nomi; i nomi si scrivono sempre con la maiuscola.",
            "L'infinito sostantivato Ã¨ neutro: das Essen, das Lernen. Suffissi comuni: -ung, -heit, -keit, -schaft.",
            "Spesso con preposizioni: beim Lernen, zum Essen, nach dem Schlafen.",
        ],
        theory_en: &[
            "In German verbs and adjectives can become nouns; nouns are always capitalized.",
            "Nominalized infinitives are neuter: das Essen, das Lernen. Common suffixes: -ung, -heit, -keit, -schaft.",
            "Often used with prepositions: beim Lernen, zum Essen, nach dem Schlafen.",
        ],
        examples: &[
            ("Das Lernen macht SpaÃŸ.", "Imparare Ã¨ divertente."),
            ("Die PrÃ¼fung war schwierig.", "L'esame Ã¨ stato difficile."),
            ("Die Freiheit ist wichtig.", "La libertÃ  Ã¨ importante."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: ___ ist wichtig. (lernen)", prompt_en: "Choose the correct form: ___ ist wichtig. (lernen)", options: &["lernen", "Das Lernen", "der Lernen"], correct_index: 1, answer_text: "", explanation_it: "Infinito sostantivato neutro: das Lernen.", explanation_en: "Nominalized infinitive (neuter): das Lernen." },
            GrammarUnitExercise { prompt_it: "Completa: Die ___ war schwierig. (prÃ¼fen)", prompt_en: "Complete: Die ___ war schwierig. (prÃ¼fen)", options: &["PrÃ¼fen", "PrÃ¼fung", "PrÃ¼fheit"], correct_index: 1, answer_text: "", explanation_it: "Da prÃ¼fen: die PrÃ¼fung (-ung).", explanation_en: "From prÃ¼fen: die PrÃ¼fung (-ung)." },
            GrammarUnitExercise { prompt_it: "Completa: Die ___ ist wichtig. (frei)", prompt_en: "Complete: Die ___ ist wichtig. (frei)", options: &["Frei", "Freiheit", "Freikeit"], correct_index: 1, answer_text: "", explanation_it: "Da frei: die Freiheit (-heit).", explanation_en: "From frei: die Freiheit (-heit)." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: Ich gehe zum ___ (essen).", prompt_en: "Type the form: Ich gehe zum ___ (essen).", options: &[], correct_index: 0, answer_text: "Essen", explanation_it: "Nominalizzazione: zum Essen.", explanation_en: "Nominalization: zum Essen." },
        ],
    }
}


fn unit_a2_riflessivi() -> GrammarUnit {
    GrammarUnit {
        id: "a2-riflessivi",
        level: "A2",
        title_it: "I verbi riflessivi",
        title_en: "Reflexive verbs",
        subtitle_it: "sich freuen, sich interessieren, sich beeilen",
        subtitle_en: "sich freuen, sich interessieren, sich beeilen",
        objective_it: "Usare i pronomi riflessivi e i verbi riflessivi piÃ¹ comuni.",
        objective_en: "Use reflexive pronouns and common reflexive verbs.",
        theory_it: &[
            "I verbi riflessivi hanno un pronome che rimanda al soggetto: mich, dich, sich, uns, euch, sich.",
            "La maggior parte usa l'accusativo; con parti del corpo si usa spesso il dativo: Ich wasche mir die HÃ¤nde.",
            "Verbi frequenti: sich freuen, sich interessieren fÃ¼r, sich treffen, sich beeilen, sich erholen, sich setzen.",
        ],
        theory_en: &[
            "Reflexive verbs have a pronoun referring back to the subject: mich, dich, sich, uns, euch, sich.",
            "Most use the accusative; with body parts the dative is often used: Ich wasche mir die HÃ¤nde.",
            "Common verbs: sich freuen, sich interessieren fÃ¼r, sich treffen, sich beeilen, sich erholen, sich setzen.",
        ],
        examples: &[
            ("Ich freue mich auf den Urlaub.", "Non vedo l'ora che arrivino le vacanze."),
            ("Wir treffen uns um 8 Uhr.", "Ci incontriamo alle 8."),
            ("Ich wasche mir die HÃ¤nde.", "Mi lavo le mani."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich freue ___ auf den Urlaub.", prompt_en: "Complete: Ich freue ___ auf den Urlaub.", options: &["mich", "mir", "dich"], correct_index: 0, answer_text: "", explanation_it: "sich freuen all'accusativo: ich freue mich.", explanation_en: "sich freuen takes accusative: ich freue mich." },
            GrammarUnitExercise { prompt_it: "Completa: Wir treffen ___ um 8 Uhr.", prompt_en: "Complete: Wir treffen ___ um 8 Uhr.", options: &["uns", "euch", "sich"], correct_index: 0, answer_text: "", explanation_it: "Con wir il pronome riflessivo Ã¨ uns.", explanation_en: "With wir the reflexive pronoun is uns." },
            GrammarUnitExercise { prompt_it: "Completa: Er beeilt ___.", prompt_en: "Complete: Er beeilt ___.", options: &["mich", "dich", "sich"], correct_index: 2, answer_text: "", explanation_it: "Con er/sie/es il pronome Ã¨ sich.", explanation_en: "With er/sie/es the pronoun is sich." },
            GrammarUnitExercise { prompt_it: "Scrivi il pronome: Ich wasche ___ die HÃ¤nde. (dativo)", prompt_en: "Type the pronoun: Ich wasche ___ die HÃ¤nde. (dative)", options: &[], correct_index: 0, answer_text: "mir", explanation_it: "Con una parte del corpo il riflessivo Ã¨ al dativo: mir.", explanation_en: "With a body part the reflexive pronoun is dative: mir." },
        ],
    }
}

fn unit_a2_subordinate() -> GrammarUnit {
    GrammarUnit {
        id: "a2-subordinate",
        level: "A2",
        title_it: "Le frasi subordinate",
        title_en: "Subordinate clauses",
        subtitle_it: "weil, dass, wenn, als, obwohl, ob: verbo alla fine",
        subtitle_en: "weil, dass, wenn, als, obwohl, ob: verb at the end",
        objective_it: "Collegare frasi con congiunzioni subordinate e mettere il verbo alla fine.",
        objective_en: "Link clauses with subordinating conjunctions and put the verb at the end.",
        theory_it: &[
            "Nella frase subordinata il verbo coniugato va alla fine: Ich bleibe zu Hause, weil ich krank bin.",
            "Congiunzioni comuni: weil (perchÃ©), dass (che), wenn (se/quando), als (quando, passato singolo), obwohl (sebbene), ob (se, domande indirette).",
            "Se la subordinata viene prima, la principale inizia con il verbo: Weil ich krank bin, bleibe ich zu Hause.",
        ],
        theory_en: &[
            "In a subordinate clause the conjugated verb goes to the end: Ich bleibe zu Hause, weil ich krank bin.",
            "Common conjunctions: weil (because), dass (that), wenn (if/when), als (when, single past event), obwohl (although), ob (whether).",
            "If the subordinate clause comes first, the main clause starts with the verb: Weil ich krank bin, bleibe ich zu Hause.",
        ],
        examples: &[
            ("Ich bleibe zu Hause, weil ich krank bin.", "Resto a casa perchÃ© sono malato."),
            ("Er sagt, dass er keine Zeit hat.", "Dice che non ha tempo."),
            ("Als ich klein war, wohnte ich in Rom.", "Quando ero piccolo, abitavo a Roma."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich bleibe zu Hause, ___ ich krank bin.", prompt_en: "Complete: Ich bleibe zu Hause, ___ ich krank bin.", options: &["weil", "denn", "aber"], correct_index: 0, answer_text: "", explanation_it: "weil introduce una subordinata con verbo alla fine.", explanation_en: "weil introduces a subordinate clause with the verb at the end." },
            GrammarUnitExercise { prompt_it: "Completa: Er sagt, ___ er keine Zeit hat.", prompt_en: "Complete: Er sagt, ___ he has no time.", options: &["weil", "dass", "wenn"], correct_index: 1, answer_text: "", explanation_it: "dass introduce una frase completiva.", explanation_en: "dass introduces a complement clause." },
            GrammarUnitExercise { prompt_it: "Evento passato singolo: ___ ich klein war, wohnte ich in Rom.", prompt_en: "Single past event: ___ ich klein war, wohnte ich in Rom.", options: &["Wenn", "Als", "Ob"], correct_index: 1, answer_text: "", explanation_it: "Per un singolo evento nel passato si usa als.", explanation_en: "For a single event in the past use als." },
            GrammarUnitExercise { prompt_it: "Scrivi la congiunzione: Ich weiÃŸ nicht, ___ er kommt. (domanda indiretta sÃ¬/no)", prompt_en: "Type the conjunction: Ich weiÃŸ nicht, ___ er kommt. (indirect yes/no question)", options: &[], correct_index: 0, answer_text: "ob", explanation_it: "Le domande indirette sÃ¬/no usano ob.", explanation_en: "Indirect yes/no questions use ob." },
        ],
    }
}


fn unit_a2_relative() -> GrammarUnit {
    GrammarUnit {
        id: "a2-relative",
        level: "A2",
        title_it: "Le frasi relative",
        title_en: "Relative clauses",
        subtitle_it: "der, die, das: descrivere persone e cose",
        subtitle_en: "der, die, das: describing people and things",
        objective_it: "Usare i pronomi relativi corretti e mettere il verbo alla fine della relativa.",
        objective_en: "Use correct relative pronouns and put the verb at the end of the relative clause.",
        theory_it: &[
            "La frase relativa descrive un sostantivo; il verbo coniugato va alla fine.",
            "Genere e numero vengono dall'antecedente; il caso dipende dalla funzione nella relativa.",
            "Forme base: nominativo der/die/das/die, accusativo den/die/das/die, dativo dem/der/dem/denen.",
            "Con preposizione la preposizione resta davanti: Der Mann, mit dem ich spreche...",
        ],
        theory_en: &[
            "A relative clause describes a noun; the conjugated verb goes to the end.",
            "Gender and number come from the antecedent; case depends on the function in the relative clause.",
            "Basic forms: nominative der/die/das/die, accusative den/die/das/die, dative dem/der/dem/denen.",
            "With a preposition it stays in front: Der Mann, mit dem ich spreche...",
        ],
        examples: &[
            ("Der Mann, der dort steht, ist mein Chef.", "L'uomo che sta lÃ  Ã¨ il mio capo."),
            ("Das Buch, das ich lese, ist spannend.", "Il libro che leggo Ã¨ avvincente."),
            ("Die Frau, der ich helfe, ist meine Nachbarin.", "La donna a cui aiuto Ã¨ la mia vicina."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (soggetto): Der Mann, ___ dort steht, ist mein Chef.", prompt_en: "Complete (subject): Der Mann, ___ dort steht, ist mein Chef.", options: &["der", "den", "dem"], correct_index: 0, answer_text: "", explanation_it: "Soggetto maschile nella relativa: der.", explanation_en: "Masculine subject in the relative clause: der." },
            GrammarUnitExercise { prompt_it: "Completa (oggetto neutro): Das Buch, ___ ich lese, ist spannend.", prompt_en: "Complete (neuter object): Das Buch, ___ ich lese, ist spannend.", options: &["der", "das", "dem"], correct_index: 1, answer_text: "", explanation_it: "Neutro all'accusativo nella relativa: das.", explanation_en: "Neuter accusative in the relative clause: das." },
            GrammarUnitExercise { prompt_it: "Completa (dativo): Die Frau, ___ ich helfe, ist nett.", prompt_en: "Complete (dative): Die Frau, ___ ich helfe, is nice.", options: &["die", "der", "denen"], correct_index: 1, answer_text: "", explanation_it: "helfen regge il dativo femminile: der.", explanation_en: "helfen takes dative feminine: der." },
            GrammarUnitExercise { prompt_it: "Scrivi il pronome: Die Kinder, mit ___ ich spiele, sind nett. (plurale dativo)", prompt_en: "Type the pronoun: Die Kinder, mit ___ ich spiele, sind nett. (plural dative)", options: &[], correct_index: 0, answer_text: "denen", explanation_it: "Plurale al dativo nella relativa: denen.", explanation_en: "Plural dative in the relative clause: denen." },
        ],
    }
}


fn unit_a1_possessivi_negazione() -> GrammarUnit {
    GrammarUnit {
        id: "a1-possessivi",
        level: "A1",
        title_it: "Possessivi e negazione",
        title_en: "Possessives and negation",
        subtitle_it: "Mio, tuo, suoâ€¦ e come dire Â«nonÂ»",
        subtitle_en: "My, your, hisâ€¦ and how to say â€œnot/noâ€",
        objective_it: "Usare i possessivi e distinguere kein da nicht.",
        objective_en: "Use possessives and distinguish kein from nicht.",
        theory_it: &[
            "I possessivi (mein, dein, sein, ihr, unser, euer, ihr/Ihr) prendono desinenze: nominativo maschile/neutro senza -e, femminile/plurale con -e.",
            "All'accusativo cambia solo il maschile: meinen Vater; femminile/neutro/plurale restano come al nominativo.",
            "kein nega un nome (come ein): kein/keine/keinen. nicht nega verbi, aggettivi o nomi propri.",
        ],
        theory_en: &[
            "Possessives (mein, dein, sein, ihr, unser, euer, ihr/Ihr) take endings: masculine/neuter nominative no -e, feminine/plural take -e.",
            "In the accusative only masculine changes: meinen Vater; feminine/neuter/plural stay the same.",
            "kein negates a noun (like ein): kein/keine/keinen. nicht negates verbs, adjectives, or proper names.",
        ],
        examples: &[
            ("Mein Vater arbeitet hier.", "Mio padre lavora qui."),
            ("Ich sehe meinen Bruder.", "Vedo mio fratello."),
            ("Ich habe keine Zeit.", "Non ho tempo."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (nominativo maschile): ___ Vater arbeitet hier. (ich)", prompt_en: "Complete (masculine nominative): ___ Vater arbeitet hier. (ich)", options: &["Mein", "Meine", "Meinen"], correct_index: 0, answer_text: "", explanation_it: "Maschile al nominativo: mein Vater.", explanation_en: "Masculine nominative: mein Vater." },
            GrammarUnitExercise { prompt_it: "Completa (accusativo maschile): Ich sehe ___ Bruder. (ich)", prompt_en: "Complete (masculine accusative): Ich sehe ___ Bruder. (ich)", options: &["mein", "meine", "meinen"], correct_index: 2, answer_text: "", explanation_it: "Accusativo maschile: meinen Bruder.", explanation_en: "Masculine accusative: meinen Bruder." },
            GrammarUnitExercise { prompt_it: "Scegli la negazione corretta: Ich habe ___ Zeit.", prompt_en: "Choose the correct negation: Ich habe ___ Zeit.", options: &["kein", "keine", "nicht"], correct_index: 1, answer_text: "", explanation_it: "Zeit Ã¨ femminile: keine Zeit.", explanation_en: "Zeit is feminine: keine Zeit." },
            GrammarUnitExercise { prompt_it: "Scrivi il possessivo: Das ist ___ Schwester. (er)", prompt_en: "Type the possessive: Das ist ___ Schwester. (er)", options: &[], correct_index: 0, answer_text: "seine", explanation_it: "Femminile al nominativo: seine Schwester.", explanation_en: "Feminine nominative: seine Schwester." },
        ],
    }
}


fn unit_a1_imperativo() -> GrammarUnit {
    GrammarUnit {
        id: "a1-imperativo",
        level: "A1",
        title_it: "L'imperativo",
        title_en: "The imperative",
        subtitle_it: "Dare ordini, consigli e istruzioni",
        subtitle_en: "Giving orders, advice, and instructions",
        objective_it: "Formare l'imperativo nelle forme du, ihr, Sie e wir.",
        objective_en: "Form the imperative in du, ihr, Sie, and wir forms.",
        theory_it: &[
            "Forma du: radice del verbo (senza -st), es. Mach! Komm! La -e finale Ã¨ spesso facoltativa.",
            "Forma ihr: radice + -t (Macht!); forma Sie: infinito + Sie (Machen Sie!); forma wir: infinito + wir (Gehen wir!).",
            "Irregolari importanti: sein -> sei/seid/seien Sie; con prefissi separabili il prefisso va in fondo: Steh auf!",
        ],
        theory_en: &[
            "du form: verb stem (without -st), e.g. Mach! Komm! Final -e is often optional.",
            "ihr form: stem + -t (Macht!); Sie form: infinitive + Sie (Machen Sie!); wir form: infinitive + wir (Gehen wir!).",
            "Important irregulars: sein -> sei/seid/seien Sie; separable prefixes go to the end: Steh auf!",
        ],
        examples: &[
            ("Mach bitte die TÃ¼r zu!", "Per favore, chiudi la porta!"),
            ("Kommt mit!", "Venite con noi!"),
            ("Warten Sie bitte hier!", "Aspetti qui, per favore!"),
            ("Sei ruhig!", "Stai calmo!"),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Imperativo du di machen:", prompt_en: "du imperative of machen:", options: &["Mach!", "Machst!", "Machen!"], correct_index: 0, answer_text: "", explanation_it: "Forma du: radice senza -st -> Mach!", explanation_en: "du form: stem without -st -> Mach!" },
            GrammarUnitExercise { prompt_it: "Imperativo ihr di gehen:", prompt_en: "ihr imperative of gehen:", options: &["Geh!", "Geht!", "Gehen!"], correct_index: 1, answer_text: "", explanation_it: "Forma ihr: radice + -t -> Geht!", explanation_en: "ihr form: stem + -t -> Geht!" },
            GrammarUnitExercise { prompt_it: "Imperativo Sie di warten:", prompt_en: "Sie imperative of warten:", options: &["Warte!", "Wartet!", "Warten Sie!"], correct_index: 2, answer_text: "", explanation_it: "Forma Sie: infinito + Sie -> Warten Sie!", explanation_en: "Sie form: infinitive + Sie -> Warten Sie!" },
            GrammarUnitExercise { prompt_it: "Scrivi l'imperativo du di sein:", prompt_en: "Type the du imperative of sein:", options: &[], correct_index: 0, answer_text: "Sei", explanation_it: "Imperativo irregolare di sein (du): Sei!", explanation_en: "Irregular du imperative of sein: Sei!" },
        ],
    }
}


fn unit_b1_futur_intro() -> GrammarUnit {
    GrammarUnit {
        id: "b1-futur",
        level: "B1",
        title_it: "Futuro e infinito con zu",
        title_en: "Future and infinitive with zu",
        subtitle_it: "Piani, previsioni e verbi che reggono zu",
        subtitle_en: "Plans, predictions, and verbs that take zu",
        objective_it: "Formare il Futur I e usare l'infinito con zu nei casi comuni.",
        objective_en: "Form Futur I and use the infinitive with zu in common cases.",
        theory_it: &[
            "Il Futur I si forma con werden + infinito in fondo: Ich werde morgen lernen.",
            "Coniugazione: ich werde, du wirst, er/sie/es wird, wir werden, ihr werdet, sie/Sie werden.",
            "Molti verbi reggono zu + infinito (versuchen, vergessen, vorhaben, hoffen). Con prefissi separabili: anzurufen.",
            "I verbi modali e lassen usano l'infinito senza zu: Ich muss lernen.",
        ],
        theory_en: &[
            "Futur I is formed with werden + infinitive at the end: Ich werde morgen lernen.",
            "Conjugation: ich werde, du wirst, er/sie/es wird, wir werden, ihr werdet, sie/Sie werden.",
            "Many verbs take zu + infinitive (versuchen, vergessen, vorhaben, hoffen). With separable prefixes: anzurufen.",
            "Modal verbs and lassen take the infinitive without zu: Ich muss lernen.",
        ],
        examples: &[
            ("Ich werde morgen nach Bozen fahren.", "Domani andrÃ² a Bolzano."),
            ("Ich versuche, pÃ¼nktlich zu kommen.", "Cerco di arrivare puntuale."),
            ("Ich habe vor, Deutsch zu lernen.", "Ho intenzione di imparare il tedesco."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ morgen nach Bozen fahren. (werden)", prompt_en: "Complete: Ich ___ morgen nach Bozen fahren. (werden)", options: &["werde", "wirst", "wird"], correct_index: 0, answer_text: "", explanation_it: "ich + werden = werde.", explanation_en: "ich + werden = werde." },
            GrammarUnitExercise { prompt_it: "Completa: Er ___ die PrÃ¼fung bestehen. (werden)", prompt_en: "Complete: Er ___ die PrÃ¼fung bestehen. (werden)", options: &["werde", "wird", "werden"], correct_index: 1, answer_text: "", explanation_it: "er + werden = wird.", explanation_en: "er + werden = wird." },
            GrammarUnitExercise { prompt_it: "Scegli: Ich habe vergessen, die Rechnung ___ bezahlen.", prompt_en: "Choose: Ich habe vergessen, die Rechnung ___ bezahlen.", options: &["zu", "um", "ohne"], correct_index: 0, answer_text: "", explanation_it: "vergessen regge l'infinito con zu.", explanation_en: "vergessen takes the infinitive with zu." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: Wir ___ im Sommer reisen. (werden)", prompt_en: "Type the form: Wir ___ im Sommer reisen. (werden)", options: &[], correct_index: 0, answer_text: "werden", explanation_it: "wir + werden = werden.", explanation_en: "wir + werden = werden." },
        ],
    }
}


fn unit_b1_doppelkonnektoren() -> GrammarUnit {
    GrammarUnit {
        id: "b1-doppelkonnektoren",
        level: "B1",
        title_it: "I connettori doppi",
        title_en: "Double connectors",
        subtitle_it: "entwederâ€¦oder, wederâ€¦noch, jeâ€¦desto",
        subtitle_en: "entwederâ€¦oder, wederâ€¦noch, jeâ€¦desto",
        objective_it: "Usare connettori doppi per collegare alternative, negazioni e confronti.",
        objective_en: "Use double connectors to link alternatives, negations, and comparisons.",
        theory_it: &[
            "entweder ... oder = o ... o; weder ... noch = nÃ© ... nÃ©; sowohl ... als auch = sia ... sia.",
            "nicht nur ... sondern auch = non solo ... ma anche; zwar ... aber = certo ... ma.",
            "je ... desto/umso = quanto piÃ¹ ... tanto piÃ¹. Nella parte con je il verbo va in fondo; con desto il verbo resta al secondo posto.",
        ],
        theory_en: &[
            "entweder ... oder = either ... or; weder ... noch = neither ... nor; sowohl ... als auch = both ... and.",
            "nicht nur ... sondern auch = not only ... but also; zwar ... aber = admittedly ... but.",
            "je ... desto/umso = the more ... the more. In the je-clause the verb goes to the end; in the desto-clause the verb stays in second position.",
        ],
        examples: &[
            ("Entweder komme ich heute oder am Freitag.", "O vengo oggi o venerdÃ¬."),
            ("Ich habe weder Zeit noch Geld.", "Non ho nÃ© tempo nÃ© soldi."),
            ("Je mehr ich lerne, desto besser verstehe ich.", "PiÃ¹ imparo, meglio capisco."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich habe ___ Zeit ___ Geld.", prompt_en: "Complete: Ich habe ___ Zeit ___ Geld.", options: &["weder ... noch", "sowohl ... als auch", "entweder ... oder"], correct_index: 0, answer_text: "", explanation_it: "Due negazioni: weder ... noch.", explanation_en: "Two negations: weder ... noch." },
            GrammarUnitExercise { prompt_it: "Completa: Sie spricht ___ Deutsch ___ Englisch.", prompt_en: "Complete: Sie spricht ___ Deutsch ___ Englisch.", options: &["weder ... noch", "sowohl ... als auch", "entweder ... oder"], correct_index: 1, answer_text: "", explanation_it: "Entrambi presenti: sowohl ... als auch.", explanation_en: "Both present: sowohl ... als auch." },
            GrammarUnitExercise { prompt_it: "Scegli: ___ mehr ich lerne, desto besser verstehe ich.", prompt_en: "Choose: ___ mehr ich lerne, desto besser verstehe ich.", options: &["Je", "Wenn", "Weil"], correct_index: 0, answer_text: "", explanation_it: "Correlazione comparativa: je ... desto.", explanation_en: "Comparative correlation: je ... desto." },
            GrammarUnitExercise { prompt_it: "Scrivi il connettore mancante: Er ist ___ jung, aber sehr erfahren.", prompt_en: "Type the missing connector: Er ist ___ jung, aber sehr erfahren.", options: &[], correct_index: 0, answer_text: "zwar", explanation_it: "Contrasto con concessione: zwar ... aber.", explanation_en: "Contrast with concession: zwar ... aber." },
        ],
    }
}

fn unit_b1_partizipien() -> GrammarUnit {
    GrammarUnit {
        id: "b1-partizipien",
        level: "B1",
        title_it: "Participi e frasi ridotte",
        title_en: "Participles and reduced clauses",
        subtitle_it: "lachend, gelesen, geÃ¶ffnet",
        subtitle_en: "lachend, gelesen, geÃ¶ffnet",
        objective_it: "Riconoscere e usare il Partizip I e il Partizip II come aggettivi.",
        objective_en: "Recognize and use Partizip I and Partizip II as adjectives.",
        theory_it: &[
            "Partizip I: infinito + d (lachen -> lachend), azione contemporanea/attiva: der lachende Mann.",
            "Partizip II: ge-...-t/en (lesen -> gelesen), spesso risultato/passivo: das gelesene Buch.",
            "Come aggettivi i participi prendono le normali desinenze: der lachende Mann, die geÃ¶ffnete TÃ¼r.",
        ],
        theory_en: &[
            "Partizip I: infinitive + d (lachen -> lachend), simultaneous/active action: der lachende Mann.",
            "Partizip II: ge-...-t/en (lesen -> gelesen), often result/passive: das gelesene Buch.",
            "As adjectives, participles take normal adjective endings: der lachende Mann, die geÃ¶ffnete TÃ¼r.",
        ],
        examples: &[
            ("Der lachende Mann ist mein Lehrer.", "L'uomo che ride Ã¨ il mio insegnante."),
            ("Das gelesene Buch liegt auf dem Tisch.", "Il libro letto Ã¨ sul tavolo."),
            ("Die geÃ¶ffnete TÃ¼r ist kaputt.", "La porta aperta Ã¨ rotta."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Partizip I di lachen:", prompt_en: "Partizip I of lachen:", options: &["lachend", "gelacht", "lachende"], correct_index: 0, answer_text: "", explanation_it: "Partizip I: infinito + d -> lachend.", explanation_en: "Partizip I: infinitive + d -> lachend." },
            GrammarUnitExercise { prompt_it: "Partizip II di lesen:", prompt_en: "Partizip II of lesen:", options: &["lesend", "gelesen", "gelesene"], correct_index: 1, answer_text: "", explanation_it: "lesen Ã¨ forte: gelesen.", explanation_en: "lesen is strong: gelesen." },
            GrammarUnitExercise { prompt_it: "Completa (aggettivo, maschile): Der ___ Mann ist mein Lehrer. (lachen)", prompt_en: "Complete (adjective, masculine): Der ___ Mann ist mein Lehrer. (lachen)", options: &["lachend", "lachende", "gelachte"], correct_index: 1, answer_text: "", explanation_it: "Der + maschile: lachend + e -> lachende.", explanation_en: "Der + masculine: lachend + e -> lachende." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: Das ___ Buch liegt auf dem Tisch. (lesen, Partizip II, neutro)", prompt_en: "Type the form: Das ___ Buch liegt auf dem Tisch. (lesen, Partizip II, neuter)", options: &[], correct_index: 0, answer_text: "gelesene", explanation_it: "Das + neutro: gelesen + e -> gelesene.", explanation_en: "Das + neuter: gelesen + e -> gelesene." },
        ],
    }
}


fn unit_a1_preposizioni_luogo() -> GrammarUnit {
    GrammarUnit {
        id: "a1-preposizioni-luogo",
        level: "A1",
        title_it: "Preposizioni di luogo",
        title_en: "Prepositions of place",
        subtitle_it: "Wo? e Wohin? tra dativo e accusativo",
        subtitle_en: "Wo? and Wohin? with dative and accusative",
        objective_it: "Scegliere la preposizione e il caso giusto per stato in luogo e moto a luogo.",
        objective_en: "Choose the right preposition and case for location and direction.",
        theory_it: &[
            "Con Wo? (dove?, stato in luogo) si usa spesso il dativo: Ich bin in der KÃ¼che.",
            "Con Wohin? (dove vai?, moto a luogo) si usa spesso l'accusativo: Ich gehe in die Schule.",
            "Preposizioni miste: in, an, auf, unter, Ã¼ber, vor, hinter, neben, zwischen. Contrazioni comuni: im, am, ins, ans, zum, zur.",
            "Con cittÃ /paesi spesso nach (nach Berlin), con persone/istituzioni spesso zu (zum Arzt, zur Schule).",
        ],
        theory_en: &[
            "With Wo? (where?, location) dative is often used: Ich bin in der KÃ¼che.",
            "With Wohin? (where to?, direction) accusative is often used: Ich gehe in die Schule.",
            "Two-way prepositions: in, an, auf, unter, Ã¼ber, vor, hinter, neben, zwischen. Common contractions: im, am, ins, ans, zum, zur.",
            "With cities/countries often nach (nach Berlin), with people/institutions often zu (zum Arzt, zur Schule).",
        ],
        examples: &[
            ("Ich bin in der Schule.", "Sono a scuola."),
            ("Ich gehe in die Schule.", "Vado a scuola."),
            ("Das Buch liegt auf dem Tisch.", "Il libro Ã¨ sul tavolo."),
            ("Wir fahren nach Berlin.", "Andiamo a Berlino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (Wohin?): Ich gehe ___ Schule.", prompt_en: "Complete (Wohin?): Ich gehe ___ Schule.", options: &["in die", "in der", "im"], correct_index: 0, answer_text: "", explanation_it: "Moto a luogo: accusativo -> in die Schule.", explanation_en: "Direction: accusative -> in die Schule." },
            GrammarUnitExercise { prompt_it: "Completa (Wo?): Ich bin ___ Schule.", prompt_en: "Complete (Wo?): Ich bin ___ Schule.", options: &["in die", "in der", "ins"], correct_index: 1, answer_text: "", explanation_it: "Stato in luogo: dativo -> in der Schule.", explanation_en: "Location: dative -> in der Schule." },
            GrammarUnitExercise { prompt_it: "Scegli: Das Buch liegt ___ Tisch. (Wo?)", prompt_en: "Choose: Das Buch liegt ___ Tisch. (Wo?)", options: &["auf den", "auf dem", "auf das"], correct_index: 1, answer_text: "", explanation_it: "Posizione: auf + dativo maschile -> auf dem Tisch.", explanation_en: "Location: auf + dative masculine -> auf dem Tisch." },
            GrammarUnitExercise { prompt_it: "Scrivi la contrazione: Ich gehe ___ Kino. (in + das)", prompt_en: "Type the contraction: Ich gehe ___ Kino. (in + das)", options: &[], correct_index: 0, answer_text: "ins", explanation_it: "in + das = ins.", explanation_en: "in + das = ins." },
        ],
    }
}


fn unit_a1_preposizioni_tempo() -> GrammarUnit {
    GrammarUnit {
        id: "a1-preposizioni-tempo",
        level: "A1",
        title_it: "Preposizioni di tempo",
        title_en: "Prepositions of time",
        subtitle_it: "um, am, im, seit, vor, nach, in",
        subtitle_en: "um, am, im, seit, vor, nach, in",
        objective_it: "Collocare azioni nel tempo con le preposizioni piÃ¹ frequenti.",
        objective_en: "Place actions in time with the most common prepositions.",
        theory_it: &[
            "um + ora (um 8 Uhr), am + giorno/data (am Montag), im + mese/stagione (im Januar, im Sommer).",
            "in + periodo futuro (in einer Woche); seit + dativo = da/per un periodo che continua (seit einem Jahr).",
            "vor + dativo = fa (vor einer Woche); nach + dativo = dopo (nach dem Essen).",
        ],
        theory_en: &[
            "um + time (um 8 Uhr), am + day/date (am Montag), im + month/season (im Januar, im Sommer).",
            "in + future period (in einer Woche); seit + dative = since/for an ongoing period (seit einem Jahr).",
            "vor + dative = ago (vor einer Woche); nach + dative = after (nach dem Essen).",
        ],
        examples: &[
            ("Am Montag habe ich frei.", "LunedÃ¬ sono libero."),
            ("Der Kurs beginnt um 9 Uhr.", "Il corso inizia alle 9."),
            ("Ich wohne seit einem Jahr in Berlin.", "Vivo a Berlino da un anno."),
            ("Nach dem Essen gehen wir spazieren.", "Dopo mangiato andiamo a passeggiare."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: ___ Montag habe ich frei.", prompt_en: "Complete: ___ Montag habe ich frei.", options: &["Am", "Im", "Um"], correct_index: 0, answer_text: "", explanation_it: "I giorni vogliono am: am Montag.", explanation_en: "Days take am: am Montag." },
            GrammarUnitExercise { prompt_it: "Completa: Der Kurs beginnt ___ 9 Uhr.", prompt_en: "Complete: Der Kurs beginnt ___ 9 Uhr.", options: &["am", "im", "um"], correct_index: 2, answer_text: "", explanation_it: "L'ora vuole um: um 9 Uhr.", explanation_en: "Clock time takes um: um 9 Uhr." },
            GrammarUnitExercise { prompt_it: "Scegli: Ich wohne ___ einem Jahr in Berlin.", prompt_en: "Choose: Ich wohne ___ einem Jahr in Berlin.", options: &["seit", "vor", "nach"], correct_index: 0, answer_text: "", explanation_it: "Periodo che continua: seit + dativo.", explanation_en: "Ongoing period: seit + dative." },
            GrammarUnitExercise { prompt_it: "Scrivi la preposizione: ___ Sommer fahren wir ans Meer.", prompt_en: "Type the preposition: ___ Sommer fahren wir ans Meer.", options: &[], correct_index: 0, answer_text: "Im", explanation_it: "Le stagioni vogliono im: im Sommer.", explanation_en: "Seasons take im: im Sommer." },
        ],
    }
}


fn unit_a1_separabili() -> GrammarUnit {
    GrammarUnit {
        id: "a1-separabili",
        level: "A1",
        title_it: "I verbi separabili",
        title_en: "Separable verbs",
        subtitle_it: "Il prefisso che va in fondo alla frase",
        subtitle_en: "The prefix that goes to the end of the clause",
        objective_it: "Riconoscere e usare i verbi con prefisso separabile nelle frasi principali.",
        objective_en: "Recognize and use separable-prefix verbs in main clauses.",
        theory_it: &[
            "Prefissi separabili comuni: auf-, an-, aus-, ein-, mit-, zu-, ab-, vor-, zurÃ¼ck-, fern-.",
            "Nella frase principale il verbo coniugato sta in seconda posizione e il prefisso va alla fine: Ich stehe um 6 Uhr auf.",
            "All'infinito il prefisso resta unito (aufstehen). Nel participio passato ge entra tra prefisso e verbo: aufgestanden.",
            "Prefissi inseparabili (be-, ge-, er-, ver-, ent-, emp-, miss-) non si separano.",
        ],
        theory_en: &[
            "Common separable prefixes: auf-, an-, aus-, ein-, mit-, zu-, ab-, vor-, zurÃ¼ck-, fern-.",
            "In main clauses the conjugated verb is in second position and the prefix goes to the end: Ich stehe um 6 Uhr auf.",
            "In the infinitive the prefix stays attached (aufstehen). In the past participle ge comes between prefix and verb: aufgestanden.",
            "Inseparable prefixes (be-, ge-, er-, ver-, ent-, emp-, miss-) do not separate.",
        ],
        examples: &[
            ("Ich stehe um 6 Uhr auf.", "Mi alzo alle 6."),
            ("Der Kurs fÃ¤ngt um 10 Uhr an.", "Il corso inizia alle 10."),
            ("Wir kaufen im Supermarkt ein.", "Facciamo la spesa al supermercato."),
            ("Kommst du mit?", "Vieni con noi?"),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ um 7 Uhr ___. (aufstehen)", prompt_en: "Complete: Ich ___ um 7 Uhr ___. (aufstehen)", options: &["stehe ... auf", "auf ... stehe", "aufstehe ... -"], correct_index: 0, answer_text: "", explanation_it: "Verbo in seconda posizione, prefisso in fondo: stehe ... auf.", explanation_en: "Verb in second position, prefix at the end: stehe ... auf." },
            GrammarUnitExercise { prompt_it: "Completa: Wir ___ heute im Supermarkt ___. (einkaufen)", prompt_en: "Complete: Wir ___ heute im Supermarkt ___. (einkaufen)", options: &["kaufen ... ein", "ein ... kaufen", "einkaufen ... -"], correct_index: 0, answer_text: "", explanation_it: "einkaufen: kaufen ... ein.", explanation_en: "einkaufen: kaufen ... ein." },
            GrammarUnitExercise { prompt_it: "Scegli: ___ du ___? (mitkommen)", prompt_en: "Choose: ___ du ___? (mitkommen)", options: &["Kommst ... mit", "Mit ... kommst", "Kommt ... mit"], correct_index: 0, answer_text: "", explanation_it: "Domanda: Kommst du mit?", explanation_en: "Question: Kommst du mit?" },
            GrammarUnitExercise { prompt_it: "Scrivi il prefisso: Er sieht abends ___. (fernsehen)", prompt_en: "Type the prefix: Er sieht abends ___. (fernsehen)", options: &[], correct_index: 0, answer_text: "fern", explanation_it: "fernsehen: sieht ... fern.", explanation_en: "fernsehen: sieht ... fern." },
        ],
    }
}


fn unit_a0_plurale() -> GrammarUnit {
    GrammarUnit {
        id: "a0-plurale",
        level: "A0",
        title_it: "Il plurale dei sostantivi",
        title_en: "Plural of nouns",
        subtitle_it: "Die Kinder, die Autos, die Frauen",
        subtitle_en: "Die Kinder, die Autos, die Frauen",
        objective_it: "Formare i plurali piÃ¹ frequenti e usare l'articolo die.",
        objective_en: "Form common plurals and use the article die.",
        theory_it: &[
            "Al plurale l'articolo Ã¨ sempre die, ma il sostantivo cambia forma in modi diversi.",
            "Desinenze comuni: -e (Tisch -> Tische), -en (Frau -> Frauen), -er (Kind -> Kinder), -s (Auto -> Autos), nessuna (Lehrer -> Lehrer).",
            "Alcuni plurali aggiungono l'umlaut: Stadt -> StÃ¤dte, Mann -> MÃ¤nner, Buch -> BÃ¼cher.",
        ],
        theory_en: &[
            "In the plural the article is always die, but the noun changes in different ways.",
            "Common endings: -e (Tisch -> Tische), -en (Frau -> Frauen), -er (Kind -> Kinder), -s (Auto -> Autos), none (Lehrer -> Lehrer).",
            "Some plurals add an umlaut: Stadt -> StÃ¤dte, Mann -> MÃ¤nner, Buch -> BÃ¼cher.",
        ],
        examples: &[
            ("Die Kinder spielen im Park.", "I bambini giocano nel parco."),
            ("Die BÃ¼cher sind interessant.", "I libri sono interessanti."),
            ("Die Autos sind neu.", "Le auto sono nuove."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Plurale di Kind:", prompt_en: "Plural of Kind:", options: &["die Kind", "die Kinder", "die Kindes"], correct_index: 1, answer_text: "", explanation_it: "Kind -> Kinder.", explanation_en: "Kind -> Kinder." },
            GrammarUnitExercise { prompt_it: "Plurale di Auto:", prompt_en: "Plural of Auto:", options: &["die Auten", "die Autos", "die Autoe"], correct_index: 1, answer_text: "", explanation_it: "Prestito con -s: die Autos.", explanation_en: "Loanword with -s: die Autos." },
            GrammarUnitExercise { prompt_it: "Plurale di Frau:", prompt_en: "Plural of Frau:", options: &["die Fraus", "die Frauen", "die FrÃ¤ue"], correct_index: 1, answer_text: "", explanation_it: "Frau -> Frauen.", explanation_en: "Frau -> Frauen." },
            GrammarUnitExercise { prompt_it: "Scrivi il plurale: das Buch -> die ___", prompt_en: "Type the plural: das Buch -> die ___", options: &[], correct_index: 0, answer_text: "BÃ¼cher", explanation_it: "Buch -> BÃ¼cher (umlaut + -er).", explanation_en: "Buch -> BÃ¼cher (umlaut + -er)." },
        ],
    }
}


fn unit_a0_alfabeto() -> GrammarUnit {
    GrammarUnit {
        id: "a0-alfabeto",
        level: "A0",
        title_it: "Alfabeto, suoni e pronuncia",
        title_en: "Alphabet, sounds and pronunciation",
        subtitle_it: "Leggere e scrivere i suoni tedeschi di base",
        subtitle_en: "Read and write basic German sounds",
        objective_it: "Riconoscere i suoni tedeschi piÃ¹ frequenti e leggere le prime parole.",
        objective_en: "Recognize common German sounds and read basic words.",
        theory_it: &[
            "L'alfabeto tedesco ha 26 lettere piÃ¹ Ã¤, Ã¶, Ã¼ e ÃŸ (vale come ss).",
            "Combinazioni chiave: ei = [ai] (mein), ie = [i:] lungo (sie), sch = [Êƒ] (Schule), z = [ts] (Zeit).",
            "v spesso suona [f] (Vater), w suona [v] (Wasser); s iniziale davanti a vocale Ã¨ sonora [z] (Sonne).",
        ],
        theory_en: &[
            "The German alphabet has 26 letters plus Ã¤, Ã¶, Ã¼ and ÃŸ (equals ss).",
            "Key combinations: ei = [ai] (mein), ie = long [i:] (sie), sch = [Êƒ] (Schule), z = [ts] (Zeit).",
            "v often sounds [f] (Vater), w sounds [v] (Wasser); initial s before a vowel is voiced [z] (Sonne).",
        ],
        examples: &[
            ("Ich heiÃŸe Anna.", "Mi chiamo Anna."),
            ("Die Schule ist schÃ¶n.", "La scuola Ã¨ bella."),
            ("Das ist mein Vater.", "Questo Ã¨ mio padre."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Come si pronuncia \"ei\" in \"mein\"?", prompt_en: "How is \"ei\" pronounced in \"mein\"?", options: &["[ai]", "[i:]", "[oi]"], correct_index: 0, answer_text: "", explanation_it: "ei si pronuncia [ai].", explanation_en: "ei is pronounced [ai]." },
            GrammarUnitExercise { prompt_it: "Come si pronuncia \"ie\" in \"sie\"?", prompt_en: "How is \"ie\" pronounced in \"sie\"?", options: &["[ai]", "[i:]", "[e]"], correct_index: 1, answer_text: "", explanation_it: "ie indica una i lunga [i:].", explanation_en: "ie indicates a long [i:]." },
            GrammarUnitExercise { prompt_it: "Quale parola contiene il suono [Êƒ] (sch)?", prompt_en: "Which word contains the [Êƒ] (sch) sound?", options: &["Sonne", "Schule", "Zeit"], correct_index: 1, answer_text: "", explanation_it: "Schule inizia con sch = [Êƒ].", explanation_en: "Schule starts with sch = [Êƒ]." },
            GrammarUnitExercise { prompt_it: "Completa: Stra___e (strada)", prompt_en: "Complete: Stra___e (street)", options: &[], correct_index: 0, answer_text: "ÃŸ", explanation_it: "StraÃŸe si scrive con ÃŸ (vale ss).", explanation_en: "StraÃŸe is written with ÃŸ (equals ss)." },
        ],
    }
}


fn unit_a1_modali() -> GrammarUnit {
    GrammarUnit {
        id: "a1-modali",
        level: "A1",
        title_it: "I verbi modali",
        title_en: "Modal verbs",
        subtitle_it: "kÃ¶nnen, mÃ¼ssen, dÃ¼rfen, wollen, sollen, mÃ¶gen, mÃ¶chten",
        subtitle_en: "kÃ¶nnen, mÃ¼ssen, dÃ¼rfen, wollen, sollen, mÃ¶gen, mÃ¶chten",
        objective_it: "Esprimere possibilitÃ , dovere, permesso e volontÃ  con i verbi modali.",
        objective_en: "Express possibility, necessity, permission, and desire with modal verbs.",
        theory_it: &[
            "Modali principali: kÃ¶nnen (potere), mÃ¼ssen (dovere), dÃ¼rfen (permesso), wollen (volere), sollen (consiglio/dovere), mÃ¶gen (piacere), mÃ¶chten (vorrei).",
            "Ich/er/sie/es non prendono -t: ich kann, er muss. Il modale Ã¨ in seconda posizione e l'infinito va alla fine: Ich kann heute nicht kommen.",
            "Con verbi riflessivi il pronome sta tra modale e infinito: Ich muss mich ausruhen. Negazione: nicht prima dell'infinito, kein davanti al sostantivo.",
        ],
        theory_en: &[
            "Main modals: kÃ¶nnen (can), mÃ¼ssen (must), dÃ¼rfen (may), wollen (want), sollen (should), mÃ¶gen (like), mÃ¶chten (would like).",
            "Ich/er/sie/es take no -t: ich kann, er muss. The modal is in second position and the infinitive goes to the end: Ich kann heute nicht kommen.",
            "With reflexive verbs the pronoun comes between modal and infinitive: Ich muss mich ausruhen. Negation: nicht before the infinitive, kein before a noun.",
        ],
        examples: &[
            ("Ich kann gut schwimmen.", "So nuotare bene."),
            ("Du musst heute arbeiten.", "Oggi devi lavorare."),
            ("Er darf nicht rauchen.", "Non puÃ² fumare."),
            ("Ich mÃ¶chte einen Kaffee.", "Vorrei un caffÃ¨."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ gut schwimmen. (kÃ¶nnen)", prompt_en: "Complete: Ich ___ gut schwimmen. (kÃ¶nnen)", options: &["kann", "kannst", "kÃ¶nnen"], correct_index: 0, answer_text: "", explanation_it: "Con ich si usa kann (senza -t).", explanation_en: "With ich use kann (no -t)." },
            GrammarUnitExercise { prompt_it: "Completa: Du ___ heute arbeiten. (mÃ¼ssen)", prompt_en: "Complete: Du ___ heute arbeiten. (mÃ¼ssen)", options: &["muss", "musst", "mÃ¼ssen"], correct_index: 1, answer_text: "", explanation_it: "Con du si usa musst.", explanation_en: "With du use musst." },
            GrammarUnitExercise { prompt_it: "Metti l'infinito alla fine: Ich kann / Deutsch / sprechen.", prompt_en: "Put the infinitive at the end: Ich kann / Deutsch / sprechen.", options: &["Ich kann Deutsch sprechen.", "Ich kann sprechen Deutsch.", "Ich spreche kann Deutsch."], correct_index: 0, answer_text: "", explanation_it: "Il modale in seconda posizione, l'infinito alla fine.", explanation_en: "Modal in second position, infinitive at the end." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: Ich ___ einen Tee. (mÃ¶chten)", prompt_en: "Type the form: Ich ___ einen Tee. (mÃ¶chten)", options: &[], correct_index: 0, answer_text: "mÃ¶chte", explanation_it: "mÃ¶chten: ich mÃ¶chte.", explanation_en: "mÃ¶chten: ich mÃ¶chte." },
        ],
    }
}

#[derive(Debug, Clone, Default)]
pub struct GrammarUnitPracticeResult {
    pub correct: usize,
    pub total: usize,
    pub percent: i32,
    pub completed: bool,
}

#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum TipoPasso {
    Ripasso,
    Grammatica,
    Comprensione,
    Produzione,
    Vocabolario,
    TestLivello,
// SessioneGuidata,
}

#[derive(Debug, Clone)]
pub struct PassoPercorso {
    pub id: String,
    pub titolo: String,
    pub descrizione: String,
    pub tipo: TipoPasso,
    pub priorita: usize,
    pub stima_minuti: i32,
    pub azione: String,
    pub urgente: bool,
    pub completato: bool,
}

#[derive(Debug, Clone)]
pub struct PercorsoGiornaliero {
    pub data: String,
    pub passi: Vec<PassoPercorso>,
    pub messaggio_motivazionale: String,
    pub prossimo_obiettivo: String,
    pub minuti_totali: i32,
    pub minuti_completati: i32,
}

impl PercorsoGiornaliero {
    pub fn passi_totali(&self) -> usize { self.passi.len() }
    pub fn passi_completati(&self) -> usize { self.passi.iter().filter(|p| p.completato).count() }
    pub fn percorso_completato(&self) -> bool { !self.passi.is_empty() && self.passi.iter().all(|p| p.completato) }
    pub fn prossimo_passo(&self) -> Option<&PassoPercorso> { self.passi.iter().find(|p| !p.completato) }
}

impl Store {
    pub fn embedded_grammar_units(&self) -> Vec<GrammarUnit> {
        embedded_grammar_units()
    }

    pub fn prossima_unita(&self) -> Option<GrammarUnit> {
        let summaries = self.grammar_mastery_summaries();
        let mut by_id: HashMap<String, GrammarMasterySummary> = HashMap::new();
        for s in summaries { by_id.insert(s.id.clone(), s); }
        let now = Utc::now().timestamp_millis();
        let units = embedded_grammar_units();
        // prefer incomplete/due in order
        for u in &units {
            if let Some(s) = by_id.get(u.id) {
                if !s.completata || s.is_due(now) { return Some(u.clone()); }
            } else {
                return Some(u.clone());
            }
        }
        None
    }

    pub fn passi_completati_oggi(&self) -> std::collections::HashSet<String> {
        let oggi = Local::now().format("%Y-%m-%d").to_string();
        let Some(p) = self.active_profile() else { return std::collections::HashSet::new(); };
        let data = p.stato.extra.get("passiGiornoData").and_then(|v| v.as_str()).unwrap_or("");
        if data != oggi { return std::collections::HashSet::new(); }
        p.stato.extra.get("passiGiornoIds").and_then(|v| v.as_array()).map(|a| {
            a.iter().filter_map(|x| x.as_str().map(|s| s.to_string())).collect()
        }).unwrap_or_default()
    }

    pub fn completa_passo(&mut self, passo_id: &str) -> Result<(), String> {
        let oggi = Local::now().format("%Y-%m-%d").to_string();
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let existing_data = p.stato.extra.get("passiGiornoData").and_then(|v| v.as_str()).unwrap_or("").to_string();
        let mut ids: Vec<String> = if existing_data == oggi {
            p.stato.extra.get("passiGiornoIds").and_then(|v| v.as_array()).map(|a| a.iter().filter_map(|x| x.as_str().map(|s| s.to_string())).collect()).unwrap_or_default()
        } else { vec![] };
        if !ids.iter().any(|x| x == passo_id) { ids.push(passo_id.to_string()); }
        p.stato.extra.insert("passiGiornoData".into(), serde_json::Value::String(oggi));
        p.stato.extra.insert("passiGiornoIds".into(), serde_json::Value::Array(ids.into_iter().map(serde_json::Value::String).collect()));
        self.save()
    }

    pub fn piano_oggi(&self) -> PercorsoGiornaliero {
        let oggi = Local::now().format("%Y-%m-%d").to_string();
        let plan = self.study_plan_summary();
        let (_total, _reviewed, da_ripassare, streak) = self.stats();
        let prossima = self.prossima_unita();
        let completati = self.passi_completati_oggi();
        let obiettivo = plan.obiettivo_livello.clone();
        let mut passi: Vec<PassoPercorso> = Vec::new();

        if da_ripassare > 0 {
            let minuti = ((da_ripassare as f32 * 0.5).round() as i32).clamp(4, 8);
            let id = "ripasso-giornaliero".to_string();
            passi.push(PassoPercorso {
                titolo: "Ripasso flashcard".into(),
                descrizione: format!("{da_ripassare} parole in scadenza Â· ~{minuti} min"),
                tipo: TipoPasso::Ripasso,
                priorita: 0,
                stima_minuti: minuti,
                azione: "ripasso".into(),
                urgente: da_ripassare >= 10,
                completato: completati.contains(&id),
                id,
            });
        }
        if plan.grammar_due > 0 || prossima.is_some() {
            let id = "grammatica-giornaliera".to_string();
            let azione = if let Some(u) = &prossima { format!("unita_grammatica/{}", u.id) } else { "grammatica".into() };
            let urgente = plan.grammar_due >= 2;
            passi.push(PassoPercorso {
                titolo: "Pratica grammaticale".into(),
                descrizione: if let Some(u) = &prossima { format!("{} Â· ~8 min", u.title_it) } else { "Ripassa grammatica Â· ~8 min".into() },
                tipo: TipoPasso::Grammatica,
                priorita: 0,
                stima_minuti: 8,
                azione,
                urgente,
                completato: completati.contains(&id),
                id,
            });
        }
        if plan.new_words > 0 && da_ripassare == 0 {
            let n = plan.new_words.min(10);
            let minuti = ((n as f32 * 0.6).round() as i32).clamp(4, 8);
            let id = "vocabolario-giornaliero".to_string();
            passi.push(PassoPercorso {
                titolo: "Nuove parole".into(),
                descrizione: format!("{n} parole nuove Â· ~{minuti} min"),
                tipo: TipoPasso::Vocabolario,
                priorita: 0,
                stima_minuti: minuti,
                azione: "wortliste".into(),
                urgente: false,
                completato: completati.contains(&id),
                id,
            });
        }
        if passi.is_empty() {
            let id = "sessione-guidata".to_string();
            passi.push(PassoPercorso {
                titolo: "Sessione libera".into(),
                descrizione: "Pratica libera di 20 minuti".into(),
                tipo: TipoPasso::Vocabolario,
                priorita: 0,
                stima_minuti: 20,
                azione: "esercizi".into(),
                urgente: false,
                completato: completati.contains(&id),
                id,
            });
        }
        // limit budget similar spirit
        let mut limited: Vec<PassoPercorso> = Vec::new();
        let mut restante = 22i32;
        for mut p in passi.into_iter() {
            if limited.len() >= 3 || restante < 4 { break; }
            let m = p.stima_minuti.min(restante).max(4);
            p.stima_minuti = m;
            restante -= m;
            p.priorita = limited.len() + 1;
            limited.push(p);
        }
        let minuti_totali: i32 = limited.iter().map(|p| p.stima_minuti).sum();
        let minuti_completati: i32 = limited.iter().filter(|p| p.completato).map(|p| p.stima_minuti).sum();
        let any_urgente = limited.iter().any(|p| p.urgente && !p.completato);
        let messaggio_motivazionale = if any_urgente {
            "Oggi conviene concentrarsi sulle aree con il badge PrioritÃ .".into()
        } else if da_ripassare >= 20 {
            "Tante parole da ripassare. Inizia dal ripasso!".into()
        } else if da_ripassare >= 10 {
            "Buon ritmo! Mantieni la costanza.".into()
        } else if streak >= 7 {
            format!("Streak di {streak} giorni! Sei inarrestabile.")
        } else if limited.iter().all(|p| p.completato) && !limited.is_empty() {
            "Prosegui cosÃ¬, stai facendo ottimi progressi.".into()
        } else {
            "Buona sessione di studio!".into()
        };
        let prossimo_obiettivo = match obiettivo.as_str() {
            "A0" => "Raggiungere A1 con 50 parole".into(),
            "A1" => "Raggiungere A2 con 150 parole".into(),
            "A2" => "Raggiungere B1 con 900 parole".into(),
            "B1" => "Raggiungere B2 con 1800 parole".into(),
            _ => "Raggiungere C1 con 3500 parole".into(),
        };

        PercorsoGiornaliero { data: oggi, passi: limited, messaggio_motivazionale, prossimo_obiettivo, minuti_totali, minuti_completati }
    }

    pub fn record_grammar_unit_practice(&mut self, unit_id: &str, correct: usize, total: usize) -> Result<GrammarUnitPracticeResult, String> {
        if total == 0 { return Err("No exercises in unit practice".into()); }
        let now = chrono::Utc::now().timestamp_millis();
        let percent = ((correct as f64 / total as f64) * 100.0).round() as i32;
        let completed = percent >= 80;
        let p = self.active_profile_mut().ok_or_else(|| "No active profile".to_string())?;
        let entry = p.stato.padronanza_grammatica.entry(unit_id.to_string()).or_insert_with(|| serde_json::json!({}));
        let prev_attempts = entry.get("tentativi").and_then(|v| v.as_i64()).or_else(|| entry.get("totali").and_then(|v| v.as_i64())).unwrap_or(0);
        let prev_correct = entry.get("corrette").and_then(|v| v.as_i64()).unwrap_or(0);
        let prev_ripassi = entry.get("ripassi").and_then(|v| v.as_i64()).unwrap_or(0);
        let next_review = if completed {
            now + 3 * 24 * 60 * 60 * 1000
        } else {
            now + 12 * 60 * 60 * 1000
        };
        *entry = serde_json::json!({
            "unitaId": unit_id,
            "percentuale": percent,
            "tentativi": prev_attempts + 1,
            "corrette": prev_correct + correct as i64,
            "totali": prev_attempts + 1,
            "completata": completed || entry.get("completata").and_then(|v| v.as_bool()).unwrap_or(false),
            "ultimaVolta": now,
            "prossimaRipasso": next_review,
            "ripassi": prev_ripassi + 1,
        });
        self.save()?;
        Ok(GrammarUnitPracticeResult { correct, total, percent, completed })
    }

    // --- settings / profile helpers (mobile-equivalent best effort) ---
    pub fn rinomina_profilo(&mut self, nome: &str) -> Result<(), String> {
        let nome = nome.trim();
        if nome.is_empty() { return Err("Nome vuoto".into()); }
        let p = self.active_profile_mut().ok_or_else(|| "Nessun profilo attivo".to_string())?;
        p.config.nome_visualizzato = nome.to_string();
        self.save()
    }

    pub fn imposta_contesto(&mut self, contesto: &str) -> Result<(), String> {
        let p = self.active_profile_mut().ok_or_else(|| "Nessun profilo attivo".to_string())?;
        p.config.contesto_utente = contesto.trim().to_string();
        self.save()
    }

    pub fn imposta_livello_e_obiettivo(&mut self, iniziale: &str, obiettivo: &str) -> Result<(), String> {
        let p = self.active_profile_mut().ok_or_else(|| "Nessun profilo attivo".to_string())?;
        let ini = iniziale.trim().to_uppercase();
        let ob = obiettivo.trim().to_uppercase();
        if !ini.is_empty() { p.config.livello_iniziale = ini.clone(); }
        if !ob.is_empty() { p.stato.progresso.obiettivo_livello = ob; }
        // Keep current level consistent with starting level when user explicitly sets it (desktop parity intent),
        // while preserving real progression semantics as much as possible.
        if !ini.is_empty() { p.stato.progresso.livello_corrente = ini; }
        self.save()
    }

    pub fn imposta_feed_url(&mut self, url: &str) -> Result<(), String> {
        let p = self.active_profile_mut().ok_or_else(|| "Nessun profilo attivo".to_string())?;
        p.config.feed_url = url.trim().to_string();
        self.save()
    }

    pub fn salva_token_cloud(&mut self, token: &str) -> Result<(), String> {
        self.prefs.github_token = token.trim().to_string();
        self.save()
    }

    pub fn salva_repo_cloud(&mut self, repo: &str) -> Result<(), String> {
        self.prefs.github_repo = repo.trim().to_string();
        self.save()
    }

    pub fn svuota_profilo_attivo(&mut self) -> Result<(), String> {
        let p = self.active_profile_mut().ok_or_else(|| "Nessun profilo attivo".to_string())?;
        p.stato.parole.clear();
        p.stato.reviews.clear();
        p.stato.ripassi_log.clear();
        p.stato.padronanza_grammatica.clear();
        p.stato.streak_giorni = 0;
        p.stato.streak_record = 0;
        p.stato.ultimo_giorno_studio = None;
        p.stato.progresso.lezione_corrente = 0;
        p.stato.progresso.ultima_attivita = 0;
        p.stato.extra.remove("passiGiornoData");
        p.stato.extra.remove("passiGiornoIds");
        self.save()
    }

    pub fn importa_wortliste_seed(&mut self) -> Result<usize, String> {
        // Best-effort: try to import from a JSON bundle/word list if present near data dir or cwd.
        let candidates = [
            self.base_dir.join("wortliste.json"),
            self.base_dir.join("Wortliste.json"),
            std::env::current_dir().unwrap_or_else(|_| PathBuf::from(".")).join("wortliste.json"),
            std::env::current_dir().unwrap_or_else(|_| PathBuf::from(".")).join("assets").join("wortliste.json"),
        ];
        for path in candidates {
            if !path.exists() { continue; }
            let data = fs::read_to_string(&path).map_err(|e| e.to_string())?;
            let v: Value = serde_json::from_str(&data).map_err(|e| e.to_string())?;
            let arr = if let Some(a) = v.as_array() { a.clone() }
                else if let Some(a) = v.get("parole").and_then(|x| x.as_array()) { a.clone() }
                else if let Some(a) = v.get("words").and_then(|x| x.as_array()) { a.clone() }
                else { continue; };
            let mut added = 0usize;
            for item in arr {
                let german = item.get("german").or_else(|| item.get("tedesco")).and_then(|x| x.as_str()).unwrap_or("").trim();
                let italian = item.get("italian").or_else(|| item.get("italiano")).and_then(|x| x.as_str()).unwrap_or("").trim();
                if german.is_empty() || italian.is_empty() { continue; }
                let article = item.get("article").or_else(|| item.get("articolo")).and_then(|x| x.as_str()).map(|s| s.to_string());
                let level = item.get("level").or_else(|| item.get("livello")).and_then(|x| x.as_str()).unwrap_or("A1");
                let example = item.get("example").or_else(|| item.get("esempio")).and_then(|x| x.as_str()).unwrap_or("");
                // add_word skips duplicates and saves each time? it saves; to reduce churn we can inline logic, but easier to reuse
                let before = self.active_profile().map(|p| p.stato.parole.len()).unwrap_or(0);
                let _ = self.add_word(german, italian, article, level, example);
                let after = self.active_profile().map(|p| p.stato.parole.len()).unwrap_or(before);
                if after > before { added += 1; }
            }
            return Ok(added);
        }
        Err("Wortliste non trovata in questa build Rust (atteso wortliste.json in cartella dati o assets).".into())
    }

    pub fn esporta_dati_completi_a(&self, path: &PathBuf) -> Result<(), String> {
        // Full export including profiles + preferences (mobile-equivalent bundle), token left as-is in prefs file normally
        // but for "Esporta tutti i dati" we omit sensitive cloud token from exported copy.
        let mut prefs = self.prefs.clone();
        prefs.github_token.clear();
        prefs.ultimo_hash_cloud.clear();
        let bundle = serde_json::json!({
            "format": "openlingo-export-completo",
            "version": 1,
            "profili": self.repo,
            "preferenze": prefs,
        });
        write_json(path, &bundle)
    }

    pub fn nome_file_backup_cloud(&self) -> String {
        "tedesco-backup.json".to_string()
    }

    pub fn ultima_sincronizzazione_cloud(&self) -> i64 {
        self.prefs.ultima_sincronizzazione_cloud
    }

    pub fn ultimo_backup_cloud(&self) -> i64 {
        self.prefs.ultimo_backup
    }

    pub fn sincronizzazione_cloud_disponibile(&self) -> bool {
        !self.prefs.github_token.trim().is_empty()
    }
}

// â”€â”€ Gender Trainer (allenamento articoli der/die/das) â”€â”€

/** Una domanda del Gender Trainer: il sostantivo senza articolo e l'articolo corretto. */
#[derive(Debug, Clone)]
pub struct GenderDomanda {
    pub id: String,
    pub parola: String,
    pub articolo: String,
    pub traduzione: String,
    pub esempio: String,
    pub livello: String,
}

/** Genera domande del Gender Trainer dal pool di parole del profilo + lessico curato. */
pub fn gender_domande(
    parole_profilo: &[WordEntity],
    livello_selezionato: &str,
    limite: Option<usize>,
) -> Vec<GenderDomanda> {
    let max_ord = level_rank(livello_selezionato);
    let mut viste: HashSet<String> = HashSet::new();

    let da_profilo: Vec<GenderDomanda> = parole_profilo
        .iter()
        .filter_map(|w| {
            let art = w.normalized_article()?;
            if !matches!(art.as_str(), "der" | "die" | "das") {
                return None;
            }
            if level_rank(&w.level) > max_ord {
                return None;
            }
            let parola = w.german.trim().to_string();
            if parola.is_empty() {
                return None;
            }
            let chiave = format!("{}|{}", art, parola.to_lowercase());
            if !viste.insert(chiave) {
                return None;
            }
            Some(GenderDomanda {
                id: format!("p_{}", w.id),
                parola,
                articolo: art,
                traduzione: w.italian.trim().to_string(),
                esempio: w.example.trim().to_string(),
                livello: w.level.clone(),
            })
        })
        .collect();

    let mut tutte = da_profilo;
    if let Some(lim) = limite {
        if lim > 0 {
            tutte.truncate(lim);
        }
    }
    tutte
}

/** 4 opzioni (3 distrattori + 1 corretta) per una domanda del Gender Trainer. */
pub fn gender_opzioni(domanda: &GenderDomanda) -> Vec<String> {
    let corretto = domanda.articolo.clone();
    let pool = ["der", "die", "das"];
    let distrattori: Vec<&str> = pool
        .iter()
        .filter(|&&a| a != corretto.as_str())
        .copied()
        .collect();
    // Prendi i primi 3 distrattori, poi mescola corretto + distrattori.
    let mut opzioni: Vec<String> = distrattori[..3].iter().map(|s| s.to_string()).collect();
    opzioni.push(corretto);
    // Deterministic shuffle (seed from domanda id) per non mescolare a ogni render.
    let seed = domanda
        .id
        .bytes()
        .fold(0u64, |acc, b| acc.wrapping_mul(31).wrapping_add(b as u64));
    for i in (1..opzioni.len()).rev() {
        let j = (seed as usize) % (i + 1);
        opzioni.swap(i, j);
    }
    opzioni
}


// ── Speech engines (Whistle STT + Phonon TTS, on-device via pip) ──

/// Motore vocale locale gestibile dall'app (installazione automatica in un venv dedicato).
#[derive(Debug, Clone, Copy, PartialEq, Eq)]
pub enum SpeechEngineKind {
    Whistle, // speech-to-text (cactus-compute)
    Phonon,  // text-to-speech (ptts / Gradium)
}

impl SpeechEngineKind {
    pub fn id(self) -> &'static str {
        match self {
            SpeechEngineKind::Whistle => "whistle",
            SpeechEngineKind::Phonon => "phonon",
        }
    }
    /// Pacchetto pip da installare.
    pub fn package(self) -> &'static str {
        match self {
            SpeechEngineKind::Whistle => "cactus-compute",
            SpeechEngineKind::Phonon => "ptts",
        }
    }
    /// Modulo Python da verificare dopo l'installazione.
    pub fn module(self) -> &'static str {
        match self {
            SpeechEngineKind::Whistle => "cactus",
            SpeechEngineKind::Phonon => "ptts",
        }
    }
    pub fn label(self) -> &'static str {
        match self {
            SpeechEngineKind::Whistle => "Whistle (Speech to Text)",
            SpeechEngineKind::Phonon => "Phonon (Text to Speech)",
        }
    }
    pub fn description(self) -> &'static str {
        match self {
            SpeechEngineKind::Whistle => "16.9 MB on-device STT - 7 languages incl. German - Apache 2.0",
            SpeechEngineKind::Phonon => "On-device TTS - multilingual incl. German - Apache 2.0 (31m)",
        }
    }
}

/// Cartella dedicata ai motori vocali: `~/.tedesco-desktop/speech/venv`.
pub fn speech_venv_dir(store: &Store) -> PathBuf {
    store.base_dir.join("speech").join("venv")
}

/// Percorso di pip nel venv (Windows vs Unix).
fn venv_pip(venv: &PathBuf) -> PathBuf {
    if cfg!(windows) {
        venv.join("Scripts").join("pip.exe")
    } else {
        venv.join("bin").join("pip")
    }
}

/// Percorso di python nel venv.
fn venv_python(venv: &PathBuf) -> PathBuf {
    if cfg!(windows) {
        venv.join("Scripts").join("python.exe")
    } else {
        venv.join("bin").join("python")
    }
}

/// Comando python di sistema (per creare il venv).
fn system_python() -> &'static str {
    if cfg!(windows) { "python" } else { "python3" }
}

/// Crea il venv se non esiste. Ritorna Ok(()) se pronto.
pub fn speech_ensure_venv(store: &Store) -> Result<(), String> {
    let venv = speech_venv_dir(store);
    if venv_python(&venv).exists() {
        return Ok(());
    }
    fs::create_dir_all(venv.parent().unwrap_or(&venv)).map_err(|e| e.to_string())?;
    let out = std::process::Command::new(system_python())
        .args(["-m", "venv"])
        .arg(&venv)
        .output()
        .map_err(|e| format!("python non trovato: {e}"))?;
    if !out.status.success() {
        return Err(format!(
            "creazione venv fallita: {}",
            String::from_utf8_lossy(&out.stderr).trim()
        ));
    }
    Ok(())
}

/// Installa (o aggiorna) un motore vocale nel venv dedicato.
pub fn speech_install(store: &Store, engine: SpeechEngineKind) -> Result<String, String> {
    speech_ensure_venv(store)?;
    let venv = speech_venv_dir(store);
    let pip = venv_pip(&venv);
    let out = std::process::Command::new(&pip)
        .args(["install", "--upgrade", engine.package()])
        .output()
        .map_err(|e| format!("pip non disponibile: {e}"))?;
    if !out.status.success() {
        return Err(format!(
            "installazione {} fallita: {}",
            engine.label(),
            String::from_utf8_lossy(&out.stderr).trim()
        ));
    }
    Ok(format!("{} installato.", engine.label()))
}

/// Disinstalla un motore vocale dal venv dedicato (non rimuove il venv).
pub fn speech_uninstall(store: &Store, engine: SpeechEngineKind) -> Result<String, String> {
    let venv = speech_venv_dir(store);
    let pip = venv_pip(&venv);
    if !pip.exists() {
        return Ok(format!("{} non era installato.", engine.label()));
    }
    let out = std::process::Command::new(&pip)
        .args(["uninstall", "-y", engine.package()])
        .output()
        .map_err(|e| format!("pip non disponibile: {e}"))?;
    if !out.status.success() {
        return Err(format!(
            "disinstallazione {} fallita: {}",
            engine.label(),
            String::from_utf8_lossy(&out.stderr).trim()
        ));
    }
    Ok(format!("{} disinstallato.", engine.label()))
}

/// Rimuove l'intero venv (tutti i motori + dipendenze).
pub fn speech_remove_all(store: &Store) -> Result<String, String> {
    let venv = speech_venv_dir(store);
    let dir = venv.parent().unwrap_or(&venv).to_path_buf();
    if !dir.exists() {
        return Ok("Nessuna installazione vocale presente.".to_string());
    }
    fs::remove_dir_all(&dir).map_err(|e| format!("rimozione cartella speech fallita: {e}"))?;
    Ok("Tutti i motori vocali rimossi.".to_string())
}

/// Verifica se un motore è importabile nel venv.
pub fn speech_is_installed(store: &Store, engine: SpeechEngineKind) -> bool {
    let venv = speech_venv_dir(store);
    let py = venv_python(&venv);
    if !py.exists() {
        return false;
    }
    let code = format!("import {}", engine.module());
    std::process::Command::new(&py)
        .args(["-c", &code])
        .output()
        .map(|o| o.status.success())
        .unwrap_or(false)
}

/// Ritorna la versione installata del pacchetto, o stringa vuota.
pub fn speech_version(store: &Store, engine: SpeechEngineKind) -> String {
    let venv = speech_venv_dir(store);
    let py = venv_python(&venv);
    if !py.exists() {
        return String::new();
    }
    let code = format!(
        "import importlib.metadata as m; print(m.version('{}'))",
        engine.package()
    );
    std::process::Command::new(&py)
        .args(["-c", &code])
        .output()
        .ok()
        .filter(|o| o.status.success())
        .map(|o| String::from_utf8_lossy(&o.stdout).trim().to_string())
        .unwrap_or_default()
}

/// Percorso del venv come stringa (per la UI).
pub fn speech_venv_path(store: &Store) -> String {
    speech_venv_dir(store).to_string_lossy().to_string()
}

// ── Pronuncia (coppie minime, consigli, scioglilingua, frasi cliniche) ──

/// Coppia minima di tedesco (due parole che differiscono per un solo suono).
#[derive(Debug, Clone)]
pub struct CoppiaMinima {
    pub a: String,
    pub b: String,
    pub nota: String,
    pub italiano_a: String,
    pub italiano_b: String,
}

/// Consiglio di pronuncia.
#[derive(Debug, Clone)]
pub struct ConsiglioPronuncia {
    pub titolo: String,
    pub testo: String,
    pub esempio: String,
}

/// Coppie minime (portate dall'app mobile).
pub fn pronuncia_coppie() -> Vec<CoppiaMinima> {
    let raw = [
        ("schon", "schön", "u corta vs ü", "già", "bello"),
        ("Mutter", "Mütter", "u vs ü", "madre", "madri"),
        ("Kuchen", "Küchen", "u vs ü", "torta", "cucine"),
        ("Höhle", "Hölle", "ö lunga vs ö corta", "caverna", "inferno"),
        ("Ofen", "offen", "o lunga vs o corta + ff", "forno", "aperto"),
        ("bitten", "bieten", "i corta vs ie lunga", "chiedere", "offrire"),
        ("fühlen", "füllen", "ü lunga vs ü corta", "sentire", "riempire"),
        ("Beet", "Bett", "e lunga vs e corta", "aiuola", "letto"),
        ("Stadt", "Staat", "a corta vs a lunga", "città", "stato"),
        ("Kirche", "Kirsche", "ch dolce vs sch", "chiesa", "ciliegia"),
        ("wissen", "weißen", "ss vs ß + ei", "sapere", "bianco (verbo)"),
        ("reisen", "reißen", "s vs ß", "viaggiare", "strappare"),
        ("Kahn", "Kann", "a lunga vs a corta", "barca", "posso"),
        ("Bruder", "Brüder", "u vs ü", "fratello", "fratelli"),
        ("Vetter", "Wetter", "v si legge 'f' vs w", "cugino", "tempo"),
        ("weisen", "wissen", "ei vs i", "indicare/biancare", "sapere"),
        ("Hund", "Hand", "au vs a", "cane", "mano"),
        ("Beeren", "Bären", "e lunga vs ä lunga", "bacche", "orsi"),
    ];
    raw.iter().map(|(a, b, n, ia, ib)| CoppiaMinima {
        a: a.to_string(), b: b.to_string(), nota: n.to_string(),
        italiano_a: ia.to_string(), italiano_b: ib.to_string(),
    }).collect()
}

/// Consigli di pronuncia.
pub fn pronuncia_consigli() -> Vec<ConsiglioPronuncia> {
    let raw = [
        ("ü", "Dì 'i' con le labbra arrotondate come per 'u'. Non è 'u'.", "Mütter, Küche, Übung"),
        ("ö", "Dì 'e' con le labbra arrotondate come per 'o'.", "schön, Höhle, können"),
        ("ch", "Dopo a/o/u è un suono gutturale (come la 'c' toscana di 'casa'); dopo i/e è dolce.", "Bach, Buch, ich, Kirche"),
        ("r", "In finale è quasi muto, vocalizzato: 'der' suona 'dea'. Non arrotare la r italiana.", "der, Mutter, Arzt"),
        ("z", "È 'ts', non 'z' italiana: 'zehn' = 'tsehn'.", "Zeit, zehn, Arzt"),
        ("s iniziale", "Davanti a vocale è sonora ('s' di 'rosa'): 'sie' = 'zii'.", "sie, sagen, Sonne"),
        ("ei / ie", "'ei' = 'ai'; 'ie' = 'ii' lunga. Sono opposti: non confonderli.", "mein vs Wien, Zeit vs sie"),
        ("Auslautverhärtung", "A fine sillaba b/d/g diventano p/t/k.", "Kind = 'kint', gab = 'gap', Tag = 'tak'"),
        ("accento", "Nelle parole tedesche l'accento è spesso sulla prima sillaba.", "Árbeit, Múttern, Phýsiotherapie (eccezione)"),
    ];
    raw.iter().map(|(t, x, e)| ConsiglioPronuncia {
        titolo: t.to_string(), testo: x.to_string(), esempio: e.to_string(),
    }).collect()
}

/// Scioglilingua.
pub fn pronuncia_scioglilingua() -> Vec<&'static str> {
    vec![
        "Fischers Fritz fischt frische Fische.",
        "Brautkleid bleibt Brautkleid und Blaukraut bleibt Blaukraut.",
        "Zehn zahme Ziegen zogen zehn Zentner Zucker zum Zoo.",
        "Schnecken erschrecken, wenn Schnecken an Schnecken schlecken.",
        "Der Whiskymixer mixt Whisky, wenn der Whiskymixer Whisky mixt.",
        "In Ulm, um Ulm und um Ulm herum.",
        "Zwischen zwei Zwetschgenzweigen sitzen zwei zwitschernde Schwalben.",
        "Esel essen Nesseln nicht, Nesseln essen Esel nicht.",
        "Der Cottbuser Postkutscher putzt den Cottbuser Postkutscherkasten.",
    ]
}

/// Frasi cliniche (contesto sanitario).
pub fn pronuncia_frasi_cliniche() -> Vec<&'static str> {
    vec![
        "Bitte beugen Sie das Knie langsam.",
        "Bitte strecken Sie das Bein.",
        "Atmen Sie tief ein und langsam aus.",
        "Wie stark sind die Schmerzen auf einer Skala von 1 bis 10?",
        "Bitte entspannen Sie die Schulter.",
        "Ich zeige Ihnen die Übung, dann machen Sie sie nach.",
        "Bitte bringen Sie den Befund mit.",
        "Haben Sie noch Fragen?",
        "Bitte drehen Sie sich auf den Bauch.",
        "Bitte heben Sie die Schulter an und lassen Sie sie langsam sinken.",
        "Wie fühlen Sie sich heute?",
        "Bitte stehen Sie langsam auf: erst seitwärts, dann aufrecht.",
        "Machen Sie dreimal zehn Wiederholungen mit einer Pause dazwischen.",
        "Strecken und beugen Sie das Knie, ohne es ganz durchzudrücken.",
        "Der Schmerz ist nachmittags schlimmer oder morgens?",
        "Heute war die Sitzung gut: Sie haben viel geschafft.",
    ]
}

// ── Südtirol / Austria module (variants, lexicon, phrases, dialogues) ──

#[derive(Debug, Clone)]
pub struct VarianteAustriaca {
    pub austriaco: String,
    pub standard: String,
    pub italiano: String,
}

#[derive(Debug, Clone)]
pub struct VoceLessicoSud {
    pub tedesco: String,
    pub italiano: String,
    pub categoria: String,
}

#[derive(Debug, Clone)]
pub struct FraseSud {
    pub tedesco: String,
    pub italiano: String,
    pub nota: String,
}

#[derive(Debug, Clone)]
pub struct Battuta {
    pub parlante: String,
    pub testo: String,
}

#[derive(Debug, Clone)]
pub struct DomandaDialogo {
    pub domanda: String,
    pub opzioni: Vec<String>,
    pub corretta: usize,
}

#[derive(Debug, Clone)]
pub struct DialogoSud {
    pub titolo: String,
    pub livello: String,
    pub contesto: String,
    pub battute: Vec<Battuta>,
    pub domande: Vec<DomandaDialogo>,
}

pub fn sudtirol_varianti() -> Vec<VarianteAustriaca> {
    let raw = [
        ("der Jänner", "der Januar", "gennaio"),
        ("der Erdapfel", "die Kartoffel", "patata"),
        ("heuer", "dieses Jahr", "quest'anno"),
        ("das Sackerl", "die Tüte", "sacchetto"),
        ("die Palatschinke", "der Pfannkuchen", "crespella"),
        ("die Marille", "die Aprikose", "albicocca"),
        ("die Ribisel", "die Johannisbeere", "ribes"),
        ("der Topfen", "der Quark", "ricotta / quark"),
        ("der Paradeiser", "die Tomate", "pomodoro"),
        ("der Karfiol", "der Blumenkohl", "cavolfiore"),
        ("die Fisolen", "die Bohnen", "fagiolini"),
        ("die Kohlsprossen", "der Rosenkohl", "cavolini di Bruxelles"),
        ("die Semmel", "das Brötchen", "panino"),
        ("die Grammeln", "die Grieben", "ciccioli"),
        ("der Leberkäse", "die Fleischwurst", "polpettone di carne"),
    ];
    raw.iter().map(|(a, s, i)| VarianteAustriaca {
        austriaco: a.to_string(), standard: s.to_string(), italiano: i.to_string(),
    }).collect()
}

pub fn sudtirol_lessico() -> Vec<VoceLessicoSud> {
    let raw = [
        ("der Befund", "referto / risultato clinico", "Ambulatorio"),
        ("die Überweisung", "impegnativa / invio", "Ambulatorio"),
        ("die Krankenkasse", "cassa mutua / assicurazione sanitaria", "Assicurazione"),
        ("die Ordination", "studio medico", "Ambulatorio"),
        ("der Termin", "appuntamento", "Ambulatorio"),
        ("das Rezept", "ricetta", "Ambulatorio"),
        ("die Arbeitsunfähigkeit", "inabilità al lavoro", "Assicurazione"),
        ("die Behandlung", "trattamento", "Terapia"),
        ("die Übung", "esercizio", "Terapia"),
        ("der Patient / die Patientin", "paziente", "Ambulatorio"),
        ("die Schmerzen", "dolori", "Sintomi"),
        ("die Bewegung", "movimento", "Terapia"),
        ("die Physiotherapie", "fisioterapia", "Terapia"),
        ("die Anamnese", "anamnesi", "Ambulatorio"),
        ("die Diagnose", "diagnosi", "Ambulatorio"),
        ("der Therapieplan", "piano terapeutico", "Terapia"),
        ("die Kontrolle", "controllo / visita di controllo", "Ambulatorio"),
    ];
    raw.iter().map(|(t, i, c)| VoceLessicoSud {
        tedesco: t.to_string(), italiano: i.to_string(), categoria: c.to_string(),
    }).collect()
}

pub fn sudtirol_frasi() -> Vec<FraseSud> {
    let raw: &[(&str, &str, &str)] = &[
        ("Guten Tag, haben Sie einen Termin?", "Buongiorno, ha un appuntamento?", ""),
        ("Ich habe seit drei Tagen Schmerzen im Knie.", "Ho dolore al ginocchio da tre giorni.", ""),
        ("Wie stark sind die Schmerzen auf einer Skala von 1 bis 10?", "Quanto è forte il dolore su una scala da 1 a 10?", ""),
        ("Bitte beugen Sie das Knie langsam.", "Pieghi lentamente il ginocchio.", "Imperativo formale: Sie."),
        ("Bitte strecken Sie das Bein.", "Distenda la gamba.", ""),
        ("Atmen Sie tief ein und langsam aus.", "Inspiri profondamente ed espiri lentamente.", ""),
        ("Die Übungen machen Sie bitte dreimal am Tag.", "Faccia gli esercizi tre volte al giorno.", ""),
        ("Bitte bringen Sie den Befund mit.", "Porti il referto.", ""),
        ("Haben Sie eine Überweisung?", "Ha un'impegnativa?", ""),
        ("Sie können sich hier anmelden.", "Può registrarsi qui.", ""),
        ("Ich verstehe nicht ganz. Können Sie das langsamer sagen?", "Non capisco del tutto. Può dirlo più lentamente?", ""),
        ("Können Sie das bitte wiederholen?", "Può ripetere, per favore?", ""),
        ("Ich spreche noch nicht so gut Deutsch.", "Non parlo ancora bene il tedesco.", ""),
        ("Sind Sie gesetzlich versichert?", "È assicurato con il servizio sanitario?", ""),
    ];
    raw.iter().map(|(t, i, n)| FraseSud {
        tedesco: t.to_string(), italiano: i.to_string(), nota: n.to_string(),
    }).collect()
}

pub fn sudtirol_dialoghi() -> Vec<DialogoSud> {
    vec![
        DialogoSud {
            titolo: "In der Ordination".into(), livello: "A2".into(),
            contesto: "Alla reception di uno studio medico.".into(),
            battute: vec![
                Battuta { parlante: "Rezeption".into(), testo: "Guten Tag, haben Sie einen Termin?".into() },
                Battuta { parlante: "Patient".into(), testo: "Ja, ich heiße Rossi. Ich habe um zehn Uhr einen Termin.".into() },
                Battuta { parlante: "Rezeption".into(), testo: "Einen Moment bitte. Haben Sie die Überweisung dabei?".into() },
                Battuta { parlante: "Patient".into(), testo: "Ja, hier bitte. Und meinen Befund habe ich auch.".into() },
            ],
            domande: vec![
                DomandaDialogo { domanda: "A che ora ha l'appuntamento il paziente?".into(), opzioni: vec!["Alle 9".into(), "Alle 10".into(), "Alle 11".into(), "Alle 12".into()], corretta: 1 },
                DomandaDialogo { domanda: "Che cosa porta con sé il paziente?".into(), opzioni: vec!["Solo il documento".into(), "L'impegnativa e il referto".into(), "La ricetta".into(), "Niente".into()], corretta: 1 },
            ],
        },
        DialogoSud {
            titolo: "Physiotherapie: Anamnese".into(), livello: "B1".into(),
            contesto: "Primo colloquio in fisioterapia.".into(),
            battute: vec![
                Battuta { parlante: "Physiotherapeutin".into(), testo: "Guten Morgen, ich bin Frau Huber. Wo tut es weh?".into() },
                Battuta { parlante: "Patient".into(), testo: "Mein Knie, seit drei Tagen. Besonders beim Treppensteigen.".into() },
                Battuta { parlante: "Physiotherapeutin".into(), testo: "Wie stark sind die Schmerzen von eins bis zehn?".into() },
                Battuta { parlante: "Patient".into(), testo: "Etwa sieben. In der Nacht wird es besser.".into() },
            ],
            domande: vec![
                DomandaDialogo { domanda: "Da quanto tempo ha dolore il paziente?".into(), opzioni: vec!["Da un giorno".into(), "Da tre giorni".into(), "Da una settimana".into(), "Da un mese".into()], corretta: 1 },
                DomandaDialogo { domanda: "Quando peggiora il dolore?".into(), opzioni: vec!["Di notte".into(), "Salendo le scale".into(), "Da seduto".into(), "Mangiando".into()], corretta: 1 },
            ],
        },
        DialogoSud {
            titolo: "Kollegengespräch: Jänner und Erdäpfel".into(), livello: "B1".into(),
            contesto: "Due colleghi parlano in pausa pranzo, con lessico austriaco.".into(),
            battute: vec![
                Battuta { parlante: "Anna".into(), testo: "Hast du heuer schon Urlaub gemacht?".into() },
                Battuta { parlante: "Lukas".into(), testo: "Ja, im Jänner. Ich war in Südtirol.".into() },
                Battuta { parlante: "Anna".into(), testo: "Und? Gab es dort gute Erdäpfel?".into() },
                Battuta { parlante: "Lukas".into(), testo: "Ja, und einen tollen Topfenstrudel. Die Semmeln waren auch frisch.".into() },
            ],
            domande: vec![
                DomandaDialogo { domanda: "Quando è andato in vacanza Lukas?".into(), opzioni: vec!["A gennaio".into(), "A luglio".into(), "In autunno".into(), "A Natale".into()], corretta: 0 },
                DomandaDialogo { domanda: "Che cosa ha mangiato di buono?".into(), opzioni: vec!["Una pizza".into(), "Uno strudel di ricotta".into(), "Un panino".into(), "Una torta".into()], corretta: 1 },
            ],
        },
    ]
}

/// Traduce una variante austriaca nello standard.
pub fn sudtirol_standard_di(austriaco: &str) -> Option<String> {
    sudtirol_varianti().into_iter()
        .find(|v| v.austriaco.eq_ignore_ascii_case(austriaco))
        .map(|v| v.standard)
}

// ── Dialoghi di ascolto (portati da Dialoghi.kt) ──

#[derive(Debug, Clone)]
pub struct BattutaDialogo { pub parlante: String, pub testo: String, pub traduzione: String }

#[derive(Debug, Clone)]
pub struct DomandaDialogoAscolto { pub domanda: String, pub opzioni: Vec<String>, pub corretta: usize }

#[derive(Debug, Clone)]
pub struct DialogoAscolto { pub id: String, pub titolo: String, pub livello: String, pub traduzione: String, pub battute: Vec<BattutaDialogo>, pub domande: Vec<DomandaDialogoAscolto>, pub sanitario: bool }

pub fn dialoghi_ascolto() -> Vec<DialogoAscolto> {
    vec![
        DialogoAscolto {
            id: "dial_01".into(), titolo: "Alla reception".into(), livello: "A2".into(), traduzione: "Un paziente si registra in fisioterapia.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Guten Tag, ich habe einen Termin um zehn Uhr.".into(), traduzione: "Buongiorno, ho un appuntamento alle dieci.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Guten Tag. Wie ist Ihr Name, bitte?".into(), traduzione: "Buongiorno. Come si chiama, per favore?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Ich heiße Rossi. Mario Rossi.".into(), traduzione: "Mi chiamo Rossi. Mario Rossi.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Herr Rossi. Haben Sie die Überweisung dabei?".into(), traduzione: "Signor Rossi. Ha l'impegnativa con sé?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Ja, hier ist sie. Und den Befund vom Orthopäden.".into(), traduzione: "Sì, eccola. E il referto dell'ortopedico.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Perfekt. Bitte nehmen Sie Platz. Die Therapeutin kommt gleich.".into(), traduzione: "Perfetto. Si accomodi pure. La terapista arriva tra poco.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Danke schön. Wie lange dauert die erste Sitzung?".into(), traduzione: "Grazie. Quanto dura la prima seduta?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Etwa 45 Minuten. Wir machen die Anamnese und einen Befund.".into(), traduzione: "Circa 45 minuti. Facciamo l'anamnesi e un esame.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "A che ora è l'appuntamento?".into(), opzioni: vec!["Alle 9".into(), "Alle 10".into(), "Alle 11".into(), "Alle 12".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Cosa porta il paziente?".into(), opzioni: vec!["Solo l'impegnativa".into(), "Impegnativa e referto".into(), "Solo il referto".into(), "Niente".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Quanto dura la prima seduta?".into(), opzioni: vec!["30 minuti".into(), "45 minuti".into(), "60 minuti".into(), "90 minuti".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_02".into(), titolo: "Anamnesi: mal di schiena".into(), livello: "B1".into(), traduzione: "Prima seduta: il fisioterapista fa l'anamnesi a un paziente con lombalgia.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Guten Morgen, ich bin Herr Müller, Ihr Physiotherapeut. Was führt Sie zu uns?".into(), traduzione: "Buongiorno, sono il signor Müller, il suo fisioterapista. Cosa la porta da noi?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ich habe seit drei Wochen Rückenschmerzen, vor allem morgens.".into(), traduzione: "Ho mal di schiena da tre settimane, soprattutto la mattina.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Können Sie mir zeigen, wo genau es wehtut?".into(), traduzione: "Mi può indicare dove fa male esattamente?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Hier, im unteren Rücken. Manchmal strahlt es ins Bein aus.".into(), traduzione: "Qui, in basso alla schiena. A volte irradia alla gamba.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Haben Sie Taubheitsgefühle oder Kribbeln im Bein?".into(), traduzione: "Ha intorpidimento o formicolio alla gamba?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Nein, nur Schmerzen. Aber nachts wache ich manchmal auf.".into(), traduzione: "No, solo dolore. Ma di notte mi sveglio a volte.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Verstehen Sie. Machen Sie Sport? Wie ist Ihr Arbeitsalltag?".into(), traduzione: "Capisco. Fa sport? Com'è la sua giornata lavorativa?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ich sitze viel am Schreibtisch. Jogge aber zweimal die Woche.".into(), traduzione: "Sto molto alla scrivania. Ma corro due volte a settimana.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Gut. Dann schauen wir uns die Beweglichkeit an. Bitte beugen Sie sich vor.".into(), traduzione: "Bene. Allora controlliamo la mobilità. Si pieghi in avanti per favore.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Da quanto tempo ha dolore il paziente?".into(), opzioni: vec!["Una settimana".into(), "Due settimane".into(), "Tre settimane".into(), "Un mese".into()], corretta: 2 },
                DomandaDialogoAscolto { domanda: "Dove irradia il dolore?".into(), opzioni: vec!["Al braccio".into(), "Alla gamba".into(), "Al collo".into(), "Non irradia".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Cosa fa il paziente di lavoro?".into(), opzioni: vec!["Lavoro fisico".into(), "Lavoro alla scrivania".into(), "Insegnante".into(), "In pensione".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_03".into(), titolo: "Telefonata: appuntamento".into(), livello: "A2".into(), traduzione: "Chiamata per fissare un appuntamento di fisioterapia.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Physiotherapie Huber, guten Tag.".into(), traduzione: "Fisioterapia Huber, buongiorno.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Guten Tag, ich möchte gerne einen Termin vereinbaren.".into(), traduzione: "Buongiorno, vorrei fissare un appuntamento.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Gerne. Haben Sie eine Überweisung vom Arzt?".into(), traduzione: "Volentieri. Ha un'impegnativa del medico?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ja, die habe ich. Ich habe Knieschmerzen seit einer Woche.".into(), traduzione: "Sì, ce l'ho. Ho dolore al ginocchio da una settimana.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Verstehe. Haben Sie morgens oder nachmittags Zeit?".into(), traduzione: "Capisco. Ha tempo la mattina o il pomeriggio?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Nachmittags wäre besser. Ab 14 Uhr.".into(), traduzione: "Il pomeriggio sarebbe meglio. Dalle 14.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Morgen um 14:30 wäre frei. Passt das?".into(), traduzione: "Domani alle 14:30 sarebbe libero. Va bene?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ja, perfekt. Danke schön!".into(), traduzione: "Sì, perfetto. Grazie mille!".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Bitte bringen Sie die Überweisung und bequeme Kleidung mit.".into(), traduzione: "Porti l'impegnativa e abbigliamento comodo.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Quando ha tempo il paziente?".into(), opzioni: vec!["Mattina".into(), "Pomeriggio dalle 14".into(), "Sera".into(), "Weekend".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Quando è l'appuntamento?".into(), opzioni: vec!["Oggi 14:30".into(), "Domani 14:30".into(), "Domani 10:00".into(), "Dopodomani 14:30".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Cosa deve portare?".into(), opzioni: vec!["Solo documento".into(), "Impegnativa e abbigliamento comodo".into(), "Referto RM".into(), "Niente".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_04".into(), titolo: "In pausa: colleghi austriaci".into(), livello: "B1".into(), traduzione: "Due colleghi in mensa usano varianti austriache (Jänner, Erdapfel, Topfen).".into(), sanitario: false,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Na, hast du heuer schon Urlaub gemacht?".into(), traduzione: "Allora, hai fatto le ferie quest'anno?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ja, im Jänner war ich in Südtirol. Die Berge sind traumhaft.".into(), traduzione: "Sì, a gennaio ero in Südtirol. Le montagne sono da sogno.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Und? Gab's dort gute Erdäpfel? Die Knödel sind der Hammer.".into(), traduzione: "E? Ci sono buone patate lì? I canederli sono spettacolari.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Absolut. Und einen Topfenstrudel muss man probieren. Nicht Quark, Topfen!".into(), traduzione: "Assolutamente. E devi provare lo strudel di topfen. Non quark, topfen!".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Stimmt. Bei uns sagt man Semmel, nicht Brötchen. Und Paradeiser für Tomaten.".into(), traduzione: "Vero. Da noi si dice Semmel, non Brötchen. E Paradeiser per pomodori.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Genau. In der Ordination sagen die Patienten oft 'Jänner' statt Januar.".into(), traduzione: "Esatto. In ambulatorio i pazienti dicono spesso 'Jänner' invece di Januar.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Macht nichts. Hauptsache, wir verstehen uns. Prost!".into(), traduzione: "Non importa. L'importante è capirsi. Cin cin!".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Prost! Auf die Erdäpfel!".into(), traduzione: "Cin cin! Alle patate!".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Quando è andato in vacanza il collega B?".into(), opzioni: vec!["A luglio".into(), "A gennaio".into(), "Ad agosto".into(), "A Natale".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Quale dolce austriaco menzionano?".into(), opzioni: vec!["Apfelstrudel".into(), "Topfenstrudel".into(), "Sachertorte".into(), "Kaiserschmarrn".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Come dicono 'panino' in Austria?".into(), opzioni: vec!["Brötchen".into(), "Semmel".into(), "Weck".into(), "Schrippe".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_05".into(), titolo: "Esercizi a casa: istruzioni".into(), livello: "B1".into(), traduzione: "Il fisioterapista spiega gli esercizi da fare a casa per il ginocchio.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "So, jetzt zeige ich Ihnen die Hausübungen für Ihr Knie.".into(), traduzione: "Allora, le mostro gli esercizi da fare a casa per il ginocchio.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Wie oft soll ich die machen?".into(), traduzione: "Quanto spesso devo farli?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Dreimal am Tag. Morgens, mittags, abends. Je zehn Wiederholungen.".into(), traduzione: "Tre volte al giorno. Mattina, mezzogiorno, sera. Dieci ripetizioni ciascuna.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Und wenn es wehtut? Soll ich aufhören?".into(), traduzione: "E se fa male? Devo smettere?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Leichter Schmerz ist okay. Aber scharfer Schmerz: sofort aufhören.".into(), traduzione: "Un leggero dolore va bene. Ma dolore acuto: smetta subito.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Verstanden. Wann kommt die Kontrolle?".into(), traduzione: "Capito. Quando è il controllo?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "In zwei Wochen. Bringen Sie das Tagebuch mit, wo Sie die Übungen eintragen.".into(), traduzione: "Tra due settimane. Porti il diario dove segna gli esercizi.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Alles klar. Danke, Herr Müller.".into(), traduzione: "Tutto chiaro. Grazie, signor Müller.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Gute Besserung! Und denken Sie dran: Bewegung ist die beste Medizin.".into(), traduzione: "Buona guarigione! E ricordi: il movimento è la migliore medicina.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Quante volte al giorno gli esercizi?".into(), opzioni: vec!["Una".into(), "Due".into(), "Tre".into(), "Quattro".into()], corretta: 2 },
                DomandaDialogoAscolto { domanda: "Quando è il prossimo controllo?".into(), opzioni: vec!["Tra una settimana".into(), "Tra due settimane".into(), "Tra un mese".into(), "Non c'è".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Cosa portare al controllo?".into(), opzioni: vec!["Il referto".into(), "Il diario degli esercizi".into(), "La ricetta".into(), "L'impegnativa".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_06".into(), titolo: "Primo giorno di corso".into(), livello: "A1".into(), traduzione: "Due partecipanti al corso di tedesco si conoscono presentandosi.".into(), sanitario: false,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Hallo! Sind Sie auch hier im Deutschkurs?".into(), traduzione: "Ciao! È anche lei qui nel corso di tedesco?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ja, ich bin hier neu. Ich heiße Sophie. Und du?".into(), traduzione: "Sì, sono nuovo qui. Mi chiamo Sophie. E tu?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Ich bin Thomas, sehr schön. Woher kommst du?".into(), traduzione: "Sono Thomas, molto piacere. Da dove vieni?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ich komme aus Brixen. Und du?".into(), traduzione: "Vengo da Bressanone. E tu?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Auch aus Brixen! Warum lernst du Deutsch?".into(), traduzione: "Anche io da Bressanone! Perché studi tedesco?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Für die Arbeit und die Prüfung. Und du?".into(), traduzione: "Per il lavoro e per l'esame. E tu?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Für meine Arbeit im Krankenhaus. Der Kurs ist jeden Dienstag.".into(), traduzione: "Per il mio lavoro in ospedale. Il corso è ogni martedì.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Da dove viene Sophie?".into(), opzioni: vec!["Da Bolzano".into(), "Da Bressanone".into(), "Da Merano".into(), "Da Trento".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Perché studia Thomas?".into(), opzioni: vec!["Per esame".into(), "Per il lavoro all'ospedale".into(), "Per hobby".into(), "Per un trasloco".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Quando è il corso?".into(), opzioni: vec!["Lunedì".into(), "Mercoledì".into(), "Martedì".into(), "Giovedì".into()], corretta: 2 },
            ],
        },
        DialogoAscolto {
            id: "dial_07".into(), titolo: "In farmacia".into(), livello: "A2".into(), traduzione: "Un cliente chiede alla farmacia un rimedio per il raffreddore.".into(), sanitario: false,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Guten Tag, was kann ich für Sie tun?".into(), traduzione: "Buongiorno, cosa posso fare per lei?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Guten Tag. Ich habe Schnuppen und Kopfschmerzen.".into(), traduzione: "Buongiorno. Ho il raffreddore e mal di testa.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Seit wann haben Sie die Symptome?".into(), traduzione: "Da quando ha i sintomi?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Seit drei Tagen. Nachts kann ich schlecht schlafen.".into(), traduzione: "Da tre giorni. Di notte dormo male.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Ich empfehle diese Tropfen und eine Salbe gegen Schmerzen.".into(), traduzione: "Le consiglio queste gocce e una crema contro il dolore.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Wie oft soll ich die Tropfen einnehmen?".into(), traduzione: "Ogni quanto devo prendere le gocce?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Dreimal täglich, nach dem Essen. Gute Besserung!".into(), traduzione: "Tre volte al giorno, dopo i pasti. Migliorie!".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Che cosa ha il cliente?".into(), opzioni: vec!["Raffreddore e mal di testa".into(), "Solo febbre".into(), "Mal di schiena".into(), "Vertigini".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Da quanto ha i sintomi?".into(), opzioni: vec!["Da un giorno".into(), "Da tre giorni".into(), "Da una settimana".into(), "Da un mese".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Come si prendono le gocce?".into(), opzioni: vec!["Prima dei pasti".into(), "Tre volte al giorno dopo i pasti".into(), "Solo la sera".into(), "Due volte nel weekend".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_08".into(), titolo: "Al supermercato".into(), livello: "A2".into(), traduzione: "Un cliente chiede indicazioni al commesso e fa la spesa.".into(), sanitario: false,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Entschuldigung, wo finde ich Brot und Milch?".into(), traduzione: "Scusi, dove trovo il pane e il latte?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Das Brot ist am Backregal hinten links. Die Milch bei den Kühlregalen.".into(), traduzione: "Il pane è nel reparto panetteria, in fondo a sinistra. Il latte nei frigo dei latticini.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Vielen Dank. Und wie viel kostet der Semmel?".into(), traduzione: "Grazie mille. E quanto costa la ciabatta?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "40 Cent das Stück. Heute haben wir eine Aktion: drei für ein Euro.".into(), traduzione: "40 centesimi l'uno. Oggi c'è una promozione: tre per un euro.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Super, dann nehme ich drei. Woher ist der Käse?".into(), traduzione: "Bene, allora ne prendo tre. Da dove viene il formaggio?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Von einer Käserei in Südtirol — sehr gutes Produkt!".into(), traduzione: "Da una latteria in Alto Adige — un ottimo prodotto!".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Dann nehme ich auch davon 200 Gramm. Danke schön.".into(), traduzione: "Allora ne prendo anche 200 grammi. Grazie mille.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Quanto costa la ciabatta?".into(), opzioni: vec!["30 centesimi".into(), "40 centesimi".into(), "Un euro".into(), "Un euro e venti".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Che promozione c'è oggi?".into(), opzioni: vec!["Tre ciabatte per un euro".into(), "Due latte per uno".into(), "Sconto del 20%".into(), "Formaggio gratis".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Da dove viene il formaggio?".into(), opzioni: vec!["Da una latteria altoatesina".into(), "Dall'Austria".into(), "Dall'Olanda".into(), "Da una fabbrica locale".into()], corretta: 0 },
            ],
        },
        DialogoAscolto {
            id: "dial_09".into(), titolo: "Dal medico di base".into(), livello: "A2".into(), traduzione: "Consultazione con il medico di base per un problema alla schiena.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Guten Tag, Frau Huber. Was führt Sie heute zu mir?".into(), traduzione: "Buongiorno, signora Huber. Cosa la porta oggi da me?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ich habe seit einer Woche Rückenschmerzen, links unterhalb der Schulter.".into(), traduzione: "Ho mal di schiena da una settimana, a sinistra sotto la spalla.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Fieber? Husten? Gewichtsverlust?".into(), traduzione: "Febbre? Tosse? Perdita di peso?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Nein, keine. Aber nachts wache ich auf.".into(), traduzione: "No, niente. Ma di notte mi sveglio.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Ich verschreibe Ihnen ein Schmerzmittel und eine Überweisung zur Physiotherapie.".into(), traduzione: "Le prescrivo un antidolorifico e un'impegnativa per la fisioterapia.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Wie lange muss ich die Tabletten nehmen?".into(), traduzione: "Per quanto tempo devo prendere le compresse?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Vier Tage, maximal zweimal täglich. Wenn es nicht besser wird: bitte zurück.".into(), traduzione: "Quattro giorni, al massimo due volte al giorno. Se non migliora, torni da me.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Dove fa male?".into(), opzioni: vec!["Sotto la spalla sinistra".into(), "Sotto la spalla destra".into(), "Sul collo".into(), "Alla gamba".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Cosa riceve la paziente?".into(), opzioni: vec!["Antidolorifico e impegnativa".into(), "Solo la impegnativa".into(), "Una radiografia".into(), "Iniezioni".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Per quanto tempo le compresse?".into(), opzioni: vec!["Due giorni".into(), "Quattro giorni".into(), "Una settimana".into(), "Un mese".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_10".into(), titolo: "Dal dentista".into(), livello: "B1".into(), traduzione: "Visita dentistica: dolore al dente e cura discussa con la dottoressa.".into(), sanitario: false,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Guten Tag, setzen Sie sich bitte. Welcher Zahn tut weh?".into(), traduzione: "Buongiorno, si sieda per favore. Quale dente fa male?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Unten rechts, der Backenzahn. Beim Kauen tut er besonders weh.".into(), traduzione: "In basso a destra, il molare. Quando mastico fa male, soprattutto.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Öffnen Sie bitte den Mund. ... Ja, ich sehe eine kleine Karies.".into(), traduzione: "Apra la bocca per favore. ... Sì, vedo una piccola carie.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Muss es eine Wurzelbehandlung sein? Ich habe Angst davor.".into(), traduzione: "Deve fare la devitalizzazione? Ho paura.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Nein, glaube ich nicht. Eine Füllung reicht. Mit Betäubung fühlen Sie nichts.".into(), traduzione: "No, credo di no. Basta un'otturazione. Con l'anestesia non sentirà nulla.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Darf ich nachher essen? Und wie oft die Zahnseide?".into(), traduzione: "Posso mangiare dopo? E ogni quanto il filo interdentale?".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Zwei Stunden warten. Und die Zahnseide bitte jeden Abend.".into(), traduzione: "Attenda due ore. E il filo interdentale ogni sera, per favore.".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Quale dente fa male?".into(), opzioni: vec!["La molare in basso a destra".into(), "L'incisivo in alto".into(), "Il dente del giudizio".into(), "Nessuno".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Che trattamento servirà?".into(), opzioni: vec!["Una devitalizzazione".into(), "Un'otturazione".into(), "L'estrazione".into(), "Una pulizia".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Quando si può mangiare?".into(), opzioni: vec!["Subito".into(), "Dopo due ore".into(), "Il giorno dopo".into(), "Mai con anestesia".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_11".into(), titolo: "Il primo giorno al lavoro".into(), livello: "B1".into(), traduzione: "Un nuovo collega viene presentato in un ambulatorio di fisioterapia.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Frau Rossi, das ist Thomas, unser neuer Kollege.".into(), traduzione: "Signora Rossi, questo è Thomas, il nostro nuovo collega.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Schön, dich kennenzulernen! Wo hast du vorher gearbeitet?".into(), traduzione: "Piacere di conoscerti! Dove hai lavorato prima?".into() },
                BattutaDialogo { parlante: "C".into(), testo: "In einem Rehabilitationszentrum in Bozen. Zwei Jahre lang.".into(), traduzione: "In un centro di riabilitazione a Bolzano. Per due anni.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Und welche Bereiche kennst du gut?".into(), traduzione: "E quali settori conosci bene?".into() },
                BattutaDialogo { parlante: "C".into(), testo: "Rückenprobleme, vor allem. Und auch Sportverletzungen.".into(), traduzione: "Problemi alla schiena, soprattutto. E anche lesioni sportive.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Perfekt. Wir bekommen viele Patienten mit Rückenschmerzen.".into(), traduzione: "Perfetto. Abbiamo molti pazienti con mal di schiena.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Am Montag kommt die erste Patientin um neun Uhr. Du machst die Anamnese.".into(), traduzione: "Lunedì arriva la prima paziente alle nove. Tu fai l'anamnesi.".into() },
                BattutaDialogo { parlante: "C".into(), testo: "Kein Problem. Wo finde ich die Unterlagen?".into(), traduzione: "Nessun problema. Dove trovo i documenti?".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Dove ha lavorato prima Thomas?".into(), opzioni: vec!["In un ospedale a Merano".into(), "In un centro di riabilitazione a Bolzano".into(), "In una palestra".into(), "In un ambulatorio lombardo".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Quali pazienti sono più frequenti?".into(), opzioni: vec!["Con mal di schiena".into(), "Con problemi cardiaci".into(), "Bambini".into(), "Post-operatori del ginocchio".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Quando arriva la prima paziente per Thomas?".into(), opzioni: vec!["Giovedì alle 9".into(), "Lunedì alle 9".into(), "Mercoledì alle 11".into(), "Sabato alle 8".into()], corretta: 1 },
            ],
        },
        DialogoAscolto {
            id: "dial_12".into(), titolo: "Colloquio di lavoro".into(), livello: "B2".into(), traduzione: "Selezione per la posizione di fisioterapista in una clinica sportiva.".into(), sanitario: true,
            battute: vec![
                BattutaDialogo { parlante: "A".into(), testo: "Herr Renato, danke für das Gespräch. Erzählen Sie kurz von sich.".into(), traduzione: "Signor Renato, grazie per il colloquio. Si presenti brevemente.".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Gerne. Ich bin Physiotherapeut mit fünf Jahren Erfahrung, davon zwei in der Sportmedizin.".into(), traduzione: "Con piacere. Sono fisioterapista con cinque anni di esperienza, di cui due in medicina sportiva.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Was empfinden Sie als Ihre größte Stärke?".into(), traduzione: "Cosa considera la sua maggiore forza?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Geduld und Flexibilität bei komplexen Fällen. Außerdem kommuniziere ich klar mit dem Team.".into(), traduzione: "Pazienza e flessibilità nei casi complessi. Inoltre comunico in modo chiaro con il team.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Wie stehen Sie zu Fortbildungen in diesem Jahr?".into(), traduzione: "Cosa pensa della formazione continua quest'anno?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Sehr positiv. Ich plane eine Zusatzausbildung in Manueller Therapie.".into(), traduzione: "Molto positivamente. Ho in programma un percorso aggiuntivo in terapia manuale.".into() },
                BattutaDialogo { parlante: "A".into(), testo: "Wir melden uns bis Freitag. Möchten Sie noch etwas fragen?".into(), traduzione: "Ci faremo sentire entro venerdì. Vuole farci altre domande?".into() },
                BattutaDialogo { parlante: "B".into(), testo: "Ja: wie groß ist das Team und wie viele Behandlungen macht man pro Tag?".into(), traduzione: "Sì: quanto è grande il team e quante sedute si fanno al giorno?".into() },
            ],
            domande: vec![
                DomandaDialogoAscolto { domanda: "Da quanti anni lavora il candidato?".into(), opzioni: vec!["Due".into(), "Cinque".into(), "Dieci".into(), "Venti".into()], corretta: 1 },
                DomandaDialogoAscolto { domanda: "Cosa pianifica come formazione aggiuntiva?".into(), opzioni: vec!["Terapia manuale".into(), "Osteopatia".into(), "Yoga terapeutico".into(), "Bioingegneria".into()], corretta: 0 },
                DomandaDialogoAscolto { domanda: "Quando riceverà la risposta?".into(), opzioni: vec!["Entro lunedì".into(), "Entro giovedì".into(), "Entro venerdì".into(), "Il mese prossimo".into()], corretta: 2 },
            ],
        },
    ]
}

/// Filtra i dialoghi in base al contesto sanitario.
pub fn dialoghi_per_contesto(mostra_sanitario: bool) -> Vec<DialogoAscolto> {
    dialoghi_ascolto().into_iter().filter(|d| mostra_sanitario || !d.sanitario).collect()
}

// ── Placement Test (test di ingresso, portato da PlacementTest.kt) ──

#[derive(Debug, Clone)]
pub struct DomandaPlacement { pub domanda: String, pub opzioni: Vec<String>, pub corretta: usize, pub livello: String }

pub fn placement_domande() -> Vec<DomandaPlacement> {
    vec![
        DomandaPlacement { domanda: "___ heißt du?".into(), opzioni: vec!["Wie".into(), "Was".into(), "Wo".into(), "Wer".into()], corretta: 0, livello: "A1".into() },
        DomandaPlacement { domanda: "Ich ___ aus Italien.".into(), opzioni: vec!["komme".into(), "kommt".into(), "kommen".into(), "kommst".into()], corretta: 0, livello: "A1".into() },
        DomandaPlacement { domanda: "___ Buch ist neu.".into(), opzioni: vec!["Der".into(), "Die".into(), "Das".into(), "Den".into()], corretta: 2, livello: "A1".into() },
        DomandaPlacement { domanda: "Ich habe ___ Hund.".into(), opzioni: vec!["ein".into(), "eine".into(), "einen".into(), "einem".into()], corretta: 2, livello: "A2".into() },
        DomandaPlacement { domanda: "Ich ___ gestern ins Kino gegangen.".into(), opzioni: vec!["bin".into(), "habe".into(), "war".into(), "hatte".into()], corretta: 0, livello: "A2".into() },
        DomandaPlacement { domanda: "Ich warte ___ den Bus.".into(), opzioni: vec!["auf".into(), "für".into(), "mit".into(), "zu".into()], corretta: 0, livello: "A2".into() },
        DomandaPlacement { domanda: "Das ist der Mann, ___ ich gestern getroffen habe.".into(), opzioni: vec!["den".into(), "dem".into(), "der".into(), "dessen".into()], corretta: 0, livello: "A2".into() },
        DomandaPlacement { domanda: "Wenn es morgen regnet, ___ ich zu Hause.".into(), opzioni: vec!["bleibe".into(), "bleiben".into(), "bleibst".into(), "geblieben".into()], corretta: 0, livello: "A2".into() },
        DomandaPlacement { domanda: "Das Buch ___ von vielen Menschen gelesen.".into(), opzioni: vec!["wird".into(), "werden".into(), "ist".into(), "hat".into()], corretta: 0, livello: "B1".into() },
        DomandaPlacement { domanda: "Wenn ich mehr Zeit ___, würde ich mehr lernen.".into(), opzioni: vec!["hätte".into(), "habe".into(), "hatte".into(), "hätte gehabt".into()], corretta: 0, livello: "B1".into() },
        DomandaPlacement { domanda: "Der Mann, ___ Auto gestohlen wurde, ruft die Polizei.".into(), opzioni: vec!["dessen".into(), "deren".into(), "dem".into(), "den".into()], corretta: 0, livello: "B1".into() },
        DomandaPlacement { domanda: "___ es regnet, gehen wir spazieren.".into(), opzioni: vec!["Obwohl".into(), "Weil".into(), "Damit".into(), "Wenn".into()], corretta: 0, livello: "B1".into() },
    ]
}

/// Valuta il test: ritorna (livello, messaggio).
pub fn placement_valuta(corrette: usize) -> (&'static str, &'static str) {
    match corrette {
        0..=2 => ("A0", "Parti dalle basi: il percorso A0 e' quello giusto."),
        3..=5 => ("A1", "Hai gia' qualche base: puoi partire da A1."),
        6..=8 => ("A2", "Buona base: parti da A2 e rinforza il Perfetto."),
        _ => ("B1", "Hai solide basi: puoi partire da B1."),
    }
}

// ── Sprechübungen (esercizi di produzione orale, portati da Sprechuebungen.kt) ──

#[derive(Debug, Clone)]
pub struct PromptSprechen { pub id: String, pub livello: String, pub situazione: String, pub prompt: String, pub vocabolario: Vec<String>, pub modello: String, pub sanitario: bool }

pub fn sprechtuebungen_prompts() -> Vec<PromptSprechen> {
    vec![
        PromptSprechen { id: "sp-a0-01".into(), livello: "A0".into(), situazione: "Salutare".into(), prompt: "Saluta qualcuno e chiedi come sta.".into(), vocabolario: vec!["Hallo".into(), "Guten Tag".into(), "Wie geht es Ihnen?".into(), "mir geht es gut".into()], modello: "Hallo! Guten Tag. Wie geht es Ihnen? — Mir geht es gut, danke.".into(), sanitario: false },
        PromptSprechen { id: "sp-a0-02".into(), livello: "A0".into(), situazione: "Dire il proprio nome".into(), prompt: "Dì come ti chiami e di dove sei.".into(), vocabolario: vec!["Ich heiße...".into(), "Ich bin aus...".into(), "Und du?".into()], modello: "Hallo, ich heiße Thomas. Ich bin aus Italien. Und du? Wie heißt du?".into(), sanitario: false },
        PromptSprechen { id: "sp-a0-03".into(), livello: "A0".into(), situazione: "Sillabare il nome".into(), prompt: "Sillaba il tuo nome alla reception, lettera per lettera.".into(), vocabolario: vec!["Mein Name ist...".into(), "der Buchstabe".into(), "Bitte buchstabieren Sie".into()], modello: "Entschuldigung, bitte buchstabieren Sie Ihren Namen. - Mein Name ist Thomas: T-H-O-M-A-S.".into(), sanitario: false },
        PromptSprechen { id: "sp-a0-04".into(), livello: "A0".into(), situazione: "Numeri e telefono".into(), prompt: "Dì i numeri da 1 a 12 e lascia il tuo numero di telefono.".into(), vocabolario: vec!["eins, zwei, drei".into(), "Meine Nummer ist...".into(), "null, sieben".into()], modello: "Eins, zwei, drei, vier, fünf, sechs, sieben, acht, neun, zehn, elf, zwölf. Meine Telefonnummer ist null-drei-vier-sieben-zwei...".into(), sanitario: false },
        PromptSprechen { id: "sp-a0-05".into(), livello: "A0".into(), situazione: "Non stare bene".into(), prompt: "Dì che non ti senti bene e ringrazia un fisioterapista.".into(), vocabolario: vec!["Ich bin krank".into(), "Es tut weh".into(), "Danke schön".into(), "Tschüss".into()], modello: "Ich bin krank. Der Rücken tut weh. Danke schön für Ihre Hilfe. Tschüss!".into(), sanitario: true },
        PromptSprechen { id: "sp-a0-06".into(), livello: "A0".into(), situazione: "Dire l'età".into(), prompt: "Dì quanti anni hai e chiedi l'età a qualcuno.".into(), vocabolario: vec!["Ich bin ... Jahre alt".into(), "Wie alt bist du?".into(), "Jahre".into()], modello: "Ich bin 32 Jahre alt. Und du? Wie alt bist du? — Ich bin 28.".into(), sanitario: false },
        PromptSprechen { id: "sp-a0-07".into(), livello: "A0".into(), situazione: "I giorni della settimana".into(), prompt: "Dì che giorno è oggi e quando è il tuo appuntamento.".into(), vocabolario: vec!["Heute ist...".into(), "am Dienstag".into(), "der Termin".into(), "morgen".into()], modello: "Heute ist Montag. Mein Termin ist am Dienstag. Morgen gehe ich zur Therapie.".into(), sanitario: false },
        PromptSprechen { id: "sp-a0-08".into(), livello: "A0".into(), situazione: "Presentare la famiglia".into(), prompt: "Presenta la tua famiglia con le frasi più semplici.".into(), vocabolario: vec!["Das ist meine Frau".into(), "mein Mann".into(), "die Kinder".into(), "Wir haben...".into()], modello: "Das ist meine Frau. Wir haben zwei Kinder. Das ist mein Bruder.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-01".into(), livello: "A1".into(), situazione: "Presentazione".into(), prompt: "Presentati: nome, età, dove vivi, cosa fai.".into(), vocabolario: vec!["heißen".into(), "wohnen".into(), "arbeiten".into(), "lernen".into()], modello: "Ich heiße Anna. Ich wohne in Bozen. Ich bin Studentin. Ich lerne Deutsch.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-02".into(), livello: "A1".into(), situazione: "Al supermercato".into(), prompt: "Chiedi dove si trova il pane e compra 2 pagnotte.".into(), vocabolario: vec!["Wo ist...?".into(), "Ich möchte...".into(), "zwei".into(), "Brot".into()], modello: "Entschuldigung, wo ist das Brot? Ich möchte zwei Brote, bitte.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-03".into(), livello: "A1".into(), situazione: "Al ristorante".into(), prompt: "Ordina un caffè e un pezzo di torta.".into(), vocabolario: vec!["Ich hätte gern".into(), "Kaffee".into(), "Kuchen".into(), "bitte".into()], modello: "Ich hätte gern einen Kaffee und ein Stück Kuchen, bitte.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-04".into(), livello: "A1".into(), situazione: "Presentare un amico".into(), prompt: "Presenta un amico: nome, provenienza, lavoro.".into(), vocabolario: vec!["Das ist...".into(), "Er/Sie kommt aus...".into(), "Er/Sie arbeitet als...".into()], modello: "Das ist Marco. Er kommt aus Italien. Er arbeitet als Physiotherapeut.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-05".into(), livello: "A1".into(), situazione: "Chiedere l'ora".into(), prompt: "Chiedi l'ora e rispondi che sono le 3.".into(), vocabolario: vec!["Wie viel Uhr ist es?".into(), "Es ist ... Uhr".into(), "drei".into()], modello: "Entschuldigung, wie viel Uhr ist es? — Es ist drei Uhr.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-06".into(), livello: "A1".into(), situazione: "Chiedere direzioni".into(), prompt: "Chiedi dove si trova la stazione e ringrazia.".into(), vocabolario: vec!["Wo ist...?".into(), "links".into(), "rechts".into(), "geradeaus".into()], modello: "Entschuldigung, wo ist der Bahnhof? — Gehen Sie geradeaus und dann links. — Danke schön!".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-07".into(), livello: "A1".into(), situazione: "Il tempo libero".into(), prompt: "Dì cosa ti piace fare nel tempo libero e chiedi a un amico.".into(), vocabolario: vec!["Ich spiele gern".into(), "Was machst du gern?".into(), "am Wochenende".into(), "hobby".into()], modello: "Ich spiele gern Fußball und lese Bücher. Was machst du gern am Wochenende?".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-08".into(), livello: "A1".into(), situazione: "Alla farmacia".into(), prompt: "Chiedi alla farmacia qualcosa per il mal di testa.".into(), vocabolario: vec!["Ich brauche...".into(), "etwas gegen".into(), "Kopfschmerzen".into(), "Wie oft?".into()], modello: "Ich brauche etwas gegen Kopfschmerzen, bitte. — Nehmen Sie zwei Tabletten pro Tag.".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-09".into(), livello: "A1".into(), situazione: "Comprare vestiti".into(), prompt: "Chiedi una maglia in misura M e il colore blu.".into(), vocabolario: vec!["Ich möchte...".into(), "die Größe".into(), "blau".into(), "probieren".into()], modello: "Ich möchte eine Jacke in Größe M, blau. Kann ich sie probieren?".into(), sanitario: false },
        PromptSprechen { id: "sp-a1-10".into(), livello: "A1".into(), situazione: "Il weekend".into(), prompt: "Dì cosa hai fatto il weekend con i verbi più comuni.".into(), vocabolario: vec!["am Wochenende".into(), "ich habe ... gemacht".into(), "Wir sind ... gegangen".into(), "schön".into()], modello: "Am Wochenende habe ich Fußball gespielt. Wir sind ins Kino gegangen. Es war schön.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-01".into(), livello: "A2".into(), situazione: "Descrivere un appartamento".into(), prompt: "Descrivi il tuo appartamento: stanze, posizione, prezzo.".into(), vocabolario: vec!["Meine Wohnung hat...".into(), "Sie liegt in...".into(), "Sie kostet...".into()], modello: "Meine Wohnung hat zwei Zimmer. Sie liegt in der Stadtmitte. Sie kostet 800 Euro im Monat.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-02".into(), livello: "A2".into(), situazione: "Al lavoro".into(), prompt: "Racconta la tua giornata lavorativa.".into(), vocabolario: vec!["Ich stehe auf um...".into(), "Ich arbeite von... bis...".into(), "Am Abend...".into()], modello: "Ich stehe um 7 Uhr auf. Ich arbeite von 9 bis 17 Uhr. Am Abend koche ich und sehe fern.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-03".into(), livello: "A2".into(), situazione: "Prenotare un tavolo".into(), prompt: "Prenota un tavolo per 4 persone per stasera alle 20.".into(), vocabolario: vec!["Ich möchte reservieren".into(), "für 4 Personen".into(), "um 20 Uhr".into(), "auf den Namen".into()], modello: "Ich möchte einen Tisch für vier Personen reservieren, heute um 20 Uhr, auf den Namen Müller.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-04".into(), livello: "A2".into(), situazione: "Descrivere una persona".into(), prompt: "Descrivi un amico: aspetto, carattere, hobby.".into(), vocabolario: vec!["Er/Sie ist...".into(), "Er/Sie hat...".into(), "Sein/Ihr Hobby ist...".into()], modello: "Marco ist groß und sportlich. Er hat braune Haare. Sein Hobby ist Fußball.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-05".into(), livello: "A2".into(), situazione: "Al telefono".into(), prompt: "Lascia un messaggio: non puoi venire all'appuntamento.".into(), vocabolario: vec!["Hier ist...".into(), "Ich kann leider nicht kommen".into(), "Können wir verschieben?".into(), "Tschüss".into()], modello: "Hier ist Anna. Ich kann leider nicht kommen. Können wir den Termin verschieben? Tschüss!".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-06".into(), livello: "A2".into(), situazione: "Il mio percorso".into(), prompt: "Racconta il tuo percorso di studi o di lavoro fino a oggi.".into(), vocabolario: vec!["Ich habe ... gemacht".into(), "Danach".into(), "Jetzt arbeite ich als...".into(), "seit".into()], modello: "Ich habe die Schule in Bozen gemacht. Danach habe ich drei Monate gewartet. Jetzt arbeite ich als Physiotherapeut.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-07".into(), livello: "A2".into(), situazione: "In vacanza".into(), prompt: "Racconta la tua prossima vacanza: dove, quando, con chi.".into(), vocabolario: vec!["Ich fahre nach...".into(), "mit".into(), "im Sommer".into(), "Eine Woche".into()], modello: "Im Sommer fahre ich mit meiner Familie nach Südtirol. Eine Woche. Wir wandern gern und essen Knödel.".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-08".into(), livello: "A2".into(), situazione: "Chiedere un favore".into(), prompt: "Chiedi a un vicino di farti un piccolo favore.".into(), vocabolario: vec!["Könntest du...?".into(), "Ich helfe dir gern".into(), "zum Beispiel".into(), "kein Problem".into()], modello: "Könntest du bitte meine Post nehmen, während ich in Urlaub bin? — Kein Problem!".into(), sanitario: false },
        PromptSprechen { id: "sp-a2-09".into(), livello: "A2".into(), situazione: "I sintomi al medico".into(), prompt: "Descrivi il tuo problema di salute al medico.".into(), vocabolario: vec!["Ich habe ... Schmerzen".into(), "seit".into(), "besonders nachts".into(), "Können Sie helfen?".into()], modello: "Ich habe Rückenschmerzen seit zwei Wochen, besonders nachts. Der Rücken ist morgens steif. Können Sie mir helfen?".into(), sanitario: false },
        PromptSprechen { id: "sp-b1-01".into(), livello: "B1".into(), situazione: "Esperienza di viaggio".into(), prompt: "Racconta un viaggio che hai fatto: dove, quando, cosa hai fatto.".into(), vocabolario: vec!["Ich war in...".into(), "Es war...".into(), "Wir haben...".into(), "Das Beste war...".into()], modello: "Ich war letztes Jahr in Berlin. Es war sehr interessant. Wir haben viele Museen besucht. Das Beste war das Brandenburger Tor.".into(), sanitario: false },
        PromptSprechen { id: "sp-b1-02".into(), livello: "B1".into(), situazione: "Pro e contro".into(), prompt: "Dai la tua opinione su lavorare da casa: vantaggi e svantaggi.".into(), vocabolario: vec!["Ich finde, dass...".into(), "Ein Vorteil ist...".into(), "Ein Nachteil ist...".into(), "Trotzdem...".into()], modello: "Ich finde, dass Homeoffice gut ist. Ein Vorteil ist die Flexibilität. Ein Nachteil ist die Einsamkeit. Trotzdem bevorzuge ich es.".into(), sanitario: false },
        PromptSprechen { id: "sp-b1-03".into(), livello: "B1".into(), situazione: "Esperienza lavorativa".into(), prompt: "Racconta la tua esperienza professionale e i tuoi obiettivi.".into(), vocabolario: vec!["Ich arbeite als...".into(), "Meine Aufgaben sind...".into(), "In Zukunft möchte ich...".into()], modello: "Ich arbeite als Physiotherapeut. Meine Aufgaben sind Behandlungen und Beratung. In Zukunft möchte ich mich weiterbilden.".into(), sanitario: true },
        PromptSprechen { id: "sp-b1-04".into(), livello: "B1".into(), situazione: "Pianificare insieme".into(), prompt: "Proponi a un collega di organizzare un evento: data, luogo, attività.".into(), vocabolario: vec!["Wir könnten...".into(), "Was hältst du von...?".into(), "Wann passt es dir?".into(), "Einverstanden?".into()], modello: "Wir könnten am Samstag ein Fest machen. Was hältst du von einem Grillabend? Wann passt es dir? Einverstanden?".into(), sanitario: false },
        PromptSprechen { id: "sp-b1-05".into(), livello: "B1".into(), situazione: "Raccontare un problema".into(), prompt: "Racconta un problema che hai avuto e come lo hai risolto.".into(), vocabolario: vec!["Ich hatte ein Problem...".into(), "Das Problem war...".into(), "Ich habe... gemacht".into(), "Am Ende...".into()], modello: "Ich hatte ein Problem mit meinem Computer. Das Problem war ein Virus. Ich habe einen Techniker gerufen. Am Ende war alles gelöst.".into(), sanitario: false },
        PromptSprechen { id: "sp-b1-06".into(), livello: "B1".into(), situazione: "Presentare la tua città".into(), prompt: "Presenta la tua città a un ospite: cosa visitare e fare.".into(), vocabolario: vec!["Meine Heimatstadt ist...".into(), "Die Lage ist...".into(), "Man kann...".into(), "Sehr gut gefällt mir...".into()], modello: "Meine Heimatstadt ist Bozen. Die Lage zwischen den Bergen ist wunderschön. Man kann dort sehr gut wandern und essen. Sehr gut gefällt mir der Markt.".into(), sanitario: false },
        PromptSprechen { id: "sp-b2-01".into(), livello: "B2".into(), situazione: "Presentazione professionale".into(), prompt: "Presenta il tuo profilo professionale: formazione, esperienza, obiettivi.".into(), vocabolario: vec!["Ich habe... studiert".into(), "Meine Erfahrung umfasst...".into(), "Mein Ziel ist...".into(), "Ich bin besonders interessiert an...".into()], modello: "Ich habe Physiotherapie studiert. Meine Erfahrung umfasst fünf Jahre in einer Klinik. Mein Ziel ist, mich auf Sportmedizin zu spezialisieren.".into(), sanitario: true },
        PromptSprechen { id: "sp-b2-02".into(), livello: "B2".into(), situazione: "Argomentare".into(), prompt: "Argomenta su un tema attuale: vantaggi e svantaggi, poi la tua posizione.".into(), vocabolario: vec!["Einerseits... andererseits...".into(), "Ich bin der Meinung, dass...".into(), "Zusammenfassend...".into(), "Meiner Ansicht nach...".into()], modello: "Einerseits spart Homeoffice Zeit, andererseits fehlt der soziale Kontakt. Ich bin der Meinung, dass eine Mischung am besten ist. Zusammenfassend: Flexibilität ist wichtig, aber nicht auf Kosten der Teamarbeit.".into(), sanitario: false },
        PromptSprechen { id: "sp-b2-03".into(), livello: "B2".into(), situazione: "Spiegare un processo".into(), prompt: "Spiega un processo o una procedura step by step.".into(), vocabolario: vec!["Zuerst...".into(), "Dann...".into(), "Anschließend...".into(), "Zum Schluss...".into()], modello: "Zuerst begrüße ich den Patienten. Dann erkläre ich die Übung. Anschließend demonstriere ich die Bewegung. Zum Schluss lasse ich den Patienten die Übung selbst machen.".into(), sanitario: true },
        PromptSprechen { id: "sp-b2-04".into(), livello: "B2".into(), situazione: "Discutere".into(), prompt: "Discuti con un collega: proponi una soluzione e difendi la tua posizione.".into(), vocabolario: vec!["Ich würde vorschlagen...".into(), "Das hat den Vorteil, dass...".into(), "Allerdings...".into(), "Es wäre sinnvoll, wenn...".into()], modello: "Ich würde vorschlagen, die Termine neu zu organisieren. Das hat den Vorteil, dass die Wartezeiten kürzer werden. Allerdings brauchen wir mehr Personal. Es wäre sinnvoll, wenn wir das mit dem Team besprechen.".into(), sanitario: false },
        PromptSprechen { id: "sp-b2-05".into(), livello: "B2".into(), situazione: "Raccontare un'esperienza".into(), prompt: "Racconta un'esperienza significativa e cosa hai imparato.".into(), vocabolario: vec!["Es war eine Situation, in der...".into(), "Ich habe gelernt, dass...".into(), "Rückblickend...".into(), "Das hat mich geprägt, weil...".into()], modello: "Es war eine Situation, in der ich einen schwierigen Patienten hatte. Ich habe gelernt, dass Geduld wichtig ist. Rückblickend war es eine wertvolle Erfahrung. Das hat mich geprägt, weil ich jetzt besser mit Stress umgehen kann.".into(), sanitario: false },
        PromptSprechen { id: "sp-b2-06".into(), livello: "B2".into(), situazione: "Discutere del futuro".into(), prompt: "Discuti con un collega come sarà il futuro della tua professione.".into(), vocabolario: vec!["Meiner Meinung nach wird...".into(), "Eine Herausforderung ist...".into(), "Wichtig für die Zukunft...".into(), "Ich stelle mir vor, dass...".into()], modello: "Meiner Meinung nach wird die Telemedizin eine größere Rolle spielen. Eine Herausforderung ist die technische Ausbildung. Wichtig für die Zukunft bleibt das persönliche Gespräch. Ich stelle mir vor, dass beides zusammen funktioniert.".into(), sanitario: false },
    ]
}

pub fn sprechtuebungen_per_livello(livello: &str, mostra_sanitario: bool) -> Vec<PromptSprechen> {
    let code = livello.to_uppercase();
    let code = code.split_whitespace().next().unwrap_or("A1").to_string();
    let tutti = sprechtuebungen_prompts();
    let esatti: Vec<_> = tutti.iter().filter(|p| p.livello == code).cloned().collect();
    let base = if esatti.is_empty() {
        tutti.iter().filter(|p| p.livello == "A1").cloned().collect()
    } else { esatti };
    if mostra_sanitario { base } else { base.into_iter().filter(|p| !p.sanitario).collect() }
}

// ── Esercizi di ascolto (Hörverstehen, portati da AscoltoData.kt) ──

#[derive(Debug, Clone)]
pub struct EsercizioAscolto { pub id: String, pub frase_tedesca: String, pub traduzione_italiana: String, pub domanda: String, pub opzioni: Vec<String>, pub risposta_corretta: usize, pub spiegazione: String, pub livello: String }

pub fn esercizi_ascolto() -> Vec<EsercizioAscolto> {
    vec![
        EsercizioAscolto { id: "a1_1".into(), frase_tedesca: "Ich heiße Marco und ich komme aus Italien.".into(), traduzione_italiana: "Mi chiamo Marco e vengo dall'Italia.".into(), domanda: "Da dove viene Marco?".into(), opzioni: vec!["Dall'Italia".into(), "Dalla Germania".into(), "Dall'Austria".into(), "Dalla Svizzera".into()], risposta_corretta: 0, spiegazione: "'Ich komme aus Italien' significa 'vengo dall'Italia'.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_2".into(), frase_tedesca: "Es ist drei Uhr nachmittags.".into(), traduzione_italiana: "Sono le tre del pomeriggio.".into(), domanda: "Che ore sono?".into(), opzioni: vec!["Le tre".into(), "Le due".into(), "Le quattro".into(), "Le cinque".into()], risposta_corretta: 0, spiegazione: "'Es ist drei Uhr' = 'sono le tre'.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_3".into(), frase_tedesca: "Ich habe zwei Brüder und eine Schwester.".into(), traduzione_italiana: "Ho due fratelli e una sorella.".into(), domanda: "Quanti fratelli ha il parlante?".into(), opzioni: vec!["Due".into(), "Tre".into(), "Uno".into(), "Nessuno".into()], risposta_corretta: 0, spiegazione: "'Zwei Brüder' = due fratelli.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_4".into(), frase_tedesca: "Das Wetter ist heute sehr schön.".into(), traduzione_italiana: "Il tempo è oggi molto bello.".into(), domanda: "Com'è il tempo oggi?".into(), opzioni: vec!["Bello".into(), "Brutto".into(), "Freddo".into(), "Piovoso".into()], risposta_corretta: 0, spiegazione: "'Sehr schön' = molto bello.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_5".into(), frase_tedesca: "Ich wohne in einer großen Wohnung in Berlin.".into(), traduzione_italiana: "Abito in un grande appartamento a Berlino.".into(), domanda: "Dove abita il parlante?".into(), opzioni: vec!["A Berlino".into(), "A Monaco".into(), "Amburgo".into(), "A Vienna".into()], risposta_corretta: 0, spiegazione: "'In Berlin' = a Berlino.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_6".into(), frase_tedesca: "Ich trinke morgens immer Kaffee.".into(), traduzione_italiana: "Bevo sempre il caffè al mattino.".into(), domanda: "Cosa beve il parlante al mattino?".into(), opzioni: vec!["Caffè".into(), "Tè".into(), "Succo".into(), "Latte".into()], risposta_corretta: 0, spiegazione: "'Kaffee' = caffè.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_7".into(), frase_tedesca: "Der Zug kommt um acht Uhr an.".into(), traduzione_italiana: "Il treno arriva alle otto.".into(), domanda: "A che ora arriva il treno?".into(), opzioni: vec!["Alle otto".into(), "Alle sette".into(), "Alle nove".into(), "Alle dieci".into()], risposta_corretta: 0, spiegazione: "'Um acht Uhr' = alle otto.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a1_8".into(), frase_tedesca: "Ich gehe gern ins Kino.".into(), traduzione_italiana: "Mi piace andare al cinema.".into(), domanda: "Cosa gli piace fare?".into(), opzioni: vec!["Andare al cinema".into(), "Andare al teatro".into(), "Andare al museo".into(), "Andare al ristorante".into()], risposta_corretta: 0, spiegazione: "'Ins Kino gehen' = andare al cinema.".into(), livello: "A1".into() },
        EsercizioAscolto { id: "a2_1".into(), frase_tedesca: "Entschuldigung, wo ist der Bahnhof?".into(), traduzione_italiana: "Scusi, dov'è la stazione ferroviaria?".into(), domanda: "Cosa sta cercando la persona?".into(), opzioni: vec!["La stazione".into(), "L'hotel".into(), "Il ristorante".into(), "Il supermercato".into()], risposta_corretta: 0, spiegazione: "'Der Bahnhof' = la stazione ferroviaria.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_2".into(), frase_tedesca: "Ich habe gestern einen Film gesehen, der war sehr spannend.".into(), traduzione_italiana: "Ieri ho visto un film che era molto emozionante.".into(), domanda: "Com'era il film?".into(), opzioni: vec!["Emozionante".into(), "Noioso".into(), "Triste".into(), "Divertente".into()], risposta_corretta: 0, spiegazione: "'Spannend' = emozionante.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_3".into(), frase_tedesca: "Könnten Sie mir bitte sagen, wie spät es ist?".into(), traduzione_italiana: "Può dirmi che ore sono?".into(), domanda: "Cosa chiede la persona?".into(), opzioni: vec!["L'ora".into(), "La data".into(), "Il giorno".into(), "Il tempo".into()], risposta_corretta: 0, spiegazione: "'Wie spät es ist' = che ore sono.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_4".into(), frase_tedesca: "Ich habe meinen Schlüssel verloren.".into(), traduzione_italiana: "Ho perso le mie chiavi.".into(), domanda: "Cosa ha perso il parlante?".into(), opzioni: vec!["Le chiavi".into(), "Il portafoglio".into(), "Il telefono".into(), "La borsa".into()], risposta_corretta: 0, spiegazione: "'Schlüssel' = chiavi.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_5".into(), frase_tedesca: "Wir treffen uns morgen um zwanzig Uhr.".into(), traduzione_italiana: "Incontriamo domani alle venti.".into(), domanda: "A che ora si incontrano?".into(), opzioni: vec!["Alle venti".into(), "Alle otto".into(), "Alle nove".into(), "Alle dieci".into()], risposta_corretta: 0, spiegazione: "'Um zwanzig Uhr' = alle venti (8 di sera).".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_6".into(), frase_tedesca: "Ich habe gestern Abend Pizza gegessen.".into(), traduzione_italiana: "Ieri sera ho mangiato la pizza.".into(), domanda: "Cosa ha mangiato ieri sera?".into(), opzioni: vec!["Pizza".into(), "Pasta".into(), "Insalata".into(), "Zuppa".into()], risposta_corretta: 0, spiegazione: "'Pizza' = pizza.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_7".into(), frase_tedesca: "Der Film beginnt erst in zehn Minuten.".into(), traduzione_italiana: "Il film inizia tra dieci minuti.".into(), domanda: "Quando inizia il film?".into(), opzioni: vec!["Tra dieci minuti".into(), "Adesso".into(), "Tra un'ora".into(), "Ieri".into()], risposta_corretta: 0, spiegazione: "'In zehn Minuten' = tra dieci minuti.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "a2_8".into(), frase_tedesca: "Ich habe keine Zeit, ich muss arbeiten.".into(), traduzione_italiana: "Non ho tempo, devo lavorare.".into(), domanda: "Perché non ha tempo?".into(), opzioni: vec!["Deve lavorare".into(), "Deve studiare".into(), "Deve dormire".into(), "Deve mangiare".into()], risposta_corretta: 0, spiegazione: "'Ich muss arbeiten' = devo lavorare.".into(), livello: "A2".into() },
        EsercizioAscolto { id: "b1_1".into(), frase_tedesca: "Ich habe gehört, dass du nächste Woche nach Deutschland reist. Wann fährst du ab?".into(), traduzione_italiana: "Ho sentito che la prossima settimana vai in Germania. Quando parti?".into(), domanda: "Quando parte per la Germania?".into(), opzioni: vec!["La prossima settimana".into(), "Questo mese".into(), "Il mese prossimo".into(), "Domani".into()], risposta_corretta: 0, spiegazione: "'Nächste Woche' = la prossima settimana.".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_2".into(), frase_tedesca: "Leider kann ich nicht kommen, ich habe schon andere Pläne.".into(), traduzione_italiana: "Purtroppo non posso venire, ho già altri programmi.".into(), domanda: "Perché non può venire?".into(), opzioni: vec!["Ha altri programmi".into(), "È malato".into(), "Non vuole".into(), "Non ha tempo".into()], risposta_corretta: 0, spiegazione: "'Ich habe schon andere Pläne' = ho già altri programmi.".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_3".into(), frase_tedesca: "Das Konzert war eigentlich schon ausverkauft, aber ich habe noch ein Ticket bekommen.".into(), traduzione_italiana: "Il concerto era in realtà già esaurito, ma ho ricevuto ancora un biglietto.".into(), domanda: "Come ha ottenuto il biglietto?".into(), opzioni: vec!["Qualcuno gli ha dato un biglietto".into(), "Lo ha comprato online".into(), "Lo ha vinto".into(), "Lo ha trovato".into()], risposta_corretta: 0, spiegazione: "'Ich habe noch ein Ticket bekommen' = ho ricevuto ancora un biglietto.".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_4".into(), frase_tedesca: "Ich bin gestern spät nach Hause gekommen, weil ich noch arbeiten musste.".into(), traduzione_italiana: "Ieri sono tornato a casa tardi perché dovevo ancora lavorare.".into(), domanda: "Perché è tornato tardi?".into(), opzioni: vec!["Doveva lavorare".into(), "Era in ritardo".into(), "Ha perso il treno".into(), "Era al cinema".into()], risposta_corretta: 0, spiegazione: "'Weil ich noch arbeiten musste' = perché dovevo ancora lavorare.".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_5".into(), frase_tedesca: "Hast du schon von dem neuen Restaurant gehört? Es soll sehr gut sein.".into(), traduzione_italiana: "Hai sentito parlare del nuovo ristorante? Dicono che sia molto buono.".into(), domanda: "Cosa si dice del nuovo ristorante?".into(), opzioni: vec!["È molto buono".into(), "È molto costoso".into(), "È molto lontano".into(), "È chiuso".into()], risposta_corretta: 0, spiegazione: "'Es soll sehr gut sein' = è molto buono (si dice).".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_6".into(), frase_tedesca: "Ich habe mein Handy verloren, kannst du mich bitte anrufen?".into(), traduzione_italiana: "Ho perso il telefono, puoi chiamarmi per favore?".into(), domanda: "Cosa chiede di fare?".into(), opzioni: vec!["Chiamarlo".into(), "Mandargli un messaggio".into(), "Aspettarlo".into(), "Cercarlo".into()], risposta_corretta: 0, spiegazione: "'Kannst du mich bitte anrufen?' = puoi chiamarmi per favore?".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_7".into(), frase_tedesca: "Der Zug hat zwanzig Minuten Verspätung, wir müssen noch warten.".into(), traduzione_italiana: "Il treno ha venti minuti di ritardo, dobbiamo ancora aspettare.".into(), domanda: "Quanto ritardo ha il treno?".into(), opzioni: vec!["Venti minuti".into(), "Dieci minuti".into(), "Trenta minuti".into(), "Un'ora".into()], risposta_corretta: 0, spiegazione: "'Zwanzig Minuten Verspätung' = venti minuti di ritardo.".into(), livello: "B1".into() },
        EsercizioAscolto { id: "b1_8".into(), frase_tedesca: "Ich habe gestern ein Buch gekauft, das sehr interessant ist.".into(), traduzione_italiana: "Ieri ho comprato un libro che è molto interessante.".into(), domanda: "Cosa ha comprato ieri?".into(), opzioni: vec!["Un libro".into(), "Un film".into(), "Una rivista".into(), "Un giornale".into()], risposta_corretta: 0, spiegazione: "'Ein Buch' = un libro.".into(), livello: "B1".into() },
        EsercizioAscolto { id: "a0_1".into(), frase_tedesca: "Ich heiße Sophie.".into(), traduzione_italiana: "Mi chiamo Sophie.".into(), domanda: "Come si chiama il parlante?".into(), opzioni: vec!["Sophie".into(), "Anna".into(), "Julia".into(), "Lea".into()], risposta_corretta: 0, spiegazione: "Il parlante dice 'Ich heiße Sophie' (Mi chiamo Sophie)".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_2".into(), frase_tedesca: "Ich komme aus Italien.".into(), traduzione_italiana: "Vengo dall'Italia.".into(), domanda: "Da dove viene il parlante?".into(), opzioni: vec!["Germania".into(), "Italia".into(), "Austria".into(), "Svizzera".into()], risposta_corretta: 1, spiegazione: "Il parlante dice 'Ich komme aus Italien' (Vengo dall'Italia)".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_3".into(), frase_tedesca: "Ich bin Studentin.".into(), traduzione_italiana: "Sono studentessa.".into(), domanda: "Qual è il lavoro/studio del parlante?".into(), opzioni: vec!["Studentessa".into(), "Insegnante".into(), "Medica".into(), "Avvocata".into()], risposta_corretta: 0, spiegazione: "Il parlante dice 'Ich bin Studentin' (Sono studentessa)".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_4".into(), frase_tedesca: "Eins, zwei, drei, vier, fünf.".into(), traduzione_italiana: "Uno, due, tre, quattro, cinque.".into(), domanda: "Quanti numeri sono stati detti?".into(), opzioni: vec!["3".into(), "5".into(), "7".into(), "9".into()], risposta_corretta: 1, spiegazione: "Sono stati detti 5 numeri: eins, zwei, drei, vier, fünf".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_5".into(), frase_tedesca: "Das ist meine Mutter.".into(), traduzione_italiana: "Questa è mia madre.".into(), domanda: "Chi è 'die Mutter'?".into(), opzioni: vec!["la madre".into(), "il padre".into(), "la sorella".into(), "il fratello".into()], risposta_corretta: 0, spiegazione: "'Die Mutter' significa 'la madre'".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_6".into(), frase_tedesca: "Ich wohne in Berlin.".into(), traduzione_italiana: "Vivo a Berlino.".into(), domanda: "Dove vive il parlante?".into(), opzioni: vec!["Monaco".into(), "Berlino".into(), "Amburgo".into(), "Vienna".into()], risposta_corretta: 0, spiegazione: "Il parlante dice 'Ich wohne in Berlin' (Vivo a Berlino)".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_7".into(), frase_tedesca: "Ich hätte gern ein Wasser, bitte.".into(), traduzione_italiana: "Vorrei un'acqua, per favore.".into(), domanda: "Cosa vuole il parlante?".into(), opzioni: vec!["caffè".into(), "vino".into(), "acqua".into(), "succo".into()], risposta_corretta: 2, spiegazione: "Il parlante dice 'ein Wasser' (un'acqua)".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_8".into(), frase_tedesca: "Guten Morgen!".into(), traduzione_italiana: "Buongiorno!".into(), domanda: "Cosa dice il parlante?".into(), opzioni: vec!["Buonanotte".into(), "Buongiorno".into(), "Arrivederci".into(), "Buonasera".into()], risposta_corretta: 1, spiegazione: "'Guten Morgen' significa 'Buongiorno'".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_9".into(), frase_tedesca: "Danke schön!".into(), traduzione_italiana: "Grazie mille!".into(), domanda: "Cosa dice il parlante?".into(), opzioni: vec!["Prego".into(), "Grazie".into(), "Scusa".into(), "Per favore".into()], risposta_corretta: 1, spiegazione: "'Danke schön' significa 'Grazie mille'".into(), livello: "A0".into() },
        EsercizioAscolto { id: "a0_10".into(), frase_tedesca: "Ich lerne Deutsch.".into(), traduzione_italiana: "Imparo tedesco.".into(), domanda: "Cosa fa il parlante?".into(), opzioni: vec!["lavora".into(), "impara tedesco".into(), "dorme".into(), "mangia".into()], risposta_corretta: 1, spiegazione: "Il parlante dice 'Ich lerne Deutsch' (Imparo tedesco)".into(), livello: "A0".into() },
    ]
}

pub fn esercizi_ascolto_per_livello(livello: &str) -> Vec<EsercizioAscolto> {
    esercizi_ascolto().into_iter().filter(|e| e.livello == livello).collect()
}

// ── Domande di comprensione (Lesen, portate da QuizData.kt) ──

#[derive(Debug, Clone)]
pub struct DomandaComprensione { pub id: String, pub domanda: String, pub opzioni: Vec<String>, pub risposta_corretta: usize, pub spiegazione: String, pub livello: String }

pub fn domande_comprensione() -> Vec<DomandaComprensione> {
    vec![
        DomandaComprensione { id: "comp_1".into(), domanda: "Was bedeutet 'der Tisch'?".into(), opzioni: vec!["il tavolo".into(), "la sedia".into(), "la porta".into(), "la finestra".into()], risposta_corretta: 0, spiegazione: "'Der Tisch' significa 'il tavolo'.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_2".into(), domanda: "Was bedeutet 'die Küche'?".into(), opzioni: vec!["il bagno".into(), "la cucina".into(), "la camera".into(), "il soggiorno".into()], risposta_corretta: 1, spiegazione: "'Die Küche' significa 'la cucina'.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_3".into(), domanda: "Quale parola tedesca significa 'lavorare'?".into(), opzioni: vec!["wohnen".into(), "arbeiten".into(), "lernen".into(), "spielen".into()], risposta_corretta: 1, spiegazione: "'Arbeiten' significa 'lavorare'. 'Wohnen' = abitare, 'lernen' = imparare, 'spielen' = giocare.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_4".into(), domanda: "Come si dice 'la settimana' in tedesco?".into(), opzioni: vec!["der Tag".into(), "die Woche".into(), "der Monat".into(), "das Jahr".into()], risposta_corretta: 1, spiegazione: "'Die Woche' significa 'la settimana'. 'Der Tag' = il giorno, 'der Monat' = il mese, 'das Jahr' = l'anno.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_5".into(), domanda: "Was bedeutet 'Ich habe zwei Brüder.'?".into(), opzioni: vec!["Io ho due fratelli.".into(), "Io ho due sorelle.".into(), "Io vado dal fratello.".into(), "Io amo i miei fratelli.".into()], risposta_corretta: 0, spiegazione: "'Zwei Brüder' = due fratelli. 'Schwestern' = sorelle.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_6".into(), domanda: "Quale parola tedesca significa 'mangiare'?".into(), opzioni: vec!["trinken".into(), "essen".into(), "schlafen".into(), "lesen".into()], risposta_corretta: 1, spiegazione: "'Essen' significa 'mangiare'. 'Trinken' = bere, 'schlafen' = dormire, 'lesen' = leggere.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_7".into(), domanda: "Was bedeutet 'Das Wetter ist schön.'?".into(), opzioni: vec!["Il tempo è bello.".into(), "Il tempo è brutto.".into(), "Fa freddo.".into(), "Fa caldo.".into()], risposta_corretta: 0, spiegazione: "'Schön' = bello. 'Das Wetter' = il tempo (meteo).".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_8".into(), domanda: "Come si dice 'domani' in tedesco?".into(), opzioni: vec!["gestern".into(), "heute".into(), "morgen".into(), "jetzt".into()], risposta_corretta: 2, spiegazione: "'Morgen' = domani. 'Gestern' = ieri, 'heute' = oggi, 'jetzt' = adesso.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_9".into(), domanda: "Was bedeutet 'Ich wohne in Berlin.'?".into(), opzioni: vec!["Abito a Berlino.".into(), "Lavoro a Berlino.".into(), "Vado a Berlino.".into(), "Amo Berlino.".into()], risposta_corretta: 0, spiegazione: "'Wohnen' = abitare. 'In Berlin' = a Berlino.".into(), livello: "A1".into() },
        DomandaComprensione { id: "comp_10".into(), domanda: "Quale parola tedesca significa 'parlare'?".into(), opzioni: vec!["sagen".into(), "sprechen".into(), "hören".into(), "sehen".into()], risposta_corretta: 1, spiegazione: "'Sprechen' = parlare. 'Sagen' = dire, 'hören' = sentire, 'sehen' = vedere.".into(), livello: "A1".into() },
    ]
}

// ── Domande di produzione (Schreiben, portate da QuizData.kt) ──

pub fn domande_produzione() -> Vec<DomandaComprensione> {
    vec![
        DomandaComprensione { id: "prod_1".into(), domanda: "Ich ___ gern Kaffee trinken.".into(), opzioni: vec!["würde".into(), "werde".into(), "habe".into(), "bin".into()], risposta_corretta: 0, spiegazione: "Konjunktiv II per desideri irreali: 'würde' + infinito.".into(), livello: "B1".into() },
        DomandaComprensione { id: "prod_2".into(), domanda: "Ich ___ gestern ins Kino gegangen.".into(), opzioni: vec!["bin".into(), "habe".into(), "war".into(), "hatte".into()], risposta_corretta: 0, spiegazione: "Perfekt con 'sein' per verbi di movimento: 'ich bin gegangen'.".into(), livello: "A1".into() },
        DomandaComprensione { id: "prod_3".into(), domanda: "Er ___ die Hausaufgaben gemacht.".into(), opzioni: vec!["hat".into(), "ist".into(), "war".into(), "wird".into()], risposta_corretta: 0, spiegazione: "Perfekt con 'haben' per la maggior parte dei verbi: 'er hat gemacht'.".into(), livello: "A1".into() },
        DomandaComprensione { id: "prod_4".into(), domanda: "Wir ___ nächste Woche nach Deutschland reisen.".into(), opzioni: vec!["werden".into(), "sind".into(), "haben".into(), "waren".into()], risposta_corretta: 0, spiegazione: "Futur con 'werden': 'wir werden reisen'.".into(), livello: "A2".into() },
        DomandaComprensione { id: "prod_5".into(), domanda: "Ich freue mich ___ deinen Besuch.".into(), opzioni: vec!["auf".into(), "über".into(), "für".into(), "mit".into()], risposta_corretta: 0, spiegazione: "'Sich freuen auf' = attendere con gioia (futuro). 'Sich freuen über' = essere felice di (passato).".into(), livello: "A2".into() },
        DomandaComprensione { id: "prod_6".into(), domanda: "Er hat mir gesagt, dass er ___ kommt.".into(), opzioni: vec!["nicht".into(), "kein".into(), "niemals".into(), "nichts".into()], risposta_corretta: 0, spiegazione: "'Dass' introduce una frase subordinata: 'dass er nicht viene'.".into(), livello: "B1".into() },
        DomandaComprensione { id: "prod_7".into(), domanda: "Ich habe ein Buch ___.".into(), opzioni: vec!["gelesen".into(), "lesen".into(), "las".into(), "liest".into()], risposta_corretta: 0, spiegazione: "Perfekt: Partizip II 'gelesen' con 'haben'.".into(), livello: "A1".into() },
        DomandaComprensione { id: "prod_8".into(), domanda: "Sie ___ seit drei Jahren in Deutschland.".into(), opzioni: vec!["wohnt".into(), "wohnte".into(), "wohnen".into(), "gewohnt".into()], risposta_corretta: 0, spiegazione: "Presente con 'seit' + periodo: 'sie wohnt seit...'.".into(), livello: "A2".into() },
        DomandaComprensione { id: "prod_9".into(), domanda: "Ich ___ dir helfen, wenn du willst.".into(), opzioni: vec!["kann".into(), "konnte".into(), "könnte".into(), "können".into()], risposta_corretta: 0, spiegazione: "Presente indicativo: 'ich kann' (possibilità attuale).".into(), livello: "A1".into() },
        DomandaComprensione { id: "prod_10".into(), domanda: "Er hat mir ___ erzählt.".into(), opzioni: vec!["alles".into(), "jedes".into(), "vieles".into(), "manches".into()], risposta_corretta: 0, spiegazione: "'Alles' = tutto (neutro). 'Er hat mir alles erzählt' = mi ha raccontato tutto.".into(), livello: "A2".into() },
    ]
}

// ── Wortliste B1 curata (portata da WortlisteB1.kt) ──

#[derive(Debug, Clone)]
pub struct VoceWortliste { pub tedesco: String, pub italiano: String, pub esempio: String, pub tema: String, pub livello: String, pub articolo: Option<String> }

pub fn wortliste_voci() -> Vec<VoceWortliste> {
    vec![
        VoceWortliste { tedesco: "der Termin".into(), italiano: "appuntamento".into(), esempio: "Ich habe morgen einen Termin.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Bewerbung".into(), italiano: "domanda di lavoro".into(), esempio: "Ich schreibe eine Bewerbung.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Lebenslauf".into(), italiano: "curriculum".into(), esempio: "Mein Lebenslauf ist aktuell.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Vorstellungsgespräch".into(), italiano: "colloquio di lavoro".into(), esempio: "Das Vorstellungsgespräch ist am Montag.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Stelle".into(), italiano: "posto di lavoro".into(), esempio: "Ich suche eine Stelle in Brixen.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Arbeitgeber".into(), italiano: "datore di lavoro".into(), esempio: "Mein Arbeitgeber ist eine Klinik.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Arbeitnehmer".into(), italiano: "dipendente".into(), esempio: "Der Arbeitnehmer hat Rechte.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Ausbildung".into(), italiano: "formazione".into(), esempio: "Meine Ausbildung dauert drei Jahre.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Weiterbildung".into(), italiano: "formazione continua".into(), esempio: "Ich mache eine Weiterbildung.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Erfahrung".into(), italiano: "esperienza".into(), esempio: "Ich habe zwei Jahre Erfahrung.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Verantwortung".into(), italiano: "responsabilità".into(), esempio: "Ich übernehme Verantwortung.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Zusammenarbeit".into(), italiano: "collaborazione".into(), esempio: "Die Zusammenarbeit im Team ist gut.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Vertrag".into(), italiano: "contratto".into(), esempio: "Ich unterschreibe den Vertrag.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Kündigung".into(), italiano: "licenziamento".into(), esempio: "Die Kündigung muss schriftlich sein.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Gehalt".into(), italiano: "stipendio".into(), esempio: "Das Gehalt ist fair.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Arbeitszeit".into(), italiano: "orario di lavoro".into(), esempio: "Die Arbeitszeit ist flexibel.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Feierabend".into(), italiano: "fine giornata lavorativa".into(), esempio: "Endlich Feierabend!".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Überstunde".into(), italiano: "ora straordinaria".into(), esempio: "Ich mache heute eine Überstunde.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Kollege".into(), italiano: "collega".into(), esempio: "Mein Kollege hilft mir.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Kollegin".into(), italiano: "collega (f.)".into(), esempio: "Meine Kollegin ist krank.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Gesundheit".into(), italiano: "salute".into(), esempio: "Gesundheit ist wichtig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Krankheit".into(), italiano: "malattia".into(), esempio: "Die Krankheit dauert eine Woche.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Schmerz".into(), italiano: "dolore".into(), esempio: "Der Schmerz ist stark.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Behandlung".into(), italiano: "trattamento".into(), esempio: "Die Behandlung beginnt morgen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Befund".into(), italiano: "referto".into(), esempio: "Der Befund ist da.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Überweisung".into(), italiano: "impegnativa".into(), esempio: "Ich brauche eine Überweisung.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Krankenkasse".into(), italiano: "cassa mutua".into(), esempio: "Die Krankenkasse zahlt die Therapie.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Ordination".into(), italiano: "studio medico".into(), esempio: "Die Ordination ist im Zentrum.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Rezept".into(), italiano: "ricetta".into(), esempio: "Der Arzt schreibt ein Rezept.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Physiotherapie".into(), italiano: "fisioterapia".into(), esempio: "Ich arbeite in der Physiotherapie.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Bewegung".into(), italiano: "movimento".into(), esempio: "Bewegung hilft gegen Schmerzen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Übung".into(), italiano: "esercizio".into(), esempio: "Diese Übung machen Sie täglich.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Patient".into(), italiano: "paziente".into(), esempio: "Der Patient kommt um neun.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Patientin".into(), italiano: "paziente (f.)".into(), esempio: "Die Patientin hat Knieschmerzen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Anamnese".into(), italiano: "anamnesi".into(), esempio: "Die Anamnese dauert zehn Minuten.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Diagnose".into(), italiano: "diagnosi".into(), esempio: "Die Diagnose ist klar.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Therapieplan".into(), italiano: "piano terapeutico".into(), esempio: "Wir erstellen einen Therapieplan.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Kontrolle".into(), italiano: "controllo".into(), esempio: "Die Kontrolle ist nächste Woche.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Entlassung".into(), italiano: "dimissione".into(), esempio: "Die Entlassung ist am Freitag.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Zuweisung".into(), italiano: "invio".into(), esempio: "Ich habe eine Zuweisung vom Arzt.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Gesellschaft".into(), italiano: "società".into(), esempio: "Die Gesellschaft verändert sich.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Politik".into(), italiano: "politica".into(), esempio: "Politik interessiert mich.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Wahl".into(), italiano: "elezione".into(), esempio: "Die Wahl ist im Herbst.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Demokratie".into(), italiano: "democrazia".into(), esempio: "Demokratie braucht Beteiligung.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Meinung".into(), italiano: "opinione".into(), esempio: "Meine Meinung ist anders.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Diskussion".into(), italiano: "discussione".into(), esempio: "Die Diskussion war lang.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Vorteil".into(), italiano: "vantaggio".into(), esempio: "Der Vorteil ist klar.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Nachteil".into(), italiano: "svantaggio".into(), esempio: "Der Nachteil ist der Preis.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Lösung".into(), italiano: "soluzione".into(), esempio: "Wir brauchen eine Lösung.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Problem".into(), italiano: "problema".into(), esempio: "Das Problem ist komplex.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Regel".into(), italiano: "regola".into(), esempio: "Diese Regel gilt für alle.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Freiheit".into(), italiano: "libertà".into(), esempio: "Freiheit ist wichtig.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Gleichberechtigung".into(), italiano: "parità di diritti".into(), esempio: "Gleichberechtigung ist ein Ziel.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Integration".into(), italiano: "integrazione".into(), esempio: "Integration braucht Zeit.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Migration".into(), italiano: "migrazione".into(), esempio: "Migration prägt die Gesellschaft.".into(), tema: "Società".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Umwelt".into(), italiano: "ambiente".into(), esempio: "Wir müssen die Umwelt schützen.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Klimawandel".into(), italiano: "cambiamento climatico".into(), esempio: "Der Klimawandel ist real.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Energie".into(), italiano: "energia".into(), esempio: "Wir brauchen saubere Energie.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Müll".into(), italiano: "rifiuti".into(), esempio: "Der Müll wird getrennt.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Recycling".into(), italiano: "riciclo".into(), esempio: "Recycling spart Ressourcen.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Verschmutzung".into(), italiano: "inquinamento".into(), esempio: "Die Verschmutzung nimmt zu.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Verkehr".into(), italiano: "traffico".into(), esempio: "Der Verkehr ist laut.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Nachhaltigkeit".into(), italiano: "sostenibilità".into(), esempio: "Nachhaltigkeit ist ein Ziel.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Ressource".into(), italiano: "risorsa".into(), esempio: "Wasser ist eine Ressource.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Naturschutz".into(), italiano: "tutela della natura".into(), esempio: "Naturschutz beginnt vor Ort.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Sonnenenergie".into(), italiano: "energia solare".into(), esempio: "Sonnenenergie ist kostenlos.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Windrad".into(), italiano: "pala eolica".into(), esempio: "Das Windrad steht auf dem Hügel.".into(), tema: "Ambiente".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Technologie".into(), italiano: "tecnologia".into(), esempio: "Technologie entwickelt sich schnell.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Digitalisierung".into(), italiano: "digitalizzazione".into(), esempio: "Die Digitalisierung verändert die Arbeit.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Internet".into(), italiano: "internet".into(), esempio: "Das Internet ist langsam.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Daten".into(), italiano: "dati".into(), esempio: "Die Daten sind sicher.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Sicherheit".into(), italiano: "sicurezza".into(), esempio: "Sicherheit ist wichtig.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Künstliche Intelligenz".into(), italiano: "intelligenza artificiale".into(), esempio: "Künstliche Intelligenz hilft bei Texten.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Gerät".into(), italiano: "dispositivo".into(), esempio: "Das Gerät ist neu.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Anwendung".into(), italiano: "applicazione".into(), esempio: "Die Anwendung ist einfach.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Entwicklung".into(), italiano: "sviluppo".into(), esempio: "Die Entwicklung dauert Jahre.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Forschung".into(), italiano: "ricerca".into(), esempio: "Die Forschung braucht Geld.".into(), tema: "Tecnologia".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Kultur".into(), italiano: "cultura".into(), esempio: "Kultur verbindet Menschen.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Tradition".into(), italiano: "tradizione".into(), esempio: "Die Tradition ist alt.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Veranstaltung".into(), italiano: "evento".into(), esempio: "Die Veranstaltung ist heute.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Ausstellung".into(), italiano: "mostra".into(), esempio: "Die Ausstellung ist interessant.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Museum".into(), italiano: "museo".into(), esempio: "Das Museum ist geschlossen.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Literatur".into(), italiano: "letteratura".into(), esempio: "Ich lese gern Literatur.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Musik".into(), italiano: "musica".into(), esempio: "Musik hilft beim Lernen.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Theater".into(), italiano: "teatro".into(), esempio: "Wir gehen ins Theater.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Brauch".into(), italiano: "usanza".into(), esempio: "Der Brauch ist typisch für Südtirol.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Geschichte".into(), italiano: "storia".into(), esempio: "Die Geschichte ist spannend.".into(), tema: "Cultura".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Emotion".into(), italiano: "emozione".into(), esempio: "Emotionen sind wichtig.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Freude".into(), italiano: "gioia".into(), esempio: "Die Freude war groß.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Angst".into(), italiano: "paura".into(), esempio: "Die Angst vor Fehlern ist normal.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Sorge".into(), italiano: "preoccupazione".into(), esempio: "Ich habe Sorgen.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Hoffnung".into(), italiano: "speranza".into(), esempio: "Die Hoffnung bleibt.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Enttäuschung".into(), italiano: "delusione".into(), esempio: "Die Enttäuschung war groß.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Wut".into(), italiano: "rabbia".into(), esempio: "Die Wut hilft nicht.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Geduld".into(), italiano: "pazienza".into(), esempio: "Geduld ist wichtig beim Lernen.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Vertrauen".into(), italiano: "fiducia".into(), esempio: "Vertrauen muss man aufbauen.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Motivation".into(), italiano: "motivazione".into(), esempio: "Meine Motivation ist hoch.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Stress".into(), italiano: "stress".into(), esempio: "Stress macht krank.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Entspannung".into(), italiano: "rilassamento".into(), esempio: "Entspannung ist wichtig.".into(), tema: "Emozioni".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Reise".into(), italiano: "viaggio".into(), esempio: "Die Reise war schön.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Unterkunft".into(), italiano: "alloggio".into(), esempio: "Die Unterkunft ist günstig.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Ankunft".into(), italiano: "arrivo".into(), esempio: "Die Ankunft ist um acht.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Abfahrt".into(), italiano: "partenza".into(), esempio: "Die Abfahrt ist morgen.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Aufenthalt".into(), italiano: "soggiorno".into(), esempio: "Der Aufenthalt dauert drei Tage.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Sehenswürdigkeit".into(), italiano: "attrazione".into(), esempio: "Die Sehenswürdigkeit ist berühmt.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Fahrkarte".into(), italiano: "biglietto".into(), esempio: "Ich kaufe eine Fahrkarte.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Anschluss".into(), italiano: "coincidenza".into(), esempio: "Der Anschluss ist knapp.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Verspätung".into(), italiano: "ritardo".into(), esempio: "Der Zug hat Verspätung.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Gepäck".into(), italiano: "bagaglio".into(), esempio: "Mein Gepäck ist schwer.".into(), tema: "Viaggio".into(), livello: "B1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Familie".into(), italiano: "famiglia".into(), esempio: "Meine Familie wohnt in Brixen.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Mutter".into(), italiano: "madre".into(), esempio: "Meine Mutter kocht gern.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Vater".into(), italiano: "padre".into(), esempio: "Mein Vater arbeitet im Garten.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Eltern".into(), italiano: "genitori".into(), esempio: "Meine Eltern sind zu Hause.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Bruder".into(), italiano: "fratello".into(), esempio: "Mein Bruder lernt Deutsch.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Schwester".into(), italiano: "sorella".into(), esempio: "Meine Schwester wohnt in Bozen.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Kind".into(), italiano: "bambino".into(), esempio: "Das Kind spielt im Hof.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Sohn".into(), italiano: "figlio".into(), esempio: "Ihr Sohn geht zur Schule.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Tochter".into(), italiano: "figlia".into(), esempio: "Die Tochter hilft in der Küche.".into(), tema: "Famiglia".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Freund".into(), italiano: "amico".into(), esempio: "Mein Freund kommt aus Deutschland.".into(), tema: "Relazioni".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Freundin".into(), italiano: "amica".into(), esempio: "Meine Freundin spricht Italienisch.".into(), tema: "Relazioni".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Haus".into(), italiano: "casa".into(), esempio: "Das Haus ist alt.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Wohnung".into(), italiano: "appartamento".into(), esempio: "Die Wohnung ist klein.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Zimmer".into(), italiano: "stanza".into(), esempio: "Mein Zimmer ist hell.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Küche".into(), italiano: "cucina".into(), esempio: "Die Küche ist modern.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Bad".into(), italiano: "bagno".into(), esempio: "Das Bad ist neben dem Schlafzimmer.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Tisch".into(), italiano: "tavolo".into(), esempio: "Das Buch liegt auf dem Tisch.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Stuhl".into(), italiano: "sedia".into(), esempio: "Der Stuhl ist bequem.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Bett".into(), italiano: "letto".into(), esempio: "Das Bett ist groß.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Tür".into(), italiano: "porta".into(), esempio: "Die Tür ist offen.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Fenster".into(), italiano: "finestra".into(), esempio: "Das Fenster ist geschlossen.".into(), tema: "Casa".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "das Wasser".into(), italiano: "acqua".into(), esempio: "Ich trinke viel Wasser.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "das Brot".into(), italiano: "pane".into(), esempio: "Das Brot ist frisch.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Apfel".into(), italiano: "mela".into(), esempio: "Der Apfel ist rot.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Milch".into(), italiano: "latte".into(), esempio: "Die Milch ist kalt.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Käse".into(), italiano: "formaggio".into(), esempio: "Der Käse kommt aus Südtirol.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Fleisch".into(), italiano: "carne".into(), esempio: "Ich esse kein Fleisch.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "das Gemüse".into(), italiano: "verdura".into(), esempio: "Das Gemüse ist frisch.".into(), tema: "Alimentazione".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Stadt".into(), italiano: "città".into(), esempio: "Die Stadt ist klein.".into(), tema: "Città".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Dorf".into(), italiano: "paese".into(), esempio: "Das Dorf liegt im Tal.".into(), tema: "Città".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Straße".into(), italiano: "strada".into(), esempio: "Die Straße ist ruhig.".into(), tema: "Città".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Platz".into(), italiano: "piazza".into(), esempio: "Der Platz ist im Zentrum.".into(), tema: "Città".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Bahnhof".into(), italiano: "stazione".into(), esempio: "Der Bahnhof ist nah.".into(), tema: "Città".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Arzt".into(), italiano: "medico".into(), esempio: "Der Arzt ist heute da.".into(), tema: "Salute".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Ärztin".into(), italiano: "medica".into(), esempio: "Die Ärztin arbeitet im Krankenhaus.".into(), tema: "Salute".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Krankenhaus".into(), italiano: "ospedale".into(), esempio: "Das Krankenhaus ist groß.".into(), tema: "Salute".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Kopf".into(), italiano: "testa".into(), esempio: "Mein Kopf tut weh.".into(), tema: "Corpo".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Arm".into(), italiano: "braccio".into(), esempio: "Mein Arm ist müde.".into(), tema: "Corpo".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Bein".into(), italiano: "gamba".into(), esempio: "Das Bein tut weh.".into(), tema: "Corpo".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Bauch".into(), italiano: "pancia".into(), esempio: "Mein Bauch ist voll.".into(), tema: "Corpo".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Hand".into(), italiano: "mano".into(), esempio: "Wasch dir die Hände.".into(), tema: "Corpo".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Fuß".into(), italiano: "piede".into(), esempio: "Mein Fuß ist kalt.".into(), tema: "Corpo".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Schule".into(), italiano: "scuola".into(), esempio: "Die Schule beginnt um acht.".into(), tema: "Istruzione".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Lehrer".into(), italiano: "insegnante".into(), esempio: "Der Lehrer erklärt die Regel.".into(), tema: "Istruzione".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Lehrerin".into(), italiano: "insegnante (f.)".into(), esempio: "Die Lehrerin ist freundlich.".into(), tema: "Istruzione".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Kurs".into(), italiano: "corso".into(), esempio: "Der Kurs dauert drei Monate.".into(), tema: "Istruzione".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Buch".into(), italiano: "libro".into(), esempio: "Das Buch ist interessant.".into(), tema: "Istruzione".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Tag".into(), italiano: "giorno".into(), esempio: "Der Tag war lang.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Woche".into(), italiano: "settimana".into(), esempio: "Die Woche beginnt am Montag.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Monat".into(), italiano: "mese".into(), esempio: "Der Monat ist fast vorbei.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Jahr".into(), italiano: "anno".into(), esempio: "Das Jahr hat zwölf Monate.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "heute".into(), italiano: "oggi".into(), esempio: "Heute lerne ich Deutsch.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "morgen".into(), italiano: "domani".into(), esempio: "Morgen habe ich einen Termin.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "gestern".into(), italiano: "ieri".into(), esempio: "Gestern war ich in Bozen.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "jetzt".into(), italiano: "adesso".into(), esempio: "Jetzt trinke ich Kaffee.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "immer".into(), italiano: "sempre".into(), esempio: "Ich bin immer pünktlich.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "oft".into(), italiano: "spesso".into(), esempio: "Ich gehe oft spazieren.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "manchmal".into(), italiano: "a volte".into(), esempio: "Manchmal koche ich allein.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "nie".into(), italiano: "mai".into(), esempio: "Ich bin nie müde.".into(), tema: "Tempo".into(), livello: "A1".into(), articolo: None },
        VoceWortliste { tedesco: "die Anmeldung".into(), italiano: "registrazione".into(), esempio: "Die Anmeldung ist online.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Formular".into(), italiano: "modulo".into(), esempio: "Bitte füllen Sie das Formular aus.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "der Ausweis".into(), italiano: "documento d'identità".into(), esempio: "Zeigen Sie bitte Ihren Ausweis.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Unterschrift".into(), italiano: "firma".into(), esempio: "Hier fehlt die Unterschrift.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Antrag".into(), italiano: "richiesta".into(), esempio: "Ich stelle einen Antrag.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Frist".into(), italiano: "termine".into(), esempio: "Die Frist endet am Freitag.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Bestätigung".into(), italiano: "conferma".into(), esempio: "Ich warte auf die Bestätigung.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Öffnungszeiten".into(), italiano: "orari di apertura".into(), esempio: "Die Öffnungszeiten sind im Internet.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Schalter".into(), italiano: "sportello".into(), esempio: "Bitte gehen Sie zum Schalter drei.".into(), tema: "Burocrazia".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Steuer".into(), italiano: "tassa".into(), esempio: "Die Steuer ist dieses Jahr höher.".into(), tema: "Denaro".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Rechnung".into(), italiano: "fattura".into(), esempio: "Die Rechnung ist schon bezahlt.".into(), tema: "Denaro".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Konto".into(), italiano: "conto".into(), esempio: "Mein Konto ist leer.".into(), tema: "Denaro".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Versicherung".into(), italiano: "assicurazione".into(), esempio: "Die Versicherung zahlt den Schaden.".into(), tema: "Denaro".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Lohn".into(), italiano: "salario".into(), esempio: "Der Lohn kommt am Monatsende.".into(), tema: "Lavoro".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Pause".into(), italiano: "pausa".into(), esempio: "Wir machen eine kurze Pause.".into(), tema: "Lavoro".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Schicht".into(), italiano: "turno".into(), esempio: "Ich arbeite in der Frühschicht.".into(), tema: "Lavoro".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Urlaub".into(), italiano: "ferie".into(), esempio: "Ich nehme im August Urlaub.".into(), tema: "Lavoro".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Krankmeldung".into(), italiano: "comunicazione di malattia".into(), esempio: "Die Krankmeldung muss heute kommen.".into(), tema: "Lavoro".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Apotheke".into(), italiano: "farmacia".into(), esempio: "Die Apotheke ist nebenan.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Medikament".into(), italiano: "medicinale".into(), esempio: "Das Medikament hilft schnell.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Tablette".into(), italiano: "compressa".into(), esempio: "Nehmen Sie eine Tablette am Tag.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Untersuchung".into(), italiano: "visita".into(), esempio: "Die Untersuchung dauert zehn Minuten.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Blutdruck".into(), italiano: "pressione sanguigna".into(), esempio: "Der Blutdruck ist normal.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Fieber".into(), italiano: "febbre".into(), esempio: "Das Fieber ist gesunken.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Erkältung".into(), italiano: "raffreddore".into(), esempio: "Ich habe eine Erkältung.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Husten".into(), italiano: "tosse".into(), esempio: "Der Husten ist schlimm.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Allergie".into(), italiano: "allergia".into(), esempio: "Ich habe eine Allergie gegen Nüsse.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Notfall".into(), italiano: "emergenza".into(), esempio: "Im Notfall rufen Sie 112 an.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Notaufnahme".into(), italiano: "pronto soccorso".into(), esempio: "Die Notaufnahme ist im Erdgeschoss.".into(), tema: "Salute".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Zug".into(), italiano: "treno".into(), esempio: "Der Zug fährt um sieben.".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "der Bus".into(), italiano: "autobus".into(), esempio: "Der Bus kommt in fünf Minuten.".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "das Auto".into(), italiano: "auto".into(), esempio: "Das Auto steht vor dem Haus.".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "das Fahrrad".into(), italiano: "bicicletta".into(), esempio: "Ich fahre mit dem Fahrrad.".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Karte".into(), italiano: "biglietto o carta".into(), esempio: "Ich kaufe eine Karte.".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Richtung".into(), italiano: "direzione".into(), esempio: "In welche Richtung gehen Sie?".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Kreuzung".into(), italiano: "incrocio".into(), esempio: "An der Kreuzung links abbiegen.".into(), tema: "Viaggio".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Nähe".into(), italiano: "vicinanza".into(), esempio: "Die Wohnung ist in der Nähe vom Zentrum.".into(), tema: "Città".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Entfernung".into(), italiano: "distanza".into(), esempio: "Die Entfernung ist kurz.".into(), tema: "Città".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Lärm".into(), italiano: "rumore".into(), esempio: "Der Lärm stört mich.".into(), tema: "Città".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Ruhe".into(), italiano: "quiete".into(), esempio: "Ich brauche Ruhe.".into(), tema: "Città".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Möglichkeit".into(), italiano: "possibilità".into(), esempio: "Es gibt viele Möglichkeiten.".into(), tema: "Società".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Entscheidung".into(), italiano: "decisione".into(), esempio: "Die Entscheidung ist schwer.".into(), tema: "Società".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Hilfe".into(), italiano: "aiuto".into(), esempio: "Danke für die Hilfe.".into(), tema: "Relazioni".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Rat".into(), italiano: "consiglio".into(), esempio: "Danke für den Rat.".into(), tema: "Relazioni".into(), livello: "A2".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Frage".into(), italiano: "domanda".into(), esempio: "Ich habe eine Frage.".into(), tema: "Relazioni".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Antwort".into(), italiano: "risposta".into(), esempio: "Die Antwort ist richtig.".into(), tema: "Relazioni".into(), livello: "A2".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "das Gespräch".into(), italiano: "conversazione".into(), esempio: "Das Gespräch war hilfreich.".into(), tema: "Relazioni".into(), livello: "A2".into(), articolo: Some("das".into()) },
        VoceWortliste { tedesco: "die Aufnahme".into(), italiano: "accettazione, ricovero".into(), esempio: "Die Aufnahme ist um acht Uhr.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Visite".into(), italiano: "visita medica".into(), esempio: "Die Visite beginnt um sieben.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Pflege".into(), italiano: "cura".into(), esempio: "Die Pflege braucht Zeit.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Pflegekraft".into(), italiano: "operatore sanitario".into(), esempio: "Die Pflegekraft hilft beim Waschen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Pflegebedarf".into(), italiano: "bisogno assistenziale".into(), esempio: "Der Pflegebedarf ist hoch.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Mobilisation".into(), italiano: "mobilizzazione".into(), esempio: "Die Mobilisation beginnt früh.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Lagerung".into(), italiano: "posizionamento".into(), esempio: "Die Lagerung muss regelmäßig wechseln.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Verband".into(), italiano: "benda".into(), esempio: "Der Verband muss gewechselt werden.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Wunde".into(), italiano: "ferita".into(), esempio: "Die Wunde ist sauber.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Narbe".into(), italiano: "cicatrice".into(), esempio: "Die Narbe ist noch rot.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Schwellung".into(), italiano: "gonfiore".into(), esempio: "Die Schwellung ist zurückgegangen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Bewegungstherapie".into(), italiano: "terapia del movimento".into(), esempio: "Die Bewegungstherapie hilft beim Gehen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Rehabilitation".into(), italiano: "riabilitazione".into(), esempio: "Die Rehabilitation dauert sechs Wochen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Rollstuhl".into(), italiano: "sedia a rotelle".into(), esempio: "Der Patient braucht einen Rollstuhl.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Gehhilfe".into(), italiano: "ausilio per camminare".into(), esempio: "Die Gehhilfe steht neben dem Bett.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Stock".into(), italiano: "bastone".into(), esempio: "Er geht mit einem Stock.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Treppe".into(), italiano: "scala".into(), esempio: "Die Treppe ist rutschig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Aufzug".into(), italiano: "ascensore".into(), esempio: "Der Aufzug ist außer Betrieb.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Station".into(), italiano: "reparto".into(), esempio: "Ich arbeite auf der Station.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Ambulanz".into(), italiano: "ambulatorio".into(), esempio: "Die Ambulanz ist geöffnet.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Praxis".into(), italiano: "studio medico".into(), esempio: "Die Praxis ist im ersten Stock.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Facharzt".into(), italiano: "medico specialista".into(), esempio: "Der Facharzt kommt morgen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Krankengeschichte".into(), italiano: "storia clinica".into(), esempio: "Die Krankengeschichte ist vollständig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Medikamentenliste".into(), italiano: "elenco dei farmaci".into(), esempio: "Die Medikamentenliste ist aktuell.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Nebenwirkung".into(), italiano: "effetto collaterale".into(), esempio: "Die Nebenwirkung ist selten.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Dosierung".into(), italiano: "dosaggio".into(), esempio: "Die Dosierung steht auf dem Rezept.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Injektion".into(), italiano: "iniezione".into(), esempio: "Die Injektion tut nicht weh.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Blutentnahme".into(), italiano: "prelievo del sangue".into(), esempio: "Die Blutentnahme ist um sieben.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Verbandwechsel".into(), italiano: "cambio della medicazione".into(), esempio: "Der Verbandwechsel erfolgt täglich.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Wundkontrolle".into(), italiano: "controllo della ferita".into(), esempio: "Die Wundkontrolle ist wichtig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Schmerztherapie".into(), italiano: "terapia del dolore".into(), esempio: "Die Schmerztherapie wird angepasst.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Sturzprophylaxe".into(), italiano: "prevenzione delle cadute".into(), esempio: "Die Sturzprophylaxe ist Teil der Pflege.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Hygiene".into(), italiano: "igiene".into(), esempio: "Hygiene ist im Krankenhaus zentral.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Desinfektion".into(), italiano: "disinfezione".into(), esempio: "Die Desinfektion der Hände dauert 30 Sekunden.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Schutzausrüstung".into(), italiano: "dispositivi di protezione".into(), esempio: "Die Schutzausrüstung ist Pflicht.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Notruf".into(), italiano: "chiamata di emergenza".into(), esempio: "Der Notruf ist kostenlos.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Vitalzeichen".into(), italiano: "parametri vitali".into(), esempio: "Die Vitalzeichen sind stabil.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Atmung".into(), italiano: "respirazione".into(), esempio: "Die Atmung ist ruhig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Puls".into(), italiano: "polso".into(), esempio: "Der Puls ist regelmäßig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Temperatur".into(), italiano: "temperatura".into(), esempio: "Die Temperatur ist erhöht.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Blutzucker".into(), italiano: "glicemia".into(), esempio: "Der Blutzucker wird gemessen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Inkontinenz".into(), italiano: "incontinenza".into(), esempio: "Die Inkontinenz wird behandelt.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Demenz".into(), italiano: "demenza".into(), esempio: "Die Demenz verändert den Alltag.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Orientierung".into(), italiano: "orientamento".into(), esempio: "Die Orientierung fällt schwer.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Sturz".into(), italiano: "caduta".into(), esempio: "Der Sturz hatte keine Folgen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Prothese".into(), italiano: "protesi".into(), esempio: "Die Prothese passt gut.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die OP".into(), italiano: "intervento".into(), esempio: "Die OP ist für morgen geplant.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Narkose".into(), italiano: "anestesia".into(), esempio: "Die Narkose ist ungefährlich.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Nachsorge".into(), italiano: "follow-up".into(), esempio: "Die Nachsorge ist wichtig.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Angehörigen".into(), italiano: "familiari".into(), esempio: "Die Angehörigen warten draußen.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Aufklärung".into(), italiano: "informazione al paziente".into(), esempio: "Die Aufklärung erfolgt vor der OP.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Einwilligung".into(), italiano: "consenso".into(), esempio: "Die Einwilligung fehlt noch.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Schweigepflicht".into(), italiano: "segreto professionale".into(), esempio: "Die Schweigepflicht gilt immer.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Übergabe".into(), italiano: "passaggio di consegne".into(), esempio: "Die Übergabe ist um sieben.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Dienstplan".into(), italiano: "piano turni".into(), esempio: "Der Dienstplan kommt am Freitag.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("der".into()) },
        VoceWortliste { tedesco: "die Dokumentation".into(), italiano: "documentazione".into(), esempio: "Die Dokumentation muss vollständig sein.".into(), tema: "Lavoro".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Wundversorgung".into(), italiano: "medicazione della ferita".into(), esempio: "Die Wundversorgung erfolgt steril.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Medikamentengabe".into(), italiano: "somministrazione dei farmaci".into(), esempio: "Die Medikamentengabe wird dokumentiert.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Schmerzskala".into(), italiano: "scala del dolore".into(), esempio: "Die Schmerzskala geht von null bis zehn.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Sturzgefahr".into(), italiano: "rischio di caduta".into(), esempio: "Bei Sturzgefahr braucht der Patient Hilfe.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Immobilität".into(), italiano: "immobilità".into(), esempio: "Die Immobilität erhöht das Risiko.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Kontraktur".into(), italiano: "contrattura".into(), esempio: "Eine Kontraktur kann vermieden werden.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Pneumonie".into(), italiano: "polmonite".into(), esempio: "Die Pneumonie muss früh erkannt werden.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "die Thrombose".into(), italiano: "trombosi".into(), esempio: "Bewegung senkt das Thromboserisiko.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("die".into()) },
        VoceWortliste { tedesco: "der Dekubitus".into(), italiano: "lesione da pressione".into(), esempio: "Der Dekubitus braucht eine spezielle Pflege.".into(), tema: "Salute".into(), livello: "B1".into(), articolo: Some("der".into()) },
    ]
}

// ── Podcast episodes (portati da PodcastData.kt) ──

#[derive(Debug, Clone)]
pub struct EpisodioPodcast { pub id: String, pub livello: String, pub titolo: String, pub canale: String, pub descrizione: String, pub piattaforma: String, pub tipo: String, pub url: String, pub durata: String, pub velocita: String, pub supporto: String, pub tema: String, pub consigliato: bool }

pub fn podcast_episodi() -> Vec<EpisodioPodcast> {
    vec![
        EpisodioPodcast { id: "a1-nicos-weg".into(), livello: "A1".into(), titolo: "Nicos Weg - A1".into(), canale: "Deutsche Welle".into(), descrizione: "Corso video gratuito in episodi brevi: Nico arriva in Germania e impara il tedesco con lui. Ogni puntata ha esercizi e sottotitoli.".into(), piattaforma: "DW".into(), tipo: "CORSO".into(), url: "https://learngerman.dw.com/en/nicos-weg/s-12758".into(), durata: "5-10 min".into(), velocita: "LENTA".into(), supporto: "Sottotitoli DE/EN + esercizi".into(), tema: "Vita quotidiana".into(), consigliato: true },
        EpisodioPodcast { id: "a1-anja".into(), livello: "A1".into(), titolo: "Learn German with Anja".into(), canale: "Anja".into(), descrizione: "Lezioni brevi e divertenti sulle basi: saluti, numeri, verbi e pronuncia. Spiegazioni semplici, ottime per iniziare.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@LearnGermanwithAnja".into(), durata: "10-20 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE/EN".into(), tema: "Basi e pronuncia".into(), consigliato: false },
        EpisodioPodcast { id: "a1-lingoni".into(), livello: "A1".into(), titolo: "lingoni GERMAN - A1".into(), canale: "lingoni GERMAN".into(), descrizione: "Playlist strutturata per livello: grammatica spiegata passo passo con esempi lenti e ripetuti. Perfetta per fissare le strutture.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@lingoniGERMAN".into(), durata: "5-12 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE/EN".into(), tema: "Grammatica di base".into(), consigliato: false },
        EpisodioPodcast { id: "a1-coffee-break".into(), livello: "A1".into(), titolo: "Coffee Break German".into(), canale: "Radio Lingua".into(), descrizione: "Podcast a lezioni: una conversazione guidata per volta, con ripetizioni e spiegazioni. Ideale per l'ascolto in movimento.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/3QiX0ZkjILyGWmpemackWB".into(), durata: "15-20 min".into(), velocita: "LENTA".into(), supporto: "Note ed episodio guidato".into(), tema: "Conversazione guidata".into(), consigliato: false },
        EpisodioPodcast { id: "a1-slow-german".into(), livello: "A1".into(), titolo: "Slow German - Annik Rubens".into(), canale: "Annik Rubens".into(), descrizione: "Il classico podcast lento sulla vita tedesca: birrerie, riciclo, tradizioni. Testo completo da leggere mentre ascolti.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/1LjZv4vFFBWfFPH8bHczGb".into(), durata: "10-15 min".into(), velocita: "LENTA".into(), supporto: "Trascrizione su slowgerman.com".into(), tema: "Cultura e vita tedesca".into(), consigliato: false },
        EpisodioPodcast { id: "a1-stories".into(), livello: "A1".into(), titolo: "Learn German with Stories".into(), canale: "German Stories".into(), descrizione: "Storie progressive in tedesco semplice: la trama aiuta a capire senza tradurre. Ottimo per allenare l'orecchio con continuità.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/3LJ4M70EO7ngyF0fiFA2AW".into(), durata: "10-20 min".into(), velocita: "LENTA".into(), supporto: "Storie graduate A1/A2".into(), tema: "Racconti".into(), consigliato: false },
        EpisodioPodcast { id: "a1-deutsch-fuer-euch".into(), livello: "A1".into(), titolo: "Deutsch für Euch".into(), canale: "Deutsch für Euch".into(), descrizione: "Pronuncia, alfabeto e primi dialoghi spiegati in modo diretto. Video corti da rivedere più volte.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@DeutschFuerEuch".into(), durata: "5-10 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE".into(), tema: "Pronuncia e basi".into(), consigliato: false },
        EpisodioPodcast { id: "a2-nicos-weg".into(), livello: "A2".into(), titolo: "Nicos Weg - A2".into(), canale: "Deutsche Welle".into(), descrizione: "La seconda parte del corso DW: dialoghi più lunghi, vita in Germania e esercizi mirati. Il ponte naturale tra A1 e B1.".into(), piattaforma: "DW".into(), tipo: "CORSO".into(), url: "https://learngerman.dw.com/en/nicos-weg/s-12758".into(), durata: "5-10 min".into(), velocita: "LENTA".into(), supporto: "Sottotitoli DE/EN + esercizi".into(), tema: "Vita quotidiana".into(), consigliato: true },
        EpisodioPodcast { id: "a2-easy-german".into(), livello: "A2".into(), titolo: "Easy German - A2/B1".into(), canale: "Easy Languages".into(), descrizione: "Interviste per strada con sottotitoli tedesco/inglese: senti come parlano davvero le persone, non l'audio di un corso.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@EasyGerman".into(), durata: "8-15 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE/EN".into(), tema: "Interviste per strada".into(), consigliato: false },
        EpisodioPodcast { id: "a2-14-minuten".into(), livello: "A2".into(), titolo: "14 Minuten".into(), canale: "14 Minuten".into(), descrizione: "Video di circa un quarto d'ora su storia, cultura e curiosità tedesche, con linguaggio chiaro e immagini di supporto.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@14Minuten".into(), durata: "10-15 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE".into(), tema: "Storia e cultura".into(), consigliato: false },
        EpisodioPodcast { id: "a2-marija".into(), livello: "A2".into(), titolo: "Deutsch mit Marija".into(), canale: "Deutsch mit Marija".into(), descrizione: "Grammatica e lessico spiegati con calma, con esempi dalla vita reale. Chiara sulle differenze tra italiano e tedesco.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@DeutschmitMarija".into(), durata: "10-20 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE".into(), tema: "Grammatica e lessico".into(), consigliato: false },
        EpisodioPodcast { id: "a2-einfach-gesagt".into(), livello: "A2".into(), titolo: "Einfach gesagt!".into(), canale: "Einfach gesagt!".into(), descrizione: "Episodi brevi per A2-B1: un tema, un vocabolario essenziale e un ritmo sostenibile. Perfetto per la routine giornaliera.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/549KA6AMWIczQUZETUbwx3".into(), durata: "10-20 min".into(), velocita: "LENTA".into(), supporto: "Episodi brevi A2/B1".into(), tema: "Conversazione".into(), consigliato: false },
        EpisodioPodcast { id: "a2-stories".into(), livello: "A2".into(), titolo: "Learn German with Stories - A2".into(), canale: "German Stories".into(), descrizione: "Le storie crescono con te: frasi più lunghe e tempi verbali nuovi, sempre dentro una trama comprensibile.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/3LJ4M70EO7ngyF0fiFA2AW".into(), durata: "10-20 min".into(), velocita: "LENTA".into(), supporto: "Storie graduate A2".into(), tema: "Racconti".into(), consigliato: false },
        EpisodioPodcast { id: "a2-deutsch-fuer-euch".into(), livello: "A2".into(), titolo: "Deutsch für Euch - A2".into(), canale: "Deutsch für Euch".into(), descrizione: "Video di grammatica con esempi e confronti: casi, preposizioni e ordine delle parole senza giri di parole.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@DeutschFuerEuch".into(), durata: "5-10 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE".into(), tema: "Grammatica".into(), consigliato: false },
        EpisodioPodcast { id: "b1-easy-german-podcast".into(), livello: "B1".into(), titolo: "Easy German Podcast".into(), canale: "Easy German".into(), descrizione: "Coco e Manuel parlano di Germania, lingua e attualità con linguaggio chiaro. Il podcast didattico più completo per il B1.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/2rnCPyV8A9J80QtmGj2pAx".into(), durata: "25-40 min".into(), velocita: "MEDIA".into(), supporto: "Trascrizione sul sito Easy German".into(), tema: "Attualità e cultura".into(), consigliato: true },
        EpisodioPodcast { id: "b1-dw-nachrichten".into(), livello: "B1".into(), titolo: "Langsam gesprochene Nachrichten".into(), canale: "Deutsche Welle".into(), descrizione: "Le notizie del giorno lette lentamente, con il testo completo. Il modo migliore per passare dalle storie alle notizie vere.".into(), piattaforma: "DW".into(), tipo: "PODCAST".into(), url: "https://learngerman.dw.com/de/langsam-gesprochene-nachrichten/s-12098".into(), durata: "5-10 min".into(), velocita: "LENTA".into(), supporto: "Testo completo".into(), tema: "Notizie".into(), consigliato: false },
        EpisodioPodcast { id: "b1-top-thema".into(), livello: "B1".into(), titolo: "Top-Thema mit Vokabeln".into(), canale: "Deutsche Welle".into(), descrizione: "Un tema di attualità a settimana con glossario ed esercizi. Ottimo per il lessico che serve davvero a un esame B1.".into(), piattaforma: "DW".into(), tipo: "PODCAST".into(), url: "https://learngerman.dw.com/de/top-thema/s-5591".into(), durata: "5-10 min".into(), velocita: "MEDIA".into(), supporto: "Testo + glossario + esercizi".into(), tema: "Notizie e attualità".into(), consigliato: false },
        EpisodioPodcast { id: "b1-auf-deutsch-gesagt".into(), livello: "B1".into(), titolo: "Auf Deutsch gesagt!".into(), canale: "Robin Meinert".into(), descrizione: "Conversazioni naturali su lavoro, viaggi e vita quotidiana. Parla come un amico colto: ottimo per abituarsi ai ritmi veri.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/036NuzN8VdHMy2i6zfQGUU".into(), durata: "30-45 min".into(), velocita: "NATURALE".into(), supporto: "Trascrizione sul sito".into(), tema: "Conversazione naturale".into(), consigliato: false },
        EpisodioPodcast { id: "b1-benjamin".into(), livello: "B1".into(), titolo: "Deutsch mit Benjamin".into(), canale: "Benjamin".into(), descrizione: "Sfumature, espressioni idiomatiche e piccoli errori tipici di chi impara. Spiegazioni precise, utili anche per la produzione scritta.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@DeutschmitBenjamin".into(), durata: "10-20 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli DE".into(), tema: "Grammatica e stile".into(), consigliato: false },
        EpisodioPodcast { id: "b1-get-germanized".into(), livello: "B1".into(), titolo: "Get Germanized".into(), canale: "Get Germanized".into(), descrizione: "Cultura, abitudini e curiosità tedesche raccontate con ironia. Utile per capire i riferimenti culturali che restano fuori dai libri.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@GetGermanized".into(), durata: "8-15 min".into(), velocita: "MEDIA".into(), supporto: "Sottotitoli EN".into(), tema: "Cultura e società".into(), consigliato: false },
        EpisodioPodcast { id: "b1-slow-german".into(), livello: "B1".into(), titolo: "Slow German - B1".into(), canale: "Annik Rubens".into(), descrizione: "Temi di cultura e società a velocità ridotta, con trascrizione. Il passaggio ideale verso i podcast per madrelingua.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/1LjZv4vFFBWfFPH8bHczGb".into(), durata: "10-15 min".into(), velocita: "LENTA".into(), supporto: "Trascrizione su slowgerman.com".into(), tema: "Cultura".into(), consigliato: false },
        EpisodioPodcast { id: "b1-ard-audiothek".into(), livello: "B1".into(), titolo: "ARD Audiothek".into(), canale: "ARD".into(), descrizione: "Il catalogo radiofonico pubblico tedesco: documentari, cultura e programmi. Scegli un tema che ti interessa e ascolta senza testo.".into(), piattaforma: "WEB".into(), tipo: "PODCAST".into(), url: "https://www.ardaudiothek.de/".into(), durata: "variabile".into(), velocita: "NATURALE".into(), supporto: "Catalogo con filtri per tema".into(), tema: "Radio e documentari".into(), consigliato: false },
        EpisodioPodcast { id: "b2-easy-german".into(), livello: "B2".into(), titolo: "Easy German - B2".into(), canale: "Easy Languages".into(), descrizione: "Interviste su politica, società e filosofia, con sottotitoli. Il tedesco autentico senza rinunciare a un appiglio scritto.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@EasyGerman".into(), durata: "10-20 min".into(), velocita: "NATURALE".into(), supporto: "Sottotitoli DE/EN".into(), tema: "Società e politica".into(), consigliato: true },
        EpisodioPodcast { id: "b2-gemischtes-hack".into(), livello: "B2".into(), titolo: "Gemischtes Hack".into(), canale: "Felix Lobrecht & Tommi Schmitt".into(), descrizione: "Due comici parlano di tutto: registro colloquiale, battute e riferimenti culturali. Difficile ma formidabile per l'orecchio.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/7BTOsF2boKmlYr76BelijW".into(), durata: "60-90 min".into(), velocita: "NATURALE".into(), supporto: "Video-podcast su Spotify".into(), tema: "Conversazione colloquiale".into(), consigliato: false },
        EpisodioPodcast { id: "b2-der-tag".into(), livello: "B2".into(), titolo: "Deutschlandfunk - Der Tag".into(), canale: "Deutschlandfunk".into(), descrizione: "Un tema al giorno spiegato fino in fondo, con ospiti e domande. Il formato giusto per il tedesco delle notizie e delle istituzioni.".into(), piattaforma: "WEB".into(), tipo: "PODCAST".into(), url: "https://www.deutschlandfunk.de/podcast-104.html".into(), durata: "20-30 min".into(), velocita: "NATURALE".into(), supporto: "Trascrizione e articolo".into(), tema: "Attualità e politica".into(), consigliato: false },
        EpisodioPodcast { id: "b2-das-war-der-tag".into(), livello: "B2".into(), titolo: "Das war der Tag".into(), canale: "Deutschlandfunk".into(), descrizione: "La rassegna serale del giorno: corrispondenti dall'estero e sintesi dei fatti. Ascolto ideale per il lessico politico.".into(), piattaforma: "WEB".into(), tipo: "PODCAST".into(), url: "https://www.deutschlandfunk.de/das-war-der-tag-100.html".into(), durata: "30-45 min".into(), velocita: "NATURALE".into(), supporto: "Trascrizione".into(), tema: "Rassegna del giorno".into(), consigliato: false },
        EpisodioPodcast { id: "b2-ard-documentari".into(), livello: "B2".into(), titolo: "ARD Audiothek - documentari".into(), canale: "ARD".into(), descrizione: "Documentari e audio-libri del servizio pubblico: lingua curata, ritmi naturali e temi che vale la pena seguire.".into(), piattaforma: "WEB".into(), tipo: "PODCAST".into(), url: "https://www.ardaudiothek.de/".into(), durata: "30-60 min".into(), velocita: "NATURALE".into(), supporto: "Catalogo con filtri".into(), tema: "Documentari e cultura".into(), consigliato: false },
        EpisodioPodcast { id: "b2-dw-themen".into(), livello: "B2".into(), titolo: "DW - Temi e notizie".into(), canale: "Deutsche Welle".into(), descrizione: "Video e reportage sulle notizie dal mondo, in tedesco standard. Utile per collegare il lessico dell'attualità alla comprensione orale.".into(), piattaforma: "DW".into(), tipo: "VIDEO".into(), url: "https://www.dw.com/de/themen/s-9077".into(), durata: "5-15 min".into(), velocita: "NATURALE".into(), supporto: "Sottotitoli DE".into(), tema: "Notizie dal mondo".into(), consigliato: false },
        EpisodioPodcast { id: "b2-benjamin".into(), livello: "B2".into(), titolo: "Deutsch mit Benjamin - B2".into(), canale: "Benjamin".into(), descrizione: "Sfumature di significato, registro e costruzioni avanzate. Per scrivere e parlare in modo più preciso, non solo corretto.".into(), piattaforma: "YOUTUBE".into(), tipo: "VIDEO".into(), url: "https://www.youtube.com/@DeutschmitBenjamin".into(), durata: "10-20 min".into(), velocita: "NATURALE".into(), supporto: "Sottotitoli DE".into(), tema: "Stile e sfumature".into(), consigliato: false },
        EpisodioPodcast { id: "b2-auf-deutsch-gesagt".into(), livello: "B2".into(), titolo: "Auf Deutsch gesagt! - B2".into(), canale: "Robin Meinert".into(), descrizione: "Conversazioni lunghe e non semplificate, con trascrizione. Per abituarsi a seguire un discorso senza perdere il filo.".into(), piattaforma: "SPOTIFY".into(), tipo: "PODCAST".into(), url: "https://open.spotify.com/show/036NuzN8VdHMy2i6zfQGUU".into(), durata: "30-45 min".into(), velocita: "NATURALE".into(), supporto: "Trascrizione sul sito".into(), tema: "Conversazione".into(), consigliato: false },
    ]
}

/// Info sui livelli (codice, titolo, obiettivo, consiglio).
pub fn podcast_livelli_info() -> Vec<[&'static str; 4]> {
    vec![
        ["A1", "Primo ascolto", "Frasi brevi, vita quotidiana, parlato lento e scandito.", "Ascolta 10 minuti al giorno con i sottotitoli. Non tradurre tutto: cerca le parole che già conosci e lascia scorrere il resto."],
        ["A2", "Storie e dialoghi", "Racconti guidati, interviste semplici, conversazioni di tutti i giorni.", "Riascolta lo stesso episodio due volte: la prima con i sottotitoli, la seconda senza. La seconda volta capirai molto di più."],
        ["B1", "Notizie e podcast", "Attualità, cultura, opinioni: il tedesco che si usa davvero.", "Scegli 3 parole nuove per episodio e usale in una frase scritta. Ascoltare senza riutilizzare lascia poco."],
        ["B2", "Deutsch autentico", "Talk show, approfondimenti, velocità naturale e registro colloquiale.", "Ascolta senza testo, poi riassumi in 5 frasi. Se ci riesci, hai capito davvero: è la prova che serve per il B2."],
    ]
}
