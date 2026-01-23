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
    namespace = "com.mifos.authenticator.biometrics"
}

kotlin {

    sourceSets {

        commonMain {
            resources.srcDir("src/commonMain/composeResources")

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


