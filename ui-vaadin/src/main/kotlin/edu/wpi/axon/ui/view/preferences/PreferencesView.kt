package edu.wpi.axon.ui.view.preferences

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.router.PageTitle
import com.vaadin.flow.router.Route
import edu.wpi.axon.aws.preferences.PreferencesManager
import edu.wpi.axon.ui.MainLayout
import org.koin.core.KoinComponent
import org.koin.core.inject
import software.amazon.awssdk.services.ec2.model.InstanceType

@Route(layout = MainLayout::class)
@PageTitle("Preferences")
class PreferencesView : KComposite(), KoinComponent {
    private val preferencesManager by inject<PreferencesManager>()

    private val root = ui {
        verticalLayout {
            formLayout {
                comboBox<InstanceType>("Training Instance Type") {
                    setItems(InstanceType.knownValues().stream().sorted())
                    isPreventInvalidInput = true
                    isRequired = true
                    placeholder = InstanceType.T2_MICRO.toString()
                }
            }
        }
    }
}
