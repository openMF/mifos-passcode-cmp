package com.mifos.passcode.sample

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main(vararg args: String) = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "mifos-passcode-sample",
    ) {
        App()
    }
}
