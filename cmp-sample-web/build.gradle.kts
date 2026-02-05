@file:OptIn(ExperimentalWasmDsl::class)

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpackConfig

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}

kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                outputFileName = "webJs.js"
            }
        }
        binaries.executable()
    }
    wasmJs {
        outputModuleName = "composeApp"
        browser()
        binaries.executable()
    }

    sourceSets {

        jsMain.dependencies {
            implementation(projects.cmpSampleShared)
            implementation(libs.html.core)
            implementation(libs.jb.composeRuntime)
            implementation(libs.jb.compose.ui)
            implementation(libs.foundation)
            implementation(libs.components.resources)
        }
        wasmJsMain.dependencies {
            implementation(projects.cmpSampleShared)
            implementation(libs.jb.composeRuntime)
            implementation(libs.jb.compose.ui)
            implementation(libs.foundation)
            implementation(libs.components.resources)
        }
    }
}
