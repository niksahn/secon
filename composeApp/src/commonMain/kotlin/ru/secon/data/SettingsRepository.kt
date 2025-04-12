package ru.secon.data

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import ru.secon.core.settings.NullableStringSettingConfig
import ru.secon.core.settings.SettingsRepository

class SettingsRepositoryImpl(
    private val settings: Settings,
    private val observableSettings: ObservableSettings
) : SettingsRepository {

    override val token: NullableStringSettingConfig =
        NullableStringSettingConfig(settings, observableSettings, "token")

    override fun clear() = settings.clear()
}