package com.springernature.e2e

import org.http4k.core.*
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.filter.DebuggingFilters
import org.http4k.routing.by
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.startServer
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import java.sql.Connection
import java.sql.DriverManager


fun main(args: Array<String>) {
    java.lang.Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection("jdbc:h2:~/test", "sa", "")
    db.createDbTables(conn)

    val corsHeaders = listOf(
        "access-control-allow-origin" to "*",
        "access-control-allow-headers" to "content-type",
        "access-control-allow-methods" to "POST, GET, OPTIONS, PUT, PATCH,  DELETE")

    fun cors(handler: HttpHandler): HttpHandler = {
        val response = handler(it)
        corsHeaders.forEach { header -> response.header(header.first, header.second) }
        response
    }


    val server = DebuggingFilters.PrintRequestAndResponse(System.out)
        .then(
            dbSession(conn, { dataContext ->
                cors(routes(
                    "/article/{id:.+}/title" to GET by updateTitleForm(dataContext),
                    "/article/{id:.+}/title" to POST by updateTitle(dataContext),
                    "/article/{id:.+}" to GET by redirectToTitle(),
                    "/article" to GET by createArticleForm(),
                    "/article" to POST by createArticle(dataContext),
                    "/static/{path:.*}" to GET by serveStaticData()
                ))
            })
        )
        .startServer(Jetty(9000))

    Runtime.getRuntime().addShutdownHook(Thread(server::stop))
    Runtime.getRuntime().addShutdownHook(Thread(conn::close))

}


fun dbSession(conn: Connection, fn: (DSLContext) -> HttpHandler): HttpHandler {
    return { request ->
        try {
            val response = fn(DSL.using(conn, SQLDialect.H2))(request)
            conn.commit()
            response
        } catch (e: Exception) {
            conn.rollback()
            throw e
        }
    }
}