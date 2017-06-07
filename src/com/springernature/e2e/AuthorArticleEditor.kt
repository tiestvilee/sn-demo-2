package com.springernature.e2e

import com.springernature.kachtml.*
import org.http4k.core.ContentType
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import org.jooq.DSLContext
import java.time.ZonedDateTime
import java.util.*

val originalContent = """
<p data-index="0">Better Form Design: One Thing Per Page (Case Study)</p>
<p data-index="1">By Adam Silver, www.smashingmagazine.comView OriginalMay 22nd, 2017</p>
<p data-index="2">May 22nd, 2017</p>
<p data-index="3">In 2008, I worked on Boots.com. They wanted a single-page checkout with the trendiest of techniques from that era, including accordions, AJAX and client-side validation.</p>

<p data-index="4" data-already-used>Each step (delivery address, delivery options and credit-card details) had an accordion panel. Each panel was submitted via AJAX. Upon successful submission, the panel collapsed and the next one opened, with a sliding transition.</p>

<p data-index="5">It looked a little like this:</p>

<img data-index="6" src="/static/images/uploaded-image.png"/>
<p data-index="7">Boots' single-page checkout, using an accordion panel for each step. (View large version2)
<p data-index="8">Users struggled to complete their orders. Errors were hard to fix because users had to scroll up and down. And the accordion panels were painful and distracting. Inevitably, the client asked us to make changes.</p>

<p data-index="9">We redesigned it so that each panel became its own page, removing the need for accordions and AJAX. However, we kept the client-side validation to avoid an unnecessary trip to the server.</p>
"""

fun updateTitleForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val record = dataContext.select(db.title).from(db.manuscript).where(db.manuscriptId.eq(id.raw)).fetchOne()
    val manuscript = Manuscript(id, MarkUp(record.value1() ?: "Manuscript: ${id.raw}"))

    htmlPage(manuscript.title, div(cl("row"),
        div(cl("col-lg-4"),
            div(cl("full-screen-height"), originalContent)
        ),
        div(cl("col-lg-4"),
            form("method" attr "POST",
                div(cl("row"),
                    select(cl("form-selector"),
                        "name" attr "formSelector",
                        option("value" attr "title", manuscript.titleState.asIcon + " Title"),
                        option("value" attr "abstract", FragmentState.invalid.asIcon + " Abstract"))),
                div(cl("row"),
                    input("style" attr "width:100%",
                        "name" attr "title",
                        "type" attr "text",
                        "value" attr manuscript.title.raw)),
                div(cl("row"),
                    div(cl("col-lg-3"),
                        button("name" attr "action", "value" attr "previous", "Previous")
                    ),
                    div(cl("col-lg-3"),
                        button("name" attr "action", "value" attr "revert", "Revert")
                    ),
                    div(cl("col-lg-3 input-group"),
                        input(id("approved"), "type" attr "checkbox", "name" attr "approved", "checked" attr "checked", "tabindex" attr "0"),
                        label("for" attr "approved", "approved")
                    ),
                    div(cl("col-lg-3"),
                        button("name" attr "action", "value" attr "next", "Next")
                    )
                )
            )
        ),
        div(cl("col-lg-4"),
            p("typeset")
        )
    ))
}

enum class FragmentState(val asIcon: String) {
    invalid("❌"), valid("🔀"), accepted("✅");
}

private val Manuscript.titleState: FragmentState
    get() = if (this.title.raw.isNotEmpty()) FragmentState.accepted else FragmentState.invalid

fun redirectToTitle(): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))
    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/title")

}

fun createArticleForm(): HttpHandler {
    return { request ->
        htmlPage(MarkUp("submission"),
            div(cl("row"),
                div(cl("col-md-4")),
                div(cl("col-md-4"),
                    div(cl("fluid card"),
                        div(cl("section"), h3("Welcome to Nature Immunology")),
                        form("method" attr "POST",
                            fieldset(
                                legend("Create a new article"),
                                formRow(label("for" attr "articleType", "Article Type"),
                                    select("id" attr "articleType",
                                        option("review"),
                                        option("obituary")
                                    )),
                                formRow(label("for" attr "uploadManuscript", "Upload Manuscript"),
                                    input("type" attr "file", "id" attr "uploadManuscript"),
                                    label("for" attr "uploadManuscript", cl("button"), "Upload")
                                ),
                                formRow(span(), button("Create", "type" attr "submit"))
                            )
                        )
                    )))
        )
    }
}

private fun formRow(label: KTag, vararg input: KTag): Div {
    return div(cl("row responsive-label"),
        div(cl("col-md-5"), label),
        div(cl("col-md"), *input))
}

val styles = """
.full-screen-height {
    height:calc(100vh - 140px);
    overflow-y:scroll;
}
.form-selector {
    width: 100%;
    font-size: 24pt;
}
"""

private fun htmlPage(title: MarkUp, content: KTag): Response {
    return Response(Status.OK).header("Content-Type", "${ContentType.TEXT_HTML.value}; charset=utf-8").body(
        page(title, content).toCompactHtml()
    )
}

private fun page(title: MarkUp, content: KTag): KTag {
    return doctype(attr("html"),
        html(
            head(
                title(title.raw),
                link("rel" attr "stylesheet", "href" attr "https://gitcdn.link/repo/Chalarangelo/mini.css/master/dist/mini-default.min.css"),
                style(styles)
            ),
            body(
                header(cl("sticky row"),
                    div(cl("col-sm col-md-10, col-md-offset-1"),
                        a("href" attr "/editor/manuscript", "role" attr "button", "Manuscripts"))),
                div(cl("container"), content),
                footer(
                    div(cl("col-sm col-md-10 col-md-offset-1"),
                        p("Copyright &copy; SpringerNature ${ZonedDateTime.now().year}"))
                )
            )))
}