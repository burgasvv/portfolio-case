package org.burgas.security

import io.ktor.http.*
import io.ktor.http.auth.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.*
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import io.ktor.server.sessions.*
import org.burgas.dto.AuthToken
import org.burgas.dto.ExceptionResponse

fun Application.configureSecurity() {

    authentication {

        jwt("jwt-auth") {
            authHeader { call ->
                val authToken = call.sessions.get(AuthToken::class) ?: return@authHeader null
                parseAuthorizationHeader("Bearer ${authToken.token}")
            }
            verifier(JwtConfig.verifier)
            validate { credentials ->
                if (!credentials["identityId"].isNullOrEmpty() && !credentials["authority"].isNullOrEmpty()) {
                    JWTPrincipal(credentials.payload)
                } else {
                    null
                }
            }
            challenge { defaultScheme, realm ->
                call.respond(HttpStatusCode.Unauthorized,
                    "JWT Token unavailable: $realm :: $defaultScheme")
            }
        }
    }

    install(Sessions) {
        cookie<AuthToken>("AUTH_TOKEN") {
            cookie.path = "/"
            cookie.httpOnly = true
            cookie.secure = false
            cookie.extensions["SameSite"] = "lax"
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