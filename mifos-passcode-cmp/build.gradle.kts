import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrainsCompose)
}

group = "com.mifos"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_1_8)
        }
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
            implementation(libs.androidx.lifecycle.viewmodel.ktx)
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
            implementation (libs.androidx.biometric)
        }
    }
}

android {
    namespace = "com.mifos"
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

    coordinates("com.mifos", "mifos-passcode-cmp", "1.0.0")

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
