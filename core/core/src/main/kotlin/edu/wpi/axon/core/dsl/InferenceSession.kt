package edu.wpi.axon.core.dsl

import java.nio.file.InvalidPathException
import java.nio.file.Paths

class InferenceSession(
    override val name: String
) : Variable {

    var modelPathName: String = ""

    override fun isConfiguredCorrectly() =
        isValidPythonIdentifier(name) && isValidPathName(modelPathName)

    @SuppressWarnings("SwallowedException")
    private fun isValidPathName(pathName: String): Boolean {
        if (pathName.isBlank()) {
            return false
        }

        return try {
            val file = Paths.get(pathName).toFile()
            file.isFile
        } catch (ex: InvalidPathException) {
            false
        }
    }
}
