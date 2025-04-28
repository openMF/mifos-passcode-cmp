package com.mifos.passcode.sample.navigation

import kotlinx.serialization.Serializable


sealed interface Route{
    @Serializable
    data object PasscodeScreen : Route

    @Serializable
    data object DeviceAuthScreen: Route

    @Serializable
    data object LoginScreen : Route

    @Serializable
    data object HomeScreen : Route

    @Serializable
    data object ChooseAuthOptionScreen: Route

}