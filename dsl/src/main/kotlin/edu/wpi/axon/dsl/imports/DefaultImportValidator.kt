package edu.wpi.axon.dsl.imports

import arrow.core.Invalid
import arrow.core.Nel
import arrow.core.None
import arrow.core.Some
import arrow.core.Valid
import arrow.core.Validated

class DefaultImportValidator : ImportValidator {

    override fun validateImports(imports: Set<Import>): Validated<Nel<Import>, Set<Import>> {
        val invalidImports = imports.filter { it.isInvalid() }
        return when (val nel = Nel.fromList(invalidImports)) {
            is Some -> Invalid(nel.t)
            is None -> Valid(imports)
        }
    }

    private fun Import.isInvalid() = components.any { it.contains(Regex("\\s")) }
}
