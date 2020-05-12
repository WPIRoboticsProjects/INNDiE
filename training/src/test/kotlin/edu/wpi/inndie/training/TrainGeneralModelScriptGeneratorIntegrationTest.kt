@file:SuppressWarnings("LongMethod", "LargeClass")

package edu.wpi.inndie.training

import arrow.core.None
import edu.wpi.inndie.dsl.defaultBackendModule
import edu.wpi.inndie.plugin.DatasetPlugins
import edu.wpi.inndie.testutil.KoinTestFixture
import edu.wpi.inndie.tfdata.Dataset
import edu.wpi.inndie.tfdata.Model
import edu.wpi.inndie.tfdata.loss.Loss
import edu.wpi.inndie.tfdata.optimizer.Optimizer
import edu.wpi.inndie.training.testutil.loadModel
import edu.wpi.inndie.util.FilePath
import io.kotlintest.assertions.arrow.validation.shouldBeValid
import io.kotlintest.matchers.types.shouldBeInstanceOf
import java.io.File
import kotlin.random.Random
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import org.koin.core.context.startKoin

internal class TrainGeneralModelScriptGeneratorIntegrationTest : KoinTestFixture() {

    @Test
    fun `test with custom model with an add`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        // TODO: The dataset is in the wrong format for the model's input (actually, there are two
        //  inputs).
        val modelName = "network_with_add.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            val script = TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it,
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    workingDir = tempDir.toPath(),
                    datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript()
            script.shouldBeValid()
        }
    }

    @Test
    fun `test code gen with mobilenetv2 from tf 1-15`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        // TODO: The dataset is in the wrong format for the model's input. Need to have a way to
        //  process it.
        /*
        def format_input_for_mobilenetv2(images):
            images = images[..., tf.newaxis]
            images = tf.image.resize_images(images, [224, 224])
            images = tf.image.grayscale_to_rgb(images)
            return images
         */
        val modelName = "mobilenetv2_tf-1-15.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            val script = TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it,
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Desktop,
                    workingDir = tempDir.toPath(),
                    datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript()
            script.shouldBeValid()
        }
    }

    @Test
    fun `test code gen with mobilenetv2 from tf 1-15 targeting the coral`(@TempDir tempDir: File) {
        startKoin {
            modules(defaultBackendModule())
        }

        // TODO: The dataset is in the wrong format for the model's input. Need to have a way to
        //  process it.
        /*
        def format_input_for_mobilenetv2(images):
            images = images[..., tf.newaxis]
            images = tf.image.resize_images(images, [224, 224])
            images = tf.image.grayscale_to_rgb(images)
            return images
         */
        val modelName = "mobilenetv2_tf-1-15.h5"
        val (model, path) = loadModel(modelName) {}
        model.shouldBeInstanceOf<Model.General> {
            val script = TrainGeneralModelScriptGenerator(
                TrainState(
                    userOldModelPath = FilePath.Local(path),
                    userDataset = Dataset.ExampleDataset.Mnist,
                    userOptimizer = Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                    userLoss = Loss.SparseCategoricalCrossentropy,
                    userMetrics = setOf("accuracy"),
                    userEpochs = 50,
                    userNewModel = it,
                    userValidationSplit = None,
                    generateDebugComments = false,
                    target = ModelDeploymentTarget.Coral(0.001),
                    workingDir = tempDir.toPath(),
                    datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
                    jobId = Random.nextInt(1, Int.MAX_VALUE)
                ),
                it
            ).generateScript()
            script.shouldBeValid()
        }
    }
}
