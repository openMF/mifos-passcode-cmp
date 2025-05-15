package com.mifos.passcode.sample

import App
import androidx.compose.ui.window.ComposeUIViewController
import com.mifos.passcode.sample.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}