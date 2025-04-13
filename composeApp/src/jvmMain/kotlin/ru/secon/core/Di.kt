package ru.secon.core

import org.koin.dsl.module
import ru.secon.core.location.LocationProvider
import ru.secon.core.location.LocationService

val domainJvmModule = module {

    single<LocationService> {
        val locationProvider = LocationProvider()
        LocationService(locationProvider)
    }
}