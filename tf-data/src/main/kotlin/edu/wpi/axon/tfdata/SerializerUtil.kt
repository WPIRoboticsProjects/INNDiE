package edu.wpi.axon.tfdata

import arrow.core.Either
import arrow.core.Tuple2
import kotlinx.serialization.Serializable

@Serializable
sealed class SerializableEither<L, R> {

    @Serializable
    data class Left<L, R>(val value: L) : SerializableEither<L, R>()

    @Serializable
    data class Right<L, R>(val value: R) : SerializableEither<L, R>()

    fun fromEither(either: Either<L, R>) = when (either) {
        is Either.Left -> TODO() // SerializableEither.Left(either.a)
        is Either.Right -> SerializableEither.Right(either.b)
    }

    fun toEither(): Either<L, R> = when (this) {
        is Left -> Either.Left(value)
        is Right -> Either.Right(value)
    }
}

fun <L, R> Either<L, R>.serializableEither(): SerializableEither<L, R> = SerializableEither.fromEither(this)

@Serializable
data class SerializableTuple2<out T1, out T2>(
    val data1: T1,
    val data2: T2
) {
    fun toTuple2(): Tuple2<T1, T2> = Tuple2(data1, data2)
}
