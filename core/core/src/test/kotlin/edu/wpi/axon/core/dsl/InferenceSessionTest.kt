package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.isFalse
import edu.wpi.axon.core.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Paths

internal class InferenceSessionTest {

    @Test
    fun `name must be specified`() {
        assertThat(InferenceSession("").isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `name must not contain whitespace`() {
        assertThat(InferenceSession("n me").isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `name must not contain special characters`() {
        assertThat(InferenceSession("n*me").isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `path name must be configured`() {
        assertThat(
            InferenceSession("name").isConfiguredCorrectly(),
            isFalse()
        )
    }

    @Test
    fun `path name must not be empty`() {
        val session = InferenceSession("name").apply { modelPath = "" }
        assertThat(session.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `path name must be valid`() {
        val session = InferenceSession("name").apply { modelPath = "" }
        assertThat(session.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `path name must be a valid file`(@TempDir tempDir: File) {
        val session = InferenceSession("name").apply {
            modelPath = tempDir.absolutePath // A directory is not a valid file
        }

        assertThat(session.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `correctly configured session`(@TempDir tempDir: File) {
        val mockModelFile = Paths.get(tempDir.absolutePath, "mockModel.onnx").toFile().apply {
            createNewFile()
        }

        val session = InferenceSession("name").apply {
            modelPath = mockModelFile.absolutePath
        }

        assertThat(session.isConfiguredCorrectly(), isTrue())
    }
}
