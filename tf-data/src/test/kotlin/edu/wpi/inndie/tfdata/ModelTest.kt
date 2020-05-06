package edu.wpi.inndie.tfdata

import com.google.common.graph.GraphBuilder
import edu.wpi.inndie.tfdata.layer.Activation
import edu.wpi.inndie.tfdata.layer.Layer
import io.kotlintest.shouldBe
import kotlin.random.Random
import org.apache.commons.lang3.RandomStringUtils
import org.junit.jupiter.api.Test
import org.octogonapus.ktguava.collections.toImmutableGraph

internal class ModelTest {

    @Test
    fun `test serialization of sequential model`() {
        val before = Model.Sequential(
            RandomStringUtils.randomAlphanumeric(10),
            (1..3).map { Random.nextInt(128) },
            setOf(
                Layer.Dense(
                    RandomStringUtils.randomAlphanumeric(
                        10
                    ), null, 10
                ).isTrainable(),
                Layer.Conv2D(
                    RandomStringUtils.randomAlphanumeric(10),
                    null,
                    9,
                    SerializableTuple2II(3, 3),
                    Activation.SoftMax
                ).isTrainable(),
                Layer.AveragePooling2D(
                    RandomStringUtils.randomAlphanumeric(10),
                    null
                ).untrainable()
            )
        )

        Model.deserialize(before.serialize()).shouldBe(before)
    }

    @Suppress("UnstableApiUsage")
    @Test
    fun `test serialization of general model`() {
        val graph = GraphBuilder.directed().allowsSelfLoops(false).build<Layer.MetaLayer>()

        setOf(
            Layer.Dense(RandomStringUtils.randomAlphanumeric(10), null, 10).isTrainable(),
            Layer.Conv2D(
                RandomStringUtils.randomAlphanumeric(10),
                null,
                9,
                SerializableTuple2II(3, 3),
                Activation.SoftMax
            ).isTrainable(),
            Layer.AveragePooling2D(RandomStringUtils.randomAlphanumeric(10), null).untrainable()
        ).reduce { acc, layer ->
            graph.putEdge(acc, layer)
            layer
        }

        val before = Model.General(
            RandomStringUtils.randomAlphanumeric(10),
            setOf(Model.General.InputData(
                RandomStringUtils.randomAlphanumeric(10),
                (1..3).map { Random.nextInt(128) }
            )),
            graph.toImmutableGraph(),
            setOf(
                Model.General.OutputData(
                    RandomStringUtils.randomAlphanumeric(10)
                )
            )
        )

        Model.deserialize(before.serialize()).shouldBe(before)
    }
}
