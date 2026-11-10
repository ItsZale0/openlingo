# Frequently asked questions

## Do I need to create an account?

No. OpenLingo does not require registration and has no ads or trackers. On first launch you create a local profile.

## Does it work without internet?

Yes. Vocabulary, offline exercises, spaced repetition, grammar, listening and production work without a connection. Internet is only needed for optional AI features, GitHub backup and update checks.

## Is AI required?

No. If you do not enter an API key, the app uses only included content and offline exercises. With a Groq or Gemini key (or other supported providers) you can enable tutor, generated exercises and personalized corrections.

## Where does my data go?

It stays on your device. You can export everything to a single JSON file from **Settings → Backup → Export all data**. See [EXPORT-DATI.md](EXPORT-DATI.md).

## Can multiple people use the app?

Yes. Each profile has separate data: progress, reviews, conversations and lessons. You can create as many profiles as you want from settings.

## Will I lose progress when I update the app?

No. Updates keep the same application identifier and signing identity, so profiles and progress remain available. Before changing devices, still export all data.

## How does the app update?

The app checks `updates/version.json` in the repository and offers a new version when available. Before installation it verifies the SHA-256 of the downloaded APK. You can also manually download the latest release from the GitHub releases page.

## I saved a backup to GitHub but can’t find it

During restore the app tries, in order:

1. the active profile’s backup file (`tedesco-backup-<profile>.json`);
2. the shared backup `tedesco-backup.json`;
3. any other valid backup in the repository, from most recent.

If restore cannot find a readable backup, verify the repository, token permissions, and that a backup file exists for the expected profile/name.
