plugins {
    alias(libs.plugins.mifos.cmp.feature)
}


kotlin {

    android {
        namespace = "cmp.sample.shared"
    }


    sourceSets {

        commonMain.dependencies {
            implementation(projects.mifosAuthenticatorBiometrics)
            implementation(projects.mifosAuthenticatorPasscode)

            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)
        }
    }

}