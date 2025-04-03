import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)
}

group = "io.github.openmf"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
    }

    wasm {
        browser()
    }

    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach{
        it.binaries.framework { 
            baseName = "mifos-passcode-cmp"
            isStatic = true
        }
    }
    
    sourceSets {

        val commonMain by getting {
            resources.srcDir("src/commonMain/composeResources")
        }

        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.navigation.compose)
            implementation(libs.multiplatform.settings.no.arg)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation (libs.androidx.biometric)
        }

        jsMain.dependencies {
            implementation(compose.ui)
        }

        val desktopMain by getting {
            dependencies {
                implementation(compose.ui)
                implementation(compose.runtime)
                implementation(compose.foundation)
            }
        }

        val iosMain by creating {
            dependsOn(commonMain)
        }

        val iosTest by creating {
            dependsOn(commonTest.get())
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        val iosX64Test by getting { dependsOn(iosTest) }
        val iosArm64Test by getting { dependsOn(iosTest) }
        val iosSimulatorArm64Test by getting { dependsOn(iosTest) }

        val wasmJsMain by getting {
            dependencies {
                implementation(compose.ui)
            }
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

mavenPublishing {
    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)

    signAllPublications()

    coordinates("io.github.openmf", "mifos-passcode-cmp", "1.0.4")

    pom {
        name = "CMP Mifos Passcode Library"
        description = "A library providing secure passcode management for Mifos applications."
        inceptionYear = "2024"
        url = "https://github.com/openMF/mifos-passcode-cmp"
        licenses {
            license {
                name = "Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.html"
                distribution = "repo"
            }
        }
        developers {
            developer {
                id = "openmf"
                name = "Mifos Initiative"
                url = "https://mifos.org"
            }
        }
        scm {
            url = "https://github.com/openMF/mifos-passcode-cmp"
            connection = "scm:git:git://github.com/openMF/mifos-passcode-cmp.git"
            developerConnection = "scm:git:ssh://git@github.com:openMF/mifos-passcode-cmp.git"
        }
    }
}
