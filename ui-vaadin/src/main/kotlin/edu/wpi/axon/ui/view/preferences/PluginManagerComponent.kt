package edu.wpi.axon.ui.view.preferences

import com.github.mvysny.karibudsl.v10.KComposite
import com.github.mvysny.karibudsl.v10.VaadinDsl
import com.github.mvysny.karibudsl.v10.button
import com.github.mvysny.karibudsl.v10.grid
import com.github.mvysny.karibudsl.v10.h3
import com.github.mvysny.karibudsl.v10.horizontalLayout
import com.github.mvysny.karibudsl.v10.init
import com.github.mvysny.karibudsl.v10.onLeftClick
import com.github.mvysny.karibudsl.v10.verticalLayout
import com.vaadin.flow.component.Component
import com.vaadin.flow.component.HasComponents
import com.vaadin.flow.component.HasSize
import com.vaadin.flow.component.button.Button
import com.vaadin.flow.component.button.ButtonVariant
import com.vaadin.flow.component.grid.ColumnTextAlign
import com.vaadin.flow.component.grid.GridVariant
import com.vaadin.flow.component.icon.Icon
import com.vaadin.flow.component.icon.VaadinIcon
import com.vaadin.flow.component.textfield.TextArea
import com.vaadin.flow.data.provider.CallbackDataProvider
import com.vaadin.flow.data.provider.CallbackDataProvider.CountCallback
import com.vaadin.flow.data.provider.CallbackDataProvider.FetchCallback
import com.vaadin.flow.data.renderer.ComponentRenderer
import com.vaadin.flow.data.renderer.TextRenderer
import edu.wpi.axon.plugin.Plugin
import edu.wpi.axon.plugin.PluginManager
import edu.wpi.axon.ui.view.HasNotifications
import java.util.function.Predicate

class PluginManagerComponent(title: String, pluginManager: PluginManager) : KComposite(), HasNotifications, HasSize {
    class PluginManagerDataProvider(pluginManager: PluginManager) :
        CallbackDataProvider<Plugin, Predicate<Plugin>>(FetchCallback<Plugin, Predicate<Plugin>> {
        pluginManager.listPlugins().stream()
                .filter(it.filter.orElse(Predicate { true }))
                .sorted { o1, o2 -> o1.name.compareTo(o2.name) }
                .skip(it.offset.toLong())
                .limit(it.limit.toLong())
    }, CountCallback<Plugin, Predicate<Plugin>> {
        pluginManager.listPlugins().stream()
                .filter(it.filter.orElse(Predicate { true }))
                .count()
                .toInt()
    })

    private val dataProvider = PluginManagerDataProvider(pluginManager)

    init {
        ui {
            verticalLayout {
                horizontalLayout {
                    setWidthFull()
                    h3(title) {
                        style["margin-right"] = "auto"
                    }
                    button("Add Plugin", Icon(VaadinIcon.PLUS_CIRCLE)) {
                        onLeftClick {
                            PluginEditorDialog(pluginManager) {
                                dataProvider.refreshAll()
                            }.apply {
                                open()
                            }
                        }
                    }
                }
                grid(dataProvider) {
                    addThemeVariants(GridVariant.LUMO_ROW_STRIPES, GridVariant.LUMO_NO_BORDER)
                    isHeightByRows = true
                    addColumn(TextRenderer { it.name })
                    addComponentColumn { plugin ->
                        Button(Icon(VaadinIcon.PENCIL)).apply {
                            if (plugin is Plugin.Unofficial) {
                                onLeftClick {
                                    PluginEditorDialog(pluginManager, plugin) {
                                        dataProvider.refreshAll()
                                    }.apply {
                                        open()
                                    }
                                }
                            } else {
                                isEnabled = false
                            }
                        }
                    }.apply {
                        textAlign = ColumnTextAlign.END
                    }
                    addComponentColumn { plugin ->
                        Button(Icon(VaadinIcon.TRASH)).apply {
                            addThemeVariants(ButtonVariant.LUMO_ERROR)
                            if (plugin is Plugin.Unofficial) {
                                onLeftClick {
                                    pluginManager.removeUnofficialPlugin(plugin)
                                    dataProvider.refreshAll()
                                }
                            } else {
                                isEnabled = false
                            }
                        }
                    }.apply {
                        textAlign = ColumnTextAlign.END
                    }
                    setItemDetailsRenderer(ComponentRenderer<Component, Plugin> { plugin ->
                        TextArea().apply {
                            setSizeFull()
                            value = plugin.contents
                            isReadOnly = true
                        }
                    })
                }
            }
        }
    }
}

@VaadinDsl
fun (@VaadinDsl HasComponents).pluginManagerComponent(title: String, pluginManager: PluginManager, block: (@VaadinDsl PluginManagerComponent).() -> Unit = {}) =
        init(PluginManagerComponent(title, pluginManager), block)
