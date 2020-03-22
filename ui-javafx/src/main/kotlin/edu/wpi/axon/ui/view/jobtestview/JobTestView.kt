package edu.wpi.axon.ui.view.jobtestview

import edu.wpi.axon.db.data.InternalJobTrainingMethod
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
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.getLocalTestRunnerWorkingDir
import edu.wpi.axon.util.getLocalTrainingScriptRunnerWorkingDir
import edu.wpi.axon.util.loadTestDataPluginManagerName
import edu.wpi.axon.util.processTestOutputPluginManagerName
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javafx.collections.FXCollections
import javafx.scene.control.Label
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.Fragment
import tornadofx.action
import tornadofx.bind
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
import tornadofx.hgrow
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.separator
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox

/**
 * Computes the next directory name inside the local test runner's working directory.
 *
 * @param jobId The ID of the Job being tested.
 * @return The working directory for this test.
 */
private fun getNextDirName(jobId: Int): Path {
    val workingDirPath = getLocalTestRunnerWorkingDir(jobId)
    val highestDirNumber = workingDirPath.toFile()
        .list()
        ?.mapNotNull { it.toIntOrNull() }
        ?.max() ?: 0
    return workingDirPath.resolve("${highestDirNumber + 1}")
}

private fun getExistingTestResults(jobId: Int): Map<String, List<File>> {
    val workingDirPath = getLocalTestRunnerWorkingDir(jobId)
    return workingDirPath.toFile()
        .list()
        ?.mapNotNull { it.toIntOrNull() }
        ?.map { workingDirPath.resolve(it.toString()).toFile() }
        ?.map { it.name to (it.resolve("output").listFiles()?.toList() ?: emptyList()) }
        ?.toMap() ?: emptyMap()
}

class JobTestView : Fragment() {

    private val job by inject<JobModel>()
    private val loadTestDataPluginManager by di<PluginManager>(loadTestDataPluginManagerName)
    private val processTestOutputPluginManager by di<PluginManager>(
        processTestOutputPluginManagerName
    )
    private val model = JobTestViewModel()
    private val testRunner = LocalTestRunner()
    private val testResults = FXCollections.observableHashMap<String, List<File>>()
    private val modelManager = ModelManager()

    override val root = borderpane {
        centerProperty().bind(job.itemProperty.objectBinding(job.status) { jobDto ->
            if (jobDto == null) {
                bottom { }
                testResults.clear()
                label("No selection.")
            } else {
                testResults.clear()
                testResults.putAll(getExistingTestResults(jobDto.id))
                when (jobDto.status) {
                    TrainingScriptProgress.Completed -> {
                        bottom {
                            buttonbar {
                                button("Run Test").action {
                                    model.commit {
                                        val workingDir = getNextDirName(jobDto.id)

                                        val resultsTask = runAsync {
                                            val localNewModelPath =
                                                when (jobDto.internalTrainingMethod) {
                                                    is InternalJobTrainingMethod.EC2 ->
                                                        modelManager.downloadModel(
                                                            ModelSource.FromFile(
                                                                FilePath.S3(jobDto.userNewModelFilename)
                                                            )
                                                        ).path

                                                    InternalJobTrainingMethod.Local ->
                                                        getLocalTrainingScriptRunnerWorkingDir(
                                                            jobDto.id
                                                        ).resolve(jobDto.userNewModelFilename)
                                                            .toAbsolutePath()
                                                            .toString()

                                                    InternalJobTrainingMethod.Untrained ->
                                                        error("The Job should have been trained by now.")
                                                }

                                            testRunner.runTest(
                                                trainedModelPath = Paths.get(localNewModelPath),
                                                testData = model.testData.value,
                                                loadTestDataPlugin = model.loadTestDataPlugin.value,
                                                processTestOutputPlugin = model.processTestOutputPlugin.value,
                                                workingDir = workingDir
                                            )
                                        }

                                        resultsTask.setOnSucceeded {
                                            val results = resultsTask.get()
                                            testResults[workingDir.fileName.toString()] = results
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
                                val root = TreeItem<Any>().apply {
                                    isExpanded = true
                                    children.bind(testResults) { workingDir, files ->
                                        (TreeItem(workingDir) as TreeItem<Any>).apply {
                                            isExpanded = true
                                            children.addAll(files.map {
                                                TreeItem(it) as TreeItem<Any>
                                            })
                                        }
                                    }
                                }

                                val resultFragment = find<ResultFragment>().apply {
                                    hgrow = Priority.ALWAYS
                                }

                                val treeView = TreeView(root).apply {
                                    isShowRoot = false
                                    resultFragment.data.bind(
                                        selectionModel.selectedItemProperty().objectBinding {
                                            (it?.value as? File)?.let {
                                                LazyResult(it.name, lazy { it })
                                            }
                                        })
                                }

                                add(treeView)
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
