package io.tujh.auth.jwt

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.tujh.configurator.Configurator

class JwtConfigurator(private val application: Application) : Configurator {
    override fun configure() {
        application.authentication {
            jwt {
                realm = "imago-server-app"
                verifier(JwtInteractor.verifier)
                validate { credential -> JwtInteractor.getUser(credential.payload) }
            }
        }
    }
}