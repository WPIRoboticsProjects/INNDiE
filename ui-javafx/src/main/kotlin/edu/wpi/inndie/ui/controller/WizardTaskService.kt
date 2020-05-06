package edu.wpi.inndie.ui.controller

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.inndie.training.ModelDeploymentTarget
import edu.wpi.inndie.ui.model.DatasetType
import edu.wpi.inndie.ui.model.TaskInput
import edu.wpi.inndie.ui.model.WizardTarget
import edu.wpi.inndie.ui.model.WizardTask
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import tornadofx.Controller

class WizardTaskService : Controller() {
    val tasks: ObservableList<WizardTask> = FXCollections.observableArrayList(
            WizardTask("Classification",
                    "Separate items into categories",
                    resources["/classification.png"],
                    listOf(
                        TaskInput("MNIST",
                                "Classify handwritten digits",
                                resources["/MNIST.png"],
                                DatasetType.EXAMPLE,
                                Dataset.ExampleDataset.Mnist,
                                Optimizer.Adam(),
                                Loss.CategoricalCrossentropy),
                        TaskInput("Fashion MNIST",
                                "Classify photos of clothing",
                                resources["/Fashion_MNIST.png"],
                                DatasetType.EXAMPLE,
                                Dataset.ExampleDataset.FashionMnist,
                                Optimizer.Adam(),
                                Loss.CategoricalCrossentropy),
                        TaskInput("IMDB",
                                "Classify positive or negative movie reviews",
                                resources["/imdb.png"],
                                DatasetType.EXAMPLE,
                                Dataset.ExampleDataset.IMDB,
                                Optimizer.Adam(),
                                Loss.CategoricalCrossentropy)
                    )),
            WizardTask("Regression",
                    "Perform a regression on a set of data",
                    resources["/regression.png"],
                    listOf(
                            TaskInput("Auto MPG",
                                    "Predict the MPG of a provided vehicle configuration",
                                    resources["/auto_mpg_car.jpg"],
                                    DatasetType.EXAMPLE,
                                    Dataset.ExampleDataset.AutoMPG,
                                    Optimizer.RMSprop(),
                                    Loss.MeanSquaredError)
                    ))
    )

    val targets: ObservableList<WizardTarget> = FXCollections.observableArrayList(
            WizardTarget("Desktop", "For inference on a computer", resources["/desktop.png"], ModelDeploymentTarget.Desktop::class),
            WizardTarget("Coral", "For inference with a Google™ Coral", resources["/coral.jpg"], ModelDeploymentTarget.Coral::class)
    )
}
