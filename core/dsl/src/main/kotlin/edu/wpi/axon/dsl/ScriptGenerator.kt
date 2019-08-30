package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.util.singleAssign

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
        configure()
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
     * TODO: What imports need to be considered duplicates?
     *
     * @return The set of Imports needed by the [variables] and [tasks].
     */
    fun computeImports(): Set<Import> = tasks.flatMap { it.value.imports }.toSet()

    /**
     * @param generateDebugComments Whether to insert debugging comments.
     * @return The entire generated script.
     */
    fun code(generateDebugComments: Boolean = false) = buildString {
        require(isConfiguredCorrectly()) {
            "The DSl was not configured correctly."
        }

        (variables.values + tasks.values + requiredVariables)
            .filter { !it.isConfiguredCorrectly() }
            .let {
                require(it.isEmpty()) {
                    """
                    |Incorrectly configured:
                    |${it.joinToString("\n")}
                    """.trimMargin()
                }
            }

        val handledNodes = mutableSetOf<AnyCode>() // The nodes that code gen has run for

        appendImports(generateDebugComments)
        append('\n')
        appendTaskCode(generateDebugComments, handledNodes)
        append('\n')
        appendRequiredVariables(generateDebugComments, handledNodes)
    }.trim()

    /**
     * Appends all the needed Imports.
     *
     * @param generateDebugComments Whether to insert debugging comments.
     */
    private fun StringBuilder.appendImports(generateDebugComments: Boolean) {
        val imports = computeImports()
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
     * Appends all the needed task code.
     *
     * @param generateDebugComments Whether to insert debugging comments.
     * @param handledNodes The nodes that code generation has already run for.
     */
    private fun StringBuilder.appendTaskCode(
        generateDebugComments: Boolean,
        handledNodes: MutableSet<AnyCode>
    ) {
        @Suppress("UNCHECKED_CAST")
        val graph = CodeGraph(tasks as PolymorphicNamedDomainObjectContainer<AnyCode>).graph

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

                if (generateDebugComments) {
                    println("Generating $node")
                }

                appendCode(node, generateDebugComments, handledNodes)
            }
        }

        appendNode(lastTask)
    }

    /**
     * Appends code for all the explicitly required variables.
     *
     * @param generateDebugComments Whether to insert debugging comments.
     * @param handledNodes The nodes that code generation has already run for.
     */
    private fun StringBuilder.appendRequiredVariables(
        generateDebugComments: Boolean,
        handledNodes: MutableSet<AnyCode>
    ) {
        requiredVariables.forEach { variable ->
            tasks.forEach { _, task ->
                if (task !in handledNodes && variable in task.outputs) {
                    if (generateDebugComments) {
                        println("Generating $task because of required variable $variable")
                    }

                    appendCode(task, generateDebugComments, handledNodes)
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
}
