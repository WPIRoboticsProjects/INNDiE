package edu.wpi.axon.core.dsl

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
