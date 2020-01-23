@file:SuppressWarnings("LargeClass")

package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.task.EmptyBaseTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.assertions.arrow.nel.shouldHaveSize
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.string.shouldContainInOrder
import io.kotlintest.shouldBe
import java.util.concurrent.CountDownLatch
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

@Suppress("UNUSED_VARIABLE")
internal class ScriptGeneratorIntegrationTest : KoinTestFixture() {

    @Test
    fun `code dependencies should be called`() {
        startKoin {
            modules(defaultBackendModule())
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
            modules(defaultBackendModule())
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
            modules(defaultBackendModule())
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
            modules(defaultBackendModule())
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
            modules(defaultBackendModule())
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

        scriptGenerator.code().shouldBeInvalid { (nel) ->
            nel.shouldHaveSize(1)
        }
    }

    @Test
    fun `a required variable as an input does not cause a task to be generated`() {
        startKoin {
            modules(defaultBackendModule())
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

        scriptGenerator.code().shouldBeInvalid()
    }

    @Test
    fun `a required variable as an output causes a task to be generated`() {
        startKoin {
            modules(defaultBackendModule())
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
                // This should be generated because it is the lastTask AND generation will happen
                // because there is a required variable
                code = "task2"
            }
            lastTask = task2
            requireGeneration(var1)
        }

        scriptGenerator.code().shouldBeValid { (code) ->
            code.shouldContainInOrder("task1", "task2")
        }
    }

    @Test
    fun `a pregeneration task runs before a normal task`() {
        startKoin {
            modules(defaultBackendModule())
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 = tasks.run(MockTask::class) {
                code = "task1"
            }
            pregenerationLastTask = task1

            val var1 by variables.creating(MockVariable::class)
            val task2 = tasks.run(MockTask::class) {
                code = "task2"
                outputs += var1
            }
            lastTask = task2
            requireGeneration(var1)
        }

        scriptGenerator.code().shouldBeValid { (code) ->
            code.shouldContainInOrder("task1", "task2")
        }
    }

    @Test
    fun `generate code with an incorrectly configured task`() {
        startKoin {
            modules(defaultBackendModule())
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class) {
                configuredCorrectly = false
            }
            lastTask = task1
        }

        scriptGenerator.code().shouldBeInvalid { (nel) ->
            nel.shouldHaveSize(1)
        }
    }

    @Test
    fun `task that depends on the lastTask`() {
        startKoin {
            modules(defaultBackendModule())
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val task1 by tasks.running(MockTask::class)
            val task2 by tasks.running(MockTask::class) {
                dependencies += task1
            }
            lastTask = task1
        }

        scriptGenerator.code().shouldBeInvalid { (nel) ->
            nel.shouldHaveSize(1)
        }
    }
}
