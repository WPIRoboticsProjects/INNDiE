package edu.wpi.axon.core.dsl

import com.google.common.graph.EndpointPair
import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainer
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
        val codes = mutableMapOf(
            "task1" to MockTask("task1")
        )

        val container = object : PolymorphicNamedDomainObjectContainer<Code<Code<*>>>,
            Map<String, Code<Code<*>>> by codes {
            override fun <U : Code<Code<*>>> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        assertThat(graph.nodes(), equalTo(codes.values.toSet<Code<Code<*>>>()))
    }

    @Test
    fun `adding a task with a dependency task adds two nodes and an edge`() {
        val codes = mutableMapOf(
            "task1" to MockTask("task1"),
            "task2" to MockTask("task2")
        )

        codes["task1"]!!.dependencies += codes["task2"]!!

        val container = object : PolymorphicNamedDomainObjectContainer<Code<Code<*>>>,
            Map<String, Code<Code<*>>> by codes {
            override fun <U : Code<Code<*>>> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        assertThat(graph.nodes(), equalTo(codes.values.toSet<Code<Code<*>>>()))
        assertThat(
            graph.edges(),
            equalTo(
                setOf<EndpointPair<Code<Code<*>>>>(
                    EndpointPair.ordered(codes["task2"], codes["task1"])
                )
            )
        )
    }

    @Test
    fun `add multiple tasks with multiple dependencies`() {
        val codes = mutableMapOf(
            "task1" to MockTask("task1"),
            "task2" to MockTask("task2")
        )

        val task3 = MockTask("task3")
        val task4 = MockTask("task4")

        codes["task1"]!!.dependencies += codes["task2"]!!
        codes["task1"]!!.dependencies += task3
        codes["task2"]!!.dependencies += task3
        task3.dependencies += task4

        val container = object : PolymorphicNamedDomainObjectContainer<Code<Code<*>>>,
            Map<String, Code<Code<*>>> by codes {
            override fun <U : Code<Code<*>>> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        assertThat(graph.nodes(), equalTo(codes.values.toSet<Code<Code<*>>>() + task3 + task4))
        assertThat(
            graph.edges(),
            equalTo(
                setOf<EndpointPair<Code<Code<*>>>>(
                    EndpointPair.ordered(codes["task2"], codes["task1"]),
                    EndpointPair.ordered(task3, codes["task1"]),
                    EndpointPair.ordered(task3, codes["task2"]),
                    EndpointPair.ordered(task4, task3)
                )
            )
        )
    }

    @Test
    fun `circular dependencies are not allowed`() {
        val codes = mutableMapOf(
            "task1" to MockTask("task1"),
            "task2" to MockTask("task2")
        )

        codes["task1"]!!.dependencies += codes["task2"]!!
        codes["task2"]!!.dependencies += codes["task1"]!!

        val container = object : PolymorphicNamedDomainObjectContainer<Code<Code<*>>>,
            Map<String, Code<Code<*>>> by codes {
            override fun <U : Code<Code<*>>> create(
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

        val codes = mutableMapOf(
            "task1" to MockTask("task1"),
            "task2" to MockTask("task2")
        )

        val variable1 = MockVariable("variable1")

        codes["task1"]!!.outputs += variable1
        codes["task2"]!!.inputs += variable1

        val container = object : PolymorphicNamedDomainObjectContainer<Code<Code<*>>>,
            Map<String, Code<Code<*>>> by codes {
            override fun <U : Code<Code<*>>> create(
                name: String,
                type: KClass<U>,
                configure: (U.() -> Unit)?
            ): U {
                TODO("not implemented")
            }
        }

        val graph = CodeGraph(container).graph

        assertThat(graph.nodes(), equalTo(codes.values.toSet<Code<Code<*>>>()))
        assertThat(
            graph.edges(),
            equalTo(
                setOf<EndpointPair<Code<Code<*>>>>(
                    EndpointPair.ordered(codes["task1"], codes["task2"])
                )
            )
        )
    }

    @Test
    fun `tasks connected by a variable cannot form a circuit`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator("variable1" to true) }
            })
        }

        val codes = mutableMapOf(
            "task1" to MockTask("task1"),
            "task2" to MockTask("task2")
        )

        val variable1 = MockVariable("variable1")

        codes["task1"]!!.dependencies += codes["task2"]!!
        codes["task1"]!!.outputs += variable1
        codes["task2"]!!.inputs += variable1

        val container = object : PolymorphicNamedDomainObjectContainer<Code<Code<*>>>,
            Map<String, Code<Code<*>>> by codes {
            override fun <U : Code<Code<*>>> create(
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
}
