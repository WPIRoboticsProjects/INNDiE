package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.PathValidator
import edu.wpi.axon.core.dsl.VariableNameValidator

// TODO: Need to detect the format of the labels or maybe ask the user what it is in the UI
class ClassLabels(
    name: String,
    variableNameValidator: VariableNameValidator,
    pathValidator: PathValidator
) : InputData(name, variableNameValidator, pathValidator)
