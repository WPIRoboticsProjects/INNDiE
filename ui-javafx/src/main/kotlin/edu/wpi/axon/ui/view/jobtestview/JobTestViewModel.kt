package edu.wpi.axon.ui.view.jobtestview

import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.testrunner.TestData
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ViewModel

class JobTestViewModel : ViewModel() {
    val testDataType = bind { SimpleObjectProperty<TestDataType>() }
    val testData = bind { SimpleObjectProperty<TestData>() }
    val loadTestDataPlugin = bind { SimpleObjectProperty<Plugin>() }
    val processTestOutputPlugin = bind { SimpleObjectProperty<Plugin>() }
}
