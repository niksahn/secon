package ru.secon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.api.ClientPlugin
import io.ktor.client.plugins.api.createClientPlugin
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.headers
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.InternalAPI
import io.ktor.utils.io.KtorDsl
import kotlinx.serialization.json.Json
import ru.secon.core.settings.SettingsRepository
import co.touchlab.kermit.Logger as KLogger

const val BASE_URL = "https://heatman.duckdns.org"

class NetworkService(
    private val settingsRepository: SettingsRepository,
) {
    var httpClient: NetworkClient =
        NetworkClient(HttpClient(configureHttpClient()))

    /** Выполняет сетевой запрос по [endpoint]. */
    suspend fun <Response> request(
        endpoint: NetworkEndpointScope.HttpEndpoint<Response>,
    ): NetworkResponse<Response> = httpClient.request(endpoint)

    private fun configureHttpClient(): HttpClientConfig<*>.() -> Unit = {

        expectSuccess = true

        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                    prettyPrint = true
                }
            )
        }

        install(HttpTimeout) {
            connectTimeoutMillis = 60000
            requestTimeoutMillis = 60000
            socketTimeoutMillis = 60000
        }
        defaultRequest {
            url(BASE_URL)
        }
        install(Logging) {
            logger = createHttpLogger()
            level = LogLevel.ALL
        }
        install(Auth) {
            onLogout = settingsRepository.token::remove
            getToken = settingsRepository.token::getValue
        }
    }

    private fun createHttpLogger(): Logger = object : Logger {
        override fun log(message: String) {
            KLogger.i("OkHttp: $message")
        }
    }
}

val Auth: ClientPlugin<AuthConfig> = createClientPlugin("Auth", ::AuthConfig) {
    val token = pluginConfig.getToken
    val onLogout = pluginConfig.onLogout
    onRequest { request, content ->
        val cuToken = token()
        if (cuToken == null) {
            onLogout()
            return@onRequest
        }
        request.headers { append("Authorization", "Bearer $cuToken") }
    }
    onResponse {
        if (it.status == HttpStatusCode.Unauthorized) onLogout()
    }
}

@KtorDsl
class AuthConfig() {
    @InternalAPI
    var isUnauthorizedResponse: suspend (HttpResponse) -> Boolean =
        { it.status == HttpStatusCode.Unauthorized }
        private set

    lateinit var getToken: () -> String?

    lateinit var onLogout: () -> Unit
}