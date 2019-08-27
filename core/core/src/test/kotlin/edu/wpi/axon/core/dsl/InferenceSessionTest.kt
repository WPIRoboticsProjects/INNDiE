package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.dsl.variable.InferenceSession
import edu.wpi.axon.core.isFalse
import edu.wpi.axon.core.isTrue
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

internal class InferenceSessionTest : KoinTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `invalid variable name but valid path`() {
        val mockVariableNameValidator = mockVariableNameValidator("name" to false)
        val mockPathValidator = mockPathValidator("pathName" to true)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockPathValidator }
            })
        }

        val session = InferenceSession("name").apply {
            modelPath = "pathName"
        }

        assertThat(
            session.isConfiguredCorrectly(),
            isFalse()
        )

        verify { mockVariableNameValidator.isValidVariableName("name") }
        verify(exactly = 0) { mockPathValidator.isValidPathName(any()) }
        confirmVerified(mockVariableNameValidator, mockPathValidator)
    }

    @Test
    fun `valid variable name but unconfigured path`() {
        val mockVariableNameValidator = mockVariableNameValidator("name" to true)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockk<PathValidator>() }
            })
        }

        assertThat(
            InferenceSession("name").isConfiguredCorrectly(),
            isFalse()
        )

        verify { mockVariableNameValidator.isValidVariableName("name") }
        confirmVerified(mockVariableNameValidator)
    }

    @Test
    fun `invalid path name`() {
        val mockVariableNameValidator = mockVariableNameValidator("name" to true)
        val mockPathValidator = mockPathValidator("pathName" to false)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockPathValidator }
            })
        }

        val session = InferenceSession(
            "name"
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
        val mockVariableNameValidator = mockVariableNameValidator("name" to true)
        val mockPathValidator = mockPathValidator("pathName" to true)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockPathValidator }
            })
        }

        val session = InferenceSession(
            "name"
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
