package edu.wpi.axon.core.dsl

import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicDomainObjectContainerDelegateProvider
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainer
import edu.wpi.axon.core.dsl.container.PolymorphicNamedDomainObjectContainerDelegateProvider
import edu.wpi.axon.core.dsl.task.Task
import edu.wpi.axon.core.dsl.variable.Variable
import kotlin.reflect.KClass

@SuppressWarnings("UseDataClass")
@ScriptGeneratorDslMarker
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
        return (variables.flatMap { it.imports } + tasks.flatMap { it.imports }).toSet()
    }

    /**
     * @return The entire generated script.
     */
    fun code(generateDebugComments: Boolean = false) = buildString {
        val imports = computeImports()
        imports.forEach { import ->
            if (generateDebugComments) {
                append("# class=${import::class.simpleName}")
                append('\n')
            }

            append(import.code())
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
    }
}

fun <T : Any, U : T> PolymorphicNamedDomainObjectContainer<T>.creating(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicNamedDomainObjectContainerDelegateProvider.of(this, type, configuration)

fun <T : Task, U : T> PolymorphicDomainObjectContainer<T>.running(
    type: KClass<U>,
    configuration: (U.() -> Unit)? = null
) = PolymorphicDomainObjectContainerDelegateProvider.of(this, type, configuration)
