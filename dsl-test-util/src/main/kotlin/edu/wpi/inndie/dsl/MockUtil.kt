package edu.wpi.inndie.dsl

import edu.wpi.inndie.dsl.validator.path.PathValidator
import edu.wpi.inndie.dsl.validator.variablename.VariableNameValidator
import edu.wpi.inndie.dsl.variable.Variable
import io.mockk.every
import io.mockk.mockk

fun mockVariableNameValidator(vararg nameValidations: Pair<String, Boolean>) =
    mockk<VariableNameValidator> {
        nameValidations.forEach { (name, isValid) ->
            every { isValidVariableName(name) } returns isValid
        }
    }

fun mockPathValidator(vararg pathValidations: Pair<String, Boolean>) =
    mockk<PathValidator> {
        pathValidations.forEach { (name, isValid) ->
            every { isValidPathName(name) } returns isValid
        }
    }

inline fun <reified T : Configurable> configuredCorrectly(inputName: String? = null) =
    mockk<T> {
        if (inputName != null && this is Variable) {
            every { name } returns inputName
        }

        every { isConfiguredCorrectly() } returns true
    }

inline fun <reified T : Configurable> configuredIncorrectly(inputName: String? = null) =
    mockk<T> {
        if (inputName != null && this is Variable) {
            every { name } returns inputName
        }

        every { isConfiguredCorrectly() } returns false
    }
