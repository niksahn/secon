package ru.secon.data.di

import com.russhwolf.settings.MapSettings
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import org.koin.dsl.module
import ru.secon.core.network.NetworkService
import ru.secon.core.settings.SettingsRepository
import ru.secon.core.utils.InAppNotificationService
import ru.secon.data.SettingsRepositoryImpl
import ru.secon.domain.AuthService

val dataModule = module {
    single<NetworkService> { NetworkService(get()) }
    single<Settings> { Settings() }
    single<ObservableSettings> { MapSettings() }
    single<InAppNotificationService> { InAppNotificationService() }
    single<AuthService> { AuthService(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get()) }
}