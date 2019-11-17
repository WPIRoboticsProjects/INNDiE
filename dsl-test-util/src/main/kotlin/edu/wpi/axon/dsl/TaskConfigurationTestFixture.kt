package edu.wpi.axon.dsl

import edu.wpi.axon.dsl.task.Task
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.testutil.KoinTestFixture
import io.kotlintest.matchers.booleans.shouldBeFalse
import io.kotlintest.shouldThrow
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty1
import kotlin.reflect.full.createType
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.DynamicTest.dynamicTest
import org.junit.jupiter.api.TestFactory
import org.koin.core.context.startKoin
import org.koin.dsl.module

/**
 * Tests various ways a task can be configured.
 *
 * @param getTask A lambda to get a new instance of the task.
 * @param properties The properties to test.
 */
open class TaskConfigurationTestFixture<T : Task>(
    private val getTask: () -> T,
    private val properties: List<KMutableProperty1<T, Variable>>
) : KoinTestFixture() {

    /**
     * @param klass The task class.
     * @param properties The properties to test.
     */
    constructor(klass: KClass<T>, properties: List<KMutableProperty1<T, Variable>>) : this(
        {
            klass.constructors.first {
                it.parameters.size == 1 && it.parameters.first().type == String::class.createType()
            }.call("")
        },
        properties
    )

    @TestFactory
    fun testUninitializedInput(): List<DynamicTest> {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        return properties.map { uninitializedProp ->
            val task = getTask()

            // Don't initialize the uninitializedProp. Just initialize everything else
            properties.filter { it != uninitializedProp }.forEach { validProp ->
                validProp.set(task, configuredCorrectly())
            }

            dynamicTest("$task with $uninitializedProp uninitialized") {
                shouldThrow<UninitializedPropertyAccessException> { task.isConfiguredCorrectly() }
            }
        }
    }

    @TestFactory
    fun testIncorrectlyConfiguredInput(): List<DynamicTest> {
        startKoin {
            modules(module {
                alwaysValidImportValidator()
                alwaysValidPathValidator()
            })
        }

        return properties.map { invalidProp ->
            val task = getTask()

            properties.filter { it != invalidProp }.forEach { validProp ->
                validProp.set(task, configuredCorrectly())
            }

            invalidProp.set(task, configuredIncorrectly())

            dynamicTest("$task with $invalidProp configured incorrectly") {
                task.isConfiguredCorrectly().shouldBeFalse()
            }
        }
    }
}
