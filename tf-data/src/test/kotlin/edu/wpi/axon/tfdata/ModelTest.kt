package edu.wpi.axon.tfdata

import edu.wpi.axon.tfdata.layer.Activation
import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.shouldBe
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test

internal class ModelTest {

    @Test
    fun `test serialization model`() {
        val before = Model.Sequential(
            RandomStringUtils.randomAlphanumeric(10),
            (1..3).map { Random.nextInt(128) },
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
        )

        Model.deserialize(before.serialize()).shouldBe(before)
    }
}
