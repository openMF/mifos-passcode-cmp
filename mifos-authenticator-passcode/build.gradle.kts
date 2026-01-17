plugins {
    alias(libs.plugins.mifos.cmp.feature)
}

kotlin {

    android {
        namespace = "org.mifos.authenticator.passcode"
    }


    sourceSets {
        desktopMain.dependencies {
            implementation(compose.desktop.currentOs)
        }
    }

}
