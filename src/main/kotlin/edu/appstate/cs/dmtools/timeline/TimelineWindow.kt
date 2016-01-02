package edu.appstate.cs.dmtools.timeline

/**
 * @author Alek Ratzloff <alekratz@gmail.com>
 */
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage

class TimelineWindow : Application() {

    val timelineView = TimelineView()

    override fun start(primaryStage: Stage) {
        
        primaryStage.title = "Timeline Creator"

        // Set up scene
        val scene = Scene(timelineView, 800.0, 600.0)
        scene.stylesheets.add("css/browser.css")
        primaryStage.scene = scene

        primaryStage.show()
    }

}