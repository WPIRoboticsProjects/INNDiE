package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import

/**
 * Loads the labels for object classes.
 *
 * TODO: Need to detect the format of the labels or maybe ask the user what it is in the UI
 */
class ClassLabels(name: String) : FileInputData(name) {

    override val imports: Set<Import> = emptySet()

    override val inputs: Set<Variable> = emptySet()

    override val outputs: Set<Variable> = emptySet()

    override val dependencies: Set<Code<*>> = emptySet()

    override fun code(): String {
        TODO("not implemented")
    }
}
