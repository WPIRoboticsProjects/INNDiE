package edu.wpi.axon.ui.view

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.DatasetModel
import edu.wpi.axon.ui.model.DatasetType
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.util.FilePath
import javafx.geometry.Pos
import javafx.scene.control.TabPane
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

    override val root = vbox {
        buttonbar {
            button("Revert") {
                enableWhen(job.dirty)
                setOnAction {
                    job.rollback()
                }
            }
            button("Save") {
                enableWhen(job.dirty)
                setOnAction {
                    job.commit()
                }
            }
        }
        squeezebox {
            fold("Inputs", expanded = true) {
                form {
                    add(find<DatasetPicker>().apply {
                        itemProperty.bind(job.userDataset)
                    })
                }
            }
            fold("Training", expanded = true) {
                form {
                    fieldset {
                        field("Epochs") {
                            spinner(1, amountToStepBy = 1, editable = true, property = job.userEpochs)
                            // TODO: Handle number format exception
                        }
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

    override val root = fieldset("Dataset") {
        field("Type") {
            combobox<DatasetType>(dataset.type) {
                items = DatasetType.values().toList().toObservable()
                cellFormat {
                    text = it.name.toLowerCase().capitalize()
                }
            }
        }
        field("Selection") {
            combobox<Dataset>(job.userDataset) {
                visibleWhen(dataset.type.booleanBinding { it == DatasetType.EXAMPLE })
                items = Dataset.ExampleDataset::class.sealedSubclasses.map { it.objectInstance }.toObservable()
                cellFormat {
                    text = it.displayName
                }
            }
            vbox {
                visibleWhen(dataset.type.booleanBinding { it == DatasetType.CUSTOM })
                button {
                    setOnAction {
                        val file = chooseFile("Pick", arrayOf(FileChooser.ExtensionFilter("Any", "*.*")))
                        file.firstOrNull()?.let {
                            job.userDataset.value = Dataset.Custom(FilePath.Local(it.path), it.name)
                        }
                    }
                }
                label(job.userDataset, converter = object: StringConverter<Dataset>() {
                    override fun toString(obj: Dataset?) = obj?.displayName ?: ""
                    override fun fromString(string: String) = null
                })
            }
        }
    }
}
