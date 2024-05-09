package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object GroupsTable : IntIdTable() {
    val number = integer("number")
    val letter = char("letter")

    init {
        uniqueIndex(number, letter)
    }
}
