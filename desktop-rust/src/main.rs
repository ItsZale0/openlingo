mod data;

use chrono::Local;
use data::{Store, WordEntity, ExerciseQuestion, ExerciseKind, build_article_quiz, build_meaning_quiz, build_german_choice_quiz, build_typing_quiz, build_cloze_quiz, build_mixed_quiz, build_guided_session, check_typing_answer, curriculum_lessons, lesson_topic};
use eframe::egui;

fn hex(c: u32) -> egui::Color32 {
    egui::Color32::from_rgb(((c >> 16) & 0xFF) as u8, ((c >> 8) & 0xFF) as u8, (c & 0xFF) as u8)
}

fn effective_dark(store: &Store, system_dark: bool) -> bool {
    match store.prefs.tema_modalita.trim().to_lowercase().as_str() {
        "light" | "chiaro" => false,
        "dark" | "scuro" => true,
        _ => system_dark || store.prefs.tema_scuro,
    }
}

fn apply_theme(ctx: &egui::Context, store: &Store, system_dark: bool) {
    let dark = effective_dark(store, system_dark);
    let id = store.prefs.tema_colore.trim().to_lowercase();
    let (primary, on_primary, primary_container, on_primary_container, secondary_container, on_secondary_container, background, on_background, surface, on_surface, surface_variant, on_surface_variant, outline) = if dark {
        match id.as_str() {
            "navy" => (hex(0xB4C5FF), hex(0x002A78), hex(0x003F9F), hex(0xDCE4FF), hex(0x3E4659), hex(0xDAE2F9), hex(0x0D1220), hex(0xE1E2EC), hex(0x11162A), hex(0xE1E2EC), hex(0x44464F), hex(0xC4C6D0), hex(0x8E9099)),
            "indigo" => (hex(0xC4B5FD), hex(0x2E1065), hex(0x4C1D95), hex(0xEDE9FE), hex(0x474060), hex(0xE7E0FF), hex(0x0F0D1A), hex(0xE6E1F4), hex(0x13111F), hex(0xE6E1F4), hex(0x49454F), hex(0xCBC4CF), hex(0x948F99)),
            "forest" => (hex(0x86EFAC), hex(0x052E16), hex(0x14532D), hex(0xDCFCE7), hex(0x344E3C), hex(0xD0E8D6), hex(0x0A120D), hex(0xDEE5E0), hex(0x0E1611), hex(0xDEE5E0), hex(0x3F4942), hex(0xBFC9C2), hex(0x89948C)),
            "rose" => (hex(0xFDA4AF), hex(0x4C0519), hex(0x881337), hex(0xFFE4E6), hex(0x53393D), hex(0xF5DCE0), hex(0x130A0D), hex(0xEEE0E3), hex(0x170E11), hex(0xEEE0E3), hex(0x524347), hex(0xD7C1C5), hex(0xA08C90)),
            "amber" => (hex(0xFCD34D), hex(0x451A03), hex(0x78350F), hex(0xFEF3C7), hex(0x4D4330), hex(0xF1E5CC), hex(0x12100A), hex(0xEAE4D8), hex(0x16140E), hex(0xEAE4D8), hex(0x4F493D), hex(0xD2C9B8), hex(0x9B9384)),
            _ => (hex(0x8FD6CD), hex(0x003733), hex(0x17433F), hex(0xB8F0E8), hex(0x334B45), hex(0xD0E8E0), hex(0x0D1413), hex(0xDEE5E1), hex(0x111817), hex(0xDEE5E1), hex(0x3F4946), hex(0xBFC9C4), hex(0x89948F)), // teal
        }
    } else {
        match id.as_str() {
            "navy" => (hex(0x003F9F), hex(0xFFFFFF), hex(0xDCE4FF), hex(0x00174B), hex(0xDAE2F9), hex(0x131B2C), hex(0xF8F9FC), hex(0x1A1C20), hex(0xFDFDFF), hex(0x1A1C20), hex(0xE1E2EC), hex(0x44474E), hex(0x74777F)),
            "indigo" => (hex(0x4C1D95), hex(0xFFFFFF), hex(0xEDE9FE), hex(0x2E1065), hex(0xE7E0FF), hex(0x1B1628), hex(0xF9F7FC), hex(0x1C1A20), hex(0xFDFBFF), hex(0x1C1A20), hex(0xE8E3EE), hex(0x49454F), hex(0x7A757F)),
            "forest" => (hex(0x14532D), hex(0xFFFFFF), hex(0xDCFCE7), hex(0x052E16), hex(0xD0E8D6), hex(0x0F2015), hex(0xF6FAF7), hex(0x191C1A), hex(0xFBFDFA), hex(0x191C1A), hex(0xDFE5E0), hex(0x444A44), hex(0x757B75)),
            "rose" => (hex(0x881337), hex(0xFFFFFF), hex(0xFFE4E6), hex(0x4C0519), hex(0xF5DCE0), hex(0x241517), hex(0xFBF6F7), hex(0x1D191A), hex(0xFDF9FA), hex(0x1D191A), hex(0xEDDFE2), hex(0x524347), hex(0x857377)),
            "amber" => (hex(0x78350F), hex(0xFFFFFF), hex(0xFEF3C7), hex(0x451A03), hex(0xF1E5CC), hex(0x201B10), hex(0xFBF9F4), hex(0x1C1A16), hex(0xFDFBF6), hex(0x1C1A16), hex(0xECE5D8), hex(0x4F493D), hex(0x807869)),
            _ => (hex(0x1F5F5B), hex(0xFFFFFF), hex(0xD6EFEA), hex(0x0A2522), hex(0xE2EBE7), hex(0x15201D), hex(0xF7FAF8), hex(0x191C1B), hex(0xFBFDFB), hex(0x191C1B), hex(0xE1E8E4), hex(0x44504C), hex(0x70807A)), // teal
        }
    };
    let mut visuals = if dark { egui::Visuals::dark() } else { egui::Visuals::light() };
    visuals.override_text_color = Some(on_background);
    visuals.panel_fill = background;
    visuals.window_fill = surface;
    visuals.extreme_bg_color = surface_variant;
    visuals.faint_bg_color = secondary_container;
    visuals.code_bg_color = surface_variant;
    visuals.hyperlink_color = primary;
    visuals.selection.bg_fill = primary_container;
    visuals.selection.stroke = egui::Stroke::new(1.0_f32, primary);
    visuals.widgets.noninteractive.bg_fill = surface;
    visuals.widgets.noninteractive.fg_stroke = egui::Stroke::new(1.0_f32, on_surface);
    visuals.widgets.noninteractive.bg_stroke = egui::Stroke::new(1.0_f32, outline);
    visuals.widgets.inactive.bg_fill = secondary_container;
    visuals.widgets.inactive.fg_stroke = egui::Stroke::new(1.0_f32, on_secondary_container);
    visuals.widgets.inactive.bg_stroke = egui::Stroke::new(1.0_f32, outline);
    visuals.widgets.hovered.bg_fill = primary_container;
    visuals.widgets.hovered.fg_stroke = egui::Stroke::new(1.0_f32, on_primary_container);
    visuals.widgets.hovered.bg_stroke = egui::Stroke::new(1.0_f32, primary);
    visuals.widgets.active.bg_fill = primary;
    visuals.widgets.active.fg_stroke = egui::Stroke::new(1.0_f32, on_primary);
    visuals.widgets.active.bg_stroke = egui::Stroke::new(1.0_f32, primary);
    visuals.widgets.open.bg_fill = secondary_container;
    visuals.widgets.open.fg_stroke = egui::Stroke::new(1.0_f32, on_secondary_container);
    visuals.widgets.open.bg_stroke = egui::Stroke::new(1.0_f32, primary);
    let rounding = egui::CornerRadius::same(12);
    visuals.widgets.noninteractive.corner_radius = rounding;
    visuals.widgets.inactive.corner_radius = rounding;
    visuals.widgets.hovered.corner_radius = rounding;
    visuals.widgets.active.corner_radius = rounding;
    visuals.widgets.open.corner_radius = rounding;
    visuals.window_corner_radius = egui::CornerRadius::same(18);
    visuals.menu_corner_radius = egui::CornerRadius::same(12);
    ctx.set_visuals(visuals);

    let mut style = (*ctx.style()).clone();
    style.spacing.item_spacing = egui::vec2(8.0, 8.0);
    style.spacing.button_padding = egui::vec2(10.0, 6.0);
    style.spacing.interact_size = egui::vec2(40.0, 28.0);
    ctx.set_style(style);
}

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
    GermanQuiz,
    TypingQuiz,
    ClozeQuiz,
    Mixed,
    GuidedSession,
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
    backup_path: String,
    grammar_unit_id: Option<String>,
    grammar_practice_index: usize,
    grammar_practice_order: Vec<usize>,
    grammar_practice_correct: usize,
    grammar_practice_answered: bool,
    grammar_practice_feedback: String,
    grammar_practice_input: String,
    grammar_practice_done: Option<(i32, bool)>, // (percent, completed)
}

fn ui_lang(store: &Store) -> &str {
    let l = store.prefs.lingua.trim().to_lowercase();
    if l == "it" { "it" } else { "en" }
}

fn tr<'a>(lang: &str, en: &'a str, it: &'a str) -> &'a str {
    if lang == "it" { it } else { en }
}

fn shuffled_indices(n: usize, seed: u64) -> Vec<usize> {
    let mut v: Vec<usize> = (0..n).collect();
    if n <= 1 { return v; }
    let mut s = seed ^ 0x9E3779B97F4A7C15;
    for i in (1..v.len()).rev() {
        s ^= s << 13;
        s ^= s >> 7;
        s ^= s << 17;
        let j = (s as usize) % (i + 1);
        v.swap(i, j);
    }
    v
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
            backup_path: String::new(),
            grammar_unit_id: None,
            grammar_practice_index: 0,
            grammar_practice_order: Vec::new(),
            grammar_practice_correct: 0,
            grammar_practice_answered: false,
            grammar_practice_feedback: String::new(),
            grammar_practice_input: String::new(),
            grammar_practice_done: None,
        };
        app.backup_path = app.store.base_dir.join("openlingo-backup.json").to_string_lossy().to_string();
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
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        ui.horizontal_wrapped(|ui| {
            ui.heading("OpenLingo");
            ui.separator();
            ui.selectable_value(&mut self.screen, Screen::Home, tr(lang, "Home", "Home"));
            ui.selectable_value(&mut self.screen, Screen::Review, tr(lang, "Review", "Ripasso"));
            ui.selectable_value(&mut self.screen, Screen::Exercises, tr(lang, "Exercises", "Esercizi"));
            ui.selectable_value(&mut self.screen, Screen::Vocabulary, tr(lang, "Vocabulary", "Vocabolario"));
            ui.selectable_value(&mut self.screen, Screen::Grammar, tr(lang, "Grammar", "Grammatica"));
            ui.selectable_value(&mut self.screen, Screen::Progress, tr(lang, "Progress", "Progressi"));
            ui.selectable_value(&mut self.screen, Screen::Settings, tr(lang, "Settings", "Impostazioni"));
            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                if let Some(p) = self.store.active_profile() {
                    ui.label(format!("{}: {}", tr(lang, "Profile", "Profilo"), p.config.nome_visualizzato));
                } else {
                    ui.label(tr(lang, "No profile", "Nessun profilo"));
                }
            });
        });
        ui.separator();
    }

    fn screen_home(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        let (total, reviewed, due, streak) = self.store.stats();
        let name = self.store.active_profile()
            .map(|p| p.config.nome_visualizzato.clone())
            .unwrap_or_else(|| tr(lang, "learner", "studente").into());
        ui.heading(format!("{}, {}", tr(lang, "Hello", "Ciao"), name));
        ui.label(tr(lang,
            "OpenLingo desktop (Rust rebuild) — language learning with real didactics and spaced repetition.",
            "OpenLingo desktop (rebuild in Rust) — apprendimento linguistico con didattica reale e ripetizione dilazionata."));
        ui.add_space(8.0);
        ui.group(|ui| {
            ui.label(if lang == "it" {
                format!("Parole: {total}  •  Ripassate: {reviewed}  •  Da ripassare ora: {due}  •  Serie: {streak} giorni")
            } else {
                format!("Words: {total}  •  Reviewed: {reviewed}  •  Due now: {due}  •  Streak: {streak} days")
            });
        });
        ui.add_space(8.0);
        ui.horizontal_wrapped(|ui| {
            if ui.button(tr(lang, "Start review", "Inizia ripasso")).clicked() {
                self.refresh_review_queue();
                self.screen = Screen::Review;
            }
            if ui.button(tr(lang, "Practice exercises", "Esercitati")).clicked() {
                self.screen = Screen::Exercises;
            }
            if ui.button(tr(lang, "Open vocabulary", "Apri vocabolario")).clicked() {
                self.screen = Screen::Vocabulary;
            }
        });
        if self.store.repo.profili.is_empty() {
            ui.add_space(12.0);
            ui.colored_label(egui::Color32::YELLOW, tr(lang,
                "No profiles found. Create one in Settings to get started.",
                "Nessun profilo trovato. Creane uno in Impostazioni per iniziare."));
        }
        if !self.status.is_empty() {
            ui.add_space(8.0);
            ui.label(&self.status);
        }
    }

    fn answer_current(&mut self, saputa: bool) {
        if self.review_queue.is_empty() { return; }
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        let idx = self.review_index.min(self.review_queue.len().saturating_sub(1));
        let word_id = self.review_queue[idx].id.clone();
        match self.store.answer_review(&word_id, saputa) {
            Ok(_) => {
                self.status = if saputa { tr(lang, "Saved: knew it", "Salvato: la sapevo").into() } else { tr(lang, "Saved: didn't know", "Salvato: non la sapevo").into() };
                self.review_queue.remove(idx);
                if self.review_index >= self.review_queue.len() && self.review_index > 0 {
                    self.review_index -= 1;
                }
                self.show_answer = false;
                if self.review_queue.is_empty() {
                    self.refresh_review_queue();
                }
            }
            Err(e) => self.status = format!("{}: {e}", tr(lang, "Save error", "Errore salvataggio")),
        }
    }

    fn screen_review(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        ui.heading(tr(lang, "Review", "Ripasso"));
        if self.review_queue.is_empty() {
            ui.label(tr(lang,
                "No words due right now. Add words or sync data in the original app/profile data.",
                "Nessuna parola da ripassare ora. Aggiungi parole o sincronizza i dati nell'app/profilo originale."));
            if ui.button(tr(lang, "Refresh", "Aggiorna")).clicked() {
                self.refresh_review_queue();
            }
            return;
        }
        let idx = self.review_index.min(self.review_queue.len().saturating_sub(1));
        let total = self.review_queue.len();
        let word = self.review_queue[idx].clone();
        ui.label(if lang == "it" { format!("Scheda {} / {}", idx + 1, total) } else { format!("Card {} / {}", idx + 1, total) });
        ui.add_space(8.0);
        ui.group(|ui| {
            ui.heading(word.label());
            ui.label(format!("{}: {}", tr(lang, "Level", "Livello"), if word.level.is_empty() { "-" } else { &word.level }));
            if self.show_answer {
                ui.add_space(6.0);
                ui.label(format!("{}: {}", tr(lang, "Meaning", "Significato"), word.italian));
                if !word.example.trim().is_empty() {
                    ui.label(format!("{}: {}", tr(lang, "Example", "Esempio"), word.example));
                }
            } else {
                ui.add_space(6.0);
                ui.label(tr(lang, "Think of the meaning, then reveal the answer.", "Pensa al significato, poi mostra la risposta."));
            }
        });
        ui.add_space(8.0);
        ui.horizontal(|ui| {
            let show_lbl = if self.show_answer { tr(lang, "Hide answer", "Nascondi risposta") } else { tr(lang, "Show answer", "Mostra risposta") };
            if ui.button(show_lbl).clicked() {
                self.show_answer = !self.show_answer;
            }
            if ui.button(tr(lang, "Knew it", "La sapevo")).clicked() {
                self.answer_current(true);
            }
            if ui.button(tr(lang, "Didn't know", "Non la sapevo")).clicked() {
                self.answer_current(false);
            }
            if ui.button(tr(lang, "Skip", "Salta")).clicked() {
                if self.review_index + 1 < self.review_queue.len() {
                    self.review_index += 1;
                } else {
                    self.review_index = 0;
                }
                self.show_answer = false;
            }
            if ui.button(tr(lang, "Refresh queue", "Aggiorna coda")).clicked() {
                self.refresh_review_queue();
            }
        });
        ui.add_space(6.0);
        ui.small(tr(lang,
            "SRS-compatible answers: saves review card, log, streak and due date (compatible with existing profile data).",
            "Risposte compatibili SRS: salva scheda di ripasso, log, serie e scadenza (compatibile con i dati profilo esistenti)."));
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
            ExerciseMode::GermanQuiz => build_german_choice_quiz(&words, seed, 20),
            ExerciseMode::TypingQuiz => build_typing_quiz(&words, seed, 20),
            ExerciseMode::ClozeQuiz => build_cloze_quiz(&words, seed, 20),
            ExerciseMode::Mixed => build_mixed_quiz(&words, &self.exercise_level, seed, 24),
            ExerciseMode::GuidedSession => {
                let now = chrono::Utc::now().timestamp_millis();
                let mut pool = self.store.due_reviews(now, 30);
                let mut neww = self.store.new_words(20);
                pool.append(&mut neww);
                if pool.is_empty() { pool = words.clone(); }
                build_guided_session(&pool, &self.exercise_level, seed, 24)
            }
        };
        self.exercise_index = 0;
        self.exercise_answer.clear();
        self.exercise_feedback.clear();
        self.exercise_correct = 0;
        self.exercise_total = 0;
        if self.exercise_queue.is_empty() {
            let lang = ui_lang(&self.store);
            self.status = tr(lang, "No words available for this exercise mode/level.", "Nessuna parola disponibile per questa modalità/livello.").into();
        } else {
            let lang = ui_lang(&self.store);
            self.status = if lang == "it" { format!("Avviate {} domande.", self.exercise_queue.len()) } else { format!("Started {} questions.", self.exercise_queue.len()) };
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
        let lang = ui_lang(&self.store);
        self.exercise_feedback = if correct {
            tr(lang, "Correct!", "Corretto!").to_string()
        } else {
            if lang == "it" { format!("Sbagliato. Risposta: {}", q.answer) } else { format!("Incorrect. Answer: {}", q.answer) }
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
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::GermanQuiz, "German quiz");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::TypingQuiz, "Typing");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::ClozeQuiz, "Cloze (from examples)");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::Mixed, "Mixed session");
            ui.selectable_value(&mut self.exercise_mode, ExerciseMode::GuidedSession, "Guided session (due + new)");
            if matches!(self.exercise_mode, ExerciseMode::ArticleQuiz | ExerciseMode::Mixed | ExerciseMode::GuidedSession) {
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
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        ui.heading(tr(lang, "Grammar & lessons", "Grammatica e lezioni"));
        if self.store.active_profile().is_none() {
            ui.label(tr(lang, "No active profile.", "Nessun profilo attivo."));
            return;
        }
        let plan = self.store.study_plan_summary();
        let (cur_topic_it, cur_topic_en) = lesson_topic(&plan.livello_corrente, plan.lezione_corrente.max(1));
        let cur_topic = if lang == "it" { cur_topic_it } else { cur_topic_en };

        ui.group(|ui| {
            ui.strong(tr(lang, "Syllabus & lesson progression", "Programma e progressione lezioni"));
            ui.label(if lang == "it" {
                format!("Livello: {}  •  Obiettivo: {}  •  Lezione corrente: {}", plan.livello_corrente, plan.obiettivo_livello, plan.lezione_corrente.max(1))
            } else {
                format!("Level: {}  •  Goal: {}  •  Current lesson: {}", plan.livello_corrente, plan.obiettivo_livello, plan.lezione_corrente.max(1))
            });
            ui.label(if lang == "it" { format!("Argomento: {cur_topic}") } else { format!("Topic: {cur_topic}") });
            ui.horizontal(|ui| {
                if ui.button(tr(lang, "Mark lesson complete / Next", "Segna lezione completata / Avanti")).clicked() {
                    match self.store.advance_lesson_progress() {
                        Ok(n) => {
                            self.status = if lang == "it" { format!("Lezione aggiornata: {n}") } else { format!("Lesson updated: {n}") };
                        }
                        Err(e) => self.status = format!("{}: {e}", tr(lang, "Update failed", "Aggiornamento fallito")),
                    }
                }
                if ui.button(tr(lang, "Reset to lesson 1", "Reimposta a lezione 1")).clicked() {
                    match self.store.set_lesson_progress(1) {
                        Ok(_) => self.status = tr(lang, "Lesson set to 1.", "Lezione impostata a 1.").into(),
                        Err(e) => self.status = format!("{}: {e}", tr(lang, "Update failed", "Aggiornamento fallito")),
                    }
                }
            });
            ui.add_space(4.0);
            let lessons = curriculum_lessons(&plan.livello_corrente, plan.lezione_corrente.max(1), 20);
            egui::ScrollArea::vertical().id_salt("grammar_syllabus").max_height(180.0).show(ui, |ui| {
                for l in &lessons {
                    let topic = if lang == "it" { &l.topic_it } else { &l.topic_en };
                    let marker = if l.is_current { "▶" } else if l.is_completed { "✓" } else { "•" };
                    let line = format!("{marker} {}. {topic}", l.number);
                    if l.is_current { ui.strong(line); } else { ui.label(line); }
                }
            });
            ui.small(tr(lang,
                "Syllabus topics follow the CEFR curriculum (A1/A2/B1) and are stored via lesson progress in the profile (compatible).",
                "Gli argomenti seguono il curriculum CEFR (A1/A2/B1) e sono salvati tramite la progressione lezione nel profilo (compatibile)."));
        });

        ui.add_space(8.0);
        ui.heading(tr(lang, "Grammar units (embedded practice)", "Unità grammaticali (pratica integrata)"));
        ui.small(tr(lang,
            "Representative embedded units with theory and interactive exercises. Completing practice saves mastery to profile data (compatible). Full GrammaticaPercorso content remains incremental.",
            "Unità integrate rappresentative con teoria ed esercizi interattivi. Il completamento salva la padronanza nei dati profilo (compatibile). Il contenuto completo GrammaticaPercorso resta incrementale."));
        ui.add_space(4.0);

        // Handle selected unit: detail/practice flow
        if let Some(unit_id) = self.grammar_unit_id.clone() {
            let unit_opt = crate::data::embedded_grammar_unit_by_id(&unit_id);
            if let Some(unit) = unit_opt {
                ui.horizontal(|ui| {
                    if ui.button(tr(lang, "← Back to units", "← Torna alle unità")).clicked() {
                        self.grammar_unit_id = None;
                        self.grammar_practice_index = 0;
                        self.grammar_practice_order.clear();
                        self.grammar_practice_correct = 0;
                        self.grammar_practice_answered = false;
                        self.grammar_practice_feedback.clear();
                        self.grammar_practice_input.clear();
                        self.grammar_practice_done = None;
                    }
                });
                ui.group(|ui| {
                    let title = if lang == "it" { unit.title_it } else { unit.title_en };
                    let subtitle = if lang == "it" { unit.subtitle_it } else { unit.subtitle_en };
                    let objective = if lang == "it" { unit.objective_it } else { unit.objective_en };
                    ui.strong(format!("{} — {}", unit.level, title));
                    ui.label(subtitle);
                    ui.label(format!("{}: {}", tr(lang, "Objective", "Obiettivo"), objective));
                    ui.add_space(4.0);
                    ui.label(tr(lang, "Theory", "Teoria"));
                    let theory = if lang == "it" { unit.theory_it } else { unit.theory_en };
                    for t in theory { ui.label(format!("• {t}")); }
                    if !unit.examples.is_empty() {
                        ui.add_space(4.0);
                        ui.label(tr(lang, "Examples", "Esempi"));
                        for (de, trn) in unit.examples { ui.label(format!("• {de} — {trn}")); }
                    }
                });

                // practice
                ui.add_space(6.0);
                if let Some((pct, completed)) = self.grammar_practice_done {
                    ui.group(|ui| {
                        ui.strong(tr(lang, "Practice completed", "Pratica completata"));
                        ui.label(if lang == "it" { format!("Punteggio: {pct}% ({}/{})", self.grammar_practice_correct, unit.exercises.len()) } else { format!("Score: {pct}% ({}/{})", self.grammar_practice_correct, unit.exercises.len()) });
                        ui.label(if completed { tr(lang, "Mastery: completed (>=80%)", "Padronanza: completata (>=80%)") } else { tr(lang, "Keep practicing to reach 80%", "Continua a esercitarti per raggiungere 80%") });
                        if ui.button(tr(lang, "Practice again", "Esercitati di nuovo")).clicked() {
                            self.grammar_practice_index = 0;
                            self.grammar_practice_order = shuffled_indices(unit.exercises.len(), chrono::Utc::now().timestamp_millis() as u64);
                            self.grammar_practice_correct = 0;
                            self.grammar_practice_answered = false;
                            self.grammar_practice_feedback.clear();
                            self.grammar_practice_input.clear();
                            self.grammar_practice_done = None;
                        }
                    });
                } else if self.grammar_practice_index >= unit.exercises.len() && !unit.exercises.is_empty() {
                    // finalize and save mastery
                    let correct = self.grammar_practice_correct;
                    let total = unit.exercises.len();
                    match self.store.record_grammar_unit_practice(unit.id, correct, total) {
                        Ok(r) => {
                            self.grammar_practice_done = Some((r.percent, r.completed));
                            self.status = if lang == "it" { format!("Padronanza salvata: {}%", r.percent) } else { format!("Mastery saved: {}%", r.percent) };
                        }
                        Err(e) => self.status = format!("{}: {e}", tr(lang, "Save failed", "Salvataggio fallito")),
                    }
                } else if unit.exercises.is_empty() {
                    ui.label(tr(lang, "No exercises available for this unit.", "Nessun esercizio disponibile per questa unità."));
                } else {
                    if self.grammar_practice_order.len() != unit.exercises.len() {
                        self.grammar_practice_order = shuffled_indices(unit.exercises.len(), chrono::Utc::now().timestamp_millis() as u64);
                    }
                    let ex_idx = *self.grammar_practice_order.get(self.grammar_practice_index).unwrap_or(&0);
                    let ex = &unit.exercises[ex_idx.min(unit.exercises.len().saturating_sub(1))];
                    let idx = self.grammar_practice_index;
                    ui.group(|ui| {
                        ui.label(if lang == "it" { format!("Esercizio {}/{}", idx + 1, unit.exercises.len()) } else { format!("Exercise {}/{}", idx + 1, unit.exercises.len()) });
                        let prompt = if lang == "it" { ex.prompt_it } else { ex.prompt_en };
                        ui.strong(prompt);
                        if !self.grammar_practice_answered {
                            if ex.options.is_empty() {
                                ui.horizontal(|ui| {
                                    ui.add(egui::TextEdit::singleline(&mut self.grammar_practice_input).hint_text(tr(lang, "Type answer...", "Scrivi la risposta...")));
                                    if ui.button(tr(lang, "Check", "Controlla")).clicked() {
                                        let given = self.grammar_practice_input.trim().to_lowercase();
                                        let expected = ex.answer_text.trim().to_lowercase();
                                        let correct = !expected.is_empty() && given == expected;
                                        self.grammar_practice_answered = true;
                                        if correct { self.grammar_practice_correct += 1; }
                                        let expl = if lang == "it" { ex.explanation_it } else { ex.explanation_en };
                                        self.grammar_practice_feedback = if correct {
                                            if lang == "it" { format!("Corretto! {expl}") } else { format!("Correct! {expl}") }
                                        } else {
                                            if lang == "it" { format!("Sbagliato. Risposta: {}. {expl}", ex.answer_text) } else { format!("Incorrect. Answer: {}. {expl}", ex.answer_text) }
                                        };
                                    }
                                });
                            } else {
                                let mut picked: Option<usize> = None;
                                for (i, opt) in ex.options.iter().enumerate() {
                                    if ui.button(*opt).clicked() { picked = Some(i); }
                                }
                                if let Some(i) = picked {
                                    self.grammar_practice_answered = true;
                                    let correct = i == ex.correct_index;
                                    if correct { self.grammar_practice_correct += 1; }
                                    let expl = if lang == "it" { ex.explanation_it } else { ex.explanation_en };
                                    let ans = ex.options.get(ex.correct_index).copied().unwrap_or("");
                                    self.grammar_practice_feedback = if correct {
                                        if lang == "it" { format!("Corretto! {expl}") } else { format!("Correct! {expl}") }
                                    } else {
                                        if lang == "it" { format!("Sbagliato. Risposta: {ans}. {expl}") } else { format!("Incorrect. Answer: {ans}. {expl}") }
                                    };
                                }
                            }
                        } else {
                            ui.label(&self.grammar_practice_feedback);
                            if ui.button(tr(lang, "Next", "Avanti")).clicked() {
                                self.grammar_practice_index += 1;
                                self.grammar_practice_answered = false;
                                self.grammar_practice_feedback.clear();
                                self.grammar_practice_input.clear();
                            }
                        }
                    });
                }
            } else {
                self.grammar_unit_id = None;
            }
            ui.add_space(8.0);
        }

        // list embedded units (when not deep in unrelated state; always show as entry point)
        if self.grammar_unit_id.is_none() {
            let units = self.store.embedded_grammar_units();
            let summaries = self.store.grammar_mastery_summaries();
            let now_ms = chrono::Utc::now().timestamp_millis();
            // recommend: first due unit, else first not-completed/mastered, else first
            let recommended_id = units.iter().find_map(|u| {
                let s = summaries.iter().find(|x| x.id == u.id);
                if s.map(|m| m.is_due(now_ms)).unwrap_or(false) { Some(u.id.to_string()) } else { None }
            }).or_else(|| {
                units.iter().find_map(|u| {
                    let s = summaries.iter().find(|x| x.id == u.id);
                    let done = s.map(|m| m.completata || m.percentuale >= 80).unwrap_or(false);
                    if !done { Some(u.id.to_string()) } else { None }
                })
            }).or_else(|| units.first().map(|u| u.id.to_string()));

            if let Some(rid) = &recommended_id {
                if let Some(u) = units.iter().find(|x| &x.id.to_string() == rid) {
                    let title = if lang == "it" { u.title_it } else { u.title_en };
                    ui.label(if lang == "it" { format!("Consigliata: [{}] {title}", u.level) } else { format!("Recommended: [{}] {title}", u.level) });
                }
            }
            ui.add_space(4.0);

            for u in &units {
                let title = if lang == "it" { u.title_it } else { u.title_en };
                let mastery = summaries.iter().find(|s| s.id == u.id);
                let state = if let Some(m) = mastery {
                    if m.is_due(now_ms) {
                        tr(lang, "due", "da ripassare")
                    } else if m.completata || m.percentuale >= 80 {
                        tr(lang, "mastered", "padroneggiata")
                    } else if m.tentativi > 0 {
                        tr(lang, "in progress", "in corso")
                    } else {
                        tr(lang, "new", "nuova")
                    }
                } else {
                    tr(lang, "new", "nuova")
                };
                ui.horizontal(|ui| {
                    if ui.button(tr(lang, "Open", "Apri")).clicked() {
                        let order = shuffled_indices(u.exercises.len(), chrono::Utc::now().timestamp_millis() as u64);
                        self.grammar_unit_id = Some(u.id.to_string());
                        self.grammar_practice_index = 0;
                        self.grammar_practice_order = order;
                        self.grammar_practice_correct = 0;
                        self.grammar_practice_answered = false;
                        self.grammar_practice_feedback.clear();
                        self.grammar_practice_input.clear();
                        self.grammar_practice_done = None;
                    }
                    ui.label(format!("[{}] {title}", u.level));
                    ui.small(format!("• {state}"));
                    if let Some(m) = mastery {
                        ui.small(if lang == "it" { format!("({}% • tent. {})", m.percentuale, m.tentativi) } else { format!("({}% • att. {})", m.percentuale, m.tentativi) });
                    }
                });
            }
            ui.add_space(8.0);
        }

        ui.heading(tr(lang, "Grammar mastery & unit progression", "Padronanza grammaticale e unità"));
        let mut summaries = self.store.grammar_mastery_summaries();
        let now = chrono::Utc::now().timestamp_millis();
        ui.label(if lang == "it" { format!("Unità tracciate: {}", summaries.len()) } else { format!("Tracked units: {}", summaries.len()) });
        ui.add_space(6.0);
        if summaries.is_empty() {
            ui.label(tr(lang,
                "No grammar mastery data yet. Complete grammar/unit practice to populate this view.",
                "Nessun dato di padronanza grammaticale. Completa pratica grammaticale/unità per popolare questa vista."));
        } else {
            summaries.sort_by(|a, b| {
                let ad = a.is_due(now);
                let bd = b.is_due(now);
                bd.cmp(&ad)
                    .then_with(|| a.completata.cmp(&b.completata))
                    .then_with(|| b.percentuale.cmp(&a.percentuale))
                    .then_with(|| a.id.cmp(&b.id))
            });
            egui::ScrollArea::vertical().id_salt("grammar_mastery").max_height(260.0).show(ui, |ui| {
                for s in &summaries {
                    ui.group(|ui| {
                        ui.horizontal(|ui| {
                            ui.strong(&s.id);
                            let state = if s.is_due(now) {
                                tr(lang, "due for review", "da ripassare")
                            } else if s.completata {
                                tr(lang, "completed", "completata")
                            } else if s.percentuale >= 80 {
                                tr(lang, "mastered", "padroneggiata")
                            } else if s.tentativi > 0 {
                                tr(lang, "in progress", "in corso")
                            } else {
                                tr(lang, "new", "nuova")
                            };
                            ui.label(format!("• {state}"));
                        });
                        ui.label(if lang == "it" {
                            format!("Punteggio: {}%  •  Tentativi: {}  •  Corrette: {}  •  Ripassi: {}", s.percentuale, s.tentativi, s.corrette, s.ripassi)
                        } else {
                            format!("Score: {}%  •  Attempts: {}  •  Correct: {}  •  Reviews: {}", s.percentuale, s.tentativi, s.corrette, s.ripassi)
                        });
                        if s.prossima_ripasso > 0 {
                            if let Some(dt) = chrono::DateTime::<chrono::Utc>::from_timestamp_millis(s.prossima_ripasso) {
                                ui.small(format!("{}: {}", tr(lang, "Next review", "Prossimo ripasso"), dt.with_timezone(&chrono::Local).format("%Y-%m-%d %H:%M")));
                            }
                        }
                        if s.ultima_volta > 0 {
                            if let Some(dt) = chrono::DateTime::<chrono::Utc>::from_timestamp_millis(s.ultima_volta) {
                                ui.small(format!("{}: {}", tr(lang, "Last activity", "Ultima attività"), dt.with_timezone(&chrono::Local).format("%Y-%m-%d %H:%M")));
                            }
                        }
                    });
                    ui.add_space(4.0);
                }
            });
        }
        if !self.status.is_empty() {
            ui.add_space(4.0);
            ui.label(&self.status);
        }
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
        let plan = self.store.study_plan_summary();
        ui.label(format!("Profile: {}", p.config.nome_visualizzato));
        ui.label(format!("Level: {}  •  Goal: {}  •  Lesson: {}", plan.livello_corrente, plan.obiettivo_livello, plan.lezione_corrente));
        ui.add_space(6.0);

        ui.group(|ui| {
            ui.strong("Study plan / next actions");
            ui.label(format!("Due words: {}  •  New words: {}", plan.due_words, plan.new_words));
            ui.label(format!("Grammar tracked: {}  •  Completed/mastered: {}  •  Due: {}", plan.grammar_tracked, plan.grammar_completed, plan.grammar_due));
            for a in &plan.next_actions {
                ui.label(format!("• {a}"));
            }
        });

        ui.add_space(6.0);
        ui.group(|ui| {
            ui.label(format!("Total active words: {total}"));
            ui.label(format!("Reviewed words: {reviewed}  •  Mature/strong: {mature}"));
            ui.label(format!("Due now: {due}"));
            ui.label(format!("Streak: {streak} days (record: {})", p.stato.streak_record));
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
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        ui.heading(tr(lang, "Settings", "Impostazioni"));
        ui.label(format!("{}: {}", tr(lang, "Data folder", "Cartella dati"), self.store.base_dir.display()));
        ui.label(format!("{}: {}", tr(lang, "Profiles file", "File profili"), self.store.profili_path.display()));
        ui.label(format!("{}: {}", tr(lang, "Preferences file", "File preferenze"), self.store.preferenze_path.display()));
        ui.add_space(8.0);

        ui.group(|ui| {
            ui.label(tr(lang, "Language", "Lingua"));
            let mut selected = if self.store.prefs.lingua.trim().eq_ignore_ascii_case("it") { "it" } else { "en" };
            ui.horizontal(|ui| {
                if ui.selectable_label(selected == "en", "English").clicked() { selected = "en"; }
                if ui.selectable_label(selected == "it", "Italiano").clicked() { selected = "it"; }
            });
            if self.store.prefs.lingua.trim().to_lowercase() != selected {
                self.store.prefs.lingua = selected.to_string();
                let _ = self.store.save();
                self.status = tr(lang, "Language saved.", "Lingua salvata.").to_string();
            }
        });

        ui.add_space(8.0);
        ui.group(|ui| {
            ui.label(tr(lang, "Theme", "Tema"));
            let mut mode = self.store.prefs.tema_modalita.trim().to_lowercase();
            if mode.is_empty() { mode = "system".into(); }
            ui.horizontal_wrapped(|ui| {
                ui.label(tr(lang, "Mode:", "Modalità:"));
                for (id, en, it) in [("system", "System", "Sistema"), ("light", "Light", "Chiaro"), ("dark", "Dark", "Scuro")] {
                    if ui.selectable_label(mode == id, tr(lang, en, it)).clicked() {
                        self.store.prefs.tema_modalita = id.to_string();
                        self.store.prefs.tema_scuro = id == "dark";
                        let _ = self.store.save();
                    }
                }
            });
            let mut colore = self.store.prefs.tema_colore.trim().to_lowercase();
            if colore.is_empty() { colore = "teal".into(); }
            ui.horizontal_wrapped(|ui| {
                ui.label(tr(lang, "Color:", "Colore:"));
                for (id, en, it) in [("teal","Teal","Teal"), ("navy","Navy blue","Blu navy"), ("indigo","Indigo","Indaco"), ("forest","Forest","Verde"), ("rose","Rose","Rosa"), ("amber","Amber","Ambra")] {
                    if ui.selectable_label(colore == id, tr(lang, en, it)).clicked() {
                        self.store.prefs.tema_colore = id.to_string();
                        let _ = self.store.save();
                    }
                }
            });
            ui.small(tr(lang,
                "Matches the Compose desktop/Android color themes (teal, navy, indigo, forest, rose, amber) and light/dark modes.",
                "Corrisponde ai temi colore di Compose desktop/Android (teal, navy, indigo, forest, rose, amber) e alle modalità chiaro/scuro."));
        });

        ui.add_space(8.0);
        ui.group(|ui| {
            ui.label(tr(lang, "Profiles", "Profili"));
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
                if ui.button(tr(lang, "Create profile", "Crea profilo")).clicked() {
                    let name = self.new_profile_name.trim().to_string();
                    if !name.is_empty() {
                        self.store.create_profile(&name);
                        self.new_profile_name.clear();
                        self.refresh_review_queue();
                        self.status = tr(lang, "Profile created.", "Profilo creato.").into();
                    }
                }
            });
        });

        ui.add_space(8.0);
        ui.horizontal(|ui| {
            if ui.button(tr(lang, "Reload from disk", "Ricarica da disco")).clicked() {
                self.store = Store::load_default();
                self.refresh_review_queue();
                self.status = if lang == "it" { format!("Ricaricato alle {}", Local::now().format("%H:%M:%S")) } else { format!("Reloaded at {}", Local::now().format("%H:%M:%S")) };
            }
            if ui.button(tr(lang, "Save", "Salva")).clicked() {
                match self.store.save() {
                    Ok(_) => self.status = tr(lang, "Saved.", "Salvato.").into(),
                    Err(e) => self.status = format!("{}: {e}", tr(lang, "Save error", "Errore salvataggio")),
                }
            }
        });

        ui.add_space(8.0);
        ui.group(|ui| {
            ui.label(tr(lang, "Backup (compatible profile data bundle)", "Backup (bundle dati profilo compatibile)"));
            ui.horizontal(|ui| {
                ui.label(tr(lang, "Path", "Percorso"));
                ui.add(egui::TextEdit::singleline(&mut self.backup_path).desired_width(420.0));
            });
            ui.horizontal(|ui| {
                if ui.button(tr(lang, "Export backup", "Esporta backup")).clicked() {
                    let path = std::path::PathBuf::from(self.backup_path.trim());
                    match self.store.export_bundle_to(&path) {
                        Ok(_) => self.status = format!("{}: {}", tr(lang, "Exported backup to", "Backup esportato in"), path.display()),
                        Err(e) => self.status = format!("{}: {e}", tr(lang, "Export failed", "Esportazione fallita")),
                    }
                }
                if ui.button(tr(lang, "Import backup", "Importa backup")).clicked() {
                    let path = std::path::PathBuf::from(self.backup_path.trim());
                    match self.store.import_bundle_from(&path) {
                        Ok(_) => {
                            self.refresh_review_queue();
                            self.status = format!("{}: {}", tr(lang, "Imported backup from", "Backup importato da"), path.display());
                        }
                        Err(e) => self.status = format!("{}: {e}", tr(lang, "Import failed", "Importazione fallita")),
                    }
                }
            });
            ui.small(tr(lang,
                "Export/import includes profiles and preferences in a JSON bundle (also accepts profili-shaped JSON on import).",
                "Esporta/importa profili e preferenze in un bundle JSON (in import accetta anche JSON nel formato profili)."));
        });

        ui.add_space(10.0);
        ui.separator();
        ui.small("OpenLingo Rust desktop rebuild: compatible profile data, SRS persistence, exercises/practice, vocabulary search/add/archive, grammar mastery view, progress/backup (Home/Review/Exercises/Vocabulary/Grammar/Progress/Settings).");
        if !self.status.is_empty() {
            ui.add_space(6.0);
            ui.label(&self.status);
        }
    }
}

impl eframe::App for OpenLingoApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        let system_dark = ctx.style().visuals.dark_mode; // best-effort; system theme may be reflected by eframe
        apply_theme(ctx, &self.store, system_dark);
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

    #[test]
    fn export_import_bundle_roundtrip() {
        let dir = temp_dir("bundle");
        let mut store = Store::load_from(dir.clone());
        let id = store.create_profile("Dana");
        {
            let p = store.repo.profili.get_mut(&id).unwrap();
            p.stato.parole.push(WordEntity { id:"w1".into(), german:"Buch".into(), italian:"libro".into(), ..Default::default() });
        }
        store.save().unwrap();
        let out = dir.join("backup.json");
        store.export_bundle_to(&out).unwrap();
        assert!(out.exists());

        let dir2 = temp_dir("bundle2");
        let mut store2 = Store::load_from(dir2.clone());
        store2.import_bundle_from(&out).unwrap();
        assert_eq!(store2.repo.profilo_attivo_id.as_deref(), Some(id.as_str()));
        assert_eq!(store2.active_profile().unwrap().config.nome_visualizzato, "Dana");
        assert_eq!(store2.active_profile().unwrap().stato.parole.len(), 1);
        let _ = fs::remove_dir_all(dir);
        let _ = fs::remove_dir_all(dir2);
    }

    #[test]
    fn ui_translation_helper_respects_language() {
        assert_eq!(super::tr("en", "Hello", "Ciao"), "Hello");
        assert_eq!(super::tr("it", "Hello", "Ciao"), "Ciao");
        let dir = temp_dir("lang");
        let mut store = Store::load_from(dir.clone());
        store.prefs.lingua = "it".into();
        assert_eq!(super::ui_lang(&store), "it");
        store.prefs.lingua = "en".into();
        assert_eq!(super::ui_lang(&store), "en");
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn german_choice_quiz_builds_options() {
        let words = vec![
            WordEntity { id:"1".into(), german:"Hund".into(), italian:"cane".into(), article:Some("der".into()), level:"A1".into(), ..Default::default() },
            WordEntity { id:"2".into(), german:"Katze".into(), italian:"gatto".into(), article:Some("die".into()), level:"A1".into(), ..Default::default() },
            WordEntity { id:"3".into(), german:"Buch".into(), italian:"libro".into(), article:Some("das".into()), level:"A1".into(), ..Default::default() },
            WordEntity { id:"4".into(), german:"Haus".into(), italian:"casa".into(), article:Some("das".into()), level:"A1".into(), ..Default::default() },
        ];
        let qs = build_german_choice_quiz(&words, 9, 2);
        assert!(!qs.is_empty());
        assert!(qs.iter().all(|q| q.options.iter().any(|o| o == &q.answer)));
        assert!(qs.iter().all(|q| q.prompt.contains("German")));
    }

    #[test]
    fn study_plan_and_grammar_due_summary() {
        let dir = temp_dir("plan");
        let mut store = Store::load_from(dir.clone());
        let id = store.create_profile("Eli");
        {
            let p = store.repo.profili.get_mut(&id).unwrap();
            p.stato.progresso.lezione_corrente = 7;
            p.stato.parole = vec![
                WordEntity { id:"w1".into(), german:"eins".into(), italian:"uno".into(), ..Default::default() },
                WordEntity { id:"w2".into(), german:"zwei".into(), italian:"due".into(), ..Default::default() },
            ];
            let mut reviews = HashMap::new();
            reviews.insert("w1".into(), ReviewEntity { word_id:"w1".into(), due_at: 1, repetitions: 1, ..Default::default() });
            p.stato.reviews = reviews;
            p.stato.padronanza_grammatica.insert("u1".into(), serde_json::json!({
                "percentuale": 85, "completata": true, "tentativi": 3, "corrette": 9, "prossimaRipasso": 1, "ripassi": 2
            }));
            p.stato.padronanza_grammatica.insert("u2".into(), serde_json::json!({
                "percentuale": 40, "completata": false, "tentativi": 1, "corrette": 2, "prossimaRipasso": 9999999999999i64
            }));
        }
        let plan = store.study_plan_summary();
        assert_eq!(plan.lezione_corrente, 7);
        assert_eq!(plan.due_words, 1);
        assert_eq!(plan.new_words, 1); // w2 not reviewed
        assert_eq!(plan.grammar_tracked, 2);
        assert_eq!(plan.grammar_completed, 1);
        assert_eq!(plan.grammar_due, 1);
        assert!(!plan.next_actions.is_empty());
        let gs = store.grammar_mastery_summaries();
        let u1 = gs.iter().find(|g| g.id == "u1").unwrap();
        assert!(u1.is_due(100));
        assert_eq!(u1.ripassi, 2);
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn curriculum_lessons_and_progression_update() {
        use crate::data::{curriculum_lessons, lesson_topic};
        let (it1, en1) = lesson_topic("A1", 1);
        assert!(!it1.is_empty() && !en1.is_empty());
        let lessons = curriculum_lessons("A2", 3, 20);
        assert_eq!(lessons.len(), 20);
        assert!(lessons[2].is_current);
        assert!(lessons[1].is_completed);
        let (it21, _) = lesson_topic("A1", 21);
        assert!(it21.contains("approfondimento") || it21.to_lowercase().contains("deepening"));

        let dir = temp_dir("syllabus");
        let mut store = Store::load_from(dir.clone());
        store.create_profile("Leo");
        let n = store.advance_lesson_progress().expect("advance");
        assert_eq!(n, 1);
        let n2 = store.advance_lesson_progress().expect("advance2");
        assert_eq!(n2, 2);
        store.set_lesson_progress(5).expect("set");
        assert_eq!(store.study_plan_summary().lezione_corrente, 5);
        // persistence round-trip
        let store2 = Store::load_from(dir.clone());
        assert_eq!(store2.study_plan_summary().lezione_corrente, 5);
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn guided_session_builds_mixed_questions() {
        use crate::data::build_guided_session;
        let words = vec![
            WordEntity { id:"1".into(), german:"Tisch".into(), article:Some("der".into()), italian:"tavolo".into(), level:"A1".into(), example:"Der Tisch ist groß.".into(), ..Default::default() },
            WordEntity { id:"2".into(), german:"Lampe".into(), article:Some("die".into()), italian:"lampada".into(), level:"A1".into(), example:"Die Lampe ist neu.".into(), ..Default::default() },
            WordEntity { id:"3".into(), german:"Buch".into(), article:Some("das".into()), italian:"libro".into(), level:"A1".into(), example:"Das Buch ist gut.".into(), ..Default::default() },
            WordEntity { id:"4".into(), german:"Stuhl".into(), article:Some("der".into()), italian:"sedia".into(), level:"A1".into(), example:"Der Stuhl ist alt.".into(), ..Default::default() },
        ];
        let qs = build_guided_session(&words, "A1", 12345, 12);
        assert!(!qs.is_empty());
        assert!(qs.len() <= 12);
        assert!(qs.iter().any(|q| !q.word.id.is_empty()));
    }

    #[test]
    fn embedded_grammar_units_practice_saves_mastery() {
        use crate::data::embedded_grammar_units;
        let units = embedded_grammar_units();
        assert!(units.len() >= 6);
        let u = &units[0];
        assert!(!u.id.is_empty() && !u.exercises.is_empty());
        // ensure at least one typing/completion exercise exists in embedded units
        assert!(units.iter().any(|uu| uu.exercises.iter().any(|ex| ex.options.is_empty() && !ex.answer_text.is_empty())));

        let dir = temp_dir("gunit");
        let mut store = Store::load_from(dir.clone());
        store.create_profile("Mia");
        let res = store.record_grammar_unit_practice(u.id, u.exercises.len(), u.exercises.len()).expect("record");
        assert_eq!(res.percent, 100);
        assert!(res.completed);
        let summaries = store.grammar_mastery_summaries();
        let s = summaries.iter().find(|x| x.id == u.id).expect("summary");
        assert!(s.percentuale >= 80);
        assert!(s.tentativi >= 1);
        // persistence
        let store2 = Store::load_from(dir.clone());
        let s2 = store2.grammar_mastery_summaries().into_iter().find(|x| x.id == u.id).expect("persisted");
        assert_eq!(s2.percentuale, s.percentuale);
        let _ = fs::remove_dir_all(dir);
    }
}
