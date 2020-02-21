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
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.Fragment
import tornadofx.ItemFragment
import tornadofx.bindTo
import tornadofx.booleanBinding
import tornadofx.borderpane
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.center
import tornadofx.chooseFile
import tornadofx.combobox
import tornadofx.enableWhen
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.hbox
import tornadofx.isInt
import tornadofx.label
import tornadofx.separator
import tornadofx.spinner
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox

class JobEditor : Fragment() {
    private val job by inject<JobModel>()

    override val root = borderpane {
        center {
            add<JobConfiguration>()
        }
        bottom = buttonbar {
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
    }
}

class JobConfiguration : Fragment("Configuration") {
    private val job by inject<JobModel>()
    private val datasetPluginManager by di<PluginManager>(datasetPluginManagerName)

    override val root = form {
        hbox(20) {
            vbox(20) {
                fieldset("Dataset") {
                    add<DatasetPicker>()
                    field("Plugin") {
                        combobox(job.datasetPlugin) {
                            items = datasetPluginManager.listPlugins().toList().toObservable()
                            cellFormat {
                                text = it.name.toLowerCase().capitalize()
                            }
                        }
                    }
                }
                separator()
                fieldset("Model") {
                    add<ModelPicker>()
                }
            }
            vbox(20) {
                fieldset {
                    field("Epochs") {
                        spinner(1, amountToStepBy = 1, editable = true, property = job.userEpochs) {
                            editor.apply {
                                filterInput {
                                    it.controlNewText.isInt()
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
            contentMap(dataset.type) {
                item(DatasetType.EXAMPLE) {
                    combobox(job.userDataset) {
                        items = Dataset.ExampleDataset::class.sealedSubclasses.map { it.objectInstance }
                                .toObservable()
                        cellFormat {
                            text = it.displayName
                        }
                    }
                }
                item(DatasetType.CUSTOM) {
                    vbox {
                        button {
                            setOnAction {
                                val file = chooseFile("Pick", arrayOf(FileChooser.ExtensionFilter("Any", "*.*")))
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
    }

    init {
        itemProperty.bind(job.userDataset)
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
        contentMap(modelSource.type) {
            item(ModelSourceType.EXAMPLE) {
                combobox(job.userOldModelPath) {
                    items = exampleModelManager.getAllExampleModels().unsafeRunSync().map {
                        ModelSource.FromExample(it)
                    }.toObservable()
                    cellFormat {
                        text = (it as? ModelSource.FromExample)?.exampleModel?.name ?: ""
                    }
                }
            }
            item(ModelSourceType.FILE) {
                vbox {
                    label(job.userOldModelPath, converter = object : StringConverter<ModelSource>() {
                        override fun toString(obj: ModelSource?) =
                                (obj as? ModelSource.FromFile)?.filePath?.toString() ?: ""
                        override fun fromString(string: String?) = null
                    })
                }
            }
            item(ModelSourceType.JOB) {
                vbox {
                    label("Job")
                }
            }
        }
    }

    init {
        itemProperty.bind(job.userOldModelPath)
    }
}
