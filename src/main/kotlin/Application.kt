package io.tujh

import io.ktor.server.application.*
import io.tujh.auth.database.Users
import io.tujh.configurator.DelegateConfigurator
import io.tujh.configurator.configurators
import io.tujh.posts.Comments
import io.tujh.posts.Favorites
import io.tujh.posts.Posts
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

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
        try {
            println("--> try to create db")
            SchemaUtils.create(Posts, Favorites, Comments, Users)
        } catch (e: Exception) {
            e.printStackTrace()
            println(e.message)
        }
    }
    DelegateConfigurator(this, configurators).configure()
}
