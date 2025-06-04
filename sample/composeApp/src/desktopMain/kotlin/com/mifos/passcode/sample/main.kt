package com.mifos.passcode.sample

import App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mifos.passcode.LocalPlatformAuthenticator
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator

fun main(vararg args: String) = application {

    System.setProperty(
        "jna.library.path",
        args[0]
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "mifos-passcode-sample",
    ) {
        App()
    }
}
