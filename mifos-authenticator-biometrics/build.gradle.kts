plugins {
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.mifos.cmp.feature)

}


android {
    namespace = "com.mifos.authenticator.biometrics"
}

kotlin {

    sourceSets {

        commonMain{
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

        val desktopMain by getting {
            dependencies {
                implementation(libs.webauthn4j.core)

                implementation(libs.java.dev.jna)
                implementation(libs.java.dev.jna.jnaplatform)
                implementation(libs.java.dev.jna.platform)
            }
        }
    }
}


