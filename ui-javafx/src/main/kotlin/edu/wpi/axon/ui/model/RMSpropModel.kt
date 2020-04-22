package edu.wpi.axon.ui.model

import edu.wpi.axon.tfdata.optimizer.Optimizer
import javafx.beans.property.Property
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.property.SimpleDoubleProperty
import tornadofx.ItemViewModel
import tornadofx.getValue
import tornadofx.setValue

class RMSpropDto(adam: Optimizer.RMSprop) {
    private val learningRateProperty = SimpleDoubleProperty(adam.learningRate)
    var learningRate by learningRateProperty
    private val rhoProperty = SimpleDoubleProperty(adam.rho)
    var rho by rhoProperty
    private val momentumProperty = SimpleDoubleProperty(adam.momentum)
    var momentum by momentumProperty
    private val epsilonProperty = SimpleDoubleProperty(adam.epsilon)
    var epsilon by epsilonProperty
    private val centeredProperty = SimpleBooleanProperty(adam.centered)
    var centered by centeredProperty
}

class RMSpropModel(private val optToSet: Property<Optimizer.RMSprop>) :
    ItemViewModel<RMSpropDto>() {
    val learningRate = bind(RMSpropDto::learningRate)
    val rho = bind(RMSpropDto::rho)
    val momentum = bind(RMSpropDto::momentum)
    val epsilon = bind(RMSpropDto::epsilon)
    val centered = bind(RMSpropDto::centered)

    override fun onCommit() {
        optToSet.value = optToSet.value.copy(
            learningRate = learningRate.value,
            rho = rho.value,
            momentum = momentum.value,
            epsilon = epsilon.value,
            centered = centered.value
        )
    }
}
