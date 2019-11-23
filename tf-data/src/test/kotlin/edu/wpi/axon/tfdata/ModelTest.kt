package edu.wpi.axon.tfdata

import edu.wpi.axon.tfdata.layer.Layer
import io.kotlintest.shouldBe
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test

internal class ModelTest {

    @Test
    fun `test serialization model`() {
        val before = Model.Sequential(
            RandomStringUtils.randomAlphanumeric(20),
            listOf(Random.nextInt(), Random.nextInt(), Random.nextInt()),
            setOf(
                Layer.Dense(
                    "dense_1",
                    null,
                    10
                ).trainable()
            )
        )

        Model.deserialize(before.serialize()).shouldBe(before)
    }
}
