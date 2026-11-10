# User data export

This guide explains how to export **all data** saved in **OpenLingo**, for moving to a new device or keeping a personal backup.

## Overview

The export produces a single JSON file (`Pacchetto`, format 1) containing:

- **all saved user profiles** (words, SRS reviews, tutor answers, corrections, grammar, comprehension, production, listening, generated lessons, badges, streaks, study plan, sessions);
- **notification preferences** and the selected **theme**.

The file uses the same structure as cloud backup, so it can be used with the restore function.

## On Android

1. Open the app.
2. Go to **Settings** and open the **Backup** section.
3. Tap **Export all data**, then **Export to file**.
4. Choose where to save the file on your device.
5. Keep the `openlingo-dati-YYYYMMDD-HHMM.json` (or `deutsch-dati-...`) file in a safe place.

Notes about the exported file:

- it **does not include** the GitHub cloud-backup token (a credential you must re-enter manually);
- it **may include** AI profile API keys/configuration: store the file securely.

## On desktop

1. Open desktop settings.
2. In the backup section use **Export all data**.
3. The JSON is saved to the Downloads folder as `openlingo-dati-YYYYMMDD-HHMM.json` (or equivalent); move it to a safe place.

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
