package io.tujh.models

import com.google.gson.annotations.SerializedName

class User(
    @SerializedName("id")
    val id: String,
    @SerializedName("avatar")
    val avatar: String?,
    @SerializedName("name")
    val name: String,
    @SerializedName("email")
    val email: String
)