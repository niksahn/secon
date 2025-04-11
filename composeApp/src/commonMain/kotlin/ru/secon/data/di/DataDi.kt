package ru.secon.data.di

import com.russhwolf.settings.Settings
import org.koin.dsl.module
import ru.secon.core.network.NetworkService
import ru.secon.core.settings.SettingsRepository
import ru.secon.data.SettingsRepositoryImpl
import ru.secon.domain.AuthService

val dataModule = module {
    single<NetworkService> { NetworkService() }
    single<Settings> { Settings() }
    single<AuthService> { AuthService(get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
}