package edu.wpi.axon.ui.controller.preferences

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.beanValidationBinder
import com.github.mvysny.karibudsl.v10.bind
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.dialog
import com.github.mvysny.karibudsl.v10.horizontalAlignSelf
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.setPrimary
import com.github.mvysny.karibudsl.v10.textArea
import com.github.mvysny.karibudsl.v10.textField
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.orderedlayout.FlexComponent
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.ui.controller.HasNotifications

class PluginEditorDialog(
    pluginManager: PluginManager,
    bean: Plugin? = null,
    onSave: (Plugin) -> Unit = {}
) : KComposite(), HasNotifications {
    private val binder = beanValidationBinder<Plugin>()

    init {
        ui {
            dialog {
                width = "50vw"
                verticalLayout {
                    textField("Name") {
                        setWidthFull()
                        addValueChangeListener { binder.validate() }
                        bind(binder).asRequired()
                            .withValidator(
                                { name ->
                                    !pluginManager.listPlugins().map { it.name }.contains(
                                        name
                                    ) || name == bean?.name
                                },
                                "A plugin with that name already exists!"
                            ).bind(Plugin::name)
                    }
                    textArea("Content") {
                        setWidthFull()
                        height = "40vh"
                        addValueChangeListener { binder.validate() }
                        bind(binder).asRequired()
                            .bind(Plugin::contents)
                    }
                    horizontalLayout {
                        horizontalAlignSelf = FlexComponent.Alignment.END
                        button("Cancel") {
                            onLeftClick {
                                this@dialog.close()
                            }
                        }
                        button("Save", Icon(VaadinIcon.CHECK_CIRCLE)) {
                            setPrimary()
                            isEnabled = false
                            binder.addStatusChangeListener { isEnabled = !it.hasValidationErrors() }
                            onLeftClick {
                                val plugin = Plugin.Unofficial("", "")
                                if (binder.validate().isOk && binder.writeBeanIfValid(plugin)) {
                                    if (bean == null) {
                                        pluginManager.addUnofficialPlugin(plugin)
                                    } else {
                                        pluginManager.modifyUnofficialPlugin(bean.name, plugin)
                                    }
                                    onSave.invoke(plugin)
                                    this@dialog.close()
                                }
                            }
                        }
                    }
                }
                open()
            }
        }

        binder.readBean(bean)
    }
}
