package io.tujh.posts

import common.Response
import common.success
import io.ktor.utils.io.*
import io.tujh.auth.database.Users
import io.tujh.common.query
import io.tujh.files.WriteImage
import io.tujh.models.Comment
import io.tujh.models.Post
import io.tujh.models.PostImage
import io.tujh.models.User
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import java.time.Instant
import java.util.*

class PostService {
    private val writeImage = WriteImage("posts")
    val folder = writeImage.folder

    suspend fun resolveAll(limit: Int, cursor: Instant) = success {
        Posts.resolvePosts(limit) { Posts.createdAt less cursor }
    }

    suspend fun resolveFor(user: User, limit: Int, cursor: Instant): Response<List<Post>> {
        val userId = UUID.fromString(user.id)
        return success {
            Posts.resolvePosts(limit) { (Posts.createdAt less cursor) and (Posts.authorId eq userId) }
        }
    }

    suspend fun resolveFavorites(user: User, limit: Int, cursor: Instant): Response<List<Post>> {
        val userUid = UUID.fromString(user.id)

        return success {
            query {
                Favorites
                    .join(Posts, JoinType.INNER, Favorites.postId, Posts.id) { Favorites.userId eq userUid }
                    .resolvePosts(limit, Favorites.dateAdded) { Posts.createdAt less cursor }
            }
        }
    }

    suspend fun write(source: ByteReadChannel): String = writeImage.original(source)!!

    suspend fun add(user: User, title: String, sizes: String, urls: List<String>) {
        val originalSizes = sizes.split(" ").map {
            val (w, h) = it.split(",").map(String::toInt)
            w to h
        }

        val postImages = originalSizes.zip(urls).map { (size, url) ->
            PostImage(url, size.first, size.second)
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

    suspend fun getComments(postId: String, limit: Int, cursor: Instant): Response<List<Comment>> = query {
        val postUid = UUID.fromString(postId)

        success {
            Comments
                .selectAll()
                .orderBy(Comments.createdAt, SortOrder.DESC)
                .where { (Comments.postId eq postUid) and (Comments.createdAt less cursor) }
                .limit(limit)
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
    }

    suspend fun addComment(user: User, idPost: String, text: String): Response<Comment> = query {
        val userId = UUID.fromString(user.id)
        val postUId = UUID.fromString(idPost)
        val date = Instant.now()

        Comments
            .insert {
                it[postId] = postUId
                it[authorId] = userId
                it[createdAt] = date
                it[Comments.text] = text
            }
            .let {
                success {
                    Comment(
                        id = it[Comments.id].toString(),
                        author = user,
                        createdAt = date.toString(),
                        text = it[Comments.text].toString()
                    )
                }
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