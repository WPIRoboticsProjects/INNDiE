package edu.wpi.axon.examplemodel

import arrow.fx.IO
import mu.KotlinLogging
import org.apache.commons.io.FileUtils
import org.eclipse.jgit.api.CloneCommand
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.lib.RepositoryBuilder
import java.io.File
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths

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
     * The example model repository directory, inside [cacheDir].
     */
    val exampleModelRepoDir: File
        get() = Paths.get(cacheDir.absolutePath, "axon-example-models").toFile()

    /**
     * The remote that the example models are pulled from.
     */
    var exampleModelRepo = "https://github.com/wpilibsuite/axon-example-models.git"

    override fun getAllExampleModels(): IO<Set<ExampleModel>> = IO {
        check(exampleModelRepoDir.exists()) {
            "The example model cache (${exampleModelRepoDir.absolutePath}) is not on disk. " +
                "Try updating the cache."
        }

        val files = exampleModelRepoDir.listFiles()!!

        ExampleModelsMetadata.deserialize(
            files.first { it.name == "exampleModels.json" }.readText()
        ).exampleModels
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun updateCache(): IO<Unit> = IO {
        // The cache dir either has to already exist or be created
        check(cacheDir.exists() || cacheDir.mkdirs()) {
            "Failed to make necessary cache directories in path: $cacheDir"
        }
    }.flatMap {
        IO {
            if (exampleModelRepoDir.exists()) {
                // The repo is on disk, pull to update it
                LOGGER.debug { "Repo dir $exampleModelRepoDir exists. Pulling." }
                RepositoryBuilder().findGitDir(exampleModelRepoDir).build().use { repo ->
                    Git(repo).use { git ->
                        git.pull().call()
                    }
                }

                Unit
            } else {
                // The repo is not on disk, clone to get it
                LOGGER.debug { "Repo dir $exampleModelRepoDir does not exist. Cloning." }
                CloneCommand().setURI(exampleModelRepo)
                    .setDirectory(exampleModelRepoDir)
                    .call()
                    .close()
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    override fun download(exampleModel: ExampleModel, path: Path): IO<File> = IO {
        val file = path.toFile()
        file.createNewFile()
        FileUtils.copyURLToFile(URL(exampleModel.url), file)
        file
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
