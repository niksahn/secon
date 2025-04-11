package ru.secon.core.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import ru.secon.core.monads.Either
import ru.secon.core.monads.flatMap
import ru.secon.core.monads.mapLeft

/** Представляет сетевой клиент. */
class NetworkClient(
    val httpClient: HttpClient,
) {
    /** Выполняет переданны в [block] сетевой запрос. */
    suspend inline fun <reified Response> request(block: HttpRequestBuilder.() -> Unit): NetworkResponse<Response> {
        val requestBuilder = HttpRequestBuilder().apply(block)
        val httpResponse = Either.catch { httpClient.request(requestBuilder) }
        return httpResponse
            .flatMap { response -> Either.catch<Response> { response.body() } }
            .mapLeft { exception ->
                println(exception)
                val failure = NetworkResponseFailure(exception)
                println(failure)
                return@mapLeft failure
            }
    }

    /** Выполняет переданный в [endpoint] сетевой запрос. */
    suspend fun <Response> request(endpoint: NetworkEndpointScope.HttpEndpoint<Response>): NetworkResponse<Response> {
        val httpResponse = Either.catch { httpClient.request(endpoint.httpRequestBuilder) }
        return httpResponse
            .flatMap { response -> Either.catch<Response> { response.body(endpoint.responseTypeInfo) } }
            .mapLeft { exception ->
                val failure = NetworkResponseFailure(exception)
                return@mapLeft failure
            }
    }
}

typealias NetworkResponse<T> = Either<NetworkResponseFailure, T>

/** Возвращает `true` если операция завершена ошибкой. */
val NetworkResponse<*>.isFailure: Boolean get() = this is Either.Left

/** Возвращает `true` если операция завершена успешно. */
val NetworkResponse<*>.isSuccess: Boolean get() = this is Either.Right
