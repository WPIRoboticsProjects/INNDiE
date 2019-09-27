package edu.wpi.axon.tfdata.code

fun boolToPythonString(bool: Boolean?): String = when (bool) {
    null -> "None"
    true -> "True"
    false -> "False"
}

fun numberToPythonString(number: Number?): String = when (number) {
    null -> "None"
    else -> number.toString()
}

fun <T> listToPythonTuple(list: List<T>, mapElement: (T) -> String): String =
    if (list.size == 1) {
        "(${mapElement(list.first())},)"
    } else {
        list.joinToString(separator = ",", prefix = "(", postfix = ")", transform = mapElement)
    }
