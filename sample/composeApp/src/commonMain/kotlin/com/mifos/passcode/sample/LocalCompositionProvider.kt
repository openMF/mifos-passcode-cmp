package com.mifos.passcode.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf


val LocalAndroidActivity:ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
val LocalContextProvider:ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
@Composable
fun LocalCompositionProvider(
    activity: Any? = LocalAndroidActivity.current,
    context: Any? = LocalContextProvider.current,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LocalAndroidActivity provides activity,
        LocalContextProvider provides context,
        content = content,
    )
}
