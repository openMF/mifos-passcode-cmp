plugins {
    alias(libs.plugins.androidApplication)
    id("org.jetbrains.kotlin.android") version "2.0.20"
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.20"
}

android {
    namespace = "com.example.sample"
    compileSdk = 34

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }
    
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}


dependencies {
    implementation(libs.appcompat.v7)

    testImplementation(libs.junit)
    androidTestImplementation(libs.test.runner)
    androidTestImplementation(libs.espresso.core)

    implementation(libs.androidx.core.ktx.v1120)
    implementation(libs.androidx.appcompat.v161)

    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.activity.compose.v182)

    implementation(platform(libs.androidx.compose.bom.v20240202))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(libs.androidx.lifecycle.runtime.ktx.v262)

    implementation(project(":mifos-passcode-cmp"))
}