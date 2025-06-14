package com.mifos.passcode.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.mifos.passcode.LibraryLocalCompositionProvider
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionRepository
import com.mifos.passcode.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import com.mifos.passcode.sample.kmpDataStore.PreferenceDataStoreImpl
import com.mifos.passcode.sample.navigation.SampleAppNavigation
import com.mifos.passcode.sample.passcode.PasscodeRepository
import com.mifos.passcode.sample.platformAuthentication.PlatformAuthenticationScreenViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {

    LibraryLocalCompositionProvider{

        MaterialTheme {

            val kmpDataStore = PreferenceDataStoreImpl()

            val chooseAuthOptionRepository = ChooseAuthOptionRepository(kmpDataStore)
            val chooseAuthOptionScreenViewmodel = ChooseAuthOptionScreenViewmodel(
                chooseAuthOptionRepository,
            )

            val platformAuthOptionScreenViewmodel = PlatformAuthenticationScreenViewModel(
                chooseAuthOptionRepository = chooseAuthOptionRepository,
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

