package edu.wpi.axon.ui.view.jobtestview

import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.testrunner.LocalTestRunner
import edu.wpi.axon.testrunner.TestData
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.view.contentMap
import edu.wpi.axon.ui.view.jobresult.LazyResult
import edu.wpi.axon.ui.view.jobresult.ResultFragment
import edu.wpi.axon.util.getLocalTestRunnerWorkingDir
import edu.wpi.axon.util.loadTestDataPluginManagerName
import edu.wpi.axon.util.processTestOutputPluginManagerName
import java.io.File
import java.nio.file.Paths
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
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
    private val testResults = FXCollections.observableArrayList<File>()
    private val modelManager = ModelManager()

    override val root = borderpane {
        centerProperty().bind(job.itemProperty.objectBinding(job.status) { jobDto ->
            if (jobDto == null) {
                bottom { }
                Label("No selection.")
            } else {
                when (jobDto.status) {
                    TrainingScriptProgress.Completed -> {
                        bottom {
                            buttonbar {
                                button("Run Test").action {
                                    model.commit {
                                        val resultsTask = runAsync {
                                            val localModel = modelManager.downloadModel(
                                                ModelSource.FromFile(jobDto.userNewModelPath)
                                            )

                                            testRunner.runTest(
                                                trainedModelPath = Paths.get(localModel.path),
                                                testData = model.testData.value,
                                                loadTestDataPlugin = model.loadTestDataPlugin.value,
                                                processTestOutputPlugin = model.processTestOutputPlugin.value,
                                                workingDir = getLocalTestRunnerWorkingDir(jobDto.id)
                                            )
                                        }

                                        resultsTask.setOnSucceeded {
                                            val results = resultsTask.get()
                                            testResults.setAll(results)
                                        }
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
                                                        TestData.FromDataset(jobDto.userDataset)
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
                                                label(jobDto.userDataset.displayName)
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
                                                        converter = object :
                                                            StringConverter<TestData>() {
                                                            override fun toString(p0: TestData?) =
                                                                when (p0) {
                                                                    is TestData.FromFile ->
                                                                        p0.filePath.fileName.toString()
                                                                    else -> ""
                                                                }

                                                            override fun fromString(p0: String?) =
                                                                null
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
                                            items = processTestOutputPluginManager.listPlugins()
                                                .toList()
                                                .toObservable()
                                            cellFormat {
                                                text = it.name.toLowerCase().capitalize()
                                            }
                                            validator { if (it == null) error("Must not be empty.") else null }
                                        }
                                    }
                                }
                            }

                            hbox(10) {
                                val resultFragment = find<ResultFragment>()

                                listview(testResults) {
                                    selectionModel.selectionMode = SelectionMode.SINGLE
                                    isEditable = false
                                    resultFragment.data.bind(
                                        selectionModel.selectedItemProperty().objectBinding {
                                            it?.let {
                                                LazyResult(it.name, lazy { it })
                                            }
                                        }
                                    )
                                }

                                add(resultFragment)
                            }
                        }
                    }

                    is TrainingScriptProgress.Error ->
                        label("The Job completed erroneously. Look at the Results view for more information.")

                    TrainingScriptProgress.NotStarted -> label("The Job has not been started yet.")

                    TrainingScriptProgress.Creating, TrainingScriptProgress.Initializing ->
                        label("The Job is starting.")

                    is TrainingScriptProgress.InProgress -> label("The Job is training.")
                }
            }
        })
    }
}
