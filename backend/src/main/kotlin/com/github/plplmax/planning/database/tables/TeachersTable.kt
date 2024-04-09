package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object TeachersTable : IntIdTable() {
    val firstname = varchar("firstname", 32)
    val lastname = varchar("lastname", 32)

    init {
        uniqueIndex(firstname, lastname)
    }
}
