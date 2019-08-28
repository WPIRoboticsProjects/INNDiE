package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.validator.path.PathValidator
import org.koin.core.KoinComponent
import org.koin.core.inject

abstract class InputData(name: String) : Variable(name), Code, KoinComponent {

    /**
     * The file path to load this data from.
     */
    var path: String? = null

    /**
     * Validates the [path].
     */
    val pathValidator: PathValidator by inject()

    /**
     * The import dependencies for this component.
     */
    abstract val imports: Set<Import>

    override fun isConfiguredCorrectly() =
        super.isConfiguredCorrectly() && path != null && pathValidator.isValidPathName(path!!)
}
