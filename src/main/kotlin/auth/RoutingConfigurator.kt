package io.tujh.auth

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.tujh.configurator.Configurator

class RoutingConfigurator(private val application: Application) : Configurator {
    override fun configure() {
        application.routing {

        }
    }
}