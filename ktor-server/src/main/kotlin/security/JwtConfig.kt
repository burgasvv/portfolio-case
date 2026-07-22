package org.burgas.security

import com.auth0.jwt.JWT
import com.auth0.jwt.JWTVerifier
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.config.*
import org.burgas.database.Authority
import java.util.*
import kotlin.time.Duration.Companion.days

object JwtConfig {

    private val config = ApplicationConfig("application.yaml")

    private val secret = config.property("jwt.secret").getString()
    private const val ISSUER = "test-issuer"
    private const val AUDIENCE = "test-audience"
    private val validity = 7.days

    private val algorithm = Algorithm.HMAC256(secret)

    val verifier: JWTVerifier = JWT.require(algorithm)
        .withAudience(AUDIENCE)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(identityId: UUID, authority: Authority): String {
        return JWT.create()
            .withSubject("Authentication")
            .withIssuer(ISSUER)
            .withAudience(AUDIENCE)
            .withClaim("identityId", identityId.toString())
            .withClaim("authority", authority.name)
            .withExpiresAt(Date(System.currentTimeMillis() + validity.inWholeMilliseconds))
            .sign(algorithm)
    }
}