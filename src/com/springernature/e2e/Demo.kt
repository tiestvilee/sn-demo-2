package com.springernature.e2e

import com.springernature.e2e.ManuscriptTable.manuscriptLabel
import org.http4k.core.HttpHandler
import org.http4k.core.Method.GET
import org.http4k.core.Method.POST
import org.http4k.core.then
import org.http4k.filter.DebuggingFilters
import org.http4k.routing.bind
import org.http4k.routing.routes
import org.http4k.server.Jetty
import org.http4k.server.startServer
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.neo4j.graphdb.GraphDatabaseService
import org.neo4j.graphdb.factory.GraphDatabaseFactory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager


fun main(args: Array<String>) {
    java.lang.Class.forName("org.h2.Driver")
    val conn = DriverManager.getConnection("jdbc:h2:./out/test", "sa", "")
    Database.createDbTables(conn)

    val graphDb = GraphDatabaseFactory().newEmbeddedDatabase(File("./out/graph.db"))
    graphDb.beginTx().use { tx ->
        if (graphDb.schema().indexFor(manuscriptLabel) == null) {
            graphDb.schema().indexFor(manuscriptLabel)
                .on("id")
                .create()
        }
        tx.success()
    }
    registerShutdownHook(graphDb)

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
            dbSession(conn, graphDb, { dataContext, graphDbInTransaction ->
                cors(routes(
                    "/article/{id:.+}/log" to GET bind logFor(dataContext),
                    "/article/{id:.+}/asGraph" to GET bind graphFor(graphDbInTransaction),
                    "/article/{id:.+}/asXml" to GET bind asXml(dataContext),
                    "/article/{id:.+}/asPdf" to GET bind asPdf(dataContext),
                    "/article/{id:.+}/title" to GET bind updateTitleForm(dataContext),
                    "/article/{id:.+}/title" to POST bind updateTitle(dataContext, graphDbInTransaction),
                    "/article/{id:.+}/abstract" to GET bind updateAbstractForm(dataContext),
                    "/article/{id:.+}/abstract" to POST bind updateAbstract(dataContext, graphDbInTransaction),
                    //                    "/article/{id:.+}/author/selection" to POST bind selectAuthors(dataContext),
//                    "/article/{id:.+}/author/add" to POST bind selectAuthors(dataContext),
//                    "/article/{id:.+}/author/{author:.+}" to POST bind selectAuthors(dataContext),
                    "/article/{id:.+}/authors" to POST bind updateAuthors(dataContext, graphDbInTransaction),
                    "/article/{id:.+}/authors" to GET bind updateAuthorsForm(dataContext),
                    "/article/{id:.+}" to GET bind redirectToTitle(),
                    "/article" to GET bind createArticleForm(),
                    "/article" to POST bind createArticle(dataContext, graphDbInTransaction),
                    "/static/{path:.*}" to GET bind serveStaticData()
                ))
            })
        )
        .startServer(Jetty(9000))

    Runtime.getRuntime().addShutdownHook(Thread(server::stop))
    Runtime.getRuntime().addShutdownHook(Thread(conn::close))

}


fun dbSession(conn: Connection, graphDb: GraphDatabaseService, fn: (DSLContext, GraphDatabaseService) -> HttpHandler): HttpHandler {
    return { request ->
        graphDb.beginTx().use { tx ->
            try {
                val response = fn(DSL.using(conn, SQLDialect.H2), graphDb)(request)
                conn.commit()
                tx.success()
                response
            } catch (e: Exception) {
                conn.rollback()
                throw e
            }
        }
    }
}

private fun registerShutdownHook(graphDb: GraphDatabaseService) {
    // Registers a shutdown hook for the Neo4j instance so that it
    // shuts down nicely when the VM exits (even if you "Ctrl-C" the
    // running application).
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            graphDb.shutdown()
        }
    })
}