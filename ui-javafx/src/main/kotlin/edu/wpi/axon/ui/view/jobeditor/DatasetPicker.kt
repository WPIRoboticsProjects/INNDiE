package edu.wpi.axon.ui.view.jobeditor

import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.DatasetModel
import edu.wpi.axon.ui.model.DatasetType
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.view.contentMap
import edu.wpi.axon.util.FilePath
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.ItemFragment
import tornadofx.bindTo
import tornadofx.button
import tornadofx.chooseFile
import tornadofx.combobox
import tornadofx.field
import tornadofx.label
import tornadofx.toObservable
import tornadofx.tooltip
import tornadofx.vbox

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
                        tooltip(
                            """
                            Example datasets are simple, easy ways to test a model before curating a real dataset.
                            """.trimIndent()
                        )
                        items = Dataset.ExampleDataset::class.sealedSubclasses
                            .map { it.objectInstance }
                            .toObservable()
                        cellFormat {
                            text = it.displayName
                        }
                    }
                }
                item(DatasetType.CUSTOM) {
                    vbox(10) {
                        button("Choose Dataset File") {
                            setOnAction {
                                val file = chooseFile(
                                    "Pick",
                                    arrayOf(FileChooser.ExtensionFilter("Tar", "*.tar"))
                                )

                                file.firstOrNull()?.let {
                                    job.userDataset.value =
                                        Dataset.Custom(FilePath.Local(it.path), it.name)
                                }
                            }
                        }

                        label(job.userDataset, converter = object : StringConverter<Dataset>() {
                            override fun toString(obj: Dataset?) = when (obj) {
                                is Dataset.Custom -> obj.baseNameWithoutExtension
                                else -> ""
                            }

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
