package edu.wpi.axon.ui.main

import arrow.core.Either
import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.aws.S3PreferencesManager
import edu.wpi.axon.aws.findAxonS3Bucket
import edu.wpi.axon.aws.plugin.S3PluginManager
import edu.wpi.axon.aws.preferences.LocalPreferencesManager
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.InternalJobTrainingMethod
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.examplemodel.GitExampleModelManager
import edu.wpi.axon.plugin.DatasetPlugins.datasetPassthroughPlugin
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypeForMobilenetPlugin
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypePlugin
import edu.wpi.axon.plugin.LocalPluginManager
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.ui.JobLifecycleManager
import edu.wpi.axon.ui.JobRunner
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.axonBucketName
import edu.wpi.axon.util.datasetPluginManagerName
import edu.wpi.axon.util.localCacheDir
import edu.wpi.axon.util.testPluginManagerName
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.dsl.module

val localScriptRunnerCache: Path = localCacheDir.resolve("local-script-runner-cache")

fun loadModel(modelName: String): Pair<Model, String> {
    val localModelPath =
        Paths.get("/home/salmon/Documents/Axon/training/src/test/resources/edu/wpi/axon/training/$modelName")
            .toString()
    val layers =
        ModelLoaderFactory().createModelLoader(localModelPath).load(File(localModelPath))
    val model = layers.attempt().unsafeRunSync()
    check(model is Either.Right)
    return model.b to localModelPath
}

fun defaultFrontendModule() = module {
    single(qualifier = named(axonBucketName), createdAtStart = true) { findAxonS3Bucket() }

    single {
        JobDb(
            Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
            )
        ).apply {
            val modelName = "32_32_1_conv_sequential.h5"
            val (model, path) = loadModel(modelName)

            create(
                name = "AWS Job",
                status = TrainingScriptProgress.NotStarted,
                userOldModelPath = ModelSource.FromFile(FilePath.S3(modelName)),
                userDataset = Dataset.ExampleDataset.FashionMnist,
                userOptimizer = Optimizer.Adam(
                    learningRate = 0.001,
                    beta1 = 0.9,
                    beta2 = 0.999,
                    epsilon = 1e-7,
                    amsGrad = false
                ),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 1,
                userNewModel = model,
                generateDebugComments = false,
                datasetPlugin = datasetPassthroughPlugin,
                internalTrainingMethod = InternalJobTrainingMethod.Untrained,
                target = ModelDeploymentTarget.Desktop
            )

            create(
                name = "Local Job",
                status = TrainingScriptProgress.NotStarted,
                userOldModelPath = ModelSource.FromFile(FilePath.Local(path)),
                userDataset = Dataset.ExampleDataset.FashionMnist,
                userOptimizer = Optimizer.Adam(),
                userLoss = Loss.SparseCategoricalCrossentropy,
                userMetrics = setOf("accuracy"),
                userEpochs = 1,
                userNewModel = model,
                generateDebugComments = false,
                datasetPlugin = processMnistTypePlugin,
                internalTrainingMethod = InternalJobTrainingMethod.Untrained,
                target = ModelDeploymentTarget.Desktop
            )
        }
    }

    single {
        when (val bucketName = get<Option<String>>(named(axonBucketName))) {
            is Some -> S3PreferencesManager(S3Manager(bucketName.t)).apply { initialize() }
            is None -> LocalPreferencesManager(
                localCacheDir.resolve("preferences.json")
            ).apply { initialize() }
        }
    }

    single(named(datasetPluginManagerName)) {
        // TODO: Load official plugins from resources
        val officialPlugins = setOf(
            datasetPassthroughPlugin,
            processMnistTypePlugin,
            processMnistTypeForMobilenetPlugin
        )

        when (val bucketName = get<Option<String>>(named(axonBucketName))) {
            is Some -> {
                S3PluginManager(
                    S3Manager(bucketName.t),
                    "axon-dataset-plugins",
                    officialPlugins
                ).apply { initialize() }
            }

            is None -> LocalPluginManager(
                localCacheDir.resolve("dataset_plugin_cache.json").toFile(),
                officialPlugins
            ).apply { initialize() }
        }
    }

    single(named(testPluginManagerName)) {
        // TODO: Load official plugins from resources
        val officialPlugins = setOf(
            Plugin.Official("Test test plugin", """print("Hello from test test plugin!")""")
        )

        when (val bucketName = get<Option<String>>(named(axonBucketName))) {
            is Some -> {
                S3PluginManager(
                    S3Manager(bucketName.t),
                    "axon-test-plugins",
                    officialPlugins
                ).apply { initialize() }
            }

            is None -> LocalPluginManager(
                localCacheDir.resolve("test_plugin_cache.json").toFile(),
                officialPlugins
            ).apply { initialize() }
        }
    }

    single {
        JobLifecycleManager(
            jobRunner = get(),
            jobDb = get(),
            waitAfterStartingJobMs = 5000L
        ).apply { initialize() }
    }

    single { ModelManager() }
    single { JobRunner() }
    single<ExampleModelManager> { GitExampleModelManager().apply { updateCache().unsafeRunSync() } }
}
