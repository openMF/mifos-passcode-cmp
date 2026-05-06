# Passcode L10n Templates

Canonical translations for the strings consumed by `mifos-authenticator-passcode`.

These files are **not** bundled in the published library artifact. The library no longer
ships its own translations; consumer apps construct a `PasscodeStrings` instance from
their own resources and pass it to `PasscodeScreen(...)`.

## How to use

### Option A — copy the templates manually

For each locale your app supports, copy the matching file from this tree into your
app's `composeResources/values-XX/strings.xml`:

```
l10n-templates/passcode/values/strings.xml      → <your-module>/.../composeResources/values/strings.xml
l10n-templates/passcode/values-de/strings.xml   → <your-module>/.../composeResources/values-de/strings.xml
l10n-templates/passcode/values-ja/strings.xml   → <your-module>/.../composeResources/values-ja/strings.xml
…
```

If a `values-XX/strings.xml` already exists in your module, **append** the `<string>`
elements from the template inside the existing `<resources>` element rather than
overwriting the file.

Then construct `PasscodeStrings` in your composable code:

```kotlin
@Composable
fun rememberPasscodeStrings(): PasscodeStrings = PasscodeStrings(
    createPasscode = stringResource(Res.string.mifos_passcode_create_passcode),
    confirmPasscode = stringResource(Res.string.mifos_passcode_confirm_passcode),
    // … 14 more ASCII string fields …
    digits = listOf(
        stringResource(Res.string.mifos_passcode_digit_0),
        stringResource(Res.string.mifos_passcode_digit_1),
        // … digit_2 through digit_9 …
    ),
)
```

See `cmp-sample-shared/.../platformAuthentication/PasscodeScreenWithBiometrics.kt` in
this repo for a working reference implementation.

### Option B — use the Gradle plugin (planned, not yet shipped)

A future Gradle plugin (`org.mifos.authenticator.l10n`) will fetch these templates over
HTTPS from a versioned tag of this repo and append them to your module's existing
`strings.xml` automatically. Track progress in `L10N_FIX_PLAN.md` (Phase 4).

## Naming convention

Every key uses the prefix `mifos_passcode_` so they cannot collide with your app's
existing string keys. Example mapping from the lib's internal API field name to the
resource key:

| `PasscodeStrings` field             | Resource key                                  |
|-------------------------------------|-----------------------------------------------|
| `createPasscode`                    | `mifos_passcode_create_passcode`              |
| `confirmPasscode`                   | `mifos_passcode_confirm_passcode`             |
| `confirmOldPasscode`                | `mifos_passcode_confirm_old_passcode`         |
| `enterPasscode`                     | `mifos_passcode_enter_your_passcode`          |
| `passcodeDoNotMatch`                | `mifos_passcode_passcode_do_not_match`        |
| `tryAgain`                          | `mifos_passcode_try_again`                    |
| `skip`                              | `mifos_passcode_skip`                         |
| `forgotPasscode`                    | `mifos_passcode_forgot_passcode`              |
| `enableExternalAuthDialogTitle`     | `mifos_passcode_enable_external_auth_dialog_title`       |
| `enableExternalAuthDialogDescription` | `mifos_passcode_enable_external_auth_dialog_description` |
| `yes`                               | `mifos_passcode_yes`                          |
| `no`                                | `mifos_passcode_no`                           |
| `cdTogglePasscodeVisibility`        | `mifos_passcode_cd_toggle_passcode_visibility` |
| `cdDeletePasscodeKey`               | `mifos_passcode_cd_delete_passcode_key`       |
| `passcodeLength4Digits`             | `mifos_passcode_passcode_length_4_digits`     |
| `passcodeLength6Digits`             | `mifos_passcode_passcode_length_6_digits`     |
| `digits[0]`..`digits[9]`            | `mifos_passcode_digit_0`..`mifos_passcode_digit_9` |

## Locales currently bundled

58 locale variants plus the base English `values/strings.xml`:

`af`, `am`, `ar`, `be`, `bg`, `bn`, `ca`, `cs`, `da`, `de`, `el`, `en-rGB`, `es`, `et`,
`fa`, `fi`, `fil`, `fr`, `gu`, `he`, `hi`, `hr`, `hu`, `in`, `it`, `iw`, `ja`, `kn`,
`ko`, `lt`, `lv`, `mk`, `ml`, `mr`, `nb`, `nl`, `pa`, `pl`, `pt-rBR`, `pt-rPT`, `ro`,
`ru`, `si`, `sk`, `sl`, `sq`, `sr`, `sv`, `sw`, `ta`, `te`, `th`, `tr`, `uk`, `ur`,
`vi`, `zh-rCN`, `zh-rTW`.

Native numeral systems (Devanagari, Eastern Arabic-Indic, Bengali, Tamil, Telugu, etc.)
override the `mifos_passcode_digit_0..9` keys so the on-screen keypad shows
locale-appropriate glyphs. See the per-locale strings.xml files for examples.

## Translation quality

These translations originated from a multi-pass machine-translation effort against
Google Pay localization conventions. The breadth-first nature of that pass means the
**low-resource locales (be, mk, sq, sl, lv, lt, sr, hr) should have a native-speaker
review pass** before final ship. The major locales (en, de, fr, es, it, ja, ko, zh-rCN,
zh-rTW, ru, pt-rBR, hi, ar) are cross-agent verified and rated high-quality.
