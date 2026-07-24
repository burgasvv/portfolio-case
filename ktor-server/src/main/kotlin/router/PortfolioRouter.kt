package org.burgas.router

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import org.burgas.dao.IdentityEntity
import org.burgas.dao.PortfolioEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.PortfolioRequest
import org.burgas.service.PortfolioService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.UUID

fun Application.configurePortfolioRouter() {

    val portfolioService by inject<PortfolioService>()

    intercept(ApplicationCallPipeline.Call) {

        if (call.request.path() == "/api/v1/portfolios/create") {

            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept portfolio on create"))
            val portfolioRequest = call.receive<PortfolioRequest>()

            val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                IdentityEntity.findById(portfolioRequest.identityId!!)
                    ?: throw IllegalArgumentException("Not found identity intercept on create")
            }
            if (identityPrincipal.id.value == identityEntity.id.value) {
                proceed()
            } else {
                throw IllegalArgumentException("Not authorized intercept portfolio on create")
            }

        } else if (call.request.path() == "/api/v1/portfolios/update") {

            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept portfolio on update"))
            val portfolioRequest = call.receive<PortfolioRequest>()

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val portfolioEntity = (PortfolioEntity.findById(portfolioRequest.id!!)
                    ?: throw IllegalArgumentException("Not found portfolio intercept on update"))
                    .load(PortfolioEntity::identity)

                if (identityPrincipal.id.value == portfolioEntity.identity.id.value) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept portfolio on update")
                }
            }

        } else if (call.request.path() == "/api/v1/portfolios/delete") {

            val identityPrincipal = (call.principal<IdentityEntity>()
                ?: throw IllegalArgumentException("Not authenticated intercept portfolio on delete"))

            suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                val portfolioEntity = (PortfolioEntity.findById(UUID.fromString(call.parameters["portfolioId"]))
                    ?: throw IllegalArgumentException("Not found portfolio intercept on update"))
                    .load(PortfolioEntity::identity)

                if (identityPrincipal.id.value == portfolioEntity.identity.id.value) {
                    proceed()
                } else {
                    throw IllegalArgumentException("Not authorized intercept portfolio on delete")
                }
            }

        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/portfolios") {

            get {
                call.respond(HttpStatusCode.OK, portfolioService.findAll())
            }

            get("/by-id") {
                val portfolioId = UUID.fromString(call.parameters["portfolioId"])
                call.respond(HttpStatusCode.OK, portfolioService.findById(portfolioId))
            }

            authenticate("jwt-auth") {

                post("/create") {
                    val portfolioRequest = call.receive<PortfolioRequest>()
                    val portfolioResponse = portfolioService.create(portfolioRequest)
                    call.respond(HttpStatusCode.OK, portfolioResponse)
                }

                put("/update") {
                    val portfolioRequest = call.receive<PortfolioRequest>()
                    val portfolioResponse = portfolioService.update(portfolioRequest)
                    call.respond(HttpStatusCode.OK, portfolioResponse)
                }

                delete("/delete") {
                    val portfolioId = UUID.fromString(call.parameters["portfolioId"])
                    portfolioService.delete(portfolioId)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }
    }
}