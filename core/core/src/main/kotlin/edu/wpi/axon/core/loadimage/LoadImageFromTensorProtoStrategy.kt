package edu.wpi.axon.core.loadimage

class LoadImageFromTensorProtoStrategy(
    private val tensorProtoPath: String
) : LoadImageStrategy {

    override fun getPythonSegment(imageVariableName: String): String =
        """
        |tensor = onnx.TensorProto()
        |with open('$tensorProtoPath', 'rb') as f:
        |    tensor.ParseFromString(f.read())
        |    $imageVariableName = numpy_helper.to_array(tensor)
        """.trimMargin()
}
