package com.tenebris.health_tracker.feature.tracking.di

import com.tenebris.health_tracker.ui.progress.ProgressViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val trackingModule =
    module {
        viewModel { ProgressViewModel(get()) }
    }
