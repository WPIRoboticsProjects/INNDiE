package edu.wpi.inndie.ui.view.jobeditor

import edu.wpi.inndie.training.ModelDeploymentTarget
import edu.wpi.inndie.ui.model.CoralDto
import edu.wpi.inndie.ui.model.CoralModel
import edu.wpi.inndie.ui.model.JobModel
import edu.wpi.inndie.ui.view.isDoubleInRange
import javafx.beans.property.Property
import javafx.util.StringConverter
import tornadofx.Fieldset
import tornadofx.Fragment
import tornadofx.ItemViewModel
import tornadofx.action
import tornadofx.button
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.isDouble
import tornadofx.label
import tornadofx.textfield
import tornadofx.validator

class TargetFragment : Fragment() {
    private val job by inject<JobModel>()
    lateinit var model: ItemViewModel<*>

    override val root = form {
        fieldset("Edit Target") {
            println("Loaded with target type: ${job.targetType.value}")
            println("Loaded with target: ${job.target.value}")
            model = when (val target = job.target.value) {
                is ModelDeploymentTarget.Desktop -> {
                    field {
                        label("Desktop has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }

                is ModelDeploymentTarget.Coral -> createCoralFields(target)
            }
        }

        button("Save") {
            action {
                model.commit {
                    close()
                }
            }
        }
    }

    private fun Fieldset.createCoralFields(target: ModelDeploymentTarget.Coral): ItemViewModel<*> {
        @Suppress("UNCHECKED_CAST")
        val coralModel = CoralModel(job.target as Property<ModelDeploymentTarget.Coral>).apply {
            item = CoralDto(target)
        }

        field("Representative Dataset Percentage") {
            textfield(
                coralModel.representativeDatasetPercentage,
                converter = object : StringConverter<Double>() {
                    override fun toString(obj: Double?) = obj?.let { it * 100 }?.toString()
                    override fun fromString(string: String?) =
                        string?.toDoubleOrNull()?.let { it / 100 }
                }) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    it.isDoubleInRange(0.0..100.0)
                }
            }
        }

        return coralModel
    }
}
