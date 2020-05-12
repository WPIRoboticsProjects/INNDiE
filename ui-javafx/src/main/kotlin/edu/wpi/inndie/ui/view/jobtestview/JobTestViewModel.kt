package edu.wpi.inndie.ui.view.jobtestview

import edu.wpi.inndie.plugin.Plugin
import edu.wpi.inndie.testrunner.TestData
import javafx.beans.property.SimpleObjectProperty
import tornadofx.ViewModel

class JobTestViewModel : ViewModel() {
    val testDataType = bind { SimpleObjectProperty<TestDataType>() }
    val testData = bind { SimpleObjectProperty<TestData>() }
    val loadTestDataPlugin = bind { SimpleObjectProperty<Plugin>() }
    val processTestOutputPlugin = bind { SimpleObjectProperty<Plugin>() }
}
