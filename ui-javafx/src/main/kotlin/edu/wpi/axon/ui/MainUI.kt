package edu.wpi.axon.ui

import arrow.core.Either
import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.JobTrainingMethod
import edu.wpi.axon.db.data.ModelSource
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
import javafx.application.Application
import javafx.scene.Scene
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.BorderPane
import javafx.stage.Stage
import org.koin.core.KoinComponent
import org.koin.core.context.startKoin
import org.koin.core.get

class MainUI : Application(), KoinComponent {

    override fun start(primaryStage: Stage) {
        startKoin {
            modules(
                listOf(
                    defaultBackendModule(),
                    defaultFrontendModule()
                )
            )
        }

        val root = AnchorPane().apply {
            children.add(BorderPane().apply {
                AnchorPane.setTopAnchor(this, 0.0)
                AnchorPane.setRightAnchor(this, 0.0)
                AnchorPane.setBottomAnchor(this, 0.0)
                AnchorPane.setLeftAnchor(this, 0.0)

                center = JobTableView().apply {
                    maxWidth = Double.MAX_VALUE
                    selectionModel.selectedItemProperty().addListener { _, _, newValue: Job? ->
                        newValue?.let {
                            right = JobDetailView(newValue) {
                                right = null
                                selectionModel.clearSelection()
                            }
                        }
                    }
                }
            })
        }

        primaryStage.apply {
            title = "Hello, World!"
            scene = Scene(root, 1000.0, 800.0).apply {
                stylesheets.add("material.css")
            }
            show()
        }

        val modelName = "32_32_1_conv_sequential.h5"
        val (model, path) = loadModel(modelName)

        get<JobDb>().create(
            name = "AWS Job",
            status = TrainingScriptProgress.NotStarted,
            userOldModelPath = ModelSource.FromFile(FilePath.S3(modelName)),
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
    }

    companion object {
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
