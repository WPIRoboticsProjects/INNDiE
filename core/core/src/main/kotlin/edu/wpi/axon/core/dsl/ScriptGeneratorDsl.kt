package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainerDelegateProvider
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
    val tasks: PolymorphicDomainObjectContainer<Task>,
    configure: ScriptGeneratorDsl.() -> Unit
) {

    var scriptOutput: Variable? = null

    init {
        configure()
    }

    /**
     * @return The set of Imports needed by the [variables] and [tasks].
     */
    fun computeImports(): Set<Import> {
        // TODO: What imports need to be considered duplicates?
        return tasks.flatMap {
            it.imports + it.inputData.flatMap { it.imports }
        }.toSet()
    }

    /**
     * @return The entire generated script.
     */
    fun code(generateDebugComments: Boolean = false) = buildString {
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

        append('\n')

        tasks.forEach { task ->
            task.inputData.forEach { inputData ->
                if (generateDebugComments) {
                    append("# class=${inputData::class.simpleName}")
                    append('\n')
                }

                append(inputData.code())
                append('\n')
                append('\n')
            }

            if (generateDebugComments) {
                append("# class=${task::class.simpleName}")
                append('\n')
            }

            append(task.code())
            append('\n')
            append('\n')
        }
    }.trim()
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
fun <T : Task, U : T> PolymorphicDomainObjectContainer<T>.running(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicDomainObjectContainerDelegateProvider.of(this, type, configuration)
