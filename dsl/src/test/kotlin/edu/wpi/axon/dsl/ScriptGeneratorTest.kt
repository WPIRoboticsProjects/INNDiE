package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.EmptyBaseTask
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.assertions.arrow.nel.shouldHaveSize
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.dsl.module

@Suppress("UNUSED_VARIABLE")
internal class ScriptGeneratorTest : KoinTestFixture() {

    @Test
    fun `create a new variable`() {
        startKoin {
            modules(module {
                mockVariableNameGenerator()
            })
        }

        val mockVariableContainer = mockk<PolymorphicNamedDomainObjectContainer<Variable>> {
            every { create("var1", MockVariable::class, any()) } returns mockk {
                every { name } returns "var1"
            }
        }

        ScriptGenerator(mockVariableContainer, mockk()) {
            val session by variables.creating(MockVariable::class)
        }

        verify { mockVariableContainer.create("var1", MockVariable::class, any()) }
        confirmVerified(mockVariableContainer)
    }

    @Test
    fun `create a new task`() {
        startKoin {
            modules(module {
                mockVariableNameGenerator()
            })
        }

        val mockTaskContainer = mockk<PolymorphicNamedDomainObjectContainer<Task>> {
            every { create("var1", MockTask::class, any()) } returns mockk()
        }

        ScriptGenerator(mockk(), mockTaskContainer) {
            val task1 by tasks.running(MockTask::class)
        }

        verify { mockTaskContainer.create("var1", MockTask::class, any()) }
        confirmVerified(mockTaskContainer)
    }

    @Test
    fun `generate code with an incorrectly configured task`() {
        startKoin {
            modules(module {
                mockVariableNameGenerator()
            })
        }

        val mockVariableContainer = mockk<PolymorphicNamedDomainObjectContainer<Variable>> {
            every { values } returns emptyList()
        }

        val mockTaskContainer = mockk<PolymorphicNamedDomainObjectContainer<Task>> {
            every { create("var1", MockTask::class, any()) } returns configuredIncorrectly()
            every {
                create("var2", EmptyBaseTask::class, any())
            } returns configuredCorrectly()
            every { values } returns listOf(configuredIncorrectly())
        }

        val scriptGenerator = ScriptGenerator(mockVariableContainer, mockTaskContainer) {
            val task1 by tasks.running(MockTask::class)
        }

        scriptGenerator.code().shouldBeInvalid { (nel) ->
            nel.shouldHaveSize(1)
        }

        verify { mockVariableContainer.values }
        verify { mockTaskContainer.create("var1", MockTask::class, any()) }
        verify { mockTaskContainer.create("var2", EmptyBaseTask::class, any()) }
        verify { mockTaskContainer.values }
        confirmVerified(mockVariableContainer, mockTaskContainer)
    }
}
