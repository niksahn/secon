package ru.secon

enum class Platform {
    Android, Pc;
}

object CurrentPlatform {
    val current: Platform
        get() = getCurrentPlatform()
}

expect fun getCurrentPlatform(): Platform