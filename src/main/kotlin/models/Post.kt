package io.tujh.models

import com.google.gson.annotations.SerializedName

class Post(
    @SerializedName("id")
    val id: String,
    @SerializedName("images")
    val images: List<PostImage>,
    @SerializedName("title")
    val title: String,
    @SerializedName("created_at")
    val createdAt: String,
)

data class PostImage(
    @SerializedName("url")
    val url: String,
    @SerializedName("original_width")
    val originalWidth: Int,
    @SerializedName("original_height")
    val originalHeight: Int
)