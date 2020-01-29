package edu.wpi.axon.db.data

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableTuple2II
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.util.FilePath
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils

fun Random.nextDataset(): Dataset {
    return if (nextBoolean()) {
        Dataset.ExampleDataset::class.sealedSubclasses.let {
            it[nextInt(it.size)].objectInstance!!
        }
    } else {
        Dataset.Custom(
            FilePath.S3(RandomStringUtils.randomAlphanumeric(20)),
            RandomStringUtils.randomAlphanumeric(20)
        )
    }
}

fun Random.nextTrainingScriptProgress(): TrainingScriptProgress =
    when (nextInt(TrainingScriptProgress::class.sealedSubclasses.count())) {
        0 -> TrainingScriptProgress.NotStarted
        1 -> TrainingScriptProgress.InProgress(nextDouble(0.0, 1.0))
        else -> TrainingScriptProgress.Completed
    }

fun Random.nextJob(
    jobDb: JobDb,
    name: String = RandomStringUtils.randomAlphanumeric(10),
    status: TrainingScriptProgress = nextTrainingScriptProgress(),
    userOldModelPath: FilePath = FilePath.S3(RandomStringUtils.randomAlphanumeric(10)),
    userNewModelName: FilePath = FilePath.S3(RandomStringUtils.randomAlphanumeric(10)),
    userDataset: Dataset = nextDataset(),
    userOptimizer: Optimizer = Optimizer.Adam(
        nextDouble(0.0, 1.0),
        nextDouble(0.0, 1.0),
        nextDouble(0.0, 1.0),
        nextDouble(0.0, 1.0),
        nextBoolean()
    ),
    userLoss: Loss = Loss.SparseCategoricalCrossentropy,
    userMetrics: Set<String> = setOf(
        RandomStringUtils.randomAlphanumeric(10),
        RandomStringUtils.randomAlphanumeric(10)
    ),
    userEpochs: Int = nextInt(1, Int.MAX_VALUE),
    userNewModel: Model = Model.Sequential(
        RandomStringUtils.randomAlphanumeric(10),
        (1..3).map { nextInt(128) },
        setOf(
            Layer.Dense(RandomStringUtils.randomAlphanumeric(10), null, 10).trainable(),
            Layer.Conv2D(
                RandomStringUtils.randomAlphanumeric(10),
                null,
                9,
                SerializableTuple2II(3, 3),
                Activation.SoftMax
            ).trainable(),
            Layer.AveragePooling2D(RandomStringUtils.randomAlphanumeric(10), null).untrainable()
        )
    ),
    generateDebugComments: Boolean = nextBoolean()
) = jobDb.create(
    name,
    status,
    userOldModelPath,
    userNewModelName,
    userDataset,
    userOptimizer,
    userLoss,
    userMetrics,
    userEpochs,
    userNewModel,
    generateDebugComments
)
