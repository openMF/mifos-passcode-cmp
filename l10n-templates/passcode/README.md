# Passcode L10n Templates

Canonical translations for the strings consumed by `mifos-authenticator-passcode`.

The library does **not** ship its own translations at runtime — consumers construct a
`PasscodeStrings` instance from their own resources and pass it to `PasscodeScreen(...)`.
These template files are the canonical source you copy into your app's
`composeResources/values-XX/strings.xml`.

Every key uses the prefix `mifos_passcode_` so the templates can be appended to your
app's existing `strings.xml` without colliding with your own keys.

## Two ways to bring the templates into your app

### Path A — manual copy (simplest)

Browse [`l10n-templates/passcode/`](https://github.com/openMF/mifos-passcode-cmp/tree/main/l10n-templates/passcode)
on GitHub and copy the files you need into your module:

```
l10n-templates/passcode/values/strings.xml      → <your-module>/src/commonMain/composeResources/values/strings.xml
l10n-templates/passcode/values-de/strings.xml   → <your-module>/src/commonMain/composeResources/values-de/strings.xml
l10n-templates/passcode/values-ja/strings.xml   → <your-module>/src/commonMain/composeResources/values-ja/strings.xml
…
```

If your `values-XX/strings.xml` already exists, **append** the `<string>` entries from
the template inside the existing `<resources>` element rather than overwriting the file.

### Path B — extract from the library JAR (Gradle snippet)

The library JAR (specifically the JVM / desktop target's JAR, published alongside the
Android AAR and other KMP target artifacts) bundles the same templates at classpath
path `mifos-passcode-l10n-templates/values-XX/strings.xml`. Paste this task into your
module's `build.gradle.kts`:

```kotlin
// Resolves a JVM variant of the passcode library and extracts its bundled l10n
// templates into your module's composeResources. Templates ride with the library
// version automatically — re-run after a version bump to pick up new keys.
val mifosPasscodeL10n by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
    attributes {
        attribute(
            org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE,
            objects.named(org.gradle.api.attributes.Usage::class, org.gradle.api.attributes.Usage.JAVA_RUNTIME),
        )
        attribute(
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.attribute,
            org.jetbrains.kotlin.gradle.plugin.KotlinPlatformType.jvm,
        )
    }
}

dependencies {
    // Match this version with your `mifos-authenticator-passcode` dependency.
    mifosPasscodeL10n("io.github.openmf:mifos-authenticator-passcode:0.2.0")
}

tasks.register<Copy>("downloadMifosPasscodeStrings") {
    description = "Extracts mifos-authenticator-passcode l10n templates into composeResources."
    group = "mifos l10n"
    // Customise the locale list to taste; any locale not present in the bundle is silently
    // skipped (this task won't fail). See l10n-templates/passcode/README.md for the full list.
    val locales = listOf("default", "hi", "ar", "ta", "de", "es", "fr", "ja", "ko", "zh-rCN")
    val includes = locales.map { loc ->
        val folder = if (loc == "default") "values" else "values-$loc"
        "mifos-passcode-l10n-templates/$folder/strings.xml"
    }
    from(mifosPasscodeL10n.map { zipTree(it) }) {
        for (pattern in includes) include(pattern)
        // Strip the "mifos-passcode-l10n-templates/" prefix so files land at values-XX/strings.xml
        eachFile {
            relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray())
        }
        includeEmptyDirs = false
    }
    into(layout.projectDirectory.dir("src/commonMain/composeResources"))
}
```

Run with: `./gradlew :<your-module>:downloadMifosPasscodeStrings`

**Caveats:**

- This task **overwrites** existing `values-XX/strings.xml` files. If you've customised
  any of the `mifos_passcode_*` translations, copy your customisations elsewhere first
  or use Path A.
- The version literal in the `dependencies` block must match the version of
  `mifos-authenticator-passcode` you already depend on. Templates and library are
  versioned together by Maven coordinates — there's no "templates server" or
  separately-versioned templates artifact to worry about.
- Only the JVM-target JAR contains the bundled templates. Android-only consumers can
  still use this task — Gradle will resolve the JVM variant via the lib's KMP metadata.
- For locales not in the template bundle, the task silently produces no file for that
  locale; create the missing `values-XX/strings.xml` manually using the key reference
  below.

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
