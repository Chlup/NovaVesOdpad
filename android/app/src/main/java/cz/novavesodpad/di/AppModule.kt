package cz.novavesodpad.di

import cz.novavesodpad.service.NotificationsBuilder
import cz.novavesodpad.service.NotificationsBuilderImpl
import cz.novavesodpad.ui.home.AndroidAssetsProvider
import cz.novavesodpad.ui.home.AssetsProvider
import cz.novavesodpad.ui.home.HomeViewModel
import cz.novavesodpad.ui.settings.PreferencesManager
import cz.novavesodpad.ui.settings.SettingsViewModel
import cz.novavesodpad.ui.settings.SharedPreferencesManager
import cz.novavesodpad.ui.trashinfo.TrashInfoViewModel
import cz.novavesodpad.util.LogcatLogger
import cz.novavesodpad.util.Logger
import cz.novavesodpad.util.TasksManager
import cz.novavesodpad.util.TasksManagerImpl
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

/**
 * Koin dependency injection module
 */
val appModule = module {
    // Utils
    single<Logger> { LogcatLogger() }
    single<TasksManager> { TasksManagerImpl() }
    single<AssetsProvider> { 
        AndroidAssetsProvider(
            resourcesProvider = { androidContext().resources },
            packageName = androidContext().packageName
        ) 
    }
    
    // Notifications
    single<NotificationsBuilder> { NotificationsBuilderImpl(androidContext(), get()) }
    single<PreferencesManager> { SharedPreferencesManager(androidContext()) }
    
    // ViewModels
    viewModel { HomeViewModel(get(), get(), get()) }
    viewModel { SettingsViewModel(androidContext(), get(), get(), get(), get()) }
    viewModel { TrashInfoViewModel(androidContext()) }
}