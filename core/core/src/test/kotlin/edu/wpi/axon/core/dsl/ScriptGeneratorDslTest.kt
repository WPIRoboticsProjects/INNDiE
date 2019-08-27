package edu.wpi.axon.core.dsl

import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class ScriptGeneratorDslTest {

    @Test
    fun `create a new variable`() {
        val mockVariableContainer = mockk<PolymorphicDomainObjectContainer<Variable>> {
            every { create("session", InferenceSession::class, any()) } returns mockk {
                every { name } returns "session"
            }
        }

        ScriptGeneratorDsl(mockVariableContainer) {
            @Suppress("UNUSED_VARIABLE")
            val session by variables.creating(InferenceSession::class) {
            }
        }

        verify { mockVariableContainer.create("session", InferenceSession::class, any()) }
        confirmVerified(mockVariableContainer)
    }
}
