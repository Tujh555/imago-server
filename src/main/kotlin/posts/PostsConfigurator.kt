package io.tujh.posts

import io.ktor.http.*
import io.ktor.http.content.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.http.content.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.ktor.server.util.*
import io.ktor.util.*
import io.ktor.utils.io.*
import io.tujh.common.respondRes
import io.tujh.common.withUser
import io.tujh.configurator.Configurator
import java.time.Instant

class PostsConfigurator(private val application: Application) : Configurator {
    private val service = PostService()

    override fun configure() {
        application.routing {
            staticFiles("/${service.folder.name}", service.folder)

            authenticate {
                route("posts") {
                    get("/my") {
                        call.withUser { user ->
                            try {
                                val limit = call.queryParameters.getOrFail("limit").toInt()
                                val cursor = Instant.parse(call.queryParameters.getOrFail("cursor"))
                                call.respondRes(service.resolveFor(user, limit, cursor))
                            } catch (e: Exception) {
                                call.respond(HttpStatusCode.BadRequest)
                            }
                        }
                    }

                    get("/all") {
                        try {
                            val limit = call.queryParameters.getOrFail("limit").toInt()
                            val cursor = Instant.parse(call.queryParameters.getOrFail("cursor"))
                            call.respondRes(service.resolveAll(limit, cursor))
                        } catch (e: Exception) {
                            call.respond(HttpStatusCode.BadRequest)
                        }
                    }

                    route("/favorites") {
                        post("/add") {
                            call.withUser { user ->
                                val postId = receive<RequestId>().id
                                respond(service.addToFavorite(user, postId))
                            }
                        }

                        get("/check") {
                            call.withUser { user ->
                                try {
                                    val postId = queryParameters.getOrFail("id")
                                    call.respond(service.checkInFavorites(user, postId))
                                } catch (e: Exception) {
                                    call.respond(HttpStatusCode.BadRequest)
                                }
                            }
                        }

                        get {
                            call.withUser { user ->
                                try {
                                    val limit = queryParameters.getOrFail("limit").toInt()
                                    val cursor = Instant.parse(queryParameters.getOrFail("cursor"))
                                    respondRes(service.resolveFavorites(user, limit, cursor))
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                    respond(HttpStatusCode.BadRequest)
                                }
                            }
                        }
                    }

                    post("/add") {
                        call.withUser { user ->
                            try {
                                var title = ""
                                var sizes = ""
                                val urls = mutableListOf<String>()

                                receiveMultipart().forEachPart { part ->
                                    when (part) {
                                        is PartData.FileItem -> {
                                            val url = service.write(part.provider())
                                            urls.add(url)
                                        }
                                        is PartData.FormItem -> {
                                            when (part.name) {
                                                "title" -> title = part.value
                                                "sizes" -> sizes = part.value
                                            }
                                        }
                                        else -> Unit
                                    }

                                    part.dispose()
                                }

                                service.add(user, title, sizes, urls)

                                respond(HttpStatusCode.OK)
                            } catch (e: Exception) {
                                e.printStackTrace()
                                respond(HttpStatusCode.BadRequest)
                            }
                        }
                    }

                    route("/comments") {
                        get {
                            call.run {
                                val limit = queryParameters.getOrFail("limit").toInt()
                                val cursor = Instant.parse(queryParameters.getOrFail("cursor"))
                                val postId = queryParameters.getOrFail("post_id")

                                respondRes(service.getComments(postId, limit, cursor))
                            }
                        }

                        post("/add") {
                            call.withUser { user ->
                                val request = receive<CommentRequest>()
                                respondRes(service.addComment(user, request.postId, request.text))
                            }
                        }
                    }
                }
            }
        }
    }
}