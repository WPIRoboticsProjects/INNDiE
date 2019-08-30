package edu.wpi.axon.dsl.imports

/**
 * Validates sets of Imports.
 */
interface ImportValidator {

    /**
     * Validates that the [imports] do not conflict with each other. If any conflict, they will
     * be fixed with a best-effort strategy.
     *
     * @param imports The imports to validate.
     * @return The (possibly) fixed set of imports.
     */
    fun validateAndFixImports(imports: Set<Import>): Set<Import>
}
