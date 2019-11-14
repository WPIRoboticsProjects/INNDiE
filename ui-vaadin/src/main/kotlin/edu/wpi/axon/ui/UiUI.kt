package edu.wpi.axon.ui

import com.vaadin.flow.component.UI
import com.vaadin.flow.component.dependency.HtmlImport
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo

@HtmlImport("frontend://styles/ui-theme.html")
@Theme(Lumo::class)
class UiUI : UI()
