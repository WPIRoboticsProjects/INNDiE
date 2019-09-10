package edu.wpi.axon.core.input

class DictionaryInputStrategy(
    private val imageDataName: String,
    private val imageSizeName: String
) : InputStrategy {

    override fun getPythonSegment(sessionInputsVariableName: String): String {
        var index = 0
        return listOf(imageDataName, imageSizeName).joinToString(prefix = "{", postfix = "}") {
            """$sessionInputsVariableName[$index].name: $it""".also { index++ }
        }
    }
}
