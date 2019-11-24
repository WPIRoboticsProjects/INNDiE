package edu.wpi.axon.dbdata

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.SerializableTuple2
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

fun Random.nextJob() = Job(
    RandomStringUtils.randomAlphanumeric(10),
    TrainingScriptProgress.Completed,
    RandomStringUtils.randomAlphanumeric(10),
    RandomStringUtils.randomAlphanumeric(10),
    nextDataset(),
    Optimizer.Adam(
        nextDouble(),
        nextDouble(),
        nextDouble(),
        nextDouble(),
        nextBoolean()
    ),
    Loss.SparseCategoricalCrossentropy,
    setOf(
        RandomStringUtils.randomAlphanumeric(10),
        RandomStringUtils.randomAlphanumeric(10)
    ),
    nextInt(),
    Model.Sequential(
        RandomStringUtils.randomAlphanumeric(10),
        (1..3).map { nextInt(128) },
        setOf(
            Layer.Dense(RandomStringUtils.randomAlphanumeric(10), null, 10).trainable(),
            Layer.Conv2D(
                RandomStringUtils.randomAlphanumeric(10),
                null,
                9,
                SerializableTuple2(3, 3),
                Activation.SoftMax
            ).trainable(),
            Layer.AveragePooling2D(RandomStringUtils.randomAlphanumeric(10), null).untrainable()
        )
    ),
    nextBoolean()
)
