package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object RoomsTable : IntIdTable() {
    val name = varchar("name", 64).uniqueIndex()
    val capacity = integer("capacity").default(1)
}
