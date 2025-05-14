package io.tujh.serialization

import io.ktor.serialization.gson.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import io.tujh.configurator.Configurator

class SerializationConfigurator(private val application: Application) : Configurator {
    override fun configure() {
        application.install(ContentNegotiation) { gson { } }
    }
}