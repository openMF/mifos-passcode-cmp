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
        browser {
            val rootDirPath = project.rootDir.path
            val projectDirPath = project.projectDir.path
            commonWebpackConfig {
                outputFileName = "composeApp.js"
                devServer = (devServer ?: KotlinWebpackConfig.DevServer()).apply {
                    static(rootDirPath, true)
                    static(projectDirPath, true)
                }
            }
        }
        binaries.executable()
    }

    sourceSets {

        jsMain.dependencies {
            implementation(projects.cmpSampleShared)
            implementation(compose.html.core)
        }
        wasmJsMain.dependencies {
            implementation(projects.cmpSampleShared)
        }
    }
}
