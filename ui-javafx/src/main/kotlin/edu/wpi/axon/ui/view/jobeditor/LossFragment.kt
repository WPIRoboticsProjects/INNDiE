package edu.wpi.axon.ui.view.jobeditor

import edu.wpi.axon.tfdata.loss.Loss
import edu.wpi.axon.ui.model.JobModel
import tornadofx.Fragment
import tornadofx.ItemViewModel
import tornadofx.action
import tornadofx.button
import tornadofx.field
import tornadofx.fieldset
import tornadofx.form
import tornadofx.label

class LossFragment : Fragment() {
    private val job by inject<JobModel>()
    lateinit var model: ItemViewModel<*>

    override val root = form {
        fieldset("Edit Loss") {
            println("Loaded with loss type: ${job.lossType.value}")
            println("Loaded with loss: ${job.userLoss.value}")
            model = when (val loss = job.userLoss.value) {
                is Loss.CategoricalCrossentropy -> {
                    field {
                        label("CategoricalCrossentropy has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }

                is Loss.SparseCategoricalCrossentropy -> {
                    field {
                        label("SparseCategoricalCrossentropy has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }

                is Loss.MeanSquaredError -> {
                    field {
                        label("MeanSquaredError has no data.")
                    }
                    object : ItemViewModel<Unit>() {}
                }
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
}
