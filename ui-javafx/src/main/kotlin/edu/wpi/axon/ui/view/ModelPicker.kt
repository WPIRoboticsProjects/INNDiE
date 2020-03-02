package edu.wpi.axon.ui.view

import edu.wpi.axon.db.data.ModelSource
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.ui.ModelManager
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.model.ModelSourceType
import edu.wpi.axon.util.FilePath
import java.io.File
import javafx.stage.FileChooser
import javafx.stage.Modality
import javafx.util.StringConverter
import tornadofx.ItemFragment
import tornadofx.action
import tornadofx.bind
import tornadofx.button
import tornadofx.chooseFile
import tornadofx.combobox
import tornadofx.field
import tornadofx.label
import tornadofx.toObservable
import tornadofx.vbox

class ModelPicker : ItemFragment<ModelSource>() {

    private val job by inject<JobModel>()
    private val exampleModelManager by di<ExampleModelManager>()
    private val modelManager by di<ModelManager>()

    init {
        job.oldModelType.addListener { _, _, newValue ->
            val newOldModelType = when (newValue) {
                ModelSourceType.EXAMPLE -> if (job.userOldModelPath
                        .value !is ModelSource.FromExample
                ) null else job.userOldModelPath.value
                ModelSourceType.FILE -> if (job.userOldModelPath
                        .value !is ModelSource.FromFile
                ) null else job.userOldModelPath.value
                ModelSourceType.JOB -> if (job.userOldModelPath
                        .value !is ModelSource.FromJob
                ) null else job.userOldModelPath.value
                null -> null
            }

            job.userOldModelPath.value = newOldModelType
        }
    }

    override val root = vbox {
        field("Source") {
            combobox(job.oldModelType) {
                items = ModelSourceType.values().toList().toObservable()
                cellFormat {
                    text = it.name.toLowerCase().capitalize()
                }
            }
        }
        field {
            contentMap(job.oldModelType) {
                item(ModelSourceType.EXAMPLE) {
                    combobox(job.userOldModelPath) {
                        items = exampleModelManager.getAllExampleModels().unsafeRunSync().map {
                            ModelSource.FromExample(it)
                        }.toObservable()

                        cellFormat {
                            text = (it as? ModelSource.FromExample)?.exampleModel?.name ?: ""
                        }

                        valueProperty().addListener { _, _, newValue ->
                            if (newValue != null && newValue is ModelSource.FromExample && job.isDirty) {
                                job.userNewModel.value = modelManager.loadModel(newValue)
                            }
                        }
                    }
                }

                item(ModelSourceType.FILE) {
                    vbox(10) {
                        label(
                            job.userOldModelPath,
                            converter = object : StringConverter<ModelSource>() {
                                override fun toString(obj: ModelSource?) =
                                    when (val path = (obj as? ModelSource.FromFile)?.filePath) {
                                        is FilePath.Local -> "Local: ${path.path}"
                                        is FilePath.S3 -> "S3: ${path.path}"
                                        else -> ""
                                    }

                                override fun fromString(string: String?) = null
                            }) {
                            isWrapText = true
                            maxWidth = 450.0
                        }

                        button("Choose a Local File") {
                            action {
                                val file = chooseFile(
                                    title = "Choose a Local File",
                                    filters = arrayOf(
                                        FileChooser.ExtensionFilter("HDF5", "*.h5", "*.hdf5")
                                    ),
                                    initialDirectory = job.userOldModelPath
                                        .value
                                        .localFileOrNull()
                                        ?.parentFile
                                ).firstOrNull()

                                if (file != null) {
                                    val modelSource =
                                        ModelSource.FromFile(FilePath.Local(file.path))
                                    job.userOldModelPath.value = modelSource
                                    job.userNewModel.value = modelManager.loadModel(modelSource)
                                }
                            }
                        }
                    }
                }

                item(ModelSourceType.JOB) {
                    vbox {
                        label("Job")
                    }
                }
            }
        }

        field {
            button("Edit Model") {
                action {
                    find<LayerEditorFragment>().openModal(modality = Modality.WINDOW_MODAL)
                }
            }
        }
    }

    init {
        itemProperty.bind(job.userOldModelPath)
    }
}

fun ModelSource?.localFileOrNull(): File? = when (this) {
    is ModelSource.FromFile -> when (filePath) {
        is FilePath.Local -> File(filePath.path)
        else -> null
    }

    else -> null
}
