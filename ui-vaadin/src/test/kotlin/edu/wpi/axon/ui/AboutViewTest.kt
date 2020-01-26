package edu.wpi.axon.ui

import com.github.mvysny.kaributesting.v10.LocatorJ._get
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.html.H1
import edu.wpi.axon.dsl.defaultBackendModule
import edu.wpi.axon.testutil.KoinTestFixture
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.koin.core.context.startKoin

class AboutViewTest : KoinTestFixture() {

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
        startKoin {
            modules(listOf(defaultBackendModule(), defaultFrontendModule()))
        }

        MockVaadin.setup(routes)

        UI.getCurrent().navigate("about")
    }

    @Test
    fun testAboutText() {
        _get(H1::class.java) { spec -> spec.withText("Created by: Austin & Ryan") }
    }
}
