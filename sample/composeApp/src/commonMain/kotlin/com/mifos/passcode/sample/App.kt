package com.mifos.passcode.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticator
import com.mifos.passcode.sample.passcode.PasscodeRepository
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import com.mifos.passcode.sample.platformAuthentication.PlatformAuthenticationScreenViewModel
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import com.mifos.passcode.sample.navigation.SampleAppNavigation
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App() {

    val kmpDataStore = PreferenceDataStoreImpl()
    val platformAuthenticator = PlatformAuthenticator(LocalAndroidActivity.current)

    val platformAuthenticationProvider = PlatformAuthenticationProvider(platformAuthenticator)

    val platformAvailableAuthenticationOption = PlatformAvailableAuthenticationOption(
        LocalContextProvider.current
    )
    val chooseAuthOptionRepository = ChooseAuthOptionRepository(kmpDataStore)
    val chooseAuthOptionScreenViewmodel = ChooseAuthOptionScreenViewmodel(
        chooseAuthOptionRepository,
        platformAuthenticationProvider = platformAuthenticationProvider,
        platformAvailableAuthenticationOption = platformAvailableAuthenticationOption
    )

    val platformAuthOptionScreenViewmodel = PlatformAuthenticationScreenViewModel(
        platformAuthenticationProvider = platformAuthenticationProvider,
        chooseAuthOptionRepository = chooseAuthOptionRepository,
        platformAvailableAuthenticationOption = platformAvailableAuthenticationOption,
        preferenceDataStore = kmpDataStore
    )

    val passcodeRepository = PasscodeRepository(kmpDataStore)

    MaterialTheme {
        SampleAppNavigation(
            passcodeRepository,
            chooseAuthOptionScreenViewmodel,
            platformAuthOptionScreenViewmodel
        )
    }
}

