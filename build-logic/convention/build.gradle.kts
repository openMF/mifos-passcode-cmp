import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `kotlin-dsl`
}

group = "com.mifos.buildlogic"

// Configure the build-logic plugins to target JDK 17
// This matches the JDK used to build the project, and is not related to what is running on device.
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
tasks.withType<KotlinCompile>().configureEach {
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.android.tools.common)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.compose.gradlePlugin)
    compileOnly(libs.detekt.gradlePlugin)
    compileOnly(libs.spotless.gradlePlugin)
    compileOnly(libs.ktlint.gradlePlugin)
}

tasks {
    validatePlugins {
        enableStricterValidation = true
        failOnWarning = false
    }
}

gradlePlugin {
    plugins {
        register("androidApplicationCompose") {
            id = "mifos.android.application.compose"
            implementationClass = "AndroidApplicationComposeConventionPlugin"
        }
        register("androidApplication") {
            id = "mifos.android.application"
            implementationClass = "AndroidApplicationConventionPlugin"
        }
        register("androidLint") {
            id = "mifos.android.lint"
            implementationClass = "AndroidLintConventionPlugin"
        }
        register("detekt") {
            id = "mifos.detekt.plugin"
            implementationClass = "MifosDetektConventionPlugin"
            description = "Configures detekt for the project"
        }
        register("spotless") {
            id = "mifos.spotless.plugin"
            implementationClass = "MifosSpotlessConventionPlugin"
            description = "Configures spotless for the project"
        }
        register("ktlint") {
            id = "mifos.ktlint.plugin"
            implementationClass = "MifosKtlintConventionPlugin"
            description = "Configures kotlinter for the project"
        }
        register("kmpLibrary") {
            id = "mifos.kmp.library"
            implementationClass = "KMPLibraryConventionPlugin"
        }
    }
}
