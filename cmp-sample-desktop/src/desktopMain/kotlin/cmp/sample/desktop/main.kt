package cmp.sample.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cmp.sample.shared.App

fun main(vararg args: String) = application {

    Window(
        onCloseRequest = ::exitApplication,
        title = "mifos-passcode-sample",
    ) {
        App()
    }
}
