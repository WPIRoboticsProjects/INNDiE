package edu.wpi.axon.ui.view.jobresult

import edu.wpi.axon.db.data.TrainingScriptProgress
import edu.wpi.axon.ui.JobLifecycleManager
import edu.wpi.axon.ui.model.JobModel
import javafx.event.EventTarget
import javafx.scene.chart.NumberAxis
import javafx.scene.control.Label
import javafx.scene.control.SelectionMode
import javafx.scene.layout.VBox
import mu.KotlinLogging
import tornadofx.Fragment
import tornadofx.borderpane
import tornadofx.bottom
import tornadofx.label
import tornadofx.linechart
import tornadofx.listview
import tornadofx.multiseries
import tornadofx.objectBinding
import tornadofx.paddingAll
import tornadofx.textarea
import tornadofx.toObservable

class JobResultsView : Fragment() {

    private val job by inject<JobModel>()
    private val jobLifecycleManager by di<JobLifecycleManager>()

    private fun EventTarget.trainingLogLineChart(trainingLogData: String) =
        linechart(
            "Training Results",
            x = NumberAxis(),
            y = NumberAxis()
        ) {
            xAxis.label = "Epoch"

            val trainingLogLines = trainingLogData.lines()
            val colNames = trainingLogLines.first().split(',')

            @Suppress("SpreadOperator")
            multiseries(
                names = *colNames.mapNotNull {
                    if (it == "epoch") null else it
                }.toTypedArray()
            ) {
                val epochIndex = colNames.indexOf("epoch")
                trainingLogLines.drop(1).forEach {
                    if (it.isNotBlank()) {
                        val values = it.split(',')
                        @Suppress("SpreadOperator")
                        data(
                            values[epochIndex].toInt(),
                            *values.mapIndexedNotNull { index, data ->
                                if (index == epochIndex) null
                                else data.toDouble()
                            }.toTypedArray()
                        )
                    }
                }
            }
        }

    override val root = borderpane {
        centerProperty().bind(job.itemProperty.objectBinding(job.status) { jobDto ->
            if (jobDto == null) {
                Label("No selection.")
            } else {
                when (val status = jobDto.status) {
                    TrainingScriptProgress.Completed -> VBox().apply {
                        spacing = 10.0

                        LOGGER.debug {
                            "Loading results for Job id: ${jobDto.id}"
                        }

                        val jobResults = jobLifecycleManager.listResults(jobDto.id).toObservable()

                        LOGGER.debug {
                            "Job results:\n${jobResults.joinToString("\n")}"
                        }

                        val trainingLogResult = jobResults.firstOrNull { it == "trainingLog.csv" }

                        if (trainingLogResult != null) {
                            trainingLogLineChart(
                                jobLifecycleManager.getResult(jobDto.id, trainingLogResult)
                                    .readText()
                            )
                        }

                        listview<String> {
                            items = jobResults
                            selectionModel.selectionMode = SelectionMode.SINGLE
                            isEditable = false
                            selectionModel.selectedItemProperty().addListener { _, _, newValue ->
                                if (newValue == null) {
                                    bottom = null
                                } else {
                                    bottom {
                                        add(find<ResultFragment>(
                                            mapOf(ResultFragment::data to LazyResult(
                                                newValue,
                                                lazy {
                                                    jobLifecycleManager.getResult(
                                                        job.id.value.toInt(),
                                                        newValue
                                                    )
                                                }
                                            ))
                                        ))
                                    }
                                }
                            }
                        }
                    }

                    is TrainingScriptProgress.Error -> VBox().apply {
                        spacing = 10.0
                        label("Job completed erroneously. Error Log:")
                        textarea {
                            text = status.log
                            isEditable = false
                            isWrapText = true
                        }
                    }

                    TrainingScriptProgress.NotStarted -> Label("Job has not been started yet.")

                    TrainingScriptProgress.Creating, TrainingScriptProgress.Initializing ->
                        Label("Job is starting.")

                    is TrainingScriptProgress.InProgress -> VBox().apply {
                        trainingLogLineChart(status.progressLog)
                    }

                    null -> Label("No Job selected.")
                }.apply {
                    paddingAll = 10
                }
            }
        })
    }

    companion object {
        private val LOGGER = KotlinLogging.logger { }
    }
}
