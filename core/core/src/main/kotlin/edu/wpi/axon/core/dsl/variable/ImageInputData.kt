package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.PathValidator
import edu.wpi.axon.core.dsl.VariableNameValidator

// TODO: Validate image format
class ImageInputData(
    name: String,
    variableNameValidator: VariableNameValidator,
    pathValidator: PathValidator
) : InputData(name, variableNameValidator, pathValidator)
