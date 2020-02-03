package edu.wpi.axon.ui

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
import edu.wpi.axon.plugin.DatasetPlugins.processMnistTypePlugin
import edu.wpi.axon.plugin.LocalPluginManager
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.util.axonBucketName
import edu.wpi.axon.util.datasetPluginManagerName
import edu.wpi.axon.util.testPluginManagerName
import java.nio.file.Paths
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.dsl.module

val localScriptRunnerCache = Paths.get(
    System.getProperty("user.home"),
    ".wpilib",
    "Axon",
    "local-script-runner-cache"
)

fun defaultFrontendModule() = module {
    single(qualifier = named(axonBucketName), createdAtStart = true) { findAxonS3Bucket() }

    single {
        JobDb(
            Database.connect(
                url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
                driver = "org.h2.Driver"
            )
        )
    }

    single {
        when (val bucketName = get<Option<String>>(named(axonBucketName))) {
            is Some -> S3PreferencesManager(S3Manager(bucketName.t)).apply { initialize() }
            is None -> LocalPreferencesManager(
                Paths.get(
                    System.getProperty("user.home"),
                    ".wpilib",
                    "Axon",
                    "preferences.json"
                )
            ).apply { initialize() }
        }
    }

    single(named(datasetPluginManagerName)) {
        // TODO: Load official plugins from resources
        val officialPlugins = listOf(
            datasetPassthroughPlugin,
            processMnistTypePlugin
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
                Paths.get(
                    System.getProperty("user.home"),
                    ".wpilib",
                    "Axon",
                    "dataset_plugin_cache.json"
                ).toFile(),
                officialPlugins
            ).apply { initialize() }
        }
    }

    single(named(testPluginManagerName)) {
        // TODO: Load official plugins from resources
        val officialPlugins = listOf(
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
                Paths.get(
                    System.getProperty("user.home"),
                    ".wpilib",
                    "Axon",
                    "test_plugin_cache.json"
                ).toFile(),
                officialPlugins
            ).apply { initialize() }
        }
    }

    single { JobLifecycleManager(jobRunner = get(), jobDb = get(), waitAfterStartingJobMs = 5000L) }
    single { JobRunner() }
    single<ExampleModelManager> { GitExampleModelManager() }
}
