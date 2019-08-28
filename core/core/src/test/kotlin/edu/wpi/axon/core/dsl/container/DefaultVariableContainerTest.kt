package edu.wpi.axon.core.dsl.container

import com.natpryce.hamkrest.assertion.assertThat
import edu.wpi.axon.core.dsl.MockVariable
import edu.wpi.axon.core.dsl.mockVariableNameValidator
import edu.wpi.axon.core.dsl.variable.Variable
import edu.wpi.axon.core.hasElementWhere
import edu.wpi.axon.core.isTrue
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.KoinTest

internal class DefaultVariableContainerTest : KoinTest {

    private val varName = "varName"

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `calling create with a name adds a new variable`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultVariableContainer.of()
        container.create(varName, MockVariable::class)
        assertThat(container, hasElementWhere { name == varName })
    }

    @Test
    fun `calling create with a name and config adds a new variable and calls configure`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultVariableContainer.of()

        var called = false
        val mockConfigure: Variable.() -> Unit = { called = true }

        container.create(varName, MockVariable::class, mockConfigure)

        assertThat(container, hasElementWhere { name == varName })
        assertThat(called, isTrue())
    }

    @Test
    fun `cannot create a variable from an abstract class`() {
        val container = DefaultVariableContainer.of()

        abstract class AbstractVariable(name: String) : Variable(name)

        assertThrows<IllegalArgumentException> {
            container.create(varName, AbstractVariable::class)
        }
    }

    @Test
    fun `cannot create a variable from a companion object`() {
        val container = DefaultVariableContainer.of()

        assertThrows<IllegalArgumentException> {
            container.create(varName, TestVariable::class)
        }
    }

    @Test
    fun `cannot create a variable without a matching constructor`() {
        val container = DefaultVariableContainer.of()

        assertThrows<IllegalArgumentException> {
            container.create(varName, TestVariable::class)
        }
    }

    class TestVariable(
        name: String,
        @Suppress("UNUSED_PARAMETER") anotherParameter: Int
    ) : Variable(name) {
        companion object : Variable("companionName")
    }
}
