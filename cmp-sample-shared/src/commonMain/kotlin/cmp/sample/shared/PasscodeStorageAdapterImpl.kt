package cmp.sample.shared

import com.russhwolf.settings.Settings
import org.mifos.authenticator.passcode.PasscodeStorageAdapter


const val PASSCODE_KEY = "org.mifos.authenticator.passcode"

class PasscodeStorageAdapterImpl(
    private val settings: Settings
): PasscodeStorageAdapter {
    override fun savePasscode(passcode: String) {
        settings.putString(PASSCODE_KEY, passcode)
    }

    override fun loadPasscode(): String? {
        return settings.getStringOrNull(PASSCODE_KEY)
    }

    override fun deletePasscode() {
        settings.remove(PASSCODE_KEY)
    }

}