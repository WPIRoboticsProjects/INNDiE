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
