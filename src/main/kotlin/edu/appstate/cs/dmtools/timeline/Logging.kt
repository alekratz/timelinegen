package edu.appstate.cs.dmtools.timeline

import org.slf4j.LoggerFactory

/**
 * This is a dummy class that is used for an internal logger object, which is used by static methods only.
 */
internal object StaticMethod {  }

/**
 * This is a logger used only by static methods.
 */
internal val staticLogger = LoggerFactory.getLogger(StaticMethod::class.java)