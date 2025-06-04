import com.vanniktech.maven.publish.SonatypeHost
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
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
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
            freeCompilerArgs.add("-Xexpect-actual-classes")
        }
    }

    js(IR) {
        browser()
        binaries.executable()
    }

    jvm("desktop") {
        compilerOptions{
            jvmTarget.set(JvmTarget.JVM_21)        }
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


            implementation(libs.navigation.compose)
            implementation(libs.kotlinx.serialization.json)

            //For Preview
            implementation(compose.components.uiToolingPreview)

            //Material Icons
            implementation(libs.material3.icons)

            implementation(libs.multiplatform.settings.no.arg)
            implementation(libs.multiplatform.settings.serialization)
            implementation(libs.multiplatform.settings.coroutines)

            //Cryptography
            implementation("dev.whyoleg.cryptography:cryptography-core:0.4.0")

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

                implementation("com.webauthn4j:webauthn4j-core:0.29.2.RELEASE")
                implementation("com.webauthn4j:webauthn4j-core-async:0.29.2.RELEASE")

                implementation("net.java.dev.jna:jna:5.17.0")
                implementation("net.java.dev.jna:jna-platform:5.17.0")
                implementation("net.java.dev.jna:platform:3.5.2")

                implementation("org.slf4j:slf4j-simple:2.0.13")

                //Cryptography
                implementation("dev.whyoleg.cryptography:cryptography-provider-jdk:0.4.0")
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

                //Cryptography
                implementation("dev.whyoleg.cryptography:cryptography-provider-webcrypto:0.4.0")
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

compose.desktop{
    application {
        mainClass = "com.mifos.passcode.auth.deviceAuth.PlatformAuthenticatorKt" // Or your actual mainKt class

        jvmArgs += listOf( // Use += to add to existing list, or = if it's the only one
            "-Djna.library.path=${project.projectDir}/nativeC", // Make sure this path is correct
            "-Djna.debug_load=true" // Add this for detailed JNA loading logs!
        )
    }
}

tasks.withType(JavaExec::class.java){
    args("$projectDir/nativeC")
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
