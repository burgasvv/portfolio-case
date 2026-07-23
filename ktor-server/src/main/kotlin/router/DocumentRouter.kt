package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.service.DocumentService
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureDocumentRouter() {

    val documentService by inject<DocumentService>()

    routing {

        route("/api/v1/documents") {

            get("/by-id") {
                val documentId = UUID.fromString(call.parameters["documentId"])
                val documentEntity = documentService.readEntity(documentId)
                call.respondBytes(
                    contentType = ContentType.parse(documentEntity.contentType),
                    status = HttpStatusCode.OK
                ) { documentEntity.data.bytes }
            }
        }
    }
}