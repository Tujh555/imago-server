package io.tujh.auth.requests

import com.google.gson.annotations.SerializedName
import io.tujh.models.User

data class AuthResponse(
    @SerializedName("user")
    val user: User,
    @SerializedName("token")
    val token: String
)