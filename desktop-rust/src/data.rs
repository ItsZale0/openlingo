use serde::{Deserialize, Serialize};
use std::collections::{HashMap, HashSet};
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
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiliRepository {
    #[serde(alias = "profiloAttivoId")]
    pub profilo_attivo_id: Option<String>,
    pub profili: HashMap<String, ProfiloUtente>,
    #[serde(alias = "profiliEliminati")]
    pub profili_eliminati: Vec<String>,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiloUtente {
    pub id: String,
    pub config: ProfiloConfig,
    pub stato: ProfiloStato,
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
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ProfiloStato {
    pub parole: Vec<WordEntity>,
    pub reviews: HashMap<String, ReviewEntity>,
    #[serde(alias = "ripassiLog")]
    pub ripassi_log: Vec<RipassoLog>,
    #[serde(alias = "padronanzaGrammatica")]
    pub padronanza_grammatica: HashMap<String, serde_json::Value>,
    #[serde(alias = "streakGiorni")]
    pub streak_giorni: i32,
    #[serde(alias = "ultimoGiornoStudio")]
    pub ultimo_giorno_studio: Option<String>,
    pub progresso: Progresso,
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
}

impl WordEntity {
    pub fn label(&self) -> String {
        match &self.article {
            Some(a) if !a.trim().is_empty() => format!("{} {}", a.trim(), self.german),
            _ => self.german.clone(),
        }
    }
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct ReviewEntity {
    #[serde(alias = "wordId")]
    pub word_id: String,
    #[serde(alias = "dueAt")]
    pub due_at: i64,
    #[serde(default)]
    pub repetitions: i32,
    #[serde(default)]
    pub interval: i32,
    #[serde(alias = "easeFactor", default)]
    pub ease_factor: f32,
}

#[derive(Debug, Clone, Serialize, Deserialize, Default)]
#[serde(default)]
pub struct RipassoLog {
    #[serde(alias = "wordId")]
    pub word_id: String,
    #[serde(default)]
    pub at: i64,
    #[serde(default)]
    pub correct: bool,
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
}

fn default_livello() -> String { "A1".into() }
fn default_obiettivo() -> String { "B2".into() }

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
            },
            stato: ProfiloStato::default(),
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
}

fn read_json<T: for<'de> Deserialize<'de> + Default>(path: &PathBuf) -> Option<T> {
    let data = fs::read_to_string(path).ok()?;
    serde_json::from_str(&data).ok()
}

fn write_json<T: Serialize>(path: &PathBuf, value: &T) -> Result<(), String> {
    let data = serde_json::to_string_pretty(value).map_err(|e| e.to_string())?;
    fs::write(path, data).map_err(|e| e.to_string())
}
