package edu.wpi.axon.ui

import arrow.core.Option
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.examplemodel.downloadAndConfigureExampleModel
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import java.io.File
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.get
import org.koin.core.inject
import org.koin.core.qualifier.named

class ModelDownloader : KoinComponent {

    private val bucketName by inject<Option<String>>(named(axonBucketName))
    private val s3Manager by lazy { bucketName.map { S3Manager(it) } }
    private val cache = mutableMapOf<ModelSource, Pair<Model, FilePath>>()

    /**
     * Downloads a model from a model source to a local file and loads the model. Example models
     * are also uploaded to S3.
     */
    fun downloadModel(modelSource: ModelSource): Pair<Model, FilePath> =
        cache.getOrPut(modelSource) {
            when (modelSource) {
                is ModelSource.FromFile -> {
                    val modelPath = modelSource.filePath
                    val localOldModel = when (modelPath) {
                        is FilePath.S3 -> {
                            // If the model to start training from is in S3, we need to download it
                            s3Manager.fold(
                                { error("Need an S3Manager to download the untrained model.") },
                                { it.downloadUntrainedModel(modelPath.path) }
                            )
                        }

                        is FilePath.Local -> File(modelPath.path)
                    }

                    ModelLoaderFactory().createModelLoader(localOldModel.name)
                        .load(localOldModel)
                        .unsafeRunSync() to modelPath
                }

                is ModelSource.FromExample -> {
                    // Download the example model locally and then upload it to S3
                    s3Manager.fold(
                        { error("Need an S3Manager to download the example model.") },
                        {
                            val (model, file) = downloadAndConfigureExampleModel(
                                modelSource.exampleModel,
                                get()
                            ).unsafeRunSync()
                            LOGGER.debug {
                                """
                                |model=$model
                                |file=$file
                                """.trimMargin()
                            }
                            it.uploadUntrainedModel(file)
                            model to FilePath.S3(file.name)
                        }
                    )
                }

                is ModelSource.FromJob ->
                    TODO("Create an S3 file with the trained output from the Job")
            }
        }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
