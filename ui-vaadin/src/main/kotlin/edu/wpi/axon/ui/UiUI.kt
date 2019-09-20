package edu.wpi.axon.ui

import com.vaadin.flow.component.dependency.HtmlImport
import com.vaadin.flow.component.UI
import com.vaadin.flow.theme.lumo.Lumo
import com.vaadin.flow.theme.Theme

@HtmlImport("frontend://styles/ui-theme.html")
@Theme(Lumo::class)
class UiUI : UI()
