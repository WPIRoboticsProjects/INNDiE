package edu.wpi.axon.ui

import arrow.core.None
import arrow.core.Option
import arrow.core.Some
import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.aws.LocalTrainingScriptRunner
import edu.wpi.axon.aws.S3PreferencesManager
import edu.wpi.axon.aws.findAxonS3Bucket
import edu.wpi.axon.aws.preferences.LocalPreferencesManager
import edu.wpi.axon.aws.preferences.PreferencesManager
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.util.axonBucketName
import java.nio.file.Paths
import org.jetbrains.exposed.sql.Database
import org.koin.core.qualifier.named
import org.koin.dsl.module

fun defaultFrontendModule() = module {
    single(named(axonBucketName)) { findAxonS3Bucket() }

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
            is Some -> S3PreferencesManager(bucketName.t).apply { initialize() }
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

    factory {
        when (val bucketName = get<Option<String>>(named(axonBucketName))) {
            is Some -> EC2TrainingScriptRunner(
                bucketName.t,
                get<PreferencesManager>().get().defaultEC2NodeType
            )
            is None -> LocalTrainingScriptRunner()
        }
    }
}
