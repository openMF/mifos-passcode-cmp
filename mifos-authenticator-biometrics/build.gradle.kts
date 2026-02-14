/*
 * Copyright 2026 Mifos Initiative
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 *
 * See https://github.com/openMF/mifos-passcode-cmp/blob/development/LICENSE
 */

import com.vanniktech.maven.publish.SonatypeHost

plugins {
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.mifos.cmp.feature)
}


android {
    namespace = "com.mifos.authenticator.biometrics"
}

kotlin {

    sourceSets {

        commonMain {
//            resources.srcDir("src/commonMain/composeResources")

            dependencies {
                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.serialization)
                implementation(libs.multiplatform.settings.coroutines)

            }
        }

        androidMain.dependencies {
            implementation(libs.androidx.biometric)
        }

        desktopMain.dependencies {
                implementation(libs.webauthn4j.core)

                implementation(libs.java.dev.jna)
                implementation(libs.java.dev.jna.jnaplatform)
                implementation(libs.java.dev.jna.platform)
        }
    }
}

val artifactId = "authenticator-biometrics"
val mavenGroup: String by project
val defaultVersion: String by project
val currentVersion = System.getenv("PACKAGE_VERSION") ?: defaultVersion
val desc: String by project
val license: String by project
val creationYear: String by project
val githubRepo: String by project


mavenPublishing {

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    coordinates(mavenGroup, artifactId, currentVersion)

    pom {
        name.set("Mifos Biometric Authenticator")
        description.set("Kotlin Multiplatform library that provides a unified API for biometric authentication (e.g., fingerprint, face ID) and device credentials (e.g., PIN, password) across Android, iOS, Desktop and Web platforms. It simplifies the process of integrating platform-specific authentication mechanisms into your application, allowing you to write a single codebase for user authentication.")
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
                name.set("MIfos Initiative")
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
