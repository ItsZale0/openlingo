## v1.51.15 — 2026-11-11

### Security
- Updated `SECURITY.md` and `SECURITY-AUDIT.md` with the latest maintenance review (SAST-oriented review of recent changes; DAST not executed) for v1.51.15.
- Documented current security posture, scope, and recommendations; retained status of previously remediated findings.

### Build / Release
- Fixed release verification/CI parsing by ensuring `version.properties` and `updates/version.json` are UTF-8 without BOM and version values are consistent.
- Updated `updates/version.json` with APK SHA-256 for the attached `OpenLingo-v1.51.15.apk`.

### Notes
- Attached APK: `OpenLingo-v1.51.15.apk` (debug build, SHA-256: `791d92c9974b39541cf7427cdaf040d1e8fb55d98c6c57d0e571f3d994772562`).
- Desktop assets not attached in this release.

**Full changelog:** https://github.com/ItsZale0/tedesco-app/blob/master/CHANGELOG.md
