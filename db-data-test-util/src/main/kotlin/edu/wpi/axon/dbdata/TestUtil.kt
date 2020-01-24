package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableTuple2II
import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils

fun Random.nextDataset(): Dataset {
    return if (nextBoolean()) {
        Dataset.ExampleDataset::class.sealedSubclasses.let {
            it[nextInt(it.size)].objectInstance!!
        }
    } else {
        Dataset.Custom(
            RandomStringUtils.randomAlphanumeric(20),
            RandomStringUtils.randomAlphanumeric(20)
        )
    }
}

fun Random.nextTrainingScriptProgress(): TrainingScriptProgress =
    when (nextInt(TrainingScriptProgress::class.sealedSubclasses.count())) {
        0 -> TrainingScriptProgress.NotStarted
        1 -> TrainingScriptProgress.InProgress(nextDouble(1.0))
        else -> TrainingScriptProgress.Completed
    }

fun Random.nextJob() = Job(
        name = RandomStringUtils.randomAlphanumeric(10),
        status = nextTrainingScriptProgress(),
        userOldModelPath = RandomStringUtils.randomAlphanumeric(10),
        userNewModelName = RandomStringUtils.randomAlphanumeric(10),
        userDataset = nextDataset(),
        userOptimizer = Optimizer.Adam(
                nextDouble(),
                nextDouble(),
                nextDouble(),
                nextDouble(),
                nextBoolean()
        ),
        userLoss = Loss.SparseCategoricalCrossentropy,
        userMetrics = setOf(
                RandomStringUtils.randomAlphanumeric(10),
                RandomStringUtils.randomAlphanumeric(10)
        ),
        userEpochs = nextInt(),
        userModel = Model.Sequential(
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
        generateDebugComments = nextBoolean()
)
