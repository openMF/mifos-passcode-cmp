pluginManagement {
    includeBuild("build-logic")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.PREFER_PROJECT
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

//plugins {
//    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
//}


enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "mifos-authenticator"


include(":mifos-authenticator-biometrics")

include(":mifos-authenticator-passcode")


include(":cmp-sample-android")
include(":cmp-sample-shared")
include(":cmp-sample-desktop")
include(":cmp-sample-ios")
include(":cmp-sample-web")

include(":gradle-l10n-plugin")
