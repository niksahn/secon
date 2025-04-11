package ru.secon.core.network

import io.ktor.client.plugins.ResponseException
import io.ktor.serialization.ContentConvertException
import kotlinx.io.IOException
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlin.coroutines.cancellation.CancellationException

/** Представляет обработанную сетевую ошибку. */
@Serializable
sealed class NetworkResponseFailure {
    /** Не удалось подключиться к серверу. */
    @Serializable
    data object NoConnection : NetworkResponseFailure()

    /** Запрос был отменен. */
    @Serializable
    data object CoroutinesCancellation : NetworkResponseFailure()

    /** Сервер ответил статус кодом отличным от 200. */
    @Serializable
    data class Response(
        val status: HttpError,
    ) : NetworkResponseFailure()

    /** Ошибка парсинга ответа. */
    @Serializable
    data object Parsing : NetworkResponseFailure()

    /** Внутреняя ошибка. */
    data class Internal(val description: String? = null) : NetworkResponseFailure()

    companion object
}

/** Ошибки сервера. **/
@Serializable
sealed interface HttpError {
    val code: Int

    /** 500-599 */
    @Serializable
    data class ServerError(override val code: Int) : HttpError

    /** 400-499 */
    @Serializable
    data class ClientRequestError(override val code: Int) :
        HttpError {
        companion object {
            /** 400 */
            val BadRequest: ClientRequestError = ClientRequestError(400)

            /** 404 */
            val NotFound: ClientRequestError = ClientRequestError(404)
        }
    }

    /** 300-399 */
    @Serializable
    data class RedirectResponseError(override val code: Int) :
        HttpError
}

/** Создает экземпляр [NetworkResponseFailure] на основе [Exception].  */
operator fun NetworkResponseFailure.Companion.invoke(throwable: Throwable) = when (throwable) {
    is IOException -> NetworkResponseFailure.NoConnection
    is CancellationException -> NetworkResponseFailure.CoroutinesCancellation
    is SerializationException, is ContentConvertException -> NetworkResponseFailure.Parsing
    is ResponseException -> {
        when (val status = throwable.response.status.value) {
            in 300..399 -> NetworkResponseFailure.Response(
                HttpError.RedirectResponseError(
                    status
                )
            )

            in 400..499 -> NetworkResponseFailure.Response(HttpError.ClientRequestError(status))
            in 500..599 -> NetworkResponseFailure.Response(HttpError.ServerError(status))
            else -> NetworkResponseFailure.Internal()
        }
    }

    else -> NetworkResponseFailure.Internal()
}
