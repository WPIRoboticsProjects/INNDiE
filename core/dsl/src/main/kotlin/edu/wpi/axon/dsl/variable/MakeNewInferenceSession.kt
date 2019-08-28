package edu.wpi.axon.dsl.variable

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.validator.path.PathValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

class MakeNewInferenceSession(name: String) : Task(name), KoinComponent {

    /**
     * The path to load the ONNX model from.
     */
    var modelPathInput: String? = null

    /**
     * The variable to save the session in.
     */
    var sessionOutput: Variable? = null

    /**
     * Validates the [modelPathInput].
     */
    private val pathValidator: PathValidator by inject()

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnxruntime"))

    override val inputs: Set<Variable>
        get() = setOf()

    override val outputs: Set<Variable>
        get() = setOf(sessionOutput!!)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun isConfiguredCorrectly() = modelPathInput != null &&
        pathValidator.isValidPathName(modelPathInput!!) && sessionOutput != null &&
        super.isConfiguredCorrectly()

    override fun code() = """
        |${sessionOutput!!.name} = onnxruntime.InferenceSession('${modelPathInput!!}')
    """.trimMargin()
}
