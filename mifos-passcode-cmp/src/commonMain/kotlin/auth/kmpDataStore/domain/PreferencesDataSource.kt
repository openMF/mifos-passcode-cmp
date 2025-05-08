package com.mifos.passcode.auth.kmpDataStore.domain

interface PreferencesDataSource {

    suspend fun getData(key: String): String

    suspend fun setData(key: String, data: String)

    fun clearInfo(key: String)

}