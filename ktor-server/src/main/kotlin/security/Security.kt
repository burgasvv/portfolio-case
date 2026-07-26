package org.burgas.security

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import io.ktor.utils.io.core.toByteArray
import org.burgas.dao.IdentityEntity
import org.burgas.database.Authority
import org.burgas.database.DatabaseConnection
import org.burgas.dto.AuthToken
import org.burgas.dto.ExceptionResponse
import org.jetbrains.exposed.v1.jdbc.transactions.suspendTransaction
import java.util.*

fun Application.configureSecurity() {

    val config = ApplicationConfig("application.yaml")

    authentication {

        jwt("jwt-auth") {
            authHeader { call ->
                val authToken = call.sessions.get(AuthToken::class) ?: return@authHeader null
                parseAuthorizationHeader("Bearer ${authToken.token}")
            }
            verifier(JwtConfig.verifier)
            validate { credentials ->
                val identity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.findById(UUID.fromString(credentials["identityId"]))
                }
                if (identity != null && identity.status) {
                    identity
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "JWT Token unavailable: $realm :: $defaultScheme"
                )
            }
        }

        jwt("jwt-auth-admin") {
            authHeader { call ->
                val authToken = call.sessions.get(AuthToken::class) ?: return@authHeader null
                parseAuthorizationHeader("Bearer ${authToken.token}")
            }
            verifier(JwtConfig.verifier)
            validate { credentials ->
                val identity = suspendTransaction(db = DatabaseConnection.postgres, readOnly = true) {
                    IdentityEntity.findById(UUID.fromString(credentials["identityId"]))
                }
                if (identity != null && identity.status && identity.authority == Authority.ADMIN) {
                    identity
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    "JWT Token unavailable: $realm :: $defaultScheme"
                )
            }
        }
    }

    install(Sessions) {
        cookie<AuthToken>("AUTH_TOKEN") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = false
            cookie.extensions["SameSite"] = "lax"
            transform(SessionTransportTransformerMessageAuthentication(
                config.property("cookie.secret").getString().toByteArray()
            ))
        }
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            val exceptionResponse = ExceptionResponse(
                status = HttpStatusCode.BadRequest.description,
                code = HttpStatusCode.BadRequest.value,
                message = cause.message
            )
            call.respond(HttpStatusCode.BadRequest, exceptionResponse)
        }
    }

    install(DoubleReceive)

    install(CORS) {
        anyMethod()

        allowHeader(HttpHeaders.Host)
        allowHeader(HttpHeaders.Origin)
        allowHeader(HttpHeaders.Accept)
        allowHeader(HttpHeaders.ContentType)
        allowHeader(HttpHeaders.Authorization)
        allowHeader("X-CSRF-Token")

        allowCredentials = true
        allowSameOrigin = true

        allowHost("localhost:9000")
        allowHost("localhost:4200")
    }

    install(CSRF) {
        allowOrigin("http://localhost:9000")
        allowOrigin("http://localhost:4200")

        checkHeader("X-CSRF-Token")

        onFailure { errorReason ->
            respond(HttpStatusCode.BadRequest, errorReason)
        }
    }
}