package edu.appstate.cs.dmtools.timeline

import javafx.beans.value.ObservableValue
import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.web.WebView
import org.slf4j.LoggerFactory
import java.util.*


/**
 * @author Alek Ratzloff <alekratz@gmail.com>
 */
class TimelineView : Region() {
    private val logger = LoggerFactory.getLogger(javaClass)
    val browser = WebView()
    val webEngine = browser.engine
    val toolBar = HBox()
    val newEventButton = Button("Add new event")
    val eventTemplateSelector = ChoiceBox<String>(
            FXCollections.observableArrayList(*TimelineEventFactory.templateNames()))
    val eventTemplateInput = HBox()

    val myTimeline = Timeline()
    var currentEvent: TimelineEvent? = null

    init {
        styleClass.add("browser")

        eventTemplateSelector.selectionModel.selectedIndexProperty().addListener {
            ov, old, new -> onEventTemplateSelection(ov, old, new)
        }
        eventTemplateSelector.prefWidth = 120.0
        eventTemplateSelector.minWidth = eventTemplateSelector.prefWidth
        newEventButton.onMouseClicked = EventHandler<MouseEvent> { e -> onNewEventButtonClicked(e) }
        newEventButton.prefWidth = 120.0
        newEventButton.minWidth = newEventButton.prefWidth

        toolBar.styleClass.add("browser-toolbar")
        toolBar.children.add(newEventButton)
        toolBar.children.add(eventTemplateSelector)
        toolBar.children.add(eventTemplateInput)

        children.add(toolBar)
        children.add(browser)

        reloadView()
    }

    fun reloadView() {
        val model = HashMap<String, Any>()
        model["css_dir"] = javaClass.getResource("/css")
        model["timeline_text"] = myTimeline.generateHTML()
        //model["img"] = javaClass.getResource("/img")

        val content = HtmlLoader.getTemplatedHTML("timelineview.ftl", model)

        webEngine.loadContent(content)
    }

    private fun onEventTemplateSelection(ov: ObservableValue<out Any?>?, old: Number?, new: Number?) {
        val whichTemplate = TimelineEventFactory.templateNames()[new!!.toInt()]
        if(currentEvent == null) currentEvent = TimelineEventFactory.getTemplate(whichTemplate)
        synchronized(currentEvent!!) {
            currentEvent = TimelineEventFactory.createNewEvent(whichTemplate)

            // clear the old template filler-outer
            eventTemplateInput.children.clear()
            for (field in currentEvent!!.fieldNames()) {
                val eventField = currentEvent!!.getField(field)!!
                val addedControl = when (eventField.inputType) {
                    InputType.TEXT_FIELD -> {
                        val textField = TextField()
                        textField.promptText = eventField.prompt
                        if (eventField.value != null)
                            textField.text = eventField.value as String

                        // Update the current template's values for this input
                        textField.textProperty().addListener { o ->
                            currentEvent!![textField.id] = textField.text
                        }

                        textField
                    }
                    InputType.TEXT_AREA -> {
                        val textArea = TextArea()
                        textArea.promptText = eventField.prompt
                        if (eventField.value != null)
                            textArea.text = eventField.value as String

                        // Update the current template's values for this input
                        textArea.textProperty().addListener { o ->
                            currentEvent!![textArea.id] = textArea.text
                        }

                        textArea
                    }
                    InputType.NUMBER_SPINNER -> {
                        val spinner = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, (eventField.value ?: 0) as Int)

                        // Update the current template's values for this input
                        spinner.valueProperty().addListener { o ->
                            currentEvent!![spinner.id] = spinner.value
                        }

                        spinner
                    }
                    InputType.NONE -> {
                        null
                    }
                }

                if (addedControl != null) {
                    addedControl.id = field
                    eventTemplateInput.children.add(addedControl)
                }
            }
        }
    }

    private fun onNewEventButtonClicked(event: MouseEvent) {
        if(event.button == MouseButton.PRIMARY) {
            for(input in eventTemplateInput.children) {
                val fieldName = input.id
                val fieldValue: Any? = when(input) {
                    is TextInputControl -> input.text
                    is Spinner<*> -> input.value as Int
                    else -> {
                        logger.warn("Field $fieldName is not a recognizable input control")
                        null
                    }
                }

                if(fieldValue != null)
                    currentEvent!![fieldName] = fieldValue
                // TODO: tooltip on else
            }

            myTimeline.add(currentEvent!!)
            reloadView()
        }
    }

    override fun layoutChildren() {
        val w = width.toDouble()
        val h = height.toDouble()
        val tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0.0, 0.0, w, h - tbHeight, 0.0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0.0, h - tbHeight, w, tbHeight, 0.0, HPos.CENTER, VPos.CENTER);
    }
}