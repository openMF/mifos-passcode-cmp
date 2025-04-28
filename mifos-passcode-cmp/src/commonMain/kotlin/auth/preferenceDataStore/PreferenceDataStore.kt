package auth.preferenceDataStore


interface PreferenceDataStore {

    fun putData(key: String, value: String)

    fun getSavedData(key: String, defaultValue: String): String

    fun clearData(key: String)

}