package edu.wpi.axon.ui.view

import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.DatasetModel
import edu.wpi.axon.ui.model.DatasetType
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.model.ModelSourceModel
import edu.wpi.axon.ui.model.ModelSourceType
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.datasetPluginManagerName
import java.lang.NumberFormatException
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import javafx.scene.control.TitledPane
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.Fragment
import tornadofx.ItemFragment
import tornadofx.bindTo
import tornadofx.booleanBinding
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.chooseFile
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.fold
import tornadofx.form
import tornadofx.hbox
import tornadofx.label
import tornadofx.spinner
import tornadofx.squeezebox
import tornadofx.tabpane
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox
import tornadofx.visibleWhen

class JobCard : Fragment() {
    private val job by inject<JobModel>()

    override val root = vbox {
        add<JobCardHeader>()
        add<JobCardContent>()
    }
}

class JobCardHeader : Fragment() {
    private val job by inject<JobModel>()

    override val root = hbox {
        label(job.name) {
            alignment = Pos.CENTER_LEFT
        }
    }
}

class JobCardContent : Fragment() {
    private val job by inject<JobModel>()

    override val root = tabpane {
        tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE

        tab<JobConfiguration>()
        tab<JobTraining>()
        tab<JobTesting>()
    }
}

class JobConfiguration : Fragment("Configuration") {
    private val job by inject<JobModel>()
    private val datasetPluginManager by di<PluginManager>(datasetPluginManagerName)

    override val root = vbox {
        buttonbar {
            button("Revert") {
                enableWhen(job.dirty)
                setOnAction {
                    job.rollback()
                }
            }
            button("Save") {
                enableWhen(job.status.booleanBinding {
                    it == TrainingScriptProgress.NotStarted
                }.and(job.dirty))
                setOnAction {
                    job.commit()
                }
            }
        }
        squeezebox {
            fold("Inputs", expanded = true) { makeInputsFold() }
            fold("Training", expanded = true) { makeTrainingFold() }
        }
    }

    private fun TitledPane.makeInputsFold() = form {
        fieldset("Dataset") {
            add(find<DatasetPicker>().apply {
                itemProperty.bind(job.userDataset)
            })
            field("Plugin") {
                combobox(job.datasetPlugin) {
                    items =
                        datasetPluginManager.listPlugins().toList().toObservable()
                    cellFormat {
                        text = it.name.toLowerCase().capitalize()
                    }
                }
            }
        }

        fieldset("Model") {
            add(find<ModelPicker>().apply {
                itemProperty.bind(job.userOldModelPath)
            })
        }
    }

    private fun TitledPane.makeTrainingFold() = form {
        fieldset {
            field("Epochs") {
                spinner(
                    1,
                    amountToStepBy = 1,
                    editable = true,
                    property = job.userEpochs
                ) {
                    valueFactory.converter = object : StringConverter<Number>() {
                        override fun toString(obj: Number?) = obj?.toString() ?: ""

                        override fun fromString(string: String?) = try {
                            string?.toInt() ?: 1
                        } catch (ex: NumberFormatException) {
                            1
                        }
                    }

                    validator {
                        if (it == null) error("The epochs field is required.") else null
                    }
                }
            }
        }
    }
}

class JobTraining : Fragment("Training") {
    private val job by inject<JobModel>()

    override val root = vbox {
        buttonbar {
            button("Cancel") {
            }
            button("Run") {
            }
        }
    }
}

class JobTesting : Fragment("Testing") {
    private val job by inject<JobModel>()

    override val root = vbox {
        label("Nothing yet!")
    }
}

class DatasetPicker : ItemFragment<Dataset>() {
    private val job by inject<JobModel>()
    private val dataset = DatasetModel().bindTo(this)

    override val root = vbox {
        field("Type") {
            combobox(dataset.type) {
                items = DatasetType.values().toList().toObservable()
                cellFormat {
                    text = it.name.toLowerCase().capitalize()
                }
            }
        }
        field("Selection") {
            combobox(job.userDataset) {
                visibleWhen(dataset.type.booleanBinding { it == DatasetType.EXAMPLE })
                items = Dataset.ExampleDataset::class.sealedSubclasses.map { it.objectInstance }
                    .toObservable()
                cellFormat {
                    text = it.displayName
                }
            }
            vbox {
                visibleWhen(dataset.type.booleanBinding { it == DatasetType.CUSTOM })
                button {
                    setOnAction {
                        val file =
                            chooseFile("Pick", arrayOf(FileChooser.ExtensionFilter("Any", "*.*")))
                        file.firstOrNull()?.let {
                            job.userDataset.value = Dataset.Custom(FilePath.Local(it.path), it.name)
                        }
                    }
                }
                label(job.userDataset, converter = object : StringConverter<Dataset>() {
                    override fun toString(obj: Dataset?) = obj?.displayName ?: ""
                    override fun fromString(string: String) = null
                })
            }
        }
    }
}

class ModelPicker : ItemFragment<ModelSource>() {
    private val job by inject<JobModel>()
    private val modelSource = ModelSourceModel().bindTo(this)
    private val exampleModelManager by di<ExampleModelManager>()

    override val root = vbox {
        field("Source") {
            combobox(modelSource.type) {
                items = ModelSourceType.values().toList().toObservable()
                cellFormat {
                    text = it.name.toLowerCase().capitalize()
                }
            }
        }
        field("Selection") {
            combobox(job.userOldModelPath) {
                visibleWhen(modelSource.type.booleanBinding { it == ModelSourceType.EXAMPLE })
                items = exampleModelManager.getAllExampleModels().unsafeRunSync().map {
                    ModelSource.FromExample(it)
                }.toObservable()
                cellFormat {
                    text = (it as? ModelSource.FromExample)?.exampleModel?.name ?: ""
                }
            }
            vbox {
                visibleWhen(modelSource.type.booleanBinding { it == ModelSourceType.FILE })
                label(job.userOldModelPath, converter = object : StringConverter<ModelSource>() {
                    override fun toString(obj: ModelSource?) =
                        (obj as? ModelSource.FromFile)?.filePath?.toString() ?: ""
                    override fun fromString(string: String?) = null
                })
            }
        }
    }
}
