package ru.secon.core.monads


import ru.secon.core.monads.Either.Right
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract


sealed interface Either<out Left, out Right> {
    /**
     * Левое значение (для неправильных или альтернативных значений) монады [Either].
     */
    data class Left<out T>(val left: T) : Either<T, Nothing> {
        companion object {
            operator fun invoke(): Left<Unit> {
                return Left(Unit)
            }
        }
    }

    /**
     * Правое значение (для правильных значений) монады [Either].
     */
    data class Right<out T>(val right: T) : Either<Nothing, T> {
        companion object {
            operator fun invoke(): Right<Unit> {
                return Right(Unit)
            }
        }
    }

    companion object {
        /**
         * Возвращает полиморфную функцию преобразующую функцию [transform] в структуру [Either].
         */
        inline fun <Left, Right, RightOut> lift(
            crossinline transform: (Right) -> RightOut,
        ): (Either<Left, Right>) -> Either<Left, RightOut> {
            return { either -> either.map(transform) }
        }

        /**
         * Возвращает полиморфную функцию преобразующую функцию [transform] в структуру [Either].
         */
        inline fun <Left, Right, RightOut> suspendLift(
            crossinline transform: suspend (Right) -> RightOut,
        ): suspend (Either<Left, Right>) -> Either<Left, RightOut> {
            return { either -> either.map { transform(it) } }
        }

        /**
         * Возвращает полиморфную функцию преобразующую функцию [leftTransform] и [rightTransform] в структуру [Either].
         */
        inline fun <Left, Right, LeftOut, RightOut> lift(
            crossinline leftTransform: (Left) -> LeftOut,
            crossinline rightTransform: (Right) -> RightOut,
        ): (Either<Left, Right>) -> Either<LeftOut, RightOut> {
            return { either -> either.bimap(leftTransform, rightTransform) }
        }

        /**
         * Возвращает полиморфную функцию преобразующую функцию [leftTransform] и [rightTransform] в структуру [Either].
         */
        inline fun <Left, Right, LeftOut, RightOut> suspendLift(
            crossinline leftTransform: suspend (Left) -> LeftOut,
            crossinline rightTransform: suspend (Right) -> RightOut,
        ): suspend (Either<Left, Right>) -> Either<LeftOut, RightOut> {
            return { either ->
                either.bimap(
                    leftTransform = { leftTransform(it) },
                    rightTransform = { rightTransform(it) },
                )
            }
        }

        /**
         * Оборачивает выполнение [block] в `try-catch`. Возвращается [Either.Left] если [block] вернул исключение.
         */
        inline fun <Right> catch(block: () -> Right): Either<Throwable, Right> {
            return try {
                Right(block())
            } catch (error: Throwable) {
                Left(error)
            }
        }
    }
}

/**
 * Возвращает значение `true`, если [Either.Left], в противном случае `false`.
 */
@OptIn(ExperimentalContracts::class)
fun <Left, Right> Either<Left, Right>.isLeft(): Boolean {
    contract {
        returns(true) implies (this@isLeft is Either.Left<Left>)
        returns(false) implies (this@isLeft is Either.Right<Right>)
    }
    return this is Either.Left
}

/**
 * Возвращает значение `true`, если [Either.Right], в противном случае `false`.
 */
@OptIn(ExperimentalContracts::class)
fun <Left, Right> Either<Left, Right>.isRight(): Boolean {
    contract {
        returns(true) implies (this@isRight is Either.Right<Right>)
        returns(false) implies (this@isRight is Either.Left<Left>)
    }
    return this is Either.Right
}
