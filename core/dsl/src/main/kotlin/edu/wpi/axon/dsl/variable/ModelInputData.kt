package edu.wpi.axon.dsl.variable

/**
 * A type of [FileInputData] which is meant to be given as the first layer's input in an ONNX model.
 */
abstract class ModelInputData(name: String) : FileInputData(name) {

    /**
     * This code is meant to be a snippet that can be pasted directly into the function parameter
     * for the ONNX model inputs.
     *
     * @param sessionInputsVariableName The name of the variable containing the model's inputs.
     */
    abstract fun codeForModelInput(sessionInputsVariableName: String): String
}
