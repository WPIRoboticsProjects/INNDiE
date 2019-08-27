package edu.wpi.axon.core.dsl

class PythonVariableNameValidator : VariableNameValidator {

    override fun isValidVariableName(name: String) = when (name) {
        "_" -> false
        else -> name.contains(Regex("^[^\\d\\W]\\w*\\Z"))
    }
}
