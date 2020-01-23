//package edu.wpi.axon.ui.view.jobs
//
//import com.github.mvysny.karibudsl.v10.KComposite
//import com.github.mvysny.karibudsl.v10.VaadinDsl
//import com.github.mvysny.karibudsl.v10.beanValidationBinder
//import com.github.mvysny.karibudsl.v10.bind
//import com.github.mvysny.karibudsl.v10.button
//import com.github.mvysny.karibudsl.v10.checkBox
//import com.github.mvysny.karibudsl.v10.comboBox
//import com.github.mvysny.karibudsl.v10.div
//import com.github.mvysny.karibudsl.v10.formItem
//import com.github.mvysny.karibudsl.v10.formLayout
//import com.github.mvysny.karibudsl.v10.init
//import com.github.mvysny.karibudsl.v10.numberField
//import com.github.mvysny.karibudsl.v10.setPrimary
//import com.github.mvysny.karibudsl.v10.textField
//import com.github.mvysny.karibudsl.v10.toInt
//import com.github.mvysny.karibudsl.v10.verticalLayout
//import com.vaadin.flow.component.HasComponents
//import com.vaadin.flow.component.Key
//import com.vaadin.flow.component.button.Button
//import com.vaadin.flow.component.button.ButtonVariant
//import com.vaadin.flow.component.dependency.StyleSheet
//import com.vaadin.flow.component.formlayout.FormLayout
//import com.vaadin.flow.component.icon.Icon
//import com.vaadin.flow.component.icon.VaadinIcon
//import edu.wpi.axon.dbdata.Job
//import edu.wpi.axon.tfdata.Dataset
//
//@StyleSheet("styles/job-form.css")
//class JobForm(val viewLogic: JobsViewLogic) : KComposite() {
//    private lateinit var deleteButton: Button
//
//    private val binder = beanValidationBinder<Job>()
//    private var job: Job? = null
//
//    private val root = ui {
//        div {
//            className = "job-form"
//            verticalLayout {
//                className = "job-form-content"
//                setSizeUndefined()
//                formLayout {
//                    responsiveSteps = listOf(
//                            FormLayout.ResponsiveStep("0", 1, FormLayout.ResponsiveStep.LabelsPosition.TOP),
//                            FormLayout.ResponsiveStep("550px", 1, FormLayout.ResponsiveStep.LabelsPosition.ASIDE)
//                    )
//                    formItem("Name") {
//                        textField {
//                            setWidthFull()
//                            bind(binder).asRequired().bind(Job::name)
//                        }
//                    }
//                    formItem("Dataset") {
//                        comboBox<Dataset> {
//                            setWidthFull()
//                            setItems(Dataset.ExampleDataset::class.sealedSubclasses.mapNotNull { it.objectInstance })
//                            setItemLabelGenerator { it.displayName }
//                            bind(binder).asRequired().bind(Job::userDataset)
//                        }
//                    }
//                    formItem("Epochs") {
//                        numberField {
//                            setWidthFull()
//                            setHasControls(true)
//                            min = 1.0
//                            step = 1.0
//                            bind(binder).toInt().asRequired().bind(Job::userEpochs)
//                        }
//                    }
//                    formItem("Generate Debug Comments") {
//                        checkBox {
//                            bind(binder).bind(Job::generateDebugComments)
//                        }
//                    }
//                }
//                verticalLayout {
//                    button("Save", Icon(VaadinIcon.CHECK)) {
//                        setPrimary()
//                        addThemeVariants(ButtonVariant.LUMO_SUCCESS)
//                        isIconAfterText = true
//                        setWidthFull()
//                        binder.addStatusChangeListener { isEnabled = binder.hasChanges() && !it.hasValidationErrors() }
//                        addClickListener {
//                            binder.writeBeanIfValid(job)
//                            viewLogic.save(job!!)
//                        }
//                    }
//                    deleteButton = button("Delete", Icon(VaadinIcon.TRASH)) {
//                        addThemeVariants(ButtonVariant.LUMO_ERROR)
//                        isIconAfterText = true
//                        setWidthFull()
//                        addClickListener {
//                        }
//                    }
//                    button("Cancel") {
//                        isIconAfterText = true
//                        setWidthFull()
//                        addClickShortcut(Key.ESCAPE)
//                        addClickListener {
//                            viewLogic.clear()
//                        }
//                    }
//                    button("Run", Icon(VaadinIcon.PLAY)) {
//                        isIconAfterText = true
//                        setWidthFull()
//                        addClickListener {
//                            viewLogic.runJob(job!!)
//                        }
//                    }
//                }
//            }
//        }
//    }
//
//    fun createJob() {
//        binder.removeBean()
//        deleteButton.isVisible = false
//    }
//
//    fun editJob(job: Job) {
//        binder.readBean(job)
//        this.job = job
//        deleteButton.isVisible = true
//    }
//}
//
//@VaadinDsl
//fun (@VaadinDsl HasComponents).jobForm(viewLogic: JobsViewLogic, block: (@VaadinDsl JobForm).() -> Unit = {}): JobForm = init(JobForm(viewLogic), block)
