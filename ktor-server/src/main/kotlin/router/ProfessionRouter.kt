package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.burgas.dto.ProfessionRequest
import org.burgas.service.ProfessionService
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configureProfessionRouter() {

    val professionService by inject<ProfessionService>()

    routing {

        route("/api/v1/professions") {

            get {
                call.respond(HttpStatusCode.OK, professionService.findAll())
            }

            get("/by-id") {
                val professionId = UUID.fromString(call.parameters["professionId"])
                call.respond(HttpStatusCode.OK, professionService.findById(professionId))
            }

            authenticate("jwt-auth-admin") {

                post("/create") {
                    val professionRequest = call.receive<ProfessionRequest>()
                    val professionResponse = professionService.create(professionRequest)
                    call.respond(HttpStatusCode.OK, professionResponse)
                }

                post("/create-by-document") {
                    val multiPartData = call.receiveMultipart(Long.MAX_VALUE)
                    professionService.createByDocument(multiPartData)
                    call.respondRedirect("/api/v1/professions")
                }

                put("/update") {
                    val professionRequest = call.receive<ProfessionRequest>()
                    val professionResponse = professionService.update(professionRequest)
                    call.respond(HttpStatusCode.OK, professionResponse)
                }

                delete("/delete") {
                    val professionId = UUID.fromString(call.parameters["professionId"])
                    professionService.delete(professionId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}