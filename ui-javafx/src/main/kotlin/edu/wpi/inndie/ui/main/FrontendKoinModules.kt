package edu.wpi.inndie.ui.main

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import edu.wpi.axon.aws.S3Manager
import edu.wpi.axon.aws.S3PreferencesManager
import edu.wpi.axon.aws.findAxonS3Bucket
import edu.wpi.axon.aws.plugin.S3PluginManager
import edu.wpi.axon.aws.preferences.LocalPreferencesManager
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.examplemodel.GitExampleModelManager
import edu.wpi.axon.plugin.DatasetPlugins.datasetPassthroughPlugin
import edu.wpi.axon.plugin.DatasetPlugins.divideByTwoFiveFivePlugin
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypeForMobilenetPlugin
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypePlugin
import edu.wpi.axon.plugin.LoadTestDataPlugins.loadExampleDatasetPlugin
import edu.wpi.axon.plugin.LocalPluginManager
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.plugin.ProcessTestOutputPlugins.autoMpgRegressionOutputPlugin
import edu.wpi.axon.plugin.ProcessTestOutputPlugins.imageClassificationModelOutputPlugin
import edu.wpi.inndie.ui.JobLifecycleManager
import edu.wpi.inndie.ui.JobRunner
import edu.wpi.inndie.ui.ModelManager
import edu.wpi.inndie.util.axonBucketName
import edu.wpi.inndie.util.datasetPluginManagerName
import edu.wpi.inndie.util.loadTestDataPluginManagerName
import edu.wpi.inndie.util.localCacheDir
import edu.wpi.inndie.util.processTestOutputPluginManagerName
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

fun defaultFrontendModule() = module {
    single(qualifier = named(axonBucketName), createdAtStart = true) { findAxonS3Bucket() }

    single {
        JobDb(
            Database.connect(
                url = "jdbc:h2:~/.wpilib/Axon/db;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
            )
        ).apply {
            // val modelName = "32_32_1_conv_sequential.h5"
            // val (model, path) = loadModel(modelName)
            //
            // create(
            //     name = "AWS Job",
            //     status = TrainingScriptProgress.NotStarted,
            //     userOldModelPath = ModelSource.FromFile(FilePath.S3(modelName)),
            //     userDataset = Dataset.ExampleDataset.FashionMnist,
            //     userOptimizer = Optimizer.Adam(
            //         learningRate = 0.001,
            //         beta1 = 0.9,
            //         beta2 = 0.999,
            //         epsilon = 1e-7,
            //         amsGrad = false
            //     ),
            //     userLoss = Loss.SparseCategoricalCrossentropy,
            //     userMetrics = setOf("accuracy"),
            //     userEpochs = 1,
            //     userNewModel = model,
            //     generateDebugComments = false,
            //     datasetPlugin = datasetPassthroughPlugin,
            //     internalTrainingMethod = InternalJobTrainingMethod.Untrained,
            //     target = ModelDeploymentTarget.Desktop
            // )
            //
            // create(
            //     name = "Local Job",
            //     status = TrainingScriptProgress.NotStarted,
            //     userOldModelPath = ModelSource.FromFile(FilePath.Local(path)),
            //     userDataset = Dataset.ExampleDataset.FashionMnist,
            //     userOptimizer = Optimizer.Adam(),
            //     userLoss = Loss.SparseCategoricalCrossentropy,
            //     userMetrics = setOf("accuracy"),
            //     userEpochs = 1,
            //     userNewModel = model,
            //     generateDebugComments = false,
            //     datasetPlugin = processMnistTypePlugin,
            //     internalTrainingMethod = InternalJobTrainingMethod.Untrained,
            //     target = ModelDeploymentTarget.Desktop
            // )
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
        bindPluginManager(
            setOf(
                datasetPassthroughPlugin,
                processMnistTypePlugin,
                processMnistTypeForMobilenetPlugin,
                divideByTwoFiveFivePlugin
            ),
            "axon-dataset-plugins",
            "dataset_plugin_cache.json"
        )
    }

    single(named(loadTestDataPluginManagerName)) {
        bindPluginManager(
            setOf(
                loadExampleDatasetPlugin
            ),
            "axon-load-test-data-plugins",
            "load_test_data_plugin_cache.json"
        )
    }

    single(named(processTestOutputPluginManagerName)) {
        bindPluginManager(
            setOf(
                imageClassificationModelOutputPlugin,
                autoMpgRegressionOutputPlugin
            ),
            "axon-process-test-output-plugins",
            "process_test_output_plugin_cache.json"
        )
    }

    single(createdAtStart = true) {
        // This needs to be eager so we eagerly resume tracking in-progress Jobs
        _root_ide_package_.edu.wpi.inndie.ui.JobLifecycleManager(
            jobRunner = get(),
            jobDb = get(),
            waitAfterStartingJobMs = 5000L
        ).apply { initialize() }
    }

    single { _root_ide_package_.edu.wpi.inndie.ui.ModelManager() }
    single { _root_ide_package_.edu.wpi.inndie.ui.JobRunner() }
    single<ExampleModelManager> { GitExampleModelManager().apply { updateCache().unsafeRunSync() } }
}

private fun Scope.bindPluginManager(
    officialPlugins: Set<Plugin.Official>,
    cacheName: String,
    cacheFileName: String
): PluginManager = when (val bucketName = get<Option<String>>(named(axonBucketName))) {
    is Some -> {
        S3PluginManager(
            S3Manager(bucketName.t),
            cacheName,
            officialPlugins
        ).apply { initialize() }
    }

    is None -> LocalPluginManager(
        localCacheDir.resolve(cacheFileName).toFile(),
        officialPlugins
    ).apply { initialize() }
}
