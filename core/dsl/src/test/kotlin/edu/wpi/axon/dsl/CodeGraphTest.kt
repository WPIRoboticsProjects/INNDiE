@file:SuppressWarnings("StringLiteralDuplication")

package edu.wpi.axon.dsl

import com.google.common.graph.EndpointPair
import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import io.kotlintest.matchers.equality.shouldBeEqualToUsingFields
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import kotlin.reflect.KClass

@Suppress("UnstableApiUsage")
internal class CodeGraphTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `adding one task creates one node`() {
        val codes = makeMockTasks("task1")

        val container = object : PolymorphicNamedDomainObjectContainer<AnyCode>,
            Map<String, AnyCode> by codes {
            override fun <U : AnyCode> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        graph.nodes().shouldBeEqualToUsingFields(codes.values)
    }

    @Test
    fun `adding a task with a dependency task adds two nodes and an edge`() {
        val codes = makeMockTasks("task1", "task2")
        codes["task1"]!!.dependencies += codes["task2"]!!

        val container = object : PolymorphicNamedDomainObjectContainer<AnyCode>,
            Map<String, AnyCode> by codes {
            override fun <U : AnyCode> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        graph.nodes().shouldBeEqualToUsingFields(codes.values)
        graph.edges().shouldBeEqualToUsingFields(
            setOf(EndpointPair.ordered(codes["task2"], codes["task1"]))
        )
    }

    @Test
    fun `add multiple tasks with multiple dependencies`() {
        val codes = makeMockTasks("task1", "task2")
        val task3 = MockTask("task3")
        val task4 = MockTask("task4")

        codes["task1"]!!.dependencies += codes["task2"]!!
        codes["task1"]!!.dependencies += task3
        codes["task2"]!!.dependencies += task3
        task3.dependencies += task4

        val container = object : PolymorphicNamedDomainObjectContainer<AnyCode>,
            Map<String, AnyCode> by codes {
            override fun <U : AnyCode> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        graph.nodes().shouldBeEqualToUsingFields(codes.values + task3 + task4)
        graph.edges().shouldBeEqualToUsingFields(
            setOf(
                EndpointPair.ordered(codes["task2"], codes["task1"]),
                EndpointPair.ordered(task3, codes["task1"]),
                EndpointPair.ordered(task3, codes["task2"]),
                EndpointPair.ordered(task4, task3)
            )
        )
    }

    @Test
    fun `circular dependencies are not allowed`() {
        val codes = makeMockTasks("task1", "task2")
        codes["task1"]!!.dependencies += codes["task2"]!!
        codes["task2"]!!.dependencies += codes["task1"]!!

        val container = object : PolymorphicNamedDomainObjectContainer<AnyCode>,
            Map<String, AnyCode> by codes {
            override fun <U : AnyCode> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container)
        assertThrows<IllegalArgumentException> { graph.graph }
    }

    @Test
    fun `tasks connected by a variable should have an edge`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator("variable1" to true) }
            })
        }

        val codes = makeMockTasks("task1", "task2")
        val variable1 = MockVariable("variable1")
        codes["task1"]!!.outputs += variable1
        codes["task2"]!!.inputs += variable1

        val container = object : PolymorphicNamedDomainObjectContainer<AnyCode>,
            Map<String, AnyCode> by codes {
            override fun <U : AnyCode> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        graph.nodes().shouldBeEqualToUsingFields(codes.values)
        graph.edges().shouldBeEqualToUsingFields(
            setOf(EndpointPair.ordered(codes["task1"], codes["task2"]))
        )
    }

    @Test
    fun `tasks connected by a variable cannot form a circuit`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator("variable1" to true) }
            })
        }

        val codes = makeMockTasks("task1", "task2")
        val variable1 = MockVariable("variable1")
        codes["task1"]!!.dependencies += codes["task2"]!!
        codes["task1"]!!.outputs += variable1
        codes["task2"]!!.inputs += variable1

        val container = object : PolymorphicNamedDomainObjectContainer<AnyCode>,
            Map<String, AnyCode> by codes {
            override fun <U : AnyCode> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container)
        assertThrows<IllegalArgumentException> { graph.graph }
    }

    private fun makeMockTasks(vararg names: String) =
        names.map { it to MockTask(it) }.toMap().toMutableMap()
}
