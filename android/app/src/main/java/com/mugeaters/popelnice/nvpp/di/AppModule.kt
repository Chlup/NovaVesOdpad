package com.mugeaters.popelnice.nvpp.di

import com.mugeaters.popelnice.nvpp.service.NotificationsBuilder
import com.mugeaters.popelnice.nvpp.service.NotificationsBuilderImpl
import com.mugeaters.popelnice.nvpp.ui.dayslist.DaysListViewModel
import com.mugeaters.popelnice.nvpp.ui.home.HomeViewModel
import com.mugeaters.popelnice.nvpp.ui.settings.PreferencesManager
import com.mugeaters.popelnice.nvpp.ui.settings.SettingsViewModel
import com.mugeaters.popelnice.nvpp.ui.settings.SharedPreferencesManager
import com.mugeaters.popelnice.nvpp.ui.trashinfo.TrashInfoViewModel
import com.mugeaters.popelnice.nvpp.util.LogcatLogger
import com.mugeaters.popelnice.nvpp.util.Logger
import com.mugeaters.popelnice.nvpp.util.TasksManager
import com.mugeaters.popelnice.nvpp.util.TasksManagerImpl
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
    
    // Notifications
    single<NotificationsBuilder> { NotificationsBuilderImpl(androidContext(), get()) }
    single<PreferencesManager> { SharedPreferencesManager(androidContext()) }
    
    // ViewModels
    viewModel { HomeViewModel(get(), get()) }
    viewModel { SettingsViewModel(androidContext(), get(), get(), get(), get()) }
    viewModel { (sections: List<com.mugeaters.popelnice.nvpp.model.TrashInfoSection>) -> 
        TrashInfoViewModel(sections)
    }
    viewModel { (days: List<com.mugeaters.popelnice.nvpp.model.TrashDay>) -> 
        DaysListViewModel(days, get(), get())
    }
}