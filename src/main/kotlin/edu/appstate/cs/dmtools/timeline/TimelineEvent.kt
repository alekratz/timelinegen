package edu.appstate.cs.dmtools.timeline

import java.util.*
import kotlin.text.Regex

enum class InputType {
    TEXT_FIELD,
    TEXT_AREA,
    NUMBER_SPINNER,
    NONE,
}

/**
 * This is the class that carries all of the metadata for the controls to be created, what type the value is, and any /actual/ value used
 */
data class TimelineEventField(val prompt: String, val inputType: InputType = InputType.NONE, var value: Any? = null)

/**
 * @author Alek Ratzloff <alekratz@gmail.com>
 */
object TimelineEventFactory {
    val templates = HashMap<String, TimelineEvent>()

    init {
        val regularTemplate = linkedMapOf(
                Pair("title", TimelineEventField("Title", InputType.TEXT_FIELD)),
                Pair("year", TimelineEventField("Year", InputType.NUMBER_SPINNER)),
                Pair("text", TimelineEventField("Description", InputType.TEXT_AREA)),
                Pair("color", TimelineEventField("", InputType.NONE, "LightGray"))
        )

        val birthTemplate = linkedMapOf(
                Pair("title", TimelineEventField("Title", InputType.NONE, "%name% was born")),
                Pair("year", TimelineEventField("Year", InputType.NUMBER_SPINNER)),
                Pair("name", TimelineEventField("Name", InputType.TEXT_FIELD)),
                Pair("text", TimelineEventField("Description", InputType.TEXT_AREA)),
                Pair("color", TimelineEventField("", InputType.NONE, "LightCyan"))
        )

        val deathTemplate = linkedMapOf(
                Pair("title", TimelineEventField("Title", InputType.NONE, "%name% died")),
                Pair("year", TimelineEventField("Year", InputType.NUMBER_SPINNER)),
                Pair("name", TimelineEventField("Name", InputType.TEXT_FIELD)),
                Pair("text", TimelineEventField("Description", InputType.TEXT_AREA)),
                Pair("color", TimelineEventField("", InputType.NONE, "LightPink"))
        )

        templates["Regular Event"] = TimelineEvent(regularTemplate)
        templates["Birth Event"] = TimelineEvent(birthTemplate)
        templates["Death Event"] = TimelineEvent(deathTemplate)
    }

    fun createNewEvent(eventType: String): TimelineEvent {
        val template = templates[eventType] as TimelineEvent
        return template.clone() as TimelineEvent
    }
    fun getTemplate(which: String): TimelineEvent = templates[which] as TimelineEvent

    fun templateNames() = templates.keys.toTypedArray()
}

class TimelineEvent(val fields: LinkedHashMap<String, TimelineEventField>) : Cloneable {
    val title: String
        get() = formattedValue("title")!!
    val color: String
        get() = formattedValue("color")!!
    val text: String
        get() = formattedValue("text")!!
    val year: Int
        get() = get("year") as Int

    public override fun clone(): Any {
        val clone = LinkedHashMap<String, TimelineEventField>()
        for(k in fieldNames())
            clone[k] = getField(k)!!.copy()
        return TimelineEvent(clone)
    }

    fun formattedValue(field: String): String? {
        val fieldValue = get(field) as String
        return fieldValue.replace(Regex("%([^%]+)%")) { r ->
            val key = r.groups.last()!!.value
            get(key)?.toString() ?: ""
        }
    }

    fun fieldNames() = fields.keys.toTypedArray()
    fun getField(k: String): TimelineEventField? = fields[k]
    operator fun get(k: String): Any? = fields[k]?.value
    operator fun set(k: String, v: Any?) { fields[k]?.value = v }
}