package com.springernature.e2e

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.lens.FormField
import org.http4k.lens.FormValidator
import org.http4k.lens.string
import org.http4k.lens.webForm
import org.http4k.routing.path
import org.jooq.DSLContext
import java.util.*

fun createArticle(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.randomUUID())

    dataContext.insertInto(db.manuscript, db.manuscriptId).values(id.raw).execute()

    Response(Status.Companion.SEE_OTHER)
        .header("Location", "/article/${id.raw}")
}

val titleParam = FormField.string().required("title")
val actionParam = FormField.string().required("action")
val formSelector = FormField.string().required("formSelector")

fun updateTitle(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val record = dataContext.select(db.title).from(db.manuscript).where(db.manuscriptId.eq(id.raw)).fetchOne()
    val manuscript = Manuscript(id, MarkUp(record.value1() ?: "Manuscript: ${id.raw}"))

    val webForm = Body.webForm(FormValidator.Strict, titleParam, actionParam, formSelector).toLens()(request)
    val title = titleParam(webForm)
    val action = actionParam(webForm)

    if (action != "revert") {
        val newManuscript = manuscript.copy(title = MarkUp(title))

        println("newManuscript = ${newManuscript}")
        dataContext.update(db.manuscript).set(db.title, newManuscript.title.raw).where(db.manuscriptId.eq(id.raw)).execute()

        println("dir " + actionParam(webForm))
        println("form " + formSelector(webForm))
    }

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/title")
}

