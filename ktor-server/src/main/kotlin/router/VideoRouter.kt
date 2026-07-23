package org.burgas.router

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.response.respondBytes
import io.ktor.server.routing.*
import org.burgas.service.VideoService
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureVideoRouter() {

    val videoService by inject<VideoService>()

    routing {

        route("/api/v1/videos") {

            get("/by-id") {
                val videoId = UUID.fromString(call.parameters["videoId"])
                val videoEntity = videoService.readEntity(videoId)
                call.respondBytes(
                    contentType = ContentType.parse(videoEntity.contentType),
                    status = HttpStatusCode.OK
                ) { videoEntity.data.bytes }
            }
        }
    }
}