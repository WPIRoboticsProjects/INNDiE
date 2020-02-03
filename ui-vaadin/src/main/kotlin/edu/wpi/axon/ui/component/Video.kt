package edu.wpi.axon.ui.component

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.HasStyle
import com.vaadin.flow.component.Tag
import com.vaadin.flow.dom.Element
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import java.io.File
import java.io.FileInputStream

@Tag("video")
class Video(file: File) : Component(), HasSize, HasStyle {
    private class Source(file: File) : Element("source") {
        init {
            setAttribute("src", StreamResource(file.name, InputStreamFactory { FileInputStream(file) }))
        }
    }

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

        element.appendChild(Source(file))
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).video(file: File, block: (@VaadinDsl Video).() -> Unit = {}) = init(Video(file), block)
