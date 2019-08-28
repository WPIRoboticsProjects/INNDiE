package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import
import org.koin.core.KoinComponent

/**
 * Makes a new ONNX inference session.
 */
class InferenceSession(name: String) : InputData(name), KoinComponent {

    override val imports = setOf(Import.ModuleOnly("onnxruntime"))

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && path != null &&
            pathValidator.isValidPathName(path!!)

    override fun code(): String {
        return """
            |$name = onnxruntime.InferenceSession('${path!!}')
        """.trimMargin()
    }
}
