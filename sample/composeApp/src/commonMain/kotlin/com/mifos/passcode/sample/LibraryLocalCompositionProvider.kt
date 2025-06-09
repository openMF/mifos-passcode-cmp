package com.mifos.passcode.sample

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider


val LibraryLocalAndroidActivity:ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
val LibraryLocalContextProvider:ProvidableCompositionLocal<Any?> = compositionLocalOf { null }
@Composable
fun LocalCompositionProvider(
    activity: Any? = LibraryLocalAndroidActivity.current,
    context: Any? = LibraryLocalContextProvider.current,
    content: @Composable () -> Unit,
) {
    CompositionLocalProvider(
        LibraryLocalAndroidActivity provides activity,
        LibraryLocalContextProvider provides context,
        content = content,
    )
}


//val LibraryLocalPlatformAuthenticatorProvider = staticCompositionLocalOf<PlatformAuthenticationProvider> {
//    error("No PlatformAuthenticationProvider provided")
//}