package io.tujh

import io.ktor.server.application.*
import io.tujh.configurator.DelegateConfigurator
import io.tujh.configurator.configurators

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DelegateConfigurator(this, configurators).configure()
}
