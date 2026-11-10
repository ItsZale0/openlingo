# Security report — OpenLingo

## Latest review

- **Date:** 2026-11-11
- **Version/state:** 1.51.15 (`VERSION_CODE` 338), branch state `master`/`publish-1.40.0` (tag `v1.51.15`)
- **Type:** maintenance security review (documentation/SAST-oriented review of recent changes; no authorized DAST execution in this session)

### Changes reviewed (recent)
- English localization/i18n updates (runtime `t()`/`Traduttore` mappings and UI rendering fixes), including pronunciation and desktop/UI strings.
- Documentation and branding updates (README, CONTRIBUTING, docs, issue templates, SECURITY docs, in-app/desktop About strings).
- Build/release metadata maintenance (version bumps, `updates/version.json` consistency, removal of UTF-8 BOM from `version.properties` and `updates/version.json` to fix release verification/CI parsing).

### SAST-oriented checks (this review)
| # | Area | Result |
|---|---|---|
| 1 | Hardcoded secrets/tokens/keystores in reviewed changes | **OK**: no secrets or credentials introduced; user tokens/AI keys remain user-configured/local |
| 2 | Injection / unsafe code execution / deserialization sinks in changes | **OK**: changes are UI/text/i18n/docs/version metadata; no new eval/exec/unsafe deserialization paths introduced |
| 3 | XSS/WebView/script injection surfaces | **OK**: Compose/Desktop UI; no WebView or HTML rendering sinks added |
| 4 | Update/integrity logic impact | **OK**: existing SHA-256/signature verification model unchanged; only manifest/version-file formatting fixed (BOM removed) |
| 5 | Data handling/privacy posture | **OK**: no new telemetry/tracking/backends added; export/backup behavior unchanged by reviewed edits |

### DAST status
- **Not executed** in this session (no authorized target/scope for active scanning). Network-facing surfaces remain: AI provider APIs (optional, user-configured), GitHub backup/sync (optional), and update/download flow.
- Recommended before major/public release: authenticated/authorized dynamic testing of update flow and API integrations in staging, with rate limiting and scope controls.

### Current status summary
- **Critical/High open issues identified in this review:** none.
- Previously remediated high/critical items (APK integrity verification, `allowBackup=false`/extraction rules, debug keystore removal from VCS) remain part of the project’s security posture unless later regressed.
- **Residual risk:** depends on third-party API/provider behavior, user token handling, device compromise, and correctness of release signing/artifact distribution; continue CI checks and periodic audits.

---

## Historical audit (2026-11-09, v1.48.73)

### Scope and methodology
- **SAST:** static analysis of Kotlin/Compose/Gradle and GitHub Actions/configs (hardcoded credentials, unsafe deserialization, XSS/WebView scripting, injection, risky APIs).
- **DAST:** not executed in that session; network surfaces reviewed statically.
- **OpenSSF Scorecard:** target **≥ 0.4**; score observed after last automated analysis at that time: **3.1**.

### Checks performed
| # | Area | Result |
|---|---|---|
| 1 | Hardcoded secrets/API keys/tokens in source code | **OK**: no real embedded keys; AI and GitHub tokens managed as user configuration (DataStore) |
| 2 | Unsafe deserialization systems (`eval`, `exec`, `ProcessBuilder`, runtime loading, `ObjectInputStream`, pickle) | **OK**: no usage found in reviewed Kotlin/Gradle code paths |
| 3 | XSS / `dangerouslySetInnerHTML` / WebView scripting | **OK**: no XSS surface in Compose UI; no WebView |
| 4 | APK download integrity | **Found + fixed**: SHA-256 verification (`VersionInfo.apkSha256`, `VerificaApk.isApkValido`) |
| 5 | Device data backup | **Found + fixed**: `allowBackup="false"` + `data_extraction_rules.xml` |
| 6 | Debug keystore | **Found + fixed**: debug keystore removed from Git tracking |
| 7 | Deep links / export files | OK: local JSON data export with format validation |

### Findings and status
| Severity | Finding | Status |
|---|---|---|
| Critical | Debug keystore tracked in repository | **Resolved** |
| High | Update flow without verification of downloaded APK hash | **Resolved** |
| High | Android backup potentially enabled by default | **Resolved** |
| Medium | — | None identified in static scan |
| Low | — | None identified in static scan |

### Accepted residuals
- AI APIs and GitHub tokens are per-user: not shared secrets and stored locally.
- Release signing depends on configured keystore/environment; debug keystore is not versioned.

## Recommendations

1. Run OpenSSF Scorecard and SAST checks (e.g. CodeQL/Semgrep) in CI on releases/PRs.
2. Perform authorized DAST/staging validation for update flow and optional API integrations before major releases.
3. Use and protect a dedicated release signing key for published APKs.
4. Keep `updates/version.json`/version metadata consistent and BOM-free; verify release artifacts and SHA-256.
5. Periodically review dependencies and GitHub Actions for supply-chain/security updates.
