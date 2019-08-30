package edu.wpi.axon.dsl

/**
 * An import statement.
 *
 * TODO: Make a DSL to write Imports like:
 * ```kotlin
 * Import { from "axon" import "preprocessYolov3" }
 * Import { import "numpy" as "np" }
 * ```
 */
sealed class Import {

    /**
     * This method must be pure.
     *
     * @return The code for this [Import].
     */
    abstract fun code(): String

    data class ModuleOnly(val module: String) : Import() {
        override fun code() = "import $module"
    }

    data class ModuleAndIdentifier(val module: String, val identifier: String) : Import() {
        override fun code() = "from $module import $identifier"
    }

    data class ModuleAndName(val module: String, val name: String) : Import() {
        override fun code() = "import $module as $name"
    }

    data class FullImport(val module: String, val identifier: String, val name: String) : Import() {
        override fun code() = "from $module import $identifier as $name"
    }
}
