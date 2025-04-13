package ru.secon.ui

import org.koin.dsl.module
import ru.secon.core.file.FileDownloader
import ru.secon.core.file.FileSaver
import ru.secon.core.location.LocationProvider
import ru.secon.core.location.LocationService
import ru.secon.data.FileStorageRepository
import ru.secon.ui.camera.CameraViewModel
import ru.secon.ui.map.MapViewModel


val uiAndroidModule = module {
    factory {
        CameraViewModel(get(), get())
    }
    factory {
        MapViewModel(get())
    }

    single<LocationService> {
        val locationProvider = LocationProvider(get())
        LocationService(locationProvider)
    }

    single<FileSaver> {
        FileSaver().apply { context = get() }
    }
}

val domainAndroidModule = module {
    single<FileStorageRepository> { FileStorageRepository() }
}