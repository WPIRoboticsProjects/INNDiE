package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.isFalse
import edu.wpi.axon.core.isTrue
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class InferenceSessionTest {

    @Test
    fun `invalid variable name`() {
        val mockVariableNameValidator = mockk<VariableNameValidator> {
            every { isValidVariableName("name") } returns false
        }

        assertThat(
            InferenceSession(
                "name",
                mockVariableNameValidator,
                mockk()
            ).isConfiguredCorrectly(), isFalse()
        )

        verify { mockVariableNameValidator.isValidVariableName("name") }
        confirmVerified(mockVariableNameValidator)
    }

    @Test
    fun `valid variable name but unconfigured path`() {
        val mockVariableNameValidator = mockk<VariableNameValidator> {
            every { isValidVariableName("name") } returns true
        }

        assertThat(
            InferenceSession(
                "name",
                mockVariableNameValidator,
                mockk()
            ).isConfiguredCorrectly(),
            isFalse()
        )

        verify { mockVariableNameValidator.isValidVariableName("name") }
        confirmVerified(mockVariableNameValidator)
    }

    @Test
    fun `invalid path name`() {
        val mockVariableNameValidator = mockk<VariableNameValidator> {
            every { isValidVariableName("name") } returns true
        }

        val mockPathValidator = mockk<PathValidator> {
            every { isValidPathName("pathName") } returns false
        }

        val session = InferenceSession(
            "name",
            mockVariableNameValidator,
            mockPathValidator
        ).apply {
            modelPath = "pathName"
        }

        assertThat(session.isConfiguredCorrectly(), isFalse())

        verify {
            mockVariableNameValidator.isValidVariableName("name")
            mockPathValidator.isValidPathName("pathName")
        }
        confirmVerified(mockVariableNameValidator, mockPathValidator)
    }

    @Test
    fun `valid variable name and valid path name`() {
        val mockVariableNameValidator = mockk<VariableNameValidator> {
            every { isValidVariableName("name") } returns true
        }

        val mockPathValidator = mockk<PathValidator> {
            every { isValidPathName("pathName") } returns true
        }

        val session = InferenceSession(
            "name",
            mockVariableNameValidator,
            mockPathValidator
        ).apply {
            modelPath = "pathName"
        }

        assertThat(session.isConfiguredCorrectly(), isTrue())
        verify {
            mockVariableNameValidator.isValidVariableName("name")
            mockPathValidator.isValidPathName("pathName")
        }
        confirmVerified(mockVariableNameValidator, mockPathValidator)
    }
}
