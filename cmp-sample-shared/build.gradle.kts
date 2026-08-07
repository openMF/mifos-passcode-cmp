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
    alias(libs.plugins.mifos.cmp.feature)
}


kotlin {

    android {
        namespace = "cmp.sample.shared"
    }


    sourceSets {
        commonMain.dependencies {

            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)

            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(projects.mifosAuthenticatorPasscode)
            implementation(projects.mifosAuthenticatorBiometrics)

        }
        androidMain.dependencies {
            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
        }

        desktopTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }

}
