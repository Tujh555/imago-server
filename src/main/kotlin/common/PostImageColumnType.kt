package io.tujh.common

import com.google.gson.Gson
import io.tujh.models.PostImage
import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.ColumnType
import org.jetbrains.exposed.sql.Table
import kotlin.reflect.javaType
import kotlin.reflect.typeOf

@OptIn(ExperimentalStdlibApi::class)
private class PostImageColumnType : ColumnType<List<PostImage>>() {
    override fun sqlType() = "TEXT"

    override fun valueFromDB(value: Any): List<PostImage>? {
        if (value !is String) {
            return null
        }

        return gson.fromJson(value, type)
    }

    override fun notNullValueToDB(value: List<PostImage>): String = gson.toJson(value, type)

    companion object {
        private val gson by lazy { Gson() }
        private val type = typeOf<List<PostImage>>().javaType
    }
}

fun Table.images(name: String): Column<List<PostImage>> = registerColumn(name, PostImageColumnType())