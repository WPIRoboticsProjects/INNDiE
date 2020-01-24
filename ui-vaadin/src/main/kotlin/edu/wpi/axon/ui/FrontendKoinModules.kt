import edu.wpi.axon.aws.EC2TrainingScriptRunner
import edu.wpi.axon.aws.S3PreferencesManager
import edu.wpi.axon.aws.TrainingScriptRunner
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
        val boundBucketName = get<String?>(named(axonBucketName))
        if (boundBucketName != null) {
            // We are using AWS
            S3PreferencesManager(boundBucketName).apply { initialize() }
        } else {
            // We are not using AWS
            LocalPreferencesManager(
                Paths.get(
                    System.getProperty("user.home"),
                    ".wpilib",
                    "Axon",
                    "preferences.json"
                )
            ).apply { initialize() }
        }
    }

    factory<TrainingScriptRunner> {
        val boundBucketName = get<String?>(named(axonBucketName))
        if (boundBucketName != null) {
            EC2TrainingScriptRunner(
                boundBucketName,
                get<PreferencesManager>().get().defaultEC2NodeType
            )
        } else {
            TODO("Support running outside of AWS. Create a local training script runner")
        }
    }
}
