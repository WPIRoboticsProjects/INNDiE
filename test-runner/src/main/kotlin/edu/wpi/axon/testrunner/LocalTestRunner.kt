package edu.wpi.axon.testrunner

import arrow.core.Either
import arrow.core.Valid
import arrow.core.left
import arrow.core.mapOf
import arrow.core.right
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
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.inndie.util.runCommand
import java.io.File
import java.nio.file.Path
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.stringify
import mu.KotlinLogging
import org.apache.commons.lang3.RandomStringUtils

/**
 * A test runner that runs inference locally.
 */
class LocalTestRunner : TestRunner {

    @UseExperimental(ImplicitReflectionSerializer::class)
    override fun runTest(
        trainedModelPath: Path,
        testData: TestData,
        loadTestDataPlugin: Plugin,
        processTestOutputPlugin: Plugin,
        workingDir: Path
    ): Either<List<File>, List<File>> {
        require(trainedModelPath.toString().isNotBlank())
        require(workingDir.toString().isNotBlank())

        LOGGER.debug {
            """
            |Running a test with:
            |trainedModelPath=$trainedModelPath
            |testData=$testData
            |loadTestDataPlugin=$loadTestDataPlugin
            |processTestOutputPlugin=$processTestOutputPlugin
            |workingDir=$workingDir
            """.trimMargin()
        }

        val scriptGenerator = ScriptGenerator(
            DefaultPolymorphicNamedDomainObjectContainer.of(),
            DefaultPolymorphicNamedDomainObjectContainer.of()
        ) {
            val model by variables.creating(Variable::class)
            tasks.run(LoadModelTask::class) {
                modelPath = trainedModelPath.toString()
                modelOutput = model
            }

            val testDataStringVar by variables.creating(Variable::class)
            tasks.run(LoadStringTask::class) {
                // TODO: Document this format
                data = Json.stringify(
                    when (testData) {
                        is TestData.FromDataset -> when (testData.exampleDataset) {
                            is Dataset.ExampleDataset ->
                                mapOf("example_dataset" to testData.exampleDataset.name)

                            is Dataset.Custom ->
                                mapOf("custom_dataset" to testData.exampleDataset.path.path)
                        }

                        is TestData.FromFile ->
                            mapOf("from_file" to testData.filePath.toAbsolutePath().toString())
                    }
                )

                output = testDataStringVar
            }

            val loadedTestData by variables.creating(Variable::class)
            val expectedOutput by variables.creating(Variable::class)
            val steps by variables.creating(Variable::class)
            tasks.run(RunPluginTask::class) {
                functionName = "load_test_data"
                functionDefinition = loadTestDataPlugin.contents
                functionInputs = listOf(testDataStringVar)
                functionOutputs = listOf(loadedTestData, expectedOutput, steps)
            }

            val inferenceOutput by variables.creating(Variable::class)
            tasks.run(RunInferenceTask::class) {
                this.model = model
                input = loadedTestData
                this.steps = steps
                output = inferenceOutput
            }

            lastTask = tasks.run(RunPluginTask::class) {
                functionName = "process_model_output"
                functionDefinition = processTestOutputPlugin.contents
                functionInputs = listOf(loadedTestData, expectedOutput, inferenceOutput)
                functionOutputs = listOf()
            }
        }

        val script = scriptGenerator.code()
        LOGGER.debug { "Generated test script:\n$script" }

        check(script is Valid)

        workingDir.resolve("output").toFile().mkdirs()
        val scriptFilename = "${RandomStringUtils.randomAlphanumeric(20)}.py"
        val scriptPath = workingDir.resolve(scriptFilename)
        scriptPath.toFile().apply {
            createNewFile()
            val patchedScript = script.a
                .replace(
                    workingDir.toAbsolutePath().toString(),
                    "."
                )
                .replace(
                    trainedModelPath.toString(),
                    "/models/${trainedModelPath.toAbsolutePath().fileName}"
                )
                .let {
                    when (testData) {
                        is TestData.FromFile -> {
                            it.replace(
                                testData.filePath.toString(),
                                "/test-data/${testData.filePath.toAbsolutePath().fileName}"
                            )
                        }

                        is TestData.FromDataset -> it
                    }
                }

            LOGGER.debug { "Patched script:\n$patchedScript" }

            writeText(patchedScript)
        }

        return runCommand(
            listOf(
                "docker",
                "run",
                "--rm",
                "-v",
                "${workingDir.toAbsolutePath()}:/home",
                "-v",
                "${trainedModelPath.parent.toAbsolutePath()}:/models"
            ) + when (testData) {
                is TestData.FromDataset -> emptyList()
                is TestData.FromFile -> listOf(
                    "-v",
                    "${testData.filePath.parent.toAbsolutePath()}:/test-data"
                )
            } + listOf(
                "wpilib/axon-ci:latest",
                "/usr/bin/python3.6",
                "/home/$scriptFilename"
            ),
            mapOf(),
            null
        ).attempt().unsafeRunSync().fold(
            {
                LOGGER.debug(it) { "Failed to run test script." }
                throw it
            },
            { (exitCode, stdOut, stdErr) ->
                val message = """
                    |Finished running test script.
                    |Exit code: $exitCode
                    |Std out:
                    |$stdOut
                    |
                    |Std err:
                    |$stdErr
                    |
                """.trimMargin()

                LOGGER.info { message }

                val outputFiles = workingDir.resolve("output")
                    .toFile()
                    .walkTopDown()
                    .filter { it.isFile }
                    .toList()

                if (exitCode != 0) {
                    val errorLogFile = workingDir.resolve("output")
                        .resolve("error_log.txt")
                        .toFile()
                    errorLogFile.writeText(message)
                    listOf(errorLogFile).left()
                } else {
                    outputFiles.right()
                }
            }
        )
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
