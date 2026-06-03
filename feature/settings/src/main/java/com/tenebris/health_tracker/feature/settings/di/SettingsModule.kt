package com.tenebris.health_tracker.feature.settings.di

import com.tenebris.health_tracker.ui.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val settingsModule =
    module {
        viewModel { SettingsViewModel(get(), get(), get(), get(), get()) }
    }
