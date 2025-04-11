package ru.secon.core.network

import kotlinx.serialization.Serializable
import ru.secon.core.monads.Operation
import ru.secon.core.monads.fold
import ru.secon.core.monads.mapLeft


typealias NetworkOperation<T> = Operation<T, NetworkFailure>

/** Классификатор результатов сетевого запроса. */
@Serializable
enum class NetworkFailure { NO_CONNECTION, ERROR }

/** Преобразует [NetworkResponseFailure] в [NetworkFailure] */
fun NetworkResponseFailure.convertToStateNetworkError() = when (this) {
    NetworkResponseFailure.NoConnection ->
        NetworkFailure.NO_CONNECTION

    NetworkResponseFailure.CoroutinesCancellation,
    is NetworkResponseFailure.Internal,
    is NetworkResponseFailure.Parsing,
    is NetworkResponseFailure.Response ->
        NetworkFailure.ERROR
}


/** Конвертирует [NetworkResponse] в [NetworkOperation]. */
fun <T> NetworkResponse<T>.toOperation(): NetworkOperation<T> = this
    .mapLeft(NetworkResponseFailure::convertToStateNetworkError)
    .fold(
        leftTransform = { Operation.Failure(it) },
        rightTransform = { response -> Operation.Success(response) }
    )

/** Обновляет значение [Operation.Success] */
fun <T> NetworkOperation<T>.updateIfSuccess(value: T): NetworkOperation<T> = when (this) {
    is Operation.Failure, Operation.Preparing -> this
    is Operation.Success -> this.copy(value)
}

/** Обновляет значение [Operation.Success] */
inline fun <T> NetworkOperation<T>.transformIfSuccess(transform: (T) -> T): NetworkOperation<T> =
    when (this) {
        is Operation.Failure, Operation.Preparing -> this
        is Operation.Success -> this.copy(transform(this.value))
    }
