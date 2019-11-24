package edu.wpi.axon.tfdata

import arrow.core.Either
import arrow.core.Tuple2
import kotlinx.serialization.Serializable

private interface SerializableEither<L, R> {

    private data class Left<L, R>(val value: L) : SerializableEither<L, R>

    private data class Right<L, R>(val value: R) : SerializableEither<L, R>

    fun toEither(): Either<L, R> = when (this) {
        is Left -> Either.Left(value)
        is Right -> Either.Right(value)
        else -> error("Unhandled case.")
    }
}

@Serializable
sealed class SerializableEitherITii : SerializableEither<Int, SerializableTuple2II> {

    @Serializable
    data class Left(val value: Int) : SerializableEitherITii()

    @Serializable
    data class Right(val value: SerializableTuple2II) : SerializableEitherITii()

    companion object {

        fun fromEither(either: Either<Int, SerializableTuple2II>): SerializableEitherITii =
            when (either) {
                is Either.Left -> Left(either.a)
                is Either.Right -> Right(either.b)
            }
    }
}

@Serializable
sealed class SerializableEitherDLd : SerializableEither<Double, List<Double>> {

    @Serializable
    data class Left(val value: Double) : SerializableEitherDLd()

    @Serializable
    data class Right(val value: List<Double>) : SerializableEitherDLd()

    companion object {

        fun fromEither(either: Either<Double, List<Double>>): SerializableEitherDLd =
            when (either) {
                is Either.Left -> Left(either.a)
                is Either.Right -> Right(either.b)
            }
    }
}

interface SerializableTuple2I<out T1, out T2> {
    val data1: T1
    val data2: T2
    fun toTuple2(): Tuple2<T1, T2> = Tuple2(data1, data2)
}

@Serializable
data class SerializableTuple2II(
    override val data1: Int,
    override val data2: Int
) : SerializableTuple2I<Int, Int>
