# User data export and import

This guide explains how to export **all data** saved in **OpenLingo** and how to import (restore) a previously exported backup file, for moving to a new device or keeping a personal backup.

## Overview

The export produces a single JSON file (`Pacchetto`, format 1) containing:

- **all saved user profiles** (words, SRS reviews, tutor answers, corrections, grammar, comprehension, production, listening, generated lessons, badges, streaks, study plan, sessions);
- **notification preferences** and the selected **theme**.

The file uses the same structure as cloud backup, so it can be restored from file or via cloud backup. When present, a full export can also restore the selected theme color.

## On Android

### Export

1. Open the app.
2. Go to **Settings** and open the **Backup** section.
3. Tap **Export all data**, then **Export to file**.
4. Choose where to save the file on your device.
5. Keep the `openlingo-dati-YYYYMMDD-HHMM.json` (or legacy `deutsch-dati-...`) file in a safe place.

Notes about the exported file:

- it **does not include** the GitHub cloud-backup token (a credential you must re-enter manually);
- it **may include** AI profile API keys/configuration: store the file securely.

### Import (restore from file)

1. Open the app.
2. Go to **Settings** and open the **Backup** section.
3. Tap **Import from file** and select a previously exported OpenLingo/backup JSON file.
4. Review the backup summary, then choose:
   - **Merge**: combine with local data, keeping the more advanced progress where applicable.
   - **Replace**: replace local profile data with the backup contents.
5. After restore, profiles/progress/notifications are applied; theme color is restored when included in a full export.

## On desktop

1. Open desktop settings.
2. In the backup section use **Export all data**.
3. The JSON is saved to the Downloads folder as `openlingo-dati-YYYYMMDD-HHMM.json` (or equivalent); move it to a safe place.

> Note: file-based import in the desktop UI may differ by version; if unavailable, use the supported backup/restore options provided by your desktop build or restore on Android and sync/export as needed.

## File structure

```json
{
  "formato": 1,
  "creatoIl": 0,
  "versioneApp": "x.y.z",
  "profili": { ... },
  "notifiche": { ... },
  "temaColore": "..."
}
```

## What “all data” means

| Data | Description |
|---|---|
| Profiles | Each profile with words, reviews, progress, conversations, lessons. |
| Reviews | SRS scheduling/review logs. |
| Tutor | Chat, corrections, production evaluations. |
| Lessons | AI-generated content and feed lessons. |
| Progress | Plan, sessions, badges, streaks. |

## Important notes when moving to a new device

- The export includes **all profiles**: selecting the correct profile on the new device restores its progress.
- **First launch does not create default profiles**: the new user creates one (see the Welcome screen).
- When restoring on a device with existing profiles, restore/merge preserves data according to the app's backup/merge rules.

## Backup vs export

- **Automatic/cloud backup**: backup file format with GitHub token configuration handled separately.
- **Full export**: user data package (see above).

For a complete device migration (data + context), use both as appropriate.
