package cmp.sample.desktop

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import cmp.sample.shared.App
import cmp.sample.shared.di.initKoin

fun main(vararg args: String) = application {

    initKoin()

    Window(
        onCloseRequest = ::exitApplication,
        title = "mifos-passcode-sample",
    ) {
        App()
    }
}
