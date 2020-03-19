package edu.wpi.axon.ui.view.jobeditor

import edu.wpi.axon.tfdata.optimizer.Optimizer
import edu.wpi.axon.ui.model.AdamDto
import edu.wpi.axon.ui.model.AdamModel
import edu.wpi.axon.ui.model.FTRLDto
import edu.wpi.axon.ui.model.FTRLModel
import edu.wpi.axon.ui.model.JobModel
import edu.wpi.axon.ui.view.isDoubleGreaterThanOrEqualToZero
import edu.wpi.axon.ui.view.isDoubleLessThanOrEqualToZero
import edu.wpi.axon.ui.view.textfield
import javafx.beans.property.Property
import tornadofx.Fieldset
import tornadofx.Fragment
import tornadofx.ItemViewModel
import tornadofx.action
import tornadofx.button
import tornadofx.checkbox
import tornadofx.field
import tornadofx.fieldset
import tornadofx.filterInput
import tornadofx.form
import tornadofx.isDouble
import tornadofx.validator

class OptimizerFragment : Fragment() {
    private val job by inject<JobModel>()
    lateinit var model: ItemViewModel<*>

    override val root = form {
        fieldset("Edit Optimizer") {
            println("Loaded with opt type: ${job.optimizerType.value}")
            println("Loaded with opt: ${job.userOptimizer.value}")

            require(job.optimizerType.value == job.userOptimizer.value::class)

            model = when (val opt = job.userOptimizer.value) {
                is Optimizer.Adam -> createAdamFields(opt)
                is Optimizer.FTRL -> createFTRLFields(opt)
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

    private fun Fieldset.createAdamFields(opt: Optimizer.Adam): ItemViewModel<*> {
        @Suppress("UNCHECKED_CAST")
        val adamModel = AdamModel(job.userOptimizer as Property<Optimizer.Adam>).apply {
            item = AdamDto(opt)
        }

        field("Learning Rate") {
            textfield(adamModel.learningRate) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Beta 1") {
            textfield(adamModel.beta1) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Beta 2") {
            textfield(adamModel.beta2) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Epsilon") {
            textfield(adamModel.epsilon) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("AMS Grad") {
            checkbox(property = adamModel.amsGrad)
        }

        return adamModel
    }

    private fun Fieldset.createFTRLFields(opt: Optimizer.FTRL): ItemViewModel<*> {
        @Suppress("UNCHECKED_CAST")
        val ftrlModel = FTRLModel(job.userOptimizer as Property<Optimizer.FTRL>).apply {
            item = FTRLDto(opt)
        }

        field("Learning Rate") {
            textfield(ftrlModel.learningRate) {
                filterInput { it.controlNewText.isDouble() }
            }
        }
        field("Learning Rate Power") {
            textfield(ftrlModel.learningRatePower) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    it.isDoubleLessThanOrEqualToZero()
                }
            }
        }
        field("Initial Accumulator Value") {
            textfield(ftrlModel.initialAccumulatorValue) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    it.isDoubleGreaterThanOrEqualToZero()
                }
            }
        }
        field("L1 Regularization Strength") {
            textfield(ftrlModel.l1RegularizationStrength) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    it.isDoubleGreaterThanOrEqualToZero()
                }
            }
        }
        field("L2 Regularization Strength") {
            textfield(ftrlModel.l2RegularizationStrength) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    it.isDoubleGreaterThanOrEqualToZero()
                }
            }
        }
        field("L2 Shrinkage Regularization Strength") {
            textfield(ftrlModel.l2ShrinkageRegularizationStrength) {
                filterInput { it.controlNewText.isDouble() }
                validator {
                    it.isDoubleGreaterThanOrEqualToZero()
                }
            }
        }

        return ftrlModel
    }
}
