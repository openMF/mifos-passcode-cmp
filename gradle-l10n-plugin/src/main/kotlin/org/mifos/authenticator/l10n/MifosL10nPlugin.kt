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

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.register

/**
 * Gradle plugin: `org.mifos.authenticator.l10n`.
 *
 * Adds a `downloadPasscodeStrings` task that fetches translation templates from the
 * mifos-passcode-cmp repo (pinned to the matching library version) and merges them into
 * the consumer's `composeResources/values-XX/strings.xml` files.
 *
 * Usage in the consumer's build.gradle.kts:
 * ```kotlin
 * plugins {
 *     id("org.mifos.authenticator.l10n") version "0.1.0"
 * }
 *
 * mifosL10n {
 *     locales("hi", "ar", "ta", "de")
 * }
 *
 * // Then: ./gradlew downloadPasscodeStrings
 * ```
 */
class MifosL10nPlugin : Plugin<Project> {

    override fun apply(target: Project) {
        val extension = target.extensions.create<MifosL10nExtension>("mifosL10n", target)

        target.tasks.register<DownloadPasscodeStringsTask>("downloadPasscodeStrings") {
            locales.set(extension.locales)
            targetModule.set(extension.targetModule)
            branch.set(extension.branch)
            repoOwner.set(extension.repoOwner)
            repoName.set(extension.repoName)
            libraryVersion.set(
                extension.libraryVersion.orElse(LIBRARY_VERSION),
            )
        }
    }
}
