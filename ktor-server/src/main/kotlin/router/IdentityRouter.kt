package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.IdentityRequest
import org.burgas.service.IdentityService
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureIdentityRouter() {

    val identityService by inject<IdentityService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/identities/change-status") {

            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept identity principal by change status"))

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(UUID.fromString(call.parameters["identityId"]))
                    ?: throw IllegalArgumentException("Not found identity intercept by change status")
            }
            if (identityPrincipal.id.value != identityEntity.id.value) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized intercept identity by change status: Matched identities")
            }

        } else if (call.request.path() == "/api/v1/identities/by-id" || call.request.path() == "/api/v1/identities/delete") {

            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept identity principal by id parameter"))

            when(identityPrincipal.authority) {
                Authority.ADMIN -> proceed()
                Authority.USER -> {
                    val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        IdentityEntity.findById(UUID.fromString(call.parameters["identityId"]))
                            ?: throw IllegalArgumentException("Not found identity intercept by id parameter")
                    }
                    if (identityPrincipal.id.value == identityEntity.id.value) {
                        proceed()
                    } else {
                        throw IllegalArgumentException("Not authorized intercept identity by id parameter")
                    }
                }
            }

        } else if (
            call.request.path() == "/api/v1/identities/upload-image" || call.request.path() == "/api/v1/identities/remove-image"
        ) {
            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept identity principal image"))

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(UUID.fromString(call.parameters["identityId"]))
                    ?: throw IllegalArgumentException("Not found identity intercept image")
            }
            if (identityPrincipal.id.value == identityEntity.id.value) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized intercept identity image")
            }

        } else if (call.request.path() == "/api/v1/identities/update" || call.request.path() == "/api/v1/identities/change-password") {

            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept identity principal by identityRequest"))
            val identityRequest = call.receive<IdentityRequest>()

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(identityRequest.id!!)
                    ?: throw IllegalArgumentException("Not found identity intercept by identityRequest")
            }
            if (identityPrincipal.id.value == identityEntity.id.value) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized intercept identity by identityRequest")
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/identities") {

            authenticate("jwt-auth-admin", optional = true) {

                post("/create") {
                    val identityRequest = call.receive<IdentityRequest>()
                    val identityResponse = identityService.create(identityRequest)
                    call.respond(HttpStatusCode.OK, identityResponse)
                }
            }

            authenticate("jwt-auth-admin") {

                post("/create-admin") {
                    val identityRequest = call.receive<IdentityRequest>()
                    val identityResponse = identityService.createAdmin(identityRequest)
                    call.respond(HttpStatusCode.OK, identityResponse)
                }

                get {
                    call.respond(HttpStatusCode.OK, identityService.findAll())
                }

                put("/change-status") {
                    val identityRequest = call.receive<IdentityRequest>()
                    identityService.changeStatus(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }

            authenticate("jwt-auth") {

                get("/by-id") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    call.respond(HttpStatusCode.OK, identityService.findById(identityId))
                }

                put("/update") {
                    val identityRequest = call.receive<IdentityRequest>()
                    val identityResponse = identityService.update(identityRequest)
                    call.respond(HttpStatusCode.OK, identityResponse)
                }

                post("/delete") {
                    val identityEntity = call.principal<IdentityEntity>()!!
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.delete(identityId)
                    if (identityEntity.id.value == identityId) {
                        call.respondRedirect("/api/v1/security/logout")
                    } else {
                        call.respond(HttpStatusCode.OK)
                    }
                }

                put("/change-password") {
                    val identityRequest = call.receive<IdentityRequest>()
                    identityService.changePassword(identityRequest)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-image") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.uploadImage(identityId, call.receiveMultipart(Long.MAX_VALUE))
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-image") {
                    val identityId = UUID.fromString(call.parameters["identityId"])
                    identityService.removeImage(identityId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}