package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import

// TODO: Need to detect the format of the labels or maybe ask the user what it is in the UI
class ClassLabels(name: String) : InputData(name) {

    override val imports: Set<Import> = emptySet()

    override fun code(): String {
        TODO("not implemented")
    }
}
