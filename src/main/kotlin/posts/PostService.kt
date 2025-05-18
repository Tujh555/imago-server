package io.tujh.posts

import io.tujh.auth.database.Users
import io.tujh.common.query
import io.tujh.models.Post
import io.tujh.models.User
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant
import java.util.UUID

class PostService {
    suspend fun resolveAll(limit: Int, cursor: Instant) = resolvePosts(limit) { Posts.createdAt greaterEq cursor }

    suspend fun resolveFor(user: User, limit: Int, cursor: Instant): List<Post> {
        val userId = UUID.fromString(user.id)
        return resolvePosts(limit) { (Posts.createdAt greaterEq cursor) and (Posts.authorId eq userId) }
    }

    suspend fun resolveFavorites(user: User, limit: Int, cursor: Instant): List<Post> {
        val userId = UUID.fromString(user.id)

        return query {
            (Favorites innerJoin Posts innerJoin Users)
                .selectAll()

            TODO()
        }
    }

    private suspend fun resolvePosts(limit: Int, filter: SqlExpressionBuilder.() -> Op<Boolean>) = query {
        Posts
            .selectAll()
            .limit(limit)
            .where(filter)
            .map { row ->
                Post(
                    id = row[Posts.id].toString(),
                    images = row[Posts.images],
                    title = row[Posts.title],
                    createdAt = row[Posts.createdAt].toString()
                )
            }
            .toList()
    }
}