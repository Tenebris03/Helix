package com.tenebris.health_tracker.feature.onboarding.di

import com.tenebris.health_tracker.ui.onboarding.OnboardingViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val onboardingModule =
    module {
        viewModel { OnboardingViewModel(get(), get(), get()) }
    }
