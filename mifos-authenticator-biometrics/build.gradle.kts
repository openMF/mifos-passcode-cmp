@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)

    alias(libs.plugins.jetbrains.kotlin.serialization)
}

group = "io.github.openmf"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    jvm("desktop") {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    wasmJs { browser() }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach {
        it.binaries.framework {
            baseName = "mifos-authenticator-biometrics"
            isStatic = true
        }
    }

    sourceSets {

        commonMain{
            resources.srcDir("src/commonMain/composeResources")

            dependencies {
                implementation(compose.components.resources)
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(libs.navigation.compose)

                implementation(libs.kotlinx.serialization.json)

                // For Preview
                implementation(compose.components.uiToolingPreview)

                // Material Icons
                implementation(libs.material3.icons)

                implementation(libs.multiplatform.settings.no.arg)
                implementation(libs.multiplatform.settings.serialization)
                implementation(libs.multiplatform.settings.coroutines)

                implementation(libs.kermit.logger)

            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {

            implementation(libs.androidx.activity.ktx)
            implementation(libs.androidx.activity.compose)

            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.biometric)
            implementation(libs.kotlinx.coroutines.android)
        }

        jsMain.dependencies {
            implementation(compose.ui)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(libs.kotlinx.coroutines.swing)

                implementation(libs.webauthn4j.core)

                implementation(libs.java.dev.jna)
                implementation(libs.java.dev.jna.jnaplatform)
                implementation(libs.java.dev.jna.platform)
            }
        }

        iosMain.dependencies {
            implementation(compose.ui)
        }

        wasmJsMain.dependencies {
            implementation(compose.ui)
        }
    }
}

android {
    namespace = "io.github.openmf"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}
