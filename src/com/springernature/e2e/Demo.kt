package com.springernature.e2e

import com.springernature.kachtml.*
import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.filter.DebuggingFilters
import org.http4k.routing.by
import org.http4k.routing.path
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.startServer
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager
import java.time.ZonedDateTime
import java.util.*


fun main(args: Array<String>) {
    java.lang.Class.forName("org.h2.Driver");
    val conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "")
    db.createDbTables(conn)

    val corsHeaders = listOf(
        "access-control-allow-origin" to "*",
        "access-control-allow-headers" to "content-type",
        "access-control-allow-methods" to "POST, GET, OPTIONS, PUT, PATCH,  DELETE")

    fun cors(handler: HttpHandler): HttpHandler = {
        val response = handler(it)
        response.copy(headers = corsHeaders + response.headers)
    }


    val server = DebuggingFilters.PrintRequestAndResponse(System.out)
        .then(
            dbSession(conn, { dataContext ->
                cors(routes(
                    GET to "/article/{id:.+}/title" by titleForm(dataContext),
                    GET to "/article/{id:.+}" by redirectToTitle(),
                    GET to "/article" by createArticleForm(dataContext),
                    POST to "/article" by createArticle(dataContext)
                ))
            })
        )
        .startServer(Jetty(9000))

    Runtime.getRuntime().addShutdownHook(Thread(server::stop))
    Runtime.getRuntime().addShutdownHook(Thread(conn::close))

}


interface HasExternalForm<T> {
    val raw: T
}

data class MarkUp(override val raw: String) : HasExternalForm<String>
data class ManuscriptId(override val raw: UUID) : HasExternalForm<UUID>

data class Manuscript(val id: ManuscriptId, val title: MarkUp)

val originalContent = """
<p data-index="0">Better Form Design: One Thing Per Page (Case Study)</p>
<p data-index="1">By Adam Silver, www.smashingmagazine.comView OriginalMay 22nd, 2017</p>
<p data-index="2">May 22nd, 2017</p>
<p data-index="3">In 2008, I worked on Boots.com. They wanted a single-page checkout with the trendiest of techniques from that era, including accordions, AJAX and client-side validation.</p>

<p data-index="4" data-already-used>Each step (delivery address, delivery options and credit-card details) had an accordion panel. Each panel was submitted via AJAX. Upon successful submission, the panel collapsed and the next one opened, with a sliding transition.</p>

<p data-index="5">It looked a little like this:</p>

<img data-index="6" src="file:///Users/vile01/work/specops/static editor/uploaded-image.png"/>
<p data-index="7">Boots' single-page checkout, using an accordion panel for each step. (View large version2)
<p data-index="8">Users struggled to complete their orders. Errors were hard to fix because users had to scroll up and down. And the accordion panels were painful and distracting. Inevitably, the client asked us to make changes.</p>

<p data-index="9">We redesigned it so that each panel became its own page, removing the need for accordions and AJAX. However, we kept the client-side validation to avoid an unnecessary trip to the server.</p>
"""


fun titleForm(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))

    val record = dataContext.select(db.title).from(db.manuscript).where(db.manuscriptId.eq(id.raw)).fetchOne()
    val manuscript = Manuscript(id, MarkUp(record.value1() ?: "Manuscript: ${id.raw}"))

    htmlPage(manuscript.title, div(cl("row"),
        div(cl("col-lg-4"),
            div(cl("full-screen-height"), originalContent)
        ),
        div(cl("col-lg-4"),
            p("form")
            // form
        ),
        div(cl("col-lg-4"),
            p("typeset")
        )
    ))
}

fun redirectToTitle(): HttpHandler = { request ->
    val id = ManuscriptId(UUID.fromString(request.path("id")!!))
    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}/title")

}

fun createArticle(dataContext: DSLContext): HttpHandler = { request ->
    val id = ManuscriptId(UUID.randomUUID())

    dataContext.insertInto(db.manuscript, db.manuscriptId).values(id.raw).execute()

    Response(Status.SEE_OTHER)
        .header("Location", "/article/${id.raw}")
}

fun createArticleForm(dataContext: DSLContext): HttpHandler {
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
"""

private fun htmlPage(title: MarkUp, content: KTag): Response {
    return Response(Status.OK).header("Content-Type", ContentType.TEXT_HTML.value).body(
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


fun dbSession(conn: Connection, fn: (DSLContext) -> HttpHandler): HttpHandler {
    val dataContext = DSL.using(conn, SQLDialect.H2)

    return fn(dataContext)
}