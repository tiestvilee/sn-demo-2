package com.springernature.e2e

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.*
import org.http4k.routing.path
import org.jooq.DSLContext
import java.util.*

fun createArticle(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.randomUUID())

    dataContext.insertInto(Database.manuscript, Database.manuscriptId).values(id.raw).execute()

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
val formSelector = FormField.string().required("formSelector")

fun updateTitle(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val webForm = Body.webForm(FormValidator.Strict, titleParam, actionParam, formSelector, approvedParam).toLens()(request)
    val title = MarkUp(titleParam(webForm))
    val action = actionParam(webForm)
    val selector = formSelector(webForm)
    val approved = approvedParam(webForm)

    if (action != "revert") {
        val newManuscript = manuscript.copy(
            title = updateMarkUpFragment(title, approved ?: false, manuscript.title)
        )
        println("newManuscript = ${newManuscript}")
        Database.saveManuscriptToDb(dataContext, newManuscript)
    }
    val formSuffix = when (action) {
        "next" -> "abstract"
        "selected" -> selector
        else -> "title"
    }

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/$formSuffix")
}

fun updateAbstract(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val manuscript = Database.retrieveManuscript(dataContext, id)

    val webForm = Body.webForm(FormValidator.Strict, abstractParam, actionParam, formSelector, approvedParam).toLens()(request)
    val abstract = MarkUp(abstractParam(webForm))
    val action = actionParam(webForm)
    val selector = formSelector(webForm)
    val approved = approvedParam(webForm)

    if (action != "revert") {
        val newManuscript = manuscript.copy(
            abstract = updateMarkUpFragment(abstract, approved ?: false, manuscript.abstract)
        )
        println("newManuscript = ${newManuscript}")
        Database.saveManuscriptToDb(dataContext, newManuscript)
    }
    val formSuffix = when (action) {
        "previous" -> "title"
        "selected" -> selector
        else -> "abstract"
    }

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/$formSuffix")
}

private fun updateMarkUpFragment(markUp: MarkUp, approved: Boolean, markUpFragment: MarkUpFragment): MarkUpFragment {
    val updateMarkup = markUpFragment.copy(markUp = markUp)
    return updateMarkup.copy(approved = if (updateMarkup.valid) approved else false)
}

