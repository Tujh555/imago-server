package io.tujh.models

import com.google.gson.annotations.SerializedName

class Comment(
    @SerializedName("id")
    val id: String,
    @SerializedName("author")
    val author: User,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("text")
    val text: String
)