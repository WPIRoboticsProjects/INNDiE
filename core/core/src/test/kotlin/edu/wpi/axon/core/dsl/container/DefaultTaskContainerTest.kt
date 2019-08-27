package edu.wpi.axon.core.dsl.container

import com.natpryce.hamkrest.assertion.assertThat
import com.natpryce.hamkrest.equalTo
import com.natpryce.hamkrest.hasSize
import edu.wpi.axon.core.dsl.Import
import edu.wpi.axon.core.dsl.MockTask
import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.InputData
import edu.wpi.axon.core.dsl.variable.Variable
import edu.wpi.axon.core.isTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class DefaultTaskContainerTest {

    @Test
    fun `calling create with a name adds a new task`() {
        val container = DefaultTaskContainer.of()
        container.create(MockTask::class)
        assertThat(container, hasSize(equalTo(1)))
    }

    @Test
    fun `calling create with a name and config adds a new task and calls configure`() {
        val container = DefaultTaskContainer.of()
        var called = false
        val mockConfigure: Task.() -> Unit = { called = true }

        container.create(MockTask::class, mockConfigure)

        assertThat(container, hasSize(equalTo(1)))
        assertThat(called, isTrue())
    }

    @Test
    fun `cannot create a task from an abstract class`() {
        val container = DefaultTaskContainer.of()

        abstract class AbstractTask : Task

        assertThrows<IllegalArgumentException> {
            container.create(AbstractTask::class)
        }
    }

    @Test
    fun `cannot create a variable from a companion object`() {
        val container = DefaultTaskContainer.of()

        assertThrows<IllegalArgumentException> {
            container.create(TestTask.Companion::class)
        }
    }

    @Test
    fun `cannot create a variable without a matching constructor`() {
        val container = DefaultTaskContainer.of()

        assertThrows<IllegalArgumentException> {
            container.create(TestTask::class)
        }
    }

    class TestTask(
        @Suppress("UNUSED_PARAMETER") anotherParameter: Int
    ) : Task {
        override val imports: Set<Import> = emptySet()
        override val inputVariables: Set<Variable> = emptySet()
        override val inputData: Set<InputData> = emptySet()
        override val output: Variable? = null
        override fun isConfiguredCorrectly() = true
        override fun code() = ""

        companion object : Task {
            override val imports: Set<Import> = emptySet()
            override val inputVariables: Set<Variable> = emptySet()
            override val inputData: Set<InputData> = emptySet()
            override val output: Variable? = null
            override fun isConfiguredCorrectly() = true
            override fun code() = ""
        }
    }
}
