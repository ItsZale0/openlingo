# Security

## Supported / current version

- **Latest app version (this repository state):** 1.51.15 (`VERSION_CODE` 338)
- Security fixes and documentation updates are tracked in releases/tags and in [SECURITY-AUDIT.md](SECURITY-AUDIT.md).

## Reporting a vulnerability

Please report suspected vulnerabilities privately (preferred: GitHub private security advisory) or contact the repository maintainers directly. Include:

- affected app version and platform (Android/desktop);
- environment and configuration details (without sharing secrets);
- clear reproduction steps and proof-of-concept if safe;
- estimated impact and any suggested mitigation.

Do not report issues that involve attacks on third-party systems or unauthorized testing.

## Security practices in this project

- **Offline-first & privacy by design:** core learning works offline; no mandatory account, ads, or trackers.
- **APK integrity verification:** update flow verifies downloaded APK integrity via SHA-256 (`VersionInfo.apkSha256`, `VerificaApk.isApkValido`) and signature consistency checks.
- **Local data handling:** profiles and learning data are stored locally (DataStore/JSON depending on platform); Android system backup is restricted (`allowBackup="false"` with data extraction rules).
- **Data export:** users can export/import their data as JSON (see [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md)). Export files may contain sensitive data (including user-provided AI keys) and should be stored securely.
- **Cloud backup (optional):** user-provided GitHub token (fine-grained, limited permissions) stored only locally; no token embedded in the repository.
- **AI keys (optional):** configured by the user in Settings and stored locally; no shared or embedded API keys in source.
- **No embedded secrets in repo:** repository is reviewed to avoid committing real credentials/keystores/tokens.

## Audit scope and approach

- **SAST (static):** code/config review for common issues (injection sinks, unsafe deserialization, hardcoded secrets, insecure crypto/storage patterns, unsafe update handling) in Kotlin/Compose/Gradle and app/desktop modules.
- **DAST (dynamic):** not continuously automated for this project in-repo; runtime/network surfaces (AI providers, GitHub APIs, update/download flow) should be tested only in authorized environments. Active scanning is out of scope without explicit authorization.
- **Triage:** findings are assessed by exploitability and impact and classified by severity (Critical / High / Medium / Low).

## OpenSSF Scorecard

Project quality target: **Scorecard ≥ 0.4** (as tracked). Current status, findings, and recommendations are documented in [SECURITY-AUDIT.md](SECURITY-AUDIT.md).
