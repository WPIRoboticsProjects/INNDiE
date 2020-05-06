@file:Suppress("UnstableApiUsage")

package edu.wpi.inndie.tfdata

import arrow.core.Either
import arrow.core.Option
import arrow.core.Tuple2
import com.google.common.graph.Graph
import com.google.common.graph.GraphBuilder
import com.google.common.graph.ImmutableGraph
import kotlin.properties.Delegates
import kotlinx.serialization.CompositeDecoder
import kotlinx.serialization.Decoder
import kotlinx.serialization.Encoder
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialDescriptor
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.Serializer
import kotlinx.serialization.internal.ArrayListSerializer
import kotlinx.serialization.internal.HashSetSerializer
import kotlinx.serialization.internal.PairSerializer
import kotlinx.serialization.internal.SerialClassDescImpl
import org.octogonapus.ktguava.collections.toImmutableGraph

interface SerializableOption<T> {
    val value: T?
    fun toOption(): Option<T> = Option.fromNullable(value)
}

@Serializable
data class SerializableOptionB(override val value: Boolean?) :
    SerializableOption<Boolean> {
    companion object {
        fun fromOption(option: Option<Boolean>) =
            SerializableOptionB(option.orNull())
    }
}

interface SerializableEither<L, R> {
    fun toEither(): Either<L, R>
}

@Serializable
sealed class SerializableEitherITii :
    SerializableEither<Int, SerializableTuple2II> {

    @Serializable
    data class Left(val value: Int) : SerializableEitherITii()

    @Serializable
    data class Right(val value: SerializableTuple2II) : SerializableEitherITii()

    override fun toEither() = when (this) {
        is Left -> Either.Left(value)
        is Right -> Either.Right(value)
    }

    companion object {

        fun fromEither(either: Either<Int, SerializableTuple2II>): SerializableEitherITii =
            when (either) {
                is Either.Left -> Left(
                    either.a
                )
                is Either.Right -> Right(
                    either.b
                )
            }
    }
}

@Serializable
sealed class SerializableEitherDLd :
    SerializableEither<Double, List<Double>> {

    @Serializable
    data class Left(val value: Double) : SerializableEitherDLd()

    @Serializable
    data class Right(val value: List<Double>) : SerializableEitherDLd()

    override fun toEither() = when (this) {
        is Left -> Either.Left(value)
        is Right -> Either.Right(value)
    }

    companion object {

        fun fromEither(either: Either<Double, List<Double>>): SerializableEitherDLd =
            when (either) {
                is Either.Left -> Left(
                    either.a
                )
                is Either.Right -> Right(
                    either.b
                )
            }
    }
}

interface SerializableTuple2<out T1, out T2> {
    val data1: T1
    val data2: T2
    fun toTuple2(): Tuple2<T1, T2> = Tuple2(data1, data2)
}

@Serializable
data class SerializableTuple2II(
    override val data1: Int,
    override val data2: Int
) : SerializableTuple2<Int, Int>

@Serializer(forClass = ImmutableGraph::class)
class ImmutableGraphSerializer<T : Any>(
    val dataSerializer: KSerializer<T>
) : KSerializer<ImmutableGraph<T>> {

    override val descriptor: SerialDescriptor =
        object : SerialClassDescImpl("ImmutableGraphSerializer") {
            init {
                addElement("directed")
                addElement("allowSelfLoops")
                addElement("adjacencyList")
            }
        }

    override fun serialize(encoder: Encoder, obj: ImmutableGraph<T>) {
        val out = encoder.beginStructure(descriptor)
        out.encodeBooleanElement(descriptor, 0, obj.isDirected)
        out.encodeBooleanElement(descriptor, 1, obj.allowsSelfLoops())
        out.encodeSerializableElement(
            descriptor,
            2,
            ArrayListSerializer(PairSerializer(dataSerializer, HashSetSerializer(dataSerializer))),
            obj.adjacencyList()
        )
        out.endStructure(descriptor)
    }

    override fun deserialize(decoder: Decoder): ImmutableGraph<T> {

        var directed by Delegates.notNull<Boolean>()
        var allowSelfLoops by Delegates.notNull<Boolean>()
        lateinit var adjacencyList: List<Pair<T, Set<T>>>

        val inp = decoder.beginStructure(descriptor)
        loop@ while (true) {
            when (val i = inp.decodeElementIndex(descriptor)) {
                CompositeDecoder.READ_DONE -> break@loop

                0 -> directed = inp.decodeBooleanElement(descriptor, i)

                1 -> allowSelfLoops = inp.decodeBooleanElement(descriptor, i)

                2 -> adjacencyList = inp.decodeSerializableElement(
                    descriptor,
                    i,
                    ArrayListSerializer(
                        PairSerializer(
                            dataSerializer,
                            HashSetSerializer(dataSerializer)
                        )
                    )
                )

                else -> throw SerializationException("Unknown index $i")
            }
        }
        inp.endStructure(descriptor)

        val graph = if (directed) {
            GraphBuilder.directed()
        } else {
            GraphBuilder.undirected()
        }.allowsSelfLoops(allowSelfLoops).build<T>()

        adjacencyList.forEach { (node, adjacentNodes) ->
            adjacentNodes.forEach { adjacentNode ->
                graph.putEdge(node, adjacentNode)
            }
        }

        return graph.toImmutableGraph()
    }

    private fun <T : Any> Graph<T>.adjacencyList(): List<Pair<T, Set<T>>> = nodes().map {
        it to successors(it)
    }
}
