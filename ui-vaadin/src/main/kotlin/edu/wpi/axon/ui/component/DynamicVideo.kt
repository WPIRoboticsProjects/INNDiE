package edu.wpi.axon.ui.component

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.Tag
import com.vaadin.flow.dom.Element
import com.vaadin.flow.server.AbstractStreamResource

@Tag("video")
class DynamicVideo : Component(), HasDynamicContent, HasSize, HasStyle {
    private class Source : Element("source"), HasDynamicContent {
        override fun clear() {
            removeAttribute("src")
        }

        override fun setSrc(source: AbstractStreamResource) {
            setAttribute("src", source)
        }
    }

    private val source = Source()

    var controls: Boolean
        get() = element.hasAttribute("controls")
        set(value) {
            if (value) {
                element.setAttribute("controls", "")
            } else {
                element.removeAttribute("controls")
            }
        }

    var volume: Double
        get() = element.getProperty("volume", 0.0)
        set(value) { element.setProperty("volume", value) }

    init {
        controls = true

        element.appendChild(source)
    }

    override fun clear() {
        source.clear()
    }

    override fun setSrc(source: AbstractStreamResource) {
        this.source.setSrc(source)
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).dynamicVideo(block: (@VaadinDsl DynamicVideo).() -> Unit = {}) =
        init(DynamicVideo(), block)
