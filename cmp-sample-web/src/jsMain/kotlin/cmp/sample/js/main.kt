package cmp.sample.js

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import cmp.sample.shared.App
import cmp.sample.shared.di.initKoin
import kotlinx.browser.document
import org.jetbrains.skiko.wasm.onWasmReady

@OptIn(ExperimentalComposeUiApi::class)
fun main(){
    initKoin()
    onWasmReady {
        ComposeViewport(document.body!!) {
            App()
        }
    }
}