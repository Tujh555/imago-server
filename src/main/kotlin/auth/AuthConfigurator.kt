package io.tujh.auth

import io.ktor.server.application.*
import io.tujh.auth.jwt.JwtConfigurator
import io.tujh.configurator.Configurator
import io.tujh.configurator.DelegateConfigurator

class AuthConfigurator(private val application: Application) : Configurator by DelegateConfigurator(
    application = application,
    delegates = listOf(::JwtConfigurator, ::RoutingConfigurator)
)