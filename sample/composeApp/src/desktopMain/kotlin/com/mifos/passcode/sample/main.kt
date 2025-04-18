package com.mifos.passcode.sample

import App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.mifos.passcode.sample.di.initKoin

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "mifos-passcode-sample",
    ) {
        initKoin()
        App()
    }
}