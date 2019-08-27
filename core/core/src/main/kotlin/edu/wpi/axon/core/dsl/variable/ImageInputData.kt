package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import

// TODO: Validate image format
class ImageInputData(name: String) : InputData(name) {

    override val imports = setOf(
        Import.ModuleAndIdentifier("PIL", "Image")
    )
}
