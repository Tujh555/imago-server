package io.tujh.posts

import com.google.gson.annotations.SerializedName

class FavoriteResponse(
    @SerializedName("in_favorite")
    val inFavorite: Boolean
)