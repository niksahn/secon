package ru.secon.core.location

expect class LocationProvider {
    suspend fun getCurrentLocation(): LocationResult
}

class LocationService(private val provider: LocationProvider) {
    suspend fun getCoordinates(): Pair<Double, Double> {
        val location = provider.getCurrentLocation()
        return location.latitude to location.longitude
    }
}

data class LocationResult(
    val latitude: Double,
    val longitude: Double,
    val accuracy: Float? = null,
    val altitude: Double? = null,
    val timestamp: Long? = null
)

sealed class LocationError : Exception() {
    object PermissionDenied : LocationError()
    object LocationUnavailable : LocationError()
    data class ServiceError(override val message: String) : LocationError()
}