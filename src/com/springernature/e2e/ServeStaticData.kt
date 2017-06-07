package com.springernature.e2e

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.io.File
import java.nio.ByteBuffer

fun serveStaticData(): HttpHandler = { request ->
    Response(Status.OK)
        .body(Body(ByteBuffer.wrap(File("static/${request.path("path")}").readBytes())))
}