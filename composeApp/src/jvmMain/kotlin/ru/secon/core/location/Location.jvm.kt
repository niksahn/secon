package ru.secon.core.location

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.net.URL

actual class LocationProvider {
    private val ipApiUrl = "http://ip-api.com/json/"

    actual suspend fun getCurrentLocation(): LocationResult {
        return try {
            val response = URL(ipApiUrl).readText()
            val data = Json.decodeFromString<IpApiResponse>(response)

            if (data.status != "success") {
                throw LocationError.LocationUnavailable
            }

            LocationResult(
                latitude = data.lat,
                longitude = data.lon
            )
        } catch (e: Exception) {
            throw LocationError.ServiceError(e.message ?: "Failed to get location")
        }
    }

    @Serializable
    private data class IpApiResponse(
        val status: String,
        val lat: Double,
        val lon: Double,
        val country: String,
        val city: String
    )
}