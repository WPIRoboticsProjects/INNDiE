package edu.wpi.axon.ui.component

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.Tag
import com.vaadin.flow.dom.Element
import com.vaadin.flow.server.StreamResource
import java.io.InputStream

@Tag("video")
class Video(source: Source) : Component() {
    class Source(name: String, factory: () -> InputStream, type: String = "video/mp4") : Element("source") {
        init {
            setAttribute("type", type)
            setAttribute("src", StreamResource(name, factory))
        }
    }

    init {
        element.appendChild(source)
        element.setAttribute("controls", "")
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).video(source: Video.Source, block: (@VaadinDsl Video).() -> Unit = {}) = init(Video(source), block)
