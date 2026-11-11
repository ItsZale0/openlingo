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
    pub options: &'static [&'static str], // empty => typing/completion answer
    pub correct_index: usize,
    pub answer_text: &'static str, // expected text for typing exercises (case-insensitive trim)
    pub explanation_it: &'static str,
    pub explanation_en: &'static str,
}

pub fn embedded_grammar_units() -> Vec<GrammarUnit> {
    vec![
        unit_a0_articoli(),
        unit_a0_pronomi_sein(),
        unit_a0_frase(),
        unit_a1_akkusativ(),
        unit_a1_sein_haben(),
        unit_a1_possessivi_negazione(),
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
        unit_b1_reflexive_verben(),
        unit_b1_praeteritum_perfekt(),
        unit_b1_genitiv_intro(),
        unit_b1_konjunktiv2_intro(),
        unit_b1_indirekte_rede(),
        unit_b1_nominalisierung(),
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
            ("die Bücher", "i libri / the books"),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Mann", prompt_en: "Choose the article: ___ Mann", options: &["der", "die", "das"], correct_index: 0, answer_text: "", explanation_it: "Mann è maschile: der Mann.", explanation_en: "Mann is masculine: der Mann." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Frau", prompt_en: "Choose the article: ___ Frau", options: &["der", "die", "das"], correct_index: 1, answer_text: "", explanation_it: "Frau è femminile: die Frau.", explanation_en: "Frau is feminine: die Frau." },
            GrammarUnitExercise { prompt_it: "Scegli l'articolo: ___ Kind", prompt_en: "Choose the article: ___ Kind", options: &["der", "die", "das"], correct_index: 2, answer_text: "", explanation_it: "Kind è neutro: das Kind.", explanation_en: "Kind is neuter: das Kind." },
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
            ("Du bist müde.", "Tu sei stanco/a."),
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
            GrammarUnitExercise { prompt_it: "Quale frase è corretta?", prompt_en: "Which sentence is correct?", options: &["Ich heute lerne Deutsch.", "Heute lerne ich Deutsch.", "Heute ich lerne Deutsch."], correct_index: 1, answer_text: "", explanation_it: "Il verbo resta in seconda posizione: Heute (1) lerne (2) ich (3)...", explanation_en: "Verb stays in position 2: Heute (1) lerne (2) ich (3)..." },
            GrammarUnitExercise { prompt_it: "Completa l'ordine corretto: ___ du Deutsch? (lernen)", prompt_en: "Complete the correct order: ___ du Deutsch? (lernen)", options: &["Du lernst", "Lernst", "Deutsch lernst"], correct_index: 1, answer_text: "", explanation_it: "Nelle domande sì/no il verbo va all'inizio.", explanation_en: "In yes/no questions the verb comes first." },
            GrammarUnitExercise { prompt_it: "Quale frase è corretta?", prompt_en: "Which sentence is correct?", options: &["Wir spielen heute Fußball.", "Wir heute spielen Fußball.", "Spielen wir heute Fußball."], correct_index: 0, answer_text: "", explanation_it: "Frase affermativa: soggetto (1), verbo (2), resto.", explanation_en: "Statement: subject (1), verb (2), rest." },
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
            GrammarUnitExercise { prompt_it: "Completa: Du ___ müde. (sein)", prompt_en: "Complete: Du ___ müde. (sein)", options: &["bin", "bist", "ist"], correct_index: 1, answer_text: "", explanation_it: "Con du: du bist.", explanation_en: "With du: du bist." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma corretta: Er ___ (haben) ein Auto.", prompt_en: "Type the correct form: Er ___ (haben) ein Auto.", options: &[], correct_index: 0, answer_text: "hat", explanation_it: "Con er/sie/es: er hat.", explanation_en: "With er/sie/es: er hat." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma corretta: Wir ___ (sein) bereit.", prompt_en: "Type the correct form: Wir ___ (sein) bereit.", options: &[], correct_index: 0, answer_text: "sind", explanation_it: "Con wir: wir sind.", explanation_en: "With wir: wir sind." },
        ],
    }
}

fn unit_a1_modalverben() -> GrammarUnit {
    GrammarUnit {
        id: "a1-modalverben",
        level: "A1",
        title_it: "Verbi modali: können, müssen, wollen",
        title_en: "Modal verbs: können, müssen, wollen",
        subtitle_it: "Potere, dovere, volere",
        subtitle_en: "Can, must, want",
        objective_it: "Usare i modali principali con l'infinito in fondo.",
        objective_en: "Use common modals with the infinitive at the end.",
        theory_it: &[
            "I modali cambiano la vocale/radice in alcune persone: ich kann, du kannst, er kann; ich muss, du musst; ich will, du willst.",
            "Nella frase principale il modale è coniugato in posizione 2 e l'infinito va alla fine.",
            "Esempio: Ich kann Deutsch sprechen. / Wir müssen heute lernen.",
        ],
        theory_en: &[
            "Modals change stem in some forms: ich kann, du kannst, er kann; ich muss, du musst; ich will, du willst.",
            "In a main clause the conjugated modal is in position 2 and the infinitive goes to the end.",
            "Example: Ich kann Deutsch sprechen. / Wir müssen heute lernen.",
        ],
        examples: &[
            ("Ich kann schwimmen.", "So nuotare."),
            ("Du musst arbeiten.", "Devi lavorare."),
            ("Wir wollen nach Hause gehen.", "Vogliamo andare a casa."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ gut kochen. (können)", prompt_en: "Complete: Ich ___ gut kochen. (können)", options: &["kann", "kannst", "können"], correct_index: 0, answer_text: "", explanation_it: "Con ich: ich kann.", explanation_en: "With ich: ich kann." },
            GrammarUnitExercise { prompt_it: "Completa: Du ___ heute lernen. (müssen)", prompt_en: "Complete: Du ___ heute lernen. (müssen)", options: &["muss", "musst", "müssen"], correct_index: 1, answer_text: "", explanation_it: "Con du: du musst.", explanation_en: "With du: du musst." },
            GrammarUnitExercise { prompt_it: "Scegli la frase corretta.", prompt_en: "Choose the correct sentence.", options: &["Ich will spielen Fußball.", "Ich will Fußball spielen.", "Will ich Fußball spielen."], correct_index: 1, answer_text: "", explanation_it: "Il modale va in posizione 2 e l'infinito alla fine.", explanation_en: "Modal in position 2, infinitive at the end." },
            GrammarUnitExercise { prompt_it: "Scrivi il modale corretto: Er ___ (können) helfen.", prompt_en: "Type the correct modal: Er ___ (können) helfen.", options: &[], correct_index: 0, answer_text: "kann", explanation_it: "Con er/sie/es: er kann.", explanation_en: "With er/sie/es: er kann." },
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
            GrammarUnitExercise { prompt_it: "Quale ausiliare è tipico per 'machen'?", prompt_en: "Which auxiliary is typical for 'machen'?", options: &["sein", "haben", "werden"], correct_index: 1, answer_text: "", explanation_it: "machen forma il Perfekt con haben: ich habe gemacht.", explanation_en: "machen forms Perfekt with haben: ich habe gemacht." },
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
            ("Sie gibt der Frau ein Buch.", "Dà un libro alla donna."),
            ("Wir fahren mit dem Bus.", "Andiamo in autobus."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich helfe ___ Mann. (der)", prompt_en: "Complete: Ich helfe ___ Mann. (der)", options: &["den", "dem", "der"], correct_index: 1, answer_text: "", explanation_it: "Dativo maschile: der -> dem.", explanation_en: "Masculine dative: der -> dem." },
            GrammarUnitExercise { prompt_it: "Completa: Sie dankt ___ Frau. (die)", prompt_en: "Complete: Sie dankt ___ Frau. (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "Dativo femminile: die -> der.", explanation_en: "Feminine dative: die -> der." },
            GrammarUnitExercise { prompt_it: "Scegli la preposizione che regge il dativo.", prompt_en: "Choose the preposition that takes dative.", options: &["für", "ohne", "mit"], correct_index: 2, answer_text: "", explanation_it: "mit richiede il dativo (es. mit dem Bus).", explanation_en: "mit takes the dative (e.g. mit dem Bus)." },
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
        objective_it: "Costruire frasi con weil (perché) e dass (che).",
        objective_en: "Build sentences with weil (because) and dass (that).",
        theory_it: &[
            "Nelle subordinate introdotte da weil/dass il verbo coniugato va alla fine.",
            "La subordinata può stare dopo o prima della principale: Ich bleibe zu Hause, weil ich krank bin. / Weil ich krank bin, bleibe ich zu Hause.",
            "Con modale: ... weil ich heute arbeiten muss.",
        ],
        theory_en: &[
            "In subordinate clauses introduced by weil/dass the conjugated verb goes to the end.",
            "The clause can come after or before the main clause: Ich bleibe zu Hause, weil ich krank bin. / Weil ich krank bin, bleibe ich zu Hause.",
            "With modal: ... weil ich heute arbeiten muss.",
        ],
        examples: &[
            ("Ich lerne Deutsch, weil ich in Wien arbeite.", "Studio tedesco perché lavoro a Vienna."),
            ("Ich weiß, dass du müde bist.", "So che sei stanco/a."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Quale frase è corretta?", prompt_en: "Which sentence is correct?", options: &["Ich bleibe, weil ich bin krank.", "Ich bleibe, weil ich krank bin.", "Weil ich krank bin, ich bleibe."], correct_index: 1, answer_text: "", explanation_it: "In subordinata il verbo va alla fine: weil ich krank bin.", explanation_en: "In subordinate clause the verb goes to the end: weil ich krank bin." },
            GrammarUnitExercise { prompt_it: "Completa: Ich denke, dass er heute ___ . (kommen)", prompt_en: "Complete: Ich denke, dass er heute ___ . (kommen)", options: &["kommt", "kommen", "kam"], correct_index: 0, answer_text: "", explanation_it: "Verbo coniugato alla fine della subordinata: dass er heute kommt.", explanation_en: "Conjugated verb at the end: dass er heute kommt." },
            GrammarUnitExercise { prompt_it: "Scegli la congiunzione: Ich lerne, ___ ich die Prüfung habe.", prompt_en: "Choose the conjunction: Ich lerne, ___ ich die Prüfung habe.", options: &["dass", "weil", "ob"], correct_index: 1, answer_text: "", explanation_it: "weil esprime causa: perché ho l'esame.", explanation_en: "weil expresses reason/cause." },
            GrammarUnitExercise { prompt_it: "Scrivi l'ultima parola: Ich weiß, dass du Recht ___ . (haben)", prompt_en: "Type the last word: Ich weiß, dass du Recht ___ . (haben)", options: &[], correct_index: 0, answer_text: "hast", explanation_it: "In subordinata il verbo coniugato va alla fine: dass du Recht hast.", explanation_en: "Conjugated verb at the end of subordinate clause: dass du Recht hast." },
        ],
    }
}

fn unit_a2_komparativ() -> GrammarUnit {
    GrammarUnit {
        id: "a2-komparativ",
        level: "A2",
        title_it: "Comparativi e superlativi",
        title_en: "Comparatives and superlatives",
        subtitle_it: "Confrontare persone, cose e qualità",
        subtitle_en: "Compare people, things, and qualities",
        objective_it: "Formare e usare comparativo e superlativo di base.",
        objective_en: "Form and use basic comparative and superlative forms.",
        theory_it: &[
            "Comparativo: aggettivo + -er (+ spesso umlaut: alt -> älter), confronto con 'als'.",
            "Superlativo: am + aggettivo + -sten (am schnellsten) oppure der/die/das + -ste in attributo.",
            "Irregolari comuni: gut -> besser -> am besten; viel -> mehr -> am meisten.",
        ],
        theory_en: &[
            "Comparative: adjective + -er (often umlaut: alt -> älter), comparison with 'als'.",
            "Superlative: am + adjective + -sten (am schnellsten) or der/die/das + -ste as attribute.",
            "Common irregulars: gut -> besser -> am besten; viel -> mehr -> am meisten.",
        ],
        examples: &[
            ("Mein Auto ist schneller als dein Auto.", "La mia auto è più veloce della tua."),
            ("Sie ist die beste Spielerin.", "Lei è la migliore giocatrice."),
            ("Im Sommer ist es am wärmsten.", "In estate fa più caldo (è il periodo più caldo)."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa il comparativo: schnell -> ___", prompt_en: "Complete the comparative: schnell -> ___", options: &["schneller", "am schnellsten", "schnellste"], correct_index: 0, answer_text: "", explanation_it: "Comparativo regolare: schnell + -er = schneller.", explanation_en: "Regular comparative: schnell + -er = schneller." },
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: gut -> ___ -> am besten", prompt_en: "Choose the correct form: gut -> ___ -> am besten", options: &["guter", "besser", "mehr"], correct_index: 1, answer_text: "", explanation_it: "gut è irregolare: besser.", explanation_en: "gut is irregular: besser." },
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
        objective_it: "Riconoscere le desinenze più comuni degli aggettivi attributivi.",
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
            ("Der kleine Hund schläft.", "Il cagnolino dorme."),
            ("Ich habe eine neue Tasche.", "Ho una borsa nuova."),
            ("Das ist ein gutes Buch.", "Questo è un buon libro."),
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
        subtitle_it: "mit, nach, bei, von, zu, aus, seit, gegenüber",
        subtitle_en: "mit, nach, bei, von, zu, aus, seit, gegenüber",
        objective_it: "Usare le preposizioni più comuni che richiedono il dativo.",
        objective_en: "Use common prepositions that take the dative.",
        theory_it: &[
            "Alcune preposizioni reggono sempre il dativo: aus, bei, mit, nach, seit, von, zu, gegenüber.",
            "Dopo queste preposizioni l'articolo maschile/neutro diventa dem, il femminile der.",
            "Esempio: mit dem Freund, bei der Ärztin, zu dem -> zum, zu der -> zur.",
        ],
        theory_en: &[
            "Some prepositions always take the dative: aus, bei, mit, nach, seit, von, zu, gegenüber.",
            "After these, masculine/neuter article becomes dem, feminine becomes der.",
            "Example: mit dem Freund, bei der Ärztin, zu dem -> zum, zu der -> zur.",
        ],
        examples: &[
            ("Ich fahre mit dem Bus.", "Vado in autobus."),
            ("Sie kommt aus der Schweiz.", "Lei viene dalla Svizzera."),
            ("Wir gehen zu dem/zum Arzt.", "Andiamo dal medico."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich fahre mit ___ Auto. (das)", prompt_en: "Complete: Ich fahre mit ___ Auto. (das)", options: &["das", "dem", "den"], correct_index: 1, answer_text: "", explanation_it: "mit + dativo neutro: dem Auto.", explanation_en: "mit + dative neuter: dem Auto." },
            GrammarUnitExercise { prompt_it: "Completa: Er wohnt bei ___ Tante. (die)", prompt_en: "Complete: Er wohnt bei ___ Tante. (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "bei + dativo femminile: der Tante.", explanation_en: "bei + dative feminine: der Tante." },
            GrammarUnitExercise { prompt_it: "Scegli la preposizione che regge il dativo.", prompt_en: "Choose the preposition that takes dative.", options: &["durch", "für", "von"], correct_index: 2, answer_text: "", explanation_it: "von richiede il dativo; durch/für l'accusativo.", explanation_en: "von takes dative; durch/für take accusative." },
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
            ("Das ist der Mann, der hier wohnt.", "Questo è l'uomo che abita qui."),
            ("Ich kenne die Frau, die du meinst.", "Conosco la donna che intendi."),
            ("Das ist das Buch, das ich lese.", "Questo è il libro che leggo."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Der Mann, ___ dort steht, ist mein Lehrer.", prompt_en: "Complete: Der Mann, ___ dort steht, ist mein Lehrer.", options: &["der", "den", "dem"], correct_index: 0, answer_text: "", explanation_it: "Soggetto maschile nella relativa: der.", explanation_en: "Masculine subject in relative clause: der." },
            GrammarUnitExercise { prompt_it: "Completa: Das Kind, ___ ich sehe, spielt.", prompt_en: "Complete: Das Kind, ___ ich sehe, spielt.", options: &["der", "das", "dem"], correct_index: 1, answer_text: "", explanation_it: "Neutro, oggetto in relativa (accusativo = nominativo qui): das.", explanation_en: "Neuter object in relative clause: das." },
            GrammarUnitExercise { prompt_it: "Completa: Die Frau, ___ du kennst, ist Ärztin. (accusativo)", prompt_en: "Complete: Die Frau, ___ du kennst, ist Ärztin. (accusative)", options: &["der", "die", "den"], correct_index: 1, answer_text: "", explanation_it: "Femminile accusativo nella relativa: die.", explanation_en: "Feminine accusative in relative clause: die." },
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
            "deshalb (perciò) introduce una conseguenza: spesso il verbo segue subito (posizione 2) nella frase principale.",
            "trotzdem (nonostante ciò) esprime contrasto rispetto a quanto detto prima.",
            "obwohl (sebbene) introduce una subordinata: il verbo coniugato va alla fine.",
        ],
        theory_en: &[
            "deshalb (therefore) introduces a consequence; the verb often follows in position 2 in the main clause.",
            "trotzdem (nevertheless) expresses contrast with what was said before.",
            "obwohl (although) introduces a subordinate clause: the conjugated verb goes to the end.",
        ],
        examples: &[
            ("Es regnet, deshalb bleibe ich zu Hause.", "Piove, perciò resto a casa."),
            ("Ich bin müde, trotzdem arbeite ich.", "Sono stanco, eppure lavoro."),
            ("Ich gehe spazieren, obwohl es regnet.", "Vado a passeggiare sebbene piova."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli il connettore: Es ist kalt, ___ ziehe ich eine Jacke an.", prompt_en: "Choose the connector: Es ist kalt, ___ ziehe ich eine Jacke an.", options: &["obwohl", "deshalb", "weil"], correct_index: 1, answer_text: "", explanation_it: "Conseguenza: fa freddo, perciò metto la giacca.", explanation_en: "Consequence: it's cold, therefore I put on a jacket." },
            GrammarUnitExercise { prompt_it: "Scegli il connettore: ___ ich krank bin, gehe ich zur Arbeit.", prompt_en: "Choose the connector: ___ ich krank bin, gehe ich zur Arbeit.", options: &["Deshalb", "Trotzdem", "Obwohl"], correct_index: 2, answer_text: "", explanation_it: "obwohl introduce una concessione/contrasto (verbo alla fine nella subordinata).", explanation_en: "obwohl introduces concession/contrast (verb at the end in the clause)." },
            GrammarUnitExercise { prompt_it: "Completa: Ich habe wenig Zeit, ___ helfe ich dir.", prompt_en: "Complete: Ich habe wenig Zeit, ___ helfe ich dir.", options: &["trotzdem", "obwohl", "weil"], correct_index: 0, answer_text: "", explanation_it: "Contrasto: poco tempo, eppure ti aiuto.", explanation_en: "Contrast: little time, nevertheless I help you." },
            GrammarUnitExercise { prompt_it: "Scrivi il connettore: Ich lerne viel, ___ möchte ich die Prüfung bestehen.", prompt_en: "Type the connector: Ich lerne viel, ___ möchte ich die Prüfung bestehen.", options: &[], correct_index: 0, answer_text: "deshalb", explanation_it: "Conseguenza: studio molto, perciò voglio superare l'esame.", explanation_en: "Consequence: I study a lot, therefore I want to pass the exam." },
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
            ("Wir interessieren uns für Musik.", "Ci interessiamo di musica."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich freue ___ .", prompt_en: "Complete: Ich freue ___ .", options: &["mich", "dich", "sich"], correct_index: 0, answer_text: "", explanation_it: "Con ich si usa mich.", explanation_en: "With ich use mich." },
            GrammarUnitExercise { prompt_it: "Completa: Du wäschst ___ .", prompt_en: "Complete: Du wäschst ___ .", options: &["mich", "dich", "sich"], correct_index: 1, answer_text: "", explanation_it: "Con du si usa dich.", explanation_en: "With du use dich." },
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
        title_it: "Präteritum vs Perfekt (uso)",
        title_en: "Präteritum vs Perfekt (usage)",
        subtitle_it: "Quando usare il passato scritto o parlato",
        subtitle_en: "When to use written vs spoken past",
        objective_it: "Distinguere l'uso tipico di Praeteritum e Perfekt in tedesco.",
        objective_en: "Distinguish typical usage of Präteritum and Perfekt in German.",
        theory_it: &[
            "Nel parlato quotidiano il Perfekt e' il tempo passato piu' comune per raccontare eventi.",
            "Il Praeteritum e' frequente nella lingua scritta, nei racconti e con alcuni verbi (sein, haben, modali) anche nel parlato.",
            "Esempio parlato: Ich habe gestern gearbeitet. / Scritto/narrativo: Er ging nach Hause.",
        ],
        theory_en: &[
            "In everyday speech, Perfekt is the most common past tense for narrating events.",
            "Präteritum is common in writing, narratives, and with some verbs (sein, haben, modals) even in speech.",
            "Spoken example: Ich habe gestern gearbeitet. / Written/narrative: Er ging nach Hause.",
        ],
        examples: &[
            ("Ich war krank.", "Ero malato."),
            ("Wir haben einen Film gesehen.", "Abbiamo visto un film."),
            ("Sie hatte keine Zeit.", "Lei non aveva tempo."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Quale forma e' tipicamente piu' comune nel parlato per 'lavorare ieri'?", prompt_en: "Which form is typically more common in speech for 'worked yesterday'?", options: &["Ich arbeitete gestern.", "Ich habe gestern gearbeitet.", "Ich hatte gestern gearbeitet."], correct_index: 1, answer_text: "", explanation_it: "Nel parlato quotidiano si preferisce spesso il Perfekt.", explanation_en: "In everyday speech Perfekt is often preferred." },
            GrammarUnitExercise { prompt_it: "Quale verbo e' spesso usato al Praeteritum anche nel parlato?", prompt_en: "Which verb is often used in Präteritum even in speech?", options: &["kaufen", "sein", "spielen"], correct_index: 1, answer_text: "", explanation_it: "sein, haben e i modali compaiono spesso al Praeteritum anche nel parlato.", explanation_en: "sein, haben and modals often appear in Präteritum even in speech." },
            GrammarUnitExercise { prompt_it: "Completa (Perfekt): Wir ___ einen Film ___ . (sehen)", prompt_en: "Complete (Perfekt): Wir ___ einen Film ___ . (sehen)", options: &["haben ... gesehen", "sind ... gesehen", "haben ... sahen"], correct_index: 0, answer_text: "", explanation_it: "Perfekt di sehen: haben + gesehen.", explanation_en: "Perfekt of sehen: haben + gesehen." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma Praeteritum di 'haben' (ich).", prompt_en: "Type the Präteritum form of 'haben' (ich).", options: &[], correct_index: 0, answer_text: "hatte", explanation_it: "Praeteritum: ich hatte.", explanation_en: "Präteritum: ich hatte." },
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
        title_it: "Preposizioni a doppio caso (Wechselpräpositionen)",
        title_en: "Two-way prepositions (Wechselpräpositionen)",
        subtitle_it: "Dativo (dove?) o accusativo (verso dove?)",
        subtitle_en: "Dative (where?) or accusative (where to?)",
        objective_it: "Scegliere dativo o accusativo con an, auf, in, neben, über, unter, vor, zwischen, hinter.",
        objective_en: "Choose dative or accusative with an, auf, in, neben, über, unter, vor, zwischen, hinter.",
        theory_it: &[
            "Le Wechselpräpositionen reggono il dativo per indicare posizione/stato (Wo?) e l'accusativo per direzione/movimento (Wohin?).",
            "Preposizioni: an, auf, hinter, in, neben, über, unter, vor, zwischen.",
            "Esempio: Ich bin in der Küche. (Wo? dativo) / Ich gehe in die Küche. (Wohin? accusativo).",
        ],
        theory_en: &[
            "Two-way prepositions take the dative for location/state (Wo?) and the accusative for direction/movement (Wohin?).",
            "Prepositions: an, auf, hinter, in, neben, über, unter, vor, zwischen.",
            "Example: Ich bin in der Küche. (Where? dative) / Ich gehe in die Küche. (Where to? accusative).",
        ],
        examples: &[
            ("Das Buch liegt auf dem Tisch.", "Il libro è sul tavolo. (posizione)"),
            ("Ich lege das Buch auf den Tisch.", "Metto il libro sul tavolo. (direzione)"),
            ("Wir sitzen im (in dem) Garten.", "Siamo seduti in giardino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (posizione, Wo?): Ich bin in ___ Küche. (die)", prompt_en: "Complete (location, Wo?): Ich bin in ___ Küche. (die)", options: &["die", "der", "den"], correct_index: 1, answer_text: "", explanation_it: "Posizione -> dativo femminile: in der Küche.", explanation_en: "Location -> dative feminine: in der Küche." },
            GrammarUnitExercise { prompt_it: "Completa (direzione, Wohin?): Ich gehe in ___ Küche. (die)", prompt_en: "Complete (direction, Wohin?): Ich gehe in ___ Küche. (die)", options: &["der", "die", "den"], correct_index: 1, answer_text: "", explanation_it: "Direzione -> accusativo femminile (uguale forma): in die Küche.", explanation_en: "Direction -> accusative feminine: in die Küche." },
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
        objective_it: "Usare forme base del Konjunktiv II (würde, hätte, wäre, könnte).",
        objective_en: "Use basic Konjunktiv II forms (würde, hätte, wäre, könnte).",
        theory_it: &[
            "Il Konjunktiv II esprime desideri, ipotesi irreali e richieste cortesi.",
            "Forme comuni: ich wäre (sarei), ich hätte (avrei), ich könnte (potrei), ich würde + infinito (farei).",
            "Esempi: Ich hätte gern einen Kaffee. / Könntest du mir helfen? / Wenn ich Zeit hätte, würde ich reisen.",
        ],
        theory_en: &[
            "Konjunktiv II expresses wishes, unreal hypotheses, and polite requests.",
            "Common forms: ich wäre (would be), ich hätte (would have), ich könnte (could), ich würde + infinitive (would).",
            "Examples: Ich hätte gern einen Kaffee. / Könntest du mir helfen? / Wenn ich Zeit hätte, würde ich reisen.",
        ],
        examples: &[
            ("Ich hätte gern ein Wasser.", "Vorrei un'acqua."),
            ("Könnten Sie das bitte wiederholen?", "Potrebbe ripeterlo, per favore?"),
            ("Wenn ich reich wäre, würde ich viel reisen.", "Se fossi ricco, viaggerei molto."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa (richiesta cortese): ___ Sie mir helfen?", prompt_en: "Complete (polite request): ___ Sie mir helfen?", options: &["Können", "Könnten", "Konnten"], correct_index: 1, answer_text: "", explanation_it: "Richiesta cortese: Konjunktiv II -> Könnten Sie...?", explanation_en: "Polite request: Konjunktiv II -> Könnten Sie...?" },
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: Ich ___ gern einen Tee. (haben)", prompt_en: "Choose the correct form: Ich ___ gern einen Tee. (haben)", options: &["habe", "hatte", "hätte"], correct_index: 2, answer_text: "", explanation_it: "Desiderio cortese: ich hätte gern...", explanation_en: "Polite wish: ich hätte gern..." },
            GrammarUnitExercise { prompt_it: "Completa: Wenn ich frei ___, würde ich kommen. (sein)", prompt_en: "Complete: Wenn ich frei ___, würde ich kommen. (sein)", options: &["bin", "war", "wäre"], correct_index: 2, answer_text: "", explanation_it: "Irreale/condizionale con sein: ich wäre.", explanation_en: "Unreal/conditional with sein: ich wäre." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: ich ___ (sein, Konjunktiv II)", prompt_en: "Type the form: ich ___ (sein, Konjunktiv II)", options: &[], correct_index: 0, answer_text: "wäre", explanation_it: "Konjunktiv II di sein: ich wäre.", explanation_en: "Konjunktiv II of sein: ich wäre." },
        ],
    }
}

fn unit_a2_praepositionen_akkusativ() -> GrammarUnit {
    GrammarUnit {
        id: "a2-praepositionen-akkusativ",
        level: "A2",
        title_it: "Preposizioni con accusativo",
        title_en: "Prepositions with accusative",
        subtitle_it: "durch, für, gegen, ohne, um",
        subtitle_en: "durch, für, gegen, ohne, um",
        objective_it: "Usare le preposizioni più comuni che richiedono l'accusativo.",
        objective_en: "Use common prepositions that take the accusative.",
        theory_it: &[
            "Alcune preposizioni reggono sempre l'accusativo: durch, für, gegen, ohne, um.",
            "Dopo queste preposizioni l'articolo maschile diventa den; femminile/neutro restano die/das.",
            "Esempio: für den Mann, ohne die Tasche, durch das Fenster.",
        ],
        theory_en: &[
            "Some prepositions always take the accusative: durch, für, gegen, ohne, um.",
            "After these, the masculine article becomes den; feminine/neuter remain die/das.",
            "Example: für den Mann, ohne die Tasche, durch das Fenster.",
        ],
        examples: &[
            ("Das Geschenk ist für dich.", "Il regalo è per te."),
            ("Wir gehen durch den Park.", "Attraversiamo il parco."),
            ("Ich gehe ohne meinen Bruder.", "Vado senza mio fratello."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Das ist für ___ Mann. (der)", prompt_en: "Complete: Das ist für ___ Mann. (der)", options: &["der", "den", "dem"], correct_index: 1, answer_text: "", explanation_it: "für + accusativo maschile: den Mann.", explanation_en: "für + accusative masculine: den Mann." },
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
            "Esempio: Das muss gemacht werden. / Die Tür kann geöffnet werden.",
            "Il modale va in posizione 2 nella frase principale; 'werden' resta alla fine.",
        ],
        theory_en: &[
            "With modal verbs the passive is formed: conjugated modal + Partizip II + werden (infinitive at the end).",
            "Example: Das muss gemacht werden. / Die Tür kann geöffnet werden.",
            "The modal is in position 2 in the main clause; 'werden' stays at the end.",
        ],
        examples: &[
            ("Die Aufgabe muss heute gemacht werden.", "Il compito deve essere fatto oggi."),
            ("Das Fenster kann geöffnet werden.", "La finestra può essere aperta."),
            ("Die Regeln sollen beachtet werden.", "Le regole devono essere rispettate."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: Die Arbeit ___ erledigt werden. (müssen)", prompt_en: "Choose the correct form: Die Arbeit ___ erledigt werden. (müssen)", options: &["muss", "musst", "müssen"], correct_index: 0, answer_text: "", explanation_it: "Soggetto singolare (die Arbeit): muss erledigt werden.", explanation_en: "Singular subject (die Arbeit): muss erledigt werden." },
            GrammarUnitExercise { prompt_it: "Completa: Das ___ gemacht werden. (können)", prompt_en: "Complete: Das ___ gemacht werden. (können)", options: &["kann", "kannst", "können"], correct_index: 0, answer_text: "", explanation_it: "Das -> kann gemacht werden.", explanation_en: "Das -> kann gemacht werden." },
            GrammarUnitExercise { prompt_it: "Qual è l'ordine corretto?", prompt_en: "Which order is correct?", options: &["Das muss werden gemacht.", "Das muss gemacht werden.", "Das gemacht muss werden."], correct_index: 1, answer_text: "", explanation_it: "Modale + Partizip II + werden: muss gemacht werden.", explanation_en: "Modal + Partizip II + werden: muss gemacht werden." },
            GrammarUnitExercise { prompt_it: "Scrivi il modale: Die Tür ___ geschlossen werden. (sollen, singolare)", prompt_en: "Type the modal: Die Tür ___ geschlossen werden. (sollen, singular)", options: &[], correct_index: 0, answer_text: "soll", explanation_it: "Singolare: die Tür soll geschlossen werden.", explanation_en: "Singular: die Tür soll geschlossen werden." },
        ],
    }
}


fn unit_a2_praeteritum() -> GrammarUnit {
    GrammarUnit {
        id: "a2-praeteritum",
        level: "A2",
        title_it: "Il Präteritum",
        title_en: "Präteritum (simple past)",
        subtitle_it: "Il passato della narrazione",
        subtitle_en: "The narrative past tense",
        objective_it: "Usare il Präteritum con verbi regolari, irregolari e modali comuni.",
        objective_en: "Use Präteritum with regular, irregular, and common modal verbs.",
        theory_it: &[
            "Il Präteritum è il passato semplice, frequente nello scritto e nei racconti.",
            "Verbi regolari: radice + -te (machen -> machte). Irregolari: cambio di radice (gehen -> ging).",
            "Forme importanti: sein -> war, haben -> hatte, modali -> konnte, musste, wollte, durfte, sollte.",
        ],
        theory_en: &[
            "Präteritum is the simple past, common in writing and narration.",
            "Regular verbs: stem + -te (machen -> machte). Irregular verbs change the stem (gehen -> ging).",
            "Important forms: sein -> war, haben -> hatte, modals -> konnte, musste, wollte, durfte, sollte.",
        ],
        examples: &[
            ("Ich war gestern im Kino.", "Ieri ero al cinema."),
            ("Wir hatten keine Zeit.", "Non avevamo tempo."),
            ("Er ging nach Berlin.", "Andò a Berlino."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich ___ gestern im Kino. (sein)", prompt_en: "Complete: Ich ___ gestern im Kino. (sein)", options: &["war", "warst", "bin"], correct_index: 0, answer_text: "", explanation_it: "sein al Präteritum: ich war.", explanation_en: "sein in Präteritum: ich war." },
            GrammarUnitExercise { prompt_it: "Completa: Wir ___ keine Zeit. (haben)", prompt_en: "Complete: Wir ___ keine Zeit. (haben)", options: &["hatte", "hatten", "haben"], correct_index: 1, answer_text: "", explanation_it: "haben al Präteritum: wir hatten.", explanation_en: "haben in Präteritum: wir hatten." },
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: Er ___ nach Berlin. (gehen)", prompt_en: "Choose the correct form: Er ___ nach Berlin. (gehen)", options: &["gehte", "ging", "gegangen"], correct_index: 1, answer_text: "", explanation_it: "gehen è irregolare: er ging.", explanation_en: "gehen is irregular: er ging." },
            GrammarUnitExercise { prompt_it: "Scrivi la forma: ich ___ (machen, Präteritum)", prompt_en: "Type the form: ich ___ (machen, Präteritum)", options: &[], correct_index: 0, answer_text: "machte", explanation_it: "Verbo regolare: machen -> machte.", explanation_en: "Regular verb: machen -> machte." },
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
            "Nel discorso indiretto si riporta ciò che dice qualcuno; spesso si usa il Konjunktiv I (sei, habe, komme).",
            "Se il Konjunktiv I coincide con l'indicativo, si può usare il Konjunktiv II per chiarezza.",
            "Domande indirette: ob per sì/no, W-Wort per le altre; il verbo va alla fine.",
        ],
        theory_en: &[
            "In indirect speech you report what someone said; Konjunktiv I is often used (sei, habe, komme).",
            "If Konjunktiv I matches the indicative, Konjunktiv II can be used for clarity.",
            "Indirect questions: ob for yes/no, a W-word for others; the verb goes to the end.",
        ],
        examples: &[
            ("Er sagt, er sei müde.", "Dice che è stanco."),
            ("Sie fragt, ob ich Zeit habe.", "Chiede se ho tempo."),
            ("Er fragt, wo ich wohne.", "Chiede dove abito."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Er sagt, er ___ krank. (sein)", prompt_en: "Complete: Er sagt, er ___ krank. (sein)", options: &["ist", "sei", "war"], correct_index: 1, answer_text: "", explanation_it: "Konjunktiv I di sein: er sei.", explanation_en: "Konjunktiv I of sein: er sei." },
            GrammarUnitExercise { prompt_it: "Completa: Sie sagt, sie ___ keine Zeit. (haben)", prompt_en: "Complete: Sie sagt, sie ___ keine Zeit. (haben)", options: &["hat", "habe", "hatte"], correct_index: 1, answer_text: "", explanation_it: "Konjunktiv I di haben: sie habe.", explanation_en: "Konjunktiv I of haben: sie habe." },
            GrammarUnitExercise { prompt_it: "Domanda indiretta sì/no: Er fragt, ___ ich komme.", prompt_en: "Indirect yes/no question: Er fragt, ___ ich komme.", options: &["dass", "ob", "weil"], correct_index: 1, answer_text: "", explanation_it: "Le domande indirette sì/no usano ob.", explanation_en: "Indirect yes/no questions use ob." },
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
            "L'infinito sostantivato è neutro: das Essen, das Lernen. Suffissi comuni: -ung, -heit, -keit, -schaft.",
            "Spesso con preposizioni: beim Lernen, zum Essen, nach dem Schlafen.",
        ],
        theory_en: &[
            "In German verbs and adjectives can become nouns; nouns are always capitalized.",
            "Nominalized infinitives are neuter: das Essen, das Lernen. Common suffixes: -ung, -heit, -keit, -schaft.",
            "Often used with prepositions: beim Lernen, zum Essen, nach dem Schlafen.",
        ],
        examples: &[
            ("Das Lernen macht Spaß.", "Imparare è divertente."),
            ("Die Prüfung war schwierig.", "L'esame è stato difficile."),
            ("Die Freiheit ist wichtig.", "La libertà è importante."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Scegli la forma corretta: ___ ist wichtig. (lernen)", prompt_en: "Choose the correct form: ___ ist wichtig. (lernen)", options: &["lernen", "Das Lernen", "der Lernen"], correct_index: 1, answer_text: "", explanation_it: "Infinito sostantivato neutro: das Lernen.", explanation_en: "Nominalized infinitive (neuter): das Lernen." },
            GrammarUnitExercise { prompt_it: "Completa: Die ___ war schwierig. (prüfen)", prompt_en: "Complete: Die ___ war schwierig. (prüfen)", options: &["Prüfen", "Prüfung", "Prüfheit"], correct_index: 1, answer_text: "", explanation_it: "Da prüfen: die Prüfung (-ung).", explanation_en: "From prüfen: die Prüfung (-ung)." },
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
        objective_it: "Usare i pronomi riflessivi e i verbi riflessivi più comuni.",
        objective_en: "Use reflexive pronouns and common reflexive verbs.",
        theory_it: &[
            "I verbi riflessivi hanno un pronome che rimanda al soggetto: mich, dich, sich, uns, euch, sich.",
            "La maggior parte usa l'accusativo; con parti del corpo si usa spesso il dativo: Ich wasche mir die Hände.",
            "Verbi frequenti: sich freuen, sich interessieren für, sich treffen, sich beeilen, sich erholen, sich setzen.",
        ],
        theory_en: &[
            "Reflexive verbs have a pronoun referring back to the subject: mich, dich, sich, uns, euch, sich.",
            "Most use the accusative; with body parts the dative is often used: Ich wasche mir die Hände.",
            "Common verbs: sich freuen, sich interessieren für, sich treffen, sich beeilen, sich erholen, sich setzen.",
        ],
        examples: &[
            ("Ich freue mich auf den Urlaub.", "Non vedo l'ora che arrivino le vacanze."),
            ("Wir treffen uns um 8 Uhr.", "Ci incontriamo alle 8."),
            ("Ich wasche mir die Hände.", "Mi lavo le mani."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich freue ___ auf den Urlaub.", prompt_en: "Complete: Ich freue ___ auf den Urlaub.", options: &["mich", "mir", "dich"], correct_index: 0, answer_text: "", explanation_it: "sich freuen all'accusativo: ich freue mich.", explanation_en: "sich freuen takes accusative: ich freue mich." },
            GrammarUnitExercise { prompt_it: "Completa: Wir treffen ___ um 8 Uhr.", prompt_en: "Complete: Wir treffen ___ um 8 Uhr.", options: &["uns", "euch", "sich"], correct_index: 0, answer_text: "", explanation_it: "Con wir il pronome riflessivo è uns.", explanation_en: "With wir the reflexive pronoun is uns." },
            GrammarUnitExercise { prompt_it: "Completa: Er beeilt ___.", prompt_en: "Complete: Er beeilt ___.", options: &["mich", "dich", "sich"], correct_index: 2, answer_text: "", explanation_it: "Con er/sie/es il pronome è sich.", explanation_en: "With er/sie/es the pronoun is sich." },
            GrammarUnitExercise { prompt_it: "Scrivi il pronome: Ich wasche ___ die Hände. (dativo)", prompt_en: "Type the pronoun: Ich wasche ___ die Hände. (dative)", options: &[], correct_index: 0, answer_text: "mir", explanation_it: "Con una parte del corpo il riflessivo è al dativo: mir.", explanation_en: "With a body part the reflexive pronoun is dative: mir." },
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
            "Congiunzioni comuni: weil (perché), dass (che), wenn (se/quando), als (quando, passato singolo), obwohl (sebbene), ob (se, domande indirette).",
            "Se la subordinata viene prima, la principale inizia con il verbo: Weil ich krank bin, bleibe ich zu Hause.",
        ],
        theory_en: &[
            "In a subordinate clause the conjugated verb goes to the end: Ich bleibe zu Hause, weil ich krank bin.",
            "Common conjunctions: weil (because), dass (that), wenn (if/when), als (when, single past event), obwohl (although), ob (whether).",
            "If the subordinate clause comes first, the main clause starts with the verb: Weil ich krank bin, bleibe ich zu Hause.",
        ],
        examples: &[
            ("Ich bleibe zu Hause, weil ich krank bin.", "Resto a casa perché sono malato."),
            ("Er sagt, dass er keine Zeit hat.", "Dice che non ha tempo."),
            ("Als ich klein war, wohnte ich in Rom.", "Quando ero piccolo, abitavo a Roma."),
        ],
        exercises: &[
            GrammarUnitExercise { prompt_it: "Completa: Ich bleibe zu Hause, ___ ich krank bin.", prompt_en: "Complete: Ich bleibe zu Hause, ___ ich krank bin.", options: &["weil", "denn", "aber"], correct_index: 0, answer_text: "", explanation_it: "weil introduce una subordinata con verbo alla fine.", explanation_en: "weil introduces a subordinate clause with the verb at the end." },
            GrammarUnitExercise { prompt_it: "Completa: Er sagt, ___ er keine Zeit hat.", prompt_en: "Complete: Er sagt, ___ he has no time.", options: &["weil", "dass", "wenn"], correct_index: 1, answer_text: "", explanation_it: "dass introduce una frase completiva.", explanation_en: "dass introduces a complement clause." },
            GrammarUnitExercise { prompt_it: "Evento passato singolo: ___ ich klein war, wohnte ich in Rom.", prompt_en: "Single past event: ___ ich klein war, wohnte ich in Rom.", options: &["Wenn", "Als", "Ob"], correct_index: 1, answer_text: "", explanation_it: "Per un singolo evento nel passato si usa als.", explanation_en: "For a single event in the past use als." },
            GrammarUnitExercise { prompt_it: "Scrivi la congiunzione: Ich weiß nicht, ___ er kommt. (domanda indiretta sì/no)", prompt_en: "Type the conjunction: Ich weiß nicht, ___ er kommt. (indirect yes/no question)", options: &[], correct_index: 0, answer_text: "ob", explanation_it: "Le domande indirette sì/no usano ob.", explanation_en: "Indirect yes/no questions use ob." },
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
            ("Der Mann, der dort steht, ist mein Chef.", "L'uomo che sta là è il mio capo."),
            ("Das Buch, das ich lese, ist spannend.", "Il libro che leggo è avvincente."),
            ("Die Frau, der ich helfe, ist meine Nachbarin.", "La donna a cui aiuto è la mia vicina."),
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
        subtitle_it: "Mio, tuo, suo… e come dire «non»",
        subtitle_en: "My, your, his… and how to say “not/no”",
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
            GrammarUnitExercise { prompt_it: "Scegli la negazione corretta: Ich habe ___ Zeit.", prompt_en: "Choose the correct negation: Ich habe ___ Zeit.", options: &["kein", "keine", "nicht"], correct_index: 1, answer_text: "", explanation_it: "Zeit è femminile: keine Zeit.", explanation_en: "Zeit is feminine: keine Zeit." },
            GrammarUnitExercise { prompt_it: "Scrivi il possessivo: Das ist ___ Schwester. (er)", prompt_en: "Type the possessive: Das ist ___ Schwester. (er)", options: &[], correct_index: 0, answer_text: "seine", explanation_it: "Femminile al nominativo: seine Schwester.", explanation_en: "Feminine nominative: seine Schwester." },
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
