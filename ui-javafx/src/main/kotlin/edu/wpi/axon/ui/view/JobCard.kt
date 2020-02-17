package edu.wpi.axon.ui.view

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.DatasetModel
import edu.wpi.axon.ui.model.DatasetType
import edu.wpi.axon.ui.model.JobModel
import javafx.geometry.Pos
import javafx.scene.control.TabPane
import tornadofx.Fragment
import tornadofx.ItemFragment
import tornadofx.bindTo
import tornadofx.booleanBinding
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.fold
import tornadofx.form
import tornadofx.hbox
import tornadofx.label
import tornadofx.squeezebox
import tornadofx.tabpane
import tornadofx.toObservable
import tornadofx.vbox

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

        tab<JobDetails>()
        tab<JobConfiguration>()
    }
}

class JobDetails : Fragment("Details") {
    private val job by inject<JobModel>()

    override val root = squeezebox {
        fold("Details", expanded = true) {
            vbox {
                label("Nothing here")
            }
        }
    }
}

class JobConfiguration : Fragment("Configuration") {
    private val job by inject<JobModel>()

    override val root = squeezebox {
        fold("Inputs", expanded = true) {
            form {
                add(find<DatasetPicker>().apply {
                    itemProperty.bind(job.userDataset)
                })
            }
        }
        fold("Training") {
            label("Nothing here")
        }
    }
}

class DatasetPicker : ItemFragment<Dataset>() {
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
            combobox<Dataset>(dataset.itemProperty) {
                visibleProperty().bind(dataset.type.booleanBinding { it == DatasetType.EXAMPLE })
                items = Dataset.ExampleDataset::class.sealedSubclasses.map { it.objectInstance }.toObservable()
                cellFormat {
                    text = it.displayName
                }
            }
        }
    }
}
