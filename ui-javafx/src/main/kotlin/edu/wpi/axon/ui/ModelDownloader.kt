package edu.wpi.axon.ui

import arrow.core.Option
import arrow.core.getOrElse
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.examplemodel.ExampleModelManager
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

class ModelManager : KoinComponent {

    private val bucketName by inject<Option<String>>(named(axonBucketName))
    private val s3Manager by lazy { bucketName.map { S3Manager(it) } }
    private val exampleModelManager by inject<ExampleModelManager>()

    fun downloadModel(modelSource: ModelSource): FilePath.Local {
        return when (modelSource) {
            is ModelSource.FromExample -> {
                val file = exampleModelManager.download(modelSource.exampleModel).unsafeRunSync()
                FilePath.Local(file.path)
            }

            is ModelSource.FromFile -> when (modelSource.filePath) {
                is FilePath.S3 -> {
                    val file = getS3Manager().downloadModel(modelSource.filePath.filename)
                    FilePath.Local(file.path)
                }

                is FilePath.Local -> modelSource.filePath as FilePath.Local
            }

            is ModelSource.FromJob -> TODO()
        }
    }

    fun uploadModel(modelSource: ModelSource): FilePath.S3 {
        return when (modelSource) {
            is ModelSource.FromExample -> {
                val file = downloadModel(modelSource)
                getS3Manager().uploadModel(File(file.path))
                FilePath.S3(file.filename)
            }

            is ModelSource.FromFile -> when (modelSource.filePath) {
                is FilePath.S3 -> modelSource.filePath as FilePath.S3
                is FilePath.Local -> {
                    getS3Manager().uploadModel(File(modelSource.filePath.path))
                    FilePath.S3(modelSource.filePath.filename)
                }
            }

            is ModelSource.FromJob -> TODO()
        }
    }

    fun loadModel(modelSource: ModelSource): Model {
        val localFile = File(downloadModel(modelSource).path)
        return ModelLoaderFactory().createModelLoader(localFile.name)
            .load(localFile)
            .unsafeRunSync()
    }

    private fun getS3Manager(): S3Manager = s3Manager.getOrElse {
        error("Must have an S3Manager.")
    }
}

// class ModelDownloader : KoinComponent {
//
//     private val bucketName by inject<Option<String>>(named(axonBucketName))
//     private val s3Manager by lazy { bucketName.map { S3Manager(it) } }
//     // TODO: Cache in Axon's folder as well
//     private val cache = mutableMapOf<ModelSource, Pair<Model, FilePath>>()
//
//     /**
//      * Downloads a model from a model source to a local file and loads the model. Example models
//      * are also uploaded to S3. Caches each [ModelSource] in memory. Caches downloaded models to
//      * disk.
//      */
//     fun downloadModel(modelSource: ModelSource): Pair<Model, FilePath> {
//         LOGGER.debug { "Downloading: $modelSource" }
//         return cache.getOrPut(modelSource) {
//             LOGGER.debug { "Not in cache: $modelSource" }
//             when (modelSource) {
//                 is ModelSource.FromFile -> {
//                     val modelPath = modelSource.filePath
//                     val localOldModel = when (modelPath) {
//                         is FilePath.S3 -> {
//                             // If the model to start training from is in S3, we need to download it
//                             s3Manager.fold(
//                                 { error("Need an S3Manager to download the untrained model.") },
//                                 { it.downloadModel(modelPath.path) }
//                             )
//                         }
//
//                         is FilePath.Local -> File(modelPath.path)
//                     }
//
//                     /*
//                     ModelLoaderFactory().createModelLoader(localOldModel.name)
//                         .load(localOldModel)
//                         .unsafeRunSync()
//                      */
//                     modelPath
//                 }
//
//                 is ModelSource.FromExample -> {
//                     // Download the example model locally and then upload it to S3
//                     s3Manager.fold(
//                         { error("Need an S3Manager to download the example model.") },
//                         {
//                             val (model, file) = downloadAndConfigureExampleModel(
//                                 modelSource.exampleModel,
//                                 get()
//                             ).unsafeRunSync()
//                             LOGGER.debug {
//                                 """
//                                 |model=$model
//                                 |file=$file
//                                 """.trimMargin()
//                             }
//
//                             if (!it.listModels().contains(file.name)) {
//                                 // The model is not in S3 so we need to upload it
//                                 it.uploadModel(file)
//                             }
//
//                             val s3Path = FilePath.S3(file.name)
//                             cache[ModelSource.FromFile(s3Path)] = model to s3Path
//                             model to s3Path
//                         }
//                     )
//                 }
//
//                 is ModelSource.FromJob ->
//                     TODO("Create an S3 file with the trained output from the Job")
//             }
//         }
//     }
//
//     companion object {
//         private val LOGGER = KotlinLogging.logger { }
//     }
// }
