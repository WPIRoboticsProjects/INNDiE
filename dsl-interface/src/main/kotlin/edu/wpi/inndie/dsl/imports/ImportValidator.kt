package edu.wpi.inndie.dsl.imports

import arrow.core.Nel
import arrow.core.Validated

/**
 * Validates sets of Imports.
 */
interface ImportValidator {

    /**
     * Validates that the [imports] do not conflict with each other.
     *
     * @param imports The imports to validate.
     * @return Either the original imports or a list of invalid imports.
     */
    fun validateImports(imports: Set<Import>): Validated<Nel<Import>, Set<Import>>
}
