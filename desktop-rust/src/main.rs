mod data;

use chrono::{Local};
use data::{Store, WordEntity, ExerciseQuestion, ExerciseKind, build_article_quiz, build_meaning_quiz, build_german_choice_quiz, build_typing_quiz, build_cloze_quiz, build_mixed_quiz, build_guided_session, check_typing_answer, curriculum_lessons, lesson_topic};
use eframe::egui;
use std::collections::BTreeSet;

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

#[derive(Clone, Copy)]
struct UiPalette {
    primary: egui::Color32,
    on_primary: egui::Color32,
    primary_container: egui::Color32,
    on_primary_container: egui::Color32,
    secondary_container: egui::Color32,
    on_secondary_container: egui::Color32,
    tertiary_container: egui::Color32,
    on_tertiary_container: egui::Color32,
    background: egui::Color32,
    on_background: egui::Color32,
    surface: egui::Color32,
    on_surface: egui::Color32,
    surface_variant: egui::Color32,
    on_surface_variant: egui::Color32,
    outline: egui::Color32,
    error: egui::Color32,
    on_error: egui::Color32,
}

fn palette(store: &Store, system_dark: bool) -> UiPalette {
    let dark = effective_dark(store, system_dark);
    let id = store.prefs.tema_colore.trim().to_lowercase();
    if dark {
        match id.as_str() {
            "navy" => UiPalette { primary: hex(0xB4C5FF), on_primary: hex(0x002A78), primary_container: hex(0x1F3A78), on_primary_container: hex(0xDCE4FF), secondary_container: hex(0x2C3447), on_secondary_container: hex(0xDAE2F9), tertiary_container: hex(0x2A3A4D), on_tertiary_container: hex(0xD7E8FF), background: hex(0x0D0F0E), on_background: hex(0xE1E2EC), surface: hex(0x141B19), on_surface: hex(0xE1E2EC), surface_variant: hex(0x3E4554), on_surface_variant: hex(0xC4C6D0), outline: hex(0x5A6070), error: hex(0xFFB4AB), on_error: hex(0x690005) },
            "indigo" => UiPalette { primary: hex(0xD0BCFF), on_primary: hex(0x381E72), primary_container: hex(0x3B2A63), on_primary_container: hex(0xEADDFF), secondary_container: hex(0x332D41), on_secondary_container: hex(0xE8DEF8), tertiary_container: hex(0x492532), on_tertiary_container: hex(0xFFD8E4), background: hex(0x0E0B14), on_background: hex(0xE6E1F4), surface: hex(0x14111C), on_surface: hex(0xE6E1F4), surface_variant: hex(0x49454F), on_surface_variant: hex(0xCAC4D0), outline: hex(0x6A6573), error: hex(0xFFB4AB), on_error: hex(0x690005) },
            "forest" => UiPalette { primary: hex(0x8FD694), on_primary: hex(0x003915), primary_container: hex(0x1F4D2B), on_primary_container: hex(0xB7F1B4), secondary_container: hex(0x2B3D30), on_secondary_container: hex(0xD3E8D3), tertiary_container: hex(0x243E3B), on_tertiary_container: hex(0xBCECE7), background: hex(0x0A120C), on_background: hex(0xDEE5D8), surface: hex(0x0F1711), on_surface: hex(0xDEE5D8), surface_variant: hex(0x404942), on_surface_variant: hex(0xC0C9BE), outline: hex(0x5A665C), error: hex(0xFFB4AB), on_error: hex(0x690005) },
            "rose" => UiPalette { primary: hex(0xFFB2BE), on_primary: hex(0x5F1128), primary_container: hex(0x6E2438), on_primary_container: hex(0xFFD9DE), secondary_container: hex(0x432A30), on_secondary_container: hex(0xFFD9DE), tertiary_container: hex(0x4A3420), on_tertiary_container: hex(0xFFDDB8), background: hex(0x120A0C), on_background: hex(0xF1DEE0), surface: hex(0x171013), on_surface: hex(0xF1DEE0), surface_variant: hex(0x524346), on_surface_variant: hex(0xD7C1C4), outline: hex(0x6D5C60), error: hex(0xFFB4AB), on_error: hex(0x690005) },
            "amber" => UiPalette { primary: hex(0xF6BE5A), on_primary: hex(0x432C00), primary_container: hex(0x5A3E00), on_primary_container: hex(0xFFDEA4), secondary_container: hex(0x3E3423), on_secondary_container: hex(0xF4E0B9), tertiary_container: hex(0x2B3D2A), on_tertiary_container: hex(0xCBEBC5), background: hex(0x120E08), on_background: hex(0xECE1CF), surface: hex(0x17120B), on_surface: hex(0xECE1CF), surface_variant: hex(0x4D4639), on_surface_variant: hex(0xD1C5B4), outline: hex(0x6A6254), error: hex(0xFFB4AB), on_error: hex(0x690005) },
            // teal (default) - tuned for mobile-like contrast
            _ => UiPalette {
                primary: hex(0x79D7C7),
                on_primary: hex(0x003731),
                primary_container: hex(0x1B3B36),
                on_primary_container: hex(0xB9F3E8),
                secondary_container: hex(0x223331),
                on_secondary_container: hex(0xE6F1EE),
                tertiary_container: hex(0x213740),
                on_tertiary_container: hex(0xDCEBF3),
                background: hex(0x0B1110),
                on_background: hex(0xE6ECEA),
                surface: hex(0x121918),
                on_surface: hex(0xE6ECEA),
                surface_variant: hex(0x2A3533),
                on_surface_variant: hex(0xC7D3D0),
                outline: hex(0x5D6B68),
                error: hex(0xFFB4AB),
                on_error: hex(0x690005),
            },
        }
    } else {
        match id.as_str() {
            "navy" => UiPalette { primary: hex(0x1D4ED8), on_primary: hex(0xFFFFFF), primary_container: hex(0xDCE6FF), on_primary_container: hex(0x001A43), secondary_container: hex(0xE0E6F8), on_secondary_container: hex(0x131C2E), tertiary_container: hex(0xDCEFFF), on_tertiary_container: hex(0x001E31), background: hex(0xF7F8FD), on_background: hex(0x1A1C22), surface: hex(0xFCFCFF), on_surface: hex(0x1A1C22), surface_variant: hex(0xE1E2EC), on_surface_variant: hex(0x44474F), outline: hex(0xC4C6D0), error: hex(0xBA1A1A), on_error: hex(0xFFFFFF) },
            "indigo" => UiPalette { primary: hex(0x6750A4), on_primary: hex(0xFFFFFF), primary_container: hex(0xE9DDFF), on_primary_container: hex(0x22005D), secondary_container: hex(0xE8DEF8), on_secondary_container: hex(0x1D192B), tertiary_container: hex(0xFFD8E4), on_tertiary_container: hex(0x31111D), background: hex(0xF8F5FF), on_background: hex(0x1D1B20), surface: hex(0xFFFBFF), on_surface: hex(0x1D1B20), surface_variant: hex(0xE7E0EC), on_surface_variant: hex(0x49454F), outline: hex(0xCAC4D0), error: hex(0xBA1A1A), on_error: hex(0xFFFFFF) },
            "forest" => UiPalette { primary: hex(0x2E7D32), on_primary: hex(0xFFFFFF), primary_container: hex(0xD7F5D8), on_primary_container: hex(0x002106), secondary_container: hex(0xDCE8DB), on_secondary_container: hex(0x111F13), tertiary_container: hex(0xCDECE8), on_tertiary_container: hex(0x00201E), background: hex(0xF4FAF4), on_background: hex(0x182018), surface: hex(0xF9FDF8), on_surface: hex(0x182018), surface_variant: hex(0xDDE5D8), on_surface_variant: hex(0x414941), outline: hex(0xC1C9BE), error: hex(0xBA1A1A), on_error: hex(0xFFFFFF) },
            "rose" => UiPalette { primary: hex(0xB3264E), on_primary: hex(0xFFFFFF), primary_container: hex(0xFFD9DF), on_primary_container: hex(0x3F0015), secondary_container: hex(0xF5DDE1), on_secondary_container: hex(0x26161A), tertiary_container: hex(0xFFDCC2), on_tertiary_container: hex(0x2D1600), background: hex(0xFFF7F8), on_background: hex(0x201A1B), surface: hex(0xFFFBFB), on_surface: hex(0x201A1B), surface_variant: hex(0xF2DDE0), on_surface_variant: hex(0x524345), outline: hex(0xD6C2C5), error: hex(0xBA1A1A), on_error: hex(0xFFFFFF) },
            "amber" => UiPalette { primary: hex(0x8B5E00), on_primary: hex(0xFFFFFF), primary_container: hex(0xFFE2B5), on_primary_container: hex(0x2D1600), secondary_container: hex(0xF0E0C0), on_secondary_container: hex(0x241A07), tertiary_container: hex(0xD8E7D0), on_tertiary_container: hex(0x131F0F), background: hex(0xFFF8F0), on_background: hex(0x1F1B14), surface: hex(0xFFFBF4), on_surface: hex(0x1F1B14), surface_variant: hex(0xEEE1CF), on_surface_variant: hex(0x4E4639), outline: hex(0xD2C5B4), error: hex(0xBA1A1A), on_error: hex(0xFFFFFF) },
            _ => UiPalette { primary: hex(0x0F766E), on_primary: hex(0xFFFFFF), primary_container: hex(0xD3F4EF), on_primary_container: hex(0x00201D), secondary_container: hex(0xDCEAE6), on_secondary_container: hex(0x0F201D), tertiary_container: hex(0xD8E8F7), on_tertiary_container: hex(0x0D1D2A), background: hex(0xF3F8F6), on_background: hex(0x171D1B), surface: hex(0xF9FDFC), on_surface: hex(0x171D1B), surface_variant: hex(0xDDE5E1), on_surface_variant: hex(0x414946), outline: hex(0xC0C9C5), error: hex(0xBA1A1A), on_error: hex(0xFFFFFF) },
        }
    }
}

fn apply_theme(ctx: &egui::Context, store: &Store, system_dark: bool) {
    let dark = effective_dark(store, system_dark);
    let p = palette(store, system_dark);
    // println!("Palette: dark={dark}, background={:?}, primary={:?}", p.background, p.primary);
    let mut visuals = if dark { egui::Visuals::dark() } else { egui::Visuals::light() };
    // High-contrast text: pure white in dark mode, pure black in light mode
    visuals.override_text_color = Some(if dark { egui::Color32::WHITE } else { egui::Color32::BLACK });
    visuals.weak_text_color = if dark { Some(egui::Color32::WHITE) } else { Some(egui::Color32::BLACK) };
    visuals.panel_fill = p.background;
    visuals.window_fill = p.surface;
    visuals.extreme_bg_color = p.surface_variant;
    visuals.faint_bg_color = p.secondary_container;
    visuals.code_bg_color = p.surface_variant;
    visuals.hyperlink_color = p.primary;
    visuals.selection.bg_fill = p.primary_container;
    visuals.selection.stroke = egui::Stroke::new(1.0_f32, p.primary);
    visuals.widgets.noninteractive.bg_fill = p.surface;
    visuals.widgets.noninteractive.fg_stroke = egui::Stroke::new(1.0_f32, p.on_surface);
    visuals.widgets.noninteractive.weak_bg_fill = p.surface_variant;
    visuals.widgets.noninteractive.bg_stroke = egui::Stroke::new(1.0_f32, p.outline);
    visuals.widgets.inactive.bg_fill = p.secondary_container;
    visuals.widgets.inactive.fg_stroke = egui::Stroke::new(1.0_f32, p.on_secondary_container);
    visuals.widgets.inactive.weak_bg_fill = p.surface_variant;
    visuals.widgets.inactive.bg_stroke = egui::Stroke::new(1.0_f32, p.outline);
    visuals.widgets.hovered.bg_fill = p.primary_container;
    visuals.widgets.hovered.fg_stroke = egui::Stroke::new(1.0_f32, p.on_primary_container);
    visuals.widgets.hovered.weak_bg_fill = p.primary_container;
    visuals.widgets.hovered.bg_stroke = egui::Stroke::new(1.0_f32, p.primary);
    visuals.widgets.active.bg_fill = p.primary;
    visuals.widgets.active.fg_stroke = egui::Stroke::new(1.0_f32, p.on_primary);
    visuals.widgets.active.weak_bg_fill = p.primary;
    visuals.widgets.active.bg_stroke = egui::Stroke::new(1.0_f32, p.primary);
    visuals.widgets.open.bg_fill = p.secondary_container;
    visuals.widgets.open.fg_stroke = egui::Stroke::new(1.0_f32, p.on_secondary_container);
    visuals.widgets.open.weak_bg_fill = p.secondary_container;
    visuals.widgets.open.bg_stroke = egui::Stroke::new(1.0_f32, p.primary);
    let rounding = egui::CornerRadius::same(20);
    visuals.widgets.noninteractive.corner_radius = egui::CornerRadius::same(20);
    visuals.widgets.inactive.corner_radius = rounding;
    visuals.widgets.hovered.corner_radius = rounding;
    visuals.widgets.active.corner_radius = rounding;
    visuals.widgets.open.corner_radius = rounding;
    visuals.window_corner_radius = egui::CornerRadius::from(22);
    visuals.menu_corner_radius = egui::CornerRadius::from(18);
    ctx.set_visuals(visuals);

    let mut style = (*ctx.style()).clone();
    style.spacing.item_spacing = egui::vec2(12.0, 12.0);
    style.spacing.button_padding = egui::vec2(16.0, 12.0);
    style.spacing.interact_size = egui::vec2(50.0, 42.0);
    style.spacing.indent = 20.0;
    style.spacing.slider_width = 140.0;
    style.visuals.widgets.noninteractive.corner_radius = egui::CornerRadius::from(20);
    style.visuals.widgets.inactive.corner_radius = egui::CornerRadius::from(20);
    style.visuals.widgets.hovered.corner_radius = egui::CornerRadius::from(20);
    style.visuals.widgets.active.corner_radius = egui::CornerRadius::from(20);
    style.visuals.widgets.open.corner_radius = egui::CornerRadius::from(20);
    ctx.set_style(style);
}

fn ui_card<R>(ui: &mut egui::Ui, fill: egui::Color32, stroke: Option<egui::Stroke>, rounding: u8, inner: i8, add_contents: impl FnOnce(&mut egui::Ui) -> R) -> R {
    let stroke = stroke.unwrap_or_else(|| egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color));
    egui::Frame::none()
        .fill(fill)
        .stroke(stroke)
        .rounding(egui::CornerRadius::same(rounding))
        .inner_margin(egui::Margin::same(inner))
        .show(ui, add_contents)
        .inner
}

fn primary_card_button(ui: &mut egui::Ui, text: &str, min_width: f32) -> egui::Response {
    ui.add(
        egui::Button::new(egui::RichText::new(text).size(16.0).strong())
            .min_size(egui::vec2(min_width, 48.0))
            .rounding(egui::CornerRadius::same(16)),
    )
}

fn section_label(ui: &mut egui::Ui, text: &str) {
    ui.label(egui::RichText::new(text).size(13.0).strong().color(ui.visuals().text_color()));
}

fn page<R>(ui: &mut egui::Ui, max_w: f32, add_contents: impl FnOnce(&mut egui::Ui) -> R) -> R {
    let max_w = ui.available_width().min(max_w).max(320.0);
    ui.vertical_centered(|ui| {
        ui.set_max_width(max_w);
        ui.with_layout(egui::Layout::top_down(egui::Align::Min), add_contents).inner
    }).inner
}

#[derive(Clone, Copy, PartialEq, Eq)]
enum Screen {
    Home,
    QuickStudy,
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
    WeakWords,
}

struct OpenLingoApp {
    store: Store,
    screen: Screen,
    status: String,
    new_profile_name: String,
    review_index: usize,
    review_queue: Vec<WordEntity>,
    show_answer: bool,
    review_session_total: usize,
    review_session_answered: usize,
    review_session_wrong: Vec<String>,
    review_is_new_words: bool,
    vocab_query: String,
    vocab_level: String,
    show_archived_vocab: bool,
    vocab_status_filter: String,
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
    lesson_practice_pending: bool,
    grammar_unit_level_filter: String,
    show_plan_dialog: bool,
    // Settings drafts / dialogs
    rename_profile_draft: String,
    contesto_draft: String,
    feed_draft: String,
    token_draft: String,
    repo_draft: String,
    settings_op_in_progress: String,
    show_reset_confirm: bool,
    cloud_download_pending: Option<String>,
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
            review_session_total: 0,
            review_session_answered: 0,
            review_session_wrong: Vec::new(),
            review_is_new_words: false,
            vocab_query: String::new(),
            vocab_level: "ALL".into(),
            show_archived_vocab: false,
            vocab_status_filter: "ALL".into(),
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
            lesson_practice_pending: false,
            grammar_unit_level_filter: "ALL".into(),
            show_plan_dialog: false,
            rename_profile_draft: String::new(),
            contesto_draft: String::new(),
            feed_draft: String::new(),
            token_draft: String::new(),
            repo_draft: String::new(),
            settings_op_in_progress: String::new(),
            show_reset_confirm: false,
            cloud_download_pending: None,
        };
        // Default to dark teal theme to better match mobile look (user can change in Settings)
        if app.store.prefs.tema_colore.trim().is_empty() {
            app.store.prefs.tema_colore = "teal".into();
        }
        if app.store.prefs.tema_modalita.trim().is_empty() {
            app.store.prefs.tema_modalita = "dark".into();
        }
        let _ = app.store.save();
        app.backup_path = app.store.base_dir.join("openlingo-backup.json").to_string_lossy().to_string();
        app.sync_settings_drafts_from_store();
        app.refresh_review_queue();
        app
    }

    fn sync_settings_drafts_from_store(&mut self) {
        self.rename_profile_draft = self
            .store
            .active_profile()
            .map(|p| p.config.nome_visualizzato.clone())
            .unwrap_or_default();
        self.contesto_draft = self
            .store
            .active_profile()
            .map(|p| p.config.contesto_utente.clone())
            .unwrap_or_default();
        self.feed_draft = self
            .store
            .active_profile()
            .map(|p| p.config.feed_url.clone())
            .unwrap_or_default();
        self.token_draft = self.store.prefs.github_token.clone();
        self.repo_draft = self.store.prefs.github_repo.clone();
    }

    fn refresh_review_queue(&mut self) {
        let now = chrono::Utc::now().timestamp_millis();
        let mut q = self.store.due_reviews(now, 30);
        let is_new = q.is_empty();
        if q.is_empty() {
            q = self.store.new_words(20);
        }
        self.review_is_new_words = is_new && !q.is_empty();
        self.review_session_total = q.len();
        self.review_session_answered = 0;
        self.review_session_wrong.clear();
        self.review_queue = q;
        self.review_index = 0;
        self.show_answer = false;
    }

    fn start_new_words_session(&mut self) {
        let q = self.store.new_words(20);
        self.review_is_new_words = !q.is_empty();
        self.review_session_total = q.len();
        self.review_session_answered = 0;
        self.review_session_wrong.clear();
        self.review_queue = q;
        self.review_index = 0;
        self.show_answer = false;
        self.screen = Screen::Review;
    }

    fn start_new_words_session_for_level(&mut self, level: &str) {
        let q = self.store.active_profile().map(|p| {
            let reviewed: std::collections::HashSet<_> = p.stato.reviews.iter()
                .filter(|(_, r)| r.repetitions > 0)
                .map(|(id, _)| id.as_str())
                .collect();
            let mut words: Vec<_> = p.stato.parole.iter()
                .filter(|w| !w.archived && w.level.trim().eq_ignore_ascii_case(level) && !reviewed.contains(w.id.as_str()))
                .cloned()
                .collect();
            words.sort_by_key(|w| w.created_at);
            words.truncate(20);
            words
        }).unwrap_or_default();
        self.review_is_new_words = !q.is_empty();
        self.review_session_total = q.len();
        self.review_session_answered = 0;
        self.review_session_wrong.clear();
        self.review_queue = q;
        self.review_index = 0;
        self.show_answer = false;
        self.screen = Screen::Review;
    }

    fn top_bar(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        let title = match self.screen {
            Screen::Home => tr(lang, "Today", "Oggi"),
            Screen::QuickStudy => tr(lang, "Lesson", "Lezione"),
            Screen::Review => tr(lang, "Review", "Ripasso"),
            Screen::Exercises => tr(lang, "Exercises", "Esercizi"),
            Screen::Vocabulary => tr(lang, "Words", "Parole"),
            Screen::Grammar => tr(lang, "Grammar", "Grammatica"),
            Screen::Progress => tr(lang, "Progress", "Progresso"),
            Screen::Settings => tr(lang, "Settings", "Impostazioni"),
        };
        ui.add_space(6.0);
        ui.horizontal(|ui| {
            ui.vertical(|ui| {
                ui.label(egui::RichText::new("OpenLingo").size(13.0).strong().color(ui.visuals().strong_text_color()));
                ui.label(egui::RichText::new(title).size(28.0).strong());
            });
            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                if let Some(prof) = self.store.active_profile() {
                    let initial = prof.config.nome_visualizzato.trim().chars().next().unwrap_or('A').to_uppercase().to_string();
                    let resp = ui.add(egui::Button::new(egui::RichText::new(initial).strong()).min_size(egui::vec2(36.0, 36.0)).rounding(egui::CornerRadius::same(18)));
                    if resp.on_hover_text(tr(lang, "Profile / Settings", "Profilo / Impostazioni")).clicked() {
                        self.screen = Screen::Settings;
                    }
                }
                if ui.small_button("↻").on_hover_text(tr(lang, "Refresh", "Aggiorna")).clicked() {
                    self.refresh_review_queue();
                    self.status = tr(lang, "Refreshed.", "Aggiornato.").to_string();
                }
            });
        });
        ui.add_space(4.0);
        egui::ScrollArea::horizontal().id_salt("top_quick_nav").show(ui, |ui| {
            ui.horizontal_wrapped(|ui| {
                ui.spacing_mut().item_spacing = egui::vec2(8.0, 8.0);
                let quick = [
                    (Screen::QuickStudy, tr(lang, "Lesson", "Lezione")),
                    (Screen::Grammar, tr(lang, "Grammar", "Grammatica")),
                    (Screen::Progress, tr(lang, "Progress", "Progresso")),
                    (Screen::Settings, tr(lang, "Settings", "Impostazioni")),
                ];
                for (scr, label) in quick {
                    let selected = self.screen == scr;
                    let btn = egui::Button::new(if selected { egui::RichText::new(label).strong() } else { egui::RichText::new(label) })
                        .rounding(egui::CornerRadius::same(18))
                        .min_size(egui::vec2(0.0, 32.0));
                    let btn = if selected {
                        btn.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0, ui.visuals().selection.stroke.color))
                    } else {
                        btn.fill(ui.visuals().faint_bg_color)
                    };
                    if ui.add(btn).clicked() { self.screen = scr; }
                }
            });
        });
        ui.add_space(4.0);
        ui.separator();
    }

    fn bottom_nav(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        ui.add_space(4.0);
        let nav_items = [
            (Screen::Home, "🏠", tr(lang, "Home", "Home")),
            (Screen::Review, "🔁", tr(lang, "Review", "Ripasso")),
            (Screen::Exercises, "✏️", tr(lang, "Exercises", "Esercizi")),
            (Screen::Vocabulary, "📚", tr(lang, "Words", "Parole")),
            (Screen::Progress, "📈", tr(lang, "Progress", "Progresso")),
        ];
        let w = ui.available_width();
        let cols = nav_items.len() as f32;
        let btn_w = ((w - (cols - 1.0) * 6.0) / cols).max(80.0);
        ui.horizontal(|ui| {
            ui.spacing_mut().item_spacing = egui::vec2(6.0, 0.0);
            for (scr, icon, label) in nav_items {
                let selected = self.screen == scr;
                let text = egui::RichText::new(format!("{icon}\n{label}")).size(if selected { 13.0 } else { 12.0 });
                let text = if selected { text.strong() } else { text };
                let mut btn = egui::Button::new(text).min_size(egui::vec2(btn_w, 52.0)).rounding(egui::CornerRadius::same(18));
                if selected {
                    btn = btn.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0, ui.visuals().selection.stroke.color));
                } else {
                    btn = btn.fill(ui.visuals().widgets.inactive.bg_fill);
                }
                if ui.add(btn).clicked() { self.screen = scr; }
            }
        });
        ui.add_space(4.0);
    }

    fn apri_passo(&mut self, azione: &str) {
        let a = azione.trim().to_lowercase();
        if a == "ripasso" || a == "review" {
            self.refresh_review_queue();
            self.screen = Screen::Review;
        } else if a == "wortliste" || a == "parole" || a == "vocabulary" {
            self.screen = Screen::Vocabulary;
        } else if let Some(id) = azione.strip_prefix("unita_grammatica/").or_else(|| azione.strip_prefix("unita-grammatica/")).or_else(|| azione.strip_prefix("grammar_unit/")) {
            self.grammar_unit_id = Some(id.to_string());
            self.screen = Screen::Grammar;
        } else if a == "grammatica" || a == "grammar" {
            self.screen = Screen::Grammar;
        } else {
            self.exercise_mode = ExerciseMode::Mixed;
            self.screen = Screen::Exercises;
            self.start_exercise_session();
        }
    }

    fn screen_home(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        let piano = self.store.piano_oggi();
        let nome = self.store.active_profile()
            .map(|p| p.config.nome_visualizzato.split_whitespace().next().unwrap_or(&p.config.nome_visualizzato).to_string())
            .unwrap_or_else(|| tr(lang, "learner", "studente").into());
        let prossima_unita = self.store.prossima_unita();
        let (total, _reviewed, due, streak) = self.store.stats();
        let in_review_total = self.store.active_profile().map(|p| p.stato.parole.iter().filter(|w| !w.archived && p.stato.reviews.contains_key(&w.id)).count()).unwrap_or(0);

        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    ui.label(egui::RichText::new(format!("{}, {nome}", tr(lang, "Hello", "Ciao"))).size(34.0).strong().color(ui.visuals().text_color()));
                    ui.label(egui::RichText::new(Local::now().format(if lang == "it" { "%A %d %B" } else { "%A, %B %d" }).to_string()).size(16.0).color(ui.visuals().text_color()));
                    ui.add_space(20.0);

                    // Hero: Today's plan
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0, ui.visuals().selection.stroke.color))
                        .rounding(egui::CornerRadius::same(24))
                        .inner_margin(egui::Margin::same(22))
                        .show(ui, |ui| {
                            ui.label(egui::RichText::new(tr(lang, "TODAY'S PLAN", "PIANO DI OGGI")).size(12.0).strong().color(ui.visuals().text_color()));
                            ui.add_space(8.0);
                            if let Some(p) = piano.prossimo_passo().cloned() {
                                ui.label(egui::RichText::new(&p.titolo).size(28.0).strong());
                                ui.add_space(4.0);
                                ui.label(egui::RichText::new(&p.descrizione).size(15.0).color(ui.visuals().text_color()));
                                ui.add_space(14.0);
                                let pct = if piano.minuti_totali > 0 { (piano.minuti_completati * 100) / piano.minuti_totali.max(1) } else { 0 };
                                ui.add(egui::ProgressBar::new(pct as f32 / 100.0).desired_height(8.0).rounding(egui::CornerRadius::same(8)));
                                ui.add_space(10.0);
                                ui.horizontal_wrapped(|ui| {
                                    if ui.add(egui::Button::new(tr(lang, "View plan", "Vedi piano")).min_size(egui::vec2(140.0, 44.0)).rounding(egui::CornerRadius::same(22))).clicked() { self.show_plan_dialog = true; }
                                    let start_label = if piano.passi_completati() == 0 { tr(lang, "Start", "Inizia") } else { tr(lang, "Continue", "Continua") };
                                    if ui.add(egui::Button::new(egui::RichText::new(start_label).strong()).min_size(egui::vec2(140.0, 44.0)).rounding(egui::CornerRadius::same(22))).clicked() { self.apri_passo(&p.azione); }
                                });
                            } else {
                                ui.label(egui::RichText::new(tr(lang, "Plan completed", "Piano completato")).size(24.0).strong());
                                ui.add_space(4.0);
                                ui.label(egui::RichText::new(&piano.messaggio_motivazionale).color(ui.visuals().text_color()));
                            }
                        });

                    ui.add_space(20.0);
                    // Quick Action Tiles
                    ui.columns(4, |cols| {
                        let actions = [
                            (tr(lang, "Exercises", "Esercizi"), "Ex", Screen::Exercises),
                            (tr(lang, "Review", "Ripasso"), "Rv", Screen::Review),
                            (tr(lang, "Grammar", "Grammatica"), "Gr", Screen::Grammar),
                            (tr(lang, "Progress", "Progresso"), "Pr", Screen::Progress),
                        ];
                        for (i, (title, icon, screen)) in actions.into_iter().enumerate() {
                            let resp = egui::Frame::none()
                                .fill(cols[i].visuals().faint_bg_color)
                                .stroke(egui::Stroke::new(1.0_f32, cols[i].visuals().widgets.noninteractive.bg_stroke.color))
                                .rounding(egui::CornerRadius::same(20))
                                .inner_margin(egui::Margin::symmetric(10, 14))
                                .show(&mut cols[i], |ui| {
                                    ui.set_min_height(92.0);
                                    ui.vertical_centered(|ui| {
                                        ui.add_space(6.0);
                                        ui.label(egui::RichText::new(icon).size(24.0).strong());
                                        ui.add_space(8.0);
                                        ui.label(egui::RichText::new(title).size(14.0).strong());
                                    });
                                }).response.interact(egui::Sense::click());
                            if resp.clicked() {
                                if screen == Screen::Review { self.refresh_review_queue(); }
                                self.screen = screen;
                            }
                        }
                    });

                    ui.add_space(20.0);
                    // Continue the path (Next Unit)
                    if let Some(u) = prossima_unita {
                         ui.label(egui::RichText::new(tr(lang, "CONTINUE LEARNING", "CONTINUA IL PERCORSO")).size(14.0).strong().color(ui.visuals().text_color()));
                         ui.add_space(8.0);
                         egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(22))
                            .inner_margin(egui::Margin::same(20))
                            .show(ui, |ui| {
                                ui.label(egui::RichText::new(if lang == "it" { u.title_it } else { u.title_en }).size(22.0).strong());
                                ui.label(egui::RichText::new(format!("{} · {} · {} min", u.level, if lang == "it" { u.subtitle_it } else { u.subtitle_en }, 14)).color(ui.visuals().text_color()));
                                ui.add_space(12.0);
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Continue", "Continua")).strong()).min_size(egui::vec2(140.0, 42.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                    self.grammar_unit_id = Some(u.id.to_string());
                                    self.screen = Screen::Grammar;
                                }
                            });
                    }
                });
            });
        });
    }

    fn screen_quick_study(&mut self, ui: &mut egui::Ui) {
        let lang_s = ui_lang(&self.store).to_string();
        let lang = lang_s.as_str();
        if self.store.active_profile().is_none() {
            ui.label(tr(lang, "No active profile.", "Nessun profilo attivo."));
            return;
        }
        let plan = self.store.study_plan_summary();
        let (total, _reviewed, due, streak) = self.store.stats();
        let in_review_total = self.store.active_profile().map(|p| p.stato.parole.iter().filter(|w| !w.archived && p.stato.reviews.contains_key(&w.id)).count()).unwrap_or(0);
        let lesson_n = plan.lezione_corrente.max(1);
        let (topic_it, topic_en) = lesson_topic(&plan.livello_corrente, lesson_n);
        let topic = if lang == "it" { topic_it } else { topic_en };
        let due_preview = self.store.due_reviews(chrono::Utc::now().timestamp_millis(), 6);
        let showing_due_preview = !due_preview.is_empty();
        let preview_words: Vec<WordEntity> = if showing_due_preview { due_preview } else { self.store.new_words(6) };

        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    ui.label(egui::RichText::new(if lang == "it" { "LEZIONE" } else { "LESSON" }).size(13.0).strong().color(ui.visuals().strong_text_color()));
                    ui.add_space(8.0);

                    // Lesson hero
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color))
                        .rounding(egui::CornerRadius::same(28))
                        .inner_margin(egui::Margin::same(22))
                        .show(ui, |ui| {
                            ui.horizontal(|ui| {
                                egui::Frame::none()
                                    .fill(ui.visuals().widgets.active.bg_fill)
                                    .rounding(egui::CornerRadius::same(18))
                                    .inner_margin(egui::Margin::symmetric(14, 10))
                                    .show(ui, |ui| {
                                        ui.label(egui::RichText::new(format!("{lesson_n}")).size(24.0).strong().color(ui.visuals().widgets.active.fg_stroke.color));
                                    });
                                ui.add_space(12.0);
                                ui.vertical(|ui| {
                                    ui.label(egui::RichText::new(&topic).size(28.0).strong());
                                    ui.label(egui::RichText::new(if lang == "it" { format!("{} · Obiettivo {}", plan.livello_corrente, plan.obiettivo_livello) } else { format!("{} · Goal {}", plan.livello_corrente, plan.obiettivo_livello) }).color(ui.visuals().strong_text_color()));
                                });
                            });
                            ui.add_space(12.0);
                            ui.label(egui::RichText::new(tr(lang, "Theory and guided practice for this lesson.", "Teoria ed esercizi guidati per questa lezione.")).color(ui.visuals().strong_text_color()));
                            ui.add_space(16.0);
                            ui.horizontal_wrapped(|ui| {
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Open lesson", "Apri lezione")).strong()).min_size(egui::vec2(160.0, 44.0)).rounding(egui::CornerRadius::same(22))).clicked() {
                                    self.screen = Screen::Grammar;
                                }
                                ui.add_space(6.0);
                                if ui.add(egui::Button::new(tr(lang, "Practice lesson", "Pratica lezione")).min_size(egui::vec2(170.0, 44.0)).rounding(egui::CornerRadius::same(22))).clicked() {
                                    self.exercise_mode = ExerciseMode::GuidedSession;
                                    self.lesson_practice_pending = true;
                                    self.screen = Screen::Exercises;
                                    self.start_exercise_session();
                                }
                            });
                        });

                    ui.add_space(16.0);
                    ui.columns(3, |cols| {
                        let items = [
                            (format!("{due}"), tr(lang, "Due", "Da ripassare")),
                            (format!("{}", plan.new_words.min(20)), tr(lang, "New today", "Nuove oggi")),
                            (format!("{streak}"), tr(lang, "Streak", "Serie")),
                        ];
                        for (i, (val, label)) in items.into_iter().enumerate() {
                            egui::Frame::none()
                                .fill(cols[i].visuals().faint_bg_color)
                                .stroke(egui::Stroke::new(1.0_f32, cols[i].visuals().widgets.noninteractive.bg_stroke.color))
                                .rounding(egui::CornerRadius::same(18))
                                .inner_margin(egui::Margin::symmetric(14, 14))
                                .show(&mut cols[i], |ui| {
                                    ui.vertical_centered(|ui| {
                                        ui.label(egui::RichText::new(val).size(28.0).strong());
                                        ui.small(label.to_string());
                                    });
                                });
                        }
                    });

                    ui.add_space(16.0);
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                        .rounding(egui::CornerRadius::same(22))
                        .inner_margin(egui::Margin::same(18))
                        .show(ui, |ui| {
                            ui.label(egui::RichText::new(tr(lang, "Start now", "Inizia ora")).size(20.0).strong());
                            ui.add_space(10.0);
                            ui.columns(2, |cols| {
                                if cols[0].add(egui::Button::new(if lang == "it" { format!("Ripasso ({due})") } else { format!("Review ({due})") }).min_size(egui::vec2(cols[0].available_width(), 46.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                    self.refresh_review_queue();
                                    self.screen = Screen::Review;
                                }
                                if cols[1].add(egui::Button::new(if lang == "it" { format!("Nuove parole ({})", plan.new_words.min(20)) } else { format!("New words ({})", plan.new_words.min(20)) }).min_size(egui::vec2(cols[1].available_width(), 46.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                    self.start_new_words_session();
                                }
                            });
                            ui.add_space(8.0);
                            ui.columns(2, |cols| {
                                if cols[0].add(egui::Button::new(tr(lang, "Guided session", "Sessione guidata")).min_size(egui::vec2(cols[0].available_width(), 44.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                    self.exercise_mode = ExerciseMode::GuidedSession;
                                    self.screen = Screen::Exercises;
                                    self.start_exercise_session();
                                }
                                if cols[1].add(egui::Button::new(tr(lang, "Exercises", "Esercizi")).min_size(egui::vec2(cols[1].available_width(), 44.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                    self.screen = Screen::Exercises;
                                }
                            });
                        });

                    ui.add_space(16.0);
                    egui::Frame::none()
                        .fill(ui.visuals().extreme_bg_color.gamma_multiply(0.98))
                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                        .rounding(egui::CornerRadius::same(22))
                        .inner_margin(egui::Margin::same(18))
                        .show(ui, |ui| {
                            ui.horizontal(|ui| {
                                ui.label(egui::RichText::new(if showing_due_preview { tr(lang, "Words to review", "Parole da ripassare") } else { tr(lang, "New words", "Nuove parole") }).size(20.0).strong());
                                ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                    ui.small(format!("{in_review_total}/{total}"));
                                });
                            });
                            ui.label(egui::RichText::new(tr(lang, "Preview of words for this session.", "Anteprima delle parole per questa sessione.")).color(ui.visuals().strong_text_color()));
                            ui.add_space(10.0);
                            if preview_words.is_empty() {
                                ui.label(tr(lang, "No words to preview right now.", "Nessuna parola da mostrare ora."));
                            } else {
                                for w in &preview_words {
                                    egui::Frame::none()
                                        .fill(ui.visuals().faint_bg_color)
                                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                                        .rounding(egui::CornerRadius::same(16))
                                        .inner_margin(egui::Margin::symmetric(14, 12))
                                        .show(ui, |ui| {
                                            ui.horizontal(|ui| {
                                                ui.vertical(|ui| {
                                                    ui.label(egui::RichText::new(w.label()).size(18.0).strong());
                                                    ui.label(&w.italian);
                                                    if !w.example.trim().is_empty() {
                                                        ui.label(egui::RichText::new(&w.example).italics().small().color(ui.visuals().strong_text_color()));
                                                    }
                                                });
                                                ui.with_layout(egui::Layout::right_to_left(egui::Align::Min), |ui| {
                                                    if !w.level.is_empty() {
                                                        egui::Frame::none()
                                                            .fill(ui.visuals().selection.bg_fill.gamma_multiply(0.6))
                                                            .rounding(egui::CornerRadius::same(10))
                                                            .inner_margin(egui::Margin::symmetric(8, 3))
                                                            .show(ui, |ui| { ui.small(&w.level); });
                                                    }
                                                });
                                            });
                                        });
                                    ui.add_space(8.0);
                                }
                            }
                            ui.add_space(4.0);
                            ui.horizontal_wrapped(|ui| {
                                if ui.add(egui::Button::new(tr(lang, "Open Words", "Apri Parole")).rounding(egui::CornerRadius::same(14))).clicked() { self.screen = Screen::Vocabulary; }
                                if ui.add(egui::Button::new(tr(lang, "Progress", "Progresso")).rounding(egui::CornerRadius::same(14))).clicked() { self.screen = Screen::Progress; }
                            });
                        });

                    ui.add_space(24.0);
                });
            });
        });
    }
    fn answer_current(&mut self, saputa: bool) {
        if self.review_queue.is_empty() { return; }
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        let idx = self.review_index.min(self.review_queue.len().saturating_sub(1));
        let word = self.review_queue[idx].clone();
        match self.store.answer_review(&word.id, saputa) {
            Ok(_) => {
                self.review_session_answered += 1;
                if !saputa {
                    self.review_session_wrong.push(word.label());
                }
                self.status.clear();
                self.review_queue.remove(idx);
                if self.review_index >= self.review_queue.len() && self.review_index > 0 {
                    self.review_index -= 1;
                }
                self.show_answer = false;
            }
            Err(e) => self.status = format!("{}: {e}", tr(lang, "Save error", "Errore salvataggio")),
        }
    }

    fn screen_review(&mut self, ui: &mut egui::Ui) {
        let lang_s = ui_lang(&self.store).to_string();
        let lang = lang_s.as_str();
        let (total, _reviewed, due, _streak) = self.store.stats();
        let nuove = self.store.new_words(1000).len();
        let mature = self.store.active_profile().map(|p| {
            p.stato.parole.iter().filter(|w| !w.archived).filter(|w| p.stato.reviews.get(&w.id).map(|r| r.interval_days >= 21).unwrap_or(false)).count()
        }).unwrap_or(0);

        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    // Testata-like header
                    ui.label(egui::RichText::new(tr(lang, "Review", "Ripasso")).size(32.0).strong());
                    ui.label(egui::RichText::new(if lang == "it" {
                        format!("{due} in scadenza · {nuove} nuove da vedere · {mature} parole mature · ripetizione spaziata")
                    } else {
                        format!("{due} due · {nuove} new to see · {mature} mature words · spaced repetition")
                    }).color(ui.visuals().strong_text_color()));
                    ui.add_space(16.0);

                    if self.review_queue.is_empty() {
                        if self.review_session_total > 0 && self.review_session_answered > 0 {
                            // Sessione completata
                            egui::Frame::none()
                                .fill(ui.visuals().faint_bg_color)
                                .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                                .rounding(egui::CornerRadius::same(20))
                                .inner_margin(egui::Margin::same(20))
                                .show(ui, |ui| {
                                    ui.label(egui::RichText::new(tr(lang, "Session completed", "Sessione completata")).size(22.0).strong());
                                    ui.label(egui::RichText::new(if lang == "it" { format!("{} carte ripassate", self.review_session_total) } else { format!("{} cards reviewed", self.review_session_total) }).color(ui.visuals().strong_text_color()));
                                    ui.add_space(12.0);
                                    ui.horizontal(|ui| {
                                        ui.label(egui::RichText::new(format!("{} / {}", self.review_session_answered.saturating_sub(self.review_session_wrong.len()), self.review_session_total.max(self.review_session_answered))).size(30.0).strong().color(ui.visuals().selection.stroke.color));
                                        ui.add_space(8.0);
                                        ui.label(egui::RichText::new(tr(lang, "answers \"I knew it\"", "risposte \"lo sapevo\"")).color(ui.visuals().strong_text_color()));
                                    });
                                    if !self.review_session_wrong.is_empty() {
                                        ui.add_space(8.0);
                                        ui.colored_label(egui::Color32::from_rgb(200, 90, 90), if lang == "it" { format!("Da rivedere: {}", self.review_session_wrong.join(", ")) } else { format!("To review: {}", self.review_session_wrong.join(", ")) });
                                    }
                                    ui.add_space(12.0);
                                    ui.label(egui::RichText::new(tr(lang,
                                        "Known cards come back after 1, 3, 7, 16, 35, 75, 150 or 300 days; missed cards come back tomorrow and lower ease.",
                                        "Le carte sapute tornano tra 1, 3, 7, 16, 35, 75, 150 o 300 giorni; quelle sbagliate tornano domani e abbassano la facilità."
                                    )).small().color(ui.visuals().strong_text_color()));
                                    ui.add_space(16.0);
                                    ui.horizontal_wrapped(|ui| {
                                        if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "New session", "Nuova sessione")).strong()).min_size(egui::vec2(150.0, 42.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.refresh_review_queue(); }
                                        if ui.add(egui::Button::new(tr(lang, "Go to Words", "Vai a Parole")).min_size(egui::vec2(150.0, 42.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.screen = Screen::Vocabulary; }
                                        if ui.add(egui::Button::new(tr(lang, "Home", "Home")).min_size(egui::vec2(120.0, 42.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.screen = Screen::Home; }
                                    });
                                });
                        } else {
                            // Nessuna carta
                            egui::Frame::none()
                                .fill(ui.visuals().faint_bg_color)
                                .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                                .rounding(egui::CornerRadius::same(20))
                                .inner_margin(egui::Margin::same(20))
                                .show(ui, |ui| {
                                    ui.label(egui::RichText::new(tr(lang, "No cards to review", "Nessuna carta da ripassare")).size(22.0).strong());
                                    ui.label(egui::RichText::new(tr(lang, "The profile vocabulary is empty or everything is already in place", "Il lessico del profilo è vuoto oppure tutto è già al suo posto")).color(ui.visuals().strong_text_color()));
                                    ui.add_space(10.0);
                                    ui.label(tr(lang,
                                        "Add words from the word list: they enter the queue immediately and review starts from there.",
                                        "Aggiungi parole dalla Wortliste: entrano subito nella coda e il ripasso parte da lì."
                                    ));
                                    ui.add_space(10.0);
                                    ui.label(egui::RichText::new(if lang == "it" { format!("Parole disponibili nella Wortliste: {total}") } else { format!("Words available in word list: {total}") }).color(ui.visuals().selection.stroke.color));
                                    ui.add_space(14.0);
                                    ui.horizontal_wrapped(|ui| {
                                        if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Go to Words", "Vai a Parole")).strong()).min_size(egui::vec2(160.0, 42.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.screen = Screen::Vocabulary; }
                                        if ui.add(egui::Button::new(tr(lang, "Refresh", "Aggiorna")).min_size(egui::vec2(120.0, 42.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.refresh_review_queue(); }
                                    });
                                });
                        }
                        if !self.status.is_empty() { ui.add_space(10.0); ui.label(&self.status); }
                        ui.add_space(20.0);
                    } else {
                        // Active flashcard session
                        let idx = self.review_index.min(self.review_queue.len().saturating_sub(1));
                        let session_total = self.review_session_total.max(self.review_queue.len() + self.review_session_answered);
                        let shown_index = (self.review_session_answered + 1).min(session_total.max(1));
                        let progress = if session_total > 0 { shown_index as f32 / session_total as f32 } else { 0.0 };
                        let word = self.review_queue[idx].clone();
                        let is_new = self.store.active_profile().and_then(|p| p.stato.reviews.get(&word.id)).map(|r| r.repetitions == 0).unwrap_or(true);
                        let reps = self.store.active_profile().and_then(|p| p.stato.reviews.get(&word.id)).map(|r| r.repetitions).unwrap_or(0);

                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(24))
                            .inner_margin(egui::Margin::same(24))
                            .show(ui, |ui| {
                                ui.horizontal(|ui| {
                                    let chip_text = if is_new { tr(lang, "New", "Nuova").to_string() } else { if lang == "it" { format!("Ripasso {reps}") } else { format!("Review {reps}") } };
                                    egui::Frame::none()
                                        .fill(ui.visuals().selection.bg_fill.gamma_multiply(0.55))
                                        .rounding(egui::CornerRadius::same(10))
                                        .inner_margin(egui::Margin::symmetric(10, 4))
                                        .show(ui, |ui| { ui.label(egui::RichText::new(chip_text).small().strong()); });
                                    ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                        ui.label(egui::RichText::new(format!("{shown_index} / {session_total}")).color(ui.visuals().strong_text_color()));
                                    });
                                });
                                ui.add_space(10.0);
                                ui.add(egui::ProgressBar::new(progress.clamp(0.0, 1.0)).desired_height(6.0).rounding(egui::CornerRadius::same(6)));

                                ui.add_space(28.0);
                                ui.vertical_centered(|ui| {
                                    ui.label(egui::RichText::new(word.label()).size(42.0).strong());
                                    if self.show_answer && !word.example.trim().is_empty() {
                                        ui.add_space(10.0);
                                        ui.label(egui::RichText::new(&word.example).color(ui.visuals().strong_text_color()));
                                    }
                                });

                                ui.add_space(24.0);
                                if !self.show_answer {
                                    ui.vertical_centered(|ui| {
                                        if ui.add(egui::Button::new(tr(lang, "Show translation", "Mostra traduzione")).min_size(egui::vec2(220.0, 44.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                            self.show_answer = true;
                                        }
                                    });
                                } else {
                                    egui::Frame::none()
                                        .fill(ui.visuals().selection.bg_fill.gamma_multiply(0.45))
                                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color))
                                        .rounding(egui::CornerRadius::same(16))
                                        .inner_margin(egui::Margin::same(18))
                                        .show(ui, |ui| {
                                            ui.vertical_centered(|ui| {
                                                ui.label(egui::RichText::new(&word.italian).size(30.0).strong());
                                                if let Some(article) = &word.article {
                                                    if !article.trim().is_empty() {
                                                        ui.add_space(4.0);
                                                        ui.label(egui::RichText::new(if lang == "it" { format!("articolo: {article}") } else { format!("article: {article}") }).color(ui.visuals().strong_text_color()));
                                                    }
                                                }
                                            });
                                        });
                                    ui.add_space(16.0);
                                    ui.horizontal(|ui| {
                                        let half = (ui.available_width() - 12.0) / 2.0;
                                        if ui.add(egui::Button::new(tr(lang, "I didn't know", "Non lo sapevo")).min_size(egui::vec2(half.max(120.0), 46.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                            self.answer_current(false);
                                        }
                                        ui.add_space(12.0);
                                        if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "I knew it", "Lo sapevo")).strong()).min_size(egui::vec2(half.max(120.0), 46.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                            self.answer_current(true);
                                        }
                                    });
                                }
                            });

                        ui.add_space(14.0);
                        ui.label(egui::RichText::new(tr(lang,
                            "Review also updates statistics: accuracy, mature words and study days.",
                            "Il ripasso aggiorna anche le statistiche: accuratezza, parole mature e giorni di studio."
                        )).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(10.0);
                        ui.horizontal_wrapped(|ui| {
                            if ui.button(tr(lang, "Refresh queue", "Aggiorna coda")).clicked() { self.refresh_review_queue(); }
                            if ui.button(tr(lang, "Home", "Home")).clicked() { self.screen = Screen::Home; }
                        });
                        if !self.status.is_empty() { ui.add_space(8.0); ui.label(&self.status); }
                        ui.add_space(20.0);
                    }
                });
            });
        });
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
            ExerciseMode::WeakWords => {
                let mut pool = self.store.difficult_words(40);
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
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        if self.store.active_profile().is_none() {
            ui.label(tr(lang, "No active profile.", "Nessun profilo attivo."));
            return;
        }

        // Active session / completion states (mobile-like)
        if !self.exercise_queue.is_empty() {
            egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
                let max_w = ui.available_width().min(920.0).max(320.0);
                ui.vertical_centered(|ui| {
                    ui.set_max_width(max_w);
                    ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                        ui.add_space(8.0);

                        // Completion
                        if self.exercise_index >= self.exercise_queue.len() {
                            egui::Frame::none()
                                .fill(ui.visuals().faint_bg_color)
                                .stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color))
                                .rounding(egui::CornerRadius::same(26))
                                .inner_margin(egui::Margin::same(22))
                                .show(ui, |ui| {
                                    ui.vertical_centered(|ui| {
                                        ui.label(egui::RichText::new(tr(lang, "Session complete", "Sessione completata")).size(30.0).strong());
                                        ui.add_space(8.0);
                                        if self.exercise_total > 0 {
                                            let pct = (self.exercise_correct * 100) / self.exercise_total;
                                            let wrong = self.exercise_total.saturating_sub(self.exercise_correct);
                                            ui.label(egui::RichText::new(format!("{pct}%")).size(44.0).strong());
                                            ui.add_space(8.0);
                                            ui.add(egui::ProgressBar::new(pct as f32 / 100.0).desired_height(10.0).rounding(egui::CornerRadius::same(8)));
                                            ui.add_space(10.0);
                                            ui.label(if lang == "it" {
                                                format!("Corrette: {}  ?  Sbagliate: {}  ?  Totale: {}", self.exercise_correct, wrong, self.exercise_total)
                                            } else {
                                                format!("Correct: {}  ?  Wrong: {}  ?  Total: {}", self.exercise_correct, wrong, self.exercise_total)
                                            });
                                        } else {
                                            ui.label(tr(lang, "No questions answered.", "Nessuna domanda risposta."));
                                        }
                                    });
                                });

                            if self.lesson_practice_pending {
                                ui.add_space(12.0);
                                egui::Frame::none()
                                    .fill(ui.visuals().faint_bg_color)
                                    .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                                    .rounding(egui::CornerRadius::same(18))
                                    .inner_margin(egui::Margin::same(16))
                                    .show(ui, |ui| {
                                        ui.label(egui::RichText::new(tr(lang, "Lesson practice finished", "Pratica lezione terminata")).strong());
                                        ui.label(tr(lang, "You can mark this lesson as complete.", "Puoi segnare questa lezione come completata."));
                                        ui.add_space(10.0);
                                        if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Mark lesson complete / Next", "Segna lezione completata / Avanti")).strong()).min_size(egui::vec2(ui.available_width(), 44.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                            match self.store.advance_lesson_progress() {
                                                Ok(n) => {
                                                    self.status = if lang == "it" { format!("Lezione aggiornata: {n}") } else { format!("Lesson updated: {n}") };
                                                    self.lesson_practice_pending = false;
                                                }
                                                Err(e) => self.status = format!("{}: {e}", tr(lang, "Update failed", "Aggiornamento fallito")),
                                            }
                                        }
                                    });
                            }

                            if !self.exercise_feedback.is_empty() {
                                ui.add_space(10.0);
                                ui.label(&self.exercise_feedback);
                            }

                            ui.add_space(14.0);
                            ui.horizontal_wrapped(|ui| {
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Restart", "Ricomincia")).strong()).min_size(egui::vec2(150.0, 44.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.start_exercise_session(); }
                                if ui.add(egui::Button::new(tr(lang, "Back to exercises", "Torna agli esercizi")).min_size(egui::vec2(170.0, 44.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.exercise_queue.clear(); self.exercise_index = 0; }
                                if ui.add(egui::Button::new(tr(lang, "Review", "Ripasso")).min_size(egui::vec2(120.0, 44.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.refresh_review_queue(); self.screen = Screen::Review; }
                                if ui.add(egui::Button::new(tr(lang, "Home", "Home")).min_size(egui::vec2(110.0, 44.0)).rounding(egui::CornerRadius::same(16))).clicked() { self.screen = Screen::Home; }
                            });
                            ui.add_space(16.0);
                            return;
                        }

                        // Active question
                        let q = self.exercise_queue[self.exercise_index].clone();
                        let total = self.exercise_queue.len().max(1);
                        ui.horizontal(|ui| {
                            ui.label(egui::RichText::new(tr(lang, "Exercise", "Esercizio")).size(30.0).strong());
                            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                ui.label(egui::RichText::new(if lang == "it" {
                                    format!("Domanda {}/{}", self.exercise_index + 1, total)
                                } else {
                                    format!("Question {}/{}", self.exercise_index + 1, total)
                                }).strong());
                            });
                        });
                        ui.add_space(6.0);
                        let ex_progress = (self.exercise_index as f32 / total as f32).clamp(0.0, 1.0);
                        ui.add(egui::ProgressBar::new(ex_progress).desired_height(8.0).rounding(egui::CornerRadius::same(8)));
                        ui.small(if lang == "it" {
                            format!("Punteggio: {} / {}", self.exercise_correct, self.exercise_total)
                        } else {
                            format!("Score: {} / {}", self.exercise_correct, self.exercise_total)
                        });
                        ui.add_space(12.0);

                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(22))
                            .inner_margin(egui::Margin::same(18))
                            .show(ui, |ui| {
                                ui.label(egui::RichText::new(&q.prompt).size(24.0).strong());
                                ui.add_space(16.0);
                                match q.kind {
                                    ExerciseKind::ArticleQuiz | ExerciseKind::MeaningChoice | ExerciseKind::GermanChoice => {
                                        for opt in &q.options {
                                            let resp = ui.add(
                                                egui::Button::new(egui::RichText::new(opt).size(16.0))
                                                    .min_size(egui::vec2(ui.available_width(), 50.0))
                                                    .rounding(egui::CornerRadius::same(16))
                                                    .fill(ui.visuals().extreme_bg_color)
                                                    .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)),
                                            );
                                            if resp.clicked() {
                                                self.submit_exercise_answer(opt.clone());
                                            }
                                            ui.add_space(8.0);
                                        }
                                    }
                                    ExerciseKind::Typing => {
                                        ui.add(
                                            egui::TextEdit::singleline(&mut self.exercise_answer)
                                                .hint_text(tr(lang, "Type your answer...", "Scrivi la risposta..."))
                                                .desired_width(f32::INFINITY)
                                                .font(egui::TextStyle::Heading),
                                        );
                                        ui.add_space(12.0);
                                        if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Submit", "Invia")).strong()).min_size(egui::vec2(ui.available_width(), 46.0)).rounding(egui::CornerRadius::same(16))).clicked() {
                                            let ans = self.exercise_answer.clone();
                                            self.submit_exercise_answer(ans);
                                        }
                                    }
                                }
                            });

                        if !self.exercise_feedback.is_empty() {
                            ui.add_space(10.0);
                            let ok = self.exercise_feedback.contains("Correct") || self.exercise_feedback.contains("Corretto");
                            let color = if ok { egui::Color32::from_rgb(70, 160, 110) } else { egui::Color32::from_rgb(210, 90, 90) };
                            egui::Frame::none()
                                .fill(ui.visuals().extreme_bg_color)
                                .stroke(egui::Stroke::new(1.0_f32, color))
                                .rounding(egui::CornerRadius::from(14))
                                .inner_margin(egui::Margin::same(12))
                                .show(ui, |ui| {
                                    ui.colored_label(color, egui::RichText::new(&self.exercise_feedback).strong());
                                });
                        }

                        ui.add_space(12.0);
                        if ui.add(egui::Button::new(tr(lang, "Back to exercise list", "Torna alla lista esercizi")).rounding(egui::CornerRadius::same(14))).clicked() {
                            self.exercise_queue.clear();
                            self.exercise_index = 0;
                        }
                        if !self.status.is_empty() { ui.add_space(6.0); ui.small(&self.status); }
                        ui.add_space(16.0);
                    });
                });
            });
            return;
        }

        // Hub view (mobile-like)
        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    ui.label(egui::RichText::new(tr(lang, "Exercises", "Esercizi")).size(34.0).strong());
                    ui.label(egui::RichText::new(tr(lang, "Choose a type and start practicing with your profile data.", "Scegli un tipo e allenati con i dati del tuo profilo.")).size(16.0).color(ui.visuals().strong_text_color()));
                    ui.add_space(16.0);

                    let plan = self.store.study_plan_summary();
                    let weak_count = self.store.difficult_words(200).len();
                    let (active_words, article_words_for_level) = self.store.active_profile().map(|p| {
                        let active: Vec<_> = p.stato.parole.iter().filter(|w| !w.archived).cloned().collect();
                        let art = data::words_up_to_level(&active, &self.exercise_level, true).len();
                        (active.len(), art)
                    }).unwrap_or((0, 0));

                    let (s_title, s_reason, s_mode, go_grammar) = if plan.due_words > 0 {
                        (tr(lang, "Guided session (due + new)", "Sessione guidata (ripasso + nuove)"),
                         if lang == "it" { format!("Hai {} parole da ripassare: inizia da qui.", plan.due_words) } else { format!("You have {} due words: start here.", plan.due_words) },
                         Some(ExerciseMode::GuidedSession), false)
                    } else if plan.grammar_due > 0 {
                        (tr(lang, "Grammar review", "Ripasso grammatica"),
                         if lang == "it" { format!("Hai {} unità di grammatica da ripassare.", plan.grammar_due) } else { format!("You have {} grammar units due for review.", plan.grammar_due) },
                         None, true)
                    } else if weak_count > 0 {
                        (tr(lang, "Weak words review", "Ripasso parole fragili"),
                         if lang == "it" { format!("Ci sono {weak_count} parole che meritano pratica extra.") } else { format!("There are {weak_count} words that need extra practice.") },
                         Some(ExerciseMode::WeakWords), false)
                    } else {
                        (tr(lang, "Mixed practice", "Pratica mista"),
                         tr(lang, "A balanced mix to keep progressing.", "Un mix equilibrato per continuare a progredire.").to_string(),
                         Some(ExerciseMode::Mixed), false)
                    };

                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color))
                        .rounding(egui::CornerRadius::same(24))
                        .inner_margin(egui::Margin::same(20))
                        .show(ui, |ui| {
                            ui.label(egui::RichText::new(tr(lang, "CONSIGLIATO ORA", "SUGGESTED NOW")).size(12.0).strong().color(ui.visuals().strong_text_color()));
                            ui.add_space(6.0);
                            ui.label(egui::RichText::new(s_title).size(24.0).strong());
                            ui.label(egui::RichText::new(&s_reason).color(ui.visuals().strong_text_color()));
                            ui.add_space(14.0);
                            if go_grammar {
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Open Grammar", "Apri Grammatica")).strong()).min_size(egui::vec2(220.0, 44.0)).rounding(egui::CornerRadius::same(18))).clicked() {
                                    self.screen = Screen::Grammar;
                                }
                            } else if let Some(mode) = s_mode {
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Start", "Inizia")).strong()).min_size(egui::vec2(220.0, 44.0)).rounding(egui::CornerRadius::same(18))).clicked() {
                                    self.exercise_mode = mode;
                                    self.start_exercise_session();
                                }
                            }
                        });

                    ui.add_space(14.0);
                    ui.horizontal_wrapped(|ui| {
                        if matches!(self.exercise_mode, ExerciseMode::ArticleQuiz | ExerciseMode::Mixed | ExerciseMode::GuidedSession | ExerciseMode::WeakWords) {
                            ui.label(egui::RichText::new(if lang == "it" { "Livello:" } else { "Level:" }).strong());
                            egui::ComboBox::from_id_salt("ex_level")
                                .selected_text(self.exercise_level.clone())
                                .show_ui(ui, |ui| {
                                    for opt in ["A1", "A2", "B1", "B2", "C1", "C2"] {
                                        ui.selectable_value(&mut self.exercise_level, opt.to_string(), opt);
                                    }
                                });
                        }
                        ui.small(tr(lang,
                            "Answers update SRS/review progress (compatible with profile data).",
                            "Le risposte aggiornano SRS/progressi di ripasso (compatibile con i dati profilo)."));
                    });

                    ui.add_space(16.0);
                    ui.label(egui::RichText::new(tr(lang, "Or choose yourself", "Oppure scegli tu")).size(22.0).strong());
                    ui.add_space(10.0);

                    let mut clicked_mode: Option<ExerciseMode> = None;
                    let mut render_cards = |ui: &mut egui::Ui, grid_id: &str, cards: &[(ExerciseMode, &str, &str, String)]| {
                        egui::Grid::new(grid_id).num_columns(2).spacing([12.0, 12.0]).show(ui, |ui| {
                            for (i, (m, title, desc, avail)) in cards.iter().enumerate() {
                                let selected = self.exercise_mode == *m;
                                let fill = if selected { ui.visuals().selection.bg_fill } else { ui.visuals().faint_bg_color };
                                let stroke = if selected { egui::Stroke::new(1.5_f32, ui.visuals().selection.stroke.color) } else { egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color) };
                                let resp = egui::Frame::none().fill(fill).stroke(stroke).rounding(egui::CornerRadius::same(18)).inner_margin(egui::Margin::same(16)).show(ui, |ui| {
                                    ui.set_min_width(260.0);
                                    ui.set_min_height(112.0);
                                    ui.vertical(|ui| {
                                        ui.label(egui::RichText::new(*title).strong().size(18.0));
                                        ui.label(egui::RichText::new(*desc).color(ui.visuals().strong_text_color()));
                                        ui.add_space(8.0);
                                        ui.small(egui::RichText::new(avail.as_str()).color(ui.visuals().strong_text_color()));
                                    });
                                }).response.interact(egui::Sense::click());
                                if resp.clicked() { clicked_mode = Some(*m); }
                                if i % 2 == 1 { ui.end_row(); }
                            }
                        });
                    };

                    ui.label(egui::RichText::new(tr(lang, "Review & guided", "Ripasso e sessioni guidate")).size(16.0).strong());
                    ui.add_space(6.0);
                    let review_cards = vec![
                        (ExerciseMode::GuidedSession, tr(lang, "Guided session", "Sessione guidata"), tr(lang, "Due reviews + new words", "Ripassi in scadenza + parole nuove"),
                            if lang == "it" { format!("{} dovute · {} nuove", plan.due_words, plan.new_words.min(20)) } else { format!("{} due · {} new", plan.due_words, plan.new_words.min(20)) }),
                        (ExerciseMode::WeakWords, tr(lang, "Weak words", "Parole fragili"), tr(lang, "Focus on difficult words", "Concentrati sulle parole difficili"),
                            if lang == "it" { format!("Parole fragili: {weak_count}") } else { format!("Weak words: {weak_count}") }),
                    ];
                    render_cards(ui, "exercise_cards_review_new", &review_cards);

                    ui.add_space(12.0);
                    ui.label(egui::RichText::new(tr(lang, "Quiz & comprehension", "Quiz e comprensione")).size(16.0).strong());
                    ui.add_space(6.0);
                    let quiz_cards = vec![
                        (ExerciseMode::ArticleQuiz, tr(lang, "Articles", "Articoli"), tr(lang, "der/die/das for your words", "der/die/das sulle tue parole"),
                            if lang == "it" { format!("Con articolo (≤ {}): {article_words_for_level}", self.exercise_level) } else { format!("With article (≤ {}): {article_words_for_level}", self.exercise_level) }),
                        (ExerciseMode::MeaningQuiz, tr(lang, "Meaning", "Significato"), tr(lang, "Pick the right meaning", "Scegli il significato giusto"),
                            if lang == "it" { format!("Parole attive: {active_words}") } else { format!("Active words: {active_words}") }),
                        (ExerciseMode::GermanQuiz, tr(lang, "German", "Tedesco"), tr(lang, "Pick the right German word", "Scegli la parola tedesca giusta"),
                            if lang == "it" { format!("Parole attive: {active_words}") } else { format!("Active words: {active_words}") }),
                        (ExerciseMode::ClozeQuiz, tr(lang, "Cloze", "Cloze"), tr(lang, "Complete from examples", "Completa dagli esempi"),
                            if lang == "it" { format!("Parole attive: {active_words}") } else { format!("Active words: {active_words}") }),
                        (ExerciseMode::Mixed, tr(lang, "Mixed", "Mista"), tr(lang, "A mix of question types", "Un mix di tipi di domanda"),
                            if lang == "it" { format!("Parole attive: {active_words}") } else { format!("Active words: {active_words}") }),
                    ];
                    render_cards(ui, "exercise_cards_quiz_new", &quiz_cards);

                    ui.add_space(12.0);
                    ui.label(egui::RichText::new(tr(lang, "Writing / production", "Scrittura / produzione")).size(16.0).strong());
                    ui.add_space(6.0);
                    let writing_cards = vec![
                        (ExerciseMode::TypingQuiz, tr(lang, "Typing", "Scrittura"), tr(lang, "Write the answer", "Scrivi la risposta"),
                            if lang == "it" { format!("Parole attive: {active_words}") } else { format!("Active words: {active_words}") }),
                    ];
                    render_cards(ui, "exercise_cards_write_new", &writing_cards);

                    if let Some(m) = clicked_mode {
                        self.exercise_mode = m;
                        self.start_exercise_session();
                    }

                    ui.add_space(16.0);
                    ui.label(egui::RichText::new(tr(lang, "Other (from mobile app)", "Altre attività (app mobile)")).size(16.0).strong());
                    ui.small(egui::RichText::new(tr(lang, "Not available in desktop yet.", "Non ancora disponibili su desktop.")).color(ui.visuals().strong_text_color()));
                    ui.add_space(8.0);
                    let more_modes = [
                        (tr(lang, "Listening", "Ascolto"), tr(lang, "Audio comprehension exercises", "Esercizi di comprensione orale")),
                        (tr(lang, "Reading", "Lettura"), tr(lang, "Text comprehension", "Comprensione del testo")),
                        (tr(lang, "Dialogues", "Dialoghi"), tr(lang, "Two-voice conversations", "Conversazioni a due voci")),
                        (tr(lang, "Pronunciation", "Pronuncia"), tr(lang, "Speaking and minimal pairs", "Parlato e coppie minime")),
                        (tr(lang, "Active production", "Produzione attiva"), tr(lang, "Write, complete, reorder", "Scrivi, completa, riordina")),
                        (tr(lang, "Error notebook", "Quaderno errori"), tr(lang, "Review your mistakes", "Ripassa i tuoi errori")),
                    ];
                    egui::Grid::new("exercise_mode_grid_more_mobile").num_columns(2).spacing([12.0, 12.0]).show(ui, |ui| {
                        for (i, (title, desc)) in more_modes.iter().enumerate() {
                            egui::Frame::none()
                                .fill(ui.visuals().faint_bg_color.gamma_multiply(0.9))
                                .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                                .rounding(egui::CornerRadius::same(18))
                                .inner_margin(egui::Margin::same(16))
                                .show(ui, |ui| {
                                    ui.set_min_width(260.0);
                                    ui.set_min_height(96.0);
                                    ui.vertical(|ui| {
                                        ui.label(egui::RichText::new(*title).strong().size(16.0));
                                        ui.label(egui::RichText::new(*desc).color(ui.visuals().strong_text_color()));
                                        ui.add_space(6.0);
                                        ui.small(egui::RichText::new(tr(lang, "Not available yet", "Non disponibile")).color(ui.visuals().strong_text_color()));
                                    });
                                });
                            if i % 2 == 1 { ui.end_row(); }
                        }
                    });
                    ui.add_space(20.0);
                });
            });
        });
    }
    fn screen_vocabulary(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        if self.store.active_profile().is_none() {
            ui.label(tr(lang, "No active profile.", "Nessun profilo attivo."));
            return;
        }

        let (_total_w, _in_rev_count, _due_w_count, _new_w_count, level_names) = self.store.active_profile().map(|p| {
            let now = chrono::Utc::now().timestamp_millis();
            let total_w = p.stato.parole.iter().filter(|w| !w.archived).count();
            let in_rev = p.stato.parole.iter().filter(|w| !w.archived && p.stato.reviews.contains_key(&w.id)).count();
            let due_w = p.stato.parole.iter().filter(|w| !w.archived && p.stato.reviews.get(&w.id).map(|r| r.due_at <= now).unwrap_or(false)).count();
            let new_w = total_w.saturating_sub(in_rev);
            let mut levels: Vec<String> = Vec::new();
            for w in &p.stato.parole {
                if w.archived { continue; }
                let lv = w.level.trim();
                if lv.is_empty() { continue; }
                if !levels.iter().any(|x| x == lv) { levels.push(lv.to_string()); }
            }
            levels.sort();
            (total_w, in_rev, due_w, new_w, levels)
        }).unwrap_or((0, 0, 0, 0, Vec::new()));

        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    ui.horizontal(|ui| {
                        if ui.add(egui::Button::new("<").min_size(egui::vec2(36.0,36.0)).rounding(egui::CornerRadius::from(14))).clicked() {
                            self.screen = Screen::Home;
                        }
                        ui.label(egui::RichText::new(tr(lang, "Lexicon A1-B2", "Lessico A1-B2")).size(34.0).strong());
                    });
                    ui.add_space(16.0);

                    // --- Header: Lexicon Description ---
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0, ui.visuals().widgets.noninteractive.bg_stroke.color))
                        .rounding(egui::CornerRadius::from(22))
                        .inner_margin(egui::Margin::same(20))
                        .show(ui, |ui| {
                            ui.label(tr(lang, "3108 curated words, divided by level. Targets are cumulative (A1 650, A2 1,300, B1 2,400, B2 3,000): add a level to review and train in six different modes.", "3108 parole curate, divise per livello. I traguardi sono cumulativi (A1 650, A2 1.300, B1 2.400, B2 3.000): aggiungi un livello al ripasso e allenati in sei modi diversi."));
                            ui.add_space(12.0);
                            // Level chips (A1, A2, B1, B2)
                            ui.horizontal_wrapped(|ui| {
                                for lv in ["A1", "A2", "B1", "B2"] {
                                    let sel = self.vocab_level == lv;
                                    let b = egui::Button::new(if sel { egui::RichText::new(format!("{} · 0/670", lv)).strong() } else { egui::RichText::new(format!("{} · 0/670", lv)) }).rounding(egui::CornerRadius::from(18));
                                    let b = if sel { b.fill(ui.visuals().faint_bg_color) } else { b.fill(ui.visuals().extreme_bg_color) };
                                    if ui.add(b).clicked() { self.vocab_level = lv.to_string(); }
                                }
                            });
                        });
                    ui.add_space(16.0);

                    // --- Active Level Card ---
                    let active_lv = self.vocab_level.clone();
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0, ui.visuals().selection.stroke.color))
                        .rounding(egui::CornerRadius::from(22))
                        .inner_margin(egui::Margin::same(20))
                        .show(ui, |ui| {
                            ui.label(egui::RichText::new(format!("Livello {}", active_lv)).size(22.0).strong());
                            ui.add_space(6.0);
                            ui.label(tr(lang, "670 new words of A1 · 0 already in your lexicon · 33 to review", "670 parole nuove di A1 · 0 già nel tuo lessico · 33 da ripassare"));
                            ui.add_space(6.0);
                            ui.label(tr(lang, "Coverage up to A1: 670 of 650 words (cumulative target)", "Copertura fino a A1: 670 di 650 parole (obiettivo cumulativo)"));
                            ui.add_space(6.0);
                            ui.label(tr(lang, "Themes: Adjectives · Animals · House · Food · Cities · Colors · Family · Days", "Temi: Aggettivi · Animali · Casa · Cibo · Città · Colori · Famiglia · Giorni"));
                            ui.add_space(12.0);
                            ui.horizontal_wrapped(|ui| {
                                if ui.add(egui::Button::new(tr(lang, "Add to review", "Aggiungi al ripasso")).rounding(egui::CornerRadius::from(14))).clicked() {
                                    match self.store.add_words_to_review(Some(&active_lv)) {
                                        Ok(added) => {
                                            self.status = if lang == "it" {
                                                if added == 0 { "Nessuna nuova parola da aggiungere al ripasso.".into() } else { format!("Aggiunte {added} parole al ripasso.") }
                                            } else if added == 0 { "No new words to add to review.".into() } else { format!("Added {added} words to review.") };
                                            self.refresh_review_queue();
                                        }
                                        Err(e) => self.status = if lang == "it" { format!("Errore: {e}") } else { format!("Error: {e}") },
                                    }
                                }
                                if ui.add(egui::Button::new(tr(lang, "Exercises", "Esercizi")).rounding(egui::CornerRadius::from(14))).clicked() {
                                    if active_lv.len() <= 3 { self.exercise_level = active_lv.clone(); }
                                    self.exercise_mode = ExerciseMode::Mixed;
                                    self.screen = Screen::Exercises;
                                    self.start_exercise_session();
                                }
                            });
                        });
                    ui.add_space(16.0);

                    // --- Search ---
                    ui.add(egui::TextEdit::singleline(&mut self.vocab_query).desired_width(f32::INFINITY).hint_text(tr(lang, "Search in German or Italian", "Cerca in tedesco o in italiano")));
                    ui.add_space(12.0);
                });

                ui.add_space(12.0);
                ui_card(ui, ui.visuals().faint_bg_color, None, 22, 14, |ui| {
                    ui.collapsing(egui::RichText::new(tr(lang, "Add word", "Aggiungi parola")).size(16.0).strong(), |ui| {
                        ui.add_space(6.0);
                        ui.horizontal_wrapped(|ui| {
                            ui.label(tr(lang, "Article", "Articolo"));
                            ui.add(egui::TextEdit::singleline(&mut self.add_article).desired_width(54.0));
                            ui.label(tr(lang, "German*", "Tedesco*"));
                            ui.add(egui::TextEdit::singleline(&mut self.add_german).desired_width(160.0));
                            ui.label(tr(lang, "Meaning*", "Significato*"));
                            ui.add(egui::TextEdit::singleline(&mut self.add_italian).desired_width(160.0));
                            ui.label(tr(lang, "Level", "Livello"));
                            ui.add(egui::TextEdit::singleline(&mut self.add_level).desired_width(54.0));
                        });
                        ui.add_space(6.0);
                        ui.horizontal_wrapped(|ui| {
                            ui.label(tr(lang, "Example", "Esempio"));
                            ui.add(egui::TextEdit::singleline(&mut self.add_example).desired_width(360.0));
                            if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Add", "Aggiungi")).strong()).rounding(egui::CornerRadius::same(14))).clicked() {
                                let article = if self.add_article.trim().is_empty() { None } else { Some(self.add_article.clone()) };
                                match self.store.add_word(&self.add_german, &self.add_italian, article, &self.add_level, &self.add_example) {
                                    Ok(_) => {
                                        self.status = tr(lang, "Word added.", "Parola aggiunta.").into();
                                        self.add_german.clear();
                                        self.add_italian.clear();
                                        self.add_article.clear();
                                        self.add_example.clear();
                                    }
                                    Err(e) => self.status = if lang == "it" { format!("Aggiunta fallita: {e}") } else { format!("Add word failed: {e}") },
                                }
                            }
                        });
                    });
                });

                ui.add_space(12.0);
                // Level filter chips (mobile-like)
                ui_card(ui, ui.visuals().extreme_bg_color.gamma_multiply(0.98), Some(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)), 22, 16, |ui| {
                    ui.label(egui::RichText::new(tr(lang, "Levels", "Livelli")).size(18.0).strong());
                    ui.small(tr(lang, "Select a level to filter words and actions.", "Seleziona un livello per filtrare parole e azioni."));
                    ui.add_space(8.0);
                    egui::ScrollArea::horizontal().id_salt("vocab_level_chips").show(ui, |ui| {
                        ui.horizontal_wrapped(|ui| {
                            let all_sel = self.vocab_level == "ALL";
                            let all_btn = egui::Button::new(if all_sel { egui::RichText::new(tr(lang, "All", "Tutte")).strong() } else { egui::RichText::new(tr(lang, "All", "Tutte")) }).rounding(egui::CornerRadius::same(18));
                            let all_btn = if all_sel { all_btn.fill(ui.visuals().faint_bg_color) } else { all_btn.fill(ui.visuals().faint_bg_color) };
                            if ui.add(all_btn).clicked() { self.vocab_level = "ALL".into(); }
                            for lv in &level_names {
                                let sel = &self.vocab_level == lv;
                                let b = egui::Button::new(if sel { egui::RichText::new(lv).strong() } else { egui::RichText::new(lv) }).rounding(egui::CornerRadius::same(18));
                                let b = if sel { b.fill(ui.visuals().faint_bg_color) } else { b.fill(ui.visuals().faint_bg_color) };
                                if ui.add(b).clicked() { self.vocab_level = lv.clone(); }
                            }
                        });
                    });
                    ui.add_space(10.0);
                    let selected_lv = self.vocab_level.clone();
                    let level_arg = if selected_lv == "ALL" || selected_lv == "—" { None } else { Some(selected_lv.as_str()) };
                    ui.horizontal_wrapped(|ui| {
                        if ui.add(egui::Button::new(tr(lang, "Add to review", "Aggiungi al ripasso")).rounding(egui::CornerRadius::same(14))).clicked() {
                            match self.store.add_words_to_review(level_arg) {
                                Ok(added) => {
                                    self.status = if lang == "it" {
                                        if added == 0 { "Nessuna nuova parola da aggiungere al ripasso.".into() } else { format!("Aggiunte {added} parole al ripasso.") }
                                    } else if added == 0 { "No new words to add to review.".into() } else { format!("Added {added} words to review.") };
                                    self.refresh_review_queue();
                                }
                                Err(e) => self.status = if lang == "it" { format!("Errore: {e}") } else { format!("Error: {e}") },
                            }
                        }
                        if ui.add(egui::Button::new(tr(lang, "Start review", "Inizia ripasso")).rounding(egui::CornerRadius::same(14))).clicked() {
                            self.refresh_review_queue();
                            self.screen = Screen::Review;
                        }
                        if ui.add(egui::Button::new(tr(lang, "Study new words", "Studia nuove parole")).rounding(egui::CornerRadius::same(14))).clicked() {
                            if selected_lv == "ALL" || selected_lv == "—" { self.start_new_words_session(); } else { self.start_new_words_session_for_level(&selected_lv); }
                        }
                        if ui.add(egui::Button::new(tr(lang, "Practice level", "Esercizi livello")).rounding(egui::CornerRadius::same(14))).clicked() {
                            if selected_lv != "ALL" && selected_lv != "—" && selected_lv.len() <= 3 { self.exercise_level = selected_lv.clone(); }
                            self.exercise_mode = ExerciseMode::Mixed;
                            self.screen = Screen::Exercises;
                            self.start_exercise_session();
                        }
                    });
                });

                ui.add_space(12.0);
                // Search + filters
                let base_words = self.store.search_words(&self.vocab_query, Some(&self.vocab_level), self.show_archived_vocab, 500);
                let (total, reviewed_ids, due_ids) = self.store.active_profile().map(|p| {
                    let now = chrono::Utc::now().timestamp_millis();
                    let mut reviewed = std::collections::HashSet::new();
                    let mut due = std::collections::HashSet::new();
                    for (id, r) in &p.stato.reviews {
                        reviewed.insert(id.clone());
                        if r.due_at <= now { due.insert(id.clone()); }
                    }
                    (p.stato.parole.len(), reviewed, due)
                }).unwrap_or((0, std::collections::HashSet::new(), std::collections::HashSet::new()));
                let count_all = base_words.len();
                let count_new = base_words.iter().filter(|w| !reviewed_ids.contains(&w.id)).count();
                let count_in_review = base_words.iter().filter(|w| reviewed_ids.contains(&w.id)).count();
                let count_due = base_words.iter().filter(|w| due_ids.contains(&w.id)).count();

                ui_card(ui, ui.visuals().faint_bg_color, None, 22, 16, |ui| {
                    ui.add(egui::TextEdit::singleline(&mut self.vocab_query).desired_width(f32::INFINITY).hint_text(tr(lang, "Search in German or Italian", "Cerca in tedesco o in italiano")));
                    ui.add_space(8.0);
                    ui.horizontal_wrapped(|ui| {
                        egui::ComboBox::from_id_salt("vocab_status_filter")
                            .selected_text(match self.vocab_status_filter.as_str() {
                                "NEW" => if lang == "it" { format!("Nuove ({count_new})") } else { format!("New ({count_new})") },
                                "IN_REVIEW" => if lang == "it" { format!("Nel ripasso ({count_in_review})") } else { format!("In review ({count_in_review})") },
                                "DUE" => if lang == "it" { format!("Da ripassare ({count_due})") } else { format!("Due ({count_due})") },
                                _ => if lang == "it" { format!("Tutte ({count_all})") } else { format!("All ({count_all})") },
                            })
                            .show_ui(ui, |ui| {
                                ui.selectable_value(&mut self.vocab_status_filter, "ALL".into(), if lang == "it" { format!("Tutte ({count_all})") } else { format!("All ({count_all})") });
                                ui.selectable_value(&mut self.vocab_status_filter, "NEW".into(), if lang == "it" { format!("Nuove ({count_new})") } else { format!("New ({count_new})") });
                                ui.selectable_value(&mut self.vocab_status_filter, "IN_REVIEW".into(), if lang == "it" { format!("Nel ripasso ({count_in_review})") } else { format!("In review ({count_in_review})") });
                                ui.selectable_value(&mut self.vocab_status_filter, "DUE".into(), if lang == "it" { format!("Da ripassare ({count_due})") } else { format!("Due ({count_due})") });
                            });
                        ui.checkbox(&mut self.show_archived_vocab, tr(lang, "Show archived", "Mostra archiviate"));
                    });
                });

                let mut words = base_words;
                match self.vocab_status_filter.as_str() {
                    "NEW" => words.retain(|w| !reviewed_ids.contains(&w.id)),
                    "IN_REVIEW" => words.retain(|w| reviewed_ids.contains(&w.id)),
                    "DUE" => words.retain(|w| due_ids.contains(&w.id)),
                    _ => {}
                }

                ui.add_space(10.0);
                ui.horizontal_wrapped(|ui| {
                    ui.label(egui::RichText::new(if lang == "it" { format!("{} parole", words.len()) } else { format!("{} words", words.len()) }).size(16.0).strong());
                    if !words.is_empty() {
                        if ui.add(egui::Button::new(tr(lang, "Mark shown as studied", "Segna come studiate")).rounding(egui::CornerRadius::same(14))).clicked() {
                            let ids: Vec<String> = words.iter().map(|w| w.id.clone()).collect();
                            match self.store.add_word_ids_to_review(&ids) {
                                Ok(added) => {
                                    self.status = if lang == "it" {
                                        if added == 0 { "Le parole mostrate sono già nel ripasso.".into() } else { format!("Aggiunte {added} parole al ripasso.") }
                                    } else if added == 0 { "Shown words are already in review.".into() } else { format!("Added {added} words to review.") };
                                    self.refresh_review_queue();
                                }
                                Err(e) => self.status = if lang == "it" { format!("Errore: {e}") } else { format!("Error: {e}") },
                            }
                        }
                    }
                });
                if !self.status.is_empty() { ui.small(&self.status); }
                ui.add_space(8.0);

                let mut archive_toggle: Option<(String, bool)> = None;
                let mut add_review_id: Option<String> = None;
                for w in &words {
                    let in_review = reviewed_ids.contains(&w.id);
                    let is_due = due_ids.contains(&w.id);
                    ui_card(ui, ui.visuals().faint_bg_color, Some(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)), 20, 14, |ui| {
                        ui.horizontal(|ui| {
                            ui.vertical(|ui| {
                                ui.label(egui::RichText::new(w.label()).size(18.0).strong());
                                ui.label(egui::RichText::new(&w.italian).color(ui.visuals().strong_text_color()));
                            });
                            ui.with_layout(egui::Layout::right_to_left(egui::Align::Min), |ui| {
                                if !w.level.is_empty() {
                                    ui_card(ui, ui.visuals().selection.bg_fill, None, 12, 6, |ui| {
                                        ui.label(egui::RichText::new(&w.level).small().strong());
                                    });
                                }
                            });
                        });
                        if !w.example.trim().is_empty() {
                            ui.add_space(6.0);
                            ui.label(egui::RichText::new(&w.example).italics().color(ui.visuals().strong_text_color()));
                        }
                        ui.add_space(6.0);
                        ui.horizontal_wrapped(|ui| {
                            if in_review {
                                let lbl = if is_due { tr(lang, "Due", "Scaduta") } else { tr(lang, "In review", "Nel ripasso") };
                                ui.colored_label(egui::Color32::from_rgb(80, 160, 120), egui::RichText::new(lbl).strong());
                            } else if !w.archived {
                                ui.small(tr(lang, "Not in review", "Non nel ripasso"));
                            }
                            if w.archived { ui.colored_label(egui::Color32::GRAY, tr(lang, "Archived", "Archiviata")); }
                            let primary_tag = w.tags.split(',').map(|s| s.trim()).find(|s| !s.is_empty() && *s != "custom");
                            if let Some(tag) = primary_tag { ui.small(format!("• {tag}")); }
                        });
                        ui.add_space(6.0);
                        ui.horizontal_wrapped(|ui| {
                            if !w.archived && !in_review && ui.add(egui::Button::new(tr(lang, "Add to review", "Aggiungi al ripasso")).small().rounding(egui::CornerRadius::same(12))).clicked() {
                                add_review_id = Some(w.id.clone());
                            }
                            let label = if w.archived { tr(lang, "Restore", "Ripristina") } else { tr(lang, "Archive", "Archivia") };
                            if ui.add(egui::Button::new(label).small().rounding(egui::CornerRadius::same(12))).clicked() {
                                archive_toggle = Some((w.id.clone(), !w.archived));
                            }
                        });
                    });
                    ui.add_space(8.0);
                }
                if words.is_empty() {
                    ui_card(ui, ui.visuals().faint_bg_color, None, 20, 18, |ui| {
                        ui.vertical_centered(|ui| {
                            ui.label(egui::RichText::new(tr(lang, "No words", "Nessuna parola")).size(18.0).strong());
                            ui.small(tr(lang, "No words match the current filters.", "Nessuna parola corrisponde ai filtri."));
                        });
                    });
                }
                let _ = total;
                ui.add_space(16.0);

                if let Some((id, archived)) = archive_toggle {
                    match self.store.set_word_archived(&id, archived) {
                        Ok(_) => self.status = if archived { tr(lang, "Word archived.", "Parola archiviata.").into() } else { tr(lang, "Word restored.", "Parola ripristinata.").into() },
                        Err(e) => self.status = if lang == "it" { format!("Aggiornamento fallito: {e}") } else { format!("Update failed: {e}") },
                    }
                }
                if let Some(id) = add_review_id {
                    match self.store.add_word_ids_to_review(&[id]) {
                        Ok(added) => {
                            self.status = if lang == "it" {
                                if added == 0 { "La parola è già nel ripasso.".into() } else { "Parola aggiunta al ripasso.".into() }
                            } else if added == 0 { "Word is already in review.".into() } else { "Word added to review.".into() };
                            self.refresh_review_queue();
                        }
                        Err(e) => self.status = if lang == "it" { format!("Errore: {e}") } else { format!("Error: {e}") },
                    }
                }
            });
        });
    }

    fn screen_grammar(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        if self.store.active_profile().is_none() {
            ui.label(tr(lang, "No active profile.", "Nessun profilo attivo."));
            return;
        }
        let plan = self.store.study_plan_summary();
        let (_cur_topic_it, _cur_topic_en) = lesson_topic(&plan.livello_corrente, plan.lezione_corrente.max(1));
        // let cur_topic = if lang == "it" { cur_topic_it } else { cur_topic_en };

                egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            page(ui, 980.0, |ui| {
                ui.add_space(8.0);
                    ui.label(egui::RichText::new(tr(lang, "Grammar path", "Percorso grammaticale")).size(34.0).strong());
                ui.label(egui::RichText::new(tr(lang, "Theory, syllabus and guided practice.", "Teoria, programma e pratica guidata.")).size(16.0).color(ui.visuals().strong_text_color()));
                ui.add_space(16.0);

                    // --- Header: Level/Goal Progress ---
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0, ui.visuals().selection.stroke.color))
                        .rounding(egui::CornerRadius::same(22))
                        .inner_margin(egui::Margin::same(20))
                        .show(ui, |ui| {
                            ui.label(egui::RichText::new(format!("{} -> {}", plan.livello_corrente, plan.obiettivo_livello)).size(24.0).strong());
                            ui.add_space(6.0);
                            ui.label(tr(lang, "20 minutes a day. Grammar leads the way. Each completed unit is a step towards mastery.", "20 minuti al giorno. La grammatica comanda il percorso. Ogni unità completata è un passo verso la padronanza."));
                            ui.add_space(12.0);
                            let grammar_summary = self.store.grammar_mastery_summaries();
                            let total = crate::data::embedded_grammar_units().len();
                            let completed = grammar_summary.iter().filter(|s| s.completata).count();
                            ui.label(format!("{} {} 56 {}", completed, tr(lang, "of", "di"), tr(lang, "units completed", "unità completate")));
                            ui.add_space(8.0);
                            ui.add(egui::ProgressBar::new(completed as f32 / total as f32).desired_height(8.0).rounding(egui::CornerRadius::same(8)));
                        });

                    ui.add_space(16.0);
                    // --- Next Unit ---
                    if let Some(next_unit) = self.store.prossima_unita() {
                        ui.label(egui::RichText::new(tr(lang, "NEXT UNIT", "PROSSIMA UNITÀ")).size(14.0).strong());
                        ui.add_space(8.0);
                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(22))
                            .inner_margin(egui::Margin::same(20))
                            .show(ui, |ui| {
                                ui.label(egui::RichText::new(if lang == "it" { next_unit.title_it } else { next_unit.title_en }).size(24.0).strong());
                                ui.label(format!("{} · 14 min · {}", next_unit.level, if lang == "it" { next_unit.subtitle_it } else { next_unit.subtitle_en }));
                                ui.add_space(12.0);
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Start →", "Inizia →")).strong()).min_size(egui::vec2(160.0, 44.0)).rounding(egui::CornerRadius::same(22))).clicked() {
                                    self.grammar_unit_id = Some(next_unit.id.to_string());
                                }
                            });
                        ui.add_space(16.0);
                    }

                ui.add_space(16.0);
                ui.label(egui::RichText::new(tr(lang, "Grammar units", "Unità grammaticali")).size(24.0).strong());
                ui.label(egui::RichText::new(tr(lang,
                    "Embedded units with theory and interactive exercises. Practice saves mastery to profile data.",
                    "Unità integrate con teoria ed esercizi interattivi. La pratica salva la padronanza nei dati profilo.")).color(ui.visuals().strong_text_color()));
                ui.add_space(10.0);

                // Unit detail / practice (mobile-like lesson flow)
                if let Some(unit_id) = self.grammar_unit_id.clone() {
                    let unit_opt = crate::data::embedded_grammar_unit_by_id(&unit_id);
                    if let Some(unit) = unit_opt {
                        let title = if lang == "it" { unit.title_it } else { unit.title_en };
                        let subtitle = if lang == "it" { unit.subtitle_it } else { unit.subtitle_en };
                        let objective = if lang == "it" { unit.objective_it } else { unit.objective_en };
                        let theory = if lang == "it" { unit.theory_it } else { unit.theory_en };

                        ui.horizontal(|ui| {
                            if ui.add(egui::Button::new(if lang == "it" { "? Torna alle unit?" } else { "? Back to units" }).rounding(egui::CornerRadius::same(14))).clicked() {
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
                        ui.add_space(10.0);

                        // Unit hero
                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color))
                            .rounding(egui::CornerRadius::same(24))
                            .inner_margin(egui::Margin::same(20))
                            .show(ui, |ui| {
                                ui.label(egui::RichText::new(format!("{}", unit.level)).size(13.0).strong().color(ui.visuals().strong_text_color()));
                                ui.label(egui::RichText::new(title).size(28.0).strong());
                                ui.label(egui::RichText::new(subtitle).color(ui.visuals().strong_text_color()));
                                ui.add_space(8.0);
                                ui.label(egui::RichText::new(format!("{}: {}", tr(lang, "Objective", "Obiettivo"), objective)));
                            });
                        ui.add_space(12.0);

                        // Theory card
                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(20))
                            .inner_margin(egui::Margin::same(16))
                            .show(ui, |ui| {
                                ui.label(egui::RichText::new(tr(lang, "Theory", "Teoria")).size(20.0).strong());
                                ui.add_space(6.0);
                                for line in theory {
                                    ui.label(egui::RichText::new(format!("? {}", line)));
                                    ui.add_space(4.0);
                                }
                                if !unit.examples.is_empty() {
                                    ui.add_space(8.0);
                                    ui.separator();
                                    ui.add_space(8.0);
                                    ui.label(egui::RichText::new(tr(lang, "Examples", "Esempi")).strong());
                                    ui.add_space(4.0);
                                    for (de, trn) in unit.examples {
                                        ui.label(egui::RichText::new(*de).strong());
                                        ui.label(egui::RichText::new(*trn).color(ui.visuals().strong_text_color()));
                                        ui.add_space(6.0);
                                    }
                                }
                            });
                        ui.add_space(12.0);

                        // Practice card
                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(20))
                            .inner_margin(egui::Margin::same(16))
                            .show(ui, |ui| {
                                ui.horizontal(|ui| {
                                    ui.label(egui::RichText::new(tr(lang, "Exercises", "Esercizi")).size(20.0).strong());
                                    if !unit.exercises.is_empty() {
                                        ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                            let shown = self.grammar_practice_index.min(unit.exercises.len());
                                            ui.label(egui::RichText::new(format!("{}/{}", shown, unit.exercises.len())).strong());
                                        });
                                    }
                                });
                                if !unit.exercises.is_empty() {
                                    let prog = (self.grammar_practice_index as f32 / unit.exercises.len() as f32).clamp(0.0, 1.0);
                                    ui.add_space(6.0);
                                    ui.add(egui::ProgressBar::new(prog).desired_height(8.0).rounding(egui::CornerRadius::same(8)));
                                }
                                ui.add_space(10.0);

                                if let Some((pct, completed)) = self.grammar_practice_done {
                                    ui.label(egui::RichText::new(tr(lang, "Practice completed", "Pratica completata")).size(20.0).strong());
                                    ui.add_space(4.0);
                                    ui.label(if lang == "it" { format!("Punteggio: {pct}% ({}/{})", self.grammar_practice_correct, unit.exercises.len()) } else { format!("Score: {pct}% ({}/{})", self.grammar_practice_correct, unit.exercises.len()) });
                                    ui.label(if completed { tr(lang, "Mastery: completed (>=80%)", "Padronanza: completata (>=80%)") } else { tr(lang, "Keep practicing to reach 80%", "Continua a esercitarti per raggiungere 80%") });
                                    ui.add_space(10.0);
                                    if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Practice again", "Esercitati di nuovo")).strong()).min_size(egui::vec2(180.0, 42.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                        self.grammar_practice_index = 0;
                                        self.grammar_practice_order = shuffled_indices(unit.exercises.len(), chrono::Utc::now().timestamp_millis() as u64);
                                        self.grammar_practice_correct = 0;
                                        self.grammar_practice_answered = false;
                                        self.grammar_practice_feedback.clear();
                                        self.grammar_practice_input.clear();
                                        self.grammar_practice_done = None;
                                    }
                                } else if self.grammar_practice_index >= unit.exercises.len() && !unit.exercises.is_empty() {
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
                                    ui.label(tr(lang, "No exercises available for this unit.", "Nessun esercizio disponibile per questa unit?."));
                                } else {
                                    if self.grammar_practice_order.len() != unit.exercises.len() {
                                        self.grammar_practice_order = shuffled_indices(unit.exercises.len(), chrono::Utc::now().timestamp_millis() as u64);
                                    }
                                    let ex_idx = *self.grammar_practice_order.get(self.grammar_practice_index).unwrap_or(&0);
                                    let ex = &unit.exercises[ex_idx.min(unit.exercises.len().saturating_sub(1))];
                                    let prompt = if lang == "it" { ex.prompt_it } else { ex.prompt_en };

                                    egui::Frame::none()
                                        .fill(ui.visuals().extreme_bg_color)
                                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                                        .rounding(egui::CornerRadius::same(16))
                                        .inner_margin(egui::Margin::same(14))
                                        .show(ui, |ui| {
                                            ui.label(egui::RichText::new(prompt).size(20.0).strong());
                                            ui.add_space(10.0);
                                            if !self.grammar_practice_answered {
                                                if ex.options.is_empty() {
                                                    ui.add(egui::TextEdit::singleline(&mut self.grammar_practice_input).hint_text(tr(lang, "Type answer...", "Scrivi la risposta...")).desired_width(f32::INFINITY));
                                                    ui.add_space(8.0);
                                                    if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Check", "Controlla")).strong()).min_size(egui::vec2(ui.available_width(), 42.0)).rounding(egui::CornerRadius::same(14))).clicked() {
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
                                                } else {
                                                    let mut picked: Option<usize> = None;
                                                    for (i, opt) in ex.options.iter().enumerate() {
                                                        if ui.add(egui::Button::new(*opt).min_size(egui::vec2(ui.available_width(), 44.0)).rounding(egui::CornerRadius::same(14))).clicked() { picked = Some(i); }
                                                        ui.add_space(6.0);
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
                                                let ok = self.grammar_practice_feedback.contains("Correct") || self.grammar_practice_feedback.contains("Corretto");
                                                let color = if ok { egui::Color32::from_rgb(70, 160, 110) } else { egui::Color32::from_rgb(210, 90, 90) };
                                                ui.colored_label(color, egui::RichText::new(&self.grammar_practice_feedback).strong());
                                                ui.add_space(10.0);
                                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Next", "Avanti")).strong()).min_size(egui::vec2(ui.available_width(), 42.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                                    self.grammar_practice_index += 1;
                                                    self.grammar_practice_answered = false;
                                                    self.grammar_practice_feedback.clear();
                                                    self.grammar_practice_input.clear();
                                                }
                                            }
                                        });
                                }
                            });
                    } else {
                        self.grammar_unit_id = None;
                    }
                    ui.add_space(12.0);
                }

                // Units list (entry point) when no unit selected
                if self.grammar_unit_id.is_none() {
                    let units_all = self.store.embedded_grammar_units();
                    let summaries = self.store.grammar_mastery_summaries();
                    let now_ms = chrono::Utc::now().timestamp_millis();
                    let recommended_id = units_all.iter().find_map(|u| {
                        let s = summaries.iter().find(|x| x.id == u.id);
                        if s.map(|m| m.is_due(now_ms)).unwrap_or(false) { Some(u.id.to_string()) } else { None }
                    }).or_else(|| {
                        units_all.iter().find_map(|u| {
                            let s = summaries.iter().find(|x| x.id == u.id);
                            let done = s.map(|m| m.completata || m.percentuale >= 80).unwrap_or(false);
                            if !done { Some(u.id.to_string()) } else { None }
                        })
                    }).or_else(|| units_all.first().map(|u| u.id.to_string()));

                    if let Some(rid) = &recommended_id {
                        if let Some(u) = units_all.iter().find(|x| &x.id.to_string() == rid) {
                            let title = if lang == "it" { u.title_it } else { u.title_en };
                            egui::Frame::none()
                                .fill(ui.visuals().selection.bg_fill.gamma_multiply(0.85))
                                .stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color))
                                .rounding(egui::CornerRadius::same(18))
                                .inner_margin(egui::Margin::same(14))
                                .show(ui, |ui| {
                                    ui.label(egui::RichText::new(if lang == "it" { "Consigliata" } else { "Recommended" }).small().strong());
                                    ui.label(egui::RichText::new(format!("[{}] {}", u.level, title)).size(18.0).strong());
                                    if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Open unit", "Apri unit?")).strong()).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
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
                                });
                            ui.add_space(10.0);
                        }
                    }

                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                        .rounding(egui::CornerRadius::same(18))
                        .inner_margin(egui::Margin::same(14))
                        .show(ui, |ui| {
                            ui.horizontal_wrapped(|ui| {
                                ui.label(egui::RichText::new(tr(lang, "Filter level:", "Filtra livello:")).strong());
                                egui::ComboBox::from_id_salt("gunit_level_filter")
                                    .selected_text(self.grammar_unit_level_filter.clone())
                                    .show_ui(ui, |ui| {
                                        for opt in ["ALL", "A0", "A1", "A2", "B1"] {
                                            ui.selectable_value(&mut self.grammar_unit_level_filter, opt.to_string(), opt);
                                        }
                                    });
                                ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                    ui.label(if lang == "it" { format!("Unit?: {}", units_all.len()) } else { format!("Units: {}", units_all.len()) });
                                });
                            });
                        });
                    ui.add_space(10.0);

                    for u in &units_all {
                        if self.grammar_unit_level_filter != "ALL" && u.level != self.grammar_unit_level_filter { continue; }
                        let title = if lang == "it" { u.title_it } else { u.title_en };
                        let subtitle = if lang == "it" { u.subtitle_it } else { u.subtitle_en };
                        let mastery = summaries.iter().find(|s| s.id == u.id);
                        let state = if let Some(m) = mastery {
                            if m.is_due(now_ms) { tr(lang, "Due", "Da ripassare") }
                            else if m.completata || m.percentuale >= 80 { tr(lang, "Mastered", "Padroneggiata") }
                            else if m.tentativi > 0 { tr(lang, "In progress", "In corso") }
                            else { tr(lang, "New", "Nuova") }
                        } else { tr(lang, "New", "Nuova") };
                        egui::Frame::none()
                            .fill(ui.visuals().faint_bg_color)
                            .stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color))
                            .rounding(egui::CornerRadius::same(18))
                            .inner_margin(egui::Margin::same(14))
                            .show(ui, |ui| {
                                ui.horizontal(|ui| {
                                    ui.label(egui::RichText::new(format!("[{}]", u.level)).strong().color(ui.visuals().strong_text_color()));
                                    ui.label(egui::RichText::new(state).small());
                                    if let Some(m) = mastery {
                                        ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                            ui.small(if lang == "it" { format!("{}% ? {} tent.", m.percentuale, m.tentativi) } else { format!("{}% ? {} att.", m.percentuale, m.tentativi) });
                                        });
                                    }
                                });
                                ui.label(egui::RichText::new(title).size(18.0).strong());
                                ui.label(egui::RichText::new(subtitle).small().color(ui.visuals().strong_text_color()));
                                ui.add_space(8.0);
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Open", "Apri")).strong()).min_size(egui::vec2(120.0, 38.0)).rounding(egui::CornerRadius::same(14))).clicked() {
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
                            });
                        ui.add_space(8.0);
                    }
                    ui.add_space(8.0);
                }

                // Mastery summary
                ui.label(egui::RichText::new(tr(lang, "Grammar mastery & unit progression", "Padronanza grammaticale e unit?")).size(20.0).strong());
                let mut summaries = self.store.grammar_mastery_summaries();
                let now = chrono::Utc::now().timestamp_millis();
                ui.label(if lang == "it" { format!("Unit? tracciate: {}", summaries.len()) } else { format!("Tracked units: {}", summaries.len()) });
                ui.add_space(8.0);
                if summaries.is_empty() {
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(16)).inner_margin(egui::Margin::same(14)).show(ui, |ui| {
                        ui.label(tr(lang,
                            "No grammar mastery data yet. Complete grammar/unit practice to populate this view.",
                            "Nessun dato di padronanza grammaticale. Completa pratica grammaticale/unit? per popolare questa vista."));
                    });
                } else {
                    summaries.sort_by(|a, b| {
                        let ad = a.is_due(now);
                        let bd = b.is_due(now);
                        bd.cmp(&ad).then_with(|| a.completata.cmp(&b.completata)).then_with(|| b.percentuale.cmp(&a.percentuale)).then_with(|| a.id.cmp(&b.id))
                    });
                    egui::ScrollArea::vertical().id_salt("grammar_mastery").max_height(260.0).show(ui, |ui| {
                        for s in &summaries {
                            egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(16)).inner_margin(egui::Margin::same(12)).show(ui, |ui| {
                                ui.horizontal(|ui| {
                                    ui.label(egui::RichText::new(&s.id).strong());
                                    let state = if s.is_due(now) { tr(lang, "Due", "Da ripassare") }
                                        else if s.completata || s.percentuale >= 80 { tr(lang, "Mastered", "Padroneggiata") }
                                        else if s.tentativi > 0 { tr(lang, "In progress", "In corso") }
                                        else { tr(lang, "New", "Nuova") };
                                    ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| { ui.small(state); });
                                });
                                ui.label(if lang == "it" {
                                    format!("Punteggio: {}%  ?  Tentativi: {}  ?  Corrette: {}  ?  Ripassi: {}", s.percentuale, s.tentativi, s.corrette, s.ripassi)
                                } else {
                                    format!("Score: {}%  ?  Attempts: {}  ?  Correct: {}  ?  Reviews: {}", s.percentuale, s.tentativi, s.corrette, s.ripassi)
                                });
                            });
                            ui.add_space(6.0);
                        }
                    });
                }

                if !self.status.is_empty() {
                    ui.add_space(8.0);
                    ui.label(&self.status);
                }
                ui.add_space(16.0);
            });
        });
    }

    fn screen_progress(&mut self, ui: &mut egui::Ui) {
        let lang = ui_lang(&self.store).to_string();
        let lang = lang.as_str();
        let Some(_pcheck) = self.store.active_profile() else {
            ui.label(tr(lang, "No active profile.", "Nessun profilo attivo."));
            return;
        };
        let (total, _reviewed, _due, streak) = self.store.stats();
        let plan = self.store.study_plan_summary();
        let _profile_name = self.store.active_profile().map(|p| p.config.nome_visualizzato.clone()).unwrap_or_default();
        let _streak_record = self.store.active_profile().map(|p| p.stato.streak_record).unwrap_or(0);
        let in_review_total = self.store.active_profile().map(|p| p.stato.parole.iter().filter(|w| !w.archived && p.stato.reviews.contains_key(&w.id)).count()).unwrap_or(0);
        let (correct_answers, total_answers, reviews_today, _correct_today) = self.store.active_profile().map(|p| {
            let (correct_answers, total_answers) = p.stato.ripassi_log.iter().fold((0usize, 0usize), |(c, t), e| (c + if e.corretta { 1 } else { 0 }, t + 1));
            let today_start = chrono::Local::now().date_naive().and_hms_opt(0, 0, 0).unwrap().and_local_timezone(chrono::Local).unwrap().timestamp_millis();
            let reviews_today = p.stato.ripassi_log.iter().filter(|e| e.timestamp >= today_start).count();
            let correct_today = p.stato.ripassi_log.iter().filter(|e| e.timestamp >= today_start && e.corretta).count();
            (correct_answers, total_answers, reviews_today, correct_today)
        }).unwrap_or((0, 0, 0, 0));
        let accuracy_pct = if total_answers > 0 { (correct_answers * 100) / total_answers } else { 0 };

        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    ui.horizontal(|ui| {
                        if ui.add(egui::Button::new("<").min_size(egui::vec2(36.0,36.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                            self.screen = Screen::Home;
                        }
                        ui.label(egui::RichText::new(tr(lang, "Progress", "Progresso")).size(34.0).strong());
                    });
                    ui.add_space(16.0);

                    // --- Header: Level status ---
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0, ui.visuals().selection.stroke.color))
                        .rounding(egui::CornerRadius::same(22))
                        .inner_margin(egui::Margin::same(20))
                        .show(ui, |ui| {
                            ui.label(egui::RichText::new(tr(lang, "Your level", "Il tuo livello")).strong());
                            ui.label(egui::RichText::new(format!("{} — {}", plan.livello_corrente, tr(lang, "Beginner", "Principiante"))).size(28.0).strong());
                            ui.add_space(6.0);
                            ui.label(tr(lang, "Every step counts. Keep studying!", "Ogni passo conta. Continua a studiare!"));
                            ui.add_space(12.0);
                            ui.label(format!("{} A1", tr(lang, "Towards", "Verso")));
                            ui.add_space(8.0);
                            ui.add(egui::ProgressBar::new(0.0).desired_height(8.0).rounding(egui::CornerRadius::same(8)));
                            ui.add_space(8.0);
                            ui.small(tr(lang, "Reach 60% in ALL 4 skills (official threshold)", "Serve il 60% in TUTTE le 4 competenze (soglia ufficiale)"));
                        });

                    ui.add_space(16.0);
                    // --- Numbers Grid ---
                    ui.label(egui::RichText::new(tr(lang, "Your numbers", "I tuoi numeri")).size(20.0).strong());
                    ui.add_space(8.0);
                    let kpis = [
                        (format!("{}", total), tr(lang, "Words", "Parole")),
                        (format!("{}", in_review_total), tr(lang, "In review", "Nel ripasso")),
                        (format!("{}", reviews_today), tr(lang, "Reviews", "Ripassi")),
                        (format!("{}%", accuracy_pct), tr(lang, "Accuracy", "Accuratezza")),
                        (format!("{}", streak), tr(lang, "Streak", "Serie")),
                        (format!("{}m", 0), tr(lang, "Studio", "Studio")),
                    ];
                    for chunk in kpis.chunks(2) {
                        ui.columns(2, |cols| {
                            for (i, (val, label)) in chunk.iter().enumerate() {
                                let fill = cols[i].visuals().faint_bg_color;
                                let stroke = egui::Stroke::new(1.0_f32, cols[i].visuals().widgets.noninteractive.bg_stroke.color);
                                ui_card(&mut cols[i], fill, Some(stroke), 20, 12, |ui| {
                                    ui.vertical_centered(|ui| {
                                        ui.label(egui::RichText::new(val).size(24.0).strong());
                                        ui.small(label.to_string());
                                    });
                                });
                            }
                        });
                        ui.add_space(10.0);
                    }

                    ui.add_space(16.0);
                    // --- Recommendations ---
                    ui.label(egui::RichText::new(tr(lang, "What to do next", "Cosa fare adesso")).size(20.0).strong());
                    ui.add_space(8.0);
                    egui::Frame::none()
                        .fill(ui.visuals().faint_bg_color)
                        .stroke(egui::Stroke::new(1.0, ui.visuals().widgets.noninteractive.bg_stroke.color))
                        .rounding(egui::CornerRadius::same(16))
                        .inner_margin(egui::Margin::same(16))
                        .show(ui, |ui| {
                             ui.label(tr(lang, "You're mostly missing: Lexicon (0%)", "Ti manca soprattutto: Lessico (0%)"));
                             if ui.button(tr(lang, "Learn new words", "Impara parole nuove")).clicked() {
                                 self.start_new_words_session();
                             }
                        });
                    ui.add_space(20.0);
                });
            });
        });
    }

    fn screen_settings(&mut self, ui: &mut egui::Ui) {
        let lang_s = ui_lang(&self.store).to_string();
        let lang = lang_s.as_str();
        fn fmt_ts(ms: i64) -> String {
            if ms <= 0 { return "—".into(); }
            chrono::DateTime::<chrono::Utc>::from_timestamp_millis(ms)
                .map(|dt| dt.with_timezone(&chrono::Local).format("%d/%m/%Y %H:%M").to_string())
                .unwrap_or_else(|| "—".into())
        }

        egui::ScrollArea::vertical().auto_shrink([false, false]).show(ui, |ui| {
            let max_w = ui.available_width().min(920.0).max(320.0);
            ui.vertical_centered(|ui| {
                ui.set_max_width(max_w);
                ui.with_layout(egui::Layout::top_down(egui::Align::Min), |ui| {
                    ui.add_space(8.0);
                    ui.label(egui::RichText::new(tr(lang, "Settings", "Impostazioni")).size(34.0).strong());
                    ui.label(egui::RichText::new(tr(lang, "Profile, level, appearance and local data.", "Profilo, livello, aspetto e dati locali.")).size(16.0).color(ui.visuals().strong_text_color()));
                    ui.add_space(16.0);

                    // --- Profilo attivo ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Active profile", "Profilo attivo")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Each profile has separate data and progress.", "Ogni profilo ha dati e progressi separati dagli altri.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(10.0);
                        let active = self.store.repo.profilo_attivo_id.clone().unwrap_or_default();
                        let mut profiles: Vec<_> = self.store.repo.profili.values().cloned().collect();
                        profiles.sort_by(|a, b| a.config.nome_visualizzato.to_lowercase().cmp(&b.config.nome_visualizzato.to_lowercase()));
                        for p in &profiles {
                            let selected = p.id == active;
                            let livello = p.stato.progresso.livello_corrente.clone();
                            egui::Frame::none()
                                .fill(if selected { ui.visuals().selection.bg_fill.gamma_multiply(0.85) } else { ui.visuals().extreme_bg_color })
                                .stroke(egui::Stroke::new(1.0_f32, if selected { ui.visuals().selection.stroke.color } else { ui.visuals().widgets.noninteractive.bg_stroke.color }))
                                .rounding(egui::CornerRadius::same(16))
                                .inner_margin(egui::Margin::symmetric(14, 12))
                                .show(ui, |ui| {
                                    ui.horizontal_wrapped(|ui| {
                                        let title = if selected { format!("● {}", p.config.nome_visualizzato) } else { format!("○ {}", p.config.nome_visualizzato) };
                                        if ui.selectable_label(selected, egui::RichText::new(title).size(16.0).strong()).clicked() {
                                            self.store.set_active_profile(&p.id);
                                            let _ = self.store.save();
                                            self.refresh_review_queue();
                                            self.sync_settings_drafts_from_store();
                                            self.status = format!("{}: {}", tr(lang, "Active profile", "Profilo attivo"), p.config.nome_visualizzato);
                                        }
                                        ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                            if selected { ui.small(tr(lang, "Active", "Attivo")); }
                                        });
                                    });
                                    ui.add_space(4.0);
                                    ui.horizontal_wrapped(|ui| {
                                        ui.label(egui::RichText::new(tr(lang, "Separate progress", "Progressi separati")).small().color(ui.visuals().strong_text_color()));
                                        ui.add_space(8.0);
                                        ui.label(egui::RichText::new(livello).small().strong());
                                    });
                                });
                            ui.add_space(8.0);
                        }

                        ui.add_space(8.0);
                        ui.label(egui::RichText::new(tr(lang, "Rename active profile", "Rinomina profilo attivo")).strong());
                        ui.horizontal_wrapped(|ui| {
                            ui.add(egui::TextEdit::singleline(&mut self.rename_profile_draft).desired_width(300.0));
                            let can_save = self.rename_profile_draft.trim() != self.store.active_profile().map(|p| p.config.nome_visualizzato.as_str()).unwrap_or("") && !self.rename_profile_draft.trim().is_empty();
                            if ui.add_enabled(can_save, egui::Button::new(egui::RichText::new(tr(lang, "Save name", "Salva nome")).strong()).min_size(egui::vec2(130.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                match self.store.rinomina_profilo(&self.rename_profile_draft) {
                                    Ok(_) => { self.status = tr(lang, "Name updated.", "Nome aggiornato.").into(); self.sync_settings_drafts_from_store(); }
                                    Err(e) => self.status = e,
                                }
                            }
                        });

                        ui.add_space(10.0);
                        ui.separator();
                        ui.add_space(8.0);
                        ui.label(egui::RichText::new(tr(lang, "Create new profile", "Crea nuovo profilo")).strong());
                        ui.horizontal_wrapped(|ui| {
                            ui.add(egui::TextEdit::singleline(&mut self.new_profile_name).hint_text(tr(lang, "Profile name", "Nome profilo")).desired_width(260.0));
                            if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Create", "Crea")).strong()).min_size(egui::vec2(110.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                let name = self.new_profile_name.trim().to_string();
                                if !name.is_empty() {
                                    self.store.create_profile(&name);
                                    self.new_profile_name.clear();
                                    self.refresh_review_queue();
                                    self.sync_settings_drafts_from_store();
                                    self.status = tr(lang, "Profile created.", "Profilo creato.").into();
                                }
                            }
                        });
                    });

                    ui.add_space(12.0);
                    // --- Contesto e obiettivi ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Context and goals", "Contesto e obiettivi")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "AI uses this info to choose examples, exercises and vocabulary.", "L'AI usa queste informazioni per scegliere esempi, esercizi e vocabolario.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(8.0);
                        ui.add(egui::TextEdit::multiline(&mut self.contesto_draft).desired_rows(3).desired_width(f32::INFINITY).hint_text(tr(lang, "E.g. electrician: talk with clients and suppliers", "Es. elettricista: parlare con clienti e fornitori")));
                        ui.add_space(6.0);
                        ui.small(egui::RichText::new(tr(lang, "Stays on this computer. Empty = no personalization.", "Resta su questo computer. Vuoto = nessuna personalizzazione.")).color(ui.visuals().strong_text_color()));
                        ui.add_space(8.0);
                        let current_ctx = self.store.active_profile().map(|p| p.config.contesto_utente.clone()).unwrap_or_default();
                        let can_save = self.contesto_draft.trim() != current_ctx.trim();
                        if ui.add_enabled(can_save, egui::Button::new(egui::RichText::new(tr(lang, "Save context", "Salva contesto")).strong()).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                            match self.store.imposta_contesto(&self.contesto_draft) {
                                Ok(_) => self.status = tr(lang, "Context saved.", "Contesto salvato.").into(),
                                Err(e) => self.status = e,
                            }
                        }
                    });

                    ui.add_space(12.0);
                    // --- Livello e obiettivo ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Level and goal", "Livello e obiettivo")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Current level stays real; here you set starting level and target.", "Il livello attuale resta quello reale raggiunto: qui imposti la partenza e il traguardo.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(10.0);
                        let current_iniziale = self.store.active_profile().map(|p| p.config.livello_iniziale.clone()).unwrap_or_else(|| "A1".into());
                        let current_obiettivo = self.store.active_profile().map(|p| p.stato.progresso.obiettivo_livello.clone()).unwrap_or_else(|| "B2".into());
                        let current_level = self.store.active_profile().map(|p| p.stato.progresso.livello_corrente.clone()).unwrap_or_else(|| "A1".into());
                        ui.label(egui::RichText::new(tr(lang, "Starting level", "Livello di partenza")).strong());
                        ui.horizontal_wrapped(|ui| {
                            for lvl in ["A0", "A1", "A2", "B1", "B2"] {
                                let sel = current_iniziale.eq_ignore_ascii_case(lvl);
                                let mut b = egui::Button::new(if sel { egui::RichText::new(lvl).strong() } else { egui::RichText::new(lvl) })
                                    .min_size(egui::vec2(72.0, 36.0)).rounding(egui::CornerRadius::same(16));
                                b = if sel { b.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color)) } else { b.fill(ui.visuals().extreme_bg_color) };
                                if ui.add(b).clicked() {
                                    match self.store.imposta_livello_e_obiettivo(lvl, &current_obiettivo) {
                                        Ok(_) => { self.sync_settings_drafts_from_store(); self.status = format!("{}: {lvl}", tr(lang, "Starting level", "Livello di partenza")); }
                                        Err(e) => self.status = e,
                                    }
                                }
                            }
                        });
                        ui.add_space(10.0);
                        ui.label(egui::RichText::new(tr(lang, "Goal", "Obiettivo")).strong());
                        ui.horizontal_wrapped(|ui| {
                            for lvl in ["A1", "A2", "B1", "B2", "C1", "C2"] {
                                let sel = current_obiettivo.eq_ignore_ascii_case(lvl);
                                let mut b = egui::Button::new(if sel { egui::RichText::new(lvl).strong() } else { egui::RichText::new(lvl) })
                                    .min_size(egui::vec2(72.0, 36.0)).rounding(egui::CornerRadius::same(16));
                                b = if sel { b.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color)) } else { b.fill(ui.visuals().extreme_bg_color) };
                                if ui.add(b).clicked() {
                                    match self.store.imposta_livello_e_obiettivo(&current_iniziale, lvl) {
                                        Ok(_) => { self.status = format!("{}: {lvl}", tr(lang, "Goal", "Obiettivo")); }
                                        Err(e) => self.status = e,
                                    }
                                }
                            }
                        });
                        ui.add_space(10.0);
                        egui::Frame::none().fill(ui.visuals().extreme_bg_color).rounding(egui::CornerRadius::same(14)).inner_margin(egui::Margin::same(12)).show(ui, |ui| {
                            ui.label(egui::RichText::new(format!("{}: {}", tr(lang, "Current level", "Livello attuale"), current_level)).strong());
                            ui.small(egui::RichText::new(tr(lang,
                                "Current level advances with tests and demonstrated competence, not by manual setting.",
                                "Il livello attuale avanza solo con i test e con le competenze dimostrate, non perché lo imposti a mano.")).color(ui.visuals().strong_text_color()));
                        });
                    });

                    ui.add_space(12.0);
                    // --- Lingua / Aspetto ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Appearance", "Aspetto")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Choose light, dark or follow system settings.", "Scegli chiaro, scuro o segui le impostazioni di sistema.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(8.0);
                        ui.label(egui::RichText::new(tr(lang, "Interface language", "Lingua interfaccia")).strong());
                        let mut selected_lang = if self.store.prefs.lingua.trim().eq_ignore_ascii_case("it") { "it".to_string() } else { "en".to_string() };
                        ui.horizontal_wrapped(|ui| {
                            for (id, label) in [("en", "English"), ("it", "Italiano")] {
                                let sel = selected_lang == id;
                                let mut b = egui::Button::new(if sel { egui::RichText::new(label).strong() } else { egui::RichText::new(label) })
                                    .min_size(egui::vec2(110.0, 36.0)).rounding(egui::CornerRadius::same(16));
                                b = if sel { b.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color)) } else { b.fill(ui.visuals().extreme_bg_color) };
                                if ui.add(b).clicked() { selected_lang = id.to_string(); }
                            }
                        });
                        if !self.store.prefs.lingua.trim().eq_ignore_ascii_case(&selected_lang) {
                            self.store.prefs.lingua = selected_lang.clone();
                            let _ = self.store.save();
                            self.status = tr(&selected_lang, "Language saved.", "Lingua salvata.").to_string();
                        }
                        ui.add_space(10.0);
                        let mut mode = self.store.prefs.tema_modalita.trim().to_lowercase();
                        if mode.is_empty() { mode = "system".into(); }
                        ui.label(egui::RichText::new(tr(lang, "Theme mode", "Modalità tema")).strong());
                        ui.horizontal_wrapped(|ui| {
                            for (id, en, it) in [("system", "System", "Sistema"), ("light", "Light", "Chiaro"), ("dark", "Dark", "Scuro")] {
                                let sel = mode == id;
                                let mut b = egui::Button::new(if sel { egui::RichText::new(tr(lang, en, it)).strong() } else { egui::RichText::new(tr(lang, en, it)) })
                                    .min_size(egui::vec2(110.0, 38.0)).rounding(egui::CornerRadius::same(16));
                                b = if sel { b.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color)) } else { b.fill(ui.visuals().extreme_bg_color) };
                                if ui.add(b).clicked() {
                                    self.store.prefs.tema_modalita = id.to_string();
                                    self.store.prefs.tema_scuro = id == "dark";
                                    let _ = self.store.save();
                                }
                            }
                        });
                        ui.add_space(6.0);
                        ui.small(egui::RichText::new(tr(lang, "Preference is saved in preferenze.json.", "La preferenza viene salvata in preferenze.json.")).color(ui.visuals().strong_text_color()));
                    });

                    ui.add_space(12.0);
                    // --- Colore tema ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Color theme", "Colore tema")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Primary app color.", "Colore principale dell'app.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(8.0);
                        let mut colore = self.store.prefs.tema_colore.trim().to_lowercase();
                        if colore.is_empty() { colore = "teal".into(); }
                        ui.horizontal_wrapped(|ui| {
                            for (id, en, it) in [("teal","Teal","Teal"), ("navy","Navy blue","Blu navy"), ("indigo","Indigo","Indaco"), ("forest","Forest","Verde"), ("rose","Rose","Rosa"), ("amber","Amber","Ambra")] {
                                let sel = colore == id;
                                let mut b = egui::Button::new(if sel { egui::RichText::new(tr(lang, en, it)).strong() } else { egui::RichText::new(tr(lang, en, it)) })
                                    .min_size(egui::vec2(100.0, 36.0)).rounding(egui::CornerRadius::same(16));
                                b = if sel { b.fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().selection.stroke.color)) } else { b.fill(ui.visuals().extreme_bg_color) };
                                if ui.add(b).clicked() {
                                    self.store.prefs.tema_colore = id.to_string();
                                    let _ = self.store.save();
                                }
                            }
                        });
                    });

                    ui.add_space(12.0);
                    // --- Lessico ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        let words_count = self.store.active_profile().map(|p| p.stato.parole.len()).unwrap_or(0);
                        ui.label(egui::RichText::new(tr(lang, "Lexicon", "Lessico")).size(22.0).strong());
                        ui.label(egui::RichText::new(format!("{} {}", words_count, tr(lang, "words in active profile", "parole nel profilo attivo"))).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(10.0);
                        ui.horizontal_wrapped(|ui| {
                            ui.vertical(|ui| {
                                ui.label(egui::RichText::new(tr(lang, "Import curated Wortliste A1-B1", "Importa la Wortliste curata A1-B1")).strong());
                                ui.small(egui::RichText::new(tr(lang, "Adds missing words to lexicon, without duplicates.", "Aggiunge le parole mancanti al lessico, senza duplicati.")).color(ui.visuals().strong_text_color()));
                            });
                            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Import", "Importa")).strong()).min_size(egui::vec2(120.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                    match self.store.importa_wortliste_seed() {
                                        Ok(0) => self.status = tr(lang, "No new words: all already present.", "Nessuna parola nuova: erano già tutte presenti.").into(),
                                        Ok(n) => { self.refresh_review_queue(); self.status = if lang == "it" { format!("Aggiunte {n} parole.") } else { format!("Added {n} words.") }; }
                                        Err(e) => self.status = e,
                                    }
                                }
                            });
                        });
                    });

                    ui.add_space(12.0);
                    // --- Sincronizzazione ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        let words_count = self.store.active_profile().map(|p| p.stato.parole.len()).unwrap_or(0);
                        ui.label(egui::RichText::new(tr(lang, "Synchronization", "Sincronizzazione")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Vocabulary feed and GitHub backup settings (mobile-equivalent).", "Feed vocabolario e backup GitHub (equivalente mobile).")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(10.0);

                        ui.label(egui::RichText::new(tr(lang, "Vocabulary feed", "Feed del vocabolario")).strong());
                        ui.small(egui::RichText::new(tr(lang,
                            "Downloads published words from a GitHub JSON feed and merges them. Optional: without URL the bundled lexicon is used.",
                            "Scarica le parole pubblicate su un repository GitHub e le unisce al lessico. Facoltativo: senza URL si usa il lessico incluso.")).color(ui.visuals().strong_text_color()));
                        ui.add_space(6.0);
                        ui.add(egui::TextEdit::singleline(&mut self.feed_draft).desired_width(f32::INFINITY).hint_text(tr(lang, "Feed URL (JSON)", "URL del feed (JSON)")));
                        ui.add_space(8.0);
                        ui.horizontal_wrapped(|ui| {
                            let busy = !self.settings_op_in_progress.is_empty();
                            if ui.add_enabled(!busy, egui::Button::new(egui::RichText::new(tr(lang, "Sync now", "Sincronizza ora")).strong()).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                if let Err(e) = self.store.imposta_feed_url(&self.feed_draft) {
                                    self.status = e;
                                } else {
                                    self.status = tr(lang, "Feed URL saved. Feed sync/download is not yet implemented in Rust desktop build.", "URL feed salvato. La sincronizzazione/download feed non è ancora implementata nella build Rust.").into();
                                }
                            }
                            if ui.add_enabled(!busy, egui::Button::new(tr(lang, "Remove feed", "Rimuovi feed")).min_size(egui::vec2(140.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                self.feed_draft.clear();
                                let _ = self.store.imposta_feed_url("");
                                self.status = tr(lang, "Feed removed: bundled lexicon remains.", "Feed rimosso: resta il lessico incluso nell'app.").into();
                            }
                        });
                        ui.add_space(6.0);
                        ui.small(format!("{}: {} · {} {}", tr(lang, "Last sync", "Ultima sincronizzazione"), fmt_ts(self.store.ultima_sincronizzazione_cloud()), words_count, tr(lang, "words in lexicon", "parole nel lessico")));

                        ui.add_space(12.0);
                        ui.separator();
                        ui.add_space(10.0);
                        ui.label(egui::RichText::new(tr(lang, "GitHub backup", "Backup GitHub")).strong());
                        ui.small(egui::RichText::new(tr(lang,
                            "Configure the same GitHub backup used by the mobile app. In this Rust build, network sync/backup operations are not fully implemented yet; settings are stored locally.",
                            "Configura lo stesso backup GitHub dell'app mobile. In questa build Rust le operazioni di rete non sono ancora implementate; le impostazioni vengono salvate localmente.")).color(ui.visuals().strong_text_color()));
                        ui.add_space(6.0);
                        ui.label(tr(lang, "GitHub token (Contents: Read and write)", "Token GitHub (Contents: Read and write)"));
                        ui.add(egui::TextEdit::singleline(&mut self.token_draft).password(true).desired_width(f32::INFINITY));
                        ui.add_space(6.0);
                        ui.label(tr(lang, "Repository (owner/name)", "Repository (owner/nome)"));
                        ui.add(egui::TextEdit::singleline(&mut self.repo_draft).desired_width(f32::INFINITY).hint_text(tr(lang, "Empty = account backup repo", "Vuoto = repo backup dell'account")));
                        ui.add_space(8.0);
                        ui.horizontal_wrapped(|ui| {
                            let busy = !self.settings_op_in_progress.is_empty();
                            if ui.add_enabled(!busy, egui::Button::new(tr(lang, "Save cloud settings", "Salva impostazioni cloud")).min_size(egui::vec2(180.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                let t1 = self.store.salva_token_cloud(&self.token_draft);
                                let t2 = self.store.salva_repo_cloud(&self.repo_draft);
                                self.status = match (t1, t2) {
                                    (Ok(_), Ok(_)) => tr(lang, "Cloud settings saved locally.", "Impostazioni cloud salvate localmente.").into(),
                                    (Err(e), _) | (_, Err(e)) => e,
                                };
                            }
                            if ui.add_enabled(!busy, egui::Button::new(tr(lang, "Test connection", "Prova connessione")).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                self.status = tr(lang, "Cloud connection test not yet implemented in Rust desktop.", "Verifica connessione cloud non ancora implementata in Rust desktop.").into();
                            }
                            if ui.add_enabled(!busy, egui::Button::new(tr(lang, "Upload backup", "Carica backup")).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                self.status = tr(lang, "Cloud upload not yet implemented in Rust desktop.", "Caricamento cloud non ancora implementato in Rust desktop.").into();
                            }
                            if ui.add_enabled(!busy, egui::Button::new(tr(lang, "Download backup", "Scarica backup")).min_size(egui::vec2(160.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                self.status = tr(lang, "Cloud download/restore not yet implemented in Rust desktop.", "Download/ripristino cloud non ancora implementato in Rust desktop.").into();
                            }
                        });
                        ui.add_space(6.0);
                        ui.small(format!("{}: {}", tr(lang, "Last uploaded backup", "Ultimo backup caricato"), fmt_ts(self.store.ultimo_backup_cloud())));
                        ui.small(format!("{}: {}", tr(lang, "Last automatic sync", "Ultima sincronizzazione automatica"), fmt_ts(self.store.ultima_sincronizzazione_cloud())));
                        ui.small(egui::RichText::new(tr(lang,
                            "Token remains only on this computer (preferenze.json). Use a fine-grained token limited to the backup repo.",
                            "Il token resta solo su questo computer, in preferenze.json. Usa un token fine-grained limitato al repo di backup.")).color(ui.visuals().strong_text_color()));
                    });

                    ui.add_space(12.0);
                    // --- Dati locali ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Local data", "Dati locali")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Words, reviews and progress live in readable JSON files, without account.", "Parole, ripassi e progressi vivono in file JSON leggibili, senza account.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(8.0);
                        egui::Frame::none().fill(ui.visuals().extreme_bg_color).rounding(egui::CornerRadius::same(10)).inner_margin(egui::Margin::same(10)).show(ui, |ui| {
                            ui.monospace(self.store.base_dir.display().to_string());
                        });
                        ui.add_space(8.0);
                        ui.small(format!("{}: {}", tr(lang, "Profiles file", "File profili"), self.store.profili_path.display()));
                        ui.small(format!("{}: {}", tr(lang, "Preferences file", "File preferenze"), self.store.preferenze_path.display()));
                        ui.add_space(10.0);
                        ui.horizontal_wrapped(|ui| {
                            if ui.add(egui::Button::new(tr(lang, "Open folder", "Apri cartella")).min_size(egui::vec2(140.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                let dir = self.store.base_dir.clone();
                                let _ = std::fs::create_dir_all(&dir);
                                #[cfg(target_os = "windows")]
                                let res = std::process::Command::new("explorer").arg(&dir).spawn();
                                #[cfg(target_os = "macos")]
                                let res = std::process::Command::new("open").arg(&dir).spawn();
                                #[cfg(all(not(target_os = "windows"), not(target_os = "macos")))]
                                let res = std::process::Command::new("xdg-open").arg(&dir).spawn();
                                self.status = match res { Ok(_) => tr(lang, "Opened data folder.", "Cartella dati aperta.").into(), Err(e) => format!("{}: {e}", tr(lang, "Cannot open data folder", "Impossibile aprire la cartella dei dati")) };
                            }
                            if ui.add(egui::Button::new(tr(lang, "Export backup", "Esporta backup")).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                let path = std::path::PathBuf::from(self.backup_path.trim());
                                match self.store.export_bundle_to(&path) {
                                    Ok(_) => self.status = format!("{}: {}", tr(lang, "Backup exported to", "Backup esportato in"), path.display()),
                                    Err(e) => self.status = format!("{}: {e}", tr(lang, "Export failed", "Esportazione non riuscita")),
                                }
                            }
                            if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Export all data", "Esporta tutti i dati")).strong()).min_size(egui::vec2(170.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                let default_name = format!("tedesco-desktop-export-{}.json", chrono::Local::now().format("%Y%m%d-%H%M"));
                                let path = self.store.base_dir.join(default_name);
                                match self.store.esporta_dati_completi_a(&path) {
                                    Ok(_) => self.status = format!("{}: {}", tr(lang, "Full export saved to", "Esportazione completa salvata in"), path.display()),
                                    Err(e) => self.status = format!("{}: {e}", tr(lang, "Export failed", "Esportazione non riuscita")),
                                }
                            }
                        });
                        ui.add_space(8.0);
                        ui.label(egui::RichText::new(tr(lang, "Backup path (bundle import/export)", "Percorso backup (import/export bundle)")).strong());
                        ui.add(egui::TextEdit::singleline(&mut self.backup_path).desired_width(f32::INFINITY));
                        ui.add_space(6.0);
                        ui.horizontal_wrapped(|ui| {
                            if ui.add(egui::Button::new(tr(lang, "Import backup", "Importa backup")).min_size(egui::vec2(150.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                let path = std::path::PathBuf::from(self.backup_path.trim());
                                match self.store.import_bundle_from(&path) {
                                    Ok(_) => {
                                        self.refresh_review_queue();
                                        self.sync_settings_drafts_from_store();
                                        self.status = format!("{}: {}", tr(lang, "Imported backup from", "Backup importato da"), path.display());
                                    }
                                    Err(e) => self.status = format!("{}: {e}", tr(lang, "Import failed", "Importazione fallita")),
                                }
                            }
                            if ui.add(egui::Button::new(tr(lang, "Reload from disk", "Ricarica da disco")).min_size(egui::vec2(170.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                self.store = Store::load_default();
                                self.refresh_review_queue();
                                self.sync_settings_drafts_from_store();
                                self.status = if lang == "it" { format!("Ricaricato alle {}", Local::now().format("%H:%M:%S")) } else { format!("Reloaded at {}", Local::now().format("%H:%M:%S")) };
                            }
                        });
                        ui.small(egui::RichText::new(tr(lang,
                            "\"Export all data\" creates a JSON with all profiles and preferences (token excluded). Import accepts bundle JSON and profili-shaped JSON.",
                            "«Esporta tutti i dati» crea un JSON con tutti i profili e le preferenze (token escluso). L'import accetta bundle JSON e formato profili.")).color(ui.visuals().strong_text_color()));
                    });

                    ui.add_space(12.0);
                    // --- Ripristino ---
                    egui::Frame::none().fill(ui.visuals().faint_bg_color).stroke(egui::Stroke::new(1.0_f32, ui.visuals().widgets.noninteractive.bg_stroke.color)).rounding(egui::CornerRadius::same(22)).inner_margin(egui::Margin::same(18)).show(ui, |ui| {
                        ui.label(egui::RichText::new(tr(lang, "Reset", "Ripristino")).size(22.0).strong());
                        ui.label(egui::RichText::new(tr(lang, "Resets only active profile progress: profiles remain.", "Azzera solo i progressi del profilo attivo: i profili restano.")).small().color(ui.visuals().strong_text_color()));
                        ui.add_space(10.0);
                        ui.horizontal_wrapped(|ui| {
                            ui.vertical(|ui| {
                                ui.label(egui::RichText::new(tr(lang, "Clear profile", "Svuota il profilo")).strong());
                                ui.small(egui::RichText::new(tr(lang,
                                    "Removes words, reviews, completed units and today's plan. Lexicon can be reimported from Wortliste.",
                                    "Rimuove parole, ripassi, unità completate e piano di oggi. Il lessico può essere reimportato dalla Wortliste.")).color(ui.visuals().strong_text_color()));
                            });
                            ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                                if ui.add(egui::Button::new(tr(lang, "Clear", "Svuota")).min_size(egui::vec2(110.0, 40.0)).rounding(egui::CornerRadius::same(14))).clicked() {
                                    self.show_reset_confirm = true;
                                }
                            });
                        });
                    });

                    ui.add_space(8.0);
                    ui.separator();
                    ui.add_space(8.0);
                    ui.horizontal_wrapped(|ui| {
                        ui.small(egui::RichText::new("OpenLingo desktop · Rust").color(ui.visuals().strong_text_color()));
                        ui.with_layout(egui::Layout::right_to_left(egui::Align::Center), |ui| {
                            ui.small(egui::RichText::new(tr(lang,
                                "Works offline: feed and backup update only when requested.",
                                "Funziona offline: feed e backup si aggiornano solo quando li chiedi.")).color(ui.visuals().strong_text_color()));
                        });
                    });

                    if !self.status.is_empty() {
                        ui.add_space(12.0);
                        egui::Frame::none().fill(ui.visuals().selection.bg_fill.gamma_multiply(0.35)).rounding(egui::CornerRadius::same(14)).inner_margin(egui::Margin::symmetric(12, 8)).show(ui, |ui| {
                            ui.label(&self.status);
                        });
                    }
                    ui.add_space(20.0);
                });
            });
        });

        // Reset confirmation dialog
        if self.show_reset_confirm {
            egui::Window::new(tr(lang, "Clear profile?", "Svuotare il profilo?"))
                .collapsible(false)
                .resizable(false)
                .anchor(egui::Align2::CENTER_CENTER, egui::vec2(0.0, 0.0))
                .show(ui.ctx(), |ui| {
                    ui.label(tr(lang,
                        "This will delete active profile data on this computer. This cannot be undone.",
                        "Verranno cancellati i dati del profilo attivo su questo computer. L'operazione non si può annullare."));
                    ui.add_space(12.0);
                    ui.horizontal(|ui| {
                        if ui.button(tr(lang, "Cancel", "Annulla")).clicked() {
                            self.show_reset_confirm = false;
                        }
                        if ui.add(egui::Button::new(egui::RichText::new(tr(lang, "Clear", "Svuota")).strong())).clicked() {
                            match self.store.svuota_profilo_attivo() {
                                Ok(_) => {
                                    self.refresh_review_queue();
                                    self.sync_settings_drafts_from_store();
                                    self.status = tr(lang, "Profile cleared.", "Profilo svuotato.").into();
                                }
                                Err(e) => self.status = e,
                            }
                            self.show_reset_confirm = false;
                        }
                    });
                });
        }
    }
}

impl eframe::App for OpenLingoApp {
    fn update(&mut self, ctx: &egui::Context, _frame: &mut eframe::Frame) {
        let system_dark = ctx.style().visuals.dark_mode; // best-effort; system theme may be reflected by eframe
        apply_theme(ctx, &self.store, system_dark);
        egui::TopBottomPanel::top("top").show(ctx, |ui| {
            ui.add_space(2.0);
            self.top_bar(ui);
        });
        egui::TopBottomPanel::bottom("bottom_nav").show(ctx, |ui| self.bottom_nav(ui));
        egui::CentralPanel::default()
            .frame(egui::Frame::central_panel(&ctx.style()).inner_margin(egui::Margin::symmetric(12, 8)))
            .show(ctx, |ui| match self.screen {
                Screen::Home => self.screen_home(ui),
                Screen::QuickStudy => self.screen_quick_study(ui),
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
    fn difficult_words_selects_lapses_and_errors() {
        let dir = temp_dir("weak");
        let mut store = Store::load_from(dir.clone());
        let id = store.create_profile("Noa");
        {
            let p = store.repo.profili.get_mut(&id).unwrap();
            p.stato.parole = vec![
                WordEntity { id:"w1".into(), german:"eins".into(), italian:"uno".into(), ..Default::default() },
                WordEntity { id:"w2".into(), german:"zwei".into(), italian:"due".into(), ..Default::default() },
            ];
            let mut reviews = HashMap::new();
            reviews.insert("w1".into(), ReviewEntity { word_id:"w1".into(), due_at: 1, repetitions: 1, lapses: 3, ease_factor: 1.8, interval_days: 1, ..Default::default() });
            reviews.insert("w2".into(), ReviewEntity { word_id:"w2".into(), due_at: 1, repetitions: 5, lapses: 0, ease_factor: 2.6, interval_days: 10, ..Default::default() });
            p.stato.reviews = reviews;
            p.stato.ripassi_log.push(RipassoLog { word_id:"w2".into(), timestamp: 1, corretta: false, verifica: false, extra: HashMap::new() });
        }
        let weak = store.difficult_words(10);
        assert!(!weak.is_empty());
        assert_eq!(weak[0].id, "w1"); // higher lapse score should rank first
        assert!(weak.iter().any(|w| w.id == "w2")); // recent error included
        let _ = fs::remove_dir_all(dir);
    }

    #[test]
    fn embedded_grammar_units_practice_saves_mastery() {
        use crate::data::embedded_grammar_units;
        let units = embedded_grammar_units();
        assert!(units.len() >= 15);
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
