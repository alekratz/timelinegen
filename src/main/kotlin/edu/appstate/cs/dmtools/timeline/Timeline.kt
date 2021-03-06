package edu.appstate.cs.dmtools.timeline

import org.slf4j.LoggerFactory
import java.util.*

/**
 * @author Alek Ratzloff <alekratz@gmail.com>
 */
class Timeline {
    private val logger = LoggerFactory.getLogger(javaClass)

    val timeline = HashMap<Int, ArrayList<TimelineEvent>>()

    /**
     * Adds a timeline event to this timeline. Because events have their own assigned years, a year is not necessary to
     * pass to this method.
     * @param event the TimelineEvent to add to the timeline.
     */
    public fun add(event: TimelineEvent) {
        val year = event.year
        logger.debug("Adding event $event in year ${event["year"]}")
        timeline.putIfAbsent(year, arrayListOf<TimelineEvent>())
        timeline[year]!!.add(event)
    }

    /**
     * Generates the HTML representation for this timeline, specific to the CSS we're using.
     */
    public fun generateHTML(): String {
        var result: String = ""
        var leftRight = false // left = false, right = true

        val sortedTimeline = timeline.toSortedMap()

        for(year in sortedTimeline.keys) {
            // Add a year marker
            result += """<li class="highlight" style="text-align: center;"><b>Year $year</b></li>"""

            val events = timeline[year]!!
            for(event in events) {
                val side = if(leftRight) "right" else "left"
                result += """<li class="$side" style="background-color: ${event.color};">
                <h3>${event.title}</h3>
                <p>${event.text}</p>
                </li>"""
                leftRight = !leftRight
            }
        }

        return "<ul>$result</ul>"
    }
}