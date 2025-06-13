package com.mifos.passcode

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf


val LibraryLocalAndroidActivity: ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
val LibraryLocalContextProvider:ProvidableCompositionLocal<Any?> = compositionLocalOf { null }

@Composable
expect fun LibraryLocalCompositionProvider(
    content: @Composable () -> Unit,
)