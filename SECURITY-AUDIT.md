# Security report — OpenLingo

Date: 2026-11-09  
Audited version: 1.48.73 (versions 1.48.74 and 1.48.75 introduced only build, export-file signing and documentation changes)

## Scope and methodology

- **SAST**: static analysis of source code (Kotlin/Compose, `:core`, `:app`, `:desktop`, GitHub Actions workflows, Gradle configurations) using dedicated pattern scans (hardcoded credentials, eval/exec/runtime code loading, unsafe deserialization, XSS/dangerouslySetInnerHTML, WebView+JavaScript, injection).
- **DAST**: not executed in this session. The app’s network surfaces (Groq/Gemini AI APIs, GitHub Contents API for backup) require user credentials and were not dynamically probed; they were reviewed statically (endpoints, headers, error handling). A full authenticated DAST remains recommended before store publication.
- **OpenSSF Scorecard**: project quality target **≥ 0.4**; score observed after the last automated analysis: **3.1**.

## Checks performed in this session

| # | Area | Result |
|---|---|---|
| 1 | Hardcoded secrets/API keys/tokens in source code | **OK**: no real embedded keys; AI and GitHub tokens managed as user configuration (DataStore) |
| 2 | Unsafe deserialization systems (`eval`, `exec`, `ProcessBuilder`, runtime loading, `ObjectInputStream`, pickle) | **OK**: no usage found in Kotlin/Gradle code paths reviewed |
| 3 | XSS / `dangerouslySetInnerHTML` / WebView scripting | **OK**: no XSS surface in Compose UI; no WebView |
| 4 | APK download integrity | **Found + fixed**: added SHA-256 verification (`VersionInfo.apkSha256`, `VerificaApk.isApkValido`, `sha256(file)`) |
| 5 | Device data backup | **Found + fixed**: `allowBackup="false"` + `data_extraction_rules.xml` for cloud backup and device transfer |
| 6 | Debug keystore | **Found + fixed**: debug keystore removed from Git tracking (`git rm --cached`) |
| 7 | Deep links / export files | OK: local JSON data export with format validation |

## Findings and status

| Severity | Finding | Status |
|---|---|---|
| Critical | Debug keystore tracked in repository | **Resolved**: removed from version control |
| High | Update flow without verification of downloaded APK hash | **Resolved**: SHA-256 verification active |
| High | Android backup potentially enabled by default | **Resolved**: `allowBackup=false` + rules |
| Medium | — | None identified in static scan |
| Low | — | None identified in static scan |

### Accepted residuals

- AI APIs and GitHub tokens are per-user: they are not shared secrets and remain local.
- Release signing depends on configured keystore/environment (`TEDESCO_KEYSTORE_*` variables for release builds where used); debug keystore is no longer versioned.

## Recommendations

1. Run OpenSSF Scorecard on every release (target ≥ 0.4).
2. Add CI SAST (e.g. Semgrep/CodeQL) for regressions.
3. Use a dedicated release signing key for published APKs.
4. Perform authenticated DAST on network surfaces before store publication.

## Conclusions

No residual critical findings emerged from static analysis and manual review of risk surfaces. High-severity issues identified during preparation of release 1.48.73 were remediated in that session.
