package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.dsl.validator.path.PathValidator
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

    private val varName = "varName"
    private val pathName = "pathName"

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `invalid variable name but valid path`() {
        val mockVariableNameValidator = mockVariableNameValidator(varName to false)
        val mockPathValidator = mockPathValidator("pathName" to true)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockPathValidator }
            })
        }

        val session = InferenceSession(varName).apply {
            modelPath = "pathName"
        }

        assertThat(
            session.isConfiguredCorrectly(),
            isFalse()
        )

        verify { mockVariableNameValidator.isValidVariableName(varName) }
        verify(exactly = 0) { mockPathValidator.isValidPathName(any()) }
        confirmVerified(mockVariableNameValidator, mockPathValidator)
    }

    @Test
    fun `valid variable name but unconfigured path`() {
        val mockVariableNameValidator = mockVariableNameValidator(varName to true)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockk<PathValidator>() }
            })
        }

        assertThat(
            InferenceSession(varName).isConfiguredCorrectly(),
            isFalse()
        )

        verify { mockVariableNameValidator.isValidVariableName(varName) }
        confirmVerified(mockVariableNameValidator)
    }

    @Test
    fun `invalid path name`() {
        val mockVariableNameValidator = mockVariableNameValidator(varName to true)
        val mockPathValidator = mockPathValidator(pathName to false)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockPathValidator }
            })
        }

        val session = InferenceSession(
            varName
        ).apply {
            modelPath = pathName
        }

        assertThat(session.isConfiguredCorrectly(), isFalse())

        verify {
            mockVariableNameValidator.isValidVariableName(varName)
            mockPathValidator.isValidPathName(pathName)
        }
        confirmVerified(mockVariableNameValidator, mockPathValidator)
    }

    @Test
    fun `valid variable name and valid path name`() {
        val mockVariableNameValidator = mockVariableNameValidator(varName to true)
        val mockPathValidator = mockPathValidator(pathName to true)

        startKoin {
            modules(module {
                single { mockVariableNameValidator }
                single { mockPathValidator }
            })
        }

        val session = InferenceSession(
            varName
        ).apply {
            modelPath = pathName
        }

        assertThat(session.isConfiguredCorrectly(), isTrue())
        verify {
            mockVariableNameValidator.isValidVariableName(varName)
            mockPathValidator.isValidPathName(pathName)
        }
        confirmVerified(mockVariableNameValidator, mockPathValidator)
    }
}
