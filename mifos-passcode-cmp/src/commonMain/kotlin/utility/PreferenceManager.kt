package com.mifos.passcode.utility


import io.github.openmf.mifos_passcode_cmp.generated.resources.Res
import io.github.openmf.mifos_passcode_cmp.generated.resources.has_passcode
import io.github.openmf.mifos_passcode_cmp.generated.resources.passcode
import com.russhwolf.settings.Settings

/**
 * @author pratyush
 * @since 15/3/24
 */

class PreferenceManager( )
{
    private val settings : Settings by lazy {
        Settings()
    }

    var hasPasscode: Boolean
        get() = settings.getBoolean(Res.string.has_passcode.toString(), false)
        set(value) = settings.putBoolean(Res.string.has_passcode.toString(), value)

    fun savePasscode(passcode: String) {
        settings.putString(Res.string.passcode.toString(), passcode)
        hasPasscode = true
    }

    fun getSavedPasscode(): String {
        return settings.getString(Res.string.passcode.toString(), "")
    }
}