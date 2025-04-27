package auth.preferenceDataStore

import com.russhwolf.settings.Settings


class PreferenceDataStoreImpl: PreferenceDataStore {
    private val settings : Settings by lazy {
        Settings()
    }

    override fun putData(
        key: String,
        value: String
    ) {
        settings.putString(key,value)
    }

    override fun getSavedData(
        key: String,
        defaultValue: String
    ) = settings.getString(key, defaultValue )


    override fun clearData(key: String) {
        settings.remove(key)

    }
}