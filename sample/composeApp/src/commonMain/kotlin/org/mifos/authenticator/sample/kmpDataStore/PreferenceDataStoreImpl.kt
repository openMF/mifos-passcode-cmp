package org.mifos.authenticator.sample.kmpDataStore

import com.russhwolf.settings.Settings

class PreferenceDataStoreImpl : PreferenceDataStore {
    private val settings: Settings by lazy {
        Settings()
    }

    override fun <T>putData(
        key: String,
        value: T
    ) {
        if (value is String) {
            settings.putString(key, value)
        } else {
            settings.putInt(key, value as Int)
        }
    }

    override fun < T>getSavedData(
        key: String,
        defaultValue: T
    ): T {
        return if(defaultValue is String){
            settings.getString(key, defaultValue) as T
        } else {
            settings.getInt(key, defaultValue as Int) as T
        }
    }

    override fun clearData(key: String) {
        settings.remove(key)
    }
}
