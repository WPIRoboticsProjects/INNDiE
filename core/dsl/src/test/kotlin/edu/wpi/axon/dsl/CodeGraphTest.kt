@file:SuppressWarnings("StringLiteralDuplication")

package edu.wpi.axon.dsl

import com.google.common.graph.EndpointPair
import io.kotlintest.assertions.arrow.either.shouldBeLeft
import io.kotlintest.assertions.arrow.either.shouldBeRight
import io.kotlintest.matchers.collections.shouldBeEmpty
import io.kotlintest.matchers.equality.shouldBeEqualToUsingFields
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module

@Suppress("UnstableApiUsage")
internal class CodeGraphTest {

    @AfterEach
    fun afterEach() {
        stopKoin()
    }

    @Test
    fun `an empty container generates an empty graph`() {
        val graph = CodeGraph(mockContainer()).graph
        graph.shouldBeRight {
            it.nodes().shouldBeEmpty()
            it.edges().shouldBeEmpty()
        }
    }

    @Test
    fun `adding one task creates one node`() {
        val codes = makeMockTasks("task1")
        val container = mockContainer(codes)
        val graph = CodeGraph(container).graph

        graph.shouldBeRight {
            it.nodes().shouldBeEqualToUsingFields(codes.values)
        }
    }

    @Test
    fun `adding a task with a dependency task adds two nodes and an edge`() {
        val codes = makeMockTasks("task1", "task2")
        val task1 = codes["task1"]!!
        val task2 = codes["task2"]!!
        task1.dependencies += task2

        val container = mockContainer(codes)
        val graph = CodeGraph(container).graph

        graph.shouldBeRight {
            it.nodes().shouldBeEqualToUsingFields(codes.values)
            it.edges().shouldBeEqualToUsingFields(
                setOf(EndpointPair.ordered(task2, task1))
            )
        }
    }

    @Test
    fun `add multiple tasks with multiple dependencies`() {
        val codes = makeMockTasks("task1", "task2")
        val task1 = codes["task1"]!!
        val task2 = codes["task2"]!!
        val task3 = MockTask("task3")
        val task4 = MockTask("task4")

        task1.dependencies += task2
        task1.dependencies += task3
        task2.dependencies += task3
        task3.dependencies += task4

        val container = mockContainer(codes)
        val graph = CodeGraph(container).graph

        graph.shouldBeRight {
            it.nodes().shouldBeEqualToUsingFields(codes.values + task3 + task4)
            it.edges().shouldBeEqualToUsingFields(
                setOf(
                    EndpointPair.ordered(task2, task1),
                    EndpointPair.ordered(task3, task1),
                    EndpointPair.ordered(task3, task2),
                    EndpointPair.ordered(task4, task3)
                )
            )
        }
    }

    @Test
    fun `circular dependencies are not allowed`() {
        val codes = makeMockTasks("task1", "task2")
        val task1 = codes["task1"]!!
        val task2 = codes["task2"]!!

        task1.dependencies += task2
        task2.dependencies += task1

        val container = mockContainer(codes)
        val graph = CodeGraph(container)

        graph.graph.shouldBeLeft()
    }

    @Test
    fun `tasks connected by a variable should have an edge`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator("variable1" to true) }
            })
        }

        val codes = makeMockTasks("task1", "task2")
        val task1 = codes["task1"]!!
        val task2 = codes["task2"]!!
        val variable1 = MockVariable("variable1")

        task1.outputs += variable1
        task2.inputs += variable1

        val container = mockContainer(codes)
        val graph = CodeGraph(container).graph

        graph.shouldBeRight {
            it.nodes().shouldBeEqualToUsingFields(codes.values)
            it.edges().shouldBeEqualToUsingFields(
                setOf(EndpointPair.ordered(task1, task2))
            )
        }
    }

    @Test
    fun `tasks connected by a variable cannot form a circuit`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator("variable1" to true) }
            })
        }

        val codes = makeMockTasks("task1", "task2")
        val task1 = codes["task1"]!!
        val task2 = codes["task2"]!!
        val variable1 = MockVariable("variable1")

        task1.dependencies += task2
        task1.outputs += variable1
        task2.inputs += variable1

        val container = mockContainer(codes)
        val graph = CodeGraph(container)
        graph.graph.shouldBeLeft()
    }

    @Test
    fun `islands are not allowed`() {
        startKoin {
            modules(module {
                single { mockVariableNameValidator("variable1" to true) }
            })
        }

        val codes = makeMockTasks("task1", "task2", "task3")
        val task1 = codes["task1"]!!
        val task2 = codes["task2"]!!

        // Don't connect task3 so it forms an islands
        task1.dependencies += task2

        val container = mockContainer(codes)
        val graph = CodeGraph(container)
        graph.graph.shouldBeLeft()
    }

    private fun makeMockTasks(vararg names: String) =
        names.map { it to MockTask(it) }.toMap().toMutableMap()
}
