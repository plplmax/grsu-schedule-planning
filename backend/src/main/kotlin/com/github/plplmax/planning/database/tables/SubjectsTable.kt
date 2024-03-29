package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object SubjectsTable : IntIdTable() {
    val name = varchar("name", 64).uniqueIndex()
}