package edu.appstate.cs.dmtools.timeline

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.VPos
import javafx.scene.Node
import javafx.scene.control.*
import javafx.scene.input.MouseButton
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Region
import javafx.scene.layout.VBox
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
    var currentEvent: TimelineEvent = TimelineEvent(LinkedHashMap()) // Set this to a non-null dummy value

    init {
        styleClass.add("browser")

        eventTemplateSelector.selectionModel.selectedIndexProperty().addListener {
            ov, old, new -> onEventTemplateSelection(new)
        }
        eventTemplateSelector.prefWidth = 120.0
        eventTemplateSelector.minWidth = eventTemplateSelector.prefWidth
        newEventButton.onMouseClicked = EventHandler<MouseEvent> { e -> onNewEventButtonClicked(e) }
        newEventButton.prefWidth = 120.0
        newEventButton.minWidth = newEventButton.prefWidth

        toolBar.styleClass.add("browser-toolbar")
        toolBar.children.add(VBox(newEventButton, eventTemplateSelector))
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

    private fun onEventTextInput(sender: TextInputControl) {
        // Update the current template's values for this input
        currentEvent[sender.id] = sender.text
        logger.trace("$currentEvent ${sender.id} property changed to ${currentEvent[sender.id]}")
    }

    private fun onEventNumberInput(sender: Spinner<*>) {
        val text = sender.editor.text
        try {
            currentEvent[sender.id] = text.toInt()
            logger.trace("$currentEvent ${sender.id} property changed to ${currentEvent[sender.id]}")
        } catch(ex: NumberFormatException) {
            logger.debug("Invalid number specified for ${sender.id}")
        }
    }

    private fun onEventTemplateSelection(new: Number?) {
        val whichTemplate = TimelineEventFactory.templateNames()[new!!.toInt()]
        currentEvent = TimelineEventFactory.createNewEvent(whichTemplate)
        logger.trace("New current event: $currentEvent")

        // clear the old template filler-outer
        eventTemplateInput.children.clear()
        for (field in currentEvent.fieldNames()) {
            val eventField = currentEvent.getField(field)!!
            val addedControl = when (eventField.inputType) {
                InputType.TEXT_FIELD -> {
                    val textField = TextField()
                    textField.promptText = eventField.prompt
                    if (eventField.value != null)
                        textField.text = eventField.value as String
                    textField.textProperty().addListener { o -> onEventTextInput(textField) }
                    textField
                }
                InputType.TEXT_AREA -> {
                    val textArea = TextArea()
                    textArea.promptText = eventField.prompt
                    if (eventField.value != null)
                        textArea.text = eventField.value as String
                    // Update the current template's values for this input
                    textArea.textProperty().addListener { o -> onEventTextInput(textArea) }
                    textArea
                }
                InputType.NUMBER_SPINNER -> {
                    val spinner = Spinner<Int>(Int.MIN_VALUE, Int.MAX_VALUE, (eventField.value ?: 0) as Int)
                    spinner.isEditable = true
                    // Update the current template's values for this input
                    spinner.editor.textProperty().addListener { o -> onEventNumberInput(spinner as Spinner<*>) }
                    spinner
                }
                InputType.NONE -> null
            }

            if (addedControl != null) {
                addedControl.id = field
                eventTemplateInput.children.add(addedControl)
            }
        }
    }

    private fun onNewEventButtonClicked(event: MouseEvent) {
        fun isNullOrEmpty(s: String?) = (s == null || s == "")
        val toClear = HashSet<TextInputControl>()

        if(event.button == MouseButton.PRIMARY) {
            for(input in eventTemplateInput.children) {
                val fieldName = input.id
                /*
                 * TODO: figure out how to clear fields out. We have a few options.
                 *      * clear everything out.
                 *      * clear all fields except for number fields. the rationale being we don't want to clear out the "year" field
                 *      * add a specific check for the "year" field - this is a special case.
                 * For now, we do the second option - clear everything except for number fields.
                 */
                val fieldValue: String? = when(input) {
                    is TextInputControl -> {
                        toClear.add(input)
                        input.text
                    }
                    is Spinner<*> -> input.editor.text
                    else -> {
                        logger.warn("Field $fieldName is not a recognizable input control")
                        null
                    }
                }

                if(isNullOrEmpty(fieldValue)) {
                    currentEvent[fieldName] = "" // for now just set it to an empty string
                    // TODO: tooltip if field is null or empty, or check to see if the field is optional(?)
                }
            }

            // Add a clone of the actual current event to the timeline; if the user adds multiple events of the same
            // type, it won't change every event
            myTimeline.add(currentEvent.clone() as TimelineEvent)
            reloadView()
            // Clear all of the appropriate inputs out
            toClear.forEach { t -> t.clear() }
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