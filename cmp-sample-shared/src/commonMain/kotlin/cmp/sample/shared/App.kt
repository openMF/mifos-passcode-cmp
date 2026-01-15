package cmp.sample.shared

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionRepository
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import cmp.sample.shared.kmpDataStore.PreferenceDataStoreImpl
import cmp.sample.shared.navigation.SampleAppNavigation
import cmp.sample.shared.passcode.PasscodeRepository
import cmp.sample.shared.platformAuthentication.AuthenticationScreenViewModel
import org.mifos.authenticator.biometrics.LibraryLocalCompositionProvider


@Composable
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

