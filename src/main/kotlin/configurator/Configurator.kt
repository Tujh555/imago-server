package io.tujh.configurator

import io.ktor.server.application.*

typealias ConfiguratorFactory = (Application) -> Configurator

fun interface Configurator {
    fun configure()
}

@Suppress("FunctionName")
fun DelegateConfigurator(application: Application, delegates: Collection<ConfiguratorFactory>) = Configurator {
    delegates.map { it(application) }.forEach(Configurator::configure)
}