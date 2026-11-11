use serde::{Deserialize, Serialize};
use serde_json::Value;
use std::collections::{BTreeMap, HashMap, HashSet};
use std::fs;
use std::path::PathBuf;

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
    #[serde(flatten)]
    pub extra: HashMap<String, Value>,
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
            let lvl = if w.level.trim().is_empty() { "—".to_string() } else { w.level.trim().to_uppercase() };
            *out.entry(lvl).or_insert(0) += 1;
        }
        out
    }

    pub fn search_words(&self, query: &str, level: Option<&str>, include_archived: bool, limit: usize) -> Vec<WordEntity> {
        let Some(p) = self.active_profile() else { return vec![]; };
        let lvl = level.map(|s| s.trim().to_uppercase()).filter(|s| !s.is_empty() && s != "ALL" && s != "—");
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
    ("Numeri 0-100, età e date", "Numbers 0-100, age and dates"),
    ("La famiglia", "Family"),
    ("Pronomi personali e verbi sein/haben", "Personal pronouns and sein/haben"),
    ("Articoli der/die/das e plurali", "Articles der/die/das and plurals"),
    ("Città e paese", "City and country"),
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
    ("Präteritum di sein e haben", "Präteritum of sein and haben"),
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
    ("Präteritum vs Perfetto", "Präteritum vs Perfekt"),
    ("Konjunktiv II", "Konjunktiv II"),
    ("Lessico: lavoro e professione sanitaria", "Vocabulary: healthcare work"),
    ("Lessico: salute, corpo e sintomi", "Vocabulary: health, body and symptoms"),
    ("Lessico: relazioni e conflitti", "Vocabulary: relationships and conflicts"),
    ("Lessico: istruzione e formazione", "Vocabulary: education and training"),
    ("Lessico: ambiente e sostenibilità", "Vocabulary: environment and sustainability"),
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
            format!("{it} — approfondimento {}: nuove parole e strutture", giro + 1),
            format!("{en} — deepening {}: new words and structures", giro + 1),
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
    pub options: &'static [&'static str],
    pub correct_index: usize,
    pub explanation_it: &'static str,
    pub explanation_en: &'static str,
}

pub fn embedded_grammar_units() -> Vec<GrammarUnit> {
    vec![unit_a0_articoli(), unit_a0_pronomi_sein(), unit_a0_frase(), unit_a1_akkusativ()]
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
            ("die Bücher", "i libri / the books"),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Mann", prompt_en: "Choose the article: ___ Mann", options: &["der", "die", "das"], correct_index: 0, explanation_it: "Mann è maschile: der Mann.", explanation_en: "Mann is masculine: der Mann." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Frau", prompt_en: "Choose the article: ___ Frau", options: &["der", "die", "das"], correct_index: 1, explanation_it: "Frau è femminile: die Frau.", explanation_en: "Frau is feminine: die Frau." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Kind", prompt_en: "Choose the article: ___ Kind", options: &["der", "die", "das"], correct_index: 2, explanation_it: "Kind è neutro: das Kind.", explanation_en: "Kind is neuter: das Kind." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo (plurale): ___ Kinder", prompt_en: "Choose the article (plural): ___ Kinder", options: &["der", "die", "das"], correct_index: 1, explanation_it: "Il plurale al nominativo usa die.", explanation_en: "Plural nominative uses die." },
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
            ("Du bist müde.", "Tu sei stanco/a."),
            ("Wir sind in Berlin.", "Siamo a Berlino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ Student.", prompt_en: "Complete: Ich ___ Student.", options: &["bin", "bist", "ist"], correct_index: 0, explanation_it: "Con ich si usa bin.", explanation_en: "With ich use bin." },
            GrammarUnitExercise { prompt_it: "Completa: Du ___ nett.", prompt_en: "Complete: Du ___ nett.", options: &["bin", "bist", "sind"], correct_index: 1, explanation_it: "Con du si usa bist.", explanation_en: "With du use bist." },
            GrammarUnitExercise { prompt_it: "Completa: Er ___ Arzt.", prompt_en: "Complete: Er ___ Arzt.", options: &["ist", "bist", "seid"], correct_index: 0, explanation_it: "Con er/sie/es si usa ist.", explanation_en: "With er/sie/es use ist." },
            GrammarUnitExercise { prompt_it: "Completa: Wir ___ Freunde.", prompt_en: "Complete: Wir ___ Freunde.", options: &["seid", "sind", "ist"], correct_index: 1, explanation_it: "Con wir si usa sind.", explanation_en: "With wir use sind." },
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
            "Nella frase principale il verbo coniugato è sempre in posizione 2.",
            "Il soggetto può stare in posizione 1 o 3: Ich lerne Deutsch. / Heute lerne ich Deutsch.",
            "Le domande sì/no iniziano con il verbo: Lernst du Deutsch?",
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
            GrammarUnitExercise { prompt_it: "Quale frase è corretta?", prompt_en: "Which sentence is correct?", options: &["Ich heute lerne Deutsch.", "Heute lerne ich Deutsch.", "Heute ich lerne Deutsch."], correct_index: 1, explanation_it: "Il verbo resta in seconda posizione: Heute (1) lerne (2) ich (3)...", explanation_en: "Verb stays in position 2: Heute (1) lerne (2) ich (3)..." },
            GrammarUnitExercise { prompt_it: "Completa l'ordine corretto: ___ du Deutsch? (lernen)", prompt_en: "Complete the correct order: ___ du Deutsch? (lernen)", options: &["Du lernst", "Lernst", "Deutsch lernst"], correct_index: 1, explanation_it: "Nelle domande sì/no il verbo va all'inizio.", explanation_en: "In yes/no questions the verb comes first." },
            GrammarUnitExercise { prompt_it: "Quale frase è corretta?", prompt_en: "Which sentence is correct?", options: &["Wir spielen heute Fußball.", "Wir heute spielen Fußball.", "Spielen wir heute Fußball."], correct_index: 0, explanation_it: "Frase affermativa: soggetto (1), verbo (2), resto.", explanation_en: "Statement: subject (1), verb (2), rest." },
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
            GrammarUnitExercise { prompt_it: "Completa: Ich sehe ___ Mann. (der)", prompt_en: "Complete: Ich sehe ___ Mann. (der)", options: &["der", "den", "dem"], correct_index: 1, explanation_it: "Maschile all'accusativo: der -> den.", explanation_en: "Masculine accusative: der -> den." },
            GrammarUnitExercise { prompt_it: "Completa: Ich habe ___ Katze. (eine)", prompt_en: "Complete: Ich habe ___ Katze. (eine)", options: &["eine", "einen", "einer"], correct_index: 0, explanation_it: "Femminile non cambia all'accusativo: eine.", explanation_en: "Feminine does not change in accusative: eine." },
            GrammarUnitExercise { prompt_it: "Completa: Er kauft ___ Buch. (ein)", prompt_en: "Complete: Er kauft ___ Buch. (ein)", options: &["ein", "einen", "einem"], correct_index: 0, explanation_it: "Neutro non cambia all'accusativo: ein.", explanation_en: "Neuter does not change in accusative: ein." },
            GrammarUnitExercise { prompt_it: "Completa: Wir brauchen ___ Tisch. (der)", prompt_en: "Complete: Wir brauchen ___ Tisch. (der)", options: &["der", "den", "des"], correct_index: 1, explanation_it: "Oggetto diretto maschile: den Tisch.", explanation_en: "Masculine direct object: den Tisch." },
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

impl Store {
    pub fn embedded_grammar_units(&self) -> Vec<GrammarUnit> {
        embedded_grammar_units()
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
}
