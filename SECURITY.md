# Security

## Reporting a vulnerability

Please open a private security advisory on GitHub or contact the repository maintainers directly, describing:

- the environment and app version;
- steps to reproduce;
- estimated impact.

Do not report issues that involve attacks on third-party systems.

## Security practices in this project

- **APK integrity verification**: updates download the APK and verify it via SHA-256 (`VersionInfo.apkSha256`, `VerificaApk.isApkValido`).
- **Cloud backup**: user-chosen GitHub token (fine-grained, limited permissions), stored only in local DataStore; no token embedded in the repository.
- **Data export**: user data can be exported to JSON from the device (see [docs/EXPORT-DATI.md](docs/EXPORT-DATI.md)).
- **Data safety**: `allowBackup="false"` and custom data extraction rules for cloud backup and device transfer.
- **AI keys**: configured by the user in Settings; no shared or embedded keys.

## Audit scope

- **SAST**: static analysis of Kotlin/Compose/Gradle code.
- **DAST**: not applicable in a fully autonomous form for an Android/desktop app; evaluation scope includes the published APK and network surfaces (AI APIs, GitHub).
- Findings are classified by severity (Critical / High / Medium / Low).

## OpenSSF Scorecard

Project quality target: **Scorecard ≥ 0.4** (as tracked). Current results and analysis details are reported in [SECURITY-AUDIT.md](SECURITY-AUDIT.md).
