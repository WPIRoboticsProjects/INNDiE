package edu.wpi.axon.dsl

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.imports.DefaultImportValidator
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.imports.ImportValidator
import edu.wpi.axon.dsl.validator.path.DefaultPathValidator
import edu.wpi.axon.dsl.validator.path.PathValidator
import edu.wpi.axon.dsl.validator.variablename.PythonVariableNameValidator
import edu.wpi.axon.dsl.validator.variablename.VariableNameValidator
import edu.wpi.axon.dsl.variable.Variable
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest
import java.util.concurrent.CountDownLatch

@Suppress("UNUSED_VARIABLE")
@SuppressWarnings("LargeClass")
internal class ScriptGeneratorIntegrationTest : KoinTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `code dependencies should be called`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val codeLatch = CountDownLatch(2)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                latch = codeLatch
            }

            val task2 by tasks.running(MockTask::class) {
                latch = codeLatch
                dependencies += task1
            }

            lastTask = task2
        }.code()

        assertThat(codeLatch.count, equalTo(0L))
    }

    @Test
    fun `tasks are not run multiple times`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val codeLatch = CountDownLatch(3)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                latch = codeLatch
            }

            val task2 by tasks.running(MockTask::class) {
                latch = codeLatch
                dependencies += task1
            }

            lastTask = task2
        }.code()

        assertThat(codeLatch.count, equalTo(1L))
    }

    @Test
    fun `two tasks that depend on the same task does not duplicate code gen`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val task1CodeLatch = CountDownLatch(2)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                latch = task1CodeLatch
            }
            val task2 by tasks.running(MockTask::class) {
                dependencies += task1
            }
            val task3 by tasks.running(MockTask::class) {
                dependencies += task1
            }
            lastTask = task3
        }.code()

        assertThat(task1CodeLatch.count, equalTo(1L))
    }

    @Test
    fun `two tasks that depend on the same task linked by a variable does not duplicate code gen`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
            })
        }

        val task1CodeLatch = CountDownLatch(2)
        ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1Var by variables.creating(Variable::class)
            val task1 by tasks.running(MockTask::class) {
                latch = task1CodeLatch
                outputs += task1Var
            }

            val task2 by tasks.running(MockTask::class) {
                inputs += task1Var
            }

            val task3 by tasks.running(MockTask::class) {
                inputs += task1Var
                dependencies += task2
            }

            lastTask = task3
        }.code()

        assertThat(task1CodeLatch.count, equalTo(1L))
    }

    @Test
    fun `a task with invalid imports makes the script invalid`() {
        startKoin {
            modules(module {
                single<VariableNameValidator> { PythonVariableNameValidator() }
                single<PathValidator> { DefaultPathValidator() }
                single<ImportValidator> { DefaultImportValidator() }
            })
        }

        val script = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(EmptyBaseTask::class) {
                imports += Import.ModuleOnly("spaces in name")
            }

            lastTask = task1
        }

        assertThrows<IllegalArgumentException> { script.code() }
    }
}
