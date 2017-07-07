package com.springernature.e2e

import org.http4k.core.Body
import org.http4k.core.HttpHandler
import org.http4k.core.Response
import org.http4k.core.Status
import org.http4k.routing.path
import java.io.File
import java.nio.ByteBuffer

fun serveStaticData(): HttpHandler = { request ->
    val filePath = request.path("path")
    val theFile = File("static/$filePath")
    val contentType = when (theFile.extension) {
        "png" -> "image/png"
        "js" -> "application/javascript"
        else -> "*/*"
    }
    Response(Status.OK)
        .header("Age", "1")
        .header("Cache-Control", "public, max-age=86400")
        .header("ETag", filePath)
        .header("Content-Type", contentType)
        .body(Body(ByteBuffer.wrap(theFile.readBytes())))
}