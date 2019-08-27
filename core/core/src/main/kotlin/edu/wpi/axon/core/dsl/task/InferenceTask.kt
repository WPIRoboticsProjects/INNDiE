package edu.wpi.axon.core.dsl.task

import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.dsl.variable.InputData

class InferenceTask : Task {

    var input: InputData? = null
    var inferenceSession: InferenceSession? = null

    override fun isConfiguredCorrectly(): Boolean {
        TODO("not implemented")
    }
}
