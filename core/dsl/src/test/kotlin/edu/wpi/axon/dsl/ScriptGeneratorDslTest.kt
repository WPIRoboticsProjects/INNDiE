package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

@Suppress("UNUSED_VARIABLE")
internal class ScriptGeneratorDslTest {

    @Test
    fun `create a new variable`() {
        val mockVariableContainer = mockk<PolymorphicNamedDomainObjectContainer<Variable>> {
            every { create("session", MockVariable::class, any()) } returns mockk {
                every { name } returns "session"
            }
        }

        ScriptGeneratorDsl(mockVariableContainer, mockk()) {
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

        ScriptGeneratorDsl(mockk(), mockTaskContainer) {
            val task1 by tasks.running(MockTask::class)
        }

        verify { mockTaskContainer.create("task1", MockTask::class, any()) }
        confirmVerified(mockTaskContainer)
    }
}