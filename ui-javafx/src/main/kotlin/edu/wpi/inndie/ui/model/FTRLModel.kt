package edu.wpi.inndie.ui.model

import edu.wpi.inndie.tfdata.optimizer.Optimizer
import javafx.beans.property.Property
import javafx.beans.property.SimpleDoubleProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class FTRLDto(ftrl: Optimizer.FTRL) {
    private val learningRateProperty = SimpleDoubleProperty(ftrl.learningRate)
    var learningRate by learningRateProperty
    private val learningRatePowerProperty = SimpleDoubleProperty(ftrl.learningRatePower)
    var learningRatePower by learningRatePowerProperty
    private val initialAccumulatorValueProperty = SimpleDoubleProperty(ftrl.initialAccumulatorValue)
    var initialAccumulatorValue by initialAccumulatorValueProperty
    private val l1RegularizationStrengthProperty =
        SimpleDoubleProperty(ftrl.l1RegularizationStrength)
    var l1RegularizationStrength by l1RegularizationStrengthProperty
    private val l2RegularizationStrengthProperty =
        SimpleDoubleProperty(ftrl.l2RegularizationStrength)
    var l2RegularizationStrength by l2RegularizationStrengthProperty
    private val l2ShrinkageRegularizationStrengthProperty =
        SimpleDoubleProperty(ftrl.l2ShrinkageRegularizationStrength)
    var l2ShrinkageRegularizationStrength by l2ShrinkageRegularizationStrengthProperty
}

class FTRLModel(private val optToSet: Property<Optimizer.FTRL>) : ItemViewModel<FTRLDto>() {
    val learningRate = bind(FTRLDto::learningRate)
    val learningRatePower = bind(FTRLDto::learningRatePower)
    val initialAccumulatorValue = bind(FTRLDto::initialAccumulatorValue)
    val l1RegularizationStrength = bind(FTRLDto::l1RegularizationStrength)
    val l2RegularizationStrength = bind(FTRLDto::l2RegularizationStrength)
    val l2ShrinkageRegularizationStrength = bind(FTRLDto::l2ShrinkageRegularizationStrength)

    override fun onCommit() {
        optToSet.value = optToSet.value.copy(
            learningRate = learningRate.value,
            learningRatePower = learningRatePower.value,
            initialAccumulatorValue = initialAccumulatorValue.value,
            l1RegularizationStrength = l1RegularizationStrength.value,
            l2RegularizationStrength = l2RegularizationStrength.value,
            l2ShrinkageRegularizationStrength = l2ShrinkageRegularizationStrength.value
        )
    }
}
