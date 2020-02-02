package edu.wpi.axon.ui.component

import com.vaadin.flow.server.AbstractStreamResource
import com.vaadin.flow.server.InputStreamFactory
import com.vaadin.flow.server.StreamResource
import java.io.File
import java.io.FileInputStream

interface HasDynamicContent {
    fun clear()

    fun setFile(source: File) {
        setSrc(StreamResource(source.name, InputStreamFactory { FileInputStream(source) }))
    }

    fun setSrc(source: AbstractStreamResource)
}