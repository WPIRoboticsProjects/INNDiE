package edu.wpi.axon.tfdata.code

import arrow.core.Tuple2

fun boolToPythonString(bool: Boolean?): String = when (bool) {
    null -> "None"
    true -> "True"
    false -> "False"
}

fun numberToPythonString(number: Number?): String = when (number) {
    null -> "None"
    else -> number.toString()
}

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

fun tupleToPythonTuple(tuple: Tuple2<*, *>) = "(${tuple.a}, ${tuple.b})"

fun quoted(string: String?) = if (string == null) "None" else """"$string""""

fun mapToPythonString(renormClipping: Map<String, Double>?): String =
    renormClipping?.entries?.joinToString(separator = ", ", prefix = "{", postfix = "}") {
        "${it.key}: ${it.value}"
    } ?: "None"
