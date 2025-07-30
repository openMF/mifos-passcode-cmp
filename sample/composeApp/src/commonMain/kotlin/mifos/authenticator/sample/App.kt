package mifos.authenticator.sample

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import mifos.authenticator.biometrics.LibraryLocalCompositionProvider
import mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionRepository
import mifos.authenticator.sample.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import mifos.authenticator.sample.kmpDataStore.PreferenceDataStoreImpl
import mifos.authenticator.sample.navigation.SampleAppNavigation
import mifos.authenticator.sample.passcode.PasscodeRepository
import mifos.authenticator.sample.platformAuthentication.AuthenticationScreenViewModel
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

            val platformAuthOptionScreenViewmodel = AuthenticationScreenViewModel(
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

