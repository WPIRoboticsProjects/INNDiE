package edu.wpi.axon.dsl.task

import arrow.data.Invalid
import arrow.data.Nel
import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.dsl.Code
import edu.wpi.axon.dsl.alwaysValidImportValidator
import edu.wpi.axon.dsl.configuredIncorrectly
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.ImportValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.isFalse
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

internal class BaseTaskTest : KoinTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `an invalid input means the task is invalid`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = stubTask(inputs = setOf(configuredIncorrectly()))
        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `an invalid output means the task is invalid`() {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
            })
        }

        val task = stubTask(outputs = setOf(configuredIncorrectly()))
        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    @Test
    fun `an invalid import means the task is invalid`() {
        val imports = setOf(Import.ModuleOnly(""))

        val mockImportValidator = mockk<ImportValidator> {
            every { validateImports(imports) } returns
                Invalid(Nel.fromListUnsafe(imports.toList()))
        }

        startKoin {
            modules(module {
                single { mockImportValidator }
            })
        }

        val task = stubTask(imports = imports)
        assertThat(task.isConfiguredCorrectly(), isFalse())
    }

    private fun stubTask(
        name: String = "task1",
        imports: Set<Import> = setOf(),
        inputs: Set<Variable> = setOf(),
        outputs: Set<Variable> = setOf(),
        dependencies: Set<Code<*>> = setOf(),
        code: String = ""
    ): Task = object : BaseTask(name) {
        override val imports: Set<Import> = imports
        override val inputs: Set<Variable> = inputs
        override val outputs: Set<Variable> = outputs
        override val dependencies: Set<Code<*>> = dependencies
        override fun code() = code
    }
}
