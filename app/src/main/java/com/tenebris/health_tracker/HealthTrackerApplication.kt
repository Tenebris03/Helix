package com.tenebris.health_tracker

import android.app.Application
import com.tenebris.health_tracker.di.appModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class HealthTrackerApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@HealthTrackerApplication)
            modules(appModule)
        }
    }
}
