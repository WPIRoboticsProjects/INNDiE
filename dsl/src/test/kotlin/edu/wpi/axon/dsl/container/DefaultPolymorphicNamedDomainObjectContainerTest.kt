package edu.wpi.axon.dsl.container

import edu.wpi.axon.dsl.MockVariable
import edu.wpi.axon.dsl.mockVariableNameValidator
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.matchers.boolean.shouldBeTrue
import io.kotlintest.matchers.maps.shouldContainKey
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.dsl.module

internal class DefaultPolymorphicNamedDomainObjectContainerTest : KoinTestFixture() {

    private val varName = "varName"

    @Test
    fun `calling create with a name adds a new variable`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()
        container.create(varName, MockVariable::class)
        container.shouldContainKey(varName)
    }

    @Test
    fun `calling create with a name and config adds a new variable and calls configure`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()

        var called = false
        val mockConfigure: Variable.() -> Unit = { called = true }

        container.create(varName, MockVariable::class, mockConfigure)

        container.shouldContainKey(varName)
        called.shouldBeTrue()
    }

    @Test
    fun `calling create with two variables of the same name throws an error`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()
        container.create(varName, MockVariable::class)
        assertThrows<IllegalArgumentException> {
            container.create(varName, MockVariable::class)
        }
    }

    @Test
    fun `cannot create a variable from an abstract class`() {
        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()

        abstract class AbstractVariable(name: String) : Variable(name)

        assertThrows<IllegalArgumentException> {
            container.create(varName, AbstractVariable::class)
        }
    }

    @Test
    fun `cannot create a variable from a companion object`() {
        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()

        assertThrows<IllegalArgumentException> {
            container.create(varName, TestVariable.Companion::class)
        }
    }

    @Test
    fun `cannot create a variable without a matching constructor`() {
        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()

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
