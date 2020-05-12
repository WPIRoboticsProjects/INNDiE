package edu.wpi.inndie.dsl.task

import edu.wpi.inndie.dsl.configuredCorrectly
import edu.wpi.inndie.testutil.KoinTestFixture
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class EarlyStoppingTaskTest : KoinTestFixture() {

    @Test
    fun `test code gen`() {
        startKoin {}

        val task = EarlyStoppingTask("").apply {
            monitor = "val_loss"
            minDelta = 0
            patience = 0
            verbose = 0
            mode = "auto"
            baseline = null
            restoreBestWeights = false
            output = configuredCorrectly("output")
        }

        task.code() shouldBe """
            |output = tf.keras.callbacks.EarlyStopping(
            |    monitor="val_loss",
            |    min_delta=0,
            |    patience=0,
            |    verbose=0,
            |    mode="auto",
            |    baseline=None,
            |    restore_best_weights=False
            |)
        """.trimMargin()
    }
}
