package edu.appstate.cs.dmtools.timeline

import freemarker.template.Configuration
import freemarker.template.TemplateExceptionHandler
import java.io.StringWriter

/**
 * @author Alek Ratzloff <alekratz@gmail.com>
 *     Loads an HTML resource (templated or otherwise)
 */
object HtmlLoader {
    val cfg = Configuration(Configuration.VERSION_2_3_23)

    init {
        cfg.setClassForTemplateLoading(Any::class.java, "/html")
        cfg.defaultEncoding = "UTF-8"

        // TODO : change this to RETHROW_HANDLER when we're not actively writing HTML anymore
        cfg.templateExceptionHandler = TemplateExceptionHandler.HTML_DEBUG_HANDLER
    }

    fun getTemplatedHTML(source: String, model: Map<String, Any>): String {
        val template = cfg.getTemplate(source)
        val out = StringWriter()
        template.process(model, out)
        return out.toString()
    }
}
