package edu.wpi.axon.testrunner

import arrow.core.Tuple3
import arrow.core.Valid
import edu.wpi.axon.dsl.ScriptGenerator
import edu.wpi.axon.dsl.container.DefaultPolymorphicNamedDomainObjectContainer
import edu.wpi.axon.dsl.creating
import edu.wpi.axon.dsl.run
import edu.wpi.axon.dsl.task.LoadModelTask
import edu.wpi.axon.dsl.task.LoadStringTask
import edu.wpi.axon.dsl.task.RunInferenceTask
import edu.wpi.axon.dsl.task.RunPluginTask
import edu.wpi.axon.dsl.variable.Variable
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.util.runCommand
import java.io.File
import java.nio.file.Path
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils

/**
 * A test runner that runs inference locally.
 */
class LocalTestRunner : TestRunner {

    override fun runTest(
        trainedModelPath: Path,
        testDataPath: Path,
        loadTestDataPlugin: Plugin,
        processTestOutputPlugin: Plugin,
        workingDir: Path
    ): List<File> {
        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val model by variables.creating(Variable::class)
            tasks.run(LoadModelTask::class) {
                modelPath = trainedModelPath.toString()
                modelOutput = model
            }

            val testDataPathVar by variables.creating(Variable::class)
            tasks.run(LoadStringTask::class) {
                data = testDataPath.toString()
                output = testDataPathVar
            }

            val testData by variables.creating(Variable::class)
            val steps by variables.creating(Variable::class)
            tasks.run(RunPluginTask::class) {
                functionName = "load_test_data"
                functionDefinition = loadTestDataPlugin.contents
                functionInputs = listOf(testDataPathVar)
                functionOutputs = listOf(testData, steps)
            }

            val inferenceOutput by variables.creating(Variable::class)
            tasks.run(RunInferenceTask::class) {
                this.model = model
                input = testData
                this.steps = steps
                output = inferenceOutput
            }

            lastTask = tasks.run(RunPluginTask::class) {
                functionName = "process_model_output"
                functionDefinition = processTestOutputPlugin.contents
                functionInputs = listOf(testData, inferenceOutput)
                functionOutputs = listOf()
            }
        }

        val script = scriptGenerator.code()
        LOGGER.debug { "Generated test script:\n$script" }

        check(script is Valid)

        workingDir.resolve("output").toFile().mkdirs()
        val scriptFile = workingDir.resolve("${RandomStringUtils.randomAlphanumeric(10)}.py")
            .toFile()
            .apply {
                createNewFile()
                writeText(script.a)
            }

        val (exitCode, _, _) = runCommand(
            listOf(
                "python3.6",
                scriptFile.path
            ),
            mapOf(),
            workingDir.toFile()
        ).attempt().unsafeRunSync().fold(
            {
                LOGGER.debug(it) { "Failed to run test script." }
                throw it
            },
            { (exitCode, stdOut, stdErr) ->
                LOGGER.debug {
                    """
                    |Finished running test script.
                    |Exit code: $exitCode
                    |Std out:
                    |$stdOut
                    |
                    |Std err:
                    |$stdErr
                    |
                    """.trimMargin()
                }

                Tuple3(exitCode, stdOut, stdErr)
            }
        )

        check(exitCode == 0)

        return workingDir.resolve("output").toFile().walkTopDown().toList()
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
