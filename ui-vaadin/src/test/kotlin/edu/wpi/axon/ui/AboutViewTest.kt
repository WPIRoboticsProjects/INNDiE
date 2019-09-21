package edu.wpi.axon.ui

import com.github.mvysny.kaributesting.v10.LocatorJ._get
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.H1
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class AboutViewTest {

    companion object {
        private lateinit var routes: Routes

        @BeforeAll
        @JvmStatic
        fun createRoutes() {
            routes = Routes().autoDiscoverViews("edu.wpi.axon.ui")
        }
    }

    @BeforeEach
    fun setupVaadin() {
        MockVaadin.setup(routes)

        UI.getCurrent().navigate("about")
    }

    @Test
    fun testAboutText() {
        _get(H1::class.java) { spec -> spec.withText("Created by: Austin & Ryan") }
    }
}
