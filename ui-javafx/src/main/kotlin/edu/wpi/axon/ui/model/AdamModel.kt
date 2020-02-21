package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.optimizer.Optimizer
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import tornadofx.Commit
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue
import kotlin.reflect.KClass

class AdamDto(adam: Optimizer.Adam) {
    private val learningRateProperty = SimpleDoubleProperty(adam.learningRate)
    var learningRate by learningRateProperty
    private val beta1Property = SimpleDoubleProperty(adam.beta1)
    var beta1 by beta1Property
    private val beta2Property = SimpleDoubleProperty(adam.beta2)
    var beta2 by beta2Property
    private val epsilonProperty = SimpleDoubleProperty(adam.epsilon)
    var epsilon by epsilonProperty
    private val amsGradProperty = SimpleBooleanProperty(adam.amsGrad)
    var amsGrad by amsGradProperty
}

class AdamModel(private val optToSet: Property<Optimizer.Adam>) : ItemViewModel<AdamDto>() {
    val learningRate = bind(AdamDto::learningRate)
    val beta1 = bind(AdamDto::beta1)
    val beta2 = bind(AdamDto::beta2)
    val epsilon = bind(AdamDto::epsilon)
    val amsGrad = bind(AdamDto::amsGrad)

    override fun onCommit() {
        optToSet.value = optToSet.value.copy(
            learningRate = learningRate.value,
            beta1 = beta1.value,
            beta2 = beta2.value,
            epsilon = epsilon.value,
            amsGrad = amsGrad.value
        )
    }
}
