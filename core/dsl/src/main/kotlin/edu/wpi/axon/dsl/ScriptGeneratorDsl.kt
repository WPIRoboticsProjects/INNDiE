package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable

/**
 * Generates a script from a DSL description of the script.
 *
 * @param variables The script variables container.
 * @param tasks The script tasks container.
 * @param configure Configures the script so it is ready for generation.
 */
@SuppressWarnings("UseDataClass")
class ScriptGeneratorDsl(
    val variables: PolymorphicNamedDomainObjectContainer<Variable>,
    val tasks: PolymorphicNamedDomainObjectContainer<Task>,
    configure: ScriptGeneratorDsl.() -> Unit
) : Configurable {

    // TODO: Do something with this (probably make the script implement a method and return this)
    // var scriptOutput: Variable? = null

    // TODO: Find an intelligent way to derive this instead of needing it to be specified
    var lastTask: Task? = null

    init {
        configure()
        // Don't check isConfiguredCorrectly here because some tests need an unconfigured script
    }

    override fun isConfiguredCorrectly() = lastTask != null

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

        (variables + tasks).filter { !it.value.isConfiguredCorrectly() }.let {
            if (it.isNotEmpty()) {
                throw IllegalArgumentException(
                    """
                    |Incorrectly configured:
                    |${it.values.joinToString("\n")}
                    """.trimMargin()
                )
            }
        }

        appendImports(generateDebugComments)
        append('\n')
        appendTaskCode(generateDebugComments)
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
     */
    private fun StringBuilder.appendTaskCode(generateDebugComments: Boolean) {
        @Suppress("UNCHECKED_CAST")
        val graph = CodeGraph(tasks as PolymorphicNamedDomainObjectContainer<Code<Code<*>>>).graph

        fun appendNode(node: Code<Code<*>>) {
            // Append the predecessors first because they are the dependencies of this node
            graph.predecessors(node)
                .sortedBy { it.code().length } // Need to make the ordering consistent
                .forEach {
                    appendNode(it)
                }

            when (node) {
                is Variable, is Task -> {
                    if (generateDebugComments) {
                        append("# ${toString()}")
                        append('\n')
                    }

                    append(node.code())
                    append('\n')
                    append('\n')
                }
            }
        }

        appendNode(lastTask!!)
    }
}
