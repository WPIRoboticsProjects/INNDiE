package edu.wpi.axon.ui.view.preferences

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.dialog
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.setPrimary
import com.github.mvysny.karibudsl.v10.textArea
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.dialog.Dialog
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.ui.view.HasNotifications

class PluginEditorDialog(pluginManager: PluginManager, onSave: (Plugin) -> Unit = {}) : KComposite(), HasNotifications {
    private val binder = beanValidationBinder<Plugin>()

    private lateinit var dialog: Dialog

    init {
        ui {
            dialog {
                dialog = this
                height = "75%"
                width = "75%"
                verticalLayout {
                    textField("Name") {
                        addValueChangeListener { binder.validate() }
                        bind(binder).asRequired()
                                .withValidator({ name -> !pluginManager.listPlugins().map { it.name }.contains(name) }, "A plugin with that name already exists!")
                                .bind(Plugin::name)
                    }
                    textArea("Content") {
                        addValueChangeListener { binder.validate() }
                        bind(binder).asRequired()
                                .bind(Plugin::contents)
                    }
                    horizontalLayout {
                        button("Cancel") {
                            onLeftClick {
                                this@dialog.close()
                            }
                        }
                        button("Add", Icon(VaadinIcon.PLUS_CIRCLE)) {
                            setPrimary()
                            isEnabled = false
                            binder.addStatusChangeListener { isEnabled = !it.hasValidationErrors() }
                            onLeftClick {
                                val plugin = Plugin.Unofficial("", "")
                                if (binder.validate().isOk && binder.writeBeanIfValid(plugin)) {
                                    pluginManager.addUnofficialPlugin(plugin)
                                    onSave.invoke(plugin)
                                    this@dialog.close()
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    fun open() = dialog.open()

    fun readBean(bean: Plugin) = binder.readBean(bean)
}