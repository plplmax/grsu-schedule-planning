package com.github.plplmax.planning.database.tables

import org.jetbrains.exposed.sql.ReferenceOption
import org.jetbrains.exposed.sql.Table

object TeachersToSubjectsTable : Table() {
    val teacherId = reference("teacherId", TeachersTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)
    val subjectId = reference("subjectId", SubjectsTable, onDelete = ReferenceOption.CASCADE, onUpdate = ReferenceOption.CASCADE)

    override val primaryKey = PrimaryKey(teacherId, subjectId)
}
