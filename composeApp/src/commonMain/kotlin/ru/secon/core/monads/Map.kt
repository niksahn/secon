package ru.secon.core.monads

import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract


@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Out> Either<Left, Right>.map(
    transform: (Right) -> Out,
): Either<Left, Out> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (this.isRight()) Either.Right(transform(right)) else this
}


@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Out> Either<Left, Right>.mapLeft(
    transform: (Left) -> Out,
): Either<Out, Right> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (this.isLeft()) Either.Left(transform(left)) else this
}

@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, LeftOut, RightOut> Either<Left, Right>.bimap(
    leftTransform: (Left) -> LeftOut,
    rightTransform: (Right) -> RightOut,
): Either<LeftOut, RightOut> {
    contract {
        callsInPlace(leftTransform, InvocationKind.AT_MOST_ONCE)
        callsInPlace(rightTransform, InvocationKind.AT_MOST_ONCE)
    }
    return if (this.isLeft()) Either.Left(leftTransform(left)) else Either.Right(
        rightTransform(
            right
        )
    )
}

@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Result> Either<Left, Right>.fold(
    leftTransform: (Left) -> Result,
    rightTransform: (Right) -> Result,
): Result {
    contract {
        callsInPlace(leftTransform, InvocationKind.AT_MOST_ONCE)
        callsInPlace(rightTransform, InvocationKind.AT_MOST_ONCE)
    }
    return if (this.isLeft()) leftTransform(left) else rightTransform(right)
}


@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Result> Either<Left, Right>.foldLeft(
    initial: Result,
    transform: (Left) -> Result,
): Result {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (this.isLeft()) transform(left) else initial
}


@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Result> Either<Left, Right>.foldRight(
    initial: Result,
    transform: (Right) -> Result,
): Result {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (this.isRight()) transform(right) else initial
}

@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Out> Either<Left, Right>.flatMap(
    transform: (Right) -> Either<Left, Out>,
): Either<Left, Out> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (isRight()) transform(right) else this
}

@OptIn(ExperimentalContracts::class)
inline fun <Left, Right, Out> Either<Left, Right>.flatMapLeft(
    transform: (Left) -> Either<Out, Right>,
): Either<Out, Right> {
    contract { callsInPlace(transform, InvocationKind.AT_MOST_ONCE) }
    return if (isLeft()) transform(left) else this
}

fun <Left, Right> Either<Left, Right>.swap(): Either<Right, Left> = fold(
    leftTransform = { Either.Right(it) },
    rightTransform = { Either.Left(it) },
)

fun <Left, Right> Either<Left, Either<Left, Right>>.flatten(): Either<Left, Right> = flatMap { it }
