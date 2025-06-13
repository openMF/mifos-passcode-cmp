package com.mifos.passcode.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import com.mifos.passcode.LibraryLocalAndroidActivity
import com.mifos.passcode.LibraryLocalCompositionProvider
import com.mifos.passcode.LibraryLocalContextProvider
import com.mifos.passcode.auth.PlatformAvailableAuthenticationOption
import com.mifos.passcode.auth.deviceAuth.PlatformAuthenticationProvider
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

    LibraryLocalCompositionProvider{

        MaterialTheme {

            val kmpDataStore = PreferenceDataStoreImpl()

            val platformAuthenticationProvider = PlatformAuthenticationProvider(
                LibraryLocalAndroidActivity.current
            )

            val platformAvailableAuthenticationOption = PlatformAvailableAuthenticationOption(
                LibraryLocalContextProvider.current
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

            SampleAppNavigation(
                passcodeRepository,
                chooseAuthOptionScreenViewmodel,
                platformAuthOptionScreenViewmodel
            )
        }
    }
}

