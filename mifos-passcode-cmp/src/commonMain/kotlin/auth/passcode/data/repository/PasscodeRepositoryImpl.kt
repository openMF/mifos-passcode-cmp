package com.mifos.passcode.auth.passcode.data.repository

import com.mifos.passcode.auth.kmpDataStore.domain.PreferencesDataSource
import com.mifos.passcode.auth.passcode.domain.PasscodeRepository

private const val PASSCODE_INFO_KEY = "passcodeInfo"

class PasscodeRepositoryImpl (
    private val source: PreferencesDataSource,
) : PasscodeRepository {

    override suspend fun getPasscode(): String {
        return source.getData(PASSCODE_INFO_KEY)
    }

    override suspend fun savePasscode(passcode: String) {
        source.setData(
            PASSCODE_INFO_KEY,
            passcode
        )
    }

    override suspend fun clearPasscode() {
        source.clearInfo(PASSCODE_INFO_KEY)
    }

    override suspend fun isPasscodeSet(): Boolean = getPasscode().isNotBlank()

}