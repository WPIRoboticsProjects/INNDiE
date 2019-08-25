package edu.wpi.axon.core.preprocess

class TensorProtoImagePreprocessStrategy(
    private val tensorName: String,
    private val imageDataName: String,
    private val imageSizeName: String
) : ImagePreprocessStrategy {

    override fun getPythonSegment() = """
        |$imageDataName = $tensorName
        |$imageSizeName = np.array(np.shape($tensorName[0,0,:,:]), dtype=np.float32).reshape(1, 2)
    """.trimMargin()
}
