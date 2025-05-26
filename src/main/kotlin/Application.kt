package io.tujh

import io.ktor.server.application.*
import io.ktor.server.plugins.calllogging.*
import io.ktor.server.request.*
import io.tujh.auth.database.Users
import io.tujh.configurator.DelegateConfigurator
import io.tujh.configurator.configurators
import io.tujh.posts.Comments
import io.tujh.posts.Favorites
import io.tujh.posts.Posts
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.event.Level

fun main(args: Array<String>) {
    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    val database = Database.connect(
        url = "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1",
        user = "root",
        driver = "org.h2.Driver",
        password = "",
    )
    transaction(database) {
        SchemaUtils.create(Users, Posts, Comments, Favorites)
    }
    install(CallLogging) {
        level = Level.INFO
    }

    DelegateConfigurator (this, configurators).configure()
}
