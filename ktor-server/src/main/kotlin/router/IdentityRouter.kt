package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.util.pipeline.PipelinePhase
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

    val afterPluginsPhase = PipelinePhase("IdentityInterceptAfterPluginsPhase")

    insertPhaseAfter(ApplicationCallPipeline.Plugins, afterPluginsPhase)

    intercept(afterPluginsPhase) {

        when(call.request.path()) {
            "/api/v1/identities/change-status" -> {
                val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                    "Not authenticated intercept identity principal by change status"
                }
                val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity[UUID.fromString(call.parameters["identityId"])]
                }
                require(identityPrincipal.id.value != identityEntity.id.value) {
                    "Not authorized intercept identity by change status: Matched identities"
                }
                proceed()
            }
            "/api/v1/identities/by-id", "/api/v1/identities/delete" -> {
                val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                    "Not authenticated intercept identity principal by id parameter"
                }
                when(identityPrincipal.authority) {
                    Authority.ADMIN -> proceed()
                    Authority.USER -> {
                        val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                            IdentityEntity[UUID.fromString(call.parameters["identityId"])]
                        }
                        require(identityPrincipal.id.value == identityEntity.id.value) {
                            "Not authorized intercept identity by id parameter"
                        }
                        proceed()
                    }
                }
            }
            "/api/v1/identities/upload-image", "/api/v1/identities/remove-image" -> {
                val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                    "Not authenticated intercept identity principal image"
                }
                val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity[UUID.fromString(call.parameters["identityId"])]
                }
                require(identityPrincipal.id.value == identityEntity.id.value) {
                    "Not authorized intercept identity by image"
                }
                proceed()
            }
            "/api/v1/identities/update", "/api/v1/identities/change-password" -> {
                val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                    "Not authenticated intercept identity principal by identityRequest"
                }
                val identityRequest = call.receive<IdentityRequest>()

                val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity[identityRequest.id!!]
                }
                require(identityPrincipal.id.value == identityEntity.id.value) {
                    "Not authorized intercept identity by identityRequest"
                }
                proceed()
            }
            else -> proceed()
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
                    val multiPartData = call.receiveMultipart(Long.MAX_VALUE)
                    identityService.uploadImage(identityId, multiPartData)
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