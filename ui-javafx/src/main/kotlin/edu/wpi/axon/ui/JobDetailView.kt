package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModel
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.tfdata.Dataset
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.StackPane
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

enum class ModelChoice {
    Example, Custom, Job
}

class JobDetailView(
    job: Job,
    onClose: () -> Unit
) : AnchorPane(), KoinComponent {

    private val jobDb by inject<JobDb>()
    private val exampleModelManager by inject<ExampleModelManager>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobProperty = SimpleObjectProperty<Job>()
    private val jobInProgressProperty = SimpleBooleanProperty()
    private val exampleModels = exampleModelManager.getAllExampleModels().unsafeRunSync()

    init {
        maxWidth = Double.MAX_VALUE
        prefWidth = 300.0

        jobProperty.addListener { _, _, newValue ->
            jobInProgressProperty.value = (newValue.status != TrainingScriptProgress.NotStarted)
        }

        jobProperty.value = job

        children.add(TabPane().apply {
            maxWidth = Double.MAX_VALUE
            setTopAnchor(this, 0.0)
            setRightAnchor(this, 0.0)
            setBottomAnchor(this, 0.0)
            setLeftAnchor(this, 0.0)

            tabs.add(Tab().apply {
                isClosable = false
                text = "Basic"
                content = VBox().apply {
                    spacing = 10.0
                    padding = Insets(5.0)

                    children.add(HBox().apply {
                        maxWidth = Double.MAX_VALUE
                        alignment = Pos.CENTER
                        children.add(Label(job.name).apply {
                            font = Font.font(Font.getDefault().family, FontWeight.BOLD, 15.0)
                        })
                    })

                    children.add(Button("Run").apply {
                        disableProperty().bind(jobInProgressProperty)
                        setOnAction {
                            scope.launch {
                                jobProperty.value = jobDb.update(
                                    jobProperty.value.id,
                                    status = TrainingScriptProgress.Initializing
                                )
                            }
                        }
                    })

                    children.add(Button("Close").apply {
                        setOnAction { onClose() }
                    })
                }
            })

            tabs.add(createInputTab(job))

            tabs.add(Tab().apply {
                isClosable = false
                text = "Training"
                content = VBox().apply {
                    spacing = 10.0
                    padding = Insets(5.0)
                }
            })

            tabs.add(Tab().apply {
                isClosable = false
                text = "Output"
                content = VBox().apply {
                    spacing = 10.0
                    padding = Insets(5.0)
                }
            })
        })
    }

    private fun createInputTab(job: Job) = Tab().apply {
        isClosable = false
        text = "Input"
        content = VBox().apply {
            spacing = 10.0
            padding = Insets(5.0)

            children.add(HBox().apply {
                spacing = 5.0
                alignment = Pos.CENTER_LEFT

                children.add(Label("Dataset"))
                children.add(ValidatedNode(
                    ComboBox<Dataset>().apply {
                        disableProperty().bind(jobInProgressProperty)
                        cellFactory = Callback { DatasetCell() }
                        buttonCell = DatasetCell()
                        items.setAll(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull {
                            it.objectInstance
                        })
                        selectionModel.select(job.userDataset)
                        selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                            // Update our copy of the job and push it to the db when the user selects
                            // a new dataset
                            scope.launch {
                                jobProperty.value = jobDb.update(
                                    jobProperty.value.id,
                                    userDataset = newValue
                                )
                            }
                        }
                    },
                    {
                        if (it.selectionModel.selectedItem == null) {
                            ValidationResult.Error("Must select a dataset.")
                        } else {
                            ValidationResult.Success
                        }
                    }
                ))
            })

            children.add(VBox().apply {
                spacing = 10.0
                alignment = Pos.TOP_LEFT

                val exampleModelForm: ValidatedNode<out Node>
                val customModelForm: ValidatedNode<out Node>
                val jobModelForm: ValidatedNode<out Node>

                val modelDetails = StackPane().apply {
                    alignment = Pos.TOP_LEFT
                    exampleModelForm = ValidatedNode(
                        ComboBox<ExampleModel>().apply {
                            disableProperty().bind(jobInProgressProperty)
                            cellFactory = Callback { ExampleModelCell() }
                            buttonCell = ExampleModelCell()
                            items.setAll(exampleModels)
                            selectionModel.select(exampleModels.first())
                            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                if (newValue != null) {
                                    scope.launch {
                                        jobProperty.value = jobDb.update(
                                            jobProperty.value.id,
                                            userOldModelPath = ModelSource.FromExample(newValue)
                                        )
                                    }
                                }
                            }
                        },
                        {
                            if (it.isVisible && it.selectionModel.selectedItem == null)
                                ValidationResult.Error("Must select a model.")
                            else ValidationResult.Success
                        }
                    )
                    children.add(exampleModelForm)

                    customModelForm = ValidatedNode(
                        ComboBox<String>().apply {
                            disableProperty().bind(jobInProgressProperty)
                            isVisible = false
                        },
                        {
                            if (it.isVisible && it.selectionModel.selectedItem == null)
                                ValidationResult.Error("Must select a model.")
                            else ValidationResult.Success
                        }
                    )
                    children.add(customModelForm)

                    jobModelForm = ValidatedNode(
                        ComboBox<Job>().apply {
                            disableProperty().bind(jobInProgressProperty)
                            isVisible = false
                        },
                        {
                            if (it.isVisible && it.selectionModel.selectedItem == null)
                                ValidationResult.Error("Must select a model.")
                            else ValidationResult.Success
                        }
                    )
                    children.add(jobModelForm)
                }

                val modelComboBox = HBox().apply {
                    spacing = 5.0
                    alignment = Pos.CENTER_LEFT

                    children.add(Label("Model"))
                    val modelSelectionBox = ValidatedNode(
                        ComboBox<ModelChoice>().apply {
                            disableProperty().bind(jobInProgressProperty)
                            items.setAll(ModelChoice.values().toList())
                            selectionModel.select(ModelChoice.Example)
                            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                exampleModelForm.isVisible = false
                                customModelForm.isVisible = false
                                jobModelForm.isVisible = false
                                when (newValue) {
                                    ModelChoice.Example, null -> exampleModelForm.isVisible = true
                                    ModelChoice.Custom -> customModelForm.isVisible = true
                                    ModelChoice.Job -> jobModelForm.isVisible = true
                                }
                            }
                        },
                        {
                            if (it.selectionModel.selectedItem == null)
                                ValidationResult.Error("Must select a model.")
                            else ValidationResult.Success
                        },
                        setOf(exampleModelForm, customModelForm, jobModelForm)
                    )
                    children.add(modelSelectionBox)
                }

                children.add(modelComboBox)
                children.add(modelDetails)
            })
        }
    }
}