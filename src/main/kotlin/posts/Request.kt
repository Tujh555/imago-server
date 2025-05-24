package io.tujh.posts

import com.google.gson.annotations.SerializedName

class RequestId(@SerializedName("id") val id: String)

class CommentRequest(
    @SerializedName("post_id")
    val postId: String,
    @SerializedName("text")
    val text: String
)