package cmp.sample.ios

import androidx.compose.ui.window.ComposeUIViewController
import cmp.sample.shared.App
import cmp.sample.shared.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}