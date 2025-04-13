package ru.secon.domain

import kotlinx.coroutines.flow.map
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import ru.secon.core.monads.Either
import ru.secon.core.network.NetworkEndpointScope
import ru.secon.core.network.NetworkResponse
import ru.secon.core.network.NetworkService
import ru.secon.core.settings.SettingsRepository

class AuthService(
    private val settingsRepository: SettingsRepository,
    private val networkService: NetworkService
) {
    fun isLogin() =  !settingsRepository.token.getValue().isNullOrEmpty()
    fun observeIsLogin() = settingsRepository.token.settingsFlow.map { !it.isNullOrEmpty() }
    fun logout() {
        settingsRepository.token.remove()
    }

    suspend fun login(
        key: AuthApi.AuthBody
    ): NetworkResponse<AuthApi.Token> =
        networkService.request(AuthApi.login(key))
            .also { if (it is Either.Right) settingsRepository.token.set(it.right.token) }

}

object AuthApi : NetworkEndpointScope() {
    fun login(
        key: AuthBody,
    ): HttpEndpoint<Token> = post("/auth/login", body = key)

    @Serializable
    data class Token(
        val token: String
    )

    @Serializable
    data class AuthBody(
        @SerialName("FirstCode") val key1: String,
        @SerialName("SecondCode") val key2: String
    )
}