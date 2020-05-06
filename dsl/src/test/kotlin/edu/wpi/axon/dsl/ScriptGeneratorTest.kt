package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.inndie.testutil.KoinTestFixture
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
}
