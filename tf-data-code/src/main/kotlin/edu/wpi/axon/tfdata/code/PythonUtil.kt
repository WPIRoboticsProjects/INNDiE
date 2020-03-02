package edu.wpi.axon.tfdata.code

import arrow.core.Either
import arrow.core.Option
import arrow.core.Tuple2
import edu.wpi.axon.tfdata.SerializableEither
import edu.wpi.axon.tfdata.SerializableTuple2

data class Unquoted(val value: String)
data class ListAsList(val list: List<Any?>)
data class ListAsTuple(val list: List<Any?>)

fun String.unquoted() = Unquoted(this)

fun List<Any?>.asList() = ListAsList(this)

fun List<Any?>.asTuple() = ListAsTuple(this)

/**
 * Converts a value into its Python string equivalent.
 *
 * @param value The value to convert.
 * @return The Python string.
 */
fun pythonString(value: Any?): String = when (value) {
    null -> "None"
    is Unquoted -> value.value
    is String -> """"$value""""
    is Char -> "'$value'"
    is Boolean -> if (value) "True" else "False"
    is Tuple2<*, *> -> "(${pythonString(value.a)}, ${pythonString(value.b)})"
    is SerializableTuple2<*, *> -> pythonString(value.toTuple2())
    is Either<*, *> -> value.fold({ pythonString(it) }, { pythonString(it) })
    is SerializableEither<*, *> -> pythonString(value.toEither())
    is Option<*> -> value.fold({ pythonString(null) }, { pythonString(it) })

    is ListAsTuple -> if (value.list.size == 1) {
        "(${value.list.first()},)"
    } else {
        value.list.joinToString(
            separator = ",",
            prefix = "(",
            postfix = ")",
            transform = ::pythonString
        )
    }

    is ListAsList -> value.list.joinToString(
        separator = ",",
        prefix = "[",
        postfix = "]",
        transform = ::pythonString
    )

    is Map<*, *> -> value.entries.joinToString(separator = ", ", prefix = "{", postfix = "}") {
        "${it.key}: ${pythonString(it.value)}"
    }

    else -> value.toString()
}

/**
 * Constructs a string specifying named arguments.
 *
 * @param namedArgs The arguments (parameter name to argument value).
 * @return The code for the arguments.
 */
inline fun <reified T : Any?> namedArguments(namedArgs: List<Pair<String, T>>) =
    namedArgs.joinToString(separator = ", ") { "${it.first}=${pythonString(it.second)}" }
