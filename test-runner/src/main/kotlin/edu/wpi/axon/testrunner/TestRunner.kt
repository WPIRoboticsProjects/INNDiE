package edu.wpi.axon.testrunner

import arrow.core.Either
import edu.wpi.axon.plugin.Plugin
import java.io.File
import java.nio.file.Path

interface TestRunner {

    /**
     * Runs an inference test with a trained model.
     *
     * @param trainedModelPath The path to the trained model to run inference with.
     * @param testData The test data to load.
     * @param loadTestDataPlugin The plugin to use for loading the test data.
     * @param processTestOutputPlugin The plugin to use for processing the output of the model.
     * @param workingDir The directory to work out of.
     * @return The output from the [processTestOutputPlugin].
     */
    fun runTest(
        trainedModelPath: Path,
        testData: TestData,
        loadTestDataPlugin: Plugin,
        processTestOutputPlugin: Plugin,
        workingDir: Path
    ): Either<List<File>, List<File>>
}
