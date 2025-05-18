package io.tujh.auth.jwt

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.Payload
import io.tujh.auth.database.Users
import io.tujh.models.User
import io.tujh.common.query
import org.jetbrains.exposed.sql.selectAll
import java.util.UUID

object JwtInteractor {
    private val algorithm = Algorithm.HMAC512("zAP5MBA4B4Ijz0MZaS48")

    val verifier = JWT
        .require(algorithm)
        .withIssuer("ktor.io")
        .build()

    fun createWith(id: String) = JWT.create()
        .withSubject("Authentication")
        .withIssuer("ktor.io")
        .withClaim("id", id)
        .sign(algorithm)

    suspend fun getUser(payload: Payload): User? {
        val id = payload.getClaim("id").asString()?.let(UUID::fromString) ?: return null
        return query {
            Users.selectAll().where { Users.id eq id }.firstOrNull()?.let { user ->
                User(
                    id = user[Users.id].toString(),
                    avatar = user[Users.avatar],
                    name = user[Users.name],
                    email = user[Users.email]
                )
            }
        }
    }
}