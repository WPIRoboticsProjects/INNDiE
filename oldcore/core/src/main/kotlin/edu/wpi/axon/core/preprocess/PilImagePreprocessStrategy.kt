package edu.wpi.axon.core.preprocess

class PilImagePreprocessStrategy(
    private val imageName: String,
    private val imageDataName: String,
    private val imageSizeName: String
) : ImagePreprocessStrategy {

    override fun getPythonSegment() = """
        |$imageDataName = preprocess($imageName)
        |$imageSizeName = np.array([$imageName.size[1], $imageName.size[0]], dtype=np.float32).reshape(1, 2)
    """.trimMargin()
}
