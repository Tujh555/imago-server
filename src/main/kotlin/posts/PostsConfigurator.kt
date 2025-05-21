package io.tujh.posts

import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.routing.*
import io.tujh.configurator.Configurator
import io.tujh.profile.ProfileService

class PostsConfigurator(private val application: Application) : Configurator {
    private val service = PostService()

    override fun configure() {
        application.routing {
            authenticate {
                route("posts") {
                    get("/my") {

                    }
                }
            }
        }
    }
}