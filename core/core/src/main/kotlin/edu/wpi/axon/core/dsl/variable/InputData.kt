package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.PathValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class InputData(name: String) : Variable(name), KoinComponent {

    var path: String? = null

    val pathValidator: PathValidator by inject()

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && path != null && pathValidator.isValidPathName(path!!)
}