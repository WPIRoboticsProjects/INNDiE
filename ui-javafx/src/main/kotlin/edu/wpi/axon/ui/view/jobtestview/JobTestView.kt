package edu.wpi.axon.ui.view.jobtestview

import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.testrunner.LocalTestRunner
import edu.wpi.axon.testrunner.TestData
import edu.wpi.axon.tfdata.Dataset
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.view.contentMap
import edu.wpi.axon.ui.view.jobresult.ResultFragment
import edu.wpi.axon.util.loadTestDataPluginManagerName
import edu.wpi.axon.util.processTestOutputPluginManagerName
import java.io.File
import javafx.scene.control.Label
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.Fragment
import tornadofx.action
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.chooseFile
import tornadofx.combobox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.hbox
import tornadofx.label
import tornadofx.listview
import tornadofx.objectBinding
import tornadofx.separator
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox

class JobTestView : Fragment() {

    private val job by inject<JobModel>()
    private val loadTestDataPluginManager by di<PluginManager>(loadTestDataPluginManagerName)
    private val processTestOutputPluginManager by di<PluginManager>(
        processTestOutputPluginManagerName
    )
    private val model = JobTestViewModel()
    private val testRunner = LocalTestRunner()

    override val root = borderpane {
        centerProperty().bind(job.itemProperty.objectBinding {
            if (it == null) {
                Label("No selection.")
            } else {
                bottom {
                    buttonbar {
                        button("Run Test") {
                            action {
                                // TODO: Start running the test async
                            }
                        }
                    }
                }
                vbox(20) {
                    form {
                        fieldset("Test Data") {
                            field("Type") {
                                combobox(model.testDataType) {
                                    items = TestDataType.values().toList().toObservable()
                                    valueProperty().addListener { _, _, newValue ->
                                        if (newValue == TestDataType.FROM_TRAINING_DATA) {
                                            model.testData.value =
                                                TestData.FromExampleDataset(job.userDataset.value)
                                        }
                                    }
                                    cellFormat {
                                        text = it.name.split("_").joinToString(" ") {
                                            it.toLowerCase().capitalize()
                                        }
                                    }
                                }
                            }

                            field("Selection") {
                                contentMap(model.testDataType) {
                                    item(TestDataType.FROM_TRAINING_DATA) {
                                        label(
                                            job.userDataset,
                                            converter = object : StringConverter<Dataset>() {
                                                override fun toString(p0: Dataset?) =
                                                    p0?.displayName
                                                        ?: "The Job is missing a dataset."

                                                override fun fromString(p0: String?) = null
                                            })
                                    }

                                    item(TestDataType.FROM_FILE) {
                                        hbox(10) {
                                            button("Choose File") {
                                                action {
                                                    val file = chooseFile(
                                                        "Pick",
                                                        arrayOf(
                                                            FileChooser.ExtensionFilter(
                                                                "Any",
                                                                "*.*"
                                                            )
                                                        )
                                                    )

                                                    file.firstOrNull()?.let {
                                                        model.testData.value =
                                                            TestData.FromFile(it.toPath())
                                                    }
                                                }
                                            }

                                            label(
                                                model.testData,
                                                converter = object : StringConverter<TestData>() {
                                                    override fun toString(p0: TestData?) =
                                                        when (p0) {
                                                            is TestData.FromFile ->
                                                                p0.filePath.fileName.toString()
                                                            else -> ""
                                                        }

                                                    override fun fromString(p0: String?) = null
                                                })
                                        }
                                    }
                                }
                            }
                        }

                        separator()

                        fieldset("Plugins") {
                            field("Load Test Data") {
                                combobox(model.loadTestDataPlugin) {
                                    items =
                                        loadTestDataPluginManager.listPlugins().toList()
                                            .toObservable()
                                    cellFormat {
                                        text = it.name.toLowerCase().capitalize()
                                    }
                                    validator { if (it == null) error("Must not be empty.") else null }
                                }
                            }

                            field("Process Test Output") {
                                combobox(model.processTestOutputPlugin) {
                                    items = processTestOutputPluginManager.listPlugins().toList()
                                        .toObservable()
                                    cellFormat {
                                        text = it.name.toLowerCase().capitalize()
                                    }
                                    validator { if (it == null) error("Must not be empty.") else null }
                                }
                            }
                        }
                    }

                    listview<File> {
                        // TODO: Bind the data to the test results
                    }

                    val resultFragment = find<ResultFragment>()
                    // TODO: Bind the data to the selection
                }
            }
        })
    }
}
