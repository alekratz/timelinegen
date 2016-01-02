package edu.appstate.cs.dmtools.timeline

import java.util.*

/**
 * @author Alek Ratzloff <alekratz@gmail.com>
 */
class Timeline {
    val timeline = HashMap<Int, ArrayList<TimelineEvent>>()

    fun add(event: TimelineEvent) {
        val year = event.year
        timeline.putIfAbsent(year, arrayListOf<TimelineEvent>())
        timeline[year]!!.add(event)
    }

    fun generateHTML(): String {
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