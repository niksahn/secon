package ru.secon.core.settings

import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SettingsListener
import kotlinx.coroutines.flow.MutableStateFlow

interface SettingsRepository {
    val token: NullableStringSettingConfig
    fun clear()
}

class NullableStringSettingConfig(
    settings: Settings,
    private val observableSettings: ObservableSettings,
    key: String,
) : SettingConfig<String>(settings, key) {

    val settingsFlow = MutableStateFlow(getValue())

    init {
        observableSettings.putString(key, getValue()?:"")
        observableSettings.addStringOrNullListener(
            key,
            { settingsFlow.tryEmit(it) })
    }

    override fun getValue(): String? =
        settings.getStringOrNull(key)

    override fun setValue(value: String) {
        settings.putString(key, value)
        observableSettings.putString(key, value)
    }

    override fun remove() {
        observableSettings.remove(key)
        settings.remove(key)
    }

}


sealed class SettingConfig<T>(
    protected val settings: Settings,
    protected val key: String,
) {
    abstract fun getValue(): T?
    abstract fun setValue(value: T)

    private var listener: SettingsListener? = null

    abstract fun remove(): Unit
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