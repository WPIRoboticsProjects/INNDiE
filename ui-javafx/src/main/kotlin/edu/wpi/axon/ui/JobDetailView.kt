package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.tfdata.Dataset
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleObjectProperty
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.AnchorPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.util.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class JobDetailView(
    job: Job,
    onClose: () -> Unit
) : AnchorPane(), KoinComponent {

    private val jobDb by inject<JobDb>()
    private val scope = CoroutineScope(Dispatchers.Default)
    private val jobProperty = SimpleObjectProperty<Job>()
    private val jobInProgressProperty = SimpleBooleanProperty()

    init {
        jobProperty.addListener { _, oldValue: Job?, newValue ->
            jobInProgressProperty.value = (newValue.status != TrainingScriptProgress.NotStarted)
            if (oldValue != null) {
                // Skip the first update to avoid deadlock
                scope.launch { jobDb.update(newValue) }
            }
        }

        jobProperty.value = job

        children.add(VBox().apply {
            spacing = 10.0
            padding = Insets(5.0)

            children.add(HBox().apply {
                maxWidth = Double.MAX_VALUE
                alignment = Pos.CENTER
                children.add(Label(job.name).apply {
                    font = Font.font(Font.getDefault().family, FontWeight.BOLD, 15.0)
                })
            })

            children.add(ComboBox<Dataset>().apply {
                disableProperty().bind(jobInProgressProperty)
                items.setAll(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull {
                    it.objectInstance
                })
                cellFactory = Callback { DatasetCell() }
                buttonCell = DatasetCell()
                selectionModel.select(job.userDataset)
                selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                    // Update our copy of the job and push it to the db when the user selects a new
                    // dataset
                    jobProperty.value = job.copy(userDataset = newValue)
                }
            })

            children.add(Button("Run").apply {
                disableProperty().bind(jobInProgressProperty)
                setOnAction {
                    jobProperty.value = job.copy(status = TrainingScriptProgress.Initializing)
                }
            })

            children.add(Button("Close").apply {
                setOnAction { onClose() }
            })
        })
    }
}
