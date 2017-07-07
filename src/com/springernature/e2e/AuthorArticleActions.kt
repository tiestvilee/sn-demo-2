package com.springernature.e2e

import com.google.gson.Gson
import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.*
import org.http4k.routing.path
import org.jooq.Configuration
import org.neo4j.graphdb.GraphDatabaseService
import java.util.*

interface Jsonable {
    fun toJson(): String {
        return Gson().toJson(this)
    }
}

interface TransactionEvent : Jsonable {

}

fun createArticle(configuration: Configuration, graphDbInTransaction: GraphDatabaseService): HttpHandler = { request ->
    val id = ManuscriptId(UUID.randomUUID())

    logEvent(configuration, id, CreateManuscript(originalContent))

    processEvents(configuration, graphDbInTransaction)

    Response(Status.Companion.SEE_OTHER)
        .header("Location", "/article/${id.raw}")
}

@Suppress("unused")
fun <IN> BiDiLensSpec<IN, String, String>.checkbox() = FormField.map(
    {
        when (it) {
            "on" -> true
            else -> false
        }
    },
    { if (it) "on" else "off" }
)

val titleParam = FormField.string().required("title")
val abstractParam = FormField.string().required("abstract")
val approvedParam = FormField.checkbox().optional("approved")
val actionParam = FormField.string().required("action")
val selectionStartParam = FormField.string().required("selectionStart")
val selectionEndParam = FormField.string().required("selectionEnd")
val formSelector = FormField.string().required("formSelector")

val forms = listOf("title", "abstract", "authors")

fun updateTitle(configuration: Configuration, graphDbInTransaction: GraphDatabaseService): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(configuration, id)

    val webForm = Body.webForm(FormValidator.Strict, titleParam, actionParam, formSelector, approvedParam, selectionStartParam, selectionEndParam).toLens()(request)
    val title = MarkUp(titleParam(webForm).replace(Regex("<p[^>]*>"), "").replace(Regex("</p[^>]*>"), ""))
    val action = actionParam(webForm)
    val selector = formSelector(webForm)
    val approved = approvedParam(webForm)
    val selection = selectionFrom(webForm)

    if (action != "revert") {
        val newManuscript = manuscript.copy(
            title = updateMarkUpFragment(title, approved ?: false, manuscript.title, selection)
        )
        updateMarkupFragment(newManuscript, manuscript, configuration, "title", newManuscript.title, graphDbInTransaction)
    }

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/${formSuffix(action, selector, "title")}")
}

fun updateAbstract(dataContext: Configuration, graphDbInTransaction: GraphDatabaseService): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val webForm = Body.webForm(FormValidator.Strict, abstractParam, actionParam, formSelector, approvedParam, selectionStartParam, selectionEndParam).toLens()(request)
    val abstract = MarkUp(abstractParam(webForm))
    val action = actionParam(webForm)
    val selector = formSelector(webForm)
    val approved = approvedParam(webForm)
    val selection = selectionFrom(webForm)

    if (action != "revert") {
        val newManuscript = manuscript.copy(
            abstract = updateMarkUpFragment(abstract, approved ?: false, manuscript.abstract, selection)
        )
        updateMarkupFragment(newManuscript, manuscript, dataContext, "abstract", newManuscript.abstract, graphDbInTransaction)
    }

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/${formSuffix(action, selector, "abstract")}")
}

fun updateAuthors(dataContext: Configuration, graphDbInTransaction: GraphDatabaseService): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))
    val manuscript = Database.retrieveManuscript(dataContext, id)

    val webForm = Body.webForm(FormValidator.Strict, actionParam, formSelector, approvedParam, selectionStartParam, selectionEndParam).toLens()(request)
    val action = actionParam(webForm)
    val selector = formSelector(webForm)

    val approved = approvedParam(webForm)
    val selection = selectionFrom(webForm)

    if (action != "revert") {
        val authors = Authors(originalDocumentLocation = selection, approved = approved ?: false)
        println("updating authors = ${authors}")
        if (authors != manuscript.authors) {
            logEvent(dataContext, manuscript.id, SetAuthorsFragment(authors))
            processEvents(dataContext, graphDbInTransaction)
        }
    }

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/${formSuffix(action, selector, "authors")}")
}

private fun formSuffix(action: String, selector: String, currentForm: String): String =
    when (action) {
        "next" -> forms[minOf(forms.indexOf(currentForm) + 1, forms.size - 1)]
        "previous" -> forms[maxOf(forms.indexOf(currentForm) - 1, 0)]
        "selected" -> selector
        else -> currentForm
    }

private fun updateMarkupFragment(newManuscript: Manuscript, manuscript: Manuscript, dataContext: Configuration, fragmentName: String, fragment: MarkUpFragment, graphDbInTransaction: GraphDatabaseService) {
    if (newManuscript != manuscript) {
        logEvent(dataContext, manuscript.id, SetMarkupFragment(fragmentName, fragment))
        processEvents(dataContext, graphDbInTransaction)
    }
}

fun updateContentFrom(manuscript: Manuscript): Manuscript {
    val doc = Xml.document("<root>${manuscript.originalContent.raw}</root>")
    val titleRange = manuscript.title.originalDocumentLocation ?: (-1..-1)
    val abstractRange = manuscript.abstract.originalDocumentLocation ?: (-1..-1)
    val authorsRange = manuscript.authors.originalDocumentLocation ?: (-1..-1)

    val nodes = doc.documentElement.childNodes
    var index = 0
    while (index < nodes.length) {
        val node = nodes.item(index)
        if (node.hasAttributes()) {
            val nodeIndex = Integer.parseInt(node.attributes.getNamedItem("data-index").nodeValue)
            when (nodeIndex) {
                in titleRange -> doc.documentElement.removeChild(node)
                in abstractRange -> doc.documentElement.removeChild(node)
                in authorsRange -> doc.documentElement.removeChild(node)
                else -> {
                    index++
                }
            }
        } else {
            index++
        }
    }

    return manuscript
        .copy(content = manuscript.content
            .copy(markUp = MarkUp(doc.asString()
                .replace("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?>", "")
                .replace("<root>", "")
                .replace("</root>", ""))))
}

fun selectionFrom(webForm: WebForm): IntRange? {
    val start = selectionStartParam(webForm)
    val end = selectionEndParam(webForm)

    return if (start.isNotEmpty() && end.isNotEmpty()) {
        IntRange(start.toInt(), end.toInt())
    } else {
        null
    }
}

private fun updateMarkUpFragment(markUp: MarkUp, approved: Boolean, markUpFragment: MarkUpFragment, selection: IntRange?): MarkUpFragment {
    val updateMarkup = markUpFragment.copy(markUp = markUp, originalDocumentLocation = selection)
    return updateMarkup.copy(approved = if (updateMarkup.valid) approved else false)
}

