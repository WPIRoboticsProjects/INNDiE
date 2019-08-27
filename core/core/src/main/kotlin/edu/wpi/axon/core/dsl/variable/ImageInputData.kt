package edu.wpi.axon.core.dsl.variable

class ImageInputData(
    override val name: String,
    override var path: String
) : InputData {

    override fun isConfiguredCorrectly(): Boolean {
        TODO("not implemented")
    }
}
