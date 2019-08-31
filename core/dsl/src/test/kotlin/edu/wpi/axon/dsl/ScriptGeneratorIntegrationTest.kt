package edu.wpi.axon.dsl

import arrow.data.Valid
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.testutil.isInvalid
import edu.wpi.axon.testutil.isValid
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import java.util.concurrent.CountDownLatch

@Suppress("UNUSED_VARIABLE")
@SuppressWarnings("LargeClass")
internal class ScriptGeneratorIntegrationTest : KoinTestFixture() {

    @Test
    fun `code dependencies should be called`() {
        startKoin {
            modules(defaultModule())
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
            modules(defaultModule())
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
            modules(defaultModule())
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
            modules(defaultModule())
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
            modules(defaultModule())
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(EmptyBaseTask::class) {
                imports += Import.ModuleOnly("spaces in name")
            }

            lastTask = task1
        }

        assertThat(scriptGenerator.code(), isInvalid())
    }

    @Test
    fun `a required variable as an input does not cause a task to be generated`() {
        startKoin {
            modules(defaultModule())
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val var1 by variables.creating(MockVariable::class)
            val task1 by tasks.running(MockTask::class) {
                inputs += var1
                code = "task1"
            }
            val task2 by tasks.running(MockTask::class) {
                code = "task2"
            }
            lastTask = task2
            requireGeneration(var1)
        }

        val code = scriptGenerator.code()

        assertThat(code, isValid())
        code as Valid
        assertFalse(code.a.contains("task1"))
        assertTrue(code.a.contains("task2"))
    }

    @Test
    fun `a required variable as an output causes a task to be generated`() {
        startKoin {
            modules(defaultModule())
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val var1 by variables.creating(MockVariable::class)
            val task1 by tasks.running(MockTask::class) {
                outputs += var1
                code = "task1"
            }
            val task2 by tasks.running(MockTask::class) {
                code = "task2"
            }
            lastTask = task2
            requireGeneration(var1)
        }

        val code = scriptGenerator.code()

        assertThat(code, isValid())
        code as Valid
        assertTrue(code.a.contains("task1"))
        assertTrue(code.a.contains("task2"))
    }
}
