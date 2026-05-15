# Passcode L10n Templates

Canonical translations for the strings consumed by `mifos-authenticator-passcode`.

## Most consumers don't need to do anything

The library ships these translations inside its own Compose Resources. A consumer
that writes `PasscodeScreen(passcodeManager = pm)` — with no `strings = ...` argument —
gets locale-aware copy automatically: a Hindi device sees Hindi, an Arabic device
sees Arabic, the rest fall back to English. The default backing the `strings`
parameter is `defaultPasscodeStrings()`, exported from
`org.mifos.authenticator.passcode`.

The rest of this doc is a reference for the minority of consumers who want to
**override** some or all of the strings — typically to match brand voice, add a
locale we don't bundle, or rewire `mifos_passcode_*` keys to the consumer's own
resource scheme.

Every key uses the prefix `mifos_passcode_` so the templates can be appended to your
app's existing `strings.xml` without colliding with your own keys.

## How to override

### Path A — copy the strings you want to customise

Browse [`l10n-templates/passcode/`](https://github.com/openMF/mifos-passcode-cmp/tree/main/l10n-templates/passcode)
on GitHub and copy the files (or individual `<string>` entries) you want to override
into your own module:

```
l10n-templates/passcode/values/strings.xml      → <your-module>/src/commonMain/composeResources/values/strings.xml
l10n-templates/passcode/values-de/strings.xml   → <your-module>/src/commonMain/composeResources/values-de/strings.xml
…
```

Edit the entries to match your brand voice, then construct a `PasscodeStrings` from
your own resources (see "Constructing `PasscodeStrings` from the keys" below) and
pass it explicitly:

```kotlin
PasscodeScreen(
    passcodeManager = pm,
    strings = rememberPasscodeStringsFromYourResources(),
)
```

An explicit `strings` argument always wins over the library's default — the bundled
translations never load for that callsite.

If your `values-XX/strings.xml` already exists, **append** the `<string>` entries from
the template inside the existing `<resources>` element rather than overwriting the
file.

## Constructing `PasscodeStrings` from the keys

Whether you used Path A or Path B, once the keys are in your module's `composeResources/`:

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

See [`PasscodeScreenWithBiometrics.kt`](../../cmp-sample-shared/src/commonMain/kotlin/cmp/sample/shared/platformAuthentication/PasscodeScreenWithBiometrics.kt)
in this repo for a complete working reference.

## Naming convention

Every key uses the prefix `mifos_passcode_` so they cannot collide with your app's
existing string keys. Mapping from `PasscodeStrings` field name to resource key:

| `PasscodeStrings` field             | Resource key                                              |
|-------------------------------------|-----------------------------------------------------------|
| `createPasscode`                    | `mifos_passcode_create_passcode`                          |
| `confirmPasscode`                   | `mifos_passcode_confirm_passcode`                         |
| `confirmOldPasscode`                | `mifos_passcode_confirm_old_passcode`                     |
| `enterPasscode`                     | `mifos_passcode_enter_your_passcode`                      |
| `passcodeDoNotMatch`                | `mifos_passcode_passcode_do_not_match`                    |
| `tryAgain`                          | `mifos_passcode_try_again`                                |
| `skip`                              | `mifos_passcode_skip`                                     |
| `forgotPasscode`                    | `mifos_passcode_forgot_passcode`                          |
| `enableExternalAuthDialogTitle`     | `mifos_passcode_enable_external_auth_dialog_title`        |
| `enableExternalAuthDialogDescription` | `mifos_passcode_enable_external_auth_dialog_description` |
| `yes`                               | `mifos_passcode_yes`                                      |
| `no`                                | `mifos_passcode_no`                                       |
| `cdTogglePasscodeVisibility`        | `mifos_passcode_cd_toggle_passcode_visibility`            |
| `cdDeletePasscodeKey`               | `mifos_passcode_cd_delete_passcode_key`                   |
| `passcodeLength4Digits`             | `mifos_passcode_passcode_length_4_digits`                 |
| `passcodeLength6Digits`             | `mifos_passcode_passcode_length_6_digits`                 |
| `digits[0]`..`digits[9]`            | `mifos_passcode_digit_0`..`mifos_passcode_digit_9`        |

## Locales currently bundled

58 locale variants plus the base English `values/strings.xml`:

`af`, `am`, `ar`, `be`, `bg`, `bn`, `ca`, `cs`, `da`, `de`, `el`, `en-rGB`, `es`, `et`,
`fa`, `fi`, `fil`, `fr`, `gu`, `he`, `hi`, `hr`, `hu`, `in`, `it`, `iw`, `ja`, `kn`,
`ko`, `lt`, `lv`, `mk`, `ml`, `mr`, `nb`, `nl`, `pa`, `pl`, `pt-rBR`, `pt-rPT`, `ro`,
`ru`, `si`, `sk`, `sl`, `sq`, `sr`, `sv`, `sw`, `ta`, `te`, `th`, `tr`, `uk`, `ur`,
`vi`, `zh-rCN`, `zh-rTW`.

Native numeral systems (Devanagari, Eastern Arabic-Indic, Bengali, Tamil, Telugu, etc.)
override the `mifos_passcode_digit_0..9` keys so the on-screen keypad shows
locale-appropriate glyphs. See the per-locale `strings.xml` files for examples.

## Translation quality

These translations originated from a multi-pass machine-translation effort against
Google Pay localization conventions. The breadth-first nature of that pass means the
**low-resource locales (be, mk, sq, sl, lv, lt, sr, hr) should have a native-speaker
review pass** before final ship. The major locales (en, de, fr, es, it, ja, ko, zh-rCN,
zh-rTW, ru, pt-rBR, hi, ar) are cross-agent verified and rated high-quality.
