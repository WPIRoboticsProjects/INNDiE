package edu.wpi.axon.core.dsl.variable

import edu.wpi.axon.core.dsl.isValidPythonIdentifier
import edu.wpi.axon.core.dsl.task.InferenceTask
import edu.wpi.axon.core.dsl.task.TaskInput
import java.nio.file.InvalidPathException
import java.nio.file.Paths

class InferenceSession(
    override val name: String
) : Variable, TaskInput<InferenceTask> {

    var modelPath: String = ""

    override fun isConfiguredCorrectly() =
        isValidPythonIdentifier(name) && isValidPathName(modelPath)

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
