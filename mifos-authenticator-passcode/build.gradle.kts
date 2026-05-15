/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */
import com.vanniktech.maven.publish.DeploymentValidation

/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */

plugins {
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.mifos.cmp.feature)
}


android {
    namespace = "io.github.openmf.mifos.authenticator.passcode"
}


// Bundle the canonical l10n templates from $rootDir/l10n-templates/passcode/
// into the lib's JVM-target JAR at classpath path
// `mifos-passcode-l10n-templates/values-XX/strings.xml`. Consumers can extract
// them with a 15-line Gradle Copy snippet from `l10n-templates/passcode/README.md`
// (see the "Extract from the library JAR" section). Templates ride with the
// library version automatically — no separate plugin publication.
val copyL10nTemplates = tasks.register<Copy>("copyL10nTemplates") {
    from(rootProject.layout.projectDirectory.dir("l10n-templates/passcode")) {
        include("values*/strings.xml")
    }
    into(layout.buildDirectory.dir("generated/l10n-resources/mifos-passcode-l10n-templates"))
}

kotlin {
    sourceSets {
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
        commonMain {
            resources.srcDir(copyL10nTemplates.map { it.destinationDir.parentFile })
        }
    }

}

val artifactId = "mifos-authenticator-passcode"
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PASSCODE_PACKAGE_VERSION") ?: defaultVersion
val desc: String by project
val license: String by project
val creationYear: String by project
val githubRepo: String by project


mavenPublishing {

    publishToMavenCentral(automaticRelease = true, validateDeployment = DeploymentValidation.PUBLISHED)
    signAllPublications()
    coordinates(mavenGroup, artifactId, currentVersion)

    pom {
        name.set("Mifos Passcode Authenticator")
        description.set("Kotlin Multiplatform passcode authentication library providing UI and logic for passcode creation, verification, and management across Android, iOS, Desktop, and Web using Compose Multiplatform.")
        inceptionYear.set("2025")
        url.set("https://github.com/openMF/mifos-passcode-cmp")

        licenses {
            license {
                name.set("Mozilla Public License Version 2.0")
                url.set("https://www.mozilla.org/en-US/MPL/2.0/")
                distribution.set("repo")
            }
        }

        developers {
            developer {
                id.set("openMF")
                name.set("Mifos Initiative")
                url.set("https://github.com/openMF")
            }
        }

        scm {
            url.set("https://github.com/openMF/mifos-passcode-cmp")
            connection.set("scm:git:git://github.com/openMF/mifos-passcode-cmp.git")
            developerConnection.set("scm:git:ssh://git@github.com:openMF/mifos-passcode-cmp.git")
        }
    }
}
