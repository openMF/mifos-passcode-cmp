package org.mifos.authenticator.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import org.mifos.authenticator.biometrics.LibraryLocalCompositionProvider
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionRepository
import org.mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import org.mifos.authenticator.sample.kmpDataStore.PreferenceDataStoreImpl
import org.mifos.authenticator.sample.navigation.SampleAppNavigation
import org.mifos.authenticator.sample.passcode.PasscodeRepository
import org.mifos.authenticator.sample.platformAuthentication.AuthenticationScreenViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
@Preview
fun App() {

    LibraryLocalCompositionProvider{
        MaterialTheme {
            val kmpDataStore = PreferenceDataStoreImpl()

            val chooseAuthOptionRepository = ChooseAuthOptionRepository(kmpDataStore)

            val passcodeRepository = PasscodeRepository(kmpDataStore)

            SampleAppNavigation(
                passcodeRepository,
                chooseAuthOptionRepository,
                preferenceDataStore = kmpDataStore
            )
        }
    }
}

