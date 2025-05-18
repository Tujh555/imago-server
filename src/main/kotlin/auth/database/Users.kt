package io.tujh.auth.database

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = uuid("id").autoGenerate()
    val email = text("email").uniqueIndex()
    val password = text("password")
    val name = text("name")
    val avatar = text("avatar").nullable().default(null)

    override val primaryKey = PrimaryKey(id)
}