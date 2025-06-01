package io.tujh

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
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
    val url = "jdbc:postgresql://localhost:5433/erusinov"
    val user = "erusinov"
    val driver = "org.postgresql.Driver"
    val password = ""
    val datasource = createHikariDataSource(url, user, driver, password)
    val database = Database.connect(datasource)
    transaction(database) {
        SchemaUtils.create(Users, Posts, Comments, Favorites)
    }
    install(CallLogging) {
        level = Level.INFO
    }

    DelegateConfigurator (this, configurators).configure()
}

fun createHikariDataSource(
    url: String,
    user: String,
    driver: String,
    password: String
): HikariDataSource {
    val config = HikariConfig()
    config.driverClassName = driver
    config.jdbcUrl = url
    config.username = user
    config.password = password
    config.maximumPoolSize = 10
    config.isAutoCommit = false
    config.transactionIsolation = "TRANSACTION_REPEATABLE_READ" // Уровень изоляции
    config.validate()
    return HikariDataSource(config)
}