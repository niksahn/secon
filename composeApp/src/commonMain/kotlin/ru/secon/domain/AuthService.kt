package ru.secon.domain

import kotlinx.serialization.Serializable
import ru.secon.core.monads.Either
import ru.secon.core.monads.Operation
import ru.secon.core.network.NetworkEndpointScope
import ru.secon.core.network.NetworkResponse
import ru.secon.core.network.NetworkService
import ru.secon.core.settings.SettingsRepository

class AuthService(
    private val settingsRepository: SettingsRepository,
    private val networkService: NetworkService
) {
    fun logout() {
        settingsRepository.token.remove()
    }

//    suspend fun login(
//        key: AuthApi.AuthBody
//    ): NetworkResponse<String> =
//        networkService.request(AuthApi.login(key))
//            .also { if (it is Either.Right) settingsRepository.token.set(it.right) }
//
    fun auth() {

    }

    suspend fun login(
        key: AuthApi.AuthBody
    ): NetworkResponse<String> = Either.Right("")
}

object AuthApi : NetworkEndpointScope() {
    fun login(
        key: AuthBody,
    ): HttpEndpoint<String> = post("/auth", body = key)

    @Serializable
    data class AuthBody(
        val key1: String,
        val key2: String
    )
}