package org.burgas.router

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.config.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.sessions.*
import io.ktor.utils.io.*
import org.burgas.dao.IdentityEntity
import org.burgas.database.DatabaseConnection
import org.burgas.database.IdentityTable
import org.burgas.dto.AuthRequest
import org.burgas.dto.AuthToken
import org.burgas.security.JwtConfig
import org.jetbrains.exposed.v1.core.eq
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import org.mindrot.jbcrypt.BCrypt
import java.util.*

@OptIn(InternalAPI::class)
fun Application.configureSecurityRouter() {

    val config = ApplicationConfig("application.yaml")
    val methods: List<HttpMethod> = listOf(
        HttpMethod.Post, HttpMethod.Delete, HttpMethod.Patch, HttpMethod.Put
    )

    intercept(ApplicationCallPipeline.Setup) {
        if (methods.contains(call.request.httpMethod)) {
            call.request.setHeader(
                HttpHeaders.Origin,
                listOf(config.property("api.ktor-server.url").getString())
            )
            call.request.setHeader(
                "X-CSRF-Token", listOf(UUID.randomUUID().toString())
            )
        } else {
            proceed()
        }
    }

    routing {

        route("/api/v1/security") {

            post("/login") {
                val authRequest = call.receive<AuthRequest>()
                val identity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.find { IdentityTable.email eq authRequest.email }.singleOrNull()
                }
                if (
                    identity != null && identity.status &&
                    BCrypt.checkpw(authRequest.password, identity.password)
                ) {
                    val generateToken = JwtConfig.generateToken(identity.id.value, identity.authority)
                    call.sessions.set(AuthToken(generateToken), AuthToken::class)
                    call.respond(
                        HttpStatusCode.OK,
                        "You successfully logged in: ${identity.email} :: ${identity.authority}"
                    )
                } else {
                    throw IllegalArgumentException("Identity not found for login")
                }
            }

            authenticate("jwt-auth") {

                post("/logout") {
                    call.sessions.clear(AuthToken::class)
                    call.respond(HttpStatusCode.OK, "You successfully logged out")
                }

                get("/get-data") {
                    call.respond(HttpStatusCode.OK, "Get data from security endpoint")
                }
            }
        }
    }
}