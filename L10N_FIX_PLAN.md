# L10n Remediation Plan

> **Source of truth:** `l10n-review/REPORT.md` (cross-agent verification report, 2026-05-02).
> Every action below traces back to a finding there. If reality and report disagree, run the verification commands in this doc and trust the filesystem.

## How to resume from a fresh session

1. `cd` into repo root, read this file, read `l10n-review/REPORT.md`.
2. Find the lowest unchecked `[ ]` row in the active phase below — that's the resume point.
3. Run the **Verification command** for that phase first to confirm reality matches what's checked. If a `[x]` row turns out to be undone, uncheck it and redo.
4. Phases must complete in order: P0 → P1 → P2 → P3. P0 blocks shipping. P1 is small fixes worth landing before review. P2/P3 are deferred (recorded for later).

## Branch strategy

- Working branch: `add-multilingual-support` (existing, not yet PR'd as of 2026-05-02).
- One commit per phase (P0, P1) keeps the diff readable. P0 ships independently if needed.

---

## P0 — Critical: missing keys cause English fallback at runtime

### P0a: 6 legacy biometric error strings missing from sample-shared in ~40 locales

**Root cause:** `biometric_error_lockout`, `_hardware_unavailable`, `_not_enrolled`, `_timeout`, `_no_space`, `_unknown` were added to `cmp-sample-shared/src/commonMain/composeResources/values/strings.xml` after the bulk-translation pass on most locales.

**18 locales that DO have them** (verified 2026-05-02 via grep): `am, ar, bn, fil, gu, kn, lt, mk, mr, pa, si, sl, sq, sr, sw, ta, te, ur`.

**Verification command:**
```bash
grep -L "biometric_error_lockout" cmp-sample-shared/src/commonMain/composeResources/values-*/strings.xml | wc -l
# 0 = done. Anything else = locales still missing.
```

**Action:** Add the 6 keys with locale-appropriate translations to every file returned by:
```bash
grep -L "biometric_error_lockout" cmp-sample-shared/src/commonMain/composeResources/values-*/strings.xml
```

Translation seed: copy the English source from `values/strings.xml`, machine-translate per locale matching Google Pay convention, then visually spot-check via `cmp-sample-android` install on at least 3 sampled locales (de, hi, ar) before considering P0a complete.

- [x] P0a executed (script run, all 6 × 40 locales added — translations sourced from `l10n-review/batch_*.json` `suggested_fix` payloads, except he/iw which were translated manually)
- [x] P0a verification grep returns 0 (all 58 locales now have all 6 keys)

### P0b: Filipino has 3 untranslated English strings

Per `l10n-review/REPORT.md:501-503`:
- `login_screen_title` = "Login Screen" (English) → "Screen ng Pag-login"
- `home_screen_title` = "Home Screen" (English) → "Home Screen" (Filipino keeps loanword) or "Pangunahing Screen"
- `biometric_setup_title` = "Biometric Setup" (English) → "Pag-setup ng Biometrics"

File: `cmp-sample-shared/src/commonMain/composeResources/values-fil/strings.xml`.

**Verification command:**
```bash
grep -E "(Login Screen|Home Screen|Biometric Setup)<" cmp-sample-shared/src/commonMain/composeResources/values-fil/strings.xml
# Empty result = done.
```

- [x] P0b: 3 fil strings translated (Screen ng Pag-login / Pangunahing Screen / Pag-setup ng Biometric)

### P0 Final verification

```bash
./gradlew :cmp-sample-android:assembleDebug :cmp-sample-shared:compileDebugKotlinAndroid
# Build green = no XML parse errors introduced.
```

- [x] Build green (verified `:cmp-sample-android:assembleDebug` + `:cmp-sample-shared:compileDebugKotlinAndroid` + `:compileKotlinDesktop`)
- [ ] Single P0 commit landed on `add-multilingual-support`

---

## P1 — High-value targeted fixes, locale-by-locale

### P1a: `af` cd_* labels use wrong verb "Stawe"

Per `l10n-review/REPORT.md:529`. File: `cmp-sample-shared/src/commonMain/composeResources/values-af/strings.xml`.

Replace `Stawe met X` → `Verifieer met X` for the 4 keys: `cd_fingerprint_icon`, `cd_face_scan_icon`, `cd_iris_scan_icon`, `cd_device_credential_icon`.

Verification: `grep -c "Stawe" cmp-sample-shared/src/commonMain/composeResources/values-af/strings.xml` → 0.

- [ ] P1a executed

### P1b: `ko` passcode terminology split (암호 vs 비밀번호)

Per `l10n-review/REPORT.md:130, 138`. Standardize on `비밀번호` (Google Pay KR convention) across:
- `mifos-authenticator-passcode/src/commonMain/composeResources/values-ko/strings.xml`
- `cmp-sample-shared/src/commonMain/composeResources/values-ko/strings.xml`

Replace every `암호` → `비밀번호`. Watch for compounds (e.g., `암호화` = "encryption" — DO NOT replace that one if it appears).

Verification:
```bash
grep -n "암호" mifos-authenticator-passcode/src/commonMain/composeResources/values-ko/strings.xml cmp-sample-shared/src/commonMain/composeResources/values-ko/strings.xml
# Empty result, or only 암호화/암호화된 contexts = done.
```

- [ ] P1b executed

### P1c: `ca` "Desbloca" → "Desbloqueja"

Per `l10n-review/REPORT.md:654`. File: `cmp-sample-shared/src/commonMain/composeResources/values-ca/strings.xml`.

Affected keys: `biometric_prompt_title`, `biometric_prompt_subtitle`, `unlock_with_biometrics`.

Also per `REPORT.md:655-657`: `Configureu` → `Configura`, `Torneu-ho` → `Torna-ho` in 3 strings.

Verification: `grep -E "Desbloca[^q]|Configureu|Torneu-ho" cmp-sample-shared/src/commonMain/composeResources/values-ca/strings.xml` → empty.

- [ ] P1c executed

### P1d: `hi` ASCII period `.` → danda `।` in 3 new error strings

Per `l10n-review/REPORT.md:250`. File: `cmp-sample-shared/src/commonMain/composeResources/values-hi/strings.xml`.

Affected keys: `biometric_error_invalid_registration_data`, `biometric_error_invalid_arguments_auth`, `biometric_error_invalid_arguments_registration`.

Replace each `.` (after Hindi text) with `।`.

Verification:
```bash
grep -E "biometric_error_invalid.*\." cmp-sample-shared/src/commonMain/composeResources/values-hi/strings.xml
# Should return 0 lines (no ASCII periods in those 3 strings).
```

- [ ] P1d executed

### P1 Final verification

- [ ] Build green: `./gradlew :cmp-sample-android:assembleDebug`
- [ ] Manual on-device check of af, ko, ca, hi locales (one screenshot each in PR description)
- [ ] P1 commit landed

---

## P2 — Register violations: deferred

**Decision (2026-05-02):** Defer to community / native-speaker review via GitHub issues labeled `l10n/register-review`. These changes (formal↔informal pronoun-form rewrites) are contentious — a non-native commit risks shipping wrong choices that a native reviewer would have caught.

**Affected locales** (16): `de, hu, ru, uk, bg, be, sr, hr, sl, sq, ro, lt, lv, mk, el, tr`.

Per-locale specifics in `l10n-review/REPORT.md`:
- `de`: 556 (sample uses `du`, banking standard is `Sie`)
- `hu`: 671-679 (uses formal `Ön`, Google Pay HU uses informal `te`)
- `ru`: 765-776
- `uk`: 783-796
- `bg`: 805-816
- `be`: 892-895
- `sr`: 824-836
- `hr`: 843-853
- `sl`: 860-868
- `sq`: 875-885
- `ro`: 912-915
- `lt`: 939-947
- `lv`: 954-960
- `mk`: 969-974
- `el`: 983-994
- `tr`: 1003-1012

- [ ] One GH issue per locale opened (16 issues), labeled `l10n/register-review`, each linking to its `REPORT.md` line range
- [ ] Note in PR description that P2 is tracked as separate issues

---

## P3 — Stylistic / verbosity / terminology drift: deferred indefinitely

**Decision (2026-05-02):** Skip until P0+P1 are merged and feedback comes in. Many of these are individually-defensible style choices that don't move user-visible quality the way P0/P1 fixes do.

Examples (non-exhaustive, see `REPORT.md` for full list):
- `cd_toggle_passcode_visibility` verbosity in mr (317), gu (258), bn (296), nb (721)
- "ઉપકરણ"/"ડિવાઇસ" inconsistency in gu (271)
- "ਬਾਇਓਮੈਟ੍ਰਿਕ"/"ਬਾਇਓਮੀਟ੍ਰਿਕ" spelling inconsistency in pa (287)
- ja `try_again` "再試行" preferred over "もう一度試す" (111)
- ar accessibility labels carry definite article (164)
- Many cd_* labels in various locales are nominal phrases instead of imperatives

- [ ] Optional: open one umbrella GH issue `l10n/style-cleanup` linking to this section. No commits required.

---

## Status log

| Date | Phase | Action |
|------|-------|--------|
| 2026-05-02 | Plan created | This file written |
| 2026-05-03 | P0a complete | 240 strings added across 40 locales |
| 2026-05-03 | P0b complete | fil 3 strings translated |
| 2026-05-03 | P0 verified | Build green, all 6 × 58 = 348 keys present |

