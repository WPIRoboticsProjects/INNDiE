package edu.wpi.axon.ui

import com.github.mvysny.kaributesting.v10.LocatorJ._get
import com.github.mvysny.kaributesting.v10.MockVaadin
import com.github.mvysny.kaributesting.v10.Routes
import com.github.mvysny.kaributesting.v10.getSuggestionItems
import com.github.mvysny.kaributesting.v10.setUserInput
import com.vaadin.flow.component.UI
import com.vaadin.flow.component.combobox.ComboBox
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class DatasetViewTest {

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

        UI.getCurrent().navigate("dataset")
    }

    @ParameterizedTest
    @ValueSource(strings = ["Boston Housing", "CIFAR-10", "Fashion MNIST", "MNIST"])
    fun testDatasetSelection(input: String) {
        val combobox = _get(ComboBox::class.java) { spec -> spec.withCaption("Dataset") }

        combobox.setUserInput(input)
        combobox.value = combobox.getSuggestionItems()[0]

        Assertions.assertNotNull(combobox.value)
    }
}
