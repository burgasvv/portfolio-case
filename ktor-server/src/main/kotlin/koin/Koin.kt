package org.burgas.koin

import io.ktor.server.application.*
import org.burgas.service.DocumentService
import org.burgas.service.IdentityService
import org.burgas.service.ImageService
import org.burgas.service.ProfessionService
import org.burgas.service.VideoService
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import org.koin.ktor.plugin.Koin
import org.koin.logger.slf4jLogger

fun Application.configureKoin() {

    val module = module {
        singleOf(::ImageService)
        singleOf(::VideoService)
        singleOf(::DocumentService)
        single { IdentityService(imageService = get<ImageService>()) }
        singleOf(::ProfessionService)
    }

    install(Koin) {
        slf4jLogger()
        modules(module)
    }
}