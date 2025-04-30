@file:OptIn(ExperimentalSerializationApi::class, ExperimentalSettingsApi::class)


package com.mifos.passcode.auth.kmpDataStore.data.repository


import com.mifos.passcode.auth.kmpDataStore.domain.PreferencesDataSource
import com.russhwolf.settings.ExperimentalSettingsApi
import com.russhwolf.settings.Settings
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi


class PreferencesDataSourceImpl(
    private val settings: Settings,
    private val dispatcher: CoroutineDispatcher,
): PreferencesDataSource {
    override suspend fun getData(key: String): String = settings.getString(key, "")

    override suspend fun setData(key: String, data: String) {
        withContext(dispatcher) {
            settings.putString(key, data)
        }
    }

    override fun clearInfo(key: String) {
        settings.remove(key)
    }

}

//private fun Settings.putData(key: String, data: PreferenceData) {
//    encodeValue(
//        key = key,
//        serializer = PreferenceData.serializer(),
//        value = data,
//    )
//}