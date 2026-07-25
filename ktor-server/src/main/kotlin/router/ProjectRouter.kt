package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.dao.IdentityEntity
import org.burgas.dao.PortfolioEntity
import org.burgas.dao.ProjectEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.ProjectFileRequest
import org.burgas.dto.ProjectRequest
import org.burgas.service.ProjectService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configureProjectRouter() {

    val projectService by inject<ProjectService>()

    val projectInterceptPlugin = createRouteScopedPlugin("ProjectInterceptPlugin") {
        on(AuthenticationChecked) { call ->

            when (call.request.path()) {

                "/api/v1/projects/create" -> {
                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept project by create"
                    }
                    val projectRequest = call.receive<ProjectRequest>()

                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val portfolioEntity = PortfolioEntity[projectRequest.portfolioId!!]
                            .load(PortfolioEntity::identity)

                        require(identityPrincipal.id == portfolioEntity.identity.id) {
                            "Not authorized intercept project by create"
                        }
                    }
                }

                "/api/v1/projects/update" -> {
                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept project by update"
                    }
                    val projectRequest = call.receive<ProjectRequest>()

                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val projectEntity = ProjectEntity[projectRequest.id!!].load(ProjectEntity::portfolio)

                        require(identityPrincipal.id == projectEntity.portfolio.identity.id) {
                            "Not authorized intercept project by update"
                        }
                    }
                }

                "/api/v1/projects/delete", "/api/v1/projects/upload-image", "/api/v1/projects/remove-image",
                "/api/v1/projects/upload-videos", "/api/v1/projects/upload-documents" -> {

                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept project by id parameter"
                    }
                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val projectEntity = ProjectEntity[UUID.fromString(call.parameters["projectId"])]
                            .load(ProjectEntity::portfolio)

                        require(identityPrincipal.id == projectEntity.portfolio.identity.id) {
                            "Not authorized intercept project by id parameter"
                        }
                    }
                }

                "/api/v1/projects/remove-videos", "/api/v1/projects/remove-documents" -> {
                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept project by projectFileRequest"
                    }
                    val projectFileRequest = call.receive<ProjectFileRequest>()

                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val projectEntity = ProjectEntity[projectFileRequest.projectId].load(ProjectEntity::portfolio)

                        require(identityPrincipal.id == projectEntity.portfolio.identity.id) {
                            "Not authorized intercept project by projectFileRequest"
                        }
                    }
                }
            }
        }
    }

    routing {

        install(projectInterceptPlugin)

        route("/api/v1/projects") {

            get("/by-id") {
                val projectId = UUID.fromString(call.parameters["projectId"])
                call.respond(HttpStatusCode.OK, projectService.findById(projectId))
            }

            authenticate("jwt-auth") {

                post("/create") {
                    val projectRequest = call.receive<ProjectRequest>()
                    val projectResponse = projectService.create(projectRequest)
                    call.respond(HttpStatusCode.OK, projectResponse)
                }

                put("/update") {
                    val projectRequest = call.receive<ProjectRequest>()
                    val projectResponse = projectService.update(projectRequest)
                    call.respond(HttpStatusCode.OK, projectResponse)
                }

                delete("/delete") {
                    val projectId = UUID.fromString(call.parameters["projectId"])
                    projectService.delete(projectId)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-image") {
                    val projectId = UUID.fromString(call.parameters["projectId"])
                    val multiPartData = call.receiveMultipart(Long.MAX_VALUE)
                    projectService.uploadImage(projectId, multiPartData)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-image") {
                    val projectId = UUID.fromString(call.parameters["projectId"])
                    projectService.removeImage(projectId)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-videos") {
                    val projectId = UUID.fromString(call.parameters["projectId"])
                    val multiPartData = call.receiveMultipart(Long.MAX_VALUE)
                    projectService.uploadVideos(projectId, multiPartData)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-videos") {
                    val projectFileRequest = call.receive<ProjectFileRequest>()
                    projectService.removeVideos(projectFileRequest)
                    call.respond(HttpStatusCode.OK)
                }

                post("/upload-documents") {
                    val projectId = UUID.fromString(call.parameters["projectId"])
                    val multiPartData = call.receiveMultipart(Long.MAX_VALUE)
                    projectService.uploadDocuments(projectId, multiPartData)
                    call.respond(HttpStatusCode.OK)
                }

                delete("/remove-documents") {
                    val projectFileRequest = call.receive<ProjectFileRequest>()
                    projectService.removeDocuments(projectFileRequest)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}