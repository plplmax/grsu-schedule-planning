package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.ReferenceOption

object PairedSubjectsTable : IntIdTable() {
    val firstSubjectId = reference("firstSubjectId", SubjectsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val secondSubjectId = reference("secondSubjectId", SubjectsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val count = integer("count")
}
