package edu.wpi.axon.core.dsl

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.dsl.variable.Variable
import edu.wpi.axon.core.hasElementWhere
import edu.wpi.axon.core.isTrue
import org.junit.jupiter.api.Test

internal class DefaultVariableContainerTest {

    @Test
    fun `calling create with a name adds a new variable`() {
        val container = DefaultVariableContainer.of()
        container.create("varName", Variable::class)
        assertThat(container, hasElementWhere { name == "varName" })
    }

    @Test
    fun `calling create with a name and config adds a new variable and calls configure`() {
        val container = DefaultVariableContainer.of()

        var called = false
        val mockConfigure: Variable.() -> Unit = { called = true }

        container.create("varName", Variable::class, mockConfigure)

        assertThat(container, hasElementWhere { name == "varName" })
        assertThat(called, isTrue())
    }
}
