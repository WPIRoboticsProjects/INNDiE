package edu.wpi.axon.dsl.variable

import edu.wpi.axon.dsl.Import
import org.koin.core.KoinComponent

/**
 * Makes a new ONNX inference session.
 */
class InferenceSession(name: String) : FileInputData(name), KoinComponent {

    override val imports = setOf(Import.ModuleOnly("onnxruntime"))

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable> = emptySet()

    override val dependencies: Set<FileInputData> = emptySet()

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && path != null &&
            pathValidator.isValidPathName(path!!)

    override fun code(): String {
        return """
            |$name = onnxruntime.InferenceSession('${path!!}')
        """.trimMargin()
    }
}
