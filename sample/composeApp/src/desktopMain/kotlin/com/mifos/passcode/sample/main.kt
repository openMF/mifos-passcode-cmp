package com.mifos.passcode.sample

import App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "mifos-passcode-sample",
    ) {
        App()
    }
}