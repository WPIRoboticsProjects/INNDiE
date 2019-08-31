package edu.wpi.axon.dsl

import arrow.data.Nel
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.shouldBe
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

        codeLatch.count shouldBe 0
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

        codeLatch.count shouldBe 1
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

        task1CodeLatch.count shouldBe 1
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

        task1CodeLatch.count shouldBe 1
    }

    @Test
    fun `a task with invalid imports makes the script invalid`() {
        startKoin {
            modules(defaultModule())
        }

        val badImport = Import.ModuleOnly("spaces in name")
        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(EmptyBaseTask::class) {
                imports += badImport
            }

            lastTask = task1
        }

        scriptGenerator.code().shouldBeInvalid(
            Nel.just(EmptyBaseTask("task1").apply {
                imports += badImport
            })
        )
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

        scriptGenerator.code().shouldBeValid { (code) ->
            assertFalse(code.contains("task1"))
            assertTrue(code.contains("task2"))
        }
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

        scriptGenerator.code().shouldBeValid { (code) ->
            assertTrue(code.contains("task1"))
            assertTrue(code.contains("task2"))
        }
    }
}
