package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider


@Composable
actual fun LibraryLocalCompositionProvider(content: @Composable (() -> Unit)) {
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides null,
        LibraryLocalContextProvider provides null,
    ){
        content()
    }
}