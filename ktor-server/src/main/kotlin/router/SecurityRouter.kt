package org.burgas.router

import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCallPipeline
import io.ktor.server.application.call
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.request.httpMethod
import io.ktor.utils.io.InternalAPI
import java.util.UUID

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
}