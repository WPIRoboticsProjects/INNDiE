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
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.util.axonBucketName
import edu.wpi.axon.util.datasetPluginManagerName
import edu.wpi.axon.util.loadTestDataPluginManagerName
import edu.wpi.axon.util.localAxonCacheDir
import edu.wpi.axon.util.processTestOutputPluginManagerName
import java.nio.file.Path
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.core.scope.Scope
import org.koin.dsl.module

val localScriptRunnerCache: Path = localAxonCacheDir.resolve("local-script-runner-cache")

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
                localAxonCacheDir.resolve("preferences.json")
            ).apply { initialize() }
        }
    }

    single(named(datasetPluginManagerName)) {
        bindPluginManager(
            setOf(
                datasetPassthroughPlugin,
                processMnistTypePlugin
            ),
            "axon-dataset-plugins",
            "dataset_plugin_cache.json"
        )
    }

    single(named(loadTestDataPluginManagerName)) {
        bindPluginManager(
            setOf(),
            "axon-load-test-data-plugins",
            "load_test_data_plugin_cache.json"
        )
    }

    single(named(processTestOutputPluginManagerName)) {
        bindPluginManager(
            setOf(),
            "axon-process-test-output-plugins",
            "process_test_output_plugin_cache.json"
        )
    }

    single { JobLifecycleManager(jobRunner = get(), jobDb = get(), waitAfterStartingJobMs = 5000L) }
    single { ModelDownloader() }
    single { JobRunner() }
    single<ExampleModelManager> { GitExampleModelManager() }
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
        localAxonCacheDir.resolve(cacheFileName).toFile(),
        officialPlugins
    ).apply { initialize() }
}
