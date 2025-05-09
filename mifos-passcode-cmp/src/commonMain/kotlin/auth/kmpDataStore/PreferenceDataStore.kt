package com.mifos.passcode.auth.kmpDataStore

interface PreferenceDataStore {

    fun putData(key: String, value: String)

    fun getSavedData(key: String, defaultValue: String): String

    fun clearData(key: String)

}