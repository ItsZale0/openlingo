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
}

#[derive(Debug, Clone, Default)]
pub struct GrammarMasterySummary {
    pub id: String,
    pub percentuale: i64,
    pub tentativi: i64,
    pub corrette: i64,
    pub completata: bool,
    pub ultima_volta: i64,
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
        }
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
