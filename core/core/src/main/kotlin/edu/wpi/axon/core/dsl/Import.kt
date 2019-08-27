package edu.wpi.axon.core.dsl

sealed class Import {
    data class ModuleOnly(val module: String) : Import()
    data class ModuleAndIdentifier(val module: String, val identifier: String) : Import()
    data class ModuleAndName(val module: String, val name: String) : Import()
    data class FullImport(val module: String, val identifier: String, val name: String) : Import()
}
