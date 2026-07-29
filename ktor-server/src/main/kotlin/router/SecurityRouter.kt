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

//    intercept(ApplicationCallPipeline.Setup) {
//
//        when(call.request.httpMethod) {
//            HttpMethod.Post, HttpMethod.Delete, HttpMethod.Patch, HttpMethod.Put -> {
//                call.request.setHeader(
//                    HttpHeaders.Origin,
//                    listOf(config.property("api.angular-client.url").getString())
//                )
//                call.request.setHeader(
//                    "X-CSRF-Token", listOf(UUID.randomUUID().toString())
//                )
//                proceed()
//            }
//            else -> proceed()
//        }
//    }

    routing {

        route("/api/v1/security") {

            post("/login") {
                val authToken = call.sessions.get(AuthToken::class)
                if (authToken != null) {
                    call.respond(HttpStatusCode.OK, "You already logged in")
                } else {
                    val authRequest = call.receive<AuthRequest>()
                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        val identity = IdentityEntity.find { IdentityTable.email eq authRequest.email }.singleOrNull()
                        if (
                            identity != null && identity.status &&
                            BCrypt.checkpw(authRequest.password, identity.password)
                        ) {
                            val generateToken = JwtConfig.generateToken(identity.id.value)
                            call.sessions.set(AuthToken(generateToken), AuthToken::class)
                            call.respond(HttpStatusCode.OK, identity.toResponse())
                        } else {
                            throw IllegalArgumentException("Identity not found for login")
                        }
                    }
                }
            }

            authenticate("jwt-auth") {

                get("/authenticated") {
                    val identityEntity = requireNotNull(call.principal<IdentityEntity>()) {
                        "Identity principal is null"
                    }
                    suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                        call.respond(HttpStatusCode.OK, identityEntity.toResponse())
                    }
                }

                post("/logout") {
                    call.sessions.clear(AuthToken::class)
                    call.respond(HttpStatusCode.OK, "You successfully logged out")
                }
            }
        }
    }
}