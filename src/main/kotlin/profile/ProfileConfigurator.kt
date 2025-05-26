package io.tujh.profile

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.tujh.common.respondRes
import io.tujh.common.withUser
import io.tujh.configurator.Configurator

class ProfileConfigurator(private val application: Application) : Configurator {
    private val service = ProfileService()

    override fun configure() {
        application.routing {
            staticFiles("/${service.folder.name}", service.folder)

            authenticate {
                route("user") {
                    post("/avatar") {
                        println("--> user/avatar")
                        call.withUser { user ->
                            println("--> user = $user")
                            receiveMultipart().forEachPart { part ->
                                println("part = $part\n${part.name}\n${part.contentType}\nheaders = ${part.headers}")
                                if (part is PartData.FileItem) {
                                    val response = service.loadAvatar(user, part.provider())
                                    respondRes(response)
                                }

                                part.dispose()
                            }
                        }
                    }

                    patch("/name") {
                        call.withUser { user ->
                            val name = receive<String>()
                            service.updateName(user, name)
                            respond(HttpStatusCode.OK)
                        }
                    }
                }
            }
        }
    }
}