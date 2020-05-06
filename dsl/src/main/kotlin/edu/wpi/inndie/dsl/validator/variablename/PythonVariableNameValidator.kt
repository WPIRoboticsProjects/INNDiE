package edu.wpi.inndie.dsl.validator.variablename

class PythonVariableNameValidator :
    VariableNameValidator {

    override fun isValidVariableName(name: String) = when (name) {
        "_" -> false
        else -> name.contains(Regex("^[^\\d\\W]\\w*\\Z"))
    }
}
