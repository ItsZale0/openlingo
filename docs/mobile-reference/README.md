# Mobile reference captures (adb)

Captured from connected Android device via `adb` to support "same as mobile app, copy from emulator/device" parity work.

- **Device:** 2511FPC34G (Xiaomi, `klee`), 1268x2756 @ 520dpi
- **Package on device:** `com.alessandro.tedesco` (legacy installed build; launcher `.ui.MainActivity`)
- **Capture date:** 2026-11-11
- **Artifacts:** screenshots (`*.png`) and UI hierarchy dumps (`*.xml`) for key screens: `home`, `esercizi`, `ripasso`, `grammatica`, `progresso`, `scegli_lezione`, `lezioni_sheet`, `lessico` (vocabulary/levels A1-B2 view), `piano` (study plan)

Notes:
- Captures reflect the installed device build/state (Italian UI observed on home) and are used as visual/flow/structure reference for Rust/Compose parity.
- The current repo/brand is OpenLingo (`com.alessandro.openlingo`); differences vs. current source may exist.
- Exhaustive runtime audit and full parity across all Android+desktop flows remain manual/blocked where device/emulator access, AI keys/contexts, or credentials are unavailable.
