package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainerDelegateProvider
import edu.wpi.axon.core.dsl.task.Task
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

    private fun StringBuilder.appendTaskCode(generateDebugComments: Boolean) {
        // TODO: Use CodeGraph instead of tasks directly
        tasks.forEach { task ->
            appendTaskDependencies(task, generateDebugComments)

            if (generateDebugComments) {
                append("# class=${task::class.simpleName}, name=${task.key}")
                append('\n')
            }

            append(task.value.code())
            append('\n')
            append('\n')
        }
    }

    private fun StringBuilder.appendTaskDependencies(
        task: Map.Entry<String, Task>,
        generateDebugComments: Boolean
    ) {
        task.value.dependencies.forEach { inputData ->
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
