package edu.wpi.axon.dsl.imports

/**
 * An import statement.
 *
 * @param components The component parts of the import (not including syntax).
 *
 * TODO: Make a DSL to write Imports like:
 * ```kotlin
 * Import { from "axon" import "preprocessYolov3" }
 * Import { import "numpy" as "np" }
 * ```
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
