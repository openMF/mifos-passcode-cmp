package com.mifos.passcode.auth.passcode

import kotlinx.serialization.Serializable


@Serializable
data class MifosPasscode (
    val passcode: String,
    val passcodeLength: Int
)