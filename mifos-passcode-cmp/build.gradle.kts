import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.vanniktech.mavenPublish)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.composeMultiplatform)

//    id("com.google.devtools.ksp") version "2.0.10-1.0.24"

    alias(libs.plugins.jetbrains.kotlin.serialization)
    alias(libs.plugins.protobuf)
}
apply(plugin = "kotlin-parcelize")

group = "io.github.openmf"
version = "1.0.0"

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            /*
                In Kotlin 2.0 and higher, aliasing annotations that trigger plugins is unsupported.
                To circumvent this, we provide a new Parcelize annotation as the "additionalAnnotation" parameter to the plugin instead.
            */
            freeCompilerArgs.addAll("-P", "plugin:org.jetbrains.kotlin.parcelize:additionalAnnotation=com.mifos.passcode.core.Parcelize")
            jvmTarget.set(JvmTarget.JVM_1_8)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    js(IR) {
        browser()
        binaries.executable()
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    jvm("desktop") {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
            kotlinOptions.freeCompilerArgs += "-Xexpect-actual-classes"
        }
    }

    wasm {
        browser()
        compilerOptions {
            freeCompilerArgs.add("-Xexpect-actual-classes")
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
        it.compilations.all {
            kotlinOptions {
                freeCompilerArgs +="-Xexpect-actual-classes"
            }
        }
    }
    
    sourceSets {

        val commonMain by getting {
            resources.srcDir("src/commonMain/composeResources")
        }

        sourceSets.named("commonMain").configure {
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }

        commonMain.dependencies {
            implementation(compose.components.resources)
            implementation(compose.ui)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.components.resources)
            implementation(libs.navigation.compose)

            api(libs.protobuf.kotlin.lite)

            implementation(libs.koin.compose.viewmodel)
            implementation(libs.koin.compose)
            implementation(libs.koin.core)

            api(libs.koin.annotations)

            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)

            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)

            //For Preview
            implementation(compose.components.uiToolingPreview)

            //Material Icons
            implementation(libs.material3.icons)

        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        androidMain.dependencies {
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation (libs.androidx.biometric)
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

//dependencies {
//    add("kspCommonMainMetadata", libs.koin.ksp.compiler)
//    add("kspAndroid", libs.koin.ksp.compiler)
//    add("kspIosX64", libs.koin.ksp.compiler)
//    add("kspIosArm64", libs.koin.ksp.compiler)
//    add("kspIosSimulatorArm64", libs.koin.ksp.compiler)
//}
//
//project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
//    if(name != "kspCommonMainKotlinMetadata") {
//        dependsOn("kspCommonMainKotlinMetadata")
//    }
//}


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
