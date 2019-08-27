package edu.wpi.axon.core.dsl.validator.path

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.isFalse
import edu.wpi.axon.core.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

internal class DefaultPathValidatorTest {

    private val validator = DefaultPathValidator()

    @Test
    fun `path name must not be empty`() {
        assertThat(
            validator.isValidPathName(""),
            isFalse()
        )
    }

    @Test
    fun `path name must be valid`() {
        assertThat(
            validator.isValidPathName("//"),
            isFalse()
        )
    }

    @Test
    fun `path name must be a valid file`(@TempDir tempDir: File) {
        assertThat(
            validator.isValidPathName(tempDir.absolutePath),
            isFalse()
        )
    }

    @Test
    fun `valid path`(@TempDir tempDir: File) {
        val mockModelFile = Paths.get(tempDir.absolutePath, "mockModel.onnx").toFile().apply {
            createNewFile()
        }

        assertThat(
            validator.isValidPathName(mockModelFile.absolutePath),
            isTrue()
        )
    }
}
