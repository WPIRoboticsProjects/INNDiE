package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.task.BaseTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Constructs the input argument for the YoloV3 model.
 */
class ConstructYoloV3ImageInput(name: String) : BaseTask(name) {

    var imageDataInput: Variable by singleAssign()
    var imageSizeInput: Variable by singleAssign()
    var sessionInput: Variable by singleAssign()
    var output: Variable by singleAssign()

    override val imports: Set<Import> = setOf(Import.ModuleOnly("onnx"))

    override val inputs: Set<Variable>
        get() = setOf(imageDataInput, imageSizeInput, sessionInput)

    override val outputs: Set<Variable>
        get() = setOf(output)

    override val dependencies: Set<Code<*>>
        get() = setOf()

    override fun code() = """
        |${output.name} = {${sessionInput.name}.get_inputs()[0].name: ${imageDataInput.name}, ${sessionInput.name}.get_inputs()[1].name: ${imageSizeInput.name}}
    """.trimMargin()
}
