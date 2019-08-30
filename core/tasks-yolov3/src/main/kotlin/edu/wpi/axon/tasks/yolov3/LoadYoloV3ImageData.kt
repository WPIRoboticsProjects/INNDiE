package edu.wpi.axon.tasks.yolov3

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.task.BaseTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

/**
 * Loads an image for YoloV3.
 *
 * TODO: Validate image format
 */
class LoadYoloV3ImageData(name: String) : BaseTask(name) {

    /**
     * The input holding the image.
     */
    var imageInput: Variable by singleAssign()

    /**
     * The output the image data will be stored in.
     */
    var imageDataOutput: Variable by singleAssign()

    /**
     * The output the image size will be stored in.
     */
    var imageSizeOutput: Variable by singleAssign()

    override val imports = setOf(
        Import.ModuleAndIdentifier("axon", "preprocessYoloV3"),
        Import.ModuleAndName("numpy", "np")
    )

    override val inputs: Set<Variable>
        get() = setOf(imageInput)

    override val outputs: Set<Variable>
        get() = setOf(imageDataOutput, imageSizeOutput)

    override val dependencies: Set<Code<*>> = emptySet()

    override fun code() = """
        |${imageDataOutput.name} = preprocessYoloV3(${imageInput.name})
        |${imageSizeOutput.name} = np.array([${imageInput.name}.size[1], ${imageInput.name}.size[0]], dtype=np.float32).reshape(1, 2)
    """.trimMargin()
}
