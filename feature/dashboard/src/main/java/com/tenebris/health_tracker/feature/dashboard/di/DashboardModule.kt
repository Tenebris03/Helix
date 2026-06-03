package com.tenebris.health_tracker.feature.dashboard.di

import com.tenebris.health_tracker.ui.coach.CoachViewModel
import com.tenebris.health_tracker.ui.dashboard.DashboardViewModel
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val dashboardModule =
    module {
        viewModel { DashboardViewModel(get(), get(), get(), get(), get(), androidApplication(), get()) }
        viewModel { CoachViewModel(get()) }
    }
