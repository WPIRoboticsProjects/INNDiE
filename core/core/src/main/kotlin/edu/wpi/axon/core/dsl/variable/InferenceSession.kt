package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.validator.path.PathValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

class InferenceSession(name: String) : Variable(name), KoinComponent {

    var modelPath: String? = null

    private val pathValidator: PathValidator by inject()

    override val imports = setOf(Import.ModuleOnly("onnxruntime"))

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && modelPath != null &&
            pathValidator.isValidPathName(modelPath!!)
}
