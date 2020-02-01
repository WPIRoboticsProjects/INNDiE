package edu.wpi.axon.aws

import edu.wpi.axon.db.data.nextDataset
import edu.wpi.axon.util.FilePath
import java.nio.file.Paths
import java.io.File
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils

internal fun randomRunTrainingScriptConfigurationUsingAWS(tempDir: File) = RunTrainingScriptConfiguration(
    oldModelName = FilePath.S3("${RandomStringUtils.randomAlphanumeric(10)}.h5"),
    dataset = Random.nextDataset(),
    scriptContents = RandomStringUtils.randomAlphanumeric(10),
    epochs = Random.nextInt(1, 10),
    workingDir = tempDir.toPath(),
    id = Random.nextInt()
)
