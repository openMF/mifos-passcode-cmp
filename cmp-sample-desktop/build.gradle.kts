plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.jetbrains.kotlin.serialization)
}


kotlin {

    jvm("desktop")
    jvmToolchain(17)
    sourceSets {

        val desktopMain by getting {
            dependencies {
                implementation(projects.cmpSampleShared)

                implementation(libs.kotlinx.coroutines.swing)
                implementation(compose.desktop.currentOs)
            }
        }
    }

}