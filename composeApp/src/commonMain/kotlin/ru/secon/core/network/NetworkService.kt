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
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as KLogger

const val BASE_URL = ""

class NetworkService {
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
            connectTimeoutMillis = 6000
            requestTimeoutMillis = 6000
            socketTimeoutMillis = 6000
        }
        defaultRequest {
            url(BASE_URL)
        }
        install(Logging) {
            logger = createHttpLogger()
            level = LogLevel.ALL
        }
    }

    private fun createHttpLogger(): Logger = object : Logger {
        override fun log(message: String) {
            KLogger.i("OkHttp: $message")
        }
    }
}

public val Auth: ClientPlugin<Unit> = createClientPlugin("Auth") {
    onRequest { request, content ->

    }
}