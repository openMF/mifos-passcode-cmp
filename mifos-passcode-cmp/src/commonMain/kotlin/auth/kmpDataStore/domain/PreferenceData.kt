package com.mifos.passcode.auth.kmpDataStore.domain

import kotlinx.serialization.Serializable


@Serializable
data class PreferenceData(
    val value: String
) {
    companion object {
        val DEFAULT = PreferenceData( "")
    }
}