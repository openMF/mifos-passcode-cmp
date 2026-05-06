/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
package org.mifos.authenticator.l10n

import org.gradle.api.Project
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import javax.inject.Inject

/**
 * Configuration DSL for the `org.mifos.authenticator.l10n` plugin.
 *
 * Example:
 * ```kotlin
 * mifosL10n {
 *     locales("hi", "ar", "ta", "de")
 *     // optional overrides:
 *     // branch.set("development")               // pull latest unreleased templates
 *     // libraryVersion.set("0.1.0")             // override version detection
 *     // targetModule.set(layout.projectDirectory) // default = current project
 * }
 * ```
 */
abstract class MifosL10nExtension @Inject constructor(project: Project) {

    /**
     * BCP-47-style locale tags to download. Matches the `values-XX` directory naming used by
     * Compose Resources (and Android XML resources). Examples: `"hi"`, `"ar"`, `"ta"`, `"de"`,
     * `"pt-rBR"`, `"zh-rCN"`. Use `"default"` or omit to also pull the base `values/strings.xml`.
     */
    abstract val locales: ListProperty<String>

    /**
     * The Gradle module whose `composeResources/values-XX/strings.xml` files will be created or
     * updated. Defaults to the project the plugin is applied to.
     */
    abstract val targetModule: DirectoryProperty

    /**
     * Optional override for the git ref used in the templates URL. Default behaviour is to use
     * the version constant baked into the plugin at build time (matching the library version).
     * Set to `"development"` (or any branch / tag / commit SHA) to pull from a different ref.
     */
    abstract val branch: Property<String>

    /**
     * Optional override for the library version used in URL construction. Set this to skip the
     * baked-in `BuildConfig.LIBRARY_VERSION` lookup — useful in tests, in monorepos with
     * non-standard tag schemes, or when explicitly bootstrapping templates from a future
     * release before that release ships.
     */
    abstract val libraryVersion: Property<String>

    /**
     * Optional override for the GitHub repository owner. Defaults to `openMF`. Override when
     * using a fork that hosts customised translations.
     */
    abstract val repoOwner: Property<String>

    /**
     * Optional override for the GitHub repository name. Defaults to `mifos-passcode-cmp`.
     */
    abstract val repoName: Property<String>

    init {
        targetModule.convention(project.layout.projectDirectory)
        repoOwner.convention("openMF")
        repoName.convention("mifos-passcode-cmp")
    }

    /** Convenience shortcut for setting the locales list with vararg syntax. */
    fun locales(vararg values: String) {
        locales.set(values.toList())
    }
}
