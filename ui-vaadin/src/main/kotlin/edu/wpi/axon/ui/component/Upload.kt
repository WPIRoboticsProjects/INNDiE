package edu.wpi.axon.ui.component

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.init
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.upload.Receiver
import com.vaadin.flow.component.upload.Upload

@VaadinDsl
fun (@VaadinDsl HasComponents).upload(receiver: Receiver, block: (@VaadinDsl Upload).() -> Unit = {}): Upload =
        init(Upload(receiver), block)
