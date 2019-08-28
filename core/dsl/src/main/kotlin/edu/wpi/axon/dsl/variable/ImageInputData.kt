package edu.wpi.axon.dsl.variable

import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.Import

/**
 * Loads an image.
 *
 * TODO: Validate image format
 * TODO: Should this use [Variable] instead of imageData and imageSize
 */
class ImageInputData(name: String) : ModelInputData(name) {

    private val imageDataName = "imageData"
    private val imageSizeName = "imageSize"

    override val imports = setOf(
        Import.ModuleAndIdentifier("PIL", "Image"),
        Import.ModuleAndName("numpy", "np")
    )

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable>
        get() = emptySet()

    override val dependencies: Set<Code<*>> = emptySet()

    override fun code() = """
        |$name = Image.open('${path!!}')
        |$imageDataName = preprocess($name)
        |$imageSizeName = np.array([$name.size[1], $name.size[0]], dtype=np.float32).reshape(1, 2)
    """.trimMargin()

    override fun codeForModelInput(sessionInputsVariableName: String): String {
        var index = 0
        return listOf(imageDataName, imageSizeName).joinToString(prefix = "{", postfix = "}") {
            """$sessionInputsVariableName[$index].name: $it""".also { index++ }
        }
    }
}
