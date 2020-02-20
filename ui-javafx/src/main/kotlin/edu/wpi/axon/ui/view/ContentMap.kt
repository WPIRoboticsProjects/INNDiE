package edu.wpi.axon.ui.view

import javafx.beans.property.ObjectProperty
import javafx.beans.property.Property
import javafx.beans.property.SimpleObjectProperty
import javafx.event.EventTarget
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.layout.Pane
import tornadofx.attachTo
import tornadofx.onChange
import tornadofx.opcr

class ContentMap<T : Any>: Pane() {
    val map = mutableMapOf<T, Node>()

    fun item(type: T, op: Parent.() -> Unit): Pane {
        val pane = opcr(this, Pane(), op)
        map[type] = pane
        return pane
    }

    val valueProperty: ObjectProperty<T> = SimpleObjectProperty(this, "value")

    init {
        valueProperty.onChange {
            it?.let {
                children.setAll(map[it]!!)
            }
        }
    }
}

fun <T : Any> EventTarget.contentMap(property: Property<T>? = null, op: ContentMap<T>.() -> Unit = {}) = ContentMap<T>().attachTo(this, op) {
    if (property != null) it.valueProperty.bind(property)
}
