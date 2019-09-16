package edu.wpi.axon.dsl.task

import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.dsl.defaultModule
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class CompileModelTaskIntegrationTest : KoinTestFixture() {

    @Test
    fun `compile a categorical model with adam and an accuracy metric`() {
        startKoin {
            modules(defaultModule())
        }

        val task = CompileModelTask("task").apply {
            modelInput = configuredCorrectly("modelName")
            optimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false)
            loss = Loss.SparseCategoricalCrossentropy
            metrics = setOf("accuracy")
        }

        task.code() shouldBe """
            |modelName.compile(
            |    optimizer=tf.keras.optimizers.Adam(0.001, 0.9, 0.999, 1.0E-7, False),
            |    loss=tf.keras.losses.sparse_categorical_crossentropy,
            |    metrics=["accuracy"]
            |)
        """.trimMargin()
    }
}
