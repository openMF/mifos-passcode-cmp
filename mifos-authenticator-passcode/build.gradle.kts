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

kotlin {

    android {
        namespace = "org.mifos.authenticator.passcode"
    }


    sourceSets {
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }

}

mavenPublishing {
    coordinates("com.example.mylibrary", "mylibrary-runtime", "1.0.3-SNAPSHOT")

    pom {
        name.set("Mifos Passcode Authenticator")
        description.set("Kotlin Multiplatform passcode authentication library providing UI and logic for passcode creation, verification, and management across Android, iOS, Desktop, and Web using Compose Multiplatform.")
        inceptionYear.set("2026")
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
                id.set("TheKalpeshPawar")
                name.set("Kalpesh Pawar")
                url.set("https://github.com/TheKalpeshPawar")
            }
        }

        scm {
            url.set("https://github.com/openMF/mifos-passcode-cmp")
            connection.set("scm:git:git://github.com/openMF/mifos-passcode-cmp.git")
            developerConnection.set("scm:git:ssh://git@github.com:openMF/mifos-passcode-cmp.git")
        }
    }
}
