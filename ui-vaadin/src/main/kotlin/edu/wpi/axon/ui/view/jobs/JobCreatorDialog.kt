package edu.wpi.axon.ui.view.jobs

import arrow.core.valid
import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.comboBox
import com.github.mvysny.karibudsl.v10.div
import com.github.mvysny.karibudsl.v10.formLayout
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.html.NativeButton
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import edu.wpi.axon.dbdata.Job
import edu.wpi.axon.examplemodel.ExampleModel
import edu.wpi.axon.examplemodel.ExampleModelManager
import edu.wpi.axon.examplemodel.downloadAndConfigureExampleModel
import edu.wpi.axon.tfdata.Dataset
import org.koin.core.KoinComponent
import org.koin.core.inject

class JobCreatorDialog : Dialog(), KoinComponent {

    private val binder = beanValidationBinder<Job>()
    private val exampleModelBinder = beanValidationBinder<ExampleModel>()
    private val exampleModelManager: ExampleModelManager by inject()

    init {
        isCloseOnEsc = false
        isCloseOnOutsideClick = false

        div {
            verticalLayout {
                formLayout {
                    textField("Job Name") {
                        bind(binder).asRequired().bind(Job::name)
                    }

                    comboBox<Dataset>("Training Dataset") {
                        bind(binder).asRequired().bind(Job::userDataset)
                    }

                    comboBox<ExampleModel>("Starting Model") {
                        val exampleModels = exampleModelManager.getAllExampleModels().unsafeRunSync()
                        setItems(exampleModels)
                        // TODO: Trying to use another binder here because Job::userOldModel is a String, not an ExampleModel
                        bind(exampleModelBinder).asRequired()
                    }
                }

                horizontalLayout {
                    button("Confirm", Icon(VaadinIcon.CHECK_CIRCLE)) {
                        val exampleModel = ExampleModel("", "", "", "", emptyMap())
                        if (exampleModelBinder.validate().isOk && exampleModelBinder.writeBeanIfValid(exampleModel)) {
                            // TODO: Create the job
                        }
                        close()
                    }

                    button("Cancel") {
                        close()
                    }
                }
            }
        }
    }
}
