package edu.wpi.axon.examplemodel

import arrow.core.None
import arrow.core.Some
import arrow.core.Tuple2
import arrow.core.toT
import arrow.fx.IO
import arrow.fx.extensions.fx
import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.tfdata.layer.Layer
import edu.wpi.inndie.tflayerloader.ModelLoaderFactory
import java.io.File
import org.octogonapus.ktguava.collections.mapNodes

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
     * @return The [File] the model was downloaded to.
     */
    fun download(exampleModel: ExampleModel): IO<File>
}

/**
 * Downloads and configures (handles freezing layers) an example model.
 *
 * @param exampleModel The example model to download.
 * @param exampleModelManager The manager to download with.
 * @return The configured model.
 */
@Suppress("UnstableApiUsage")
fun downloadAndConfigureExampleModel(
    exampleModel: ExampleModel,
    exampleModelManager: ExampleModelManager
): IO<Tuple2<Model, File>> = IO.fx {
    val file = exampleModelManager.download(exampleModel).bind()
    val model = ModelLoaderFactory().createModelLoader(file.name).load(File(file.absolutePath)).bind()

    val freezeLayerTransform: (Layer.MetaLayer) -> Layer.MetaLayer = { layer ->
        exampleModel.freezeLayers[layer.name]?.let {
            when (val trainableFlag = it.toOption()) {
                is Some -> layer.layer.isTrainable(trainableFlag.t)
                is None -> layer.layer.untrainable()
            }
        } ?: layer
    }

    when (model) {
        is Model.Sequential -> model.copy(
            layers = model.layers.mapTo(
                mutableSetOf(),
                freezeLayerTransform
            )
        )

        is Model.General -> model.copy(layers = model.layers.mapNodes(freezeLayerTransform))
    } toT file
}
