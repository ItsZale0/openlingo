# Release process

This page describes how a version of OpenLingo is prepared and published. The process is intentionally simple and verifiable: assets are built locally, the manifest declares the APK SHA-256, and CI checks tag consistency.

## Versioning

The source of truth is `version.properties`:

```properties
VERSION_CODE=...
VERSION_NAME=...
```

`VERSION_CODE` is the integer Android uses to compare versions; `VERSION_NAME` is the human-readable version. Tags follow the format `v<version>` (e.g. `v1.51.12`).

The Gradle task `bumpVersion` increments both and rewrites `updates/version.json`:

```bash
./gradlew bumpVersion -Pchangelog="What changed"
```

The manifest also contains `apkUrl` and `apkSha256`. `bumpVersion` does not know the APK hash: it must be filled in after the build, before committing/tagging the release.

## 1. Prepare the version

1. Update `VERSION_CODE` and `VERSION_NAME` (or use `bumpVersion`).
2. Update `updates/version.json`: version, changelog and `apkUrl` for the tag you are about to create.
3. Update documentation if behavior or UI changes.

## 2. Build the assets

```bash
./gradlew :core:jar :app:assembleDebug :desktop:compileKotlin
./gradlew :app:testDebugUnitTest :core:test
```

For the desktop package:

```bash
./gradlew :desktop:createDistributable
```

Produced artifacts include:

- `app/build/outputs/apk/debug/app-debug.apk` (or the configured build output);
- `desktop/build/compose/binaries/main/app/Deutsch/` (desktop distributable path as configured).

Rename assets using the release naming scheme:

```
OpenLingo-v<version>.apk
OpenLingo-desktop-v<version>-windows.zip
OpenLingo.exe
```

> Note: older releases may use legacy `Deutsch-*` artifact names for compatibility; keep `updates/version.json` (`apkUrl`, `apkSha256`) aligned with the actual uploaded APK name and hash.

## 3. Compute SHA-256

```powershell
Get-FileHash "OpenLingo-v<version>.apk" -Algorithm SHA256
```

Insert the (lowercase) hash into `updates/version.json`:

```json
"apkSha256": "<hash>"
```

The app rejects an APK whose hash does not match the declared one. If the APK is rebuilt after this step, the hash must be recalculated.

## 4. Publish the code

```bash
git add -A
git commit -m "v<version>: <summary>"
git push origin master
git tag v<version>
git push origin v<version>
```

Pushing the tag triggers the **Verify release** workflow, which checks:

- consistency between `version.properties`, `updates/version.json` and the tag name;
- APK compilation (as configured).

The workflow does not publish the release: the maintainer creates it so the distributed APK keeps the same signature and SHA-256 as declared in the manifest.

## 5. Create the release

Create a GitHub release associated with the tag and upload the assets:

- `OpenLingo-v<version>.apk` (Android);
- `OpenLingo-desktop-v<version>-windows.zip` / `OpenLingo.exe` (Windows desktop), if available (legacy `Deutsch-*` names in older releases).

The description should include key changes and installation requirements. Before finishing, verify that:

1. the downloaded APK has the same SHA-256 as the manifest;
2. the signature matches the previous version;
3. the manifest on `master` points to the correct release.

## Signing and updates

All releases must be signed with the **same key**. Android rejects updates signed with a different key: changing keys requires a fresh install and loss of local data unless data is exported and restored.

The debug keystore is not versioned. For reproducible/release builds use environment variables as configured in the project:

```
TEDESCO_KEYSTORE_FILE
TEDESCO_KEYSTORE_PASSWORD
TEDESCO_KEY_ALIAS
TEDESCO_KEY_PASSWORD
```

Without these variables Gradle uses the local debug keystore.

## Pre-release checklist

- [ ] `version.properties` and `updates/version.json` aligned.
- [ ] Tests passing (`:app:testDebugUnitTest`, `:core:test`).
- [ ] APK installable and updatable from the previous version.
- [ ] `apkSha256` matches the uploaded APK.
- [ ] Desktop assets runnable.
- [ ] Changelog and release notes updated.
- [ ] Full data export performed before push (per project workflow).
