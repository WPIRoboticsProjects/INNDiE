package edu.wpi.axon.ui.event

import com.vaadin.flow.component.Component
import com.vaadin.flow.component.ComponentEvent

class EditEvent(source: Component?) : ComponentEvent<Component>(source, false)
