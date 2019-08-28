package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import

/**
 * Loads an image.
 *
 * TODO: Validate image format
 */
class ImageInputData(name: String) : ModelInputData(name) {

    private val imageDataName = "imageData"
    private val imageSizeName = "imageSize"

    override val imports = setOf(
        Import.ModuleAndIdentifier("PIL", "Image"),
        Import.ModuleAndName("numpy", "np")
    )

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
