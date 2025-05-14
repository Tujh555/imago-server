package io.tujh.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.tujh.configurator.Configurator

class JwtConfigurator(private val application: Application) : Configurator {
    override fun configure() {
        application.authentication {
            jwt {
                realm = jwtRealm
                verifier(
                    JWT
                        .require(Algorithm.HMAC256(jwtSecret))
                        .withAudience(jwtAudience)
                        .withIssuer(jwtDomain)
                        .build()
                )
                validate { credential ->
                    if (credential.payload.audience.contains(jwtAudience)) JWTPrincipal(credential.payload) else null
                }
            }
        }
    }

    companion object {
        private const val jwtAudience = "jwt-audience"
        private const val jwtDomain = "https://jwt-provider-domain/"
        private const val jwtRealm = "ktor sample app"
        private const val jwtSecret = "secret"
    }
}