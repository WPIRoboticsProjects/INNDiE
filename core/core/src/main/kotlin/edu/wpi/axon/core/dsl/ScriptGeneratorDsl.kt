package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainerDelegateProvider
import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.Code
import edu.wpi.axon.core.dsl.variable.Variable
import kotlin.reflect.KClass

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
) {

    // TODO: Do something with this (probably make the script implement a method and return this)
    var scriptOutput: Variable? = null

    // TODO: Find an intelligent way to derive this instead of needing it to be specified
    var lastTask: Task? = null

    init {
        configure()
    }

    /**
     * @return The set of Imports needed by the [variables] and [tasks].
     */
    fun computeImports(): Set<Import> {
        // TODO: What imports need to be considered duplicates?
        return tasks.flatMap { it.value.imports }.toSet()
    }

    /**
     * @param generateDebugComments Whether to insert debugging comments.
     * @return The entire generated script.
     */
    fun code(generateDebugComments: Boolean = false) = buildString {
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
            graph.successors(node).forEach {
                appendNode(it)
            }

            when (node) {
                is Variable -> {
                    if (generateDebugComments) {
                        append("# class=${node::class.simpleName}, name=${node.name}")
                        append('\n')
                    }

                    append(node.code())
                    append('\n')
                    append('\n')
                }

                is Task -> {
                    if (generateDebugComments) {
                        append("# class=${node::class.simpleName}, name=${node.name}")
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

    private fun StringBuilder.appendTaskDependencies(task: Task, generateDebugComments: Boolean) {
        task.dependencies.forEach { inputData ->
            if (generateDebugComments) {
                append("# class=${inputData::class.simpleName}")
                append('\n')
            }

            append(inputData.code())
            append('\n')
            append('\n')
        }
    }
}

/**
 * Creates a new variable and configures it.
 */
fun <T : Variable, U : T> PolymorphicNamedDomainObjectContainer<T>.creating(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicNamedDomainObjectContainerDelegateProvider.of(this, type, configuration)

/**
 * Creates a new task and configures it.
 */
fun <T : Task, U : T> PolymorphicNamedDomainObjectContainer<T>.running(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicNamedDomainObjectContainerDelegateProvider.of(this, type, configuration)
