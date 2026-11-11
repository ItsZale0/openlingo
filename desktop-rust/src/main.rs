mod data;

use chrono::Local;
use data::{Store, WordEntity};
use eframe::egui;

#[derive(Clone, Copy, PartialEq, Eq)]
enum Screen {
    Home,
    Review,
    Vocabulary,
    Progress,
    Settings,
}

struct OpenLingoApp {
    store: Store,
    screen: Screen,
    status: String,
    new_profile_name: String,
    review_index: usize,
    review_queue: Vec<WordEntity>,
    show_answer: bool,
}

impl OpenLingoApp {
    fn new(_cc: &eframe::CreationContext<'_>) -> Self {
        let store = Store::load_default();
        let mut app = Self {
            store,
            screen: Screen::Home,
            status: String::new(),
            new_profile_name: String::new(),
            review_index: 0,
            review_queue: Vec::new(),
            show_answer: false,
        };
        app.refresh_review_queue();
        app
    }

    fn refresh_review_queue(&mut self) {
        let now = chrono::Utc::now().timestamp_millis();
        let mut q = self.store.due_reviews(now, 30);
        if q.is_empty() {
            q = self.store.new_words(20);
        }
        self.review_queue = q;
        self.review_index = 0;
        self.show_answer = false;
    }

    fn top_bar(&mut self, ui: &mut egui::Ui) {
        ui.horizontal(|ui| {
            ui.heading("OpenLingo");
            ui.separator();
            ui.selectable_value(&mut self.screen, Screen::Home, "Home");
            ui.selectable_value(&mut self.screen, Screen::Review, "Review");
            ui.selectable_value(&mut self.screen, Screen::Vocabulary, "Vocabulary");
            ui.selectable_value(&mut self.screen, Screen::Progress, "Progress");
            ui.selectable_value(&mut self.screen, Screen::Settings, "Settings");
            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                if let Some(p) = self.store.active_profile() {
                    ui.label(format!("Profile: {}", p.config.nome_visualizzato));
                } else {
                    ui.label("No profile");
                }
            });
        });
        ui.separator();
    }

    fn screen_home(&mut self, ui: &mut egui::Ui) {
        let (total, reviewed, due, streak) = self.store.stats();
        let name = self.store.active_profile()
            .map(|p| p.config.nome_visualizzato.clone())
            .unwrap_or_else(|| "studente".into());
        ui.heading(format!("Ciao, {}", name));
        ui.label("OpenLingo desktop (Rust rebuild) — language learning with real didactics and spaced repetition.");
        ui.add_space(8.0);
        ui.group(|ui| {
            ui.label(format!("Words: {total}  •  Reviewed: {reviewed}  •  Due now: {due}  •  Streak: {streak} days"));
        });
        ui.add_space(8.0);
        ui.horizontal(|ui| {
            if ui.button("Start review").clicked() {
                self.refresh_review_queue();
                self.screen = Screen::Review;
            }
            if ui.button("Open vocabulary").clicked() {
                self.screen = Screen::Vocabulary;
            }
        });
        if self.store.repo.profili.is_empty() {
            ui.add_space(12.0);
            ui.colored_label(egui::Color32::YELLOW, "No profiles found. Create one in Settings to get started.");
        }
        if !self.status.is_empty() {
            ui.add_space(8.0);
            ui.label(&self.status);
        }
    }

    fn answer_current(&mut self, saputa: bool) {
        if self.review_queue.is_empty() { return; }
        let idx = self.review_index.min(self.review_queue.len().saturating_sub(1));
        let word_id = self.review_queue[idx].id.clone();
        match self.store.answer_review(&word_id, saputa) {
            Ok(_) => {
                self.status = if saputa { "Saved: knew it".into() } else { "Saved: didn't know".into() };
                self.review_queue.remove(idx);
                if self.review_index >= self.review_queue.len() && self.review_index > 0 {
                    self.review_index -= 1;
                }
                self.show_answer = false;
                if self.review_queue.is_empty() {
                    self.refresh_review_queue();
                }
            }
            Err(e) => self.status = format!("Save error: {e}"),
        }
    }

    fn screen_review(&mut self, ui: &mut egui::Ui) {
        ui.heading("Review");
        if self.review_queue.is_empty() {
            ui.label("No words due right now. Add words or sync data in the original app/profile data.");
            if ui.button("Refresh").clicked() {
                self.refresh_review_queue();
            }
            return;
        }
        let idx = self.review_index.min(self.review_queue.len().saturating_sub(1));
        let total = self.review_queue.len();
        let word = self.review_queue[idx].clone();
        ui.label(format!("Card {} / {}", idx + 1, total));
        ui.add_space(8.0);
        ui.group(|ui| {
            ui.heading(word.label());
            ui.label(format!("Level: {}", if word.level.is_empty() { "-" } else { &word.level }));
            if self.show_answer {
                ui.add_space(6.0);
                ui.label(format!("Meaning: {}", word.italian));
                if !word.example.trim().is_empty() {
                    ui.label(format!("Example: {}", word.example));
                }
            } else {
                ui.add_space(6.0);
                ui.label("Think of the meaning, then reveal the answer.");
            }
        });
        ui.add_space(8.0);
        ui.horizontal(|ui| {
            if ui.button(if self.show_answer { "Hide answer" } else { "Show answer" }).clicked() {
                self.show_answer = !self.show_answer;
            }
            if ui.button("Knew it").clicked() {
                self.answer_current(true);
            }
            if ui.button("Didn't know").clicked() {
                self.answer_current(false);
            }
            if ui.button("Skip").clicked() {
                if self.review_index + 1 < self.review_queue.len() {
                    self.review_index += 1;
                } else {
                    self.review_index = 0;
                }
                self.show_answer = false;
            }
            if ui.button("Refresh queue").clicked() {
                self.refresh_review_queue();
            }
        });
        ui.add_space(6.0);
        ui.small("SRS-compatible answers: saves review card, log, streak and due date (compatible with existing profile data).");
        if !self.status.is_empty() {
            ui.add_space(4.0);
            ui.label(&self.status);
        }
    }

    fn screen_vocabulary(&mut self, ui: &mut egui::Ui) {
        ui.heading("Vocabulary");
        let Some(p) = self.store.active_profile() else {
            ui.label("No active profile.");
            return;
        };
        ui.label(format!("{} words in profile", p.stato.parole.len()));
        ui.add_space(6.0);
        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            for w in p.stato.parole.iter().take(500) {
                ui.horizontal(|ui| {
                    ui.strong(w.label());
                    ui.label("—");
                    ui.label(&w.italian);
                    if !w.level.is_empty() {
                        ui.small(format!("[{}]", w.level));
                    }
                });
                if !w.example.trim().is_empty() {
                    ui.small(format!("  {}", w.example));
                }
                ui.separator();
            }
            if p.stato.parole.len() > 500 {
                ui.label(format!("… and {} more", p.stato.parole.len() - 500));
            }
        });
    }

    fn screen_progress(&mut self, ui: &mut egui::Ui) {
        ui.heading("Progress");
        let Some(p) = self.store.active_profile() else {
            ui.label("No active profile.");
            return;
        };
        let (total, reviewed, due, streak) = self.store.stats();
        ui.label(format!("Profile: {}", p.config.nome_visualizzato));
        ui.label(format!("Level: {}  •  Goal: {}", p.stato.progresso.livello_corrente, p.stato.progresso.obiettivo_livello));
        ui.add_space(6.0);
        ui.group(|ui| {
            ui.label(format!("Total words: {total}"));
            ui.label(format!("Reviewed words: {reviewed}"));
            ui.label(format!("Due now: {due}"));
            ui.label(format!("Streak: {streak} days (record: {})", p.stato.streak_record));
            ui.label(format!("Grammar units tracked: {}", p.stato.padronanza_grammatica.len()));
            ui.label(format!("Review log entries: {}", p.stato.ripassi_log.len()));
            if let Some(day) = &p.stato.ultimo_giorno_studio {
                ui.label(format!("Last study day: {day}"));
            }
        });
        ui.add_space(6.0);
        ui.label(format!("Data folder: {}", self.store.base_dir.display()));
    }

    fn screen_settings(&mut self, ui: &mut egui::Ui) {
        ui.heading("Settings");
        ui.label(format!("Data folder: {}", self.store.base_dir.display()));
        ui.label(format!("Profiles file: {}", self.store.profili_path.display()));
        ui.label(format!("Preferences file: {}", self.store.preferenze_path.display()));
        ui.add_space(8.0);

        ui.group(|ui| {
            ui.label("Profiles");
            let ids: Vec<(String, String)> = self.store.repo.profili.values()
                .map(|p| (p.id.clone(), p.config.nome_visualizzato.clone()))
                .collect();
            let active = self.store.repo.profilo_attivo_id.clone().unwrap_or_default();
            for (id, name) in ids {
                ui.horizontal(|ui| {
                    let selected = id == active;
                    if ui.selectable_label(selected, format!("{name} ({id})")).clicked() {
                        self.store.set_active_profile(&id);
                        let _ = self.store.save();
                        self.refresh_review_queue();
                    }
                });
            }
            ui.add_space(6.0);
            ui.horizontal(|ui| {
                ui.text_edit_singleline(&mut self.new_profile_name);
                if ui.button("Create profile").clicked() {
                    let name = self.new_profile_name.trim().to_string();
                    if !name.is_empty() {
                        self.store.create_profile(&name);
                        self.new_profile_name.clear();
                        self.refresh_review_queue();
                        self.status = "Profile created.".into();
                    }
                }
            });
        });

        ui.add_space(8.0);
        ui.horizontal(|ui| {
            if ui.button("Reload from disk").clicked() {
                self.store = Store::load_default();
                self.refresh_review_queue();
                self.status = format!("Reloaded at {}", Local::now().format("%H:%M:%S"));
            }
            if ui.button("Save").clicked() {
                match self.store.save() {
                    Ok(_) => self.status = "Saved.".into(),
                    Err(e) => self.status = format!("Save error: {e}"),
                }
            }
        });

        ui.add_space(10.0);
        ui.separator();
        ui.small("OpenLingo Rust desktop rebuild: compatible profile data, SRS answer persistence (intervals/streak/log), Home/Review/Vocabulary/Progress/Settings.");
        if !self.status.is_empty() {
            ui.add_space(6.0);
            ui.label(&self.status);
        }
    }
}

impl eframe::App for OpenLingoApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        egui::TopBottomPanel::top("top").show(ctx, |ui| self.top_bar(ui));
        egui::CentralPanel::default().show(ctx, |ui| match self.screen {
            Screen::Home => self.screen_home(ui),
            Screen::Review => self.screen_review(ui),
            Screen::Vocabulary => self.screen_vocabulary(ui),
            Screen::Progress => self.screen_progress(ui),
            Screen::Settings => self.screen_settings(ui),
        });
    }
}

fn main() -> eframe::Result<()> {
    let options = eframe::NativeOptions {
        viewport: egui::ViewportBuilder::default()
            .with_title("OpenLingo (Rust Desktop)")
            .with_inner_size([1100.0, 720.0]),
        ..Default::default()
    };
    eframe::run_native(
        "OpenLingo",
        options,
        Box::new(|cc| Ok(Box::new(OpenLingoApp::new(cc)))),
    )
}

#[cfg(test)]
mod tests {
    use super::data::*;
    use std::collections::HashMap;
    use std::fs;
    use std::time::{SystemTime, UNIX_EPOCH};

    fn temp_dir(name: &str) -> std::path::PathBuf {
        let nanos = SystemTime::now().duration_since(UNIX_EPOCH).unwrap().as_nanos();
        let p = std::env::temp_dir().join(format!("openlingo-{name}-{nanos}"));
        fs::create_dir_all(&p).unwrap();
        p
    }

    #[test]
    fn word_label_uses_article() {
        let w = WordEntity { article: Some("der".into()), german: "Hund".into(), ..Default::default() };
        assert_eq!(w.label(), "der Hund");
        let w2 = WordEntity { german: "gehen".into(), ..Default::default() };
        assert_eq!(w2.label(), "gehen");
    }

    #[test]
    fn store_create_save_reload_profile() {
        let dir = temp_dir("store");
        let mut store = Store::load_from(dir.clone());
        assert!(store.repo.profili.is_empty());
        let id = store.create_profile("Alice");
        assert!(store.repo.profili.contains_key(&id));
        assert_eq!(store.active_profile().unwrap().config.nome_visualizzato, "Alice");

        let store2 = Store::load_from(dir.clone());
        assert_eq!(store2.repo.profilo_attivo_id.as_deref(), Some(id.as_str()));
        assert_eq!(store2.active_profile().unwrap().config.nome_visualizzato, "Alice");
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn due_reviews_filters_and_sorts() {
        let dir = temp_dir("due");
        let mut store = Store::load_from(dir.clone());
        let id = store.create_profile("Bob");
        {
            let p = store.repo.profili.get_mut(&id).unwrap();
            p.stato.parole = vec![
                WordEntity { id: "w1".into(), german: "eins".into(), italian: "uno".into(), ..Default::default() },
                WordEntity { id: "w2".into(), german: "zwei".into(), italian: "due".into(), ..Default::default() },
                WordEntity { id: "w3".into(), german: "drei".into(), italian: "tre".into(), archived: true, ..Default::default() },
            ];
            let mut reviews = HashMap::new();
            reviews.insert("w1".into(), ReviewEntity { word_id: "w1".into(), due_at: 200, repetitions: 1, ..Default::default() });
            reviews.insert("w2".into(), ReviewEntity { word_id: "w2".into(), due_at: 100, repetitions: 1, ..Default::default() });
            reviews.insert("w3".into(), ReviewEntity { word_id: "w3".into(), due_at: 50, repetitions: 1, ..Default::default() });
            p.stato.reviews = reviews;
        }
        let due = store.due_reviews(150, 10);
        assert_eq!(due.len(), 1);
        assert_eq!(due[0].id, "w2"); // w1 due at 200 > 150, w3 archived
        let due_all = store.due_reviews(500, 10);
        assert_eq!(due_all.iter().map(|w| w.id.as_str()).collect::<Vec<_>>(), vec!["w2", "w1"]);
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn loads_compatible_preferenze_shape() {
        let dir = temp_dir("prefs");
        fs::write(dir.join("preferenze.json"), r#"{"temaScuro":true,"temaModalita":"dark","temaColore":"navy","profiloAttivoId":null,"lingua":"en"}"#).unwrap();
        let store = Store::load_from(dir.clone());
        assert!(store.prefs.tema_scuro);
        assert_eq!(store.prefs.tema_modalita, "dark");
        assert_eq!(store.prefs.tema_colore, "navy");
        assert_eq!(store.prefs.lingua, "en");
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn srs_answer_advances_and_saves() {
        let dir = temp_dir("srs");
        let mut store = Store::load_from(dir.clone());
        let id = store.create_profile("Cara");
        {
            let p = store.repo.profili.get_mut(&id).unwrap();
            p.stato.parole = vec![WordEntity { id: "w1".into(), german: "Haus".into(), italian: "casa".into(), ..Default::default() }];
        }
        store.answer_review("w1", true).unwrap();
        {
            let p = store.repo.profili.get(&id).unwrap();
            let r = p.stato.reviews.get("w1").unwrap();
            assert_eq!(r.repetitions, 1);
            assert_eq!(r.interval_days, 1);
            assert_eq!(p.stato.ripassi_log.len(), 1);
            assert!(p.stato.ripassi_log[0].corretta);
            assert!(p.stato.streak_giorni >= 1);
        }
        store.answer_review("w1", false).unwrap();
        let p = store.repo.profili.get(&id).unwrap();
        let r = p.stato.reviews.get("w1").unwrap();
        assert_eq!(r.repetitions, 0);
        assert_eq!(r.lapses, 1);
        assert_eq!(p.stato.ripassi_log.len(), 2);
        // reload persists
        let store2 = Store::load_from(dir.clone());
        assert!(store2.repo.profili.get(&id).unwrap().stato.reviews.contains_key("w1"));
        let _ = fs::remove_dir_all(dir);
    }
}
