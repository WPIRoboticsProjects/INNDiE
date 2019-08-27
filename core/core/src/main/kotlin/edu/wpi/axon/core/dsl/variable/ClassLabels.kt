package edu.wpi.axon.core.dsl.variable

// TODO: Need to detect the format of the labels or maybe ask the user what it is in the UI
class ClassLabels(
    override val name: String,
    override var path: String
) : InputData {

    override fun isConfiguredCorrectly(): Boolean {
        TODO("not implemented")
    }
}
