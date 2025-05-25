package io.tujh.posts

import common.instant
import io.tujh.auth.database.Users
import io.tujh.common.images
import org.jetbrains.exposed.sql.Table

object Posts : Table() {
    val id = uuid("id").autoGenerate()
    val authorId = uuid("user_id") references Users.id
    val title = text("title")
    val createdAt = instant("created_at")
    val images = images("images")

    override val primaryKey = PrimaryKey(id)
}

object Comments : Table() {
    val id = uuid("id").autoGenerate()
    val postId = uuid("post_id") references Posts.id
    val authorId = uuid("author_id") references Users.id
    val createdAt = instant("created_at")
    val text = text("text")

    override val primaryKey = PrimaryKey(id)
}

object Favorites : Table() {
    val postId = uuid("post_id") references Posts.id
    val userId = uuid("user_id") references Users.id
    val dateAdded = instant("date_added")

    override val primaryKey = PrimaryKey(postId, userId)
}