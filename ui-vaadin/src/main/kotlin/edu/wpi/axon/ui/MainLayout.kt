package edu.wpi.axon.ui

import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.drawer
import com.github.mvysny.karibudsl.v10.drawerToggle
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.icon
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.karibudsl.v10.label
import com.github.mvysny.karibudsl.v10.navbar
import com.github.mvysny.karibudsl.v10.span
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.applayout.AppLayout
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import com.vaadin.flow.component.page.Push
import com.vaadin.flow.router.RouterLink
import com.vaadin.flow.server.PWA
import com.vaadin.flow.theme.Theme
import com.vaadin.flow.theme.lumo.Lumo
import edu.wpi.axon.ui.view.about.AboutView
import edu.wpi.axon.ui.view.jobs.JobsView
import edu.wpi.axon.ui.view.preferences.PreferencesView
import edu.wpi.axon.ui.view.test.TestView
import kotlin.reflect.KClass

/**
 * The main layout of the application.
 */
@Push
@Theme(value = Lumo::class)
@PWA(name = "Axon", shortName = "axon")
class MainLayout : AppLayout() {
    init {
        UI.getCurrent().page.addStyleSheet("styles/shared-styles.css")
        UI.getCurrent().page.addStyleSheet("styles/menu.css")

        navbar {
            drawerToggle {
                className = "menu-toggle"
            }
            horizontalLayout {
                className = "menu-header"
                label("Axon")
                defaultVerticalComponentAlignment = FlexComponent.Alignment.CENTER
            }
        }
        drawer {
            menuLink(VaadinIcon.CONTROLLER, "Jobs", JobsView::class)
            menuLink(VaadinIcon.FLASK, "Test", TestView::class)
            menuLink(VaadinIcon.COG, "Preferences", PreferencesView::class)
            menuLink(VaadinIcon.INFO, "About", AboutView::class)
        }
    }

    private fun (@VaadinDsl HasComponents).menuLink(
        icon: VaadinIcon? = null,
        text: String? = null,
        viewType: KClass<out Component>,
        block: (@VaadinDsl RouterLink).() -> Unit = {}
    ): RouterLink {
        val link = RouterLink(null, viewType.java)
        if (icon != null) link.icon(icon)
        if (text != null) link.span(text)
        link.className = "menu-link"
        init(link, block)
        return link
    }
}
