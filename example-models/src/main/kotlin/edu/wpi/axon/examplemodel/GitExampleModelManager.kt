package edu.wpi.axon.examplemodel

import arrow.fx.IO
import java.io.File
import java.net.URL
import java.nio.file.Paths
import org.apache.commons.io.FileUtils

/**
 * An [ExampleModelManager] that pulls models from a Git repository.
 */
class GitExampleModelManager : ExampleModelManager {

    /**
     * The directory the example model cache lives in. Modify this to set a different cache
     * directory.
     */
    var cacheDir: File =
        Paths.get(System.getProperty("user.home"), ".wpilib", "Axon", "example-model-cache")
            .toFile()

    /**
     * The example models metadata file.
     */
    val exampleModelMetadataFile: File
        get() = Paths.get(cacheDir.absolutePath, "exampleModels.json").toFile()

    /**
     * The URL to download the example models metadata from.
     */
    var exampleModelMetadataUrl = "https://raw.githubusercontent.com/wpilibsuite/axon-example-models/master/exampleModels.json"

    override fun getAllExampleModels(): IO<Set<ExampleModel>> = IO {
        check(exampleModelMetadataFile.exists()) {
            "The example model metadata file (${exampleModelMetadataFile.absolutePath}) is not on " +
                    "disk. Try updating the cache."
        }

        ExampleModelsMetadata.deserialize(exampleModelMetadataFile.readText()).exampleModels
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun updateCache(): IO<Unit> = IO {
        // The cache dir either has to already exist or be created
        check(cacheDir.exists() || cacheDir.mkdirs()) {
            "Failed to make necessary cache directories in path: $cacheDir"
        }
    }.flatMap {
        IO {
            FileUtils.copyURLToFile(URL(exampleModelMetadataUrl), exampleModelMetadataFile)
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun download(exampleModel: ExampleModel): IO<File> = IO {
        // The models dir either has to already exist or be created
        check(cacheDir.exists() || cacheDir.mkdirs()) {
            "Failed to make necessary models directories in path: $cacheDir"
        }

        val file = Paths.get(cacheDir.absolutePath, exampleModel.fileName).toFile()
        file.createNewFile()
        FileUtils.copyURLToFile(URL(exampleModel.url), file)
        file
    }
}
