package io.tujh.auth

import common.Response
import common._error
import common.success
import io.tujh.auth.database.Users
import io.tujh.auth.jwt.JwtInteractor
import io.tujh.auth.requests.AuthRequest
import io.tujh.auth.requests.AuthResponse
import io.tujh.common.query
import io.tujh.models.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt

class AuthService {
    suspend fun register(request: AuthRequest): Response<AuthResponse> = query {
        if (resolveUser(request.email) != null) {
            return@query _error(401)
        }
        val (user, token) = insertNew(request.email, request.password, request.name)
        success { AuthResponse(user, token) }
    }

    suspend fun login(request: AuthRequest): Response<AuthResponse> = query {
        val existing = resolveUser(request.email) ?: return@query _error(402)
        if (BCrypt.checkpw(request.password, existing[Users.password]).not()) {
            return@query _error(403)
        }
        val token = JwtInteractor.createWith(existing[Users.id].toString())
        val dto = with(existing) {
            User(
                id = get(Users.id).toString(),
                name = get(Users.name),
                avatar = get(Users.avatar),
                email = get(Users.email),
            )
        }
        success { AuthResponse(dto, token) }
    }


    private fun resolveUser(email: String) = Users
        .selectAll()
        .where { Users.email eq email }
        .firstOrNull()

    private fun insertNew(email: String, password: String, name: String): Pair<User, String> {
        val hashed = BCrypt.hashpw(password, BCrypt.gensalt())
        val user = Users.insert {
            it[Users.email] = email
            it[Users.password] = hashed
            it[Users.name] = name
        }
        val id = user[Users.id].toString()

        val token = JwtInteractor.createWith(id)
        val dto = User(
            id = id,
            avatar = null,
            name = name,
            email = email
        )

        return dto to token
    }
}