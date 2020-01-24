package edu.wpi.axon.ui

import arrow.core.Either
import defaultFrontendModule
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.dbdata.TrainingScriptProgress
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.Model
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.tflayerloader.ModelLoaderFactory
import edu.wpi.axon.training.ModelPath
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

        val newModelName = "32_32_1_conv_sequential-trained.h5"
        val (model, path) = loadModel("32_32_1_conv_sequential.h5")
        get<JobDb>().create(
            Job(
                "Job 1",
                TrainingScriptProgress.NotStarted,
                ModelPath.S3(path),
                ModelPath.S3(newModelName),
                Dataset.ExampleDataset.FashionMnist,
                Optimizer.Adam(0.001, 0.9, 0.999, 1e-7, false),
                Loss.SparseCategoricalCrossentropy,
                setOf("accuracy"),
                1,
                model,
                false
            )
        )
    }

    override fun contextDestroyed(sce: ServletContextEvent?) {
        LOGGER.info { "Stopping web app." }
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }

        // TODO: Encapsulate this somewhere better
        fun loadModel(modelName: String): Pair<Model, String> {
            val localModelPath =
                Paths.get("/Users/austinshalit/git/Axon/training/src/test/resources/edu/wpi/axon/training/$modelName")
                    .toString()
            val layers =
                ModelLoaderFactory().createModeLoader(localModelPath).load(File(localModelPath))
            val model = layers.attempt().unsafeRunSync()
            check(model is Either.Right)
            return model.b to localModelPath
        }
    }
}
