package edu.wpi.inndie.dsl.validator.path

import java.nio.file.InvalidPathException
import java.nio.file.Paths

/**
 * Validates file paths for the default file system.
 */
class DefaultPathValidator : PathValidator {

    @SuppressWarnings("SwallowedException")
    override fun isValidPathName(pathName: String): Boolean {
        if (pathName.isBlank()) {
            return false
        }

        return try {
            val file = Paths.get(pathName).toFile()
            !(file.exists() && file.isDirectory)
        } catch (ex: InvalidPathException) {
            false
        }
    }
}
