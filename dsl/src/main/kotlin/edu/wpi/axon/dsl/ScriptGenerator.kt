@file:Suppress("UnstableApiUsage")

package edu.wpi.axon.dsl

import arrow.core.Either
import arrow.data.Invalid
import arrow.data.Nel
import arrow.data.ValidatedNel
import arrow.data.invalidNel
import arrow.data.valid
import com.google.common.graph.ImmutableGraph
import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.imports.Import
import edu.wpi.axon.dsl.task.EmptyBaseTask
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign
import mu.KotlinLogging

/**
 * Generates a script from a DSL description of the script.
 *
 * @param variables The script variables container.
 * @param tasks The script tasks container.
 * @param configure Configures the script so it is ready for generation.
 */
@SuppressWarnings("UseDataClass")
class ScriptGenerator(
    val variables: PolymorphicNamedDomainObjectContainer<Variable>,
    val tasks: PolymorphicNamedDomainObjectContainer<Task>,
    configure: ScriptGenerator.() -> Unit
) : Configurable {

    // TODO: Do something with this (probably make the script implement a method and return this)
    // var scriptOutput: Variable? = null

    // TODO: Find an intelligent way to derive this instead of needing it to be specified
    // Once CodeGraph verifies there are no islands, we should be able to start from any node and
    // find the last node in the DAG.
    var lastTask: Task by singleAssign()

    private val requiredVariables = mutableSetOf<Variable>()

    init {
        this.configure()
        // Don't check isConfiguredCorrectly here because some tests need an unconfigured script
    }

    override fun isConfiguredCorrectly() = true

    /**
     * Requires that the [Variable] is emitted in the generated code. Any tasks that output to this
     * [Variable] will also be generated.
     *
     * @param variable The [Variable].
     */
    fun requireGeneration(variable: Variable) {
        requiredVariables += variable
    }

    /**
     * @param generateDebugComments Whether to insert debugging comments.
     * @return The entire generated script.
     */
    fun code(generateDebugComments: Boolean = false): ValidatedNel<String, String> {
        if (!isConfiguredCorrectly()) {
            return "$this is configured incorrectly.".invalidNel()
        }

        @Suppress("UNUSED_VARIABLE")
        val finalCompositeTask by tasks.running(EmptyBaseTask::class) {
            // Depend on what the user said was their last task so this runs after
            dependencies += lastTask

            // Add dependencies for any required variables. Before this point, generating the
            // CodeGraph could result in islands. This step resolves islands that would form if
            // the user added tasks that are only connected by required variables.
            dependOnRequiredVariables()
        }

        (variables.values + tasks.values)
            .filter { !it.isConfiguredCorrectly() }
            .let {
                if (it.isNotEmpty()) {
                    return Invalid(Nel.fromListUnsafe(it).map { "$it is configured incorrectly." })
                }
            }

        val handledNodes = mutableSetOf<AnyCode>() // The nodes that code gen has run for

        @Suppress("UNCHECKED_CAST")
        val graph = CodeGraph(tasks as PolymorphicNamedDomainObjectContainer<AnyCode>).graph

        return when (graph) {
            is Either.Left -> graph.a.invalidNel()

            is Either.Right -> buildString {
                appendImports(generateDebugComments, graph.b)
                append('\n')
                appendTaskCode(generateDebugComments, graph.b, finalCompositeTask, handledNodes)
            }.trim().valid()
        }
    }

    /**
     * Appends all the needed Imports.
     *
     * @param generateDebugComments Whether to insert debugging comments.
     * @param graph The [CodeGraph] to generate from.
     */
    private fun StringBuilder.appendImports(
        generateDebugComments: Boolean,
        graph: ImmutableGraph<AnyCode>
    ) {
        val imports = computeImports(graph)
            .map { it to it.code() }
            .sortedBy { it.second }

        imports.forEach { (import, code) ->
            if (generateDebugComments) {
                append("# class=${import::class.simpleName}")
                append('\n')
            }

            append(code)
            append('\n')
        }
    }

    /**
     * TODO: What imports need to be considered duplicates?
     *
     * @param graph The [CodeGraph] to generate from.
     * @return The set of Imports needed by the [variables] and [tasks].
     */
    private fun computeImports(graph: ImmutableGraph<AnyCode>): Set<Import> =
        graph.nodes().flatMap { it.imports }.toSet()

    /**
     * Appends all the needed task code.
     *
     * @param generateDebugComments Whether to insert debugging comments.
     * @param graph The [CodeGraph] to generate from.
     * @param handledNodes The nodes that code generation has already run for.
     */
    private fun StringBuilder.appendTaskCode(
        generateDebugComments: Boolean,
        graph: ImmutableGraph<AnyCode>,
        startingTask: AnyCode,
        handledNodes: MutableSet<AnyCode>
    ) {
        fun appendNode(node: AnyCode) {
            if (node !in handledNodes) {
                // Append the predecessors first because they are the dependencies of this node
                graph.predecessors(node)
                    .sortedBy {
                        // Need to make the ordering consistent. Process more complex nodes first.
                        graph.predecessors(it).size
                    }
                    .forEach {
                        appendNode(it)
                    }

                logger.debug { "Generating $node" }
                appendCode(node, generateDebugComments, handledNodes)
            }
        }

        appendNode(startingTask)
    }

    /**
     * Adds dependencies on the tasks that output to any of the explicitly required variables.
     */
    private fun EmptyBaseTask.dependOnRequiredVariables() {
        requiredVariables.forEach { variable ->
            tasks.forEach { _, task ->
                if (variable in task.outputs) {
                    logger.debug {
                        "Adding dependency on $task because of required variable $variable"
                    }

                    dependencies += task
                }
            }
        }
    }

    private fun StringBuilder.appendCode(
        node: AnyCode,
        generateDebugComments: Boolean,
        handledNodes: MutableSet<AnyCode>
    ) {
        if (generateDebugComments) {
            append("# $node")
            append('\n')
        }

        append(node.code())
        append('\n')
        append('\n')

        handledNodes += node
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}
