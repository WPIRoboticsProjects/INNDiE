package edu.wpi.axon.ui

import arrow.core.Either
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.plugin.DatasetPlugins
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.training.ModelDeploymentTarget
import edu.wpi.axon.util.FilePath
import java.io.File
import java.nio.file.Paths
import javax.servlet.ServletContextEvent
import javax.servlet.ServletContextListener
import javax.servlet.annotation.WebListener
import mu.KotlinLogging
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get

@WebListener
class WebAppListener : ServletContextListener, KoinComponent {

    override fun contextInitialized(sce: ServletContextEvent?) {
        LOGGER.info { "Starting web app." }

        startKoin {
            modules(
                listOf(
                    defaultBackendModule(),
                    defaultFrontendModule()
                )
            )
        }

        val modelName = "32_32_1_conv_sequential.h5"
        val (model, path) = loadModel(modelName)

        get<JobDb>().create(
            name = "AWS Job",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.S3(modelName),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(
                learningRate = 0.001,
                beta1 = 0.9,
                beta2 = 0.999,
                epsilon = 1e-7,
                amsGrad = false
            ),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = model,
            generateDebugComments = false,
            datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
            trainingMethod = JobTrainingMethod.EC2("i-0ca5697ea71b6772e"),
            target = ModelDeploymentTarget.Desktop
        )

        get<JobDb>().create(
            name = "Local Job",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.Local(path),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(
                learningRate = 0.001,
                beta1 = 0.9,
                beta2 = 0.999,
                epsilon = 1e-7,
                amsGrad = false
            ),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = model,
            generateDebugComments = false,
            datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
            trainingMethod = JobTrainingMethod.Untrained,
            target = ModelDeploymentTarget.Desktop
        )

        get<JobDb>().create(
            name = "Local Job 2",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = FilePath.Local(path),
            userDataset = Dataset.ExampleDataset.FashionMnist,
            userOptimizer = Optimizer.Adam(
                learningRate = 0.001,
                beta1 = 0.9,
                beta2 = 0.999,
                epsilon = 1e-7,
                amsGrad = false
            ),
            userLoss = Loss.SparseCategoricalCrossentropy,
            userMetrics = setOf("accuracy"),
            userEpochs = 1,
            userNewModel = model,
            generateDebugComments = false,
            datasetPlugin = DatasetPlugins.datasetPassthroughPlugin,
            trainingMethod = JobTrainingMethod.Untrained,
            target = ModelDeploymentTarget.Desktop
        )

        get<JobLifecycleManager>().initialize()
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        LOGGER.info { "Stopping web app." }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }

        // TODO: Encapsulate this somewhere better
        fun loadModel(modelName: String): Pair<Model, String> {
            val localModelPath =
                Paths.get("/home/salmon/Documents/Axon/training/src/test/resources/edu/wpi/axon/training/$modelName")
                    .toString()
            val layers =
                ModelLoaderFactory().createModelLoader(localModelPath).load(File(localModelPath))
            val model = layers.attempt().unsafeRunSync()
            check(model is Either.Right)
            return model.b to localModelPath
        }
    }
}
