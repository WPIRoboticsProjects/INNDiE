package edu.wpi.axon.ui

import edu.wpi.axon.db.JobDb
import edu.wpi.axon.db.JobDbOp
import edu.wpi.axon.db.data.Job
import edu.wpi.axon.db.data.TrainingScriptProgress
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.control.ScrollBar
import javafx.scene.control.TableColumn
import javafx.scene.control.TableView
import javafx.scene.control.cell.PropertyValueFactory
import javafx.util.Callback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.core.KoinComponent
import org.koin.core.inject

class JobTableView : TableView<Job>(), KoinComponent {

    private val jobDb by inject<JobDb>()
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val dataCoroutineScope = CoroutineScope(Dispatchers.Main)
    private val data = FXCollections.observableArrayList<Job>()

    private fun getScrollBar(): ScrollBar? = lookupAll(".scroll-bar").firstOrNull {
        it is ScrollBar && it.orientation == Orientation.VERTICAL
    } as? ScrollBar

    init {
        isFocusTraversable = false
        isEditable = false
        // Disable individual cell selection. Still allows row selection.
        selectionModel.isCellSelectionEnabled = false
        items = data.sorted { o1, o2 -> o2.id.compareTo(o1.id) }

        // Subscribe to Job updates to keep up to date.
        jobDb.subscribe { op, job ->
            dataCoroutineScope.launch {
                when (op) {
                    JobDbOp.Create -> {
                        if (job !in data) {
                            data.add(job)
                        }
                    }

                    JobDbOp.Update -> {
                        val index = data.indexOfFirst { it.id == job.id }
                        if (index != -1) {
                            // Skip this update if the job wasn't in the list
                            data[index] = job
                        }
                    }

                    JobDbOp.Remove -> data.remove(job)
                }
            }
        }

        coroutineScope.launch {
            while (true) {
                val scrollBar: ScrollBar? = getScrollBar()
                if (scrollBar == null) {
                    dataCoroutineScope.launch { data.addAll(jobDb.fetch(20, 0)) }
                    delay(500) // Wait to let the UI render, otherwise we could load too much
                } else {
                    scrollBar.valueProperty().addListener { _, _, newValue ->
                        if (newValue.toDouble() > scrollBar.max * 0.80) {
                            dataCoroutineScope.launch {
                                data.addAll(jobDb.fetch(10, data.size - 1).filter { it !in data })
                            }
                        }
                    }
                    break
                }
            }
        }

        val idCol = TableColumn<Job, Int>("Id").apply {
            isFocusTraversable = false
            isEditable = false
            cellValueFactory = PropertyValueFactory("id")
            cellFactory = Callback { JobIdTableCell() }
        }

        val nameCol = TableColumn<Job, String>("Name").apply {
            isFocusTraversable = false
            isEditable = false
            minWidth = 80.0
            cellValueFactory = PropertyValueFactory("name")
            cellFactory = Callback { JobNameTableCell() }
        }

        val statusCol = TableColumn<Job, TrainingScriptProgress>("Status").apply {
            isFocusTraversable = false
            isEditable = false
            minWidth = 200.0
            cellValueFactory = PropertyValueFactory("status")
            cellFactory = Callback { JobTableStatusCell() }
        }

        columns.setAll(idCol, nameCol, statusCol)
    }
}
