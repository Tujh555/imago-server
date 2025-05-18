package io.tujh.profile

import common.Response
import common._error
import common.success
import io.ktor.utils.io.*
import io.tujh.auth.database.Users
import io.tujh.common.query
import io.tujh.files.WriteImage
import io.tujh.models.User
import org.jetbrains.exposed.sql.update
import java.util.UUID

class ProfileService {
    private val writeImage = WriteImage("avatars")
    val folder = writeImage.folder

    suspend fun loadAvatar(user: User, source: ByteReadChannel): Response<AvatarUpdateResponse> {
        val url = writeImage.resized(source) ?: return _error(500)
        val id = UUID.fromString(user.id)
        query {
            Users.update(where = { Users.id eq id }) { it[avatar] = url }
        }

        return success { AvatarUpdateResponse(url) }
    }

    suspend fun updateName(user: User, newName: String) {
        val id = UUID.fromString(user.id)
        query { Users.update(where = { Users.id eq id }) { it[name] = newName } }
    }
}