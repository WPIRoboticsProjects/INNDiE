package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.Variable
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class ScriptGeneratorDslTest {

    @Test
    fun `create a new variable`() {
        val mockVariableContainer = mockk<PolymorphicNamedDomainObjectContainer<Variable>> {
            every { create("session", MockVariable::class, any()) } returns mockk {
                every { name } returns "session"
            }
        }

        ScriptGeneratorDsl(mockVariableContainer, mockk()) {
            @Suppress("UNUSED_VARIABLE")
            val session by variables.creating(MockVariable::class)
        }

        verify { mockVariableContainer.create("session", MockVariable::class, any()) }
        confirmVerified(mockVariableContainer)
    }

    @Test
    fun `create a new task`() {
        val mockTaskContainer = mockk<PolymorphicDomainObjectContainer<Task>> {
            every { create(MockTask::class, any()) } returns mockk()
        }

        ScriptGeneratorDsl(mockk(), mockTaskContainer) {
            @Suppress("UNUSED_VARIABLE")
            val task1 by tasks.running(MockTask::class)
        }

        verify { mockTaskContainer.create(MockTask::class, any()) }
        confirmVerified(mockTaskContainer)
    }
}
