package ru.secon.data

import com.russhwolf.settings.Settings
import ru.secon.core.settings.NullableStringSettingConfig
import ru.secon.core.settings.SettingsRepository

class SettingsRepositoryImpl(private val settings: Settings) : SettingsRepository {

    override val token: NullableStringSettingConfig =
        NullableStringSettingConfig(settings, "token", "")

    override fun clear() = settings.clear()
}