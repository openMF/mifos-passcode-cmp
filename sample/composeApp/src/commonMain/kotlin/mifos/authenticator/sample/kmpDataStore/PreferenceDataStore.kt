package mifos.authenticator.sample.kmpDataStore

interface PreferenceDataStore {

    fun <T>putData(key: String, value: T)

    fun <T>getSavedData(key: String, defaultValue: T): T

    fun clearData(key: String)
}