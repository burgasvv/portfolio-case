package org.burgas.security

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.plugins.cors.routing.*
import io.ktor.server.plugins.csrf.CSRF
import io.ktor.server.plugins.doublereceive.*
import io.ktor.server.plugins.statuspages.*
import io.ktor.server.response.*
import org.burgas.dto.ExceptionResponse

fun Application.configureSecurity() {

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