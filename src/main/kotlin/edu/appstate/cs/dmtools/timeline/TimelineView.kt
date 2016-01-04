package edu.appstate.cs.dmtools.timeline

import javafx.collections.FXCollections
import javafx.event.EventHandler
import javafx.geometry.HPos
import javafx.geometry.Side
import javafx.geometry.VPos
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
 *     Provides an entire view and toolbar for a timeline editor. Pretty slick, eh?
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

    /**
     * Sets up a few style classes, as well as adds the layout of the entire UI for the timeline. It finally reloads the
     * timeline's HTML browser view for the initial view.
     */
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

        updateCreateEventButton()
        reloadView()
    }

    ////////////////////////////////////
    // Utility methods and operations //
    ////////////////////////////////////

    /**
     * Reloads the view of the browser for the timeline. It also sets appropriate values that are used by the template
     * engine for HTML rendering.
     */
    public fun reloadView() {
        val model = HashMap<String, Any>()
        model["css_dir"] = javaClass.getResource("/css")
        model["timeline_text"] = myTimeline.generateHTML()
        //model["img"] = javaClass.getResource("/img")
        val content = HtmlLoader.getTemplatedHTML("timelineview.ftl", model)
        webEngine.loadContent(content)
    }

    /**
     * This ensures that the "create event button" in the class is enabled/disabled based on whether we have a timeline
     * event that's being edited, ergo different field inputs being on screen already.
     */
    private fun updateCreateEventButton() {
        newEventButton.isDisable = eventTemplateInput.children.size == 0
    }

    ///////////////////////////////////////////////////////////////////////////////
    // UI element events - this are more complicated than just a one-line lambda //
    ///////////////////////////////////////////////////////////////////////////////

    /**
     * Updates the text of the given field of the current timeline event we're editing. These values are seen directly
     * as Strings.
     *
     * @param sender the TextInputControl that raised the text input
     */
    private fun onEventTextInput(sender: TextInputControl) {
        // Update the current template's values for this input
        currentEvent[sender.id] = sender.text
        logger.trace("$currentEvent ${sender.id} property changed to ${currentEvent[sender.id]}")
    }

    /**
     * Updates the integer value of the given field of the current timeline event we're editing. These values are seen
     * as Ints. This is actually raised by the internal textbox <i>in</i> the spinner, so we want to look at its text
     * property, instead of the spinner.value property.
     *
     * @param sender the Spinner control that raised the number input event
     */
    private fun onEventNumberInput(sender: Spinner<*>) {
        val text = sender.editor.text
        try {
            currentEvent[sender.id] = text.toInt()
            logger.trace("$currentEvent ${sender.id} property changed to ${currentEvent[sender.id]}")
        } catch(ex: NumberFormatException) {
            logger.debug("Invalid number specified for ${sender.id}")
        }
    }

    /**
     * This is fired whenever the combobox of event types has a selection made. This creates all of the necessary inputs
     * for the event for the user to fill out.
     *
     * @param new the index of the item selected.
     */
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
        updateCreateEventButton()
    }

    /**
     * When the new event button is clicked, this event is fired. It validates the user input, and if all is good, it
     * sends the current event field we're editing to the actual timeline. It then renders the new view of the HTML
     * control, and clears all of the non-number input boxes.
     *
     * @param event the mouse event of the button being clicked
     */
    private fun onNewEventButtonClicked(event: MouseEvent) {
        // Don't bother if it's not a left click
        if(event.button != MouseButton.PRIMARY) return

        fun isNullOrEmpty(s: String?) = (s == null || s == "")
        val toClear = HashSet<TextInputControl>()
        val inputSuggestions = HashSet<TextInputControl>()
        var success = true

        for(input in eventTemplateInput.children) {
            val fieldName = input.id
            val fieldValueInput: TextInputControl? = when(input) {
                is TextInputControl -> input
                is Spinner<*> -> input.editor
                else -> {
                    logger.warn("Field $fieldName is not a recognizable input control")
                    null
                }
            }

            val fieldValue = fieldValueInput!!.text
            if(currentEvent.getField(fieldName)!!.required && isNullOrEmpty(fieldValue)) {
                inputSuggestions.add(fieldValueInput)
                success = false
            } else if(currentEvent.getField(fieldName)!!.clearOnCreate) {
                // Add this to the set of items to clear out, provided it's been filled out
                if(!isNullOrEmpty(fieldValue))
                    toClear.add(fieldValueInput)
            }
        }

        if(!success) {
            for (input in inputSuggestions) {
                val validator = ContextMenu()
                val menuitem = MenuItem("This input is\nrequired")
                menuitem.styleClass.add("validation-menu-item")
                validator.styleClass.add("validation-context-menu")
                validator.items.add(menuitem)
                validator.isAutoHide = true
                validator.show(input, Side.TOP, 0.0, 0.0)
            }
            return // don't actually do anything
        }

        // If the user adds multiple events of the same type, it won't change every event
        myTimeline.add(currentEvent.clone() as TimelineEvent)
        reloadView()
        toClear.forEach { t -> t.clear() }
    }

    /////////////////////
    // UI method hooks //
    /////////////////////

    /**
     * When the form is resized, this hook is called. This just aligns the browser and the toolbar appropriately.
     */
    override fun layoutChildren() {
        val w = width.toDouble()
        val h = height.toDouble()
        val tbHeight = toolBar.prefHeight(w);
        layoutInArea(browser, 0.0, 0.0, w, h - tbHeight, 0.0, HPos.CENTER, VPos.CENTER);
        layoutInArea(toolBar, 0.0, h - tbHeight, w, tbHeight, 0.0, HPos.CENTER, VPos.CENTER);
    }
}