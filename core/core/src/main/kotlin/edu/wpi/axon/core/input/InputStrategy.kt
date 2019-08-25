package edu.wpi.axon.core.input

interface InputStrategy {
    fun getPythonSegment(sessionInputsVariableName: String): String
}
