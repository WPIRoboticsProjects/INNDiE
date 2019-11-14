package edu.wpi.axon.ui

import com.github.mvysny.kaributesting.v10.LocatorJ._click
import com.github.mvysny.kaributesting.v10.LocatorJ._get
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.kaributesting.v10.expectNotifications
import com.vaadin.flow.component.button.Button
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class UiViewTest {

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
        MockVaadin.setup(routes!!)
    }

    @Test
    fun testGreeting() {
        // simulate a button click as if clicked by the user
        _click(_get(Button::class.java) { spec -> spec.withCaption("Click me") })

        // look up the Example Template and assert on its value
        expectNotifications("Clicked!")
    }
}
