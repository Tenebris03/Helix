package com.tenebris.health_tracker

import android.app.Application
import com.tenebris.health_tracker.core.data.di.coreDataModule
import com.tenebris.health_tracker.feature.dashboard.di.dashboardModule
import com.tenebris.health_tracker.feature.onboarding.di.onboardingModule
import com.tenebris.health_tracker.feature.settings.di.settingsModule
import com.tenebris.health_tracker.feature.tracking.di.trackingModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HealthTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HealthTrackerApplication)
            modules(
                coreDataModule,
                dashboardModule,
                onboardingModule,
                trackingModule,
                settingsModule,
            )
        }
    }
}
