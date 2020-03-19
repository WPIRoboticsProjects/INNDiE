package edu.wpi.axon.testrunner

import edu.wpi.axon.plugin.Plugin
import java.io.File
import java.nio.file.Path

interface TestRunner {

    /**
     * Runs an inference test with a trained model.
     *
     * @param trainedModelPath The path to the trained model to run inference with.
     * @param testDataPath The path to the test data to load.
     * @param loadTestDataPlugin The plugin to use for loading the test data.
     * @param processTestOutputPlugin The plugin to use for processing the output of the model.
     * @param workingDir The directory to work out of.
     * @return The output from the [processTestOutputPlugin].
     */
    fun runTest(
        trainedModelPath: Path,
        testDataPath: Path,
        loadTestDataPlugin: Plugin,
        processTestOutputPlugin: Plugin,
        workingDir: Path
    ): List<File>
}
