pluginManagement {
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

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "mifos-authenticator"

include(":core:designsystem")

include(":mifos-authenticator-biometrics")

include(":mifos-authenticator-passcode")

include(":sample:composeApp")
include(":cmp-sample-android")
include(":cmp-sample-shared")
include(":cmp-sample-desktop")
include(":cmp-sample-ios")
include(":cmp-sample-web")
