package edu.wpi.inndie.dsl.container

import edu.wpi.inndie.dsl.Code
import edu.wpi.inndie.dsl.MockVariable
import edu.wpi.inndie.dsl.imports.Import
import edu.wpi.inndie.dsl.mockVariableNameGenerator
import edu.wpi.inndie.dsl.mockVariableNameValidator
import edu.wpi.inndie.dsl.runExactlyOnce
import edu.wpi.inndie.dsl.task.BaseTask
import edu.wpi.inndie.dsl.task.Task
import edu.wpi.inndie.dsl.variable.Variable
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.matchers.booleans.shouldBeTrue
import io.kotlintest.matchers.maps.shouldContainExactly
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
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()

        abstract class AbstractVariable(name: String) : Variable(name)

        assertThrows<IllegalArgumentException> {
            container.create(varName, AbstractVariable::class)
        }
    }

    @Test
    fun `cannot create a variable without a matching constructor`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator(varName to true) }
            })
        }

        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Variable>()

        assertThrows<IllegalArgumentException> {
            container.create(varName, TestVariable::class)
        }
    }

    @Test
    fun `run exactly once called twice`() {
        startKoin {
            modules(module {
                mockVariableNameGenerator()
            })
        }

        val container = DefaultPolymorphicNamedDomainObjectContainer.of<Task>()

        val task = container.runExactlyOnce(MockTask::class)
        container.runExactlyOnce(MockTask::class)
        container.shouldContainExactly(mapOf("var1" to task))
    }

    class TestVariable(
        name: String,
        @Suppress("UNUSED_PARAMETER") anotherParameter: Int
    ) : Variable(name)

    data class MockTask(override val name: String) : BaseTask(name) {
        override val imports: Set<Import> = emptySet()
        override val inputs: Set<Variable> = emptySet()
        override val outputs: Set<Variable> = emptySet()
        override val dependencies: MutableSet<Code<*>> = mutableSetOf()
        override fun code() = ""
    }
}
