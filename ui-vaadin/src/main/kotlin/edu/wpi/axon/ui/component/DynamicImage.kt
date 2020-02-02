package edu.wpi.axon.ui.component

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.html.Image

class DynamicImage : Image(), HasDynamicContent {
    override fun clear() {
        src = ""
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).dynamicImage(block: (@VaadinDsl DynamicImage).() -> Unit = {})
        = init(DynamicImage(), block)
