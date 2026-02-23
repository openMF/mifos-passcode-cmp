package cmp.sample.shared.di

import cmp.sample.shared.PasscodeStorageAdapterImpl
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionRepository
import cmp.sample.shared.chooseAuthOption.ChooseAuthOptionScreenViewmodel
import cmp.sample.shared.platformAuthentication.AuthenticationScreenViewModel
import com.russhwolf.settings.Settings
import kotlinx.coroutines.MainScope
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.bind
import org.koin.dsl.module
import org.mifos.authenticator.passcode.PasscodeManager
import org.mifos.authenticator.passcode.PasscodeStorageAdapter


val passcodeModule = module {
    singleOf(::PasscodeStorageAdapterImpl).bind<PasscodeStorageAdapter>()
    single {
        PasscodeManager(get(), MainScope()).initialize()
    }
    single { Settings() }
    single { ChooseAuthOptionRepository(get()) }
    viewModelOf(::ChooseAuthOptionScreenViewmodel)
    viewModelOf(::AuthenticationScreenViewModel)
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(passcodeModule)
    }
}