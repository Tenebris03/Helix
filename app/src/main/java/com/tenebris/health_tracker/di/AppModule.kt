package com.tenebris.health_tracker.di

import com.tenebris.health_tracker.BuildConfig
import com.tenebris.health_tracker.data.Constants
import com.tenebris.health_tracker.data.local.AppDatabase
import com.tenebris.health_tracker.data.local.CachedProductDao
import com.tenebris.health_tracker.data.local.FoodDao
import com.tenebris.health_tracker.data.local.ProfileDao
import com.tenebris.health_tracker.data.local.WeightDao
import com.tenebris.health_tracker.data.pref.EncryptedStorageManager
import com.tenebris.health_tracker.data.pref.UserPreferences
import com.tenebris.health_tracker.data.remote.FoodGatekeeper
import com.tenebris.health_tracker.data.remote.FoodVisionService
import com.tenebris.health_tracker.data.remote.OpenFoodFactsApi
import com.tenebris.health_tracker.data.repository.CoachRepository
import com.tenebris.health_tracker.data.repository.FoodRepository
import com.tenebris.health_tracker.data.repository.VisionRepository
import com.tenebris.health_tracker.data.service.CalendarContextResolver
import com.tenebris.health_tracker.data.service.DistilBertFoodClassifier
import com.tenebris.health_tracker.data.service.FoodProblemDetector
import com.tenebris.health_tracker.data.service.HeuristicFoodProblemDetector
import com.tenebris.health_tracker.data.service.TrendAnalyzer
import com.tenebris.health_tracker.data.service.WeatherService
import com.tenebris.health_tracker.ui.coach.CoachViewModel
import com.tenebris.health_tracker.ui.dashboard.DashboardViewModel
import com.tenebris.health_tracker.ui.onboarding.OnboardingViewModel
import com.tenebris.health_tracker.ui.progress.ProgressViewModel
import com.tenebris.health_tracker.ui.settings.SettingsViewModel
import com.google.ai.client.generativeai.GenerativeModel
import com.tenebris.health_tracker.data.service.LocationProvider
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidApplication
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json

val appModule = module {

    // Database
    single { AppDatabase.getDatabase(get()) }
    single<FoodDao> { get<AppDatabase>().foodDao() }
    single<WeightDao> { get<AppDatabase>().weightDao() }
    single<ProfileDao> { get<AppDatabase>().profileDao() }
    single<CachedProductDao> { get<AppDatabase>().cachedProductDao() }

    // Preferences
    single { UserPreferences(get()) }
    single { EncryptedStorageManager(get()) }

    // Network
    single {
        val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
        OkHttpClient.Builder()
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .header("User-Agent", "HealthTrackerApp - Android - Version 1.0 - https://github.com/tenebris/healthtracker")
                    .build()
                chain.proceed(request)
            }
            .build()
    }

    single {
        val json = Json { ignoreUnknownKeys = true }
        Retrofit.Builder()
            .baseUrl(OpenFoodFactsApi.BASE_URL)
            .client(get())
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(OpenFoodFactsApi::class.java)
    }

    // Services
    single { FoodGatekeeper() }
    single<FoodProblemDetector> { DistilBertFoodClassifier(HeuristicFoodProblemDetector()) }

    single {
        GenerativeModel(
            modelName = Constants.GEMINI_MODEL,
            apiKey = BuildConfig.GEMINI_API_KEY,
        )
    }

    single { FoodVisionService(get()) }

    // Repositories
    single { FoodRepository(get(), get(), get()) }
    single { VisionRepository(get(), get(), get()) }

    // Workers depend on these
    single { TrendAnalyzer(get(), get()) }
    single { CalendarContextResolver(get()) }
    single { WeatherService() }
    single { LocationProvider(get()) }
    single { CoachRepository(get(), get(), get()) }

    // ViewModels
    viewModel { DashboardViewModel(get(), get(), get(), get(), get(), androidApplication(), get()) }
    viewModel { OnboardingViewModel(get(), get(), get()) }
    viewModel { ProgressViewModel(get()) }
    viewModel { SettingsViewModel(get(), get(), get(), get()) }
    viewModel { CoachViewModel(get()) }
}
