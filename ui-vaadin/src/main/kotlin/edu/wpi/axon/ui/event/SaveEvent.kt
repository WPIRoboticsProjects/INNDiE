package edu.wpi.axon.ui.event

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEvent

class SaveEvent(source: Component?, fromClient: Boolean = false) : ComponentEvent<Component>(source, fromClient)
