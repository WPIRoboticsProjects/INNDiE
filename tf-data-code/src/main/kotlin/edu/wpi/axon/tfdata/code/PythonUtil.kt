package edu.wpi.axon.tfdata.code

import arrow.core.Tuple2

/**
 * Converts a [Boolean] to the equivalent Python code.
 *
 * @param bool The [Boolean].
 * @return The Python code equivalent.
 */
fun boolToPythonString(bool: Boolean?): String = when (bool) {
    null -> "None"
    true -> "True"
    false -> "False"
}

/**
 * Converts a [Number] to the equivalent Python code.
 *
 * @param number The [Number].
 * @return The Python code equivalent.
 */
fun numberToPythonString(number: Number?): String = when (number) {
    null -> "None"
    else -> number.toString()
}

/**
 * Converts a [List] to the equivalent Python code for a tuple.
 *
 * @param list The [List].
 * @param mapElement A mapping function applied to each element of the [list].
 * @return The Python code equivalent.
 */
fun <T> listToPythonTuple(list: List<T>?, mapElement: ((T) -> String)? = null): String =
    when (list) {
        null -> "None"
        else -> if (list.size == 1) {
            "(${list.first()},)"
        } else {
            list.joinToString(
                separator = ",",
                prefix = "(",
                postfix = ")",
                transform = mapElement
            )
        }
    }

/**
 * Converts a [Tuple2] to the equivalent Python code.
 *
 * @param tuple The [Tuple2].
 * @return The Python code equivalent.
 */
fun tupleToPythonTuple(tuple: Tuple2<*, *>) = "(${tuple.a}, ${tuple.b})"

/**
 * Puts quotes around a string. `null` strings become `None`.
 *
 * @param string The [String].
 * @return The quoted string or `None`.
 */
fun quoted(string: String?) = if (string == null) "None" else """"$string""""

/**
 * Converts a [Map] to the equivalent Python code.
 *
 * @param map The [Map].
 * @return The Python code equivalent.
 */
fun mapToPythonString(map: Map<String, Double>?): String =
    map?.entries?.joinToString(separator = ", ", prefix = "{", postfix = "}") {
        "${it.key}: ${it.value}"
    } ?: "None"

/**
 * Constructs a string specifying named arguments.
 *
 * @param namedArgs The arguments (parameter name to argument value).
 * @return The code for the arguments.
 */
fun <T : Any> namedArguments(namedArgs: List<Pair<String, T?>>): String {
    return namedArgs.joinToString(separator = ", ") {
        if (it.second != null) """${it.first}=${it.second}""" else "None"
    }
}
