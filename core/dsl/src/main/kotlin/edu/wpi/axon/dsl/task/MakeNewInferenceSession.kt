package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign
import org.koin.core.KoinComponent
import org.koin.core.inject

class MakeNewInferenceSession(name: String) : Task(name), KoinComponent {

    /**
     * The path to load the ONNX model from.
     */
    var modelPathInput: String by singleAssign()

    /**
     * The variable to save the session in.
     */
    var sessionOutput: Variable by singleAssign()

    /**
     * Validates the [modelPathInput].
     */
    private val pathValidator: PathValidator by inject()

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnxruntime"))

    override val inputs: Set<Variable>
        get() = setOf()

    override val outputs: Set<Variable>
        get() = setOf(sessionOutput)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun isConfiguredCorrectly() = pathValidator.isValidPathName(modelPathInput) &&
        super.isConfiguredCorrectly()

    override fun code() = """
        |${sessionOutput.name} = onnxruntime.InferenceSession('$modelPathInput')
    """.trimMargin()
}
