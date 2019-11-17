package edu.wpi.axon.examplemodel

import arrow.fx.IO
import java.io.File
import java.nio.file.Path

/**
 * Manages downloading, caching, and reading the example models.
 */
interface ExampleModelManager {

    /**
     * Reads all the example models. Does not update the cache on its own; call [updateCache] at
     * least once before calling this.
     *
     * @return All example models.
     */
    fun getAllExampleModels(): IO<Set<ExampleModel>>

    /**
     * Updates the example model cache, if an update is necessary. This is meant to be run
     * asynchronously at program start-up to reduce the time needed to run [getAllExampleModels] the
     * first time.
     *
     * Updating the cache can take a long time (10+ seconds). Checking if the cache is up-to-date
     * is much shorter, but can still take a while (100+ ms) compared to the speed of regular code.
     *
     * @return An effect for continuation.
     */
    fun updateCache(): IO<Unit>

    /**
     * Downloads the [ExampleModel].
     *
     * @param exampleModel The model to download.
     * @param path The [Path] to create a file at.
     * @return The [File] the model was downloaded to.
     */
    fun download(exampleModel: ExampleModel, path: Path): IO<File>
}
