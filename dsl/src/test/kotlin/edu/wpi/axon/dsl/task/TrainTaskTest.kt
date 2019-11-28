package edu.wpi.axon.dsl.task

import arrow.core.None
import arrow.core.Some
import edu.wpi.axon.dsl.TaskConfigurationTestFixture
import edu.wpi.axon.dsl.configuredCorrectly
import edu.wpi.axon.testutil.KoinTestFixture
import edu.wpi.axon.tfdata.Verbosity
import io.kotlintest.shouldBe
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

internal class TrainTaskConfigurationTest : TaskConfigurationTestFixture<TrainTask>(
    TrainTask::class,
    listOf(
        TrainTask::modelInput,
        TrainTask::trainInputData,
        TrainTask::trainOutputData
    )
)

internal class TrainTaskTest : KoinTestFixture() {

    @Test
    fun `train code gen with null batch size and no callbacks`() {
        startKoin { }

        val task = TrainTask("task").apply {
            modelInput = configuredCorrectly("model")
            trainInputData = configuredCorrectly("trainInput")
            trainOutputData = configuredCorrectly("trainOutput")
            validationInputData = Some(configuredCorrectly("validationInput"))
            validationOutputData = Some(configuredCorrectly("validationOutput"))
            callbacks = setOf()
            batchSize = null
            epochs = 10
            verbose = Verbosity.Silent
            shuffle = true
        }

        task.code() shouldBe """
            |model.fit(
            |    trainInput,
            |    trainOutput,
            |    batch_size=None,
            |    epochs=10,
            |    verbose=0,
            |    callbacks=[],
            |    validation_split=0.0,
            |    validation_data=(validationInput, validationOutput),
            |    shuffle=True
            |)
        """.trimMargin()
    }

    @Test
    fun `train code gen with nonnull batch size and two callbacks`() {
        startKoin { }

        val task = TrainTask("task").apply {
            modelInput = configuredCorrectly("model")
            trainInputData = configuredCorrectly("trainInput")
            trainOutputData = configuredCorrectly("trainOutput")
            validationInputData = Some(configuredCorrectly("validationInput"))
            validationOutputData = Some(configuredCorrectly("validationOutput"))
            callbacks = setOf(configuredCorrectly("cb1"), configuredCorrectly("cb2"))
            batchSize = 3
            epochs = 1
            verbose = Verbosity.OneLinePerEpoch
            shuffle = false
        }

        task.code() shouldBe """
            |model.fit(
            |    trainInput,
            |    trainOutput,
            |    batch_size=3,
            |    epochs=1,
            |    verbose=2,
            |    callbacks=[cb1, cb2],
            |    validation_split=0.0,
            |    validation_data=(validationInput, validationOutput),
            |    shuffle=False
            |)
        """.trimMargin()
    }

    @Test
    fun `train code gen with no validation data`() {
        startKoin { }

        val task = TrainTask("task").apply {
            modelInput = configuredCorrectly("model")
            trainInputData = configuredCorrectly("trainInput")
            trainOutputData = configuredCorrectly("trainOutput")
            validationInputData = None
            validationOutputData = None
            callbacks = setOf()
            batchSize = null
            epochs = 10
            verbose = Verbosity.Silent
            shuffle = true
        }

        task.code() shouldBe """
            |model.fit(
            |    trainInput,
            |    trainOutput,
            |    batch_size=None,
            |    epochs=10,
            |    verbose=0,
            |    callbacks=[],
            |    validation_split=0.0,
            |    shuffle=True
            |)
        """.trimMargin()
    }

    @Test
    fun `train code gen with validation split`() {
        startKoin { }

        val task = TrainTask("task").apply {
            modelInput = configuredCorrectly("model")
            trainInputData = configuredCorrectly("trainInput")
            trainOutputData = configuredCorrectly("trainOutput")
            validationSplit = 0.2
            callbacks = setOf()
            batchSize = null
            epochs = 10
            verbose = Verbosity.Silent
            shuffle = true
        }

        task.code() shouldBe """
            |model.fit(
            |    trainInput,
            |    trainOutput,
            |    batch_size=None,
            |    epochs=10,
            |    verbose=0,
            |    callbacks=[],
            |    validation_split=0.2,
            |    shuffle=True
            |)
        """.trimMargin()
    }
}
