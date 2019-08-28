package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import
import edu.wpi.axon.dsl.variable.Variable

/**
 * Constructs the input argument for the YoloV3 model.
 */
class ConstructYoloV3ImageInput(name: String) : Task(name) {

    var imageDataInput: Variable? = null
    var imageSizeInput: Variable? = null
    var sessionInput: Variable? = null
    var output: Variable? = null

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnx"))

    override val inputs: Set<Variable>
        get() = setOf(imageDataInput!!, imageSizeInput!!, sessionInput!!)

    override val outputs: Set<Variable>
        get() = setOf(output!!)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun isConfiguredCorrectly() = imageDataInput != null && imageSizeInput != null &&
        sessionInput != null && output != null && super.isConfiguredCorrectly()

    override fun code() = """
        |${output!!.name} = {${sessionInput!!.name}.get_inputs()[0].name: ${imageDataInput!!.name}, ${sessionInput!!.name}.get_inputs()[1].name: ${imageSizeInput!!.name}}
    """.trimMargin()
}
