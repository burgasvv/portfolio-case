package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.burgas.dao.IdentityEntity
import org.burgas.dao.PortfolioEntity
import org.burgas.database.DatabaseConnection
import org.burgas.dto.PortfolioRequest
import org.burgas.service.PortfolioService
import org.jetbrains.exposed.v1.dao.load
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.koin.ktor.ext.inject
import java.util.*

fun Application.configurePortfolioRouter() {

    val portfolioService by inject<PortfolioService>()

    val portfolioInterceptPlugin = createRouteScopedPlugin("PortfolioInterceptPlugin") {
        on(AuthenticationChecked) { call ->

            when(call.request.path()) {

                "/api/v1/portfolios/create" -> {
                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept portfolio on create"
                    }
                    val portfolioRequest = call.receive<PortfolioRequest>()

                    val identityEntity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        IdentityEntity[portfolioRequest.identityId!!]
                    }
                    require(identityPrincipal.id == identityEntity.id) {
                        "Not authorized intercept portfolio on create"
                    }
                }

                "/api/v1/portfolios/update" -> {
                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept portfolio on update"
                    }
                    val portfolioRequest = call.receive<PortfolioRequest>()

                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val portfolioEntity = PortfolioEntity[portfolioRequest.id!!].load(PortfolioEntity::identity)

                        require(identityPrincipal.id == portfolioEntity.identity.id) {
                            "Not authorized intercept portfolio on update"
                        }
                    }
                }

                "/api/v1/portfolios/delete" -> {
                    val identityPrincipal = requireNotNull(call.principal<IdentityEntity>()) {
                        "Not authenticated intercept portfolio on delete"
                    }
                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val portfolioEntity = PortfolioEntity[UUID.fromString(call.parameters["portfolioId"])]
                            .load(PortfolioEntity::identity)

                        require(identityPrincipal.id == portfolioEntity.identity.id) {
                            "Not authorized intercept portfolio on delete"
                        }
                    }
                }
            }
        }
    }

    routing {

        install(portfolioInterceptPlugin)

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