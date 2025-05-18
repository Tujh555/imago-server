package io.tujh.auth

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.tujh.auth.requests.AuthRequest
import io.tujh.common.respondRes
import io.tujh.configurator.Configurator

class RoutingConfigurator(private val application: Application) : Configurator {
    private val service = AuthService()

    override fun configure() {
        application.routing {
            route("auth") {
                post("/login") {
                    val request = call.receive<AuthRequest>()
                    val response = service.login(request)
                    call.respondRes(response)
                }

                post("/register") {
                    val request = call.receive<AuthRequest>()
                    val response = service.register(request)
                    call.respondRes(response)
                }
            }
        }
    }
}