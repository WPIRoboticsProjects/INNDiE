package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import io.kotlintest.assertions.arrow.nel.shouldHaveSize
import io.kotlintest.assertions.arrow.validation.shouldBeInvalid
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

@Suppress("UNUSED_VARIABLE")
internal class ScriptGeneratorTest {

    @Test
    fun `create a new variable`() {
        val mockVariableContainer = mockk<PolymorphicNamedDomainObjectContainer<Variable>> {
            every { create("session", MockVariable::class, any()) } returns mockk {
                every { name } returns "session"
            }
        }

        ScriptGenerator(mockVariableContainer, mockk()) {
            val session by variables.creating(MockVariable::class)
        }

        verify { mockVariableContainer.create("session", MockVariable::class, any()) }
        confirmVerified(mockVariableContainer)
    }

    @Test
    fun `create a new task`() {
        val mockTaskContainer = mockk<PolymorphicNamedDomainObjectContainer<Task>> {
            every { create("task1", MockTask::class, any()) } returns mockk()
        }

        ScriptGenerator(mockk(), mockTaskContainer) {
            val task1 by tasks.running(MockTask::class)
        }

        verify { mockTaskContainer.create("task1", MockTask::class, any()) }
        confirmVerified(mockTaskContainer)
    }

    @Test
    fun `generate code with an incorrectly configured task`() {
        val mockTask = mockk<MockTask> {
            every { isConfiguredCorrectly() } returns false
        }

        val mockVariableContainer = mockk<PolymorphicNamedDomainObjectContainer<Variable>> {
            every { values } returns emptyList()
        }

        val mockTaskContainer = mockk<PolymorphicNamedDomainObjectContainer<Task>> {
            every { create("task1", MockTask::class, any()) } returns mockTask
            every { values } returns listOf(mockTask)
        }

        val scriptGenerator = ScriptGenerator(mockVariableContainer, mockTaskContainer) {
            val task1 by tasks.running(MockTask::class)
        }

        scriptGenerator.code().shouldBeInvalid { (nel) ->
            nel.shouldHaveSize(1)
        }

        verify { mockTask.isConfiguredCorrectly() }
        verify { mockVariableContainer.values }
        verify { mockTaskContainer.create("task1", MockTask::class, any()) }
        verify { mockTaskContainer.values }
        confirmVerified(mockVariableContainer, mockTaskContainer)
    }
}
