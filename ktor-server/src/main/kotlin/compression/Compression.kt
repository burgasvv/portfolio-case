package org.burgas.compression

import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.plugins.compression.Compression
import io.ktor.server.plugins.compression.deflate
import io.ktor.server.plugins.compression.gzip
import io.ktor.server.plugins.compression.minimumSize

fun Application.configureCompression() {

    install(Compression) {
        gzip { minimumSize(1024) }
        deflate { priority = 0.9 }
    }
}