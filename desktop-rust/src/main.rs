mod data;

use chrono::Local;
use data::{Store, WordEntity, ExerciseQuestion, ExerciseKind, build_article_quiz, build_meaning_quiz, build_typing_quiz, build_cloze_quiz, build_mixed_quiz, check_typing_answer};
use eframe::egui;

#[derive(Clone, Copy, PartialEq, Eq)]
enum Screen {
    Home,
    Review,
    Exercises,
    Vocabulary,
    Grammar,
    Progress,
    Settings,
}

#[derive(Clone, Copy, PartialEq, Eq)]
enum ExerciseMode {
    ArticleQuiz,
    MeaningQuiz,
    TypingQuiz,
    ClozeQuiz,
    Mixed,
}

struct OpenLingoApp {
    store: Store,
    screen: Screen,
    status: String,
    new_profile_name: String,
    review_index: usize,
    review_queue: Vec<WordEntity>,
    show_answer: bool,
    vocab_query: String,
    vocab_level: String,
    show_archived_vocab: bool,
    add_german: String,
    add_italian: String,
    add_article: String,
    add_level: String,
    add_example: String,
    exercise_mode: ExerciseMode,
    exercise_level: String,
    exercise_queue: Vec<ExerciseQuestion>,
    exercise_index: usize,
    exercise_answer: String,
    exercise_feedback: String,
    exercise_correct: usize,
    exercise_total: usize,
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
            vocab_query: String::new(),
            vocab_level: "ALL".into(),
            show_archived_vocab: false,
            add_german: String::new(),
            add_italian: String::new(),
            add_article: String::new(),
            add_level: "A1".into(),
            add_example: String::new(),
            exercise_mode: ExerciseMode::ArticleQuiz,
            exercise_level: "A1".into(),
            exercise_queue: Vec::new(),
            exercise_index: 0,
            exercise_answer: String::new(),
            exercise_feedback: String::new(),
            exercise_correct: 0,
            exercise_total: 0,
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
        ui.horizontal_wrapped(|ui| {
            ui.heading("OpenLingo");
            ui.separator();
            ui.selectable_value(&mut self.screen, Screen::Home, "Home");
            ui.selectable_value(&mut self.screen, Screen::Review, "Review");
            ui.selectable_value(&mut self.screen, Screen::Exercises, "Exercises");
            ui.selectable_value(&mut self.screen, Screen::Vocabulary, "Vocabulary");
            ui.selectable_value(&mut self.screen, Screen::Grammar, "Grammar");
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
        ui.horizontal_wrapped(|ui| {
            if ui.button("Start review").clicked() {
                self.refresh_review_queue();
                self.screen = Screen::Review;
            }
            if ui.button("Practice exercises").clicked() {
                self.screen = Screen::Exercises;
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

    fn start_exercise_session(&mut self) {
        let words: Vec<WordEntity> = self.store.active_profile()
            .map(|p| p.stato.parole.clone())
            .unwrap_or_default();
        let seed = chrono::Utc::now().timestamp_millis() as u64;
        self.exercise_queue = match self.exercise_mode {
            ExerciseMode::ArticleQuiz => build_article_quiz(&words, &self.exercise_level, seed, 1000),
            ExerciseMode::MeaningQuiz => build_meaning_quiz(&words, seed, 20),
            ExerciseMode::TypingQuiz => build_typing_quiz(&words, seed, 20),
            ExerciseMode::ClozeQuiz => build_cloze_quiz(&words, seed, 20),
            ExerciseMode::Mixed => build_mixed_quiz(&words, &self.exercise_level, seed, 24),
        };
        self.exercise_index = 0;
        self.exercise_answer.clear();
        self.exercise_feedback.clear();
        self.exercise_correct = 0;
        self.exercise_total = 0;
        if self.exercise_queue.is_empty() {
            self.status = "No words available for this exercise mode/level.".into();
        } else {
            self.status = format!("Started {} questions.", self.exercise_queue.len());
        }
    }

    fn submit_exercise_answer(&mut self, given: String) {
        if self.exercise_index >= self.exercise_queue.len() { return; }
        let q = self.exercise_queue[self.exercise_index].clone();
        let correct = match q.kind {
            ExerciseKind::Typing => check_typing_answer(&q.answer, &given),
            _ => q.answer.trim().eq_ignore_ascii_case(given.trim()),
        };
        self.exercise_total += 1;
        if correct { self.exercise_correct += 1; }
        // persist to SRS/review log when possible
        let _ = self.store.answer_review(&q.word.id, correct);
        self.exercise_feedback = if correct {
            "Correct!".to_string()
        } else {
            format!("Incorrect. Answer: {}", q.answer)
        };
        self.exercise_index += 1;
        self.exercise_answer.clear();
    }

    fn screen_exercises(&mut self, ui: &mut egui::Ui) {
        ui.heading("Exercises / Practice");
        if self.store.active_profile().is_none() {
            ui.label("No active profile.");
            return;
        }
        ui.horizontal_wrapped(|ui| {
            ui.label("Mode:");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::ArticleQuiz, "Article quiz (der/die/das)");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::MeaningQuiz, "Meaning quiz");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::TypingQuiz, "Typing");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::ClozeQuiz, "Cloze (from examples)");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::Mixed, "Mixed session");
            if matches!(self.exercise_mode, ExerciseMode::ArticleQuiz | ExerciseMode::Mixed) {
                ui.label("Level ≤");
                egui::ComboBox::from_id_salt("ex_level")
                    .selected_text(self.exercise_level.clone())
                    .show_ui(ui, |ui| {
                        for opt in ["A1", "A2", "B1", "B2", "C1", "C2"] {
                            ui.selectable_value(&mut self.exercise_level, opt.to_string(), opt);
                        }
                    });
            }
            if ui.button("Start new session").clicked() {
                self.start_exercise_session();
            }
        });
        ui.small("Article quiz uses all active vocabulary words with der/die/das for the selected level and below (deduped, shuffled). Cloze uses word examples. Answers update SRS/review progress.");
        ui.add_space(6.0);

        if self.exercise_queue.is_empty() {
            ui.label("No active exercise session. Choose a mode and start.");
            if !self.status.is_empty() { ui.label(&self.status); }
            return;
        }
        if self.exercise_index >= self.exercise_queue.len() {
            ui.group(|ui| {
                ui.heading("Session complete");
                ui.label(format!("Score: {} / {}", self.exercise_correct, self.exercise_total));
                if self.exercise_total > 0 {
                    let pct = (self.exercise_correct * 100) / self.exercise_total;
                    ui.label(format!("Accuracy: {pct}%"));
                }
            });
            if !self.exercise_feedback.is_empty() { ui.label(&self.exercise_feedback); }
            if ui.button("Restart").clicked() { self.start_exercise_session(); }
            return;
        }

        let q = self.exercise_queue[self.exercise_index].clone();
        ui.label(format!("Question {} / {}", self.exercise_index + 1, self.exercise_queue.len()));
        ui.label(format!("Score: {} / {}", self.exercise_correct, self.exercise_total));
        ui.add_space(6.0);
        ui.group(|ui| {
            ui.strong(&q.prompt);
            match q.kind {
                ExerciseKind::ArticleQuiz | ExerciseKind::MeaningChoice | ExerciseKind::GermanChoice => {
                    for opt in &q.options {
                        if ui.button(opt).clicked() {
                            self.submit_exercise_answer(opt.clone());
                        }
                    }
                }
                ExerciseKind::Typing => {
                    ui.add(egui::TextEdit::singleline(&mut self.exercise_answer).hint_text("Type your answer..."));
                    ui.horizontal(|ui| {
                        if ui.button("Submit").clicked() {
                            let ans = self.exercise_answer.clone();
                            self.submit_exercise_answer(ans);
                        }
                    });
                }
            }
        });
        if !self.exercise_feedback.is_empty() {
            ui.add_space(4.0);
            ui.label(&self.exercise_feedback);
        }
    }

    fn screen_vocabulary(&mut self, ui: &mut egui::Ui) {
        ui.heading("Vocabulary");
        if self.store.active_profile().is_none() {
            ui.label("No active profile.");
            return;
        }

        ui.collapsing("Add word", |ui| {
            ui.horizontal(|ui| {
                ui.label("Article");
                ui.add(egui::TextEdit::singleline(&mut self.add_article).desired_width(50.0));
                ui.label("German*");
                ui.add(egui::TextEdit::singleline(&mut self.add_german).desired_width(140.0));
                ui.label("Meaning*");
                ui.add(egui::TextEdit::singleline(&mut self.add_italian).desired_width(140.0));
                ui.label("Level");
                ui.add(egui::TextEdit::singleline(&mut self.add_level).desired_width(50.0));
            });
            ui.horizontal(|ui| {
                ui.label("Example");
                ui.add(egui::TextEdit::singleline(&mut self.add_example).desired_width(320.0));
                if ui.button("Add").clicked() {
                    let article = if self.add_article.trim().is_empty() { None } else { Some(self.add_article.clone()) };
                    match self.store.add_word(&self.add_german, &self.add_italian, article, &self.add_level, &self.add_example) {
                        Ok(_) => {
                            self.status = "Word added.".into();
                            self.add_german.clear();
                            self.add_italian.clear();
                            self.add_article.clear();
                            self.add_example.clear();
                        }
                        Err(e) => self.status = format!("Add word failed: {e}"),
                    }
                }
            });
        });
        ui.add_space(6.0);

        ui.horizontal(|ui| {
            ui.label("Search");
            ui.add(egui::TextEdit::singleline(&mut self.vocab_query).desired_width(220.0).hint_text("German, meaning, example, tags"));
            ui.label("Level");
            egui::ComboBox::from_id_salt("vocab_level")
                .selected_text(self.vocab_level.clone())
                .show_ui(ui, |ui| {
                    for opt in ["ALL", "A1", "A2", "B1", "B2", "C1", "C2", "—"] {
                        ui.selectable_value(&mut self.vocab_level, opt.to_string(), opt);
                    }
                });
            ui.checkbox(&mut self.show_archived_vocab, "Show archived");
        });

        let words = self.store.search_words(&self.vocab_query, Some(&self.vocab_level), self.show_archived_vocab, 500);
        let total = self.store.active_profile().map(|p| p.stato.parole.len()).unwrap_or(0);
        ui.label(format!("Showing {} of {} words", words.len(), total));
        if !self.status.is_empty() {
            ui.small(&self.status);
        }
        ui.add_space(4.0);

        let mut archive_toggle: Option<(String, bool)> = None;
        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            for w in &words {
                ui.horizontal(|ui| {
                    ui.strong(w.label());
                    ui.label("—");
                    ui.label(&w.italian);
                    if !w.level.is_empty() {
                        ui.small(format!("[{}]", w.level));
                    }
                    if w.archived {
                        ui.colored_label(egui::Color32::GRAY, "(archived)");
                    }
                    ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                        let label = if w.archived { "Restore" } else { "Archive" };
                        if ui.small_button(label).clicked() {
                            archive_toggle = Some((w.id.clone(), !w.archived));
                        }
                    });
                });
                if !w.example.trim().is_empty() {
                    ui.small(format!("  {}", w.example));
                }
                ui.separator();
            }
            if words.is_empty() {
                ui.label("No words match the current filters.");
            }
        });
        if let Some((id, archived)) = archive_toggle {
            match self.store.set_word_archived(&id, archived) {
                Ok(_) => self.status = if archived { "Word archived.".into() } else { "Word restored.".into() },
                Err(e) => self.status = format!("Update failed: {e}"),
            }
        }
    }

    fn screen_grammar(&mut self, ui: &mut egui::Ui) {
        ui.heading("Grammar mastery");
        if self.store.active_profile().is_none() {
            ui.label("No active profile.");
            return;
        }
        let summaries = self.store.grammar_mastery_summaries();
        ui.label(format!("Tracked units: {}", summaries.len()));
        ui.add_space(6.0);
        if summaries.is_empty() {
            ui.label("No grammar mastery data yet. Complete grammar exercises in the main app to populate this view.");
            return;
        }
        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            for s in &summaries {
                ui.group(|ui| {
                    ui.horizontal(|ui| {
                        ui.strong(&s.id);
                        let state = if s.completata { "completed" } else if s.percentuale >= 80 { "mastered" } else if s.tentativi > 0 { "in progress" } else { "new" };
                        ui.label(format!("• {state}"));
                    });
                    ui.label(format!("Score: {}%  •  Attempts: {}  •  Correct: {}", s.percentuale, s.tentativi, s.corrette));
                    if s.ultima_volta > 0 {
                        if let Some(dt) = chrono::DateTime::<chrono::Utc>::from_timestamp_millis(s.ultima_volta) {
                            ui.small(format!("Last activity: {}", dt.with_timezone(&chrono::Local).format("%Y-%m-%d %H:%M")));
                        }
                    }
                });
                ui.add_space(4.0);
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
        let mature = self.store.mature_count();
        let levels = self.store.word_level_counts();
        let grammar_total = p.stato.padronanza_grammatica.len();
        let grammar_done = self.store.grammar_mastery_summaries().iter().filter(|s| s.completata || s.percentuale >= 80).count();
        ui.label(format!("Profile: {}", p.config.nome_visualizzato));
        ui.label(format!("Level: {}  •  Goal: {}", p.stato.progresso.livello_corrente, p.stato.progresso.obiettivo_livello));
        ui.add_space(6.0);
        ui.group(|ui| {
            ui.label(format!("Total active words: {total}"));
            ui.label(format!("Reviewed words: {reviewed}  •  Mature/strong: {mature}"));
            ui.label(format!("Due now: {due}"));
            ui.label(format!("Streak: {streak} days (record: {})", p.stato.streak_record));
            ui.label(format!("Grammar units tracked: {grammar_total}  •  Completed/mastered: {grammar_done}"));
            ui.label(format!("Review log entries: {}", p.stato.ripassi_log.len()));
            if let Some(day) = &p.stato.ultimo_giorno_studio {
                ui.label(format!("Last study day: {day}"));
            }
        });
        ui.add_space(8.0);
        ui.heading("Words by level");
        if levels.is_empty() {
            ui.label("No words yet.");
        } else {
            for (lvl, count) in levels {
                ui.label(format!("{lvl}: {count}"));
            }
        }
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
        ui.small("OpenLingo Rust desktop rebuild: compatible profile data, SRS persistence, vocabulary search/add/archive, grammar mastery view, progress stats (Home/Review/Vocabulary/Grammar/Progress/Settings).");
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
            Screen::Exercises => self.screen_exercises(ui),
            Screen::Vocabulary => self.screen_vocabulary(ui),
            Screen::Grammar => self.screen_grammar(ui),
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

    #[test]
    fn vocab_search_add_and_archive() {
        let dir = temp_dir("vocab");
        let mut store = Store::load_from(dir.clone());
        let id = store.create_profile("Vera");
        let wid = store.add_word("Hund", "cane", Some("der".into()), "a1", "Der Hund bellt.").unwrap();
        {
            let p = store.repo.profili.get(&id).unwrap();
            assert!(p.stato.parole.iter().any(|w| w.id == wid));
        }
        let found = store.search_words("hund", Some("A1"), false, 10);
        assert_eq!(found.len(), 1);
        assert_eq!(found[0].label(), "der Hund");
        let none = store.search_words("katze", Some("ALL"), false, 10);
        assert!(none.is_empty());
        store.set_word_archived(&wid, true).unwrap();
        assert!(store.search_words("hund", Some("ALL"), false, 10).is_empty());
        assert_eq!(store.search_words("hund", Some("ALL"), true, 10).len(), 1);
        let store2 = Store::load_from(dir.clone());
        assert_eq!(store2.search_words("hund", Some("ALL"), true, 10).len(), 1);
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn grammar_mastery_summary_parses_values() {
        let v = serde_json::json!({
            "unitaId": "praesens",
            "percentuale": 85,
            "completata": true,
            "tentativi": 4,
            "corrette": 17,
            "ultimaVolta": 1710000000000i64
        });
        let s = GrammarMasterySummary::from_value("praesens", &v);
        assert_eq!(s.id, "praesens");
        assert_eq!(s.percentuale, 85);
        assert!(s.completata);
        assert_eq!(s.tentativi, 4);
        assert_eq!(s.corrette, 17);
        assert_eq!(s.ultima_volta, 1710000000000);
    }

    #[test]
    fn preserves_unknown_profile_fields_on_roundtrip() {
        let dir = temp_dir("compat");
        let json = serde_json::json!({
            "profiloAttivoId": "p1",
            "profili": {
                "p1": {
                    "id": "p1",
                    "config": {
                        "nomeVisualizzato": "Test",
                        "livelloIniziale": "A1",
                        "futureConfigField": "keep-me"
                    },
                    "stato": {
                        "parole": [{"id":"w1","german":"Haus","italian":"casa","source":"SYNCED"}],
                        "reviews": {},
                        "ripassiLog": [],
                        "padronanzaGrammatica": {"u1": {"percentuale": 50, "completata": false}},
                        "streakGiorni": 2,
                        "streakRecord": 3,
                        "ultimoGiornoStudio": null,
                        "progresso": {"livelloCorrente":"A1","obiettivoLivello":"B2","lezioneCorrente":1,"ultimaAttivita":0},
                        "lezioni": [{"id":"lez1","special":"must-keep"}],
                        "testGrammatica": [{"id":"t1"}]
                    },
                    "futureProfileField": 123
                }
            },
            "profiliEliminati": []
        });
        fs::write(dir.join("profili.json"), serde_json::to_string_pretty(&json).unwrap()).unwrap();
        fs::write(dir.join("preferenze.json"), "{}").unwrap();
        let store = Store::load_from(dir.clone());
        store.save().unwrap();
        let saved: serde_json::Value = serde_json::from_str(&fs::read_to_string(dir.join("profili.json")).unwrap()).unwrap();
        let stato = &saved["profili"]["p1"]["stato"];
        assert_eq!(stato["lezioni"][0]["special"], "must-keep");
        assert_eq!(stato["testGrammatica"][0]["id"], "t1");
        assert_eq!(saved["profili"]["p1"]["config"]["futureConfigField"], "keep-me");
        assert_eq!(saved["profili"]["p1"]["futureProfileField"], 123);
        assert_eq!(stato["parole"][0]["source"], "SYNCED");
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn article_quiz_uses_level_words_deduped() {
        let words = vec![
            WordEntity { id:"1".into(), german:"Hund".into(), italian:"cane".into(), article:Some("der".into()), level:"A1".into(), ..Default::default() },
            WordEntity { id:"2".into(), german:"Katze".into(), italian:"gatto".into(), article:Some("die".into()), level:"A1".into(), ..Default::default() },
            WordEntity { id:"3".into(), german:"Haus".into(), italian:"casa".into(), article:Some("das".into()), level:"A2".into(), ..Default::default() },
            WordEntity { id:"4".into(), german:"Auto".into(), italian:"auto".into(), article:Some("das".into()), level:"B1".into(), ..Default::default() },
            WordEntity { id:"5".into(), german:"Hund".into(), italian:"cane".into(), article:Some("der".into()), level:"A1".into(), ..Default::default() }, // dup
            WordEntity { id:"6".into(), german:"gehen".into(), italian:"andare".into(), article:None, level:"A1".into(), ..Default::default() },
        ];
        let q = build_article_quiz(&words, "A1", 42, 50);
        assert_eq!(q.len(), 2); // Hund, Katze only (A1, with article, deduped)
        assert!(q.iter().all(|x| ["der","die","das"].contains(&x.answer.as_str())));
        let q2 = build_article_quiz(&words, "A2", 42, 50);
        assert_eq!(q2.len(), 3); // includes Haus
    }

    #[test]
    fn typing_check_is_case_and_space_insensitive() {
        assert!(check_typing_answer("der Hund", "Der   hund"));
        assert!(check_typing_answer("gehen", " gehen "));
        assert!(!check_typing_answer("der Hund", "die Hund"));
    }

    #[test]
    fn cloze_quiz_builds_from_examples() {
        let words = vec![
            WordEntity { id:"1".into(), german:"Hund".into(), italian:"cane".into(), article:Some("der".into()), level:"A1".into(), example:"Der Hund spielt.".into(), ..Default::default() },
            WordEntity { id:"2".into(), german:"Katze".into(), italian:"gatto".into(), article:Some("die".into()), level:"A1".into(), example:"".into(), ..Default::default() },
        ];
        let qs = build_cloze_quiz(&words, 7, 10);
        assert_eq!(qs.len(), 1);
        assert!(qs[0].prompt.contains("_____"));
        assert_eq!(qs[0].answer, "Hund");
    }
}
