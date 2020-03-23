package edu.wpi.axon.ui.view.jobtestview

import edu.wpi.axon.db.data.InternalJobTrainingMethod
import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.testrunner.LocalTestRunner
import edu.wpi.axon.testrunner.TestData
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.ui.model.JobDto
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.view.contentMap
import edu.wpi.axon.ui.view.jobresult.LazyResult
import edu.wpi.axon.ui.view.jobresult.ResultFragment
import edu.wpi.axon.util.FilePath
import edu.wpi.axon.util.getLocalTestRunnerWorkingDir
import edu.wpi.axon.util.getLocalTrainingScriptRunnerWorkingDir
import edu.wpi.axon.util.loadTestDataPluginManagerName
import edu.wpi.axon.util.processTestOutputPluginManagerName
import javafx.beans.property.SimpleBooleanProperty
import java.io.File
import java.nio.file.Path
import java.nio.file.Paths
import javafx.collections.FXCollections
import javafx.event.EventTarget
import javafx.scene.control.SelectionMode
import javafx.scene.control.TreeCell
import javafx.scene.control.TreeItem
import javafx.scene.control.TreeView
import javafx.scene.layout.Priority
import javafx.stage.FileChooser
import javafx.util.StringConverter
import tornadofx.Fragment
import tornadofx.UIComponent
import tornadofx.action
import tornadofx.bind
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.button
import tornadofx.buttonbar
import tornadofx.chooseFile
import tornadofx.cleanBind
import tornadofx.clear
import tornadofx.combobox
import tornadofx.fail
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.hbox
import tornadofx.hgrow
import tornadofx.label
import tornadofx.objectBinding
import tornadofx.progressindicator
import tornadofx.required
import tornadofx.runAsyncWithOverlay
import tornadofx.separator
import tornadofx.success
import tornadofx.textarea
import tornadofx.toObservable
import tornadofx.validator
import tornadofx.vbox

data class TestResultFile(val file: File) {
    override fun toString(): String = file.name
}

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
                bottom { clear() }
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
                                    model.commit { runTest(jobDto) }
                                }
                            }
                        }

                        vbox(20) {
                            createForm(jobDto)
                            createResultView()
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

    private fun EventTarget.createForm(job: JobDto) = form {
        fieldset("Test Data") {
            field("Type") {
                combobox(model.testDataType) {
                    items = TestDataType.values().toList().toObservable()
                    required()
                    valueProperty().addListener { _, _, newValue ->
                        if (newValue == TestDataType.FROM_TRAINING_DATA) {
                            model.testData.value = TestData.FromDataset(job.userDataset)
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
                        label(job.userDataset.displayName)
                    }

                    item(TestDataType.FROM_FILE) {
                        hbox(10) {
                            button("Choose File").action {
                                val file = chooseFile(
                                    "Pick",
                                    arrayOf(FileChooser.ExtensionFilter("Any", "*.*"))
                                )

                                file.firstOrNull()?.let {
                                    model.testData.value = TestData.FromFile(it.toPath())
                                }
                            }

                            label(
                                model.testData,
                                converter = object : StringConverter<TestData>() {
                                    override fun toString(p0: TestData?) =
                                        when (p0) {
                                            is TestData.FromFile -> p0.filePath.fileName.toString()
                                            else -> ""
                                        }

                                    override fun fromString(p0: String?) = null
                                }
                            )
                        }
                    }
                }
            }
        }

        separator()

        fieldset("Plugins") {
            field("Load Test Data") {
                combobox(model.loadTestDataPlugin) {
                    items = loadTestDataPluginManager.listPlugins().toList().toObservable()
                    required()
                    cellFormat {
                        text = it.name.toLowerCase().capitalize()
                    }
                }
            }

            field("Process Test Output") {
                combobox(model.processTestOutputPlugin) {
                    items = processTestOutputPluginManager.listPlugins().toList().toObservable()
                    required()
                    cellFormat {
                        text = it.name.toLowerCase().capitalize()
                    }
                }
            }
        }
    }

    private fun EventTarget.createResultView() = hbox(10) {
        @Suppress("UNCHECKED_CAST")
        val root = TreeItem<Any>().apply {
            isExpanded = true
            children.bind(testResults) { workingDir, files ->
                (TreeItem(workingDir) as TreeItem<Any>).apply {
                    isExpanded = true
                    children.addAll(files.map {
                        TreeItem(TestResultFile(it)) as TreeItem<Any>
                    })
                }
            }
        }

        val resultFragment = find<ResultFragment>().apply {
            hgrow = Priority.ALWAYS
        }

        val treeView = TreeView(root).apply {
            isShowRoot = false
            selectionModel.selectionMode = SelectionMode.SINGLE
            isEditable = false
            resultFragment.data.bind(
                selectionModel.selectedItemProperty().objectBinding {
                    (it?.value as? TestResultFile)?.let {
                        LazyResult(it.file.name, lazy { it.file })
                    }
                })
        }

        add(treeView)
        add(resultFragment)
    }

    private fun UIComponent.runTest(job: JobDto) {
        val workingDir = getNextDirName(job.id)

        runAsyncWithOverlay {
            val localNewModelPath = when (job.internalTrainingMethod) {
                is InternalJobTrainingMethod.EC2 ->
                    modelManager.downloadModel(
                        ModelSource.FromFile(
                            FilePath.S3(job.userNewModelFilename)
                        )
                    ).path

                InternalJobTrainingMethod.Local ->
                    getLocalTrainingScriptRunnerWorkingDir(
                        job.id
                    ).resolve(job.userNewModelFilename)
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
        } success {
            testResults[workingDir.fileName.toString()] = it
        } fail {
            dialog("Test Failed to Run") {
                field("Error") {
                    textarea(it.localizedMessage) {
                        isEditable = false
                        isWrapText = true
                    }
                }
            }
        }
    }
}
