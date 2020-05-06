package edu.wpi.inndie.dsl.validator.path

import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.matchers.booleans.shouldBeTrue
import java.io.File
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

internal class DefaultPathValidatorTest {

    private val validator = DefaultPathValidator()

    @Test
    fun `path name must not be empty`() {
        validator.isValidPathName("").shouldBeFalse()
    }

    @Test
    fun `path name must be valid`() {
        validator.isValidPathName("//").shouldBeFalse()
    }

    @Test
    fun `path name must be a valid file`(@TempDir tempDir: File) {
        validator.isValidPathName(tempDir.absolutePath).shouldBeFalse()
    }

    @Test
    fun `valid path`(@TempDir tempDir: File) {
        val mockModelFile = tempDir.toPath().resolve("mockModel.onnx").toFile().apply {
            createNewFile()
        }

        validator.isValidPathName(mockModelFile.absolutePath).shouldBeTrue()
    }

    @Test
    fun `relative path is valid`() {
        validator.isValidPathName("mockModel.onnx").shouldBeTrue()
    }
}
