package ru.secon.core.settings

import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener

interface SettingsRepository {
    val token: NullableStringSettingConfig
    fun clear()
}

class NullableStringSettingConfig(
    settings: Settings,
    key: String,
    defaultValue: String
) : SettingConfig<String>(settings, key, defaultValue) {

    override fun getValue(): String? =
        settings.getStringOrNull(key)

    override fun setValue(value: String) =
        settings.putString(key, value)
}


sealed class SettingConfig<T>(
    val settings: Settings,
    val key: String,
    private val defaultValue: T
) {
    protected abstract fun getValue(): T?
    protected abstract fun setValue(value: T)

    private var listener: SettingsListener? = null

    fun remove() = settings.remove(key)
    fun exists(): Boolean = settings.hasKey(key)

    fun set(value: T): Boolean {
        return try {
            setValue(value)
            true
        } catch (exception: Exception) {
            false
        }
    }

    override fun toString() = key
}