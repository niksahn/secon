package ru.secon.core.monads

sealed interface Operation<out Success, out Failure> {
    data object Preparing : Operation<Nothing, Nothing>
    data class Success<out Success>(val value: Success) : Operation<Success, Nothing>
    data class Failure<out Failure>(val value: Failure) : Operation<Nothing, Failure>
}
