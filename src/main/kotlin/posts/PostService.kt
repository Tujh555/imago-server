package io.tujh.posts

import common.Response
import io.ktor.http.*
import io.ktor.utils.io.*
import io.tujh.auth.database.Users
import io.tujh.common.query
import io.tujh.files.WriteImage
import io.tujh.models.Comment
import io.tujh.models.Post
import io.tujh.models.PostImage
import io.tujh.models.User
import kotlinx.coroutines.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.UUID
import kotlin.random.Random
import kotlin.system.measureTimeMillis

class PostService {
    private val writeImage = WriteImage("avatars")
    val folder = writeImage.folder

    suspend fun resolveAll(limit: Int, cursor: Instant) = Posts.resolvePosts(limit) { Posts.createdAt less cursor }

    suspend fun resolveFor(user: User, limit: Int, cursor: Instant): List<Post> {
        val userId = UUID.fromString(user.id)
        return Posts.resolvePosts(limit) { (Posts.createdAt less cursor) and (Posts.authorId eq userId) }
    }

    suspend fun resolveFavorites(user: User, limit: Int, cursor: Instant): List<Post> {
        val userId = UUID.fromString(user.id)

        return (Favorites innerJoin Posts innerJoin Users).resolvePosts(limit, Favorites.dateAdded) {
            (Favorites.userId eq userId) and (Posts.createdAt less cursor)
        }
    }

    suspend fun add(user: User, title: String, sizes: String, sources: List<ByteReadChannel>) {
        val files = coroutineScope {
            sources
                .map { source -> async { writeImage.original(source)!! } }
                .awaitAll()
        }
        val originalSizes = sizes.split(" ").map {
            val (w, h) = it.split(",").map(String::toInt)
            w to h
        }
        val postImages = originalSizes.mapIndexed { index, (width, height) ->
            val url = files[index]
            PostImage(url, width, height)
        }
        query {
            val userId = UUID.fromString(user.id)

            Posts.insert {
                it[authorId] = userId
                it[this.title] = title
                it[createdAt] = Instant.now()
                it[images] = postImages
            }
        }
    }

    suspend fun addToFavorite(user: User, postId: String): FavoriteResponse = query {
        val userId = UUID.fromString(user.id)
        val postUid = UUID.fromString(postId)
        val exists = Favorites
            .selectAll()
            .where { (Favorites.postId eq postUid) and (Favorites.userId eq userId) }
            .firstOrNull() != null

        if (exists) {
            Favorites.deleteWhere { (Favorites.postId eq postUid) and (Favorites.userId eq userId) }
        } else {
            Favorites.insert {
                it[Favorites.postId] = postUid
                it[Favorites.userId] = userId
                it[dateAdded] = Instant.now()
            }
        }

        FavoriteResponse(exists.not())
    }

    suspend fun checkInFavorites(user: User, postId: String): FavoriteResponse = query {
        val userId = UUID.fromString(user.id)
        val postUid = UUID.fromString(postId)

        val inFavorites = Favorites
            .selectAll()
            .where { (Favorites.postId eq postUid) and (Favorites.userId eq userId) }
            .firstOrNull() != null

        FavoriteResponse(inFavorites)
    }

    suspend fun getComments(postId: String, limit: Int, cursor: Instant): List<Comment> = query {
        val postUid = UUID.fromString(postId)

        Comments
            .selectAll()
            .orderBy(Comments.createdAt, SortOrder.DESC)
            .where { (Comments.postId eq postUid) and (Comments.createdAt less cursor) }
            .map { comment ->
                val author = Users
                    .selectAll()
                    .where { Users.id eq comment[Comments.authorId] }
                    .map { row ->
                        User(
                            id = row[Users.id].toString(),
                            avatar = row[Users.avatar],
                            name = row[Users.name],
                            email = row[Users.email]
                        )
                    }
                    .first()

                Comment(
                    id = comment[Comments.id].toString(),
                    author = author,
                    createdAt = comment[Comments.createdAt].toString(),
                    text = comment[Comments.text]
                )
            }
    }

    suspend fun addComment(user: User, idPost: String, text: String): Comment = query {
        val userId = UUID.fromString(user.id)
        val postUId = UUID.fromString(idPost)

        Comments
            .insert {
                it[postId] = postUId
                it[authorId] = userId
                it[createdAt] = Instant.now()
                it[Comments.text] = text
            }
            .let {
                Comment(
                    id = it[Comments.id].toString(),
                    author = user,
                    createdAt = it[Comments.createdAt].toString(),
                    text = it[Comments.text].toString()
                )
            }
    }

    private suspend fun FieldSet.resolvePosts(
        limit: Int,
        orderBy: Column<Instant> = Posts.createdAt,
        filter: SqlExpressionBuilder.() -> Op<Boolean>
    ) = query {
        selectAll()
            .where(filter)
            .orderBy(orderBy, SortOrder.DESC)
            .limit(limit)
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