package io.tujh.common

import common.Response
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import io.tujh.models.User
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <T> queryCatching(block: suspend Transaction.() -> T): T? = runCatching { query(block) }.getOrNull()

suspend fun <T> query(block: suspend Transaction.() -> T) = newSuspendedTransaction(Dispatchers.IO, statement = block)

suspend inline fun <reified T : Any> RoutingCall.respondRes(response: Response<T>) {
    when (response) {
        is Response.Error -> respond(HttpStatusCode(response.error.code, response.error.message), response.error.message)
        is Response.Success<T> -> respond(response.data)
    }
}

suspend inline fun RoutingCall.withUser(block: RoutingCall.(User) -> Unit) {
    val user = authentication.principal<User>()

    if (user != null) {
        block(user)
    } else {
        respond(HttpStatusCode.Unauthorized)
    }
}

val ApplicationCall.user
    get() = authentication.principal<User>()