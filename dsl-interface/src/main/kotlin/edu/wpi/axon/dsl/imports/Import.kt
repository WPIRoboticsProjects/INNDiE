package edu.wpi.axon.dsl.imports

import arrow.core.Some
import edu.wpi.inndie.patternmatch.Variable
import edu.wpi.inndie.patternmatch.match

/**
 * An import statement.
 *
 * @param components The component parts of the import (not including syntax).
 */
sealed class Import(val components: Set<String>) {

    /**
     * This method must be pure.
     *
     * @return The code for this [Import].
     */
    abstract fun code(): String

    data class ModuleOnly(val module: String) : Import(setOf(module)) {
        override fun code() = "import $module"
    }

    data class ModuleAndIdentifier(val module: String, val identifier: String) :
        Import(setOf(module, identifier)) {
        override fun code() = "from $module import $identifier"
    }

    data class ModuleAndName(val module: String, val name: String) : Import(setOf(module, name)) {
        override fun code() = "import $module as $name"
    }

    data class FullImport(val module: String, val identifier: String, val name: String) :
        Import(setOf(module, identifier, name)) {
        override fun code() = "from $module import $identifier as $name"
    }
}

/**
 * Makes an [Import] from the equivalent Python statement.
 *
 * @param import The Python import string.
 * @return The equivalent [Import].
 */
fun makeImport(import: String): Import {
    val components = import.split(Regex("\\s"))
    return (when (components.size) {
        2, 4, 6 -> match<List<String>, String, Import>(
            components
        ) {
            pattern("import", Variable) returns {
                Import.ModuleOnly(firstMatch())
            }

            pattern(
                "from",
                Variable,
                "import",
                Variable
            ) returns {
                Import.ModuleAndIdentifier(firstMatch(), secondMatch())
            }

            pattern(
                "import",
                Variable,
                "as",
                Variable
            ) returns {
                Import.ModuleAndName(firstMatch(), secondMatch())
            }

            pattern(
                "from",
                Variable,
                "import",
                Variable,
                "as",
                Variable
            ) returns {
                Import.FullImport(firstMatch(), secondMatch(), thirdMatch())
            }
        }

        else -> throw IllegalArgumentException("Invalid import string: $import")
    } as Some).t
}
